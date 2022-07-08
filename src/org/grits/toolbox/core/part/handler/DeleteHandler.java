
package org.grits.toolbox.core.part.handler;

import java.util.List;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolItem;
import org.grits.toolbox.core.part.ProjectEntryPart;

public class DeleteHandler
{
	private static final Logger logger = Logger.getLogger(DeleteHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.core.command.entry.project.table.delete";

	@Execute
	public void execute(ESelectionService eSelectionService,
			@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService)
	{
		if(ProjectEntryPart.PART_ID.equals(part.getElementId()))
		{
			TableViewer tableViewer = null;
			if(eSelectionService.getSelection() instanceof SelectionEvent)
			{
				SelectionEvent event = ((SelectionEvent) eSelectionService.getSelection());
				ToolItem item = (ToolItem) event.getSource();
				tableViewer = (TableViewer) item.getParent().getData();
			}
			else if(eSelectionService.getSelection() instanceof KeyEvent)
			{
				KeyEvent event = (KeyEvent) eSelectionService.getSelection();
				tableViewer = (TableViewer) event.data;
			}

			if(tableViewer != null)
			{
				logger.debug("START : Deleting from table");
				List<?> input = (List<?>) tableViewer.getInput();
				int selectionIndex = tableViewer.getTable().getSelectionIndex();
				if(selectionIndex >= 0 
						&& selectionIndex < input.size())
				{
					Object selected = 
							tableViewer.getElementAt(selectionIndex);
					input.remove(selected);
					part.setDirty(true);
					tableViewer.refresh();
					selectionIndex = Math.max(selectionIndex - 1, 0);
					tableViewer.getTable().select(selectionIndex);
					tableViewer.setSelection(tableViewer.getSelection());
				}
				logger.debug("END   : Deleting from table");
			}
		}
	}
}