package org.grits.toolbox.core.datamodel.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerLabelProvider;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerViewContentProvider;
import org.grits.toolbox.core.utilShare.sort.EntryComparator;

public class ProjectExplorerDialog extends TitleAreaDialog{

	private List<String> filter = new ArrayList<String>();
	private Entry entry = null;
	protected Shell parent = null;
	
	//this is the main tree
	private TreeViewer viewer;
	
	//selected entry whenever a node is clicked in the tree
	private Entry selectedEntry = null;
	
	public ProjectExplorerDialog(Shell parentShell) {
		super(parentShell);
		this.parent = parentShell;
		//find the center of a main monitor
		Monitor primary = parentShell.getMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = parentShell.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
		parentShell.setLocation(x, y);
		//somehow it will automatically size, which is too big!
		//so hard coded!!
		parentShell.setSize(300, 300);
		create();
	}
	
	public void addFilter(String filter) {
		this.filter.add(filter);
	}
	
	public Entry getEntry() {
		return this.entry;
	}

	@Override
	public Control createDialogArea(Composite parent) {
		//Stick with GridLayout
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
		
		//initialize TreeViewer object with some settings.
		viewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		
		//set content provider
		viewer.setContentProvider(new ProjectExplorerViewContentProvider());
		
		//set label provider
		viewer.setLabelProvider(new ProjectExplorerLabelProvider());
		
		//set the initial tree model
		viewer.setInput(PropertyHandler.getDataModel().getRoot());

        // sets the comparator
        viewer.setComparator(new EntryComparator());
		
		GridData treeData = new GridData();
		treeData.minimumHeight = 180;
		treeData.grabExcessHorizontalSpace = true;
		treeData.grabExcessVerticalSpace = true;
		treeData.horizontalAlignment = GridData.FILL;
		treeData.horizontalSpan = 3;
		viewer.getTree().setLayoutData(treeData);
		
		//add listener
		viewer.addSelectionChangedListener
		(
			new ISelectionChangedListener()
			{
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//if the selected node is a project node then enables OK button
				//Otherwise, disable it.
				ISelection selection = event.getSelection();
				
				if(selection.isEmpty()) return;
				
				//convert it to TreeSelection object type
				TreeSelection to = (TreeSelection)selection;
				
				//check if only one is selected!
				if (to.size() == 1)
				{
					//check filter
					//get the selected node..
					Entry node = (Entry)to.getFirstElement();
					boolean match = false;
					for(String ft : filter)
					{
						if(node.getProperty().getType().equals(ft))
						{
							match = true;
						}
					}
					if(match)
					{
						//then enables OK button
						getButton(OK).setEnabled(true);
						selectedEntry = node;
					}
					else
					{
						getButton(OK).setEnabled(false);
					}
				}
				else
				{
					getButton(OK).setEnabled(false);
				}
			}
		});
		return parent;
	}
	
	@Override
	protected void okPressed() {
		entry = selectedEntry;
		super.okPressed();
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}
}
