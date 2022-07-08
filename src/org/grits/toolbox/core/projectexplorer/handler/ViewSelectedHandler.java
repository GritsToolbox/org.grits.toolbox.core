
package org.grits.toolbox.core.projectexplorer.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.EditorHandler;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;
import org.grits.toolbox.core.service.IGritsUIService;

@SuppressWarnings("deprecation")
public class ViewSelectedHandler
{
	private static final Logger logger = Logger.getLogger(ViewSelectedHandler.class);
	public static final String COMMAND_ID = 
			"org.grits.toolbox.core.command.projectexplorer.entry.view";

	@Inject private MApplication application;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Entry selectedEntry,
			@Named(IServiceConstants.ACTIVE_PART) MPart projectExplorerPart,
			EModelService modelService, IGritsUIService gritsUIService)
	{
		if(ProjectExplorerPart.PART_ID.equals(projectExplorerPart.getElementId())
				&& selectedEntry != null)
		{
			logger.info("opening entry " + selectedEntry.getDisplayName());
			if(gritsUIService.openEntryInPart(selectedEntry) == null)
			{
				logger.info("No e4 part could be found to display this entry type : "
						+ selectedEntry.getProperty().getType()
						+ "\n Switching to compatibility perspective");

				MPerspective compatEditorPersp = (MPerspective)
						modelService.find(IGritsConstants.ID_DEFAULT_PERSPECTIVE, application);
				if(compatEditorPersp != null)
				{
					gritsUIService.selectPerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
				}
				else
				{
					gritsUIService.selectPerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE
							+ ".<Default Perspective>");
				}

				logger.info("Using 3.x way to handle opening the entry");
				handleOldStuff(selectedEntry);
			}
		}
	}

	private void handleOldStuff(Entry selectedEntry)
	{
		EditorHandler.openEditorForEntry(selectedEntry);
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		return object instanceof Entry;
	}
}