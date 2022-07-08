
package org.grits.toolbox.core.projectexplorer.handler;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;
import org.grits.toolbox.core.service.IGritsDataModelService;

public class RefreshEntryHandler
{
	private static final Logger logger = Logger.getLogger(RefreshEntryHandler.class);
	@Inject private IEventBroker eventBroker;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			EPartService partService)
	{
		MPart projectExplorerPart = partService.findPart(ProjectExplorerPart.PART_ID);
		if(projectExplorerPart != null && projectExplorerPart.getObject() != null)
		{
			if(object instanceof StructuredSelection)
			{
				logger.debug("Refreshing entries in the project explorer");
				Iterator<?> iterator = ((StructuredSelection) object).iterator();
				Object nextSelection = null;
				while(iterator.hasNext())
				{
					nextSelection = iterator.next();
					if(nextSelection instanceof Entry)
					{
						eventBroker.post(IGritsDataModelService.EVENT_DATA_MODEL_CHANGED, nextSelection);
//						((ProjectExplorerPart) projectExplorerPart.getObject()
//								).getTreeViewer().refresh((Entry) nextSelection);
					}
				}
			}
			else if(object instanceof Entry)
			{
				logger.debug("Refreshing the selected entry");
				eventBroker.post(IGritsDataModelService.EVENT_DATA_MODEL_CHANGED, object);
//				Entry entry = (Entry) object;
//				((ProjectExplorerPart) 
//						projectExplorerPart.getObject()).getTreeViewer().refresh(entry);
			}
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		return object instanceof Entry || object instanceof StructuredSelection;
	}
}