package org.grits.toolbox.core.utilShare;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ModalDialog;

public class ComboPopupDialog extends ModalDialog{

	private Combo cb = null;
	
	private Text nameText;
	
	public ComboPopupDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	public void create()
	{
		super.create();
		setTitle("New Value");
		setMessage("To create a new value");
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) 
	{
		//has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
		
		//create label
		GridData nameData = new GridData();
		nameData.horizontalSpan = 1;
		Label nameLabel = new Label(parent, SWT.LEFT);
		nameLabel.setText("Value");
		nameLabel = setMandatoryLabel(nameLabel);
		nameLabel.setLayoutData(nameData);
		
		//create text field
		GridData nameTextData = new GridData();
		nameTextData.grabExcessHorizontalSpace = true;
		nameTextData.horizontalAlignment = GridData.FILL;
		nameTextData.horizontalSpan = 2;
		nameText = new Text(parent, SWT.BORDER);
		nameText.setLayoutData(nameTextData);
		
		//create OK and cancel button
		new Label(parent, SWT.NONE);
		createButtonCancel(parent);
		createButtonOK(parent);
		
		return parent;
	}
	
	@Override
	protected Button createButtonCancel(Composite parent2) {
		//create a grdiData for CANCEL button
		GridData cancelData = new GridData();
		cancelData.horizontalAlignment = GridData.END;
		cancelData.grabExcessHorizontalSpace = true;
		cancelData.widthHint = 100;
		cancelData.horizontalSpan = 1;
		Button CancelButton = new Button(parent2, SWT.PUSH);
		CancelButton.setText("Cancel");
		CancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setReturnCode(CANCEL);
				close();
			}
		});
		CancelButton.setLayoutData(cancelData);
		return CancelButton;
	}
	
	@Override
	protected Button createButtonOK(Composite parent2) {
		//create a grdiData for OKButton
		GridData okData = new GridData();
		//okData.grabExcessHorizontalSpace = true;
		okData.horizontalAlignment = GridData.END;
		okData.horizontalSpan = 1;
		okData.widthHint = 100;
		Button OKbutton = new Button(parent2, SWT.PUSH);
		OKbutton.setText("OK");
		OKbutton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (isValidInput()) {
					okPressed();
					close();
				}
			}
		});
		OKbutton.setLayoutData(okData);
		return OKbutton;
	}
	
	public Combo getCb() {
		return cb;
	}

	public void setCb(Combo cb) {
		this.cb = cb;
	}

	@Override
	protected boolean isValidInput() {
		createEntry();
		return true;
	}

	@Override
	protected void okPressed() {
		//update the combo list
		if(nameText.getText().length() != 0)
		{
			if(!found())
			{
				cb.setItem(cb.getItemCount()-1, nameText.getText());
				cb.add("other");
			}
		}
	}
	
	private boolean found() {
		String[] items = cb.getItems();
		for(int i=0; i<cb.getItemCount(); i++)
		{
			if(items[i].equals(nameText.getText()))
			{
				cb.select(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public Entry createEntry() {
		return null;
	}
	
}
