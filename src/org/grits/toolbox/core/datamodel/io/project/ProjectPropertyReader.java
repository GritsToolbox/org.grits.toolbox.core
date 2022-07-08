package org.grits.toolbox.core.datamodel.io.project;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.io.project.readers.ReaderVersion0;
import org.grits.toolbox.core.datamodel.io.project.readers.ReaderVersion1;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.jdom.Element;

/**
 * Reader for project entry
 * @author kitaemyoung
 *
 */
public class ProjectPropertyReader extends PropertyReader
{
	private static final Logger logger = Logger.getLogger(ProjectPropertyReader.class);

	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException 
	{
		ProjectProperty property = new ProjectProperty();

		PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null)
		{
			logger.debug("Loading pre-versioning project.");
			return ReaderVersion0.read(propertyElement, property);
		}
		else if(property.getVersion().equals("1.0"))
		{
			logger.debug("Loading project version \"1.0\"");
			return ReaderVersion1.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException(
					"This project version is currently not supported.", property.getVersion());
	}
}
