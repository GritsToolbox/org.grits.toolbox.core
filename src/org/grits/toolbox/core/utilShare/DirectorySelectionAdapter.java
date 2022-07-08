package org.grits.toolbox.core.utilShare;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DirectorySelectionAdapter extends SelectionAdapter {
	
	private Shell shell = null;
	private Text text = null;
	
	public void widgetSelected(SelectionEvent event) 
	{
		DirectoryDialog dlg = new DirectoryDialog(shell);
		// Set the initial filter path according
		// to anything they've selected or typed in
		dlg.setFilterPath(null);
		// Change the title bar text
		dlg.setText("Directory explorer");
		// Customizable message displayed in the dialog
		dlg.setMessage("Select a directory");
		// Calling open() will open and run the dialog.
		// It will return the selected directory, or
		// null if user cancels
		String dir = dlg.open();
		if (dir != null) {
			// Set the text box to the new selection
			text.setText(dir);
		}
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
