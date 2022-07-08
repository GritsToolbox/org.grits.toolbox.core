/**
 * 
 */
package org.grits.toolbox.core.wizard.taskexport.pages;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 *
 */
public class SelectColumnPage extends WizardPage
{
    private static Set<String> DEFAULT_COLUMNS = new HashSet<String>();
    public static final String PROJECT = "Project";
    public static final String NAME_ADDRESS = "Name/Address";
    public static final String GROUP_PI = "Group/P.I.";
    public static final String ROLE = "Role";
    public static final String TASK = "Task";
    public static final String STATUS = "Status";
    public static final String NUMBER_OF_SAMPLES = "#Samples";
    public static final String COMPLETION_DATE = "Completion Date";
	public static final String[] COLUMNS = new String[] {PROJECT, NAME_ADDRESS, 
		GROUP_PI, ROLE, TASK, STATUS, 
		NUMBER_OF_SAMPLES, COMPLETION_DATE};
	private static Logger logger = Logger.getLogger(SelectColumnPage.class);
	private static String title = "Preference";
	public Button[] columnsToExport = null;

	public SelectColumnPage()
	{
		super(title );
		setTitle(title);
		setMessage("Select Columns to export");
	}

	@Override
	public void createControl(Composite parent)
	{
    	logger.debug("START : Creating Columns part");
    	DEFAULT_COLUMNS = new HashSet<String>(Arrays.asList(PROJECT));
        Composite container = new Composite(parent, SWT.FILL);
        GridLayout layout = new GridLayout();
        layout.marginRight = 8;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 10;
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = true;
        container.setLayout(layout);

        Label label = new Label(container, SWT.NONE);
        label.setText("Columns");
        GridData selectExistingData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        selectExistingData.horizontalSpan = 1;
        selectExistingData.verticalSpan = 1;
        label.setLayoutData(selectExistingData);

        createColumnVisibilityPart(container);

		setControl(container);

    	logger.debug("START : Creating Columns part");
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
        String[] buttonNames = COLUMNS;
        columnsToExport = new Button[buttonNames.length];

        int i = 0;
        while(i < buttonNames.length)
        {
            columnsToExport[i] = createCheckbox(composite, buttonNames[i]);
            if(DEFAULT_COLUMNS.contains(buttonNames[i]));
            {
                columnsToExport[i].setSelection(true);
            }
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
}
