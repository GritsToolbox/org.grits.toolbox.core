/**
 * 
 */
package org.grits.toolbox.core.wizard.taskexport.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.EntryLabelProvider;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;

/**
 * 
 *
 */
public class TaskFilterPage extends WizardPage
{
	private static String title = "Filter Criteria";
	public static final String DEFAULT_FILENAME = "GRITS_TaskExport.xlsx";
	public CheckboxTableViewer projectTableViewer = null;
	public CheckboxTableViewer statusTableViewer = null;
	public Text locationText = null;

	public TaskFilterPage() 
	{
		super(title );
		setTitle(title);
		setMessage("Choose Project and Status you want to export task from.");
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginBottom = 20;
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NONE);
		label.setText("Projects");
		GridData labelGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		labelGridData.horizontalSpan = 1;
		label.setLayoutData(labelGridData);

		Table projectsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 3;
		tableLayouData.heightHint = 100;
		projectsTable.setLayoutData(tableLayouData);
		projectTableViewer = new CheckboxTableViewer(projectsTable, "Select");

		TableViewerColumn tableColumn2 = new TableViewerColumn(projectTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Projects");
		tableColumn2.getColumn().setWidth(300);

		projectTableViewer.getTable().setHeaderVisible(true);
		projectTableViewer.getTable().setLinesVisible(true);
		projectTableViewer.setContentProvider(new GenericListContentProvider());
		projectTableViewer.setLabelProvider(new EntryLabelProvider());

		Entry workspaceEntry = PropertyHandler.getDataModel().getRoot();
		List<Entry> projectEntries = new ArrayList<Entry>();
		projectTableViewer.setInput(projectEntries);
		for(Entry child : workspaceEntry.getChildren())
		{
			if(child.getProperty() instanceof ProjectProperty)
			{
				projectEntries.add(child);
				projectTableViewer.add(child);
				projectTableViewer.setChecked(child, 
						((ProjectProperty) child.getProperty()).getOpen());
			}
		}

		label = new Label(container, SWT.NONE);
		label.setText("Status");
		labelGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		labelGridData.horizontalSpan = 1;
		label.setLayoutData(labelGridData);

		Table statusTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData statusLayouData = new GridData(GridData.FILL_BOTH);
		statusLayouData.horizontalSpan = 3;
		statusLayouData.heightHint = 100;
		statusTable.setLayoutData(statusLayouData);
		statusTableViewer = new CheckboxTableViewer(statusTable, "Select");

		TableViewerColumn statusColumn2 = new TableViewerColumn(statusTableViewer, SWT.FILL, 1);
		statusColumn2.getColumn().setText("Status");
		statusColumn2.getColumn().setWidth(300);

		statusTableViewer.getTable().setHeaderVisible(true);
		statusTableViewer.getTable().setLinesVisible(true);
		statusTableViewer.setContentProvider(new GenericListContentProvider());
		statusTableViewer.setLabelProvider(new ITableLabelProvider()
		{
			@Override
			public void removeListener(ILabelProviderListener listener)
			{

			}

			@Override
			public boolean isLabelProperty(Object element, String property)
			{
				return false;
			}

			@Override
			public void dispose()
			{

			}

			@Override
			public void addListener(ILabelProviderListener listener)
			{

			}

			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				if(columnIndex > 0 
						&& element instanceof String)
				{
					return (String) element;
				}
				return null;
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}
		});

		List<String> status = new ArrayList<String>();
		status.addAll(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.STATUS).getAllValues());
		Collections.sort(status, String.CASE_INSENSITIVE_ORDER);
		statusTableViewer.setInput(status);
		String[] allStatus = new String[status.size()];
		int i = 0;
		for(String keyword : status)
		{
			allStatus[i++] = keyword;
		}
		statusTableViewer.setCheckedElements(allStatus);
		statusTableViewer.refresh();

		locationText =  new Text(container, SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 2;
		locationText.setLayoutData(textData);
		locationText.setText(System.getProperty("user.home")
				+ File.separator + DEFAULT_FILENAME);
		locationText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) 
			{
				if(locationText.getText().isEmpty())
				{
					setErrorMessage("Choose a location to Save");
				}
				else
				{
					setErrorMessage(null);
				}
			}
		});
		
		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setText("Browse Location");
		GridData browseButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseButtonGridData.horizontalSpan = 1;
		browseButton.setLayoutData(browseButtonGridData);

		browseButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
                fileDialog.setText("Select File");
                fileDialog.setFilterExtensions(new String[] { "*.xlsx" });
                fileDialog.setFilterNames(new String[] { "Excel (*.xlsx)" });
                fileDialog.setFileName(DEFAULT_FILENAME);
                fileDialog.setOverwrite(true);
                String selected = fileDialog.open();
                if (selected != null)
                {
                    locationText.setText(selected);
                }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		browseButton.setFocus();
		
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(container);
	}
}
