
package org.grits.toolbox.core.part.toolitem;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.part.CollaboratorPart;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

public class SaveCollaborator
{
	private static final Logger logger = Logger.getLogger(SaveCollaborator.class);
	private static MultiChoicePreference<ProjectCollaborator> collaboratorPreference =
			ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.COLLABORATOR);

	@Execute
	public void execute(MDirectToolItem toolbarItem, MPart collaboratorPart)
	{
		logger.info("Adding collaborator to preference");
		if(collaboratorPart != null && collaboratorPart.getObject() != null)
		{
			ProjectCollaborator collaborator = ((CollaboratorPart) collaboratorPart.getObject()).getCollaborator();
			saveToPreference(collaborator, false);
			toolbarItem.setEnabled(false);
		}
		logger.info("Collaborator added to preference");
	}

	public static void saveToPreference(ProjectCollaborator collaborator, boolean silentMode)
	{
		if(collaborator != null)
		{
			logger.info("Adding collaborator to preference : " + collaborator.getName());

			if(isUnique(collaborator))
			{
				collaboratorPreference.addOtherValue(collaborator.getACopy());
				collaboratorPreference.savePreference();
			}
			else
			{
				String duplicateMessage = "Cannot add this collaborator \""
						+ collaborator.getName() +
						"\" to preference. This collaborator (collaborator with same name,"
						+ " funding agency and grant number) already exists.";
				logger.warn(duplicateMessage);
				if(!silentMode)
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							"Duplicate Collaborator", duplicateMessage);
				}
			}
		}
	}

	public static boolean isUnique(ProjectCollaborator collaborator)
	{
		return isUnique(collaboratorPreference.getSelectedValues(), collaborator)
				&& isUnique(collaboratorPreference.getOtherValues(), collaborator);
	}

	public static boolean isUnique(List<ProjectCollaborator> collaborators,
			ProjectCollaborator projectCollaborator)
	{
		boolean unique = true;
		if(projectCollaborator != null)
		{
			for(ProjectCollaborator thisCollaborator : collaborators)
			{
				if(thisCollaborator.matches(projectCollaborator))
				{
					unique = false;
					break;
				}
			}
		}
		return unique;
	}
}