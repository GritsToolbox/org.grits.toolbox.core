package org.grits.toolbox.core.datamodel.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;

public abstract class ModalDialog extends TitleAreaDialog{
	
	protected Composite parent;
	
	public ModalDialog(Shell parentShell) {
		super(parentShell);
		parent = parentShell;
		//find the center of a main monitor
		Monitor primary = parentShell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = parentShell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		parentShell.setLocation(x, y);
	}

	// setup bold font
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);  
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		// Do nothing!! We will create own buttons
	}
	
	protected boolean checkBasicLengthCheck(Label targetLabel,Text targetText, int min, int max)
	{
		//check if it is a mandatory field or not
		if(targetLabel.getFont().equals(boldFont))
		{
			if (targetText.getText().length() == min) {
				setError(targetLabel,targetLabel.getText() + " cannot be empty. Please provide a name.");
				return false;
			}
		}
		//length check
		if (targetText.getText().length() > max || targetText.getText().length() < min)
		{
			setError(targetLabel, targetLabel.getText() + " cannot be longer than "+ max +" characters");
			return false;
		}
		//then true
		//turn the label back to normal
		removeError(targetLabel);
		return true;
	}
	
	protected void removeError(Label targetLabel) {
		targetLabel.setForeground(this.parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		setErrorMessage(null);
	}
	
	protected void setError(Label targetLabel, String string) {
		//call setErrorMsg
		setErrorMessage(string);
		
		//make the color of the label red
		//change the color of label text to red also
		targetLabel.setForeground(this.parent.getDisplay().getSystemColor(SWT.COLOR_RED));
	}
	
	protected Label createSeparator(int span) {
		GridData separatorData = new GridData();
		separatorData.grabExcessHorizontalSpace = true;
		separatorData.horizontalAlignment = GridData.FILL;
		separatorData.horizontalSpan = span;
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorData);
		return separator;
	}
	
	protected Button createButtonOK(final Composite parent2) {
		//create a grdiData for OKButton
		GridData okData = new GridData();
		okData.grabExcessHorizontalSpace = true;
		okData.horizontalAlignment = GridData.END;
		okData.horizontalSpan = 2;
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
	
	protected Button createButtonCancel(final Composite parent2) {
		//create a grdiData for CANCEL button
		GridData cancelData = new GridData();
		cancelData.horizontalAlignment = GridData.END;
		cancelData.grabExcessHorizontalSpace = true;
		cancelData.widthHint = 100;
		cancelData.horizontalSpan = 2;
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
	
	protected Label setMandatoryLabel(Label lable) {
		lable.setText(lable.getText()+"*");
		lable.setFont(boldFont);
		return lable;
	}
	
	protected boolean findSameNameEntry(String _entryName, Entry parent, String type) {
		if(parent != null)
		{
			List<Entry> children = parent.getChildren();
			
			for(Entry child : children)
			{
				if(child.getProperty().getType().equals(type))
				{
					if(child.getDisplayName().equals(_entryName))
					{
						return true;
					}
				}
			}
		}
		
		//there is no project folder
		return false;
	}

	abstract protected boolean isValidInput();
	abstract protected Entry createEntry();
}
