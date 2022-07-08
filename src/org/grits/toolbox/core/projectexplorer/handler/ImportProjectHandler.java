 
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.MissingReaderException;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.projectexplorer.dialog.importentry.ImportEntryDialog;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utils.FileCopyProgressDialog;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ImportProjectHandler
{
	private static final Logger logger = Logger.getLogger(ImportProjectHandler.class);
	private String workspaceFolder = null;
	private HashMap<String, Element> selectedElementMap = null;

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Optional @Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			IGritsDataModelService gritsDataModelService)
	{
		logger.info("Importing Projects from workspace");

		Set<Entry> selectedEntrySet = new HashSet<Entry>();
		if(object instanceof StructuredSelection)
		{
			Iterator<?> iterator = ((StructuredSelection) object).iterator();
			Object nextSelection = null;
			while(iterator.hasNext())
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					selectedEntrySet.add(gritsDataModelService.findParentByType(
							(Entry) object, ProjectProperty.TYPE));
				}
			}
		}
		else if(object instanceof Entry)
		{
			selectedEntrySet.add(gritsDataModelService.findParentByType((Entry) object, ProjectProperty.TYPE));
		}

		logger.info("opening import entry dialog");
		ImportEntryDialog dialog = new ImportEntryDialog(PropertyHandler.getModalDialog(shell));
		if (dialog.open() == Window.OK)
		{
			workspaceFolder = PropertyHandler.getVariable("workspace_location");
			FileCopyProgressDialog progressDialog = new FileCopyProgressDialog(new File(workspaceFolder));
			try
			{
				List<Element> selectedEntryElements = dialog.getSelectedEntryElements();
				selectedElementMap  = new HashMap<String, Element>();
				for(Element selectedEntryElement : selectedEntryElements)
				{
					selectedElementMap.put(
							selectedEntryElement.getAttributeValue("previousName"), selectedEntryElement);
				}

				ZipFile exportedZippedFile = dialog.getExportedZipFile();
				Enumeration<? extends ZipEntry> entries = exportedZippedFile.entries();

				progressDialog.setTotalSize(exportedZippedFile.size() + selectedEntryElements.size());
				progressDialog.openProgressBar();

				// create a separate thread for copying files
				new Thread()
				{
					public void run()
					{
						try
						{
							ZipEntry zipEntry;
							while(entries.hasMoreElements())
							{
								// decompress next zip entry
								zipEntry = entries.nextElement();
								if(zipEntry.getName().contains("/")
										&& selectedElementMap.containsKey(zipEntry.getName().substring(
												0, zipEntry.getName().indexOf("/"))))
								{
									logger.debug(zipEntry.getName());
									try
									{
										deCompressFiles(exportedZippedFile, zipEntry);
									} catch (IOException ex)
									{
										logger.error(ex.getMessage(), ex);
									}
								}
								progressDialog.updateProgressBar(1);
							}

							// add all selected project entries to workspace
							for(String selectedElement : selectedElementMap.keySet())
							{
								logger.info("adding entry " + selectedElement);
								addProjectEntry(gritsDataModelService,
										selectedElementMap.get(selectedElement).getAttributeValue("name"));
								progressDialog.updateProgressBar(1);
							}
						}
						finally
						{
							progressDialog.closeProgressBar();
						}
					}
				}.start();
			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		logger.debug("Importing Projects from workspace");
	}

	private void addProjectEntry(IGritsDataModelService gritsDataModelService, String projectNewName)
	{
		try
		{
			File projectXmlFile = new File(workspaceFolder,
					projectNewName + File.separator + ".project.xml");
			Map<String, Element> propertyIdElementMap = new HashMap<String, Element>();
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(projectXmlFile);
			Element project = doc.getRootElement();

			if (!"project".equals(project.getName()))
			{
				throw new IOException("Project file does not start with project tag "
						+ projectXmlFile.getAbsolutePath());
			}

			@SuppressWarnings("unchecked")
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
					propertyIdElementMap.put(childElement.getAttributeValue("id"), childElement);
					break;
				default:
					throw new IOException("In .project.xml file project's child element "
							+ "has tag other than entry or property");
				}
			}

			if(entryElement == null)
			{
				throw new IOException("Unable to find entry tag "+ projectXmlFile.getAbsolutePath());
			}

			
			Element propertyElement = propertyIdElementMap.get(entryElement.getAttributeValue("id"));
			if(propertyElement != null &&
					ProjectProperty.TYPE.equals(propertyElement.getAttributeValue("type")))
			{
				entryElement.setAttribute("name", projectNewName);
				FileWriter fileWriter = null;
				try
				{
					XMLOutputter xmlOutput = new XMLOutputter();
					xmlOutput.setFormat(Format.getPrettyFormat());
					if(projectXmlFile.exists() && projectXmlFile.isHidden()
							&& System.getProperty("os.name").startsWith("Windows"))
					{
						Files.setAttribute(projectXmlFile.toPath(), "dos:hidden", false);
					}
					fileWriter = new FileWriter(projectXmlFile);
					xmlOutput.output(doc, fileWriter);
					fileWriter.close();
				} finally
				{
					IOUtils.closeQuietly(fileWriter);
				}
			}

		} catch (JDOMException | IOException e)
		{
			logger.fatal(e.getMessage(), e);
			MessageDialog.openError(
					Display.getCurrent().getActiveShell(),
					"Error", "Error loading project to workspace.");
			return;
		}

		Entry projectEntry = null;
		try
		{
			projectEntry = ProjectFileHandler.loadProject(projectNewName);
			gritsDataModelService.addProjectEntry(projectEntry);
			WorkspaceXMLHandler.updateWorkspaceXMLFile(projectEntry);
		} catch (IOException | UnsupportedVersionException | MissingReaderException e)
		{
			logger.error(e.getMessage(), e);
			try
			{
				// load closed project ".project.xml" is there but the contents cannot be parsed correctly
				projectEntry = ProjectFileHandler.loadPartialProject(projectNewName);
				gritsDataModelService.addProjectEntry(projectEntry);
				WorkspaceXMLHandler.closeProject(projectNewName);
			} catch (Exception e1)
			{
				logger.fatal(e1.getMessage(), e1);
				MessageDialog.openError(
						Display.getCurrent().getActiveShell(),
						"Error", "Error loading partial project to workspace.");
			}
		} catch (Exception e)
		{
			logger.error("ErrorMessage.\n" + e.getMessage(), e);
			WorkspaceXMLHandler.closeProject(projectNewName);
			MessageDialog.openError(
					Display.getCurrent().getActiveShell(),
					"Error", "Error loading project for workspace.");
		}
	}

	private void deCompressFiles(ZipFile exportedZippedFile, ZipEntry zipEntry) throws IOException
	{
		logger.info("decompressing file " + exportedZippedFile.getName());
		String name = zipEntry.getName();
		String projectName = name.substring(0, name.indexOf("/"));
		String newProjectName = selectedElementMap.get(projectName).getAttributeValue("name");
		String newFullName = newProjectName + name.substring(name.indexOf(projectName) + projectName.length());
		FileOutputStream fileOutputstream = null;
		if(zipEntry.isDirectory())
		{
			newFullName = newFullName.replaceAll("\\\\/", File.separator);
			(new File(workspaceFolder, newFullName)).mkdir();
		}
		else
		{
			File entryFile = new File(workspaceFolder + File.separator + newFullName);
			fileOutputstream = new FileOutputStream(entryFile);
			try
			{
				IOUtils.copy(exportedZippedFile.getInputStream(zipEntry), fileOutputstream);
			} finally
			{
				IOUtils.closeQuietly(fileOutputstream);
			}
		}
	}
}