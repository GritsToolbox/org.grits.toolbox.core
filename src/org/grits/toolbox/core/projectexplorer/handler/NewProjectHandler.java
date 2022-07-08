 
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.GritsIntroPart;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.GeneralSettings;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.projectexplorer.dialog.NewProjectDialog;
import org.grits.toolbox.core.projectexplorer.dialog.NewProjectInfoDialog;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utils.SettingsHandler;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;

@SuppressWarnings("restriction")
public class NewProjectHandler
{
	private static final Logger logger = Logger.getLogger(NewProjectHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.core.command.projectexplorer.new.project";
	public static final String PARAMETER_CLASS_NAME =
			"org.grits.toolbox.core.commandparameter.eventtrigger.classname";

	@Inject ESelectionService selectionService;
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			ECommandService commandService, EHandlerService handlerService,
			EPartService partService, IGritsDataModelService gritsDataModelService,
			IGritsUIService gritsUIService, @Optional @Named(PARAMETER_CLASS_NAME) String triggeringClass)
	{
		logger.info("creating a new project");
		NewProjectDialog dialog = new NewProjectDialog(PropertyHandler.getModalDialog(shell));
		if (dialog.open() == Window.OK)
		{
			try
			{
				Entry projectEntry = ProjectFileHandler.createProject(
						dialog.getProjectName(), dialog.getDescription());
				try
				{
					WorkspaceXMLHandler.updateWorkspaceXMLFile(projectEntry);
					gritsDataModelService.addProjectEntry(projectEntry);
					gritsUIService.openEntryInPart(projectEntry);
					if(GritsIntroPart.class.getName().equals(triggeringClass))
					{
						//first check the settings to see if the user checked "do not show it again"
						GeneralSettings settings = null;
						try {
							settings = SettingsHandler.readSettings();
						} catch (Exception e) {
							logger.warn("Settings file does not exist yet");
						}
						if (settings == null || !settings.isHiddenDialog(GeneralSettings.SHOWINFO_SETTING))
							(new NewProjectInfoDialog(shell)).open();
					}
				} catch (FileNotFoundException e)
				{
					logger.fatal(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(shell,
							"Workspace file is missing.\n" + e.getMessage(), e);
				} catch (Exception e)
				{
					logger.fatal(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(shell,
							"Error updating workspace.\n" + e.getMessage(), e);
				}
			} catch (FileAlreadyExistsException e)
			{
				logger.fatal(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(shell,
						"There is a project folder already in the workspace with this name."
								+ dialog.getProjectName()
								+ "Please delete this project first.\n" + e.getMessage(), e);
			} catch (IOException e)
			{
				logger.fatal(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(shell,
						"Error creating project.\n" + e.getMessage(), e);
			}
		}
	}
}