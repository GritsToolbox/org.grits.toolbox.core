package org.grits.toolbox.core.projectexplorer.dialog;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ModalDialog;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.core.utilShare.validator.EntryNameValidator;

/**
 * Dialog for creating a new project.
 * It has error message handler, length and duplicate name checking methods.
 * It has to use GridLayout.
 * OK and CANCEL buttons are provided.
 * @author Ki Tae Myoung
 *
 */
public class NewProjectDialog extends ModalDialog{

	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewProjectDialog.class);

	private Text projectNameText = null;
	private String projectName = null;
	private Label projectNameLabel = null;

	private Text descriptionText = null;
	private String description = null;
	private Label descriptionLabel = null;

	private Entry workspaceEntry = null;
	private Set<String> existingNames = null;

	private Button okButton = null;

	public NewProjectDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("New Project");
		setMessage("To create a new project");
		okButton.setEnabled(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		logger.debug("START : Opening the new Project Dialog");

		//has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);

		/*
		 * First row starts
		 */
		GridData projectNameData = new GridData();
		projectNameLabel = new Label(parent, SWT.NONE);
		projectNameLabel.setText("Name");
		projectNameLabel = setMandatoryLabel(projectNameLabel);
		projectNameLabel.setLayoutData(projectNameData);

		GridData projectNameTextData = new GridData();
		projectNameTextData.grabExcessHorizontalSpace = true;
		projectNameTextData.horizontalAlignment = GridData.FILL;
		projectNameTextData.horizontalSpan = 3;
		projectNameText = new Text(parent, SWT.BORDER);
		projectNameText.setLayoutData(projectNameTextData);

		workspaceEntry = DataModelHandler.instance().getRoot();
		existingNames = EntryNameValidator.getExistingNames(workspaceEntry, ProjectProperty.TYPE, null);
		projectNameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				okButton.setEnabled(isValidInput());
			}
		});

		/*
		 * Second row starts
		 */
		GridData descriptionData = new GridData();
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(descriptionData);

		GridData descriptionTextData = new GridData();
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.grabExcessVerticalSpace = true;
		descriptionTextData.horizontalAlignment = GridData.FILL;
		descriptionTextData.horizontalSpan = 3;
		descriptionText = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.addTraverseListener(ListenerFactory.getTabTraverseListener());
		descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());

		//then add separator
		createSeparator(4);

		createButtonCancel(parent);
		okButton  = createButtonOK(parent);

		logger.debug("END   : Opening the new Project Dialog");

		return parent;
	}

	@Override
	protected boolean isValidInput() {

		//basic check!
		if(!checkBasicLengthCheck(projectNameLabel, projectNameText, 0, 32))
		{
			return false;
		}

		String errorMessage = EntryNameValidator.validateProjectName(
				existingNames, projectNameText.getText().trim());
		if(errorMessage != null)
		{
			setError(projectNameLabel, errorMessage);
			return false;
		}
		else
		{
			//if OK then remove error
			removeError(projectNameLabel);
		}

		if(!checkBasicLengthCheck(descriptionLabel, descriptionText, 0, Integer.parseInt(PropertyHandler.getVariable("descriptionLength"))))
		{
			return false;
		}

		return true;
	}

	//when OK button is pressed what to do!
	protected void okPressed() {
		saveInput();
	}

	// Copy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	protected void saveInput()
	{
		projectName = projectNameText.getText().trim();
		description = descriptionText.getText().trim();
	}

	/**
	 * create an entry 
	 */
	public Entry createEntry()
	{
		return null;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getProjectName()
	{
		return projectName;
	}

} 