package org.grits.toolbox.core.datamodel.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.project.ProjectFileReader;
import org.grits.toolbox.core.datamodel.io.project.ProjectFileWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.ErrorUtils;

/**
 * Manage project xml file
 *
 * @author Ki Tae Myoung
 */
@Singleton
public class ProjectFileHandler
{
	private static final Logger				logger				= Logger.getLogger(ProjectFileHandler.class);
	public static final SimpleDateFormat	DATEFORMATER		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			Locale.US);

	private static ProjectFileReader		projectFileReader	= null;
	private static ProjectFileWriter		projectFileWriter	= null;

	@Inject
	@Named(IGritsConstants.WORKSPACE_LOCATION)
	static String							workspaceLocation;

	@Inject
	public ProjectFileHandler(IEclipseContext eclipseContext)
	{
		projectFileReader = ContextInjectionFactory.make(ProjectFileReader.class, eclipseContext);
		projectFileWriter = ContextInjectionFactory.make(ProjectFileWriter.class, eclipseContext);
	}

	/**
	 * It creates projectEntry, ''project'' folder, ".project.xml" file and
	 * "project_details.xml" file with default content for the project
	 *
	 * @param projectName
	 *            name of the project
	 * @param description
	 *            description of the project
	 * @return returns projectEntry or null if projectName is null (does not
	 *         create files or folder)
	 * @throws FileAlreadyExistsException
	 *             when ''project'' folder with this name and (or) its
	 *             ".project.xml" file already exists.
	 * @throws IOException
	 *             if i/o error happens while writing ".project.xml" file.
	 */
	public static Entry createProject(String projectName, String description)
			throws FileAlreadyExistsException, IOException
	{
		Entry projectEntry = null;
		if (projectName != null)
		{
			String projectFolderName = workspaceLocation + projectName;
			File projectFolder = new File(projectFolderName);
			if (!projectFolder.exists())
			{
				projectFolder.mkdir();
				projectEntry = new Entry();
				projectEntry.setDisplayName(projectName);
				projectEntry.setCreationDate(new Date());
				projectEntry.setProperty(new ProjectProperty());
				projectFileWriter.createProjectFiles(projectEntry, description);
			}
			else
				throw new FileAlreadyExistsException("Cannot create project as project folder already exists : "
						+ projectFolderName + " at " + projectFolder.getAbsolutePath());
		}
		else
			logger.error("project name is null.");
		return projectEntry;
	}

	/**
	 * It creates projectEntry, ''project'' folder, ".project.xml" file and
	 * "project_details.xml" file with default content and keywords for the project
	 *
	 * @param projectName
	 *            name of the project
	 * @param description
	 *            description of the project
	 * @param keywords
	 *            sets the keywords for the project for searching or filtering
	 * @return returns projectEntry or null if projectName is null (does not
	 *         create files or folder)
	 * @throws FileAlreadyExistsException
	 *             when ''project'' folder with this name and (or) its
	 *             ".project.xml" file already exists.
	 * @throws IOException
	 *             if i/o error happens while writing ".project.xml" file.
	 */
	public static Entry createProject(String projectName, String description, Set<String> keywords)
			throws FileAlreadyExistsException, IOException
	{
		Entry projectEntry = null;
		if (projectName != null)
		{
			String projectFolderName = workspaceLocation + projectName;
			File projectFolder = new File(projectFolderName);
			if (!projectFolder.exists())
			{
				projectFolder.mkdir();
				projectEntry = new Entry();
				projectEntry.setDisplayName(projectName);
				projectEntry.setCreationDate(new Date());
				projectEntry.setProperty(new ProjectProperty());
				projectFileWriter.createProjectFiles(projectEntry, description, keywords);
			}
			else
				throw new FileAlreadyExistsException("Cannot create project as project folder already exists : "
						+ projectFolderName + " at " + projectFolder.getAbsolutePath());
		}
		else
			logger.error("project name is null.");
		return projectEntry;
	}

	/**
	 * It reads ".project.xml" file from the ''project'' folder and returns an
	 * entry or null (if the name was null). It creates "project_details.xml"
	 * file with default values, if this file does not exist. It throws
	 * FileNotFoundException if the ''project'' folder or ".project.xml" file is
	 * missing. It throws IOException if one of the existing files could not be
	 * read.
	 *
	 * @param projectName
	 *            name of the project
	 * @return returns projectEntry or null if projectName is null
	 * @throws FileNotFoundException
	 *             if the ''project'' folder or ".project.xml" file is missing
	 * @throws IOException
	 *             if one of the existing files could not be read
	 * @throws UnsupportedVersionException
	 *             if the content of ".project.xml" file has child elements with
	 *             version that are no longer supported in current version
	 * @throws MissingReaderException
	 *             if some of the entry cannot be read from the current readers
	 */
	public static Entry loadProject(String projectName)
			throws FileNotFoundException, IOException, UnsupportedVersionException, MissingReaderException
	{
		Entry projectEntry = null;
		if (projectName != null)
		{
			String projectFolderName = workspaceLocation + projectName + File.separator + ".project.xml";
			File projectXmlFile = new File(projectFolderName);
			if (projectXmlFile.exists())
			{
				try
				{
					projectEntry = projectFileReader.readProjectXMLFile(projectXmlFile);
					if (PropertyReader.UPDATE_PROJECT_XML)
					{
						projectFileWriter.createProjectXMLFile(projectEntry);
						PropertyReader.UPDATE_PROJECT_XML = false;
					}
				}
				catch (IOException ex)
				{
					logger.error(ex.getMessage(), ex);
					throw new IOException("Error reading project : " + projectName + ".\n" + ex.getMessage(), ex);
				}
			}
			else
				throw new FileNotFoundException("\".project.xml\" file is missing : " + projectFolderName + " at "
						+ projectXmlFile.getAbsolutePath());
		}
		else
			logger.error("project name is null.");
		return projectEntry;
	}

	/**
	 * It reads ".project.xml" file from the ''project'' folder and returns an
	 * entry or null (if the name was null). It creates "project_details.xml"
	 * file with default values, if this file does not exist. It throws
	 * FileNotFoundException if the ''project'' folder or ".project.xml" file is
	 * missing. It throws IOException if one of the existing files could not be
	 * read.
	 *
	 * @param projectName
	 *            name of the project
	 * @return returns projectEntry or null if projectName is null
	 * @throws FileNotFoundException
	 *             if the ''project'' folder or ".project.xml" file is missing
	 * @throws IOException
	 *             if one of the existing files could not be read
	 */
	public static Entry loadPartialProject(String projectName) throws FileNotFoundException, IOException
	{
		Entry projectEntry = null;
		if (projectName != null)
		{
			String projectFolderName = workspaceLocation + projectName + File.separator + ".project.xml";
			File projectXmlFile = new File(projectFolderName);
			if (projectXmlFile.exists())
			{
				try
				{
					projectEntry = projectFileReader.readPartialProjectXMLFile(projectXmlFile);
				}
				catch (IOException ex)
				{
					logger.error(ex.getMessage(), ex);
					throw new IOException("Error reading project : " + projectName + ".\n" + ex.getMessage(), ex);
				}
			}
			else
				throw new FileNotFoundException("\".project.xml\" file is missing : " + projectFolderName + " at "
						+ projectXmlFile.getAbsolutePath());
		}
		else
			logger.error("project name is null.");
		return projectEntry;
	}

	/**
	 * It saves ".project.xml" file and "project_details.xml" file with the
	 * entry content and re-writes both the files. It creates
	 * "project_details.xml" file with default values, if this file does not
	 * exist.
	 *
	 * @param projectEntry
	 * @return true if saved else false (if projectEntry is null)
	 * @throws FileNotFoundException
	 *             if the ''project'' folder or ".project.xml" file is missing
	 * @throws IOException
	 *             if i/o error happens while writing ".project.xml" file.
	 */
	public static boolean saveProject(Entry projectEntry) throws FileNotFoundException, IOException
	{
		boolean saved = false;
		if (projectEntry != null)
		{
			projectFileWriter.createProjectXMLFile(projectEntry);
			saved = true;
		}
		else
			logger.error("project entry is null.");
		return saved;
	}

	/**
	 * renames entry inside the project with the newName and re-writes the
	 * ".project.xml" file
	 *
	 * @param entryToBeRenamed
	 * @param newName
	 * @return returns the renamedEntry or null (if either entryToBeRenamed is
	 *         null or newName is null or Empty)
	 * @throws FileNotFoundException
	 *             if the ''project'' folder or ".project.xml" file is missing
	 * @throws IOException
	 *             if ".project.xml" file could not be read
	 */
	public static boolean renameEntryInProject(Entry entryToBeRenamed, String newName)
			throws FileNotFoundException, IOException
	{
		boolean renamed = false;
		if (entryToBeRenamed != null && newName != null && !newName.isEmpty())
		{
			Entry projectEntry = DataModelSearch.findParentByType(entryToBeRenamed, ProjectProperty.TYPE);
			String projectFolderLocation = workspaceLocation + projectEntry.getDisplayName();
			try
			{
				File projectFolder = new File(projectFolderLocation);
				if (projectFolder.exists() && projectFolder.isDirectory())
				{
					File projectXmlFile = null;
					for (File childFile : projectFolder.listFiles())
					{
						if (childFile.isFile() && childFile.getName().equals(".project.xml"))
						{
							projectXmlFile = childFile;
							break;
						}
					}
					if (projectXmlFile != null)
					{
						projectFileWriter.renameEntryInProject(projectXmlFile, entryToBeRenamed, newName);
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Unable to update project xml",
						e);
				return false;
			}
		}
		else
			logger.error("project entry is null.");
		return renamed;
	}

	/**
	 * It removes the child entry from the project and re-writes the
	 * ".project.xml" file
	 *
	 * @param childEntry
	 *            the entry to be removed
	 * @return true if deleted
	 * @throws FileAlreadyExistsException
	 *             if the ''project'' folder or ".project.xml" file is missing
	 * @throws IOException
	 *             if ".project.xml" file could not be read
	 */
	public static boolean deleteEntryFromProject(Entry childEntry) throws FileAlreadyExistsException, IOException
	{
		boolean deleted = false;
		if (childEntry != null)
		{
			Entry projectEntry = DataModelSearch.findParentByType(childEntry, ProjectProperty.TYPE);
			if (removeEntry(projectEntry, childEntry))
			{
				projectFileWriter.createProjectXMLFile(projectEntry);
				deleted = true;
			}
		}
		else
			logger.error("project entry is null.");
		return deleted;
	}

	private static boolean removeEntry(Entry parentEntry, Entry childToRemove)
	{
		boolean removed = false;
		if (!(removed = parentEntry.removeChild(childToRemove)))
		{
			for (Entry child : parentEntry.getChildren())
			{
				if (removeEntry(child, childToRemove))
				{
					removed = true;
					break;
				}
			}
		}
		return removed;
	}
}