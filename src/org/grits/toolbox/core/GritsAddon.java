
package org.grits.toolbox.core;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.EditorHandler;
import org.grits.toolbox.core.preference.GritsPreferenceStore;
import org.grits.toolbox.core.preference.PreferenceHandler;
import org.grits.toolbox.core.preference.PreferenceManagerLoader;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;

@SuppressWarnings("deprecation")
public class GritsAddon
{
	private static final Logger logger = Logger.getLogger(GritsAddon.class);
	@Inject IEclipseContext eclipseContext;

	@PostConstruct
	public void applicationStarted(MApplication application, IGritsUIService gritsUIService,
			@Named (IGritsDataModelService.WORKSPACE_ENTRY) Entry workspaceEntry)
	{
		logger.info("Initializing GritsAddon");

	//	application.getContext().set(IGritsPreferenceStore.class,
		//		ContextInjectionFactory.make(GritsPreferenceStore.class, application.getContext()));

		application.getContext().set(PreferenceHandler.class,
				ContextInjectionFactory.make(PreferenceHandler.class, application.getContext()));
//		application.getContext().set(PreferenceManagerLoader.class,
//				ContextInjectionFactory.make(PreferenceManagerLoader.class, application.getContext()));
		PreferenceManagerLoader managerLaoder = ContextInjectionFactory.make(
				PreferenceManagerLoader.class, application.getContext());
		application.getContext().set(
				PreferenceManagerLoader.GRITS_PREFERENCE_MANAGER, managerLaoder.loadPreferenceManager());

		//application.getContext().set(ProjectPreferenceStore.class,
		//		ContextInjectionFactory.make(ProjectPreferenceStore.class, application.getContext()));

		application.getContext().set(
				EditorHandler.class, 
				ContextInjectionFactory.make(EditorHandler.class, application.getContext()));

		if(!workspaceEntry.hasChildren())
		{
			try
			{
				gritsUIService.selectPerspective(IGritsConstants.ID_INTRO_PERSPECTIVE);
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				gritsUIService.selectPerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
			}
		}
		logger.info("GritsAddon Initialized");
	}
}
