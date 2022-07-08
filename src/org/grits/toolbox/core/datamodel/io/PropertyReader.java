package org.grits.toolbox.core.datamodel.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.jdom.Attribute;
import org.jdom.Element;

public abstract class PropertyReader 
{
	private static final Logger logger = Logger.getLogger(PropertyReader.class);
	public static boolean UPDATE_PROJECT_XML = false;

	/**
	 * It adds generic information to the property only if the version attribute is not null.
	 * It adds <b>version</b> to the property object and adds the list of files (<b>name</b>, 
	 * <b>version</b>, <b>type</b>). If the <b>version</b> attribute of the property is missing
	 * it sets version as null and returns the object without the list of files.
	 * @param propertyElement the property element that contains the information
	 * @param property the object that is to be filled
	 */
	public static void addGenericInfo(Element propertyElement, Property property)
	{
		Attribute versionAttribute = propertyElement.getAttribute("version");
		if(versionAttribute != null)
		{
			property.setVersion(versionAttribute.getValue());

			List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
			Element fileElement = null;
			String name = null;
			PropertyDataFile dataFile = null;
			for(Object ch : propertyElement.getChildren("file"))
			{
				if(ch instanceof Element)
				{
					fileElement = (Element) ch;
					name = fileElement.getAttributeValue("name");
					if(name != null)
					{
						dataFile = new PropertyDataFile(name,
								fileElement.getAttributeValue("version"),
								fileElement.getAttributeValue("type"));
						dataFiles.add(dataFile);
					}
					else
					{
						logger.error("No file name found for data file in property : " + property.getType());
					}
				}
			}
			property.setDataFiles(dataFiles);
		}
		else
		{
			property.setVersion(null);
			logger.error("No version was found for the property : " + property.getType());
		}
	}

	public abstract Property read(Element propertyElement) throws UnsupportedVersionException, IOException;
}
