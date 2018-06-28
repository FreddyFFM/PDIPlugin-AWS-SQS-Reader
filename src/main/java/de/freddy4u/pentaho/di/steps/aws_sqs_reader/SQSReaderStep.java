/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package de.freddy4u.pentaho.di.steps.aws_sqs_reader;

import java.io.IOException;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.freddy4u.pentaho.di.steps.aws_sqs_reader.aws_sqs_reader.AWS_SQS_Reader;

/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
public class SQSReaderStep extends BaseStep implements StepInterface {
	
	private static Class<?> PKG = SQSReaderStepMeta.class; // for i18n purposes

	private TransMeta transMeta;
	private Trans trans;

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public SQSReaderStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
		this.transMeta = t;
		this.trans = dis;
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It's establishing the AWS Connection
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		SQSReaderStepMeta meta = (SQSReaderStepMeta) smi;
		SQSReaderStepData data = (SQSReaderStepData) sdi;
		
		data.aws_sqs = new AWS_SQS_Reader(smi, this.transMeta, this);
		if (!data.aws_sqs.getAWSConnection()) {
			setErrors(1);
			stopAll();
			setOutputDone();
			return false;
		}
		
		data.realSQSQueue = transMeta.environmentSubstitute(meta.getSqsQueue());
		
		data.realMessageIDFieldName = transMeta.environmentSubstitute(meta.gettFldMessageID());
		data.realMessageBodyFieldName = transMeta.environmentSubstitute(meta.gettFldMessageBody());
		data.realReceiptHandleFieldName = transMeta.environmentSubstitute(meta.gettFldReceiptHandle());
		data.realBodyMD5FieldName = transMeta.environmentSubstitute(meta.gettFldBodyMD5());
		data.realSNSMessageFieldName = transMeta.environmentSubstitute(meta.gettFldSNSMessage());
		
		try {
			data.realMaxMessages = Integer.valueOf(transMeta.environmentSubstitute(meta.gettFldMaxMessages()));
			if (data.realMaxMessages < 0) {
				throw new NumberFormatException("Max Messages value < 0");
			}
			
		} catch(NumberFormatException e) {
			
			logError (BaseMessages.getString( PKG, "SQSReader.Log.MaxMessagesNumber.ERROR" ));
			setErrors(1);
			stopAll();
			setOutputDone();
			return false;
		}

		return super.init(meta, data);
	}	
	
	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		SQSReaderStepMeta meta = (SQSReaderStepMeta) smi;
		SQSReaderStepData data = (SQSReaderStepData) sdi;		

		// the "first" flag is inherited from the base step implementation
		// it is used to guard some processing tasks, like figuring out field indexes
		// in the row structure that only need to be done once
		
		if (first) {
			first = false;
			// clone the input row structure and place it in our data object
			data.outputRowMeta = new RowMeta();
			// use meta.getFields() to change it, so it reflects the output row structure 
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
			
			logBasic ("Start reading from queue");
		}
		
		if (Utils.isEmpty( data.realMessageIDFieldName ) || Utils.isEmpty( data.realMessageBodyFieldName)) {
			logError ( BaseMessages.getString( PKG, "SQSReader.Log.NoMessageFields.ERROR" ) );
			throw new KettleException( BaseMessages.getString( PKG, "SQSReader.Log.NoMessageFields.ERROR" ) );
		}
		
		// Catch Messages from Queue
		if ((data.realMaxMessages == 0) || (getLinesInput() < data.realMaxMessages)) {
			
			int numMessages = (int) ((data.realMaxMessages == 0) ? 10 : (data.realMaxMessages - getLinesInput()));
		
			List<Message> messages = data.aws_sqs.readMessages(data.realSQSQueue, numMessages, trans.isPreview());
			
			if (!messages.isEmpty()) {
				
				for (Message m : messages) {
					
					Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
					
					int idxMessageIdField = data.outputRowMeta.indexOfValue( data.realMessageIDFieldName);
					if (idxMessageIdField >= 0) {
						outputRow[idxMessageIdField] = m.getMessageId();
					}
					
					int idxMessageBodyField = data.outputRowMeta.indexOfValue( data.realMessageBodyFieldName);
					if (idxMessageBodyField >= 0) {
						outputRow[idxMessageBodyField] = m.getBody();
					}
					
					int idxReceiptHandleField = data.outputRowMeta.indexOfValue( data.realReceiptHandleFieldName);
					if (idxReceiptHandleField >= 0) {
						outputRow[idxReceiptHandleField] = m.getReceiptHandle();
					}
					
					int idxBodyMD5Field = data.outputRowMeta.indexOfValue( data.realBodyMD5FieldName);
					if (idxBodyMD5Field >= 0) {
						outputRow[idxBodyMD5Field] = m.getMD5OfBody();
					}
					
					int idxSNSMessageField = data.outputRowMeta.indexOfValue( data.realSNSMessageFieldName);
					if (idxSNSMessageField >= 0) {
						outputRow[idxSNSMessageField] = getSNSMessageContent(m.getBody());
					}
					
					putRow(data.outputRowMeta, outputRow);	            
					incrementLinesInput();
		        }
				
			} else {
				
				setOutputDone();
				logBasic ("Finished reading from queue");
				
				return false;				
				
			}
			
		} else {
						
			setOutputDone();
			logBasic ("Finished reading from queue");
			
			return false;
		}
		

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}		

		// indicate that processRow() should be called again
		return true;
	}	

	private String getSNSMessageContent(String body) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = new JsonFactory();
			factory.setCodec(mapper);
			JsonParser parser = factory.createParser(body);		
			JsonNode jsonNode = parser.readValueAsTree();
			JsonNode statusNode = jsonNode.get("Message");
			return statusNode.textValue();
			
		} catch (JsonParseException e) {
			logError(e.getMessage());
			return "";
			
		} catch (IOException e) {
			logError(e.getMessage());
			return "";
			
		}		
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		SQSReaderStepMeta meta = (SQSReaderStepMeta) smi;
		SQSReaderStepData data = (SQSReaderStepData) sdi;
		
		data.aws_sqs.disconnectAWSConnection();
		
		super.dispose(meta, data);
	}
	
	

}
