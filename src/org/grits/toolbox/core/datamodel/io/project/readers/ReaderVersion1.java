/**
 * 
 */
package org.grits.toolbox.core.datamodel.io.project.readers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.jdom.Element;

/**
 * 
 *
 */
public class ReaderVersion1
{
	private static final Logger logger = Logger.getLogger(ReaderVersion0.class);

	public static Property read(Element propertyElement, ProjectProperty property) throws IOException
	{
		logger.debug("Reading project element from version 1.0");
		return property;
	}

}
