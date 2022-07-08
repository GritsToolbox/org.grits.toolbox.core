package org.grits.toolbox.core.utilShare;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FileSelectionAdapter extends SelectionAdapter {
	private Shell shell = null;
	private Text text = null;
	private String[] filterExtensions = null;
	private String[] filterNames = null;
	public void widgetSelected(SelectionEvent event) 
	{
		FileDialog dlg = new FileDialog(shell);
		// Set the initial filter path according
		// to anything they've selected or typed in
		dlg.setFilterPath(null);
		// Change the title bar text
		dlg.setText("File explorer");
		// Calling open() will open and run the dialog.
		// It will return the selected directory, or
		// null if user cancels
		
		if ( filterExtensions != null && filterExtensions.length > 0 ) {
			dlg.setFilterExtensions(filterExtensions);
		}
		if ( filterNames != null && filterNames.length > 0 ) {
			dlg.setFilterNames(filterNames);
		}
		String dir = dlg.open();
		if (dir != null) {
			// Set the text box to the new selection
			text.setText(dir);
		}
	}
	
	public void setFilterNames(String[] filterNames) {
		this.filterNames = filterNames;
	}
	
	public void setFilterExtensions(String[] filterExtensions) {
		this.filterExtensions = filterExtensions;
	}
	
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}
}
