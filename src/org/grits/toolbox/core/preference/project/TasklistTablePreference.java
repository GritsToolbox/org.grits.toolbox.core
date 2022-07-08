/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.core.preference.share.MultiChoiceInteger;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

/**
 * 
 *
 */
public class TasklistTablePreference extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(TasklistTablePreference.class);

	private Button[] buttons = null;

	private MultiChoiceInteger columnPreference = null;

	private void loadValuesAndSelect()
	{
		logger.info("Loading preference " +
				ProjectPreferenceStore.IntegerPreference.TASKLIST_TABLE.getPreferenceName());

		columnPreference =
				ProjectPreferenceStore.getMultiChoiceInteger(
						ProjectPreferenceStore.IntegerPreference.TASKLIST_TABLE);

		buttons[0].setSelection(true);
		for(int i = 1; i < TasklistTableColumn.COLUMNS.length; i++)
		{
			buttons[i].setSelection(columnPreference.getSelectedValues().contains(i));
		}
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite container = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NONE);
		label.setText("Visible Columns");
		GridData selectExistingData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		selectExistingData.horizontalSpan = 1;
		selectExistingData.verticalSpan = 1;
		label.setLayoutData(selectExistingData);

		createColumnVisibilityPart(container);

		loadValuesAndSelect();
		return container;
	}

	private void createColumnVisibilityPart(Composite container)
	{
		Composite composite = new Composite(container, SWT.BORDER|SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.marginTop = 10;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);
		String[] buttonNames = TasklistTableColumn.COLUMNS;
		buttons = new Button[buttonNames.length];

		buttons[0] = createCheckbox(composite, buttonNames[0]);
		((GridData) buttons[0].getLayoutData()).horizontalSpan = 2;
		buttons[0].setSelection(true);
		buttons[0].setEnabled(false);
		int i = 1;
		while(i < buttonNames.length)
		{
			buttons[i] = createCheckbox(composite, buttonNames[i]);
			i++;
		}

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.horizontalSpan = 1;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData );
	}

	private Button createCheckbox(Composite composite, String label)
	{
		Button checkButton = new Button(composite, SWT.CHECK);
		checkButton.setText(label);
		GridData createNewData = new GridData(GridData.FILL_HORIZONTAL);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		checkButton.setLayoutData(createNewData);
		return checkButton;
	}

	@Override
	protected void performDefaults()
	{
		Map<Integer, Boolean> allSelectedMap =
				columnPreference.getDefaultValuesObjectMap();
		buttons[0].setSelection(true);
		for(int i = 1; i < TasklistTableColumn.COLUMNS.length; i++)
		{
			buttons[i].setSelection(allSelectedMap.get(i));
		}
		super.performDefaults();
	}

	@Override
	protected void performApply()
	{
		save();
	}

	@Override
	public boolean performOk()
	{
		return save();
	}

	private boolean save()
	{
		columnPreference.setSelectedValues(new ArrayList<Integer>());
		columnPreference.setOtherValues(new ArrayList<Integer>());
		int columnNumber = 0;
		for(Button button : buttons)
		{
			columnNumber = TasklistTableColumn.getColumnNumber(button.getText());
			if(button.getSelection())
			{
				columnPreference.addSelectedValue(columnNumber);
			}
			else
			{
				columnPreference.addOtherValue(columnNumber);
			}
		}
		return columnPreference.savePreference();
	}
}
