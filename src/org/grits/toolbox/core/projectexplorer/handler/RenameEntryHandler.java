
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.File;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.service.impl.GritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utilShare.validator.EntryNameValidator;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;

@SuppressWarnings("restriction")
public class RenameEntryHandler
{
	private static final Logger logger = Logger.getLogger(RenameEntryHandler.class);

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			EHandlerService handlerService, ECommandService commandService, EPartService partService,
			IGritsDataModelService gritsDataModelService, IGritsUIService gritsUIService)
	{
		logger.info("- START COMMAND : Rename entry");
		if(object instanceof Entry)
		{
			Entry selectedEntry = (Entry) object;
			InputDialog dialog = new InputDialog(
					Display.getCurrent().getActiveShell(),
					"Rename Entry", "Give a unique name to this entry.",
					selectedEntry.getDisplayName(), new EntryNameValidator(selectedEntry));

			if(dialog.open() == Window.OK)
			{
				String newName = dialog.getValue().trim();
				if(!newName.equals(selectedEntry.getDisplayName()))
				{
					boolean needToReopen = false;;
					MPartDescriptor partDescriptor = gritsUIService.findPartDescriptorForEntry(selectedEntry);
					if(partDescriptor != null)
					{
						MPart part = gritsUIService.findPartForEntry(selectedEntry);
						needToReopen = part != null && part.getObject() != null && part.isVisible();
						partService.hidePart(part);
						closeChildEntries (selectedEntry, partService, gritsUIService);
					}
					if(renameEntry(gritsDataModelService, selectedEntry, newName) && needToReopen) {
						handlerService.executeHandler(
								commandService.createCommand(ViewSelectedHandler.COMMAND_ID, null));
					}
				}
			}
		}
	}
	
	private void closeChildEntries (Entry selectedEntry, EPartService partService, IGritsUIService gritsUIService) {
		//check for child entries and close them
		for (MPart part: partService.getParts()) {
			if (part.isVisible()) {
				Entry child = (Entry) part.getTransientData().get(GritsUIService.TRANSIENT_DATA_KEY_PART_ENTRY);
				if (child != null && checkIfParent(selectedEntry.getProperty(), child.getProperty()))
					partService.hidePart(part);
			}
		}
	}
	
	private boolean checkIfParent(Property selectedProp, Property child) {
		Property parentProperty = child.getParentProperty();
		if (parentProperty != null && parentProperty.equals(selectedProp)) 
			return true;
		if (parentProperty != null)
			return checkIfParent (selectedProp, parentProperty);
		return false;
	}

	private boolean renameEntry(IGritsDataModelService gritsDataModelService, Entry entry, String newName)
	{
		logger.debug("Renaming entry " + entry.getDisplayName());

		String workspaceLocation = 
				((WorkspaceProperty) gritsDataModelService.getRootEntry().getProperty()).getLocation();
		workspaceLocation = workspaceLocation.substring(0, workspaceLocation.length()-1);
		if(entry.getProperty().getType().equals(ProjectProperty.TYPE))
		{
			try
			{
				File workspaceFolder = new File(workspaceLocation);
				if(workspaceFolder.exists() && workspaceFolder.isDirectory())
				{
					File fileToBeRenamed = null;
					for(File child : workspaceFolder.listFiles())
					{
						logger.debug("Matching with " + child.getName());
						if(child.isDirectory() && child.getName().equals(entry.getDisplayName()))
						{
							fileToBeRenamed = child;
							break;
						}
					}
					if(fileToBeRenamed != null)
					{
						fileToBeRenamed.renameTo(new File(workspaceFolder, newName));
						WorkspaceXMLHandler.renameEntry(entry, newName);
						gritsDataModelService.renameEntry(entry, newName);
						try
						{
							ProjectDetails projectDetails = ProjectDetailsHandler.getProjectDetails(entry);
							if(!projectDetails.getEntryName().equals(newName))
							{
								projectDetails.setEntryName(newName);
								ProjectDetailsHandler.writeProjectDetails(projectDetails);
							}
							ProjectFileHandler.saveProject(entry);
						} catch (Exception e)
						{
							logger.error(e.getMessage(), e);
							ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), 
									"Unable to update project_details.xml file with the new project name", e);
							return false;
						}
					}
				}
			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), 
						"Unable to update workspace file", e);
				return false;
			}
		}
		else
		{
			try
			{
				ProjectFileHandler.renameEntryInProject(entry, newName);
				gritsDataModelService.renameEntry(entry, newName);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), 
						"Unable to update project xml", e);
				return false;
			}
		}

		logger.debug("Renamed entry " + entry.getDisplayName());
		return true;
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		return object instanceof Entry && ((Entry) object).getProperty().isRenamable();
	}
}