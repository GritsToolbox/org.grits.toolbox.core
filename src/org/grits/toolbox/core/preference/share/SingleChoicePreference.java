/**
 * 
 */
package org.grits.toolbox.core.preference.share;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.PreferenceHandler;
import org.grits.toolbox.core.preference.project.UtilityPreferenceValue;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore.Preference;
import org.jdom.Element;

/**
 * 
 *
 */

public class SingleChoicePreference
{
	private static final Logger logger = Logger.getLogger(SingleChoicePreference.class);

	private IGritsPreferenceStore gritsPreferenceStore = null;

	private String preferenceName = null;
	private Set<String> allValues = new HashSet<String>();
	private String defaultValue = null;

	private boolean dirty = false;

	SingleChoicePreference(IGritsPreferenceStore gritsPreferenceStore,
			ProjectPreferenceStore.Preference preference)
	{
		logger.info("Creating single selection type preference : "
				+ preference.getPreferenceName());
		this.gritsPreferenceStore = gritsPreferenceStore;
		this.preferenceName = preference.getPreferenceName();

		initializeValues(preference);

		if(dirty) savePreference();
	}

	/**
	 * initializes a preference map using the current preference name or
	 * pre-versioned preference names. It loads default values if
	 * neither of them was found. Then loads value from the map.
	 * @param preference the preference which is to be retrieved
	 */
	protected void initializeValues(Preference preference)
	{
		Map<String, Boolean> valueSelectionMap = getValueSelectionMap(preferenceName);

		int index = 0;
		// try getting pre-versioning values with previous names
		while(valueSelectionMap == null && index < preference.previousNames.length)
		{
			valueSelectionMap = getPreversioningValues(preference.previousNames[index++]);
		}

		// get default value from file
		if(valueSelectionMap == null)
		{
			valueSelectionMap = loadDefaultValues(preference);
			dirty = true;
		}

		loadFromMap(valueSelectionMap);
	}

	private void loadFromMap(Map<String, Boolean> valueSelectionMap)
	{
		for(String value : valueSelectionMap.keySet())
		{
			allValues.add(value);
			if(valueSelectionMap.get(value))
			{
				defaultValue = value;
			}
		}
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		if(defaultValue != null && !allValues.contains(defaultValue))
			addValue(defaultValue);

		this.defaultValue = defaultValue;
	}

	public Set<String> getAllValues()
	{
		return allValues;
	}

	public void setAllValues(Set<String> allValues)
	{
		this.allValues = allValues;
	}

	public boolean addValue(String value)
	{
		return allValues.add(value);
	}

	/**
	 * saves the preference values into the preference.xml file
	 * @return true if it was successfully saved
	 */
	public boolean savePreference()
	{
		PreferenceEntity preferenceEntity = new PreferenceEntity(preferenceName);
		preferenceEntity.setValue(
				UtilityPreferenceValue.getPreferenceValueString(allValues, defaultValue));
		return gritsPreferenceStore.savePreference(preferenceEntity);
	}

	/**
	 * reads default file and initializes the map
	 * @param preference name of the preference variable
	 * @return a map with values or <b>empty map</b> if defaultFile name is null
	 */
	public Map<String, Boolean> loadDefaultValues(Preference preference)
	{
		return preference.getDefaultFileName() == null
				? new HashMap<String, Boolean>()
				: PreferenceHandler.getPreferenceValues(preference.getDefaultFileName());
	}

	/**
	 * returns a map of values and their selection (true if selected)
	 * for peference which can be serialized as supported versions
	 * of {@link PreferenceEntity} object or <b>null</b> if 
	 * {@link UnsupportedVersionException} is thrown while reading
	 * it using {@link IGritsPreferenceStore}
	 * @param preferenceName name of the preference variable
	 * @return a map of string values and their selection status
	 * (only one of them is selected) or <b>null</b>
	 */
	protected Map<String, Boolean> getValueSelectionMap(String preferenceName)
	{
		Map<String, Boolean> valueSelectionMap = null;
		try
		{
			PreferenceEntity preferenceEntity = 
					gritsPreferenceStore.getPreferenceEntity(preferenceName);
			if(preferenceEntity != null)
			{
				logger.info("Preference found : " + preferenceName);
				valueSelectionMap = 
						UtilityPreferenceValue.getPreferenceValuesWithSelection(preferenceEntity);
			}
		} catch (UnsupportedVersionException e)
		{
			logger.error(e.getMessage(), e);
		}
		return valueSelectionMap;
	}

	/**
	 * returns a map of values and their selection (true if selected)
	 * for preversioning peference names
	 * @param previousName name of the previous previous variable
	 * @return a map of string values and its selection
	 */
	protected Map<String, Boolean> getPreversioningValues(String previousName)
	{
		Map<String, Boolean> valueSelectionMap = null;
		Element preferenceElement = gritsPreferenceStore.getPreferenceElement(preferenceName);
		if(preferenceElement != null)
		{
			logger.info("Preference found : " + preferenceName);
			valueSelectionMap = new HashMap<String, Boolean>();
			for(String v : UtilityPreferenceValue.getPreversioningValues(preferenceElement))
			{
				valueSelectionMap.put(v, false);
			}
			String selectedValue = UtilityPreferenceValue.getPreversioningSelected(preferenceElement);
			if(selectedValue != null && !selectedValue.trim().isEmpty())
				valueSelectionMap.put(selectedValue.trim(), true);
			gritsPreferenceStore.removePreference(previousName);
			dirty = true;
		}
		return valueSelectionMap;
	}
}
