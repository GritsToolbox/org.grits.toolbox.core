package org.grits.toolbox.core.datamodel.io;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.ReportsProperty;
import org.jdom.Element;

/**
 * Reader for SimGlycan. Should check empty values
 * @author kitaemyoung
 *
 */
public class ReportsPropertyReader extends PropertyReader
{

	@Override
	public Property read(Element propertyElement) throws IOException
	{
		ReportsProperty property = new ReportsProperty();

		PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null)
		{
			return readPreVersioning(propertyElement, property);
		}
		else if(property.getVersion().equals("1.0"))
		{
			return readVersion1(propertyElement, property);
		}
		else return null;
	}

	private static Property readVersion1(Element propertyElement,
			ReportsProperty property)
	{
		return property;
	}

	private static Property readPreVersioning(Element propertyElement,
			ReportsProperty property)
	{
		return property;
	}
}
