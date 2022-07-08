package org.grits.toolbox.core;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.grits.toolbox.core.dataShare.IGritsConstants;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
	{
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	/**
	 * Get a default perspective for this plugin
	 */
	public String getInitialWindowPerspectiveId()
	{
		return IGritsConstants.ID_DEFAULT_PERSPECTIVE;
	}

	@Override
	public boolean preShutdown() {
		Shell shell = getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow().getShell();
		String dialogBoxTitle = "Confirm Exit";
		String question = "Are you sure you want to close this application?";
		return MessageDialog.openQuestion(shell, dialogBoxTitle, question);
	}
}