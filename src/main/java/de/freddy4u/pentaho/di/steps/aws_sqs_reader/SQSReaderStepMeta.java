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

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;



/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
@Step(	
		id = "AwsSQSreader",
		image = "de/freddy4u/pentaho/di/steps/aws_sqs_reader/resources/aws_sqs_reader.svg",
		i18nPackageName="de.freddy4u.pentaho.di.steps.aws_sqs_reader",
		name="SQSReaderStep.Name",
		description = "SQSReaderStep.TooltipDesc",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.Input",
		documentationUrl="https://freddyffm.github.io/PDIPlugin-AWS-SQS-Reader/"
)
public class SQSReaderStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = SQSReaderStepMeta.class; // for i18n purposes
	
	private String awsCredChain;
	private String awsKey;
	private String awsKeySecret;
	private String awsRegion;
	private String sqsQueue;
	private String tFldMessageID;
	private String tFldMessageBody;
	private String tFldReceiptHandle;
	private String tFldBodyMD5;
	private String tFldSNSMessage;
	private String tFldMessageDelete;
	private String tFldMaxMessages;

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public SQSReaderStepMeta() {
		super(); 
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new SQSReaderStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new SQSReaderStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new SQSReaderStepData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		// outputField = "demo_field";
	}
	
	public String getAwsCredChain() {
		return awsCredChain;
	}

	public void setAwsCredChain(String awsCredChain) {
		this.awsCredChain = awsCredChain == null ? "N" : awsCredChain;
	}

	public String getAWSKey() {
		return awsKey == null ? "" : awsKey;
	}

	public void setAWSKey(String aws_key) {
		this.awsKey = aws_key;
	}
	
	public String getAWSKeySecret() {
		return awsKeySecret == null ? "" : awsKeySecret;
	}

	public void setAWSKeySecret(String awsKeySecret) {
		this.awsKeySecret = awsKeySecret;
	}

	public String getAWSRegion() {
		return awsRegion == null ? "" : awsRegion;
	}

	public void setAWSRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public String getSqsQueue() {
		return sqsQueue == null ? "" : sqsQueue;
	}

	public void setSqsQueue(String sqsQueue) {
		this.sqsQueue = sqsQueue;
	}

	public String gettFldMessageID() {
		return tFldMessageID == null ? "" : tFldMessageID;
	}

	public void settFldMessageID(String tFldMessageID) {
		this.tFldMessageID = tFldMessageID;
	}

	public String gettFldMessageBody() {
		return tFldMessageBody == null ? "" : tFldMessageBody;
	}

	public void settFldMessageBody(String tFldMessageBody) {
		this.tFldMessageBody = tFldMessageBody;
	}

	public String gettFldReceiptHandle() {
		return tFldReceiptHandle == null ? "" : tFldReceiptHandle;
	}

	public void settFldReceiptHandle(String tFldReceiptHandle) {
		this.tFldReceiptHandle = tFldReceiptHandle;
	}

	public String gettFldBodyMD5() {
		return tFldBodyMD5 == null ? "" : tFldBodyMD5;
	}

	public void settFldBodyMD5(String tFldBodyMD5) {
		this.tFldBodyMD5 = tFldBodyMD5;
	}

	public String gettFldSNSMessage() {
		return tFldSNSMessage == null ? "" : tFldSNSMessage;
	}

	public void settFldSNSMessage(String tFldSNSMessage) {
		this.tFldSNSMessage = tFldSNSMessage;
	}

	public String gettFldMessageDelete() {
		return tFldMessageDelete == null ? "N" : tFldMessageDelete;
	}

	public void settFldMessageDelete(String tFldMessageDelete) {
		this.tFldMessageDelete = tFldMessageDelete;
	}

	public String gettFldMaxMessages() {
		return tFldMaxMessages == null ? "0" : tFldMaxMessages;
	}

	public void settFldMaxMessages(String tFldMaxMessages) {
		this.tFldMaxMessages = tFldMaxMessages;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		
		// only one field to serialize
		String xml = "";
		xml += XMLHandler.addTagValue("AWSCredChain", awsCredChain);
		xml += XMLHandler.addTagValue("AWSKey", awsKey);
		xml += XMLHandler.addTagValue("AWSKeySecret", awsKeySecret);
		xml += XMLHandler.addTagValue("AWSRegion", awsRegion);
		xml += XMLHandler.addTagValue("SQSQueue", sqsQueue);
		xml += XMLHandler.addTagValue("FieldMessageID", tFldMessageID);
		xml += XMLHandler.addTagValue("FieldMessageBody", tFldMessageBody);
		xml += XMLHandler.addTagValue("FieldReceiptHandle", tFldReceiptHandle);
		xml += XMLHandler.addTagValue("FieldBodyMD5", tFldBodyMD5);
		xml += XMLHandler.addTagValue("FieldSNSMessage", tFldSNSMessage);
		xml += XMLHandler.addTagValue("FieldMessageDelete", tFldMessageDelete);
		xml += XMLHandler.addTagValue("FieldMaxMessages", tFldMaxMessages);
		return xml;
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param metaStore the metaStore to optionally read from
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {

		try {
			setAwsCredChain(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSCredChain")));
			setAWSKey(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSKey")));
			setAWSKeySecret(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSKeySecret")));
			setAWSRegion(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSRegion")));
			setSqsQueue(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "SQSQueue")));
			settFldMessageID(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMessageID")));
			settFldMessageBody(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMessageBody")));
			settFldReceiptHandle(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldReceiptHandle")));
			settFldBodyMD5(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldBodyMD5")));
			settFldSNSMessage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldSNSMessage")));
			settFldMessageDelete(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMessageDelete")));
			settFldMaxMessages(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMaxMessages")));
		} catch (Exception e) {
			throw new KettleXMLException("Demo plugin unable to read step info from XML node", e);
		}

	}	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param metaStore				the metaStore to optionally write to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		
		try{
			rep.saveStepAttribute(id_transformation, id_step, "AWSCredChain", awsCredChain); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "AWSKey", awsKey); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "AWSKeySecret", awsKeySecret); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "AWSRegion", awsRegion); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "SQSQueue", sqsQueue); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMessageID", tFldMessageID); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMessageBody", tFldMessageBody); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldReceiptHandle", tFldReceiptHandle); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldBodyMD5", tFldBodyMD5); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldSNSMessage", tFldSNSMessage); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMessageDelete", tFldMessageDelete); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMaxMessages", tFldMaxMessages); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param metaStore	the metaStore to optionally read from
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException  {
		
		try{
			awsCredChain  = rep.getStepAttributeString(id_step, "AWSCredChain"); //$NON-NLS-1$
			awsKey  = rep.getStepAttributeString(id_step, "AWSKey"); //$NON-NLS-1$
			awsKeySecret  = rep.getStepAttributeString(id_step, "AWSKeySecret"); //$NON-NLS-1$
			awsRegion  = rep.getStepAttributeString(id_step, "AWSRegion"); //$NON-NLS-1$			
			sqsQueue  = rep.getStepAttributeString(id_step, "SQSQueue"); //$NON-NLS-1$
			tFldMessageID  = rep.getStepAttributeString(id_step, "FieldMessageID"); //$NON-NLS-1$
			tFldMessageBody  = rep.getStepAttributeString(id_step, "FieldMessageBody"); //$NON-NLS-1$
			tFldReceiptHandle  = rep.getStepAttributeString(id_step, "FieldReceiptHandle"); //$NON-NLS-1$
			tFldBodyMD5  = rep.getStepAttributeString(id_step, "FieldBodyMD5"); //$NON-NLS-1$
			tFldSNSMessage  = rep.getStepAttributeString(id_step, "FieldSNSMessage"); //$NON-NLS-1$
			tFldMessageDelete  = rep.getStepAttributeString(id_step, "FieldMessageDelete"); //$NON-NLS-1$
			tFldMaxMessages  = rep.getStepAttributeString(id_step, "FieldMaxMessages"); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param inputRowMeta		the row structure coming in to the step
	 * @param name 				the name of the step making the changes
	 * @param info				row structures of any info steps coming in
	 * @param nextStep			the description of a step this step is passing rows to
	 * @param space				the variable space for resolving variables
	 * @param repository		the repository instance optionally read from
	 * @param metaStore			the metaStore to optionally read from
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException{

		/*
		 * This implementation appends the outputField to the row-stream
		 */
		
		try {
		
			if (tFldMessageID != null && !tFldMessageID.equals("")) {
				String realMessageIDFieldName = space.environmentSubstitute(tFldMessageID);
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(realMessageIDFieldName, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(realMessageIDFieldName.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
			if (tFldMessageBody != null && !tFldMessageBody.equals("")) {
				String realMessageBodyFieldName = space.environmentSubstitute(tFldMessageBody);
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(realMessageBodyFieldName, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(realMessageBodyFieldName.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
			if (tFldReceiptHandle != null && !tFldReceiptHandle.equals("")) {
				String realReceiptHandleFieldName = space.environmentSubstitute(tFldReceiptHandle);
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(realReceiptHandleFieldName, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(realReceiptHandleFieldName.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
			if (tFldBodyMD5 != null && !tFldBodyMD5.equals("")) {
				String realBodyMD5FieldName = space.environmentSubstitute(tFldBodyMD5);
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(realBodyMD5FieldName, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(realBodyMD5FieldName.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
			if (tFldSNSMessage != null && !tFldSNSMessage.equals("")) {
				String realSNSMessageFieldName = space.environmentSubstitute(tFldSNSMessage);
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(realSNSMessageFieldName, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(realSNSMessageFieldName.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
		} catch (KettlePluginException e) {
			logBasic(e.getMessage());
			throw new KettleStepException( e );
		}
		
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 *   @param metaStore	metaStore to optionally read from
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)  {
		
		CheckResult cr;					
		
		// Check for Credentials
		if ((getAwsCredChain() == "N") && (getAWSKey().isEmpty() || getAWSKeySecret().isEmpty() || getAWSRegion().isEmpty())) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SQSReader.CheckResult.AWSCredentials.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SQSReader.CheckResult.AWSCredentials.OK"), stepMeta);
			remarks.add(cr);	
		}	
		
		// Check for SQS Queue URL
		if (getSqsQueue().isEmpty()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SQSReader.CheckResult.SQSQueue.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SQSReader.CheckResult.SQSQueue.OK"), stepMeta);
			remarks.add(cr);
		}
		
		// Check for MessageID
		if (gettFldMessageID().isEmpty()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SQSReader.CheckResult.MessageID.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SQSReader.CheckResult.MessageID.OK"), stepMeta);
			remarks.add(cr);
		}	
		
		// Check for MessageBody
		if (gettFldMessageBody().isEmpty()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SQSReader.CheckResult.MessageBody.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SQSReader.CheckResult.MessageBody.OK"), stepMeta);
			remarks.add(cr);
		}    	
	}
	
	public boolean supportsErrorHandling() {
		return true;
	}
	
}
