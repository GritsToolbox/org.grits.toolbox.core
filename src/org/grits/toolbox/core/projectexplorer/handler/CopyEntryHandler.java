
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.IOException;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.NotImplementedException;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.projectexplorer.dialog.CopyEntryDialog;
import org.grits.toolbox.core.service.IGritsDataModelService;

public class CopyEntryHandler
{
	private static final Logger logger = Logger.getLogger(CopyEntryHandler.class);

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			EPartService partService, IGritsDataModelService gritsDataModelService)
	{
		logger.info("- START COMMAND : Copy entry");
		if(object instanceof Entry)
		{
			Entry currentEntry = (Entry) object;
			CopyEntryDialog dialog = new CopyEntryDialog(Display.getCurrent().getActiveShell());
			dialog.setCurrentEntry(currentEntry);
			if (dialog.open() == Window.OK) 
			{
				Entry destinationParent = dialog.getDestinationParentEntry();
				try
				{
					Entry copiedEntry = currentEntry.copyToEntry(destinationParent, gritsDataModelService);
					gritsDataModelService.addEntry(destinationParent, copiedEntry);
					Entry projectEntry = gritsDataModelService.findParentByType(copiedEntry, ProjectProperty.TYPE);
					try
					{
						ProjectFileHandler.saveProject(projectEntry);
//						MPart projectExplorerPart = partService.findPart(ProjectExplorerPart.PART_ID);
//						if(projectExplorerPart != null 
//								&& projectExplorerPart.getObject() != null)
//						{
//							((ProjectExplorerPart) projectExplorerPart.getObject()
//									).getTreeViewer().refresh(projectEntry);
//						}
						logger.info("- END COMMAND : Copied entry");
					} catch (IOException e)
					{
						gritsDataModelService.deleteEntry(copiedEntry);
						logger.error(e.getMessage(),e);
						throw e;
					}
				} catch (NotImplementedException | IOException e)
				{
					logger.error(e.getMessage(), e);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), 
							"Copy unsuccessful", e.getMessage());
				}
			}
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		if (object instanceof Entry) {
			if (((Entry) object).getProperty() != null)
				return ((Entry) object).getProperty().directCopyEnabled();
		}
		return false;
	}

}