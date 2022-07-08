package org.grits.toolbox.core.preference.share;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.PreferenceHandler;
import org.jdom.Element;

public class PreferenceReader
{
	/**
	 * reads the preference from preference file
	 * @param preferenceName is the <b>name</b> of the preference object to be read
	 * @return returns {@link PreferenceEntity} object if the preference element with the given <b>name</b> was found.
	 * It returns <code>null</code> if the element with the given <b>name</b> was not found in the file
	 * @throws UnsupportedVersionException if the <b>version</b> was null for the element 
	 * or if the element with the given <b>name</b> was not serializable 
	 * as the current version {@link PreferenceEntity} object
	 */
	public static PreferenceEntity getPreferenceByName(String preferenceName) throws UnsupportedVersionException
	{
		return PreferenceHandler.getPreferenceEntity(preferenceName);
	}

	/**
	 * returns the preference element from the preference file
	 * @param preferenceName is the <b>name</b> of the preference element to be read
	 * @return the org.jdom.Element object as read from the preference file.
	 * This method should only be used if the method <b>getPreferenceByName</b>
	 * fails to read the preference object
	 */
	public static Element getPreferenceElement(String preferenceName)
	{
		return PreferenceHandler.getPreferenceElement(preferenceName);
	}
}
