
package org.grits.toolbox.core.part.toolitem;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.part.TaskPart;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

public class SaveTask
{
	private static final Logger logger = Logger.getLogger(SaveTask.class);
	private static MultiChoicePreference<ProjectTasklist> tasklistPreference = 
			ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.TASKLIST);

	@Execute
	public void execute(MDirectToolItem toolbarItem, MPart taskPart)
	{
		logger.info("Adding project task to preference");
		if(taskPart != null && taskPart.getObject() != null)
		{

			ProjectTasklist tasklist = ((TaskPart) taskPart.getObject()).getProjectTasklist();
			saveToPreference(tasklist, false);
			toolbarItem.setEnabled(false);
		}

		logger.info("Project task added to preference");
	}

	public static void saveToPreference(ProjectTasklist tasklist, boolean silentMode)
	{
		if(tasklist != null)
		{
			logger.info("Adding task to preference : " + tasklist.getTask() +
					" ( "+ tasklist.getPerson() + " )");

			if(isUnique(tasklist))
			{
				tasklistPreference.addOtherValue(tasklist.getACopy());
				tasklistPreference.savePreference();
			}
			else
			{
				String duplicateMessage = "Cannot add this task \""+ tasklist.getTask() +
						" ( "+ tasklist.getPerson() + " )\" to preference."
						+ " Same pair of \"Task ( Person )\" already exists.";
				logger.warn(duplicateMessage);
				if(!silentMode)
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							"Duplicate Task", duplicateMessage);
				}
			}
		}
	}

	public static boolean isUnique(ProjectTasklist tasklist)
	{
		return isUnique(tasklistPreference.getSelectedValues(), tasklist)
				&& isUnique(tasklistPreference.getOtherValues(), tasklist);
	}

	public static boolean isUnique(List<ProjectTasklist> tasklist,
			ProjectTasklist projectTasklist)
	{
		boolean unique = true;
		if(projectTasklist != null)
		{
			for(ProjectTasklist thisTasklist : tasklist)
			{
				if(thisTasklist.matches(projectTasklist))
				{
					unique = false;
					break;
				}
			}
		}
		return unique;
	}
}