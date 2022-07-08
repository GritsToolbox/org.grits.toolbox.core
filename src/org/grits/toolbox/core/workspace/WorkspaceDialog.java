package org.grits.toolbox.core.workspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.jdom.JDOMException;

/**
 * This class provides directory explorer and handles the path of a workspace that a user selects
 * @author kitaeUser
 *
 */
public class WorkspaceDialog {

	private Shell parent;
	private int flag = Window.CANCEL;
	private String path = null;
	private List<WorkspaceHistoryEntry> history = new ArrayList<WorkspaceHistoryEntry>();
	private final String INITSTR = "<Choose workspace folder>";
	
	private Button OKbutton;
	
	//log4J Logger
	private static final Logger logger = Logger.getLogger(WorkspaceDialog.class);
	
	/**
	 * Constructor
	 * @param parentShell parent
	 */
	public WorkspaceDialog(Shell parentShell) {
		this.parent = parentShell;
		parentShell.setText("Select Workspace");
		//find the center of a main monitor
		Monitor primary = parentShell.getDisplay().getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = parentShell.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
		parentShell.setLocation(x, y);
		//parentShell.setSize(550, 105);
		createContents(parentShell);
		parentShell.pack();
	}

	/**
	 * Open a dialog!
	 * @return int 0-OK button, 1-cancel button, 2-exit(x) button 
	 */
	public int open()
	{
		parent.open();
		while (!parent.isDisposed()) {
			if (!parent.getDisplay().readAndDispatch()) {
				parent.getDisplay().sleep();
			}
		}
		return flag;
	}

	/**
	 * Creates a combo list and buttons. More information is available at 
	 * http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html
	 * 1st row: WORKSPACE() COMBO() BROWSER()
	 * 2nd row: 2 span         OK() CANCEL()
	 * @param shell the parent shell
	 */
	private void createContents(final Shell shell) {
		//let's stick to GridLayout for all dialogs
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);
		
		/*
		 * First row starts
		 */
		//Workspace Label
		//set minimum width and height:
		GridData workspaceData = new GridData(SWT.FILL, SWT.FILL, true, false);
		Label label = new Label(shell, SWT.NONE);
		label.setText("Workspace:");
		label.setLayoutData(workspaceData);
		
		//Combo
		final Combo combo = new Combo(shell, SWT.DROP_DOWN);
		GridData comboData = new GridData(SWT.FILL, SWT.FILL, true, false);
		comboData.grabExcessHorizontalSpace = true;
		comboData.horizontalAlignment = GridData.FILL;
		comboData.minimumWidth = 200;
		combo.setLayoutData(comboData);
		createCombo(shell,combo);
		
		//Browse button
		createBrowseButton(shell,combo);
		
		/*
		 * Second row starts
		 */
		
		//2nd row 1st to 2th column
		//OK button 
		createOKButton(shell,combo);

		//2nd row 3rd column
		//CANCEL button 
		createCANCELButton(shell);
	}

	private void createCANCELButton(final Shell shell) {
		//create a grdiData for CANCEL button
		GridData cancelData = new GridData();
		Button CancelButton = new Button(shell, SWT.PUSH);
		cancelData.horizontalAlignment = GridData.FILL;
		CancelButton.setText("Cancel");
		CancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				//set the flag to 1;
				flag = Window.CANCEL;
				parent.close();
			}
		});
		CancelButton.setLayoutData(cancelData);
	}

	private void createOKButton(final Shell shell, final Combo combo) {
		//create a grdiData for OKButton
		GridData okData = new GridData();
		okData.grabExcessHorizontalSpace = true;
		//set this button location to END
		okData.horizontalAlignment = GridData.END;
		okData.horizontalSpan = 2;
		OKbutton = new Button(shell, SWT.PUSH);
		OKbutton.setText("    OK    ");
		path = combo.getText();
		if (path.equals("") || path.equals(INITSTR))
		{
			OKbutton.setEnabled(false);
		}
		else
		{
			OKbutton.setEnabled(true);
		}
		OKbutton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				path = combo.getText();
				//if nothing was typed, then the path becomes empty string;
				//so if path is empty string, then give the user warning message!
				//System.out.println(path);
				if (path.equals("") || path.equals(INITSTR) || path.contains(INITSTR))
				{
					MessageDialog.openInformation(shell, "Error", "Please select a location for workspace.");
				}
				else
				{
					//need to check if it is a valid path or not
					File newLocation = new File(path);

					boolean folderExists = newLocation.exists();
					if (!folderExists)
					{
						//create a new directory
						folderExists = newLocation.mkdir();
					}

					//check if the combo-box (previous user history) contains the selected location
					boolean existsInHistoryFile = false;

					for(int i=0; i<history.size(); i++)
					{
						if (history.get(i).getPath().equals(path))
						{
							existsInHistoryFile = true;
							break;
						}
					}

					if(folderExists)
					{
						try
						{
							WorkspaceHistoryFileHandler.updateHistoryXMLFile(path, existsInHistoryFile);
						} catch (IOException e) {
							logger.error(e.getMessage(),e);
							MessageDialog.openInformation(shell, "Error", e.getMessage());
						} catch (JDOMException e) {
							logger.error(e.getMessage(),e);
							MessageDialog.openInformation(shell, "Error", e.getMessage());
						}
					}
					else
					{
						logger.error("Invalid file name, Please choose another name for the workspace.");
						MessageDialog.openInformation(shell, "Invalid Folder Name",
								"Invalid folder name. \nPlease choose another name for the workspace.");
					}
					//for OK
					flag = Window.OK;
					parent.close();
				}
			}
		});
		OKbutton.setLayoutData(okData);
	}

	private void createCombo(final Shell shell, final Combo combo) {
		//need to get a list from the configuration file
		try {
			history = WorkspaceHistoryFileHandler.readHistoryFile();
		} catch (Exception e) {
			logger.fatal(e.getMessage(),e);
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setText("Error");
		    messageBox.setMessage("Error while reading history xml file. Create a new history file");
		    messageBox.open();
		    //create a new history file
		    try {
				WorkspaceHistoryFileHandler.createHistoryXMLFile();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
				//cannot create history xml file. Should return 1 
				MessageBox messageBox2 = new MessageBox(shell, SWT.OK);
				messageBox2.setText("Error while creating history xml file");
			    messageBox2.setMessage(e1.getMessage());
			    messageBox2.open();
			    //set the flag to 1;
				flag = Window.CANCEL;
				parent.close();
			}
		}
		
		//set the last active element to the combo list
		try {
			if (WorkspaceHistoryFileHandler.getLastActiveHistory() != null)
			{
				//show it in the top of the dropdown list
				combo.setText(WorkspaceHistoryFileHandler.getLastActiveHistory());
			}
			else
			{
				combo.setText(INITSTR);
			}
		} catch (Exception e) {
			logger.fatal(e.getMessage(),e);
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setText("Error");
		    messageBox.setMessage("Error while reading history xml file. Create a new history file");
		    messageBox.open();
		    //create a new history file
		    try {
				WorkspaceHistoryFileHandler.createHistoryXMLFile();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
				//cannot create history xml file. Should return 1 
				MessageBox messageBox2 = new MessageBox(shell, SWT.OK);
				messageBox2.setText("Error while creating history xml file");
			    messageBox2.setMessage(e1.getMessage());
			    messageBox2.open();
			    //set the flag to 1;
				flag = Window.CANCEL;
				parent.close();
			}
		}
		
		for(int i=0; i<history.size(); i++)
		{
			combo.add(history.get(i).getPath(),i);
		}
		
		combo.addSelectionListener(new SelectionListener() 
		{
			public void widgetSelected(SelectionEvent e) {
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				String text = combo.getText();
				if(combo.indexOf(text) < 0) { // Not in the list yet. 
					combo.add(text);
					// Re-sort
					String[] items = combo.getItems();
					Arrays.sort(items);
					combo.setItems(items);
				}
			}
		});
		
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				path = combo.getText();
				//if nothing was typed, then the path becomes empty string;
				//so if path is empty string, then give the user warning message!
				//System.out.println(path);
				if (path.equals("") || path.equals(INITSTR))
				{
					OKbutton.setEnabled(false);
				}
				else
				{
					OKbutton.setEnabled(true);
				}
			}
		});

		combo.addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e)
			{
				if(e.character == '\r')
				{
					OKbutton.notifyListeners(SWT.Selection, new Event());
				}
			}
		});
	}

	private void createBrowseButton(final Shell shell, final Combo combo) {
		GridData browseButtonData = new GridData();
		Button button = new Button(shell, SWT.PUSH);
		button.setLayoutData(browseButtonData);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() 
		{
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
					combo.setText(dir.trim());
					path = dir;
				}
			}
		});
	}

	/**
	 * Get method for the path
	 * @return
	 */
	
	public String getPath() {
		return path;
	}

}
