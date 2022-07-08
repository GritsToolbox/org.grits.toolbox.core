package org.grits.toolbox.core.utilShare;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;

/**
 * This is a model how to use ProjectExplorerDialog in a new popup dialog.
 * Enables a user to choose a different entry based on property type.
 * @author kitaeUser
 *
 */
public class EntrySelectionAdapter extends SelectionAdapter 
{
    protected Composite parent = null;
    protected Entry entry = null;
    protected Text text = null;
    protected SelectionInterface parentWindow;
    protected String m_propertyType = null;
    protected String m_title = null;
    protected String m_message = null;

    public EntrySelectionAdapter(String a_propertyType, String a_dialogTitle, String a_dialogMessage)
    {
        super();
        this.m_propertyType = a_propertyType;
        this.m_title = a_dialogTitle;
        this.m_message = a_dialogMessage;
    }

    public void widgetSelected(SelectionEvent event) 
    {
        Shell newShell = new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET);
        ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
        // Set the parent as a filter
        dlg.addFilter(this.m_propertyType);
        // Change the title bar text
        dlg.setTitle(this.m_title);
        // Customizable message displayed in the dialog
        dlg.setMessage(this.m_message);
        // Calling open() will open and run the dialog.
        if (dlg.open() == Window.OK) {
        	Entry selected = dlg.getEntry();
	        if (selected != null) 
	        {
	            entry = selected;
	            // Set the text box as the project text
	            //			text.setText(entry.getDisplayName());
	            if ( parentWindow != null )
	            {
	                parentWindow.updateComponent( this );
	            }
	        }
        }
    }

    public void setParentWindow(SelectionInterface parentWindow) {
        this.parentWindow = parentWindow;
    }

    public SelectionInterface getParentWindow() {
        return parentWindow;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    /*
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}
     */
    public Composite getParent() {
        return parent;
    }

    public void setParent(Composite parent) {
        this.parent = parent;
    }

}
