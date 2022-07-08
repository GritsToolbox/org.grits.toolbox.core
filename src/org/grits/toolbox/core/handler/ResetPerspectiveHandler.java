
package org.grits.toolbox.core.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.grits.toolbox.core.service.IGritsUIService;

public class ResetPerspectiveHandler
{
	@Execute
	public void execute(EModelService modelService, EPartService partService,
			MWindow window, IGritsUIService gritsUIService)
	{
		MPerspective activePerspective =
				(MPerspective) modelService.getActivePerspective(window);

//		// possible eclipse bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=404231
//		modelService.resetPerspectiveModel(activePerspective, window);

		// does nothing
		gritsUIService.selectPerspective(activePerspective.getElementId());
	}
}