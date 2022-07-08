/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.CheckboxStringSorter;

/**
 * 
 * @author sena
 *
 */
//Modified (Sena) March 2017 to inform the user for errors (ticket #826)
public class TaskPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(TaskPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.core.preference.project.tasklist.task";

	public static String lastSelection = null;

	private SingleChoicePreference taskPreference = null;
	private List<String> allTasks = null;
	private String defaultTask = null;

	private CheckboxTableViewer checkboxTableViewer = null;
	private TableViewerColumn tableColumn2 = null;

	@Override
	protected Control createContents(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 15;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		Label label = new Label(container, SWT.None);
		label.setText("Tasks");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createTasksTableViewer(container);

		Button addButton = createButton(container, "Add", true);
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				final String TASK_PREFIX = "Task ";

				int newTaskCount = 1;
				while(allTasks.contains(TASK_PREFIX + newTaskCount))
				{
					newTaskCount++;
				}

				String newTask = TASK_PREFIX + newTaskCount;
				allTasks.add(newTask);

				// for only one task make it default
				if(allTasks.size() == 1)
				{
					defaultTask = newTask;
				}

				checkboxTableViewer.refresh();
				tableColumn2.getViewer().editElement(newTask, 1);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		Button removeButton = createButton(container, "Remove", false);
		removeButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				remove();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				remove();
			}

			private void remove()
			{
				StructuredSelection selection =
						((StructuredSelection) checkboxTableViewer.getSelection());
				if(!selection.isEmpty())
				{
					String selectedTask = 
							(String) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					allTasks.remove(selectedTask);
					if(selectedTask.equals(defaultTask))
					{
						defaultTask = allTasks.isEmpty() ?
								null : allTasks.iterator().next();
					}
					checkboxTableViewer.refresh();

					// select next value
					if(!allTasks.isEmpty())
					{
						selectionIndex = Math.min(selectionIndex, allTasks.size() - 1);
						checkboxTableViewer.setSelection(new StructuredSelection(
								checkboxTableViewer.getElementAt(selectionIndex)));
					}
				}
			}
		});

		checkboxTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				StructuredSelection selection = (StructuredSelection)
						checkboxTableViewer.getSelection();
				lastSelection = selection.isEmpty() ?
						lastSelection : (String) selection.getFirstElement();
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
				return element.equals(defaultTask);
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				defaultTask = (String) event.getElement();
			}
		});

		loadValues();
		initializeTable();
		return container;
	}

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.Preference.TASK.getPreferenceName());

		taskPreference  =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.TASK);

		allTasks = new ArrayList<String>();
		allTasks.addAll(taskPreference.getAllValues());
		defaultTask = taskPreference.getDefaultValue();
	}

	private Button createButton(Composite container, String buttonText, boolean grabHorizontal)
	{
		Button button = new Button(container, SWT.None);
		button.setText(buttonText);
		GridData buttonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonData.widthHint = 80;
		buttonData.verticalSpan = 1;
		buttonData.grabExcessHorizontalSpace = grabHorizontal;
		buttonData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		buttonData.grabExcessVerticalSpace = true;
		button.setLayoutData(buttonData);
		return button;
	}

	private void initializeTable()
	{
		Collections.sort(allTasks, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(allTasks);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createTasksTableViewer(Composite container)
	{
		Table tasksTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		tasksTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(tasksTable, "Default", true);

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Tasks");
		tableColumn2.getColumn().setWidth(300);
		tableColumn2.setEditingSupport(new EditingSupport(tableViewer)
		{
			TextCellEditor textEditor = new TextCellEditor(tableViewer.getTable());

			@Override
			protected void setValue(Object element, Object value)
			{
				if(element instanceof String 
						&& value instanceof String)
				{
					String newValue = ((String) value).trim();
					if(checkboxTableViewer.getChecked(element))
						defaultTask = newValue;
					if(allTasks.contains(element))
						allTasks.set(allTasks.indexOf(element), newValue);
					checkboxTableViewer.refresh();
					checkboxTableViewer.setSelection(new StructuredSelection(newValue));
				}
			}

			@Override
			protected Object getValue(Object element)
			{
				return element;
			}

			@Override
			protected CellEditor getCellEditor(Object element)
			{
				return element instanceof String ? textEditor : null;
			}

			@Override
			protected boolean canEdit(Object element)
			{
				return element instanceof String;
			}
		});

		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new GenericListContentProvider());
		tableViewer.setLabelProvider(new StringLabelProvider());

		tableColumn2.getColumn().addSelectionListener(
				new CheckboxStringSorter(tableViewer));
		return tableViewer;
	}

	@Override
	protected void performDefaults()
	{
		Map<String, Boolean> allTasksMap =
				taskPreference.loadDefaultValues(
						ProjectPreferenceStore.Preference.TASK);

		allTasks = new ArrayList<String>();
		defaultTask = null;
		for(String task : allTasksMap.keySet())
		{
			allTasks.add(task);
			if(allTasksMap.get(task))
			{
				defaultTask = task;
			}
		}
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Task\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		Set<String> uniqueTasks = new HashSet<String>();
		uniqueTasks.addAll(allTasks);
		errorMessage = uniqueTasks.size() == allTasks.size() ?
				errorMessage : "Non-unique Tasks";
		if(uniqueTasks.contains(""))
		{
			errorMessage = "Empty Task";
			checkboxTableViewer.setSelection(new StructuredSelection(""));
		}
		return errorMessage;
	}

	private boolean save()
	{
		taskPreference.setAllValues(new HashSet<String>());
		taskPreference.setDefaultValue(null);
		for(String task : allTasks)
		{
			if(checkboxTableViewer.getChecked(task)
					&& taskPreference.getDefaultValue() == null)
			{
				taskPreference.setDefaultValue(task);
			}
			taskPreference.addValue(task);
		}
		return taskPreference.savePreference();
	}
}