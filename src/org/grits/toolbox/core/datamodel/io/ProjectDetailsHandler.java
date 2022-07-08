/**
 * 
 */
package org.grits.toolbox.core.datamodel.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

/**
 * 
 *
 */
public class ProjectDetailsHandler
{
	private static final Logger logger = Logger.getLogger(ProjectDetailsHandler.class);

	public static ProjectDetails getProjectDetails(Entry projectEntry) throws IOException
	{
		if(projectEntry != null && projectEntry.getDisplayName() != null)
		{
			try
			{
				if(projectEntry.getProperty().getType().equals(ProjectProperty.TYPE))
				{
					String workspaceFolder = PropertyHandler.getVariable("workspace_location");
					ProjectProperty projectProperty = (ProjectProperty) projectEntry.getProperty();
					File projectFolder = new File(workspaceFolder.substring(0, workspaceFolder.length()-1)
							+ File.separator
							+ projectEntry.getDisplayName());

					ProjectDetails projectDetails = null;
					File projectDetailsFile = new File(projectFolder, projectProperty.getDetailsFile().getName());
					if(projectDetailsFile.exists())
					{
						projectDetails = getProjectDetailsFromFile(projectDetailsFile);
						if(!projectDetails.getEntryName().equals(projectEntry.getDisplayName()))
						{
							projectDetails.setEntryName(projectEntry.getDisplayName());
							writeProjectDetails(projectDetails);
						}
					}
					else
					{
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Missing Project Details", 
								"The project details file is missing. "
								+ "Creating default project details for " + projectEntry.getDisplayName());
						try
						{
							projectDetails = ProjectDetailsHandler
									.createDefaultProjectDetails(projectEntry.getDisplayName(), null);
							ProjectDetailsHandler.writeProjectDetails(projectDetails);
							ProjectDetailsHandler.addDetailFileNameToProperty(projectEntry);
						} catch (IOException e)
						{
							String errorMessage  = "The project details file could not be created for : " 
									+ projectEntry.getDisplayName() + ".\n" + e.getMessage();
							logger.error(errorMessage, e);
							throw new IOException(errorMessage, e);
						}
					}
					return projectDetails;
				}

			}catch (Exception e)
			{
				logger.error("The project xml could not be read for project Entry." + e.getMessage(), e);
			}
		}
		return null;
	}

	public static ProjectDetails createDefaultProjectDetails(String projectEntryName, String description)
	{
		ProjectDetails projectDetails = null;
		if(projectEntryName != null)
		{
			logger.debug("END  : Creating default project details for project " + projectEntryName);
			projectDetails = new ProjectDetails(projectEntryName);
			List<ProjectCollaborator> collaborators = new ArrayList<ProjectCollaborator>();
			MultiChoicePreference<ProjectCollaborator> collaboratorPreference =
					ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.COLLABORATOR);
			for(ProjectCollaborator defaultCollaborator : collaboratorPreference.getSelectedValues())
			{
				collaborators.add(defaultCollaborator.getACopy());
			}
			projectDetails.setCollaborators(collaborators);

			List<ProjectTasklist> tasklist = new ArrayList<ProjectTasklist>();
			MultiChoicePreference<ProjectTasklist> tasklistPreference =
					ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.TASKLIST);
			for(ProjectTasklist defaultTasklist : tasklistPreference.getSelectedValues())
			{
				tasklist.add(defaultTasklist.getACopy());
			}
			projectDetails.setTasklists(tasklist);

			List<ProjectEvent> events = new ArrayList<ProjectEvent>();
			MultiChoicePreference<ProjectEvent> eventPreference =
					ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.EVENT);
			for(ProjectEvent thisEvent : eventPreference.getSelectedValues())
			{
				events.add(thisEvent.getACopy());
			}
			projectDetails.setEvents(events);

			Set<String> keywords = new HashSet<String>();
			MultiChoicePreference<String> keywordPreference =
					ProjectPreferenceStore.getMultiChoicePreference(
							ProjectPreferenceStore.ParameterizedPreference.KEYWORD);
			keywords.addAll(keywordPreference.getSelectedValues());
			projectDetails.setKeywords(keywords);

			projectDetails.setDescription(description);
			projectDetails.setModificationTime(new Date());
		}
		else
		{
			logger.error("Cannot create default project details for project as projectEntry is null ");
		}

		logger.debug("END  : Creating default project details for project " + projectEntryName);
		return projectDetails;
	}

	/**
	 * serializes project details into a file
	 * @param projectDetails details of the project, uses entry name from the details to find the entry
	 * if projectDetails object is null or its entryName is null
	 * @return true if written to file else return false
	 * @throws IOException if cannot be serialized or for i/o error while writing to file
	 */
	public static boolean writeProjectDetails(ProjectDetails projectDetails) throws IOException
	{
		if(projectDetails == null || projectDetails.getEntryName() == null)
			return false;

		boolean updated = false;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			JAXBContext context = JAXBContext.newInstance(ProjectDetails.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
			marshaller.marshal(projectDetails, os);
			try
			{
				String workspaceFolder = PropertyHandler.getVariable("workspace_location");
				String fileLocation = workspaceFolder.substring(0, workspaceFolder.length()-1) 
						+ File.separator
						+ projectDetails.getEntryName() + File.separator
						+ ProjectProperty.PROJECT_DETAILS_XML;
				FileWriter fileWriter = new FileWriter(fileLocation);
				fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
				fileWriter.close();
				os.close();
				updated = true;
			} catch (IOException e) {
				logger.error("The changes made could not be written to the file..\n" + e.getMessage(), e);
				throw e;
			}
		} catch (JAXBException e) {
			logger.error("The changes made could not be serialized as xml.\n" + e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}
		return updated;
	}

	public static void addDetailFileNameToProperty(Entry projectEntry)
	{
		if(projectEntry != null && projectEntry.getProperty() instanceof ProjectProperty)
		{
			ProjectProperty projectProperty = (ProjectProperty) projectEntry.getProperty();
			addDetailFileNameToProperty(projectProperty);
		}
	}

	private static void addDetailFileNameToProperty(ProjectProperty projectProperty)
	{
		if(projectProperty.getDetailsFile() == null)
		{
			PropertyDataFile propertyDataFile = new PropertyDataFile(
					ProjectProperty.PROJECT_DETAILS_XML,
					ProjectDetails.CURRENT_VERSION,
					ProjectProperty.DETAILS_TYPE);
			projectProperty.getDataFiles().add(propertyDataFile);
		}
	}

	private static ProjectDetails getProjectDetailsFromFile(File file)
	{
		ProjectDetails projectDetails = null;
		try
		{
			FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(inputStream, 
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			JAXBContext context = JAXBContext.newInstance(ProjectDetails.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			projectDetails  = (ProjectDetails) unmarshaller.unmarshal(reader);
		} catch (FileNotFoundException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Reading File", 
					"The object could not be read from xml.");
		} catch (UnsupportedEncodingException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Reading File", 
					"The object could not be read from xml.");
		} catch (JAXBException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Reading File", 
					"The object could not be read from xml.");
		}
		return projectDetails;
	}

}
