package org.grits.toolbox.core.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.AboutDialog;

public class AboutHandler {
	
	@Inject
	private IEclipseContext context;
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		AboutDialog dialog = new AboutDialog(shell);
		
		ContextInjectionFactory.inject(dialog, context);
		
		dialog.open();
	}
}