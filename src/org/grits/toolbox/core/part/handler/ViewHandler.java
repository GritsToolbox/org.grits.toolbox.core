 
package org.grits.toolbox.core.part.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolItem;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.part.CollaboratorPart;
import org.grits.toolbox.core.part.EventPart;
import org.grits.toolbox.core.part.ProjectEntryPart;
import org.grits.toolbox.core.part.TaskPart;

public class ViewHandler
{
	private static final Logger logger = Logger.getLogger(ViewHandler.class);
	public static final String COMMAND_ID = "org.grits.toolbox.core.command.entry.project.table.view";
	@Inject IEventBroker eventBroker;

	@Execute
	public void execute(ESelectionService eSelectionService,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService)
	{
		if(ProjectEntryPart.PART_ID.equals(part.getElementId()))
		{
			
			logger.debug("Viewing selected item in project entry part");
			StructuredSelection selection = null;
			if(eSelectionService.getSelection() instanceof SelectionEvent)
			{
				logger.debug("viewing selected item");
				SelectionEvent event = ((SelectionEvent) eSelectionService.getSelection());
				ToolItem item = (ToolItem) event.getSource();
				TableViewer tableViewer = (TableViewer) item.getParent().getData();
				selection = (StructuredSelection) tableViewer.getSelection();
			}
			else if(eSelectionService.getSelection() instanceof DoubleClickEvent)
			{
				logger.debug("viewing double clicked item");
				DoubleClickEvent event = ((DoubleClickEvent) eSelectionService.getSelection());
				selection = (StructuredSelection) event.getSelection();
			}
			if(selection.getFirstElement() != null)
			{
				String correspondingPartId = null;
				if(ProjectCollaborator.class.equals(selection.getFirstElement().getClass()))
				{
					correspondingPartId = CollaboratorPart.PART_ID;
				}
				else if(ProjectTasklist.class.equals(selection.getFirstElement().getClass()))
				{
					correspondingPartId = TaskPart.PART_ID;
				}
				else if(ProjectEvent.class.equals(selection.getFirstElement().getClass()))
				{
					correspondingPartId = EventPart.PART_ID;
				}

				if(correspondingPartId != null)
				{
					partService.bringToTop(partService.findPart(correspondingPartId));
					eventBroker.post(ProjectEntryPart.EVENT_TOPIC_FIELD_SELECTION, selection.getFirstElement());
				}
			}
		}
	}
}