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
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
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
public class EventPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(EventPreference.class);

	private MultiChoicePreference<ProjectEvent> eventPreference = null;
	public List<ProjectEvent> defaultEvents = null;
	public List<ProjectEvent> otherEvents = null;

	private CheckboxTableViewer checkboxTableViewer = null;
	private Button addButton = null;
	private Button editButton = null;
	private Button removeButton = null;
	private AddEditEventDialog dialog = null;

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.ParameterizedPreference.EVENT.getPreferenceName());

		eventPreference = ProjectPreferenceStore.getMultiChoicePreference(
						ProjectPreferenceStore.ParameterizedPreference.EVENT);

		ProjectEvent copiedEvent = null;
		defaultEvents = new ArrayList<ProjectEvent>();
		for(ProjectEvent event : eventPreference.getSelectedValues())
		{
			copiedEvent = event.getACopy();
			copiedEvent.setAddByDefault(true);
			defaultEvents.add(copiedEvent);
		}

		otherEvents = new ArrayList<ProjectEvent>();
		for(ProjectEvent event : eventPreference.getOtherValues())
		{
			copiedEvent = event.getACopy();
			copiedEvent.setAddByDefault(false);
			otherEvents.add(copiedEvent);
		}

		dialog = new AddEditEventDialog(getShell());
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
		Table eventsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 3;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 150;
		tableLayouData.heightHint = 300;
		eventsTable.setLayoutData(tableLayouData);
		checkboxTableViewer = new CheckboxTableViewer(eventsTable, "Default");

		TableViewerColumn tableColumn2 = new TableViewerColumn(checkboxTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Event");
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
						&& element instanceof ProjectEvent)
				{
					ProjectEvent event = ((ProjectEvent) element);
					value = event.getProjectAction().getAction();
					value += event.getDescription() != null 
							? " " : event.getDescription();
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
					ProjectEvent selectedEvent = (ProjectEvent) selection.getFirstElement();
					dialog.setProjectEvent(selectedEvent.getACopy());
					if(dialog.open() == Window.OK)
					{
						ProjectEvent editedEvent = dialog.getProjectEvent();
						@SuppressWarnings("unchecked")
						List<ProjectEvent> events = 
								(List<ProjectEvent>) checkboxTableViewer.getInput();
						events.set(events.indexOf(selectedEvent), editedEvent);
						checkboxTableViewer.refresh();
						checkboxTableViewer.setSelection(new StructuredSelection(editedEvent));
					}
				}
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				ProjectEvent projectEvent = 
						(ProjectEvent) event.getElement();
				boolean checked = event.getChecked();
				projectEvent.setAddByDefault(checked);
				checkboxTableViewer.setChecked(projectEvent, checked);
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
				if(element instanceof ProjectEvent)
				{
					return ((ProjectEvent) element).isAddByDefault();
				}
				return false;
			}
		});
		addViewerComparator(checkboxTableViewer);
	}

	private void initializeTable()
	{
		List<ProjectEvent> input = new ArrayList<ProjectEvent>();
		input.addAll(otherEvents);
		input.addAll(defaultEvents);
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
				List<ProjectEvent> events = 
						(List<ProjectEvent>) checkboxTableViewer.getInput();
				dialog.setProjectEvent(null);
				if(dialog.open() == Window.OK)
				{
					ProjectEvent newEvent = dialog.getProjectEvent();
					events.add(newEvent);
					checkboxTableViewer.refresh();
					checkboxTableViewer.setSelection(new StructuredSelection(newEvent));
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
					ProjectEvent selectedEvent = (ProjectEvent) selection.getFirstElement();
					dialog.setProjectEvent(selectedEvent.getACopy());
					if(dialog.open() == Window.OK)
					{
						ProjectEvent editedEvent = dialog.getProjectEvent();
						@SuppressWarnings("unchecked")
						List<ProjectEvent> events = 
								(List<ProjectEvent>) checkboxTableViewer.getInput();
						events.set(events.indexOf(selectedEvent), editedEvent);
						checkboxTableViewer.refresh();
						checkboxTableViewer.setSelection(new StructuredSelection(editedEvent));
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
				List<ProjectEvent> events = 
						(List<ProjectEvent>) checkboxTableViewer.getInput();
				StructuredSelection selection = (StructuredSelection) checkboxTableViewer.getSelection();
				if(!selection.isEmpty())
				{
					ProjectEvent selectedEvent =
							(ProjectEvent) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					events.remove(selectedEvent);
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
		defaultEvents = new ArrayList<ProjectEvent>(); 
		otherEvents = new ArrayList<ProjectEvent>();
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Event\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		@SuppressWarnings("unchecked")
		List<ProjectEvent> tableInput = 
				(List<ProjectEvent>) checkboxTableViewer.getInput();
		List<ProjectEvent> events = new ArrayList<ProjectEvent>();
		for(ProjectEvent projectEvent : tableInput)
		{
			if(!isUnique(events, projectEvent))
			{
				errorMessage = "Similar Events : " + projectEvent.getProjectAction().getAction();
				break;
			}
			events.add(projectEvent);
		}
		return errorMessage;
	}

	private boolean isUnique(List<ProjectEvent> events, ProjectEvent projectEvent)
	{
		boolean unique = true;
		for(ProjectEvent thisProjectEvent : events)
		{
			if(thisProjectEvent.matches(projectEvent))
			{
				unique = false;
				break;
			}
		}
		return unique;
	}

	private boolean save()
	{
		@SuppressWarnings("unchecked")
		List<ProjectEvent> tableInput = 
				(List<ProjectEvent>) checkboxTableViewer.getInput();
		defaultEvents = new ArrayList<ProjectEvent>();
		otherEvents = new ArrayList<ProjectEvent>();

		for(ProjectEvent projectEvent : tableInput)
		{
			if(checkboxTableViewer.getChecked(projectEvent))
			{
				defaultEvents.add(projectEvent);
			}
			else
			{
				otherEvents.add(projectEvent);
			}
		}
		eventPreference.setSelectedValues(defaultEvents);
		eventPreference.setOtherValues(otherEvents);
		return eventPreference.savePreference();
	}
}
