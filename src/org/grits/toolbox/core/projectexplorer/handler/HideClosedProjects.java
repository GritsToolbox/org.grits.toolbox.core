 
package org.grits.toolbox.core.projectexplorer.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.grits.toolbox.core.projectexplorer.filter.ClosedProjectsFilter;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;

public class HideClosedProjects {
	
	ClosedProjectsFilter filter = new ClosedProjectsFilter();
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, final MToolItem item) {
		ProjectExplorerPart curView = null;
		if (part != null && part.getObject() instanceof ProjectExplorerPart ) {
			curView = (ProjectExplorerPart) part.getObject();
			filter.setApplied(!item.isSelected());
			curView.filter(filter, item.isSelected());
		}
	}
}