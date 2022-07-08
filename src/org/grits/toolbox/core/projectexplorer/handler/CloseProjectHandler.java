/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.handler;

import java.util.Iterator;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;

/**
 * 
 *
 */
public class CloseProjectHandler
{
	private static final Logger logger = Logger.getLogger(CloseProjectHandler.class);

	public static final String COMMAND_ID = 
			"org.grits.toolbox.core.command.projectexplorer.close.project";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object selection,
			IGritsDataModelService gritsDataModelService, IGritsUIService gritsUIService)
	{
		if (selection instanceof Entry) {
			Entry entry = (Entry)selection;
			if(entry != null && entry.getProperty() != null
					&& entry.getProperty() instanceof ProjectProperty)
			{
				ProjectProperty projectProperty = (ProjectProperty) entry.getProperty();
				if(projectProperty.isOpen())
					projectProperty.setOpen(false);
				try
				{
					WorkspaceXMLHandler.setOpenClosed(entry);
				} catch (Exception e)
				{
					logger.fatal(e.getMessage(), e);
				}
				gritsUIService.closePartForEntry(entry);
				gritsDataModelService.closeProject(entry);
			}
		} else if (selection instanceof StructuredSelection) {
			Iterator<?> iterator = ((StructuredSelection) selection).iterator();
			Object nextSelection = null;
			Entry entry = null;
			while(iterator.hasNext())
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					entry = (Entry) nextSelection;
					if (entry.getProperty() != null && entry.getProperty() instanceof ProjectProperty) {
						ProjectProperty projectProperty = (ProjectProperty) entry.getProperty();
						if(projectProperty.isOpen())
							projectProperty.setOpen(false);
						try
						{
							WorkspaceXMLHandler.setOpenClosed(entry);
						} catch (Exception e)
						{
							logger.fatal(e.getMessage(), e);
						}
						gritsUIService.closePartForEntry(entry);
						gritsDataModelService.closeProject(entry);
					}
				}
			}
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		if(object instanceof Entry)
		{
			Entry entry = (Entry) object;
			return entry.getProperty() instanceof ProjectProperty
					&& ((ProjectProperty) entry.getProperty()).isOpen();
		} else if (object instanceof StructuredSelection) {
			Iterator<?> iterator = ((StructuredSelection) object).iterator();
			Object nextSelection = null;
			boolean isOpenProject = false;
			while(iterator.hasNext())
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					isOpenProject =  ((Entry) nextSelection).getProperty() instanceof ProjectProperty 
							&& (((ProjectProperty)((Entry) nextSelection).getProperty()).isOpen());
				}
			}
			return isOpenProject;
		}
		return false;
	}
}
