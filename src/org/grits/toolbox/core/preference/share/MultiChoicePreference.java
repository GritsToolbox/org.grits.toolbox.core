/**
 * 
 */
package org.grits.toolbox.core.preference.share;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.PreferenceHandler;
import org.grits.toolbox.core.preference.project.UtilityPreferenceValue;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore.ParameterizedPreference;
import org.jdom.Element;

/**
 * 
 *
 */
public class MultiChoicePreference<T>
{
	protected static final Logger logger = Logger.getLogger(MultiChoicePreference.class);

	protected IGritsPreferenceStore gritsPreferenceStore = null;
	protected ParameterizedPreference parameterizedPreference = null;

	protected String preferenceName = null;
	protected List<T> selectedValues = new ArrayList<T>();
	protected List<T> otherValues = new ArrayList<T>();

	protected boolean dirty = false;

	protected MultiChoicePreference(IGritsPreferenceStore gritsPreferenceStore, String preferenceName)
	{
		logger.info("Creating multi selection type preference : " + preferenceName);
		this.gritsPreferenceStore = gritsPreferenceStore;
		this.preferenceName = preferenceName;
	}

	MultiChoicePreference(IGritsPreferenceStore gritsPreferenceStore,
			ProjectPreferenceStore.ParameterizedPreference parameterizedPreference)
	{
		this(gritsPreferenceStore, parameterizedPreference.getPreferenceName());
		this.parameterizedPreference = parameterizedPreference;

		initializeValues();

		if(dirty) savePreference();
	}

	/**
	 * initializes a preference map using the current preference name or
	 * pre-versioned preference names. It loads default values if
	 * neither of them was found. Then loads value from the map.
	 * @param preference the preference which is to be retrieved
	 */
	@SuppressWarnings("unchecked")
	protected void initializeValues()
	{
		Map<String, Boolean> valueSelectionMap = getValueSelectionMap(preferenceName);

		// try getting pre-versioning values with previous names
		// get pre-versioning pair of selected and unselected preference variables
		if(parameterizedPreference.previousNames.length%2 == 0)
		{
			int index = 0;
			while(valueSelectionMap == null
					&& index < parameterizedPreference.previousNames.length - 1)
			{
				valueSelectionMap = getPreversioningValues(
						parameterizedPreference.previousNames[index++],
						parameterizedPreference.previousNames[index++]);
			}
		}

		// get default value from file
		if(valueSelectionMap == null)
		{
			valueSelectionMap = loadDefaultValues();
			dirty = true;
		}

			loadFromMap((Class<T>) parameterizedPreference.unmarshallerClass, valueSelectionMap);
	}

	private void loadFromMap(Class<T> clazz, Map<String, Boolean> valueSelectionMap)
	{
		for(String value : valueSelectionMap.keySet())
		{
			T object = parseObject(clazz, value);
			if(object != null)
			{
				if(valueSelectionMap.get(value))
				{
					selectedValues.add(object);
				}
				else
				{
					otherValues.add(object);
				}
			}
		}
	}

	public List<T> getSelectedValues()
	{
		return selectedValues;
	}

	public void setSelectedValues(List<T> selectedValues)
	{
		this.selectedValues = selectedValues;
	}

	public boolean addSelectedValue(T selectedValue)
	{
		return selectedValues.add(selectedValue);
	}

	public List<T> getOtherValues()
	{
		return otherValues;
	}

	public void setOtherValues(List<T> otherValues)
	{
		this.otherValues = otherValues;
	}

	public boolean addOtherValue(T otherValue)
	{
		return otherValues.add(otherValue);
	}

	/**
	 * saves the preference values into the preference.xml file
	 * @return true if it was successfully saved
	 */
	public boolean savePreference()
	{
		PreferenceEntity preferenceEntity = new PreferenceEntity(preferenceName);
		Set<String> ov = new HashSet<String>();
		String stringValue = null;
		for(T t : otherValues)
		{
			stringValue = getStringForObject(t);
			if(stringValue != null)
				ov.add(stringValue);
		}
		Set<String> sv = new HashSet<String>();
		for(T t : selectedValues)
		{
			stringValue = getStringForObject(t);
			if(stringValue != null)
				sv.add(stringValue);
		}
		preferenceEntity.setValue(UtilityPreferenceValue.getPreferenceValueString(sv, ov));
		if (gritsPreferenceStore != null)
			return gritsPreferenceStore.savePreference(preferenceEntity);
		return false;
	}

	/**
	 * reads default file and initializes the map
	 * @return a map with string values and their selection or
	 * <b>empty map</b> if defaultFile name is null
	 */
	protected Map<String, Boolean> loadDefaultValues()
	{
		return parameterizedPreference.getDefaultFileName() == null
				? new HashMap<String, Boolean>() :
					PreferenceHandler.getPreferenceValues(parameterizedPreference.getDefaultFileName());
	}

	/**
	 * reads default file and initializes the map
	 * @return a map with objects of unmarshaller class and their selection or
	 * <b>empty map</b> if defaultFile name is null
	 */
	@SuppressWarnings("unchecked")
	public Map<T, Boolean> getDefaultValuesObjectMap()
	{
		Map<String, Boolean> defaultStringValueMap = loadDefaultValues();
		Map<T, Boolean> defaultObjectMap = new HashMap<T, Boolean>();
		T object = null;
		for(String stringObject : defaultStringValueMap.keySet())
		{
			object = parseObject((Class<T>) parameterizedPreference.unmarshallerClass, stringObject);
			if(object != null)
				defaultObjectMap.put(object, defaultStringValueMap.get(stringObject));
		}
		return defaultObjectMap;
	}

	/**
	 * returns a map of values and their selection (true if selected)
	 * for peference which can be serialized as supported versions
	 * of {@link PreferenceEntity} object or <b>null</b> if 
	 * {@link UnsupportedVersionException} is thrown while reading
	 * it using {@link IGritsPreferenceStore}
	 * @param preferenceName name of the preference variable
	 * @return a map of serialized string values and their selection status
	 * (multiple of them could be selected) or <b>null</b>
	 */
	protected Map<String, Boolean> getValueSelectionMap(String preferenceName)
	{
		Map<String, Boolean> valueSelectionMap = null;
		try
		{
			if (gritsPreferenceStore != null)  {
				PreferenceEntity preferenceEntity = 
						gritsPreferenceStore.getPreferenceEntity(preferenceName);
				if(preferenceEntity != null)
				{
					logger.info("Preference found : " + preferenceName);
					valueSelectionMap = 
							UtilityPreferenceValue.getPreferenceValuesWithSelection(preferenceEntity);
				}
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
	protected Map<String, Boolean> getPreversioningValues(String defaultName, String otherName)
	{
		Map<String, Boolean> valueSelectionMap = null;
		if (gritsPreferenceStore == null) // not initialized yet!
			return null;
		Element preferenceElement = gritsPreferenceStore.getPreferenceElement(defaultName);
		if(preferenceElement != null)
		{
			logger.info("Preference found : " + defaultName);
			valueSelectionMap = new HashMap<String, Boolean>();
			String selectedValue = UtilityPreferenceValue.getPreversioningSelected(preferenceElement);
			if(selectedValue != null && selectedValue.trim().isEmpty())
				valueSelectionMap.put(selectedValue.trim(), true);
			gritsPreferenceStore.removePreference(defaultName);
			dirty = true;
		}
		preferenceElement = gritsPreferenceStore.getPreferenceElement(otherName);
		if(preferenceElement != null)
		{
			logger.info("Preference found : " + defaultName);
			valueSelectionMap = valueSelectionMap == null ?
					new HashMap<String, Boolean>() : valueSelectionMap;
			for(String v : UtilityPreferenceValue.getPreversioningValues(preferenceElement))
			{
				valueSelectionMap.put(v, false);
			}
			gritsPreferenceStore.removePreference(otherName);
			dirty = true;
		}
		return valueSelectionMap;
	}

	/**
	 * de-serializes the string value into the given class object. 
	 * If the given class (of type T) is {@link String} then it returns the same value
	 * else it uses JAXB to unmarshal it
	 * @param unmarshallerClass class to which the value is to be de-serialized
	 * @param stringValue value of the string
	 * @return the object of type T or <b>null</b> if JaXB could not unmarshall it
	 */
	@SuppressWarnings("unchecked")
	protected T parseObject(Class<T> unmarshallerClass, String stringValue)
	{
		if(String.class.equals(unmarshallerClass))
		{
			return (T) stringValue;
		}

		try
		{
			JAXBContext context = JAXBContext.newInstance(unmarshallerClass);
			return (T) context.createUnmarshaller().unmarshal(new StringReader(stringValue));
		} catch (JAXBException e)
		{
			logger.error("The object could not be read from xml.\n" + e.getMessage(), e);
		} catch (Exception e)
		{
			logger.error("The object could not be read from xml.\n" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * serializes the object into a string
	 * @param object to be serialized
	 * @return a serialized string or <b>null</b> if it could not be marshalled
	 * or for some other error 
	 */
	protected String getStringForObject(T object)
	{
		if(object instanceof String)
		{
			return (String) object;
		}

		String xmlString = null;
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JAXBContext context = JAXBContext.newInstance(object.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
			marshaller.marshal(object, os);
			xmlString = os.toString();
		} catch (JAXBException e) {
			logger.error("The object could not be serialized as xml." + e.getMessage(), e);
		} catch (Exception e) {
			logger.fatal("The object could not be serialized as xml." + e.getMessage(), e);
		}
		return xmlString ;
	}
}
