/**
 * 
 */
package org.grits.toolbox.core.utilShare;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.grits.toolbox.core.preference.IPreferenceUpdateListener;

/**
 * 
 *
 */
public class UtilityPreference
{
	private static final Logger logger = Logger.getLogger(UtilityPreference.class);


	/**
	 * update both editors and views after changing preferences
	 * @param preferenceIds the ids of the preference elements that were changed
	 * e.g. "org.grits.toolbox.entry.sample.preference.AnalytePreferenceVariables.numberOfComponents" is the 
	 * id of the preference "Number of Components" in the Analyte Editor
	 */
	public static void updateUIs(List<String> preferenceIds)
	{
		updateEditors(preferenceIds);
		updateViews(preferenceIds);
	}

	/**
	 * update the editors after changing preferences
	 * @param preferenceIds the ids of the preference elements that were changed
	 * e.g. "org.grits.toolbox.entry.sample.preference.AnalytePreferenceVariables.numberOfComponents" is the 
	 * id of the preference "Number of Components" in the Analyte Editor
	 */
	public static void updateEditors(List<String> preferenceIds)
	{
		if(preferenceIds != null)
		{
			IEditorPart editorPart = null;
			for(IEditorReference editorReference : PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getEditorReferences())
			{
				try
				{
					editorPart = editorReference.getEditor(false);
					if(editorPart instanceof IPreferenceUpdateListener)
					{
						((IPreferenceUpdateListener) editorPart).preferenceUpdated(preferenceIds);
					}
				} catch (Exception e)
				{
					logger.error("Error updating editor : " + editorReference + "\n" + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * update the views after changing preferences
	 * @param preferenceIds the ids of the preference elements that were changed
	 * e.g. "org.grits.toolbox.entry.sample.preference.AnalytePreferenceVariables.numberOfComponents" is the 
	 * id of the preference "Number of Components" in the Analyte Editor
	 */
	public static void updateViews(List<String> preferenceIds)
	{
		if(preferenceIds != null)
		{
			IViewPart viewPart = null;
			for(IViewReference viewReference : PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getViewReferences())
			{
				try
				{
					viewPart = viewReference.getView(false);
					if(viewPart instanceof IPreferenceUpdateListener)
					{
						((IPreferenceUpdateListener) viewPart).preferenceUpdated(preferenceIds);
					}
				} catch (Exception e)
				{
					logger.error("Error updating view : " + viewReference + "\n" + e.getMessage(), e);
				}
			}
		}
	}

}
