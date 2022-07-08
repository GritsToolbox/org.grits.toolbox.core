 
package org.grits.toolbox.core.handler;

import java.util.List;

import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

public class AboutMenu {
	
	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, EModelService modelService) {
		Menu systemMenu = Display.getDefault().getSystemMenu();
		if (systemMenu == null) {
			MDirectMenuItem dynamicItem = modelService
	                .createModelElement(MDirectMenuItem.class);
		    dynamicItem.setLabel("About GRITS");
		    dynamicItem.setContributorURI("platform:/plugin/org.grits.toolbox.core");
		    dynamicItem
		            .setContributionURI("bundleclass://org.grits.toolbox.core/org.grits.toolbox.core.handler.AboutHandler");   
		    dynamicItem.setVisible(true);
		    dynamicItem.setEnabled(true);
		    items.add(dynamicItem);
		}
	}
		
}