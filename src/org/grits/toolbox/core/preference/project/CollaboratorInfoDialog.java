/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.wizard.addcollaborator.CollaboratorEditListener;
import org.grits.toolbox.core.wizard.addcollaborator.ProjectCollaboratorUI;

/**
 * 
 *
 */
public class CollaboratorInfoDialog extends TitleAreaDialog implements CollaboratorEditListener
{
	private static final Logger logger = Logger.getLogger(CollaboratorInfoDialog.class);

	private ProjectCollaborator collaborator = null;
	private ProjectCollaboratorUI projectCollaboratorUI = null;

	public CollaboratorInfoDialog(Shell parentShell)
	{
		super(parentShell);
		projectCollaboratorUI  = new ProjectCollaboratorUI(this);
	}

	public void setCollaborator(ProjectCollaborator collaborator)
	{
		this.collaborator = collaborator;
	}

	public ProjectCollaborator getCollaborator()
	{
		return this.collaborator;
	}

	@Override
	public void create()
	{
		super.create();
		super.setTitle("Project Collaborator");
		if(collaborator == null)
		{
			super.setMessage("Create a new Collaborator");
			collaborator = new ProjectCollaborator();
			collaborator.setName("Collaborator");
			collaborator.setPosition(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.POSITION).getDefaultValue());
			collaborator.setCountry(ProjectPreferenceStore.StringPreference.COUNTRY.getValue());
			collaborator.setFundingAgency(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.FUNDING).getDefaultValue());
		}
		else
		{
			super.setMessage(collaborator.getName());
		}
		projectCollaboratorUI.setCollaborator(collaborator);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.debug("START : Creating Colloborator UI");

		projectCollaboratorUI.createPartControl(parent);

		logger.debug("END   : Creating Colloborator UI");

		return parent;
	}

	@Override
	public void valueEdited()
	{

	}

}
