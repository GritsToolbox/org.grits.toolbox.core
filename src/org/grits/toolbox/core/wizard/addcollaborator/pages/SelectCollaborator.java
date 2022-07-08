/**
 * 
 */
package org.grits.toolbox.core.wizard.addcollaborator.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.TableColumnComparatorListener;
import org.grits.toolbox.core.utilShare.sort.TableViewerComparator;

/**
 * 
 *
 */
public class SelectCollaborator extends WizardPage
{
	private static String title = "Add Collaborator(s)";
	private CheckboxTableViewer checkboxTableViewer;
	private Button addNewButton;
	public Button selectFromButton;

	public SelectCollaborator() {
		super(title );
		setTitle(title);
		setMessage("Add new Collaborator or "
				+ "Select from existing Collaborators");
	}

	@Override
	public void createControl(Composite parent)
	{
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginRight = 8;
        layout.marginBottom = 8;
        layout.verticalSpacing = 15;
        layout.horizontalSpacing = 10;
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        container.setLayout(layout);
        
        createButtonsPart(container);
        createTablePart(container);

		setControl(container);
	}


	private void createButtonsPart(Composite container) {
		addNewButton = new Button(container, SWT.RADIO);
		addNewButton.setText("Add New Collaborator");
		addNewButton.setLayoutData(new GridData(GridData.BEGINNING));

		selectFromButton = new Button(container, SWT.RADIO);
		selectFromButton.setText("Select Collaborator(s) from Table");
		selectFromButton.setLayoutData(new GridData(GridData.BEGINNING));

		addNewButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkboxTableViewer.setEditable(selectFromButton.getSelection());
				setPageComplete(true);
				getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				setPageComplete(true);

			}
		});

		selectFromButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkboxTableViewer.setEditable(selectFromButton.getSelection());
				setPageComplete(false);
				getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
	}

	private void createTablePart(Composite container)
	{
        Table collaboratorsTable = new Table(container, 
                SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
                SWT.FULL_SELECTION);
        GridData tableLayouData = new GridData(GridData.FILL_BOTH);
        tableLayouData.horizontalSpan = 3;
        tableLayouData.verticalSpan = 1;
        tableLayouData.minimumHeight = 100;
        tableLayouData.heightHint = 300;
        collaboratorsTable.setLayoutData(tableLayouData);
        checkboxTableViewer = new CheckboxTableViewer(collaboratorsTable, "Select");

        TableViewerColumn tableColumn2 = new TableViewerColumn(checkboxTableViewer, SWT.FILL, 1);
        tableColumn2.getColumn().setText("Collaborators");
        tableColumn2.getColumn().setWidth(500);

        checkboxTableViewer.getTable().setHeaderVisible(true);
        checkboxTableViewer.getTable().setLinesVisible(true);
        checkboxTableViewer.setContentProvider(new GenericListContentProvider());
        checkboxTableViewer.setLabelProvider(new ITableLabelProvider()
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
            	String value = null;
                if(columnIndex > 0 
                        && element instanceof ProjectCollaborator)
                {
					ProjectCollaborator collaborator = ((ProjectCollaborator) element);
					value = collaborator.getName() + " (";
					value += (collaborator.getFundingAgency() == null
							|| collaborator.getFundingAgency().isEmpty()) 
							? " -- " : collaborator.getFundingAgency();
					value += " ";
					value += (collaborator.getGrantNumber() == null
							|| collaborator.getGrantNumber().isEmpty()) 
							? " -- " : collaborator.getGrantNumber();
					value += ")";
				}
                return value;
            }

            @Override
            public Image getColumnImage(Object element, int columnIndex)
            {
                return null;
            }
        });

        checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
        {
        	@Override
        	public void checkStateChanged(CheckStateChangedEvent event)
        	{
        		ProjectCollaborator projectCollaborator = 
        				(ProjectCollaborator) event.getElement();
        		boolean checked = event.getChecked();
        		if(!selectFromButton.getSelection())
        		{
        			selectFromButton.setSelection(true);
        			addNewButton.setSelection(false);
        		}
        		checkboxTableViewer.setChecked(projectCollaborator, checked);
				projectCollaborator.setAddByDefault(checked);
        		getContainer().updateButtons();
        	}
        });
        checkboxTableViewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				if(element instanceof ProjectCollaborator)
				{
					return ((ProjectCollaborator) element).isAddByDefault();
				}
				return false;
			}
		});

		List<ProjectCollaborator> collaborators = new ArrayList<ProjectCollaborator>();
		MultiChoicePreference<ProjectCollaborator> collaboratorPreference =
				ProjectPreferenceStore.getMultiChoicePreference(
				ProjectPreferenceStore.ParameterizedPreference.COLLABORATOR);
		for(ProjectCollaborator defaultCollaborator : collaboratorPreference.getSelectedValues())
		{
			collaborators.add(defaultCollaborator.getACopy());
		}
		for(ProjectCollaborator otherCollaborator : collaboratorPreference.getOtherValues())
		{
			collaborators.add(otherCollaborator.getACopy());
		}

        checkboxTableViewer.setComparator(new TableViewerComparator());
		tableColumn2.getColumn().addSelectionListener(
				new TableColumnComparatorListener(checkboxTableViewer));
        checkboxTableViewer.setInput(collaborators);
        checkboxTableViewer.refresh();

        selectFromButton.setEnabled(!collaborators.isEmpty());
        addNewButton.setSelection(true);
        addNewButton.notifyListeners(SWT.SELECTED, new Event());
	}

	public List<ProjectCollaborator> getSelectedCollaborators()
	{
		List<ProjectCollaborator> selectedCollaborators = 
				new ArrayList<ProjectCollaborator>();
		for(Object collaborator : checkboxTableViewer.getCheckedElements())
		{
			selectedCollaborators.add((ProjectCollaborator) collaborator);
			
		}
		return selectedCollaborators;
	}
}
