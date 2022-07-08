package org.grits.toolbox.core.editor;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;

public abstract class EntryEditorPart implements IEntryEditorPart {
	
	public static final String EVENT_TOPIC_CONTENT_MODIFIED = "content_modified_in_a_tab";
	
	protected Entry entry = null;
	protected Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	protected String errMsg = null;
	
	boolean dirty = false;
	@Inject protected IEclipseContext context;
	@Inject IEventBroker eventBroker;
	
	public IEclipseContext getContext() {
		return context;
	}
	
	@Override
	public Entry getEntry() {
		return entry;
	}

	@Override
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	@Override
	public void setDirty(boolean d) {
		this.dirty = d;
		eventBroker.send(EVENT_TOPIC_CONTENT_MODIFIED, this);
	}
	
	@Persist
	public void doSave(IProgressMonitor monitor) {
		updateProjectProperty();
		savePreference();
	    setDirty(false);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	public Image getTitleImage() {
		return this.entry.getProperty().getImage().createImage();
	}
	
	protected void setErrorMessage(Label targetLabel, String errMsg) {
		//need to change color of this label to red
		targetLabel.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_RED));
		
		//set the errMsg 
		this.errMsg = errMsg;
	}
	
	protected void removeError(Label targetLabel) {
		targetLabel.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}
	
	protected boolean checkBasicLengthCheck(Label tagetLabel,Text targetText, int min, int max)
	{
		//check if it is a mandatory field or not
		if(tagetLabel.getFont().equals(boldFont))
		{
			if (targetText.getText().length() == min) {
				setErrorMessage(tagetLabel,"Please provide a name for " + tagetLabel.getText());
				return false;
			}
		}
		else
		{
			//length check
			if (targetText.getText().length() > max)
			{
				setErrorMessage(tagetLabel, tagetLabel.getText() + " cannot be longer than "+ max +" characters");
				return false;
			}
		}
		return true;
	}
	
	protected boolean checkValidDouble(Label label, String input) {
		//then check if it is double value or not
		try
		{
			double a = Double.parseDouble(input);
			if(a < 0)
			{
				setErrorMessage(label, "Invalid number. Please use a positive number.");
				return false;
			}
		}
		catch(NumberFormatException e)
		{
			setErrorMessage(label, "Invalid format. Please use a number.");
			return false;
		}
		removeError(label);
		return true;
	}
	
	protected Label setMandatoryLabel(Label lable) {
		lable.setText(lable.getText()+"*");
		lable.setFont(boldFont);
		return lable;
	}
	
	abstract protected void updateProjectProperty();
	abstract protected void savePreference();
	abstract protected Composite getParent();
}
