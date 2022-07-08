/**
 * 
 */
package org.grits.toolbox.core.preference;

import java.util.List;

/**
 * 
 *
 */
public interface IPreferenceUpdateListener
{
	/**
	 * notifies the class implementing this interface when some of the preference values are changed
	 * @param preferenceIds are the list of ids of the preference element that were changed
	 * e.g.  "org.grits.toolbox.entry.sample.preference.AnalytePreferenceVariables.numberOfComponents" is the 
	 * id of the preference "Number of Components" in the Analyte Editor.
	 * Each "id" refers to the name of the preference element in ".preference.xml" that was changed.
	 */
	public void preferenceUpdated(List<String> preferenceIds);
}
