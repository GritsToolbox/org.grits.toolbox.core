/**
 * 
 */
package org.grits.toolbox.core.wizard.addcollaborator.pages;


import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.wizard.addcollaborator.CollaboratorEditListener;
import org.grits.toolbox.core.wizard.addcollaborator.ProjectCollaboratorUI;

/**
 * 
 *
 */
public class AddNewCollaborator extends WizardPage implements CollaboratorEditListener
{
	private static Logger logger = Logger.getLogger(AddNewCollaborator.class);
	private static String title = "Add Collaborator";
	private ProjectCollaboratorUI projectCollaboratorUI = null;
	public Button addToPreference = null;

	public AddNewCollaborator()
	{
		super(title );
		setTitle(title);
		setMessage("Add a new collaborator");
		projectCollaboratorUI  = new ProjectCollaboratorUI(this);
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.debug("START : Creating Colloborator UI");

		projectCollaboratorUI.createPartControl(parent);
		addToPreference = new Button(projectCollaboratorUI.getComposite(), SWT.CHECK);
		addToPreference.setText("Add to Preference");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 2;
		addToPreference.setLayoutData(gridData);
		setControl(projectCollaboratorUI.getComposite());

		logger.debug("END   : Creating Colloborator UI");
	}


	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if(visible)
		{
			if(projectCollaboratorUI.getCollaborator() == null)
			{
				ProjectCollaborator newCollaborator = new ProjectCollaborator();
				newCollaborator.setName("Collaborator");
				newCollaborator.setPosition(ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.POSITION).getDefaultValue());
				newCollaborator.setCountry(ProjectPreferenceStore.StringPreference.COUNTRY.getValue());
				newCollaborator.setFundingAgency(ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.FUNDING).getDefaultValue());
				projectCollaboratorUI.setCollaborator(newCollaborator);
			}
		}
	}
	public ProjectCollaborator getCollaborator()
	{
		return this.projectCollaboratorUI.getCollaborator();
	}

	@Override
	public void valueEdited()
	{
		setPageComplete(projectCollaboratorUI.getErrorMessage() == null);
		setErrorMessage(projectCollaboratorUI.getErrorMessage());
		getContainer().updateButtons();
	}
}
