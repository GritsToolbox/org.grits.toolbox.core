
package org.grits.toolbox.core.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.preference.PreferenceManagerLoader;

public class OpenPreferenceHandler
{
	public static final String COMMAND_ID =
			"org.grits.toolbox.core.command.preference.open";
	public static final String PARAM_PREFERENCE_PAGE_ID =
			"org.grits.toolbox.core.commandparameter.preference.pageid";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
//			PreferenceManagerLoader preferenceManagerLoader,
			@Named(PreferenceManagerLoader.GRITS_PREFERENCE_MANAGER) PreferenceManager preferenceManager,
			@Optional @Named (PARAM_PREFERENCE_PAGE_ID) String preferencePageId)
	{
		PreferenceDialog preferenceDialog = new PreferenceDialog(shell, preferenceManager);
		if(preferencePageId != null)
			preferenceDialog.setSelectedNode(preferencePageId);
//		PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(
//				shell, preferencePageId, null, null);
		preferenceDialog.open();
		preferenceDialog.close();
	}
}