
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.MissingReaderException;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;

@SuppressWarnings("restriction")
public class OpenProjectHandler
{
	private static final Logger logger = Logger.getLogger(OpenProjectHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.core.command.projectexplorer.open.project";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object selection,
			ECommandService commandService, EHandlerService handlerService,
			EPartService partService, ESelectionService selectionService,
			IGritsDataModelService gritsDataModelService)
	{
		if (selection instanceof Entry) {
			Entry entry = (Entry)selection;
			if(entry != null && entry.getProperty() != null
					&& entry.getProperty() instanceof ProjectProperty)
			{
				openProject(entry, gritsDataModelService, commandService, handlerService, selectionService, partService);
			}
		} else if(selection instanceof StructuredSelection)
		{
			Iterator<?> iterator = ((StructuredSelection) selection).iterator();
			Object nextSelection = null;
			Entry entry = null;
			while(iterator.hasNext())
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					entry = (Entry) nextSelection;
					if (entry.getProperty() != null && entry.getProperty() instanceof ProjectProperty)
						openProject(entry, gritsDataModelService, commandService, handlerService, selectionService, partService);
				}
			}
		}
	}
	
	private void openProject (Entry entry, IGritsDataModelService gritsDataModelService,
			ECommandService commandService, 
			EHandlerService handlerService,
			ESelectionService selectionService,
			EPartService partService) {
		ProjectProperty projectProperty = (ProjectProperty) entry.getProperty();
		try
		{
			// reloading the entry makes sure that the project entry is not corrupt
			gritsDataModelService.getRootEntry().removeChild(entry);
			entry = ProjectFileHandler.loadProject(entry.getDisplayName());
			if(!projectProperty.isOpen())
				projectProperty.setOpen(true);
			gritsDataModelService.addProjectEntry(entry);
			try
			{
				WorkspaceXMLHandler.setOpenClosed(entry);
			} catch (Exception e)
			{
				logger.fatal(e.getMessage(), e);
			}
		}
		catch (FileNotFoundException e) 
		{
			logger.error("The project will be removed." + "\n" + e.getMessage(),e);
			showMessage("The project will be removed.", e.getMessage());
			DeleteEntryHandler.removeProject(gritsDataModelService, entry);
		}
		catch (IOException | MissingReaderException | UnsupportedVersionException e) 
		{
			logger.error("The project will be closed" + "\n" + e.getMessage(),e);
			showMessage("The project will be closed", e.getMessage());
			selectionService.setSelection(entry);
			handlerService.executeHandler(
					commandService.createCommand(CloseProjectHandler.COMMAND_ID, null));
		}
		catch (Exception e) 
		{
			logger.fatal("The project will be removed." + "\n" + e.getMessage(),e);
			showMessage("The project will be removed.", e.getMessage());
			DeleteEntryHandler.removeProject(gritsDataModelService, entry);
		}
	}

	private void showMessage(String title, String error)
	{
		MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.OK);
		messageBox.setText(title);
		messageBox.setMessage(title + "\n\n" + error);
		messageBox.open();
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		if(object instanceof Entry)
		{
			Entry entry = (Entry) object;
			return entry.getProperty() instanceof ProjectProperty
					&& !((ProjectProperty) entry.getProperty()).isOpen();
		} else if (object instanceof StructuredSelection) {
			Iterator<?> iterator = ((StructuredSelection) object).iterator();
			Object nextSelection = null;
			boolean isClosedProject = false;
			while(iterator.hasNext())
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					isClosedProject =  ((Entry) nextSelection).getProperty() instanceof ProjectProperty 
							&& !(((ProjectProperty)((Entry) nextSelection).getProperty()).isOpen());
				}
			}
			return isClosedProject;
		}
		return false;
	}
}