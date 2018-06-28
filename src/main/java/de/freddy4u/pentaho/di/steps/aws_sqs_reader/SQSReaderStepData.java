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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import de.freddy4u.pentaho.di.steps.aws_sqs_reader.aws_sqs_reader.AWS_SQS_Reader;


/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
public class SQSReaderStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public AWS_SQS_Reader aws_sqs;
	public String realMessageIDFieldName;
	public String realMessageBodyFieldName;
	public String realReceiptHandleFieldName;
	public String realBodyMD5FieldName;
	public String realSQSQueue;
	public Integer realMaxMessages;
	public String realSNSMessageFieldName;
	
    public SQSReaderStepData()
	{
		super();
	}
}
	
