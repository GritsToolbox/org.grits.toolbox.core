 
package org.grits.toolbox.core.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class SaveHandler
{
	@Execute
	public void execute(EPartService partService, @Named(IServiceConstants.ACTIVE_PART) MPart part)
	{
		partService.savePart(part, false);
	}

	@CanExecute
	boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part)
	{
		return part == null ? false : part.isDirty();
	}

}