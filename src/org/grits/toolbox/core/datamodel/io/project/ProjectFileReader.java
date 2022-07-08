/**
 * 
 */
package org.grits.toolbox.core.datamodel.io.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.GritsDataModelService;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.MissingReaderException;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * 
 *
 */
@Singleton
public class ProjectFileReader
{
	private static final Logger logger = Logger.getLogger(ProjectFileReader.class);

	@Inject private IGritsDataModelService gritsDataModelService;

	private boolean partialReadMode = false;
	private HashMap<String,Element> propertyIdElementMap = new HashMap<String,Element>();
	
	public Integer lastIdentifier = 0;

	private void clearMap()
	{
		propertyIdElementMap.clear();
	}
	
	private Entry getEntry(Element entryElement) throws IOException, UnsupportedVersionException, MissingReaderException 
	{
		Entry currentEntry = new Entry();
		String idValue = entryElement.getAttributeValue("id");
		Integer identifier = null;
		if (idValue != null) {
			try {
				identifier = Integer.parseInt(idValue);
			} catch (NumberFormatException e) {
				logger.fatal("Identifier is invalid for entry name (id=" + entryElement.getAttributeValue("id") + ").");
				throw new IOException("Identifier is invalid for entry name (id=" + entryElement.getAttributeValue("id") + ").");
			}
		}
		String attributeValue = entryElement.getAttributeValue("name");
		if (attributeValue == null)
		{
			logger.fatal("Unable to find entry name (id=" + entryElement.getAttributeValue("id") + ").");
			throw new IOException("Unable to find entry name (id=" + entryElement.getAttributeValue("id") + ").");
		}
		if (identifier > lastIdentifier)
			lastIdentifier = identifier;
		currentEntry.setId(identifier);
		currentEntry.setDisplayName(attributeValue);
		Date creationTime = new Date();
		attributeValue = entryElement.getAttributeValue("creationTime");
		try
		{
			creationTime = attributeValue == null 
					? creationTime : ProjectFileHandler.DATEFORMATER.parse(attributeValue);
		}
		catch(Exception e)
		{
			logger.error("Unable to parse creation date for entry (id=" 
					+ entryElement.getAttributeValue("id") + "): " + attributeValue);
		}
		currentEntry.setCreationDate(creationTime);

		attributeValue = entryElement.getAttributeValue("lastEditorId");
		if (attributeValue != null)
		{
			currentEntry.setLastEditorId(attributeValue);
		}

		attributeValue = entryElement.getAttributeValue("type");
		if (attributeValue != null)
		{
			try
			{
				currentEntry.setEntryType(Integer.parseInt(attributeValue));
			}
			catch (Exception e)
			{
				logger.error("Entry type was not a number: " + attributeValue);
			}
		}

		Element propertyElement = this.propertyIdElementMap.get(entryElement.getAttributeValue("id"));
		if(propertyElement == null)
		{
			logger.fatal("Unable to find property for entry (id=" + entryElement.getAttributeValue("id") + ").");
			throw new IOException("Unable to find property for entry (id=" + entryElement.getAttributeValue("id") + ").");
		}

		try 
		{
			PropertyReader reader = this.buildReader(propertyElement.getAttributeValue("type"));
			Property t_property = reader.read(propertyElement);
			currentEntry.setProperty(t_property);
			//t_property.setIdentifier(identifier);

			if(!partialReadMode)
			{
				if(!(currentEntry.getProperty() instanceof ProjectProperty)
						|| ((ProjectProperty) currentEntry.getProperty()).isOpen())
				{
					addChildrenEntries(entryElement, currentEntry);
				}
			}
		} catch (MissingReaderException ex)
		{
			logger.fatal("No Reader found for " + propertyElement.getAttributeValue("type")
					+ "\n" + ex.getMessage(), ex);
			if(!partialReadMode) throw ex;
		} catch (UnsupportedVersionException ex)
		{
			logger.fatal("Version is not supported for " + propertyElement.getAttributeValue("type")
					+ "\n" + ex.getMessage(), ex);
			if(!partialReadMode) throw ex;
		} catch (IOException ex) 
		{
			logger.fatal(ex.getMessage(), ex);
			throw ex;
		}

		return currentEntry;
	}

