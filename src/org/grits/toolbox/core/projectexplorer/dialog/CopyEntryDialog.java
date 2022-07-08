/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerLabelProvider;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerViewContentProvider;
import org.grits.toolbox.core.utilShare.sort.EntryComparator;

/**
 * 
 *
 */
public class CopyEntryDialog extends TitleAreaDialog 
{
	private Entry currentEntry = null;
	private Entry destinationParentEntry = null;
	private TreeViewer treeViewer = null;

	public CopyEntryDialog(Shell parentShell) 
	{
		super(parentShell);
	}
	
	@Override
	public void create()
	{
		super.create();
		getButton(OK).setEnabled(false);
		setTitle("Select the entry where you want to copy");
	}

	public Control createDialogArea(Composite parent) 
	{
		getShell().setText("Select Entry");
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(new GridLayout());

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setLabelProvider(new ProjectExplorerLabelProvider());
		treeViewer.setContentProvider(new ProjectExplorerViewContentProvider());
		treeViewer.setComparator(new EntryComparator());

		GridData treeData = new GridData();
		treeData.minimumHeight = 300;
		treeData.grabExcessHorizontalSpace = true;
		treeData.grabExcessVerticalSpace = true;
		treeData.horizontalAlignment = GridData.FILL;
		treeViewer.getTree().setLayoutData(treeData);
		treeViewer.setInput(DataModelHandler.instance().getRoot());

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				setErrorMessage(null);
				getButton(OK).setEnabled(false);
				ISelection selection = event.getSelection();
				if(!selection.isEmpty())
				{
					TreeSelection treeSelection = (TreeSelection) selection;
					if (treeSelection.size() == 1)
					{
						Entry selectedEntry = (Entry) treeSelection.getFirstElement();
						if(selectedEntry.getProperty() instanceof ProjectProperty
								&& !((ProjectProperty) selectedEntry.getProperty()).isOpen())
						{
							setErrorMessage("Cannot copy here. The selected project is closed.");
						}
						else
						{
							if(currentEntry.getParent().getProperty().getType()
									.equals(selectedEntry.getProperty().getType()))
							{
								boolean uniqueName = true;
								for(Entry child : selectedEntry.getChildren())
								{
									if(child.getDisplayName().equals(currentEntry.getDisplayName()))
									{
										uniqueName = false;
										break;
									}
								}
								if(uniqueName)
								{
									getButton(OK).setEnabled(true);
									destinationParentEntry = selectedEntry;
								}
								else
								{
									setErrorMessage("There is already an entry "
											+ "with this name in the selected parent.");
									
								}
							}
							else
							{
								setErrorMessage("Cannot copy here.");
							}
						}
					}
				}
			}
		});
		treeViewer.getTree().setFocus();

		return composite;
	}

	protected void okPressed() 
	{
		super.okPressed();
	}

	public void cancelPressed() 
	{
		super.cancelPressed();
	}

	/**
	 * @return the currentEntry
	 */
	public Entry getCurrentEntry()
	{
		return currentEntry;
	}

	/**
	 * @param currentEntry the currentEntry to set
	 */
	public void setCurrentEntry(Entry currentEntry)
	{
		this.currentEntry = currentEntry;
	}

	/**
	 * @return the destinationParentEntry
	 */
	public Entry getDestinationParentEntry()
	{
		return destinationParentEntry;
	}

	/**
	 * @param destinationParentEntry the destinationParentEntry to set
	 */
	public void setDestinationParentEntry(Entry destinationParentEntry)
	{
		this.destinationParentEntry = destinationParentEntry;
	}

}
