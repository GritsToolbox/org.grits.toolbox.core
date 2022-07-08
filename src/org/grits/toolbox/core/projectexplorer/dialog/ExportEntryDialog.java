/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.EntryLabelProvider;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.EntryComparator;

/**
 * 
 *
 */
public class ExportEntryDialog extends TitleAreaDialog
{
	public static final String EXTENSION = ".GR";
	private static final String DEFAULT_FILENAME = "Projects" + EXTENSION;
	private CheckboxTableViewer projectTableViewer = null;
	private List<Entry> selectedEntries = new ArrayList<Entry>();
	private String savingLocation = null;
	private Text locationText = null;
	private boolean userSelectedLocation = false;

	public ExportEntryDialog(Shell parentShell, List<Entry> selectedEntries)
	{
		super(parentShell);
		this.selectedEntries = selectedEntries;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Export Projects");
		setMessage("Export Projects in a Zip");
		getShell().setText("Export");
		getButton(OK).setText("Export");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginBottom = 20;
		layout.verticalSpacing = 15;
		container.setLayout(layout);

		Table projectsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 3;
		tableLayouData.heightHint = 200;
		projectsTable.setLayoutData(tableLayouData);
		projectTableViewer = new CheckboxTableViewer(projectsTable, "Select");

		TableViewerColumn tableColumn2 = new TableViewerColumn(projectTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Projects");
		tableColumn2.getColumn().setWidth(550);

		projectTableViewer.getTable().setHeaderVisible(true);
		projectTableViewer.getTable().setLinesVisible(true);
		projectTableViewer.setContentProvider(new GenericListContentProvider());
		projectTableViewer.setLabelProvider(new EntryLabelProvider());
		projectTableViewer.setComparator(new EntryComparator());

		Entry workspaceEntry = PropertyHandler.getDataModel().getRoot();
		List<Entry> projectEntries = new ArrayList<Entry>();
		projectTableViewer.setInput(projectEntries);
		for(Entry child : workspaceEntry.getChildren())
		{
			if(child.getProperty() instanceof ProjectProperty)
			{
				projectEntries.add(child);
				projectTableViewer.add(child);
				projectTableViewer.setChecked(child, selectedEntries.contains(child));
			}
		}

		Button selectAllButton = addAButton(container, "Select All", GridData.BEGINNING);
		selectAllButton.addSelectionListener(new SelectionListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				for(Entry selectedEntry : (List<Entry>) projectTableViewer.getInput())
				{
						projectTableViewer.setChecked(selectedEntry, true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		Button deselectAllButton = addAButton(container, "Deselect All", GridData.BEGINNING);
		deselectAllButton.addSelectionListener(new SelectionListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				for(Entry selectedEntry : (List<Entry>) projectTableViewer.getInput())
				{
						projectTableViewer.setChecked(selectedEntry, false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});

		locationText =  new Text(container, SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 2;
		locationText.setLayoutData(textData);
		locationText.setText(System.getProperty("user.home")
				+ File.separator + DEFAULT_FILENAME);
		locationText.setEnabled(false);

		Button browseButton = addAButton(container,"Download Here", GridData.HORIZONTAL_ALIGN_END);

		browseButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog.setText("Select File");
				fileDialog.setFilterExtensions(new String[] {EXTENSION});
				fileDialog.setFilterNames(new String[] { "Grits Export (" + EXTENSION + ")" });
				fileDialog.setFileName(DEFAULT_FILENAME);
				fileDialog.setOverwrite(true);
				String selected = fileDialog.open();
				if (selected != null)
				{
					locationText.setText(selected);
					userSelectedLocation = true;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});
		browseButton.setFocus();

		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		return comp;
	}

	private Button addAButton(Composite container, String label, int horizontalAlignment)
	{
		Button button = new Button(container, SWT.PUSH);
		button.setText(label);
		GridData buttonGridData = new GridData(horizontalAlignment);
		buttonGridData.horizontalSpan = 1;
		buttonGridData.widthHint = 150;
		button.setLayoutData(buttonGridData);
		return button;
	}

	public List<Entry> getSelectedEntries()
	{
		return selectedEntries;
	}

	@Override
	protected void okPressed()
	{
		if(projectTableViewer.getCheckedElements().length > 0)
		{
			setErrorMessage(null);
			if(locationText.getText().isEmpty())
			{
				setErrorMessage("Choose a location to Save");
			}
			else if(!userSelectedLocation)
			{
				// check for a duplicate file overwrite
				if(new File(locationText.getText().trim()).exists()
						&& !MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
								"Overwrite File", "Do you want to overwrite an existing export file?"))
				{
					setErrorMessage("A file with this name already exists. Select \"Yes\""
							+ " to overwrite while exporting.");
				}
			}

			// should have no error or else return
			if(getErrorMessage() != null)
				return;

			savingLocation = locationText.getText().trim();
			selectedEntries = new ArrayList<Entry>();
			String workspaceLocationFolder = PropertyHandler.getVariable("workspace_location");
			File workspaceFolder = new File(workspaceLocationFolder);
			if(workspaceFolder.exists())
			{
				workspaceLocationFolder = workspaceLocationFolder.substring(0, workspaceLocationFolder.length());
				for(File childFile : workspaceFolder.listFiles())
				{
					for(Object pj : projectTableViewer.getCheckedElements())
					{
						if(pj instanceof Entry)
						{
							Entry projectEntry = ((Entry) pj);
							if(childFile.getName().equals(projectEntry.getDisplayName()))
							{
								selectedEntries.add(projectEntry);
								break;
							}
						}
					}
				}
			}
			super.okPressed();
		}
		else
		{
			setErrorMessage("No Entry selected for export.");
		}
	}

	public String getSavingLocation()
	{
		return savingLocation;
	}
}
