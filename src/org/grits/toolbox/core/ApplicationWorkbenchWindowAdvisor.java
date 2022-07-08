package org.grits.toolbox.core;

import javax.inject.Named;

import org.eclipse.ui.IPartService;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.grits.toolbox.core.dataShare.GritsEditorTracker;
import org.grits.toolbox.core.dataShare.GritsPerspectiveAdapter;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.service.IGritsDataModelService;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{	
    @Named(IGritsDataModelService.WORKSPACE_ENTRY)
    Entry workspaceEntry;

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
    {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer)
    {
        return new ApplicationActionBarAdvisor(configurer);
    }

    /**
     * A setting for
     */
    @Override
    public void preWindowOpen()
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowMenuBar(true);
        // configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(false);
        //configurer.setTitle("GRITS toolbox 1.2 (Morning Blend)");
        configurer.setTitle("GRITS Toolbox "+ GritsConfig.VERSION + " (" + GritsConfig.VERSION_NAME + ") - Workspace: " + PropertyHandler.getVariable(IGritsConstants.WORKSPACE_LOCATION));
        // configurer.setShowPerspectiveBar(true);
    }

    @Override
    public void postWindowOpen()
    {
        super.postWindowOpen();
        getWindowConfigurer().getWindow().getShell().setMaximized(true);
        getWindowConfigurer().getWindow().addPerspectiveListener(new GritsPerspectiveAdapter());

        IPartService service = getWindowConfigurer().getWindow().getService(IPartService.class);
        service.addPartListener(new GritsEditorTracker());
    }
}