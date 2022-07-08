package org.grits.toolbox.core.preference.project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.jdom.Element;

public class UtilityPreferenceValue
{
	private static final String VALUE_SEPERATOR = "||||";
	private static final String SELECTION_INDICATOR = "~~";
	private static final String PREVIOUS_VALUE_SEPERATOR = "|";

	/**
	 * returns set of preference values from the PreferenceEntity object. The 
	 * PreferenceEntity object is supposed to be version "1.0".
	 * @param preferenceEntity
	 * @return set of String which are parsed from the value of the PreferenceEntity
	 * using the splitter <code>VALUE_SEPERATOR = "||||"</code>.
	 * Example values inside the object could be <b>"value1||||value2||||value3"</b>. The
	 * set would contain <b>[value1, value2, value3]</b>.
	 */
	public static Set<String> getPreferenceValues(PreferenceEntity preferenceEntity)
	{
		Set<String> valueSet = new HashSet<String>();
		if(preferenceEntity.getValue() != null && !preferenceEntity.getValue().isEmpty())
		{
			String[] writtenValues = preferenceEntity.getValue().split(Pattern.quote(VALUE_SEPERATOR));
			for(String value : writtenValues)
			{
					valueSet.add(value);
			}
		}
		return valueSet;
	}

	/**
	 * returns a map of preference values and their selection (i.e. true if selected)
	 * @param preferenceEntity object is version "1.0"
	 * @return HashMap containing string values and their selection (true if selected).
	 * Example value of the preferenceEntity object would be <b>"value1||||~~value2~~||||value3"</b>.
	 * In this case <b>value2</b> is selected.
	 */
	public static Map<String, Boolean> getPreferenceValuesWithSelection(PreferenceEntity preferenceEntity)
	{
		Map<String, Boolean> valueSelectionMap = new HashMap<String, Boolean>();
		if(preferenceEntity.getValue() != null && !preferenceEntity.getValue().isEmpty())
		{
			String[] writtenValues = preferenceEntity.getValue().split(Pattern.quote(VALUE_SEPERATOR));
			for(String value : writtenValues)
			{
				if(value.startsWith(SELECTION_INDICATOR) 
						&& value.endsWith(SELECTION_INDICATOR) 
						&& value.length() > 2*(SELECTION_INDICATOR.length()))
				{
					valueSelectionMap.put(value.substring(SELECTION_INDICATOR.length(), 
							value.lastIndexOf(SELECTION_INDICATOR)), true);
				}
				else
				{
					valueSelectionMap.put(value, false);
				}
			}
		}
		return valueSelectionMap;
	}

	/**
	 * 
	 * @param allValues set of values.
	 * @param selectedValue value that is selected in the passed values selected.
	 * @return returns value as string <b>"value1||||~~value2~~||||value3"</b>.
	 * In this case <b>value2</b> is passed as selected.
	 */
	public static String getPreferenceValueString(Set<String> allValues, String selectedValue)
	{
		String value = "";
		for(String v : allValues)
		{
			if(!v.isEmpty())
			{
				value += VALUE_SEPERATOR;
				value += v.equals(selectedValue) ? 
						SELECTION_INDICATOR + v + SELECTION_INDICATOR : v;
			}
		}
		value = value.isEmpty() ? value : value.substring(VALUE_SEPERATOR.length());
		return value;
	}

	public static String getPreferenceValueString(Set<String> selectedValues, Set<String> unselectedValues)
	{
		String value = "";
		for(String v : selectedValues)
		{
			value += v.isEmpty() ? "" : VALUE_SEPERATOR 
					+ SELECTION_INDICATOR + v + SELECTION_INDICATOR;
		}
		for(String v : unselectedValues)
		{
			value += v.isEmpty() ? "" : VALUE_SEPERATOR + v;
		}
		value = value.isEmpty() ? value : value.substring(VALUE_SEPERATOR.length());
		return value;
	}

	/**
	 * returns an empty set or set of string
	 * @param preferenceElement stored in the preference file
	 * @return set of string value splitted by <b>"|"</b> from the value 
	 * of the attribute <b>"values"</b> in the preference element
	 */
	public static Set<String> getPreversioningValues(Element preferenceElement)
	{
		Set<String> values = new HashSet<String>();
		if(preferenceElement != null && preferenceElement.getAttribute("values") != null)
		{
			String value = preferenceElement.getAttributeValue("values");
			if(value != null && !value.isEmpty())
			{
				String[] valueArray = value.split(Pattern.quote(PREVIOUS_VALUE_SEPERATOR));
				for(String v : valueArray)
				{
					values.add(v);
				}
			}
		}
		return values;
	}

	/**
	 * returns null or the string value
	 * @param preferenceElement stored in the preference file
	 * @return string value of the attribute <b>"selected"</b> in the preference element
	 */
	public static String getPreversioningSelected(Element preferenceElement)
	{
		return preferenceElement != null 
				&& preferenceElement.getAttribute("selected") != null
				? preferenceElement.getAttributeValue("selected") : null;
	}

	/**
	 * returns null or the string value
	 * @param preferenceElement stored in the preference file
	 * @return string value of the attribute <b>"value"</b> in the preference element
	 */
	public static String getPreversioningValue(Element preferenceElement)
	{
		return preferenceElement != null 
				&& preferenceElement.getAttribute("value") != null
				? preferenceElement.getAttributeValue("value") : null;
	}
}
