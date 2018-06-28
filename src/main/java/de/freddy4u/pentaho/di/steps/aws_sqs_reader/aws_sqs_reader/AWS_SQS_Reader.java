package de.freddy4u.pentaho.di.steps.aws_sqs_reader.aws_sqs_reader;

import java.util.List;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import de.freddy4u.pentaho.di.steps.aws_sqs_reader.SQSReaderStepMeta;


/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SQS-Reader
 *
 */
public class AWS_SQS_Reader {
	
	private AmazonSQSClient sqsClient;	
	private SQSReaderStepMeta meta;
	private String awsKey;
	private String awsRegion;
	private String awsKeySecret;
	private BaseStep baseStep;
	private TransMeta transMeta;
	private String awsCredChain;
	private String deleteMessage;
	
	/**
	 * 
	 * Constructor for new AWS SQS Object
	 * 
	 * @param smi	StepMetaInterface
	 * @param t		TransMeta
	 * @param bst	BaseStep
	 */
	public AWS_SQS_Reader(StepMetaInterface smi, TransMeta t, BaseStep bst) {
		
		this.meta = (SQSReaderStepMeta) smi;
		this.baseStep = (BaseStep) bst;
		this.transMeta = t;
		
		this.awsCredChain = transMeta.environmentSubstitute(meta.getAwsCredChain());
		this.awsKey = transMeta.environmentSubstitute(meta.getAWSKey());
		this.awsKeySecret = transMeta.environmentSubstitute(meta.getAWSKeySecret());
		this.awsRegion = transMeta.environmentSubstitute(meta.getAWSRegion());
		this.deleteMessage = transMeta.environmentSubstitute(meta.gettFldMessageDelete());
	}
	
	/**
	 * 
	 * Establishing new Connection to Amazon Webservices
	 * 
	 * @return	true on successfull connection
	 */
	public boolean getAWSConnection() {
		try {
			baseStep.logBasic("Starting connection to AWS SQS");
			
			if (this.awsCredChain.equalsIgnoreCase("N")) { 
				BasicAWSCredentials awsCreds = new BasicAWSCredentials(this.awsKey, this.awsKeySecret);
				sqsClient = (AmazonSQSClient) AmazonSQSClientBuilder.standard()
						.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
						.withRegion(this.awsRegion)
						.build();
				
				baseStep.logBasic("Connected to SQS in Region " + this.awsRegion + " with API-Key >>" + this.awsKey + "<<");
				
			} else {
				AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
				sqsClient = (AmazonSQSClient) AmazonSQSClientBuilder.standard()
						.withCredentials(provider)
						.build();
				
				baseStep.logBasic("Connected to SQS with provided Credentials Chain");
			}
			return true;
			
		} catch (AmazonClientException e) {			
			baseStep.logError(e.getMessage());
			
		} catch (Exception e) {			
			baseStep.logError(e.getMessage());
		}		
		
		return false;
	}
	
	
	/**
	 * Disconnects from AWS
	 */
	public void disconnectAWSConnection() {
		try {
			sqsClient.shutdown();
			
			baseStep.logBasic("Disconnected from SQS");
	
		} catch (AmazonClientException e) {
			baseStep.logError(e.getMessage());
			baseStep.setErrors(1);
		}
		
	}
	
	/**
	 * 
	 * @param queueURL
	 * @param numMsgs
	 * @param isPreview
	 * @return
	 * @throws AmazonSQSException
	 */
	public List<Message> readMessages(String queueURL, int numMsgs, boolean isPreview) throws AmazonSQSException {	
		
		int numMessages = (numMsgs > 10) ? 10 : numMsgs;
		
		try {
			
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURL);
			receiveMessageRequest.setMaxNumberOfMessages(numMessages);
			List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
			
			baseStep.logBasic(messages.size() + " Message(s) retrieved from queue");
			
			if (this.deleteMessage.equalsIgnoreCase("Y") && !isPreview) {				
				
				for (Message m : messages) {
		            sqsClient.deleteMessage(queueURL, m.getReceiptHandle());
		        } 				
				baseStep.logBasic(messages.size() + " Message(s) deleted from queue");
			}		
			
			return messages;
			
		} catch (AmazonSQSException e) {			
			throw e;
			
		}
	}

}