	@SuppressWarnings("unchecked")
	private void addChildrenEntries(Element entryElement, Entry parentEntry) throws IOException, UnsupportedVersionException, MissingReaderException
	{
		List<Element> children = entryElement.getChildren();
		for(Element child : children)
		{
			if (child.getName().equals("entry"))
			{
				GritsDataModelService.updateMode = false;
				gritsDataModelService.addEntry(parentEntry, this.getEntry(child));
				GritsDataModelService.updateMode = true;
			}
		}
	}

	private PropertyReader buildReader(String entryType) throws IOException, MissingReaderException
	{
		for (IExtension thisExtention : Platform.getExtensionRegistry().getExtensionPoint(
				Activator.PLUGIN_ID,"propertyreader").getExtensions())
		{
			for (IConfigurationElement thisConfigurationElement : thisExtention.getConfigurationElements())
			{
				if(thisConfigurationElement.getName().equals("reader")
						&& thisConfigurationElement.getAttribute("propertyId").equals(entryType))
				{
					try
					{
						Object readerClass = thisConfigurationElement.createExecutableExtension("class");
						PropertyReader propertyReader = (PropertyReader) readerClass;
						return propertyReader;
					}
					catch (Exception e)
					{
						logger.fatal("Unable to instantiate PropertyReader for type: " + entryType, e);
						throw new IOException("Unable to instantiate PropertyReader for type: " + entryType,e);
					}

				}
			}
		}
		logger.warn("Unable to find PropertyReader for type: " + entryType);
		throw new MissingReaderException("Unable to find PropertyReader for type: " + entryType);
	}

	public Entry readProjectXMLFile(File projectXmlFile) throws FileNotFoundException, IOException, UnsupportedVersionException, MissingReaderException
	{
		clearMap();
		partialReadMode = false;
		return buildProjectEntry(projectXmlFile);
	}

	public Entry readPartialProjectXMLFile(File projectXmlFile) throws IOException
	{
		clearMap();
		partialReadMode = true;
		Entry projectEntry = null;
		try
		{
			projectEntry = buildProjectEntry(projectXmlFile);
			((ProjectProperty) projectEntry.getProperty()).setOpen(false);
		} catch(UnsupportedVersionException | MissingReaderException ex)
		{
			logger.fatal("This error should not happen in partial read mode.\n" + ex.getMessage(), ex);
		} catch(IOException ex)
		{
			logger.fatal(ex.getMessage(), ex);
			throw ex;
		} finally
		{
			partialReadMode = false;
		}
		return projectEntry;
	}

	@SuppressWarnings("unchecked")
	private Entry buildProjectEntry(File projectXmlFile) throws FileNotFoundException, IOException, UnsupportedVersionException, MissingReaderException
	{
		Entry projectEntry = null;
		if(projectXmlFile != null && projectXmlFile.exists())
		{
			try
			{
				this.propertyIdElementMap.clear();
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(projectXmlFile);
				Element project = doc.getRootElement();
				if (!project.getName().equals("project"))
				{
					logger.fatal("Project file does not start with project tag "+ projectXmlFile.getAbsolutePath());
					throw new IOException("Project file does not start with project tag "+ projectXmlFile.getAbsolutePath());
				}
				List<Element> children = project.getChildren();
				Element entryElement = null;
				for (Element childElement : children)
				{
					switch (childElement.getName())
					{
					case "entry":
						entryElement = childElement;
						break;
					case "property":
						this.propertyIdElementMap.put(childElement.getAttributeValue("id"), childElement);
						break;
					default:
						throw new IOException("In .project.xml file project's child element "
								+ "has tag other than entry or property");
					}
				}

				if (entryElement == null)
				{
					logger.fatal("Unable to find entry tag "+ projectXmlFile.getAbsolutePath());
					throw new IOException("Unable to find entry tag "+ projectXmlFile.getAbsolutePath());
				}

				projectEntry = this.getEntry(entryElement);
				PropertyHandler.getDataModel().setShow(true);
				if (projectEntry != null) {
					gritsDataModelService.setLastIdentifierForProject (projectEntry, lastIdentifier+1);
					lastIdentifier = 0;
				}
				this.propertyIdElementMap.clear();
			}
			catch (JDOMException e)
			{
				logger.fatal(e.getMessage(), e);
				throw new IOException("Project xml is not a valid xml file [" 
						+ projectXmlFile.getAbsolutePath() + "]: \n" + e.getMessage());
			}
		}
		else
		{
			logger.fatal("Project xml file does not exist: " + projectXmlFile.getAbsolutePath());
			throw new FileNotFoundException("Project xml file does not exist: " + projectXmlFile.getAbsolutePath());
		}
		
		return projectEntry;
	}

}
