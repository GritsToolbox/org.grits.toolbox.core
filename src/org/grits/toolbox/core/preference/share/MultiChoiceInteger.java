/**
 * 
 */
package org.grits.toolbox.core.preference.share;

import java.util.HashMap;
import java.util.Map;

import org.grits.toolbox.core.preference.share.ProjectPreferenceStore.IntegerPreference;

/**
 * 
 *
 */
public class MultiChoiceInteger extends MultiChoicePreference<Integer>
{
	/**
	 * the max value starting from 0 which can be selected or de-selected
	 */
	int maxValue = 0;

	MultiChoiceInteger(IGritsPreferenceStore gritsPreferenceStore,
			IntegerPreference integerPreference)
	{
		super(gritsPreferenceStore,  integerPreference.parameterizedPreference.getPreferenceName());

		logger.info("Creating multi selection integer preference : " + preferenceName);

		parameterizedPreference = integerPreference.parameterizedPreference;
		this.maxValue = integerPreference.getMaxValue() < 0 ?
				0 : integerPreference.getMaxValue();

		initializeValues();

		if(dirty) savePreference();
	}

	/**
	 * loads the value from 0 to the maxValue with true as its
	 * selection status for all of them
	 */
	@Override
	protected Map<String, Boolean> loadDefaultValues()
	{
		Map<String, Boolean> valueSelectionMap =new HashMap<String, Boolean>();
		if(parameterizedPreference.getDefaultFileName() == null)
		{
			for(int i = 0; i < maxValue; i++)
				valueSelectionMap.put(i + "", true);
		}

		return valueSelectionMap;
	}

	/**
	 * casts the integerValue to string
	 */
	@Override
	protected String getStringForObject(Integer integerValue)
	{
		return integerValue == null ? "0" : integerValue + "";
	}

	/**
	 * uses {@link Integer#parseInt(String)} parsing the string value.
	 * It returns 0 as default value of it could not be parsed.
	 */
	@Override
	protected Integer parseObject(Class<Integer> unmarshallerClass, String stringValue)
	{
		Integer integerValue = 0;
		try
		{
			integerValue = Integer.parseInt(stringValue);
		} catch (NumberFormatException e)
		{

		}
		return integerValue;
	}
}
