package org.grits.toolbox.core.dataShare;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;

public class GritsPerspectiveAdapter extends PerspectiveAdapter {   
    @Override
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspectiveDescriptor) {
        super.perspectiveActivated(page, perspectiveDescriptor);
       // page.resetPerspective();    // think about resetting since it overrides the user's changes in the layout
    }
}