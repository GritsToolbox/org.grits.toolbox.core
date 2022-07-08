/**
 *
 */
package org.grits.toolbox.core.datamodel.io.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 *
 */
@Singleton
public class ProjectFileWriter
{
	private static final Logger		logger	= Logger.getLogger(ProjectFileWriter.class);

	@Inject
	@Named(IGritsConstants.WORKSPACE_LOCATION)
	static String					workspaceLocation;
	@Inject
	private IGritsDataModelService	gritsDataModelService;

	// private Integer identifier = 0;

	private void reset(Entry projectEntry)
	{
		gritsDataModelService.setLastIdentifierForProject(projectEntry, 0);
	}

	public void createProjectFiles(Entry projectEntry, String description) throws IOException
	{
		if (projectEntry != null && projectEntry.getDisplayName() != null)
		{
			reset(projectEntry);
			ProjectDetails projectDetails = ProjectDetailsHandler
					.createDefaultProjectDetails(projectEntry.getDisplayName(), description);
			ProjectDetailsHandler.writeProjectDetails(projectDetails);
			ProjectDetailsHandler.addDetailFileNameToProperty(projectEntry);
			createProjectXMLFile(projectEntry);
		}
		else
			throw new IOException("Entry or its name cannot be null.");
	}
	
	public void createProjectFiles(Entry projectEntry, String description, Set<String> keywords) throws IOException
	{
		if (projectEntry != null && projectEntry.getDisplayName() != null)
		{
			reset(projectEntry);
			ProjectDetails projectDetails = ProjectDetailsHandler
					.createDefaultProjectDetails(projectEntry.getDisplayName(), description);
			projectDetails.setKeywords(keywords);
			ProjectDetailsHandler.writeProjectDetails(projectDetails);
			ProjectDetailsHandler.addDetailFileNameToProperty(projectEntry);
			createProjectXMLFile(projectEntry);
		}
		else
			throw new IOException("Entry or its name cannot be null.");
	}

	public void createProjectXMLFile(Entry projectEntry) throws IOException
	{
		if (projectEntry != null)
		{
			String projectFolderName = workspaceLocation + projectEntry.getDisplayName();
			File projectFolder = new File(projectFolderName);
			if (projectFolder.exists() && projectFolder.isDirectory())
			{
				// reset();
				Element projectElement = new Element("project");
				Document doc = new Document(projectElement);
				doc.setRootElement(projectElement);
				this.addEntryAndProperty(projectEntry, projectElement);
				createXmlFile(doc, new File(projectFolderName, ".project.xml"));
			}
		}
	}

	private void addEntryAndProperty(Entry currentEntry, Element parentElement) throws IOException
	{
		Element entryElement = addEntry(currentEntry, parentElement);
		addProperty(currentEntry, parentElement.getDocument().getRootElement());
		Entry projectEntry = findProjectEntry(currentEntry);
		if (projectEntry == null)
		{
			logger.fatal("Cannot determine the project for entry: " + currentEntry.getDisplayName());
			throw new IOException("Cannot determine the project for entry: " + currentEntry.getDisplayName());
		}
		if (currentEntry.getId() == null)
		{// if not we are using the existing id from the object, no need to
			// increment the lastIdentifier
			Integer identifier = gritsDataModelService.getLastIdentifierForProject(projectEntry);
			if (identifier == null)
			{
				logger.fatal("Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
				throw new IOException(
						"Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
			}
			gritsDataModelService.setLastIdentifierForProject(projectEntry, identifier + 1);
		}
		for (Entry child : currentEntry.getChildren())
		{
			if (child.getId() == null)
			{ // if not we are using the existing id from the object, no need to
				// increment the lastIdentifier
				Integer identifier = gritsDataModelService.getLastIdentifierForProject(projectEntry);
				if (identifier == null)
				{
					logger.fatal(
							"Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
					throw new IOException(
							"Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
				}
				gritsDataModelService.setLastIdentifierForProject(projectEntry, identifier + 1);
			}
			this.addEntryAndProperty(child, entryElement);
		}
	}

	private Element addEntry(Entry currentEntry, Element parentElement) throws IOException
	{
		Element entryElement = new Element("entry");
		if (currentEntry.getId() != null)
			entryElement.setAttribute("id", currentEntry.getId().toString());
		else
		{ // creating a completely new one
			Entry projectEntry = findProjectEntry(currentEntry);
			if (projectEntry != null)
			{
				Integer identifier = gritsDataModelService.getLastIdentifierForProject(projectEntry);
				if (identifier != null)
				{
					entryElement.setAttribute("id", identifier.toString());
					// update the entry with the identifier info
					currentEntry.setId(identifier);
					gritsDataModelService.setLastIdentifierForProject(projectEntry, identifier+1);
				}
				else
				{
					logger.fatal(
							"Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
					throw new IOException(
							"Cannot determine the identifier for entry for project: " + projectEntry.getDisplayName());
				}
			}
			else
			{
				logger.fatal("Cannot determine the project for entry: " + currentEntry.getDisplayName());
				throw new IOException("Cannot determine the project for entry: " + currentEntry.getDisplayName());
			}
		}
		entryElement.setAttribute("name", currentEntry.getDisplayName());
		entryElement.setAttribute("creationTime",
				ProjectFileHandler.DATEFORMATER.format(currentEntry.getCreationDate()));
		entryElement.setAttribute("type", currentEntry.getEntryType().toString());
		if (currentEntry.getLastEditorId() != null)
		{
			entryElement.setAttribute("lastEditorId", currentEntry.getLastEditorId());
		}
		parentElement.addContent(entryElement);
		return entryElement;
	}

	private void addProperty(Entry currentEntry, Element rootElement) throws IOException
	{
		Element propertyElement = new Element("property");
		if (currentEntry.getId() != null)
			propertyElement.setAttribute("id", currentEntry.getId().toString());
		else
		{ // creating a completely new one
			Entry projectEntry = findProjectEntry(currentEntry);
			if (projectEntry != null)
			{
				Integer identifier = gritsDataModelService.getLastIdentifierForProject(projectEntry);
				if (identifier != null) {
					propertyElement.setAttribute("id", identifier.toString());
					gritsDataModelService.setLastIdentifierForProject(projectEntry, identifier+1);
				}
				else
				{
					logger.fatal("Cannot determine the identifier for property for project: "
							+ projectEntry.getDisplayName());
					throw new IOException("Cannot determine the identifier for property for project: "
							+ projectEntry.getDisplayName());
				}
			}
			else
			{
				logger.fatal("Cannot determine the project for entry: " + currentEntry.getDisplayName());
				throw new IOException("Cannot determine the project for entry: " + currentEntry.getDisplayName());
			}
			// currentEntry.getProperty().setIdentifier(gritsDataModelService.getLastIdentifier());
		}
		String version = currentEntry.getProperty().getVersion();
		version = version == null ? Property.CURRENT_VERSION : version;
		propertyElement.setAttribute("version", version);
		propertyElement.setAttribute("type", currentEntry.getProperty().getType());
		rootElement.addContent(propertyElement);
		PropertyWriter writer = currentEntry.getProperty().getWriter();
		writer.write(currentEntry.getProperty(), propertyElement);
	}

	protected static void createXmlFile(Document doc, File file) throws IOException
	{
		FileWriter fileWriter = null;
		try
		{
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			if (file.exists() && file.isHidden() && System.getProperty("os.name").startsWith("Windows"))
			{
				Files.setAttribute(file.toPath(), "dos:hidden", false);
			}
			fileWriter = new FileWriter(file);
			xmlOutput.output(doc, fileWriter);
			fileWriter.close();
		}
		finally
		{
			IOUtils.closeQuietly(fileWriter);
		}
	}

	public void renameEntryInProject(File projectXmlFile, Entry entry, String newName) throws IOException
	{
		if (entry.getDisplayName() == null || entry.getDisplayName().isEmpty() || newName == null || newName.isEmpty())
		{
			logger.fatal("entryName, new Name is null or empty : " + entry.getDisplayName() + ", " + newName);
			throw new IOException("entryName, new Name is null or empty : " + entry.getDisplayName() + ", " + newName);
		}
		else
			if (!projectXmlFile.exists())
			{
				logger.fatal("Project xml file does not exist: " + projectXmlFile.getAbsolutePath());
				throw new FileNotFoundException("Project xml file does not exist: " + projectXmlFile.getAbsolutePath());
			}
			else
			{
				// reset(); // do not reset ids, we don't want them to change
				// with renaming the entry
				Document document = null;
				SAXBuilder builder = new SAXBuilder();
				try
				{
					document = builder.build(projectXmlFile);
				}
				catch (JDOMException e)
				{
					logger.fatal(e.getMessage(), e);
					throw new IOException("Project xml is not a valid xml file[" + projectXmlFile.getAbsolutePath()
							+ "]: \n" + e.getMessage());
				}

				Element project = document.getRootElement();
				if (!project.getName().equals("project"))
				{
					logger.fatal("Project file does not start with project tag " + projectXmlFile.getAbsolutePath());
					throw new IOException(
							"Project file does not start with project tag " + projectXmlFile.getAbsolutePath());
				}
				Element entryElement = matchElementWithEntry(project, entry);

				if (entryElement == null)
				{
					logger.fatal("Unable to find entry " + entry.getDisplayName());
					throw new IOException("Unable to find entry " + entry.getDisplayName());
				}
				entryElement.setAttribute("name", newName);
				try
				{
					createXmlFile(document, projectXmlFile);
				}
				catch (IOException e)
				{
					logger.fatal("Error occurred while writing to project xml file : "
							+ projectXmlFile.getAbsolutePath() + "\n" + e.getMessage(), e);
					throw e;
				}
			}
	}
	
	private Entry findProjectEntry(Entry entry)
	{
		return gritsDataModelService.findParentByType(entry, ProjectProperty.TYPE);
	}

	private Element matchElementWithEntry(Element element, Entry entry)
	{
		Element childElement = null;
		Element thisChildElement = null;
		for (Object child : element.getChildren("entry"))
		{
			if (child != null && child instanceof Element)
			{
				thisChildElement = (Element) child;
				if (thisChildElement.getAttribute("name").getValue().equals(entry.getDisplayName())
						&& matchUptoProject(thisChildElement, entry))
				{
					childElement = thisChildElement;
					break;
				}
				else
				{
					childElement = matchElementWithEntry(thisChildElement, entry);
					if (childElement != null)
					{
						break;
					}
				}
			}
		}
		return childElement;
	}

	private boolean matchUptoProject(Element thisChildElement, Entry entry)
	{
		boolean match = thisChildElement != null && thisChildElement.getParentElement() != null && entry != null
				&& entry.getParent() != null;
		if (match)
		{
			Element parentElement = thisChildElement.getParentElement();
			Entry parentEntry = entry.getParent();
			if (parentElement.getName().equals("project"))
			{
				match = parentEntry.getProperty().getType().equals(WorkspaceProperty.TYPE);
			}
			else
			{
				match = parentElement.getAttribute("name").getValue().equals(parentEntry.getDisplayName());
				if (match)
				{
					match = matchUptoProject(parentElement, parentEntry);
				}
			}
		}
		return match;
	}

}
