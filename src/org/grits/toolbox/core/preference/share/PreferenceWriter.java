package org.grits.toolbox.core.preference.share;

import org.grits.toolbox.core.preference.PreferenceHandler;


public class PreferenceWriter
{
	/**
	 * saves the PreferenceEntity into the preference file
	 * @param preferenceEntity type {@link PreferenceEntity} should contain atleast <b>name</b> 
	 * and <b>version</b> of the preference
	 * @return returns <code>true</code> if the preference was successfully saved in the file 
	 * else returns <code>false</code>.
	 * It cannot save the preference if the <b>name</b> or <b>version</b> is <code>null</code>. For
	 * these cases it returns <code>false</code>. 
	 */
	public static boolean savePreference(PreferenceEntity preferenceEntity)
	{
		return PreferenceHandler.savePreference(preferenceEntity);
	}

	/**
	 * deletes all the preferences with the given name
	 * @param preferenceName name of the preference
	 * @return true if deleted
	 */
	public static boolean deletePreference(String preferenceName)
	{
		return PreferenceHandler.removePreference(preferenceName);
	}
}
