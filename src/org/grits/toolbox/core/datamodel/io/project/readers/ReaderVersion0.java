/**
 * 
 */
package org.grits.toolbox.core.datamodel.io.project.readers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.jdom.Element;

/**
 * 
 *
 */
public class ReaderVersion0
{
	private static final Logger logger = Logger.getLogger(ReaderVersion0.class);

	private static final String TAG_PROJECT = "project";
	private static final String TAG_DESCRIPTION = "description";
	public static final String ATTRIBUTE_PDETAILS_FILE = "projectDetailsFile";
	private static final String ATTRIBUTE_OPEN = "open";

	public static ProjectProperty read(Element projectPropertyElement, ProjectProperty projectProperty) throws IOException 
	{
		Element projectElement = projectPropertyElement.getChild(TAG_PROJECT);
		if ( projectElement == null )
		{
			String errorMessage = "Project projectProperty misses <" + TAG_PROJECT + "> element.";
			logger.fatal(errorMessage);
			throw new IOException(errorMessage);
		}

		Element descriptionElement = projectElement.getChild(TAG_DESCRIPTION);
		String description = descriptionElement == null ? null : descriptionElement.getValue();
		String projectDetailsFile = projectElement.getAttributeValue(ATTRIBUTE_PDETAILS_FILE);
		if(projectDetailsFile == null)
		{
			logger.error("Creating default Project details");
			Element entryElement = projectPropertyElement.getDocument()
					.getRootElement().getChild("entry");
			String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");
			if(projectName != null)
			{
				ProjectDetails projectDetails = ProjectDetailsHandler
						.createDefaultProjectDetails(projectName, description);
				if(!ProjectDetailsHandler.writeProjectDetails(projectDetails))
				{
					throw new IOException("Error writing project details file for project : " + projectName);
				}
			}
			else
			{
				String errorMessage = "Cannot find project name in .project.xml "
						+ "file while creating project details";
				logger.error(errorMessage);
				throw new IOException(errorMessage);
			}
		}

		PropertyDataFile propertyDataFile = new PropertyDataFile(
				ProjectProperty.PROJECT_DETAILS_XML, 
				ProjectDetails.CURRENT_VERSION, 
				ProjectProperty.DETAILS_TYPE);
		projectProperty.getDataFiles().add(propertyDataFile);
		PropertyReader.UPDATE_PROJECT_XML = true;

		String isOpen = projectElement.getAttributeValue(ATTRIBUTE_OPEN);
		projectProperty.setOpen(isOpen != null && (isOpen.equalsIgnoreCase("true") || isOpen.equalsIgnoreCase("1")));

		return projectProperty;
	}

}
