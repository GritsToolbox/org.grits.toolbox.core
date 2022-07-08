/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.part.toolitem.SaveCollaborator;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.TableColumnComparatorListener;
import org.grits.toolbox.core.utilShare.sort.TableViewerComparator;

/**
 * 
 * @author sena
 *
 */
//Modified (Sena) March 2017 to inform the user for errors (ticket #826)
public class CollaboratorPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(CollaboratorPreference.class);

	private MultiChoicePreference<ProjectCollaborator> collaboratorPreference = null;
	public List<ProjectCollaborator> defaultCollaborators = null;
	public List<ProjectCollaborator> otherCollaborators = null;

	private CheckboxTableViewer checkboxTableViewer = null;
	private Button addButton = null;
	private Button editButton = null;
	private Button removeButton = null;
	private CollaboratorInfoDialog dialog = null;

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.ParameterizedPreference.COLLABORATOR.getPreferenceName());

		collaboratorPreference = ProjectPreferenceStore.getMultiChoicePreference(
						ProjectPreferenceStore.ParameterizedPreference.COLLABORATOR);

		ProjectCollaborator copiedCollaborator = null;
		defaultCollaborators = new ArrayList<ProjectCollaborator>();
		for(ProjectCollaborator collaborator : collaboratorPreference.getSelectedValues())
		{
			copiedCollaborator = collaborator.getACopy();
			copiedCollaborator.setAddByDefault(true);
			defaultCollaborators.add(copiedCollaborator);
		}

		otherCollaborators = new ArrayList<ProjectCollaborator>();
		for(ProjectCollaborator collaborator : collaboratorPreference.getOtherValues())
		{
			copiedCollaborator = collaborator.getACopy();
			copiedCollaborator.setAddByDefault(false);
			otherCollaborators.add(copiedCollaborator);
		}

		dialog = new CollaboratorInfoDialog(getShell());
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.marginBottom = 8;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		createTablePart(container);
		createButtonPart(container);
		loadValues();
		initializeTable();
		return container;
	}

	private void createTablePart(Composite container)
	{
		Table collaboratorsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 3;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 150;
		tableLayouData.heightHint = 300;
		collaboratorsTable.setLayoutData(tableLayouData);
		checkboxTableViewer = new CheckboxTableViewer(collaboratorsTable, "Default");

		TableViewerColumn tableColumn2 = new TableViewerColumn(checkboxTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Collaborators");
		tableColumn2.getColumn().setWidth(300);

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
				if(columnIndex == 1 
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

		checkboxTableViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				StructuredSelection selection = (StructuredSelection) checkboxTableViewer.getSelection();
				if(!selection.isEmpty())
				{
					ProjectCollaborator selectedCollaborator = (ProjectCollaborator) selection.getFirstElement();
					dialog.setCollaborator(selectedCollaborator.getACopy());
					if(dialog.open() == Window.OK)
					{
						ProjectCollaborator editedCollaborator = dialog.getCollaborator();
						@SuppressWarnings("unchecked")
						List<ProjectCollaborator> collaborators = 
								(List<ProjectCollaborator>) checkboxTableViewer.getInput();
						collaborators.set(collaborators.indexOf(selectedCollaborator), editedCollaborator);
						checkboxTableViewer.refresh();
						checkboxTableViewer.setSelection(new StructuredSelection(editedCollaborator));
					}
				}
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
				projectCollaborator.setAddByDefault(checked);
				checkboxTableViewer.setChecked(projectCollaborator, checked);
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
		addViewerComparator(checkboxTableViewer);
	}

	private void initializeTable()
	{
		List<ProjectCollaborator> input = new ArrayList<ProjectCollaborator>();
		input.addAll(otherCollaborators);
		input.addAll(defaultCollaborators);
		checkboxTableViewer.setInput(input);
		checkboxTableViewer.refresh();
	}

	private void addViewerComparator(TableViewer tableViewer) 
	{
		tableViewer.setComparator(new TableViewerComparator());
		int totalColumns = tableViewer.getTable().getColumns().length;
		for(int i = 0 ; i < totalColumns ; i++)
		{
			tableViewer.getTable().getColumn(i).addSelectionListener(
					new TableColumnComparatorListener(tableViewer));
		}
	}

	private void createButtonPart(Composite container)
	{
		Composite composite = new Composite(container, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.marginTop = 20;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);
		addButton = createButton(composite, "Add");
		editButton = createButton(composite, "Edit");
		removeButton = createButton(composite, "Delete");
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				@SuppressWarnings("unchecked")
				List<ProjectCollaborator> collaborators = 
						(List<ProjectCollaborator>) checkboxTableViewer.getInput();
				dialog.setCollaborator(null);
				if(dialog.open() == Window.OK)
				{
					ProjectCollaborator newCollaborator = dialog.getCollaborator();
					collaborators.add(newCollaborator);
					checkboxTableViewer.refresh();
					checkboxTableViewer.setSelection(new StructuredSelection(newCollaborator));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		editButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				StructuredSelection selection = (StructuredSelection) checkboxTableViewer.getSelection();
				if(!selection.isEmpty())
				{
					ProjectCollaborator selectedCollaborator = (ProjectCollaborator) selection.getFirstElement();
					dialog.setCollaborator(selectedCollaborator.getACopy());
					if(dialog.open() == Window.OK)
					{
						ProjectCollaborator editedCollaborator = dialog.getCollaborator();
						@SuppressWarnings("unchecked")
						List<ProjectCollaborator> collaborators = 
								(List<ProjectCollaborator>) checkboxTableViewer.getInput();
						collaborators.set(collaborators.indexOf(selectedCollaborator), editedCollaborator);
						checkboxTableViewer.refresh();
						checkboxTableViewer.setSelection(new StructuredSelection(editedCollaborator));
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		removeButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				@SuppressWarnings("unchecked")
				List<ProjectCollaborator> collaborators = 
						(List<ProjectCollaborator>) checkboxTableViewer.getInput();
				StructuredSelection selection = (StructuredSelection) checkboxTableViewer.getSelection();
				if(!selection.isEmpty())
				{
					ProjectCollaborator selectedCollaborator =
							(ProjectCollaborator) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					collaborators.remove(selectedCollaborator);
					checkboxTableViewer.refresh();
					selectionIndex = Math.max(selectionIndex - 1, 0);
					checkboxTableViewer.getTable().select(selectionIndex);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

	}

	private Button createButton(Composite composite, String buttonLabel)
	{
		Button button = new Button(composite, SWT.None);
		button.setText(buttonLabel);
		GridData addButtonData = new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 1);
		addButtonData.widthHint = 80;
		button.setLayoutData(addButtonData);

		GridData compositeLayoutData = new GridData(SWT.END, SWT.BEGINNING, true, true);
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);
		return button;
	}

	@Override
	protected void performDefaults()
	{
		defaultCollaborators = new ArrayList<ProjectCollaborator>(); 
		otherCollaborators = new ArrayList<ProjectCollaborator>();
		initializeTable();
		super.performDefaults();
	}

	@Override
	protected void performApply() 
	{
		String errorMessage = validateInput();
		setErrorMessage(errorMessage);
		if(errorMessage == null)
		{
			save();
		}
	}

	@Override
	public boolean performOk()
	{
		String errorMessage = validateInput();
		setErrorMessage(errorMessage);
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Collaborator\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		@SuppressWarnings("unchecked")
		List<ProjectCollaborator> tableInput = 
				(List<ProjectCollaborator>) checkboxTableViewer.getInput();
		List<ProjectCollaborator> collaborators = new ArrayList<ProjectCollaborator>();
		for(ProjectCollaborator projectCollaborator : tableInput)
		{
			if(!SaveCollaborator.isUnique(collaborators, projectCollaborator))
			{
				errorMessage = "Similar collaborators : " + projectCollaborator.getName();
				break;
			}
			collaborators.add(projectCollaborator);
		}
		return errorMessage;
	}

	private boolean save()
	{
		@SuppressWarnings("unchecked")
		List<ProjectCollaborator> tableInput = 
				(List<ProjectCollaborator>) checkboxTableViewer.getInput();
		defaultCollaborators = new ArrayList<ProjectCollaborator>();
		otherCollaborators = new ArrayList<ProjectCollaborator>();

		for(ProjectCollaborator projectCollaborator : tableInput)
		{
			if(checkboxTableViewer.getChecked(projectCollaborator))
			{
				defaultCollaborators.add(projectCollaborator);
			}
			else
			{
				otherCollaborators.add(projectCollaborator);
			}
		}
		collaboratorPreference.setSelectedValues(defaultCollaborators);
		collaboratorPreference.setOtherValues(otherCollaborators);
		return collaboratorPreference.savePreference();
	}
}
