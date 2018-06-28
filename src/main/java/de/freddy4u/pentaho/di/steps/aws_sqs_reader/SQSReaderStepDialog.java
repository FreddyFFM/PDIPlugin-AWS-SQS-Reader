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

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQS;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;


/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SQS-Reader
 *
 */
public class SQSReaderStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = SQSReaderStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private SQSReaderStepMeta meta;

	// text field holding the name of the field to add to the row stream
	private CTabFolder tabFolder;
	private CTabItem tbtmSettings;
	private ScrolledComposite scrlSettingsComp;
	private Composite settingsComp;
	private Label lblAWSCredChain;
	private ComboVar tAWSCredChain;
	private Label lblAWSKey;
	private TextVar tAWSKey;
	private Label lblAWSKeySecret;
	private PasswordTextVar tAWSKeySecret;
	private Label lblAWSRegion;
	private ComboVar tAWSRegion;
	private CTabItem tbtmReaderOutput;
	private ScrolledComposite scrlreaderOutputComp;
	private Composite readerOutputComp;
	private Label lblMessageID;
	private TextVar tMessageID;
	private Label lblMessageBody;
	private TextVar tMessageBody;
	private Label lblReceiptHandle;
	private TextVar tReceiptHandle;
	private Label lblBodyMD5;
	private TextVar tBodyMD5;
	private Label lblMessageDelete;
	private ComboVar tMessageDelete;
	private Label lblSQSQueue;
	private TextVar tSQSQueue;
	private Label lblDevInfo;
	private Group grpOutputField;
	private Group grpOutputSettings;
	private Label lblMaxMessages;
	private TextVar tMaxMessages;
	private Label lblSNSMessage;
	private TextVar tSNSMessage;


	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public SQSReaderStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (SQSReaderStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SQSReader.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		// ------------------------------------------------------- //
		// DEVELOPER INFO //
		// ------------------------------------------------------- //
		
		lblDevInfo = new Label(shell, SWT.RIGHT);
		props.setLook(lblDevInfo);
		FormData fdDevInfo = new FormData();
		fdDevInfo.bottom = new FormAttachment(100, -margin);
		fdDevInfo.right = new FormAttachment(100, -margin);

		lblDevInfo.setLayoutData(fdDevInfo);
		lblDevInfo.setText(BaseMessages.getString(PKG, "SQSReaderStep.Developer.PopUp.Title"));
		lblDevInfo.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText(BaseMessages.getString(PKG, "SQSReaderStep.Developer.PopUp.Title"));
				dialog.setMessage(BaseMessages.getString(PKG, "SQSReaderStep.Developer.PopUp.Label"));

				// open dialog and await user selection
				dialog.open();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}

			@Override
			public void mouseDown(MouseEvent arg0) {

			}

		});
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// ------------------------------------------------------- //
		// TABULATOREN START //
		// ------------------------------------------------------- //

		// TABS - ANFANG
		tabFolder = new CTabFolder(shell, SWT.BORDER);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.right = new FormAttachment(100, 0);
		fd_tabFolder.top = new FormAttachment(wStepname, margin);
		fd_tabFolder.left = new FormAttachment(0, 0);
		fd_tabFolder.bottom = new FormAttachment(100, -50);
		tabFolder.setLayoutData(fd_tabFolder);
		props.setLook(tabFolder);

		// ------------------------------------------------------- //
		// - TAB Settings START //
		// ------------------------------------------------------- //

		// Settings-TAB - ANFANG
		tbtmSettings = new CTabItem(tabFolder, SWT.NONE);
		tbtmSettings.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.Title"));

		scrlSettingsComp = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		scrlSettingsComp.setLayout(new FillLayout());
		props.setLook(scrlSettingsComp);

		settingsComp = new Composite(scrlSettingsComp, SWT.NONE);
		props.setLook(settingsComp);

		FormLayout settingsLayout = new FormLayout();
		settingsLayout.marginWidth = 3;
		settingsLayout.marginHeight = 3;
		settingsComp.setLayout(settingsLayout);
		
		// Use AWS Credentials Provider Chain
		// Credentials Chain
		lblAWSCredChain = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSCredChain);
		FormData fd_lblAWSCredChain = new FormData();
		fd_lblAWSCredChain.left = new FormAttachment(0, 0);
		fd_lblAWSCredChain.top = new FormAttachment(0, margin);
		fd_lblAWSCredChain.right = new FormAttachment(middle, -margin);
		lblAWSCredChain.setLayoutData(fd_lblAWSCredChain);
		lblAWSCredChain.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSCredChain.Label"));
		
		tAWSCredChain = new ComboVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSCredChain);
		FormData fd_tAWSCredChain = new FormData();
		fd_tAWSCredChain.top = new FormAttachment(0, margin);
		fd_tAWSCredChain.left = new FormAttachment(middle, 0);
		fd_tAWSCredChain.right = new FormAttachment(100, 0);
		tAWSCredChain.setLayoutData(fd_tAWSCredChain);
		tAWSCredChain.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSCredChain.Tooltip"));		
		tAWSCredChain.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				changeCredentialChainSelection();				
			}
		});
		
		// AWS Key
		lblAWSKey = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSKey);
		FormData fd_lblAWSKey = new FormData();
		fd_lblAWSKey.left = new FormAttachment(0, 0);
		fd_lblAWSKey.top = new FormAttachment(tAWSCredChain, margin);
		fd_lblAWSKey.right = new FormAttachment(middle, -margin);
		lblAWSKey.setLayoutData(fd_lblAWSKey);
		lblAWSKey.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSKey.Label"));

		tAWSKey = new TextVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSKey);
		FormData fd_tAWSKey = new FormData();
		fd_tAWSKey.top = new FormAttachment(tAWSCredChain, margin);
		fd_tAWSKey.left = new FormAttachment(middle, 0);
		fd_tAWSKey.right = new FormAttachment(100, 0);
		tAWSKey.setLayoutData(fd_tAWSKey);
		tAWSKey.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSKey.Tooltip"));

		// AWS Key Secret
		lblAWSKeySecret = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSKeySecret);
		FormData fd_lblAWSKeySecret = new FormData();
		fd_lblAWSKeySecret.left = new FormAttachment(0, 0);
		fd_lblAWSKeySecret.top = new FormAttachment(tAWSKey, margin);
		fd_lblAWSKeySecret.right = new FormAttachment(middle, -margin);
		lblAWSKeySecret.setLayoutData(fd_lblAWSKeySecret);
		lblAWSKeySecret.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSKeySecret.Label"));

		tAWSKeySecret = new PasswordTextVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSKeySecret);
		FormData fd_tAWSKeySecret = new FormData();
		fd_tAWSKeySecret.top = new FormAttachment(tAWSKey, margin);
		fd_tAWSKeySecret.left = new FormAttachment(middle, 0);
		fd_tAWSKeySecret.right = new FormAttachment(100, 0);
		tAWSKeySecret.setLayoutData(fd_tAWSKeySecret);
		tAWSKeySecret.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSKeySecret.Tooltip"));
		
		// AWS Region
		lblAWSRegion = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSRegion);
		FormData fd_lblAWSRegion = new FormData();
		fd_lblAWSRegion.left = new FormAttachment(0, 0);
		fd_lblAWSRegion.top = new FormAttachment(tAWSKeySecret, margin);
		fd_lblAWSRegion.right = new FormAttachment(middle, -margin);
		lblAWSRegion.setLayoutData(fd_lblAWSRegion);
		lblAWSRegion.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSRegion.Label"));

		tAWSRegion = new ComboVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(tAWSRegion);
		FormData fd_tAWSRegion = new FormData();
		fd_tAWSRegion.top = new FormAttachment(tAWSKeySecret, margin);
		fd_tAWSRegion.left = new FormAttachment(middle, 0);
		fd_tAWSRegion.right = new FormAttachment(100, 0);
		tAWSRegion.setLayoutData(fd_tAWSRegion);
		tAWSRegion
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.AWSRegion.Tooltip"));
		populateAWSRegion(tAWSRegion);
		
		// SQS Queue
		lblSQSQueue = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblSQSQueue);
		FormData fd_lblSQSQueue = new FormData();
		fd_lblSQSQueue.left = new FormAttachment(0, 0);
		fd_lblSQSQueue.top = new FormAttachment(tAWSRegion, margin);
		fd_lblSQSQueue.right = new FormAttachment(middle, -margin);
		lblSQSQueue.setLayoutData(fd_lblSQSQueue);
		lblSQSQueue.setText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.SQSQueue.Label"));

		tSQSQueue = new TextVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(tSQSQueue);
		FormData fd_tSQSQueue = new FormData();
		fd_tSQSQueue.top = new FormAttachment(tAWSRegion, margin);
		fd_tSQSQueue.left = new FormAttachment(middle, 0);
		fd_tSQSQueue.right = new FormAttachment(100, 0);
		tSQSQueue.setLayoutData(fd_tSQSQueue);
		tSQSQueue
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.Settings.SQSQueue.Tooltip"));
				
		Control[] queueTabList = new Control[] { tAWSCredChain, tAWSKey, tAWSKeySecret, tAWSRegion };
		settingsComp.setTabList(queueTabList);

		settingsComp.pack();
		Rectangle settingsBounds = settingsComp.getBounds();

		scrlSettingsComp.setContent(settingsComp);
		scrlSettingsComp.setExpandHorizontal(true);
		scrlSettingsComp.setExpandVertical(true);
		scrlSettingsComp.setMinWidth(settingsBounds.width);
		scrlSettingsComp.setMinHeight(settingsBounds.height);
		// Settings-TAB - ENDE
		
		
		// ------------------------------------------------------- //
		// - TAB Output START //
		// ------------------------------------------------------- //

		// Output-TAB - ANFANG
		tbtmReaderOutput = new CTabItem(tabFolder, SWT.NONE);
		tbtmReaderOutput.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.Title"));

		scrlreaderOutputComp = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		scrlreaderOutputComp.setLayout(new FillLayout());
		props.setLook(scrlreaderOutputComp);

		readerOutputComp = new Composite(scrlreaderOutputComp, SWT.NONE);
		props.setLook(readerOutputComp);

		FormLayout ReaderOutputLayout = new FormLayout();
		ReaderOutputLayout.marginWidth = 3;
		ReaderOutputLayout.marginHeight = 3;
		readerOutputComp.setLayout(ReaderOutputLayout);
		
		// ------------------------------------------------------- //
		// --- GROUP Output Settings START //
		// ------------------------------------------------------- //
		
		grpOutputSettings = new Group(readerOutputComp, SWT.SHADOW_NONE);
		props.setLook(grpOutputSettings);
		grpOutputSettings.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.OutputSettings.GroupTitle"));
		FormData fd_grpOutputSettings = new FormData();
		fd_grpOutputSettings.top = new FormAttachment(0, margin);
		fd_grpOutputSettings.left = new FormAttachment(0, margin);
		fd_grpOutputSettings.right = new FormAttachment(100, -margin);
		fd_grpOutputSettings.bottom = new FormAttachment(40, -margin);
		grpOutputSettings.setLayoutData(fd_grpOutputSettings);

		FormLayout outputSettingsLayout = new FormLayout();
		outputSettingsLayout.marginWidth = 10;
		outputSettingsLayout.marginHeight = 10;
		grpOutputSettings.setLayout(outputSettingsLayout);
		
		// FELDER
		// Message Deletion
		lblMessageDelete = new Label(grpOutputSettings, SWT.RIGHT);
		props.setLook(lblMessageDelete);
		FormData fd_lblMessageDelete = new FormData();
		fd_lblMessageDelete.left = new FormAttachment(0, 0);
		fd_lblMessageDelete.top = new FormAttachment(0, margin);
		fd_lblMessageDelete.right = new FormAttachment(middle, -margin);
		lblMessageDelete.setLayoutData(fd_lblMessageDelete);
		lblMessageDelete.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageDelete.Label"));
		
		tMessageDelete = new ComboVar(transMeta, grpOutputSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tMessageDelete);
		FormData fd_tMessageDelete = new FormData();
		fd_tMessageDelete.top = new FormAttachment(0, margin);
		fd_tMessageDelete.left = new FormAttachment(middle, 0);
		fd_tMessageDelete.right = new FormAttachment(100, 0);
		tMessageDelete.setLayoutData(fd_tMessageDelete);
		tMessageDelete.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageDelete.Tooltip"));
		
		// Max Messages
		lblMaxMessages = new Label(grpOutputSettings, SWT.RIGHT);
		props.setLook(lblMaxMessages);
		FormData fd_lblMaxMessages = new FormData();
		fd_lblMaxMessages.left = new FormAttachment(0, 0);
		fd_lblMaxMessages.top = new FormAttachment(tMessageDelete, margin);
		fd_lblMaxMessages.right = new FormAttachment(middle, -margin);
		lblMaxMessages.setLayoutData(fd_lblMaxMessages);
		lblMaxMessages.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MaxMessages.Label"));
		
		tMaxMessages = new TextVar(transMeta, grpOutputSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tMaxMessages);
		FormData fd_tMaxMessages = new FormData();
		fd_tMaxMessages.top = new FormAttachment(tMessageDelete, margin);
		fd_tMaxMessages.left = new FormAttachment(middle, 0);
		fd_tMaxMessages.right = new FormAttachment(100, 0);
		tMaxMessages.setLayoutData(fd_tMaxMessages);
		tMaxMessages.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MaxMessages.Tooltip"));
		
		Control[] readerSettingsTabList = new Control[] { tMessageDelete, tMaxMessages };
		grpOutputSettings.setTabList(readerSettingsTabList);	
		
		// ------------------------------------------------------- //
		// --- GROUP Output Fields START //
		// ------------------------------------------------------- //
		
		grpOutputField = new Group(readerOutputComp, SWT.SHADOW_NONE);
		props.setLook(grpOutputField);
		grpOutputField.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.OutputFields.GroupTitle"));
		FormData fd_grpOutputField = new FormData();
		fd_grpOutputField.top = new FormAttachment(40, margin);
		fd_grpOutputField.left = new FormAttachment(0, margin);
		fd_grpOutputField.right = new FormAttachment(100, -margin);
		fd_grpOutputField.bottom = new FormAttachment(100, -margin);
		grpOutputField.setLayoutData(fd_grpOutputField);

		FormLayout outputFieldsLayout = new FormLayout();
		outputFieldsLayout.marginWidth = 10;
		outputFieldsLayout.marginHeight = 10;
		grpOutputField.setLayout(outputFieldsLayout);
		
		// FELDER
		// MessageID
		lblMessageID = new Label(grpOutputField, SWT.RIGHT);
		props.setLook(lblMessageID);
		FormData fd_lblMessageID = new FormData();
		fd_lblMessageID.left = new FormAttachment(0, 0);
		fd_lblMessageID.top = new FormAttachment(0, margin);
		fd_lblMessageID.right = new FormAttachment(middle, -margin);
		lblMessageID.setLayoutData(fd_lblMessageID);
		lblMessageID.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageID.Label"));

		tMessageID = new TextVar(transMeta, grpOutputField, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tMessageID);
		FormData fd_tMessageID = new FormData();
		fd_tMessageID.top = new FormAttachment(0, margin);
		fd_tMessageID.left = new FormAttachment(middle, 0);
		fd_tMessageID.right = new FormAttachment(100, 0);
		tMessageID.setLayoutData(fd_tMessageID);
		tMessageID
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageID.Tooltip"));
		
		// MessageBody
		lblMessageBody = new Label(grpOutputField, SWT.RIGHT);
		props.setLook(lblMessageBody);
		FormData fd_lblMessageBody = new FormData();
		fd_lblMessageBody.left = new FormAttachment(0, 0);
		fd_lblMessageBody.top = new FormAttachment(tMessageID, margin);
		fd_lblMessageBody.right = new FormAttachment(middle, -margin);
		lblMessageBody.setLayoutData(fd_lblMessageBody);
		lblMessageBody.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageBody.Label"));

		tMessageBody = new TextVar(transMeta, grpOutputField, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tMessageBody);
		FormData fd_tMessageBody = new FormData();
		fd_tMessageBody.top = new FormAttachment(tMessageID, margin);
		fd_tMessageBody.left = new FormAttachment(middle, 0);
		fd_tMessageBody.right = new FormAttachment(100, 0);
		tMessageBody.setLayoutData(fd_tMessageBody);
		tMessageBody
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.MessageBody.Tooltip"));
			
		// ReceiptHandle
		lblReceiptHandle = new Label(grpOutputField, SWT.RIGHT);
		props.setLook(lblReceiptHandle);
		FormData fd_lblReceiptHandle = new FormData();
		fd_lblReceiptHandle.left = new FormAttachment(0, 0);
		fd_lblReceiptHandle.top = new FormAttachment(tMessageBody, margin);
		fd_lblReceiptHandle.right = new FormAttachment(middle, -margin);
		lblReceiptHandle.setLayoutData(fd_lblReceiptHandle);
		lblReceiptHandle.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.ReceiptHandle.Label"));

		tReceiptHandle = new TextVar(transMeta, grpOutputField, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tReceiptHandle);
		FormData fd_tReceiptHandle = new FormData();
		fd_tReceiptHandle.top = new FormAttachment(tMessageBody, margin);
		fd_tReceiptHandle.left = new FormAttachment(middle, 0);
		fd_tReceiptHandle.right = new FormAttachment(100, 0);
		tReceiptHandle.setLayoutData(fd_tReceiptHandle);
		tReceiptHandle
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.ReceiptHandle.Tooltip"));
		
		// BodyMD5
		lblBodyMD5 = new Label(grpOutputField, SWT.RIGHT);
		props.setLook(lblBodyMD5);
		FormData fd_lblBodyMD5 = new FormData();
		fd_lblBodyMD5.left = new FormAttachment(0, 0);
		fd_lblBodyMD5.top = new FormAttachment(tReceiptHandle, margin);
		fd_lblBodyMD5.right = new FormAttachment(middle, -margin);
		lblBodyMD5.setLayoutData(fd_lblBodyMD5);
		lblBodyMD5.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.BodyMD5.Label"));

		tBodyMD5 = new TextVar(transMeta, grpOutputField, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tBodyMD5);
		FormData fd_tBodyMD5 = new FormData();
		fd_tBodyMD5.top = new FormAttachment(tReceiptHandle, margin);
		fd_tBodyMD5.left = new FormAttachment(middle, 0);
		fd_tBodyMD5.right = new FormAttachment(100, 0);
		tBodyMD5.setLayoutData(fd_tBodyMD5);
		tBodyMD5
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.BodyMD5.Tooltip"));
		
		// SNSMessage
		lblSNSMessage = new Label(grpOutputField, SWT.RIGHT);
		props.setLook(lblSNSMessage);
		FormData fd_lblSNSMessage = new FormData();
		fd_lblSNSMessage.left = new FormAttachment(0, 0);
		fd_lblSNSMessage.top = new FormAttachment(tBodyMD5, margin);
		fd_lblSNSMessage.right = new FormAttachment(middle, -margin);
		lblSNSMessage.setLayoutData(fd_lblSNSMessage);
		lblSNSMessage.setText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.SNSMessage.Label"));

		tSNSMessage = new TextVar(transMeta, grpOutputField, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tSNSMessage);
		FormData fd_tSNSMessage = new FormData();
		fd_tSNSMessage.top = new FormAttachment(tBodyMD5, margin);
		fd_tSNSMessage.left = new FormAttachment(middle, 0);
		fd_tSNSMessage.right = new FormAttachment(100, 0);
		tSNSMessage.setLayoutData(fd_tSNSMessage);
		tSNSMessage
				.setToolTipText(BaseMessages.getString(PKG, "SQSReaderStep.ReaderOutput.SNSMessage.Tooltip"));
		
		Control[] readerOutputTabList = new Control[] { tMessageID, tMessageBody, tReceiptHandle, tBodyMD5, tSNSMessage };
		grpOutputField.setTabList(readerOutputTabList);	
		
		
		
		readerOutputComp.pack();
		Rectangle ReaderOutputBounds = readerOutputComp.getBounds();

		scrlreaderOutputComp.setContent(readerOutputComp);
		scrlreaderOutputComp.setExpandHorizontal(true);
		scrlreaderOutputComp.setExpandVertical(true);
		scrlreaderOutputComp.setMinWidth(ReaderOutputBounds.width);
		scrlreaderOutputComp.setMinHeight(ReaderOutputBounds.height);
		// ReaderOutput-TAB - Ende
		
		scrlSettingsComp.layout();
		tbtmSettings.setControl(scrlSettingsComp);

		scrlreaderOutputComp.layout();
		tbtmReaderOutput.setControl(scrlreaderOutputComp);

		tabFolder.setSelection(0);

		// TABS ENDE
		      
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		tAWSKey.addSelectionListener(lsDef);
		tAWSKeySecret.addSelectionListener(lsDef);
		tAWSRegion.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateYesNoSelection();
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	protected void changeCredentialChainSelection() {
		// Output-Info set in fields
		if (tAWSCredChain.getText().equalsIgnoreCase("Y")) {

			// Settings-Fields
			lblAWSKey.setEnabled(false);
			tAWSKey.setEnabled(false);
			lblAWSKeySecret.setEnabled(false);
			tAWSKeySecret.setEnabled(false);
			lblAWSRegion.setEnabled(false);
			tAWSRegion.setEnabled(false);

			// Output-Info set in Config
		} else {

			// Settings-Fields
			lblAWSKey.setEnabled(true);
			tAWSKey.setEnabled(true);
			lblAWSKeySecret.setEnabled(true);
			tAWSKeySecret.setEnabled(true);
			lblAWSRegion.setEnabled(true);
			tAWSRegion.setEnabled(true);

		}

		meta.setChanged();
		
	}

	private void populateYesNoSelection() {
		
		tAWSCredChain.removeAll();				
		tAWSCredChain.add("Y");
		tAWSCredChain.add("N");
		tAWSCredChain.select(0);
		
		tMessageDelete.removeAll();				
		tMessageDelete.add("Y");
		tMessageDelete.add("N");
		tMessageDelete.select(0);
	}	

	/**
	 * This method fills the CombarVar with all available AWS Regions
	 * 
	 * @param ComboVar tAWSRegion2
	 */
	private void populateAWSRegion(ComboVar tAWSRegion2) {
		
		tAWSRegion2.removeAll();
			
		try {
		
			List<Region> snsRegions = RegionUtils.getRegionsForService(AmazonSQS.ENDPOINT_PREFIX);
			
			for (Iterator<Region> i = snsRegions.iterator(); i.hasNext();) {
				Region region = i.next();
				tAWSRegion2.add(region.getName());
			}

		} catch (AmazonClientException e) {
			logError(BaseMessages.getString(PKG, e.getMessage()));
		}
		
	}

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		
		tAWSCredChain.setText(meta.getAwsCredChain());
		tAWSKey.setText(meta.getAWSKey());	
		tAWSKeySecret.setText(meta.getAWSKeySecret());
		tAWSRegion.setText(meta.getAWSRegion());
		tSQSQueue.setText(meta.getSqsQueue());
		
		tMessageDelete.setText(meta.gettFldMessageDelete());
		tMaxMessages.setText(meta.gettFldMaxMessages());
		
		tMessageID.setText(meta.gettFldMessageID());
		tMessageBody.setText(meta.gettFldMessageBody());
		tReceiptHandle.setText(meta.gettFldReceiptHandle());
		tBodyMD5.setText(meta.gettFldBodyMD5());
		tSNSMessage.setText(meta.gettFldSNSMessage());
		
	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText(); 
		
		// Setting the  settings to the meta object
		meta.setAwsCredChain(tAWSCredChain.getText());
		meta.setAWSKey(tAWSKey.getText());
		meta.setAWSKeySecret(tAWSKeySecret.getText());
		meta.setAWSRegion(tAWSRegion.getText());
		meta.setSqsQueue(tSQSQueue.getText());
		
		meta.settFldMessageDelete(tMessageDelete.getText());
		meta.settFldMaxMessages(tMaxMessages.getText());

		meta.settFldMessageID(tMessageID.getText());
		meta.settFldMessageBody(tMessageBody.getText());
		meta.settFldReceiptHandle(tReceiptHandle.getText());
		meta.settFldBodyMD5(tBodyMD5.getText());
		meta.settFldSNSMessage(tSNSMessage.getText());
		
		
		// close the SWT dialog window
		dispose();
	}
}
