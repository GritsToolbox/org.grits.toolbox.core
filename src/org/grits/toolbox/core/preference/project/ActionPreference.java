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
// Modified (Sena) March 2017 to inform the user for errors (ticket #826)
public class ActionPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(ActionPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.core.preference.project.event.action";

	public static String lastSelection = null;

	private SingleChoicePreference actionPreference = null;
	private List<String> allActions = null;
	private String defaultAction = null;

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
		label.setText("Actions");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createActionsTableViewer(container);

		Button addButton = createButton(container, "Add", true);
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				final String ACTION_PREFIX = "Action ";

				int newActionCount = 1;
				while(allActions.contains(ACTION_PREFIX + newActionCount))
				{
					newActionCount++;
				}

				String newAction = ACTION_PREFIX  + newActionCount;
				allActions.add(newAction);

				// for only one action make it default
				if(allActions.size() == 1)
				{
					defaultAction = newAction;
				}

				checkboxTableViewer.refresh();
				tableColumn2.getViewer().editElement(newAction, 1);
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
					String selectedAction = 
							(String) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					allActions.remove(selectedAction);
					if(selectedAction.equals(defaultAction))
					{
						defaultAction = allActions.isEmpty() ?
								null : allActions.iterator().next();
					}
					checkboxTableViewer.refresh();

					// select next value
					if(!allActions.isEmpty())
					{
						selectionIndex = Math.min(selectionIndex, allActions.size() - 1);
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
				return element.equals(defaultAction);
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				defaultAction = (String) event.getElement();
			}
		});

		loadValues();
		initializeTable();
		return container;
	}

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.Preference.ACTION.getPreferenceName());

		actionPreference  =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.ACTION);

		allActions = new ArrayList<String>();
		allActions.addAll(actionPreference.getAllValues());
		defaultAction = actionPreference.getDefaultValue();
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
		Collections.sort(allActions, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(allActions);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createActionsTableViewer(Composite container)
	{
		Table actionsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		actionsTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(actionsTable, "Default", true);

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Actions");
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
						defaultAction = newValue;
					if(allActions.contains(element))
						allActions.set(allActions.indexOf(element), newValue);
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
		Map<String, Boolean> allActionsMap =
				actionPreference.loadDefaultValues(
						ProjectPreferenceStore.Preference.ACTION);

		allActions = new ArrayList<String>();
		defaultAction = null;
		for(String action : allActionsMap.keySet())
		{
			allActions.add(action);
			if(allActionsMap.get(action))
			{
				defaultAction = action;
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Action\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		Set<String> uniqueActions = new HashSet<String>();
		uniqueActions.addAll(allActions);
		errorMessage = uniqueActions.size() == allActions.size() ?
				errorMessage : "Non-unique Actions";
		if(uniqueActions.contains(""))
		{
			errorMessage = "Empty Action";
			checkboxTableViewer.setSelection(new StructuredSelection(""));
		}
		return errorMessage;
	}

	private boolean save()
	{
		actionPreference.setAllValues(new HashSet<String>());
		actionPreference.setDefaultValue(null);
		for(String action : allActions)
		{
			if(checkboxTableViewer.getChecked(action)
					&& actionPreference.getDefaultValue() == null)
			{
				actionPreference.setDefaultValue(action);
			}
			actionPreference.addValue(action);
		}
		return actionPreference.savePreference();
	}
}