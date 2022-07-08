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
public class StatusPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(StatusPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.core.preference.project.tasklist.status";

	public static String lastSelection = null;

	private SingleChoicePreference statusPreference = null;
	private List<String> allStatus = null;
	private String defaultStatus = null;

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
		label.setText("Status");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createStatusTableViewer(container);

		Button addButton = createButton(container, "Add", true);
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				final String STATUS_PREFIX = "Status ";

				int newStatusCount = 1;
				while(allStatus.contains(STATUS_PREFIX + newStatusCount))
				{
					newStatusCount++;
				}

				String newStatus = STATUS_PREFIX + newStatusCount;
				allStatus.add(newStatus);

				// for only one status make it default
				if(allStatus.size() == 1)
				{
					defaultStatus = newStatus;
				}

				checkboxTableViewer.refresh();
				tableColumn2.getViewer().editElement(newStatus, 1);
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
					String selectedStatus = 
							(String) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					allStatus.remove(selectedStatus);
					if(selectedStatus.equals(defaultStatus))
					{
						defaultStatus = allStatus.isEmpty() ?
								null : allStatus.iterator().next();
					}
					checkboxTableViewer.refresh();

					// select next value
					if(!allStatus.isEmpty())
					{
						selectionIndex = Math.min(selectionIndex, allStatus.size() - 1);
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
				return element.equals(defaultStatus);
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				defaultStatus = (String) event.getElement();
			}
		});

		loadValues();
		initializeTable();
		return container;
	}

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.Preference.STATUS.getPreferenceName());

		statusPreference  =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.STATUS);

		allStatus = new ArrayList<String>();
		allStatus.addAll(statusPreference.getAllValues());
		defaultStatus = statusPreference.getDefaultValue();
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
		Collections.sort(allStatus, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(allStatus);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createStatusTableViewer(Composite container)
	{
		Table statussTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		statussTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(statussTable, "Default", true);

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Status");
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
						defaultStatus = newValue;
					if(allStatus.contains(element))
						allStatus.set(allStatus.indexOf(element), newValue);
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
		Map<String, Boolean> allStatusMap =
				statusPreference.loadDefaultValues(
						ProjectPreferenceStore.Preference.STATUS);

		allStatus = new ArrayList<String>();
		defaultStatus = null;
		for(String status : allStatusMap.keySet())
		{
			allStatus.add(status);
			if(allStatusMap.get(status))
			{
				defaultStatus = status;
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Status\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		Set<String> uniqueStatus = new HashSet<String>();
		uniqueStatus.addAll(allStatus);
		errorMessage = uniqueStatus.size() == allStatus.size() ?
				errorMessage : "Non-unique Status";
		if(uniqueStatus.contains(""))
		{
			errorMessage = "Empty Status";
			checkboxTableViewer.setSelection(new StructuredSelection(""));
		}

		return errorMessage;
	}

	private boolean save()
	{
		statusPreference.setAllValues(new HashSet<String>());
		statusPreference.setDefaultValue(null);
		for(String status : allStatus)
		{
			if(checkboxTableViewer.getChecked(status)
					&& statusPreference.getDefaultValue() == null)
			{
				statusPreference.setDefaultValue(status);
			}
			statusPreference.addValue(status);
		}
		return statusPreference.savePreference();
	}
}