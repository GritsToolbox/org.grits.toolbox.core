package org.grits.toolbox.core.datamodel.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MultiConfirmationDialog extends Dialog {

	public static int APPLY_ALL = 256;
	private String message;
	private String title;
	
	private boolean bSelected = false;
	private Button checkBox;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public MultiConfirmationDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
		
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	protected void buttonPressed(int buttonId) { 
		if ( buttonId == IDialogConstants.OK_ID) {
			okPressed();
		} else if ( buttonId == IDialogConstants.CANCEL_ID ) {
			cancelPressed();
		} else {
			noPressed();
		}
	}
	
	@Override
	protected void okPressed() {
		int code = SWT.OK;
		if ( checkBox.getSelection() ) {
			code |= SWT.YES;
		}
		setReturnCode(code);
		close();
	}
	
	@Override
	protected void cancelPressed() {
		int code = SWT.CANCEL;
		setReturnCode(code);
		close();
	}
	
	protected void noPressed() {
		int code = SWT.NO;
		if ( checkBox.getSelection() ) {
			code |= SWT.YES;
		}
		setReturnCode(code);
		close();
	}

	
	@Override
	public int open() {
		return super.open();
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		// The text fields will grow with the size of the dialog
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		Label label1 = new Label(parent, SWT.NONE);
		label1.setText(message);
		label1.setLayoutData(gridData);

		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		Label label2 = new Label(parent, SWT.NONE);
		label2.setText("");
		label2.setLayoutData(gridData);
		
		// You should not re-use GridData
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText("Remember my decision");
		checkBox.setLayoutData(gridData);
		checkBox.setSelection(this.bSelected);
		
		parent.getShell().setText(title);
		return container;
	}

	public void setChecked( boolean bSelected ) {
		this.bSelected = bSelected;
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.YES_LABEL,
				true);
		createButton(parent, IDialogConstants.NO_ID,
				IDialogConstants.NO_LABEL, false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 200);
	}

}
