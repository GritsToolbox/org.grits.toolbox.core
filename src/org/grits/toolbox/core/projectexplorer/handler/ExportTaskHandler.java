
package org.grits.toolbox.core.projectexplorer.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.wizard.taskexport.TaskExportWizard;

public class ExportTaskHandler
{
	private static final Logger logger = Logger.getLogger(ExportTaskHandler.class);

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		logger.debug("Exporting Contributors from workspace");

		TaskExportWizard wizard  = new TaskExportWizard();
		wizard.setWindowTitle("Export to Excel");
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();

		logger.debug("Exporting Contributors from workspace");
	}

	@CanExecute
	public boolean canExecute(IGritsDataModelService gritsDataModelService)
	{
		return gritsDataModelService.getRootEntry().hasChildren();
	}
}