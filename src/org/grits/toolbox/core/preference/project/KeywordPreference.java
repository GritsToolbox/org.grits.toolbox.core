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
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.CheckboxStringSorter;

/**
 * 
 * @author sena
 *
 */
//Modified (Sena) March 2017 to inform the user for errors (ticket #826)
public class KeywordPreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(KeywordPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.core.preference.project.keyword";

	public static String lastSelection = null;

	private MultiChoicePreference<String> keywordPreference = null;
	private List<String> otherKeywords = null;
	private List<String> defaultKeywords = null;

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
		label.setText("Keywords");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createKeywordsTableViewer(container);

		Button addButton = createButton(container, "Add", true);
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				@SuppressWarnings("unchecked")
				List<String> allKeywords = 
				(List<String>) checkboxTableViewer.getInput();
				final String KEYWORD_PREFIX = "Keyword ";

				int newKeywordCount = 1;
				while(allKeywords.contains(KEYWORD_PREFIX + newKeywordCount))
				{
					newKeywordCount++;
				}

				String newKeyword = KEYWORD_PREFIX  + newKeywordCount;

				otherKeywords.add(newKeyword);
				allKeywords.add(newKeyword);

				checkboxTableViewer.refresh();
				tableColumn2.getViewer().editElement(newKeyword, 1);
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
					@SuppressWarnings("unchecked")
					List<String> allKeywords = 
					(List<String>) checkboxTableViewer.getInput();

					String selectedKeyword = 
							(String) selection.getFirstElement();
					int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
					if(defaultKeywords.contains(selectedKeyword))
						defaultKeywords.remove(selectedKeyword);
					else
						otherKeywords.remove(selectedKeyword);
					allKeywords.remove(selectedKeyword);
					checkboxTableViewer.refresh();

					// select next value
					if(!allKeywords.isEmpty())
					{
						selectionIndex = Math.min(selectionIndex, allKeywords.size() - 1);
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
				return defaultKeywords.contains(element);
			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				String selectedKeyword = 
						(String) event.getElement();
				boolean checked = event.getChecked();
				if(checked)
				{
					logger.debug("Select keyword : " + selectedKeyword);
					defaultKeywords.add(selectedKeyword);
					otherKeywords.remove(selectedKeyword);
				}
				else
				{
					logger.debug("Unselect keyword : " + selectedKeyword);
					otherKeywords.add(selectedKeyword);
					defaultKeywords.remove(selectedKeyword);
				}
			}
		});

		loadValues();
		initializeTable();
		return container;
	}

	private void loadValues()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.ParameterizedPreference.KEYWORD.getPreferenceName());

		keywordPreference  =
				ProjectPreferenceStore.getMultiChoicePreference(
						ProjectPreferenceStore.ParameterizedPreference.KEYWORD);

		defaultKeywords = new ArrayList<String>();
		defaultKeywords.addAll(keywordPreference.getSelectedValues());
		otherKeywords = new ArrayList<String>();
		otherKeywords.addAll(keywordPreference.getOtherValues());
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
		List<String> allKeywords = new ArrayList<String>();
		allKeywords.addAll(defaultKeywords);
		allKeywords.addAll(otherKeywords);
		Collections.sort(allKeywords, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(allKeywords);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createKeywordsTableViewer(Composite container)
	{
		Table keywordsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		keywordsTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(keywordsTable, "Default");

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Keywords");
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
					@SuppressWarnings("unchecked")
					List<String> allKeywords = 
					(List<String>) checkboxTableViewer.getInput();
					String newValue = ((String) value).trim();
					if(checkboxTableViewer.getChecked(element))
					{
						if(defaultKeywords.contains(element))
							defaultKeywords.set(defaultKeywords.indexOf(element), newValue);
						else
							defaultKeywords.add(newValue);
					}
					else
					{
						if(otherKeywords.contains(element))
							otherKeywords.set(otherKeywords.indexOf(element), newValue);
						else
							otherKeywords.add(newValue);
					}
					if(allKeywords.contains(element))
						allKeywords.set(allKeywords.indexOf(element), newValue);
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
		Map<String, Boolean> allKeywordsMap =
				keywordPreference.getDefaultValuesObjectMap();

		ArrayList<String> allKeywords = new ArrayList<String>();
		defaultKeywords = new ArrayList<String>();
		otherKeywords = new ArrayList<String>();
		for(String keyword : allKeywordsMap.keySet())
		{
			allKeywords.add(keyword);
			if(allKeywordsMap.get(keyword))
				defaultKeywords.add(keyword);
			else
				otherKeywords.add(keyword);
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
		if (errorMessage != null) MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "There is an error in \"Keyword\" preference values. Please fix before saving! Error: " + errorMessage);
		return errorMessage == null ? save() : false;
	}

	private String validateInput()
	{
		@SuppressWarnings("unchecked")
		List<String> allKeywords = 
		(List<String>) checkboxTableViewer.getInput();
		String errorMessage = null;
		Set<String> uniqueKeywords = new HashSet<String>();
		uniqueKeywords.addAll(allKeywords);
		errorMessage = uniqueKeywords.size() == allKeywords.size() ?
				errorMessage : "Duplicate Keywords";
		if(uniqueKeywords.contains(""))
		{
			errorMessage = "Empty Keyword";
			checkboxTableViewer.setSelection(new StructuredSelection(""));
		}

		return errorMessage;
	}

	private boolean save()
	{
		@SuppressWarnings("unchecked")
		List<String> allKeywords = 
		(List<String>) checkboxTableViewer.getInput();
		keywordPreference.setSelectedValues(new ArrayList<String>());
		keywordPreference.setOtherValues(new ArrayList<String>());
		for(String keyword : allKeywords)
		{
			if(checkboxTableViewer.getChecked(keyword))
				keywordPreference.addSelectedValue(keyword);
			else
				keywordPreference.addOtherValue(keyword);
		}
		return keywordPreference.savePreference();
	}
}