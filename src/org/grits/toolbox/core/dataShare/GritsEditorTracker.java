package org.grits.toolbox.core.dataShare;

import org.apache.log4j.Logger;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.EditorPart;

public class GritsEditorTracker implements IPartListener
{
	private static final Logger logger = Logger.getLogger(GritsEditorTracker.class);

	@Override
	public void partActivated(IWorkbenchPart part)
	{

	}


	@Override
	public void partBroughtToTop(IWorkbenchPart part)
	{
		if (part instanceof EditorPart)
		{
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IPerspectiveDescriptor currentPerspective = page.getPerspective();
			String currentPerspectiveId = currentPerspective.getId();
			String newPerspectiveId = IGritsConstants.ID_DEFAULT_PERSPECTIVE; // default perspective
			EditorPart editor = (EditorPart)part;
			String editorClass = editor.getClass().getName();
			if (editorClass.contains("experimentdesigner"))
			{
				logger.debug("Switching into org.grits.toolbox.editor.experimentdesigner.designPerspective1" );
				// switch to the correct perspective
				newPerspectiveId = "org.grits.toolbox.editor.experimentdesigner.designPerspective1";
			}
			else if (editorClass.contains("glycanarray"))
			{
				// switch to the correct perspective
				newPerspectiveId = "uk.ac.imperial.glycosciences.glycanarray.perspective";
			}
			else if (editorClass.contains("qrtpcr"))
			{
				// switch to the correct perspective
				newPerspectiveId = "org.grits.toolbox.entry.qrtpcr.perspective";
			}

			// switch only if it is different from current perspective
			if(!newPerspectiveId.equals(currentPerspectiveId))
			{
				if(IGritsConstants.ID_DEFAULT_PERSPECTIVE.equals(newPerspectiveId))
				{
					PropertyHandler.changePerspective(newPerspectiveId);
					// compatibility mode issue
					PropertyHandler.changePerspective(newPerspectiveId + ".<Default Perspective>");
				}
				else
				{
					try
					{
						PlatformUI.getWorkbench().showPerspective(
								newPerspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
						if (newPerspectiveId.contains("experimentdesigner"))
							PropertyHandler.changePerspective("org.grits.toolbox.editor.experimentdesigner.designPerspective.<Experiment Design>");
					} catch (WorkbenchException e)
					{
						logger.error(e.getMessage(), e);
						PropertyHandler.changePerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
					}
				}
			}
		}
	}


	@Override
	public void partClosed(IWorkbenchPart part)
	{

	}


	@Override
	public void partDeactivated(IWorkbenchPart part)
	{

	}


	@Override
	public void partOpened(IWorkbenchPart part)
	{

	}
}