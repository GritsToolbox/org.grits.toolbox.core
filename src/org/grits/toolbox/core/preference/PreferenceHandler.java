package org.grits.toolbox.core.preference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

@Singleton
public class PreferenceHandler
{
	private static final Logger logger = Logger.getLogger(PreferenceHandler.class);
	@Inject
	static IGritsPreferenceStore gritsPreferenceStore;

	/**
	 * reads the preference from preference file
	 * @param preferenceName is the <b>name</b> of the preference object to be read
	 * @return returns {@link PreferenceEntity} object if the preference element with the given <b>name</b> was found.
	 * It returns <code>null</code> if the element with the given <b>name</b> was not found in the file
	 * @throws UnsupportedVersionException if the <b>version</b> was null for the element 
	 * or if the element with the given <b>name</b> was not successfully deserialized 
	 * as the current version {@link PreferenceEntity} object
	 * @see {@link IGritsPreferenceStore#getPreferenceEntity(String)}
	 */
	public static PreferenceEntity getPreferenceEntity(String preferenceName) throws UnsupportedVersionException
	{
		return gritsPreferenceStore.getPreferenceEntity(preferenceName);
	}

	/**
	 * returns the preference element from the preference file
	 * @param preferenceName is the <b>name</b> of the preference element to be read
	 * @return the org.jdom.Element object as read from the preference file.
	 * This method should only be used if the method <b>getPreferenceByName</b>
	 * fails to read the preference object
	 * @see {@link IGritsPreferenceStore#getPreferenceElement(String)}
	 */
	public static Element getPreferenceElement(String preferenceName)
	{
		return gritsPreferenceStore.getPreferenceElement(preferenceName);
	}

	/**
	 * saves the PreferenceEntity into the preference file
	 * @param preferenceEntity type {@link PreferenceEntity} should contain atleast <b>name</b> 
	 * and <b>version</b> of the preference
	 * @return returns <code>true</code> if the preference was successfully saved in the file 
	 * else returns <code>false</code>.
	 * It cannot save the preference if the <b>name</b> or <b>version</b> is <code>null</code>. For
	 * these cases it returns <code>false</code>.
	 * @see {@link IGritsPreferenceStore#savePreference(PreferenceEntity)}
	 */
	public static boolean savePreference(PreferenceEntity preferenceEntity)
	{
		return gritsPreferenceStore.savePreference(preferenceEntity);
	}

	/**
	 * deletes all the preferences with the given name from the preference file
	 * @param preferenceName name of the preference
	 * @return true if deleted
	 * @see {@link IGritsPreferenceStore#removePreference(String)}
	 */
	public static boolean removePreference(String preferenceName)
	{
		return gritsPreferenceStore.removePreference(preferenceName);
	}

	public static HashMap<String, Boolean> getPreferenceValues(String fileName, String pluginId)
	{
		HashMap<String, Boolean> preferenceValues = new HashMap<String, Boolean>();
		try
		{
			URL resourceURL = FileLocator.toFileURL(
					Platform.getBundle(pluginId).getResource("preference"));
			String fileLocation = resourceURL.getPath() + File.separator + fileName;
			File preferenceFile = new File(fileLocation);
			if(preferenceFile.exists())
			{
				try
				{
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(preferenceFile);
					Element defaultPreferences = document == null ? null : document.getRootElement();
					if (defaultPreferences != null)
					{
						List<?> children = defaultPreferences.getChildren("preference");
						String name = null;
						boolean selected = false;
						for(Object child : children)
						{
							name = ((Element) child).getAttributeValue("name");
							if(name != null)
							{
								selected = ((Element) child).getAttributeValue("selected") == null
										? false : ((Element) child).getAttributeValue("selected").equals("true");
								preferenceValues.put(name, selected);
							}
						}
					}
				} catch (JDOMException e)
				{
					logger.error(e.getMessage(), e);
				} catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			else
			{
				logger.error("File not found for default preference : " + fileLocation);
			}
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		return preferenceValues;		
	}

	public static HashMap<String, Boolean> getPreferenceValues(String fileName)
	{
		return getPreferenceValues(fileName, Activator.PLUGIN_ID);
	}
}
