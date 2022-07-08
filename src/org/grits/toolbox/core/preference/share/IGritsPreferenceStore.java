/**
 * 
 */
package org.grits.toolbox.core.preference.share;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.jdom.Element;

/**
 * 
 *
 */
public interface IGritsPreferenceStore
{
	static final String PREFERENCE_LOCATION = PropertyHandler.getVariable("preference");

	/**
	 * Grits broadcasts a change in preference using the event bus.
	 * Subscribe to this topic if you want to get notified
	 * when a preference is saved to a preference file.
	 * <br/>Example code :
	 * <blockquote><pre>
	 * @Optional @Inject
	 * public void resetSomething(@UIEventTopic({@link
	 * 				IGritsPreferenceStore#EVENT_TOPIC_PREF_VALUE_CHANGED})
	 * 						String preferenceName)
	 * {
	 *  	if(MY_PREFERENCE_NAME.equals(preferenceName)
	 *  	{
	 *  	 // reset this
	 *  	}
	 * }
	 * </pre></blockquote>
	 */
	public static final String EVENT_TOPIC_PREF_VALUE_CHANGED =
			"preference_value_changed_in_grits";


	/**
	 * reads the preference from preference file
	 * @param preferenceName is the <b>name</b> of the preference object to be read
	 * @return returns {@link PreferenceEntity} object if the preference element with the given <b>name</b> was found.
	 * It returns <code>null</code> if the element with the given <b>name</b> was not found in the file
	 * @throws UnsupportedVersionException if the <b>version</b> was null for the element 
	 * or if the element with the given <b>name</b> was not successfully deserialized 
	 * as the current version {@value PreferenceEntity#CURRENT_VERSION} {@link PreferenceEntity} object
	 */
	public PreferenceEntity getPreferenceEntity(String preferenceName) throws UnsupportedVersionException;

	/**
	 * returns the preference element from the preference file
	 * @param preferenceName is the <b>name</b> of the preference element to be read
	 * @return the {@link Element} object as read from the preference file or null if not found.
	 * This method should only be used if the method <b>getPreferenceByName</b>
	 * fails to read the preference object
	 */
	public Element getPreferenceElement(String preferenceName);

	/**
	 * saves the PreferenceEntity into the preference file
	 * @param preferenceEntity type {@link PreferenceEntity} should contain atleast <b>name</b> 
	 * and <b>version</b> of the preference
	 * @return returns <code>true</code> if the preference was successfully saved in the file 
	 * else returns <code>false</code>.
	 * It cannot save the preference if the <b>name</b> or <b>version</b> is <code>null</code>. For
	 * these cases it returns <code>false</code>. 
	 */
	public boolean savePreference(PreferenceEntity preferenceEntity);

	/**
	 * deletes all the preferences with the given name from the preference file
	 * @param preferenceName name of the preference
	 * @return true if deleted
	 */
	public boolean removePreference(String preferenceName);
}
