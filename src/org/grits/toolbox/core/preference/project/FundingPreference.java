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
public class FundingPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(FundingPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.core.preference.project.collaborator.funding";

	public static String lastSelection = null;

	private SingleChoicePreference fundingPreference = null;
	private List<String> allFundings = null;
	private String defaultFunding = null;

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
		label.setText("Fundings");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createFundingsTableViewer(container);

		Button addButton = createButton(container, "Add", true);
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				final String FUNDING_PREFIX = "Funding ";

				int newFundingCount = 1;
				while(allFundings.contains(FUNDING_PREFIX + newFundingCount))
				{
					newFundingCount++;
				}

				String newFunding = FUNDING_PREFIX + newFundingCount;
				allFundings.add(newFunding);

				// for only one funding make it default
				if(allFundings.size() == 1)
				{
					defaultFunding = newFunding;
				}

				checkboxTableViewer.refresh();
				tableColumn2.getViewer().editElement(newFunding, 1);
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
					String selectedFunding = 
							(String) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					allFundings.remove(selectedFunding);
					if(selectedFunding.equals(defaultFunding))
					{
						defaultFunding = allFundings.isEmpty() ?
								null : allFundings.iterator().next();
					}
					checkboxTableViewer.refresh();

					// select next value
					if(!allFundings.isEmpty())
					{
						selectionIndex = Math.min(selectionIndex, allFundings.size() - 1);
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
				return element.equals(defaultFunding);
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				defaultFunding = (String) event.getElement();
			}
		});

		loadValues();
		initializeTable();
		return container;
	}

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.Preference.FUNDING.getPreferenceName());

		fundingPreference  =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.FUNDING);

		allFundings = new ArrayList<String>();
		allFundings.addAll(fundingPreference.getAllValues());
		defaultFunding = fundingPreference.getDefaultValue();
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
		Collections.sort(allFundings, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(allFundings);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createFundingsTableViewer(Composite container)
	{
		Table fundingsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		fundingsTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(fundingsTable, "Default", true);

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Fundings");
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
						defaultFunding = newValue;
					if(allFundings.contains(element))
						allFundings.set(allFundings.indexOf(element), newValue);
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
		Map<String, Boolean> allFundingsMap =
				fundingPreference.loadDefaultValues(
						ProjectPreferenceStore.Preference.FUNDING);

		allFundings = new ArrayList<String>();
		defaultFunding = null;
		for(String funding : allFundingsMap.keySet())
		{
			allFundings.add(funding);
			if(allFundingsMap.get(funding))
			{
				defaultFunding = funding;
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Funding\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		String errorMessage = null;
		Set<String> uniqueFundings = new HashSet<String>();
		uniqueFundings.addAll(allFundings);
		errorMessage = uniqueFundings.size() == allFundings.size() ?
				errorMessage : "Non-unique Fundings";
		if(uniqueFundings.contains(""))
		{
			errorMessage = "Empty Funding";
			checkboxTableViewer.setSelection(new StructuredSelection(""));
		}
		return errorMessage;
	}

	private boolean save()
	{
		fundingPreference.setAllValues(new HashSet<String>());
		fundingPreference.setDefaultValue(null);
		for(String funding : allFundings)
		{
			if(checkboxTableViewer.getChecked(funding)
					&& fundingPreference.getDefaultValue() == null)
			{
				fundingPreference.setDefaultValue(funding);
			}
			fundingPreference.addValue(funding);
		}
		return fundingPreference.savePreference();
	}
}