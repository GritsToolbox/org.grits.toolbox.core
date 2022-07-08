 
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.projectexplorer.dialog.ExportEntryDialog;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.utils.FileCopyProgressDialog;
import org.grits.toolbox.core.utils.UtilityZip;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ExportProjectHandler
{
	private static final Logger logger = Logger.getLogger(ExportProjectHandler.class);

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Optional @Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			IGritsDataModelService gritsDataModelService)
	{
		logger.info("Exporting Projects from workspace");

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
							(Entry) nextSelection, ProjectProperty.TYPE));
				}
			}
		}
		else if(object instanceof Entry)
		{
			selectedEntrySet.add(gritsDataModelService.findParentByType(
					(Entry) object, ProjectProperty.TYPE));
		}

		logger.info("opening export dialog");
		ExportEntryDialog dialog = new ExportEntryDialog(
				PropertyHandler.getModalDialog(shell), new ArrayList<Entry>(selectedEntrySet));
		if (dialog.open() == Window.OK)
		{
			try
			{
				List<Entry> selectedEntries = dialog.getSelectedEntries();

				FileOutputStream fos = new FileOutputStream(new File(dialog.getSavingLocation()));
				ZipOutputStream  zipOutputStream = new ZipOutputStream(fos);
				zipOutputStream.setComment("Grits Export from workspace on " + new Date());

				FileCopyProgressDialog progressDialog = 
						new FileCopyProgressDialog(new File(PropertyHandler.getVariable("workspace_location")));
				// set total tasks to number of files to be copied
				long totalTasks = getFileCount(selectedEntries);
				totalTasks = totalTasks == 0l ? 1 : totalTasks;
				progressDialog.setTotalSize(totalTasks);
				progressDialog.openProgressBar();

				logger.info("creating a thread for copying entry");
				// create a new thread for copying files
				new Thread()
				{
					public void run()
					{
						try
						{
							Element entryElements = (new Element("export").setAttribute(
									new Attribute("version", ProjectProperty.CURRENT_VERSION)));
							Document document = new Document(entryElements);
							copyEntries(selectedEntries, entryElements, zipOutputStream, progressDialog);
							saveDocument(document, zipOutputStream);
							zipOutputStream.closeEntry();
						} catch (Exception e)
						{
							logger.error(e.getMessage(), e);
						}
						finally
						{
							try
							{
								zipOutputStream.close();
							} catch (IOException e)
							{
								logger.fatal(e.getMessage(), e);
							}
							try
							{
								fos.close();
							} catch (IOException e)
							{
								logger.fatal(e.getMessage(), e);
							}
							progressDialog.closeProgressBar();
						}
					}
				}.start();

			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		logger.info("Exporting Projects from workspace");
	}

	private void copyEntries(List<Entry> selectedEntries, Element entryElements,
			ZipOutputStream zipOutputStream, FileCopyProgressDialog progressDialog)
	{
		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		// cannot copy folder with size more than this
		final long MAX_FOLDER_SIZE = 5*1024*1024*1024;
		File downloadFile = null;
		for(Entry selectedEntry : selectedEntries)
		{
			// try copying as many entries as possible
			try
			{
				logger.info("copying entry : " + selectedEntry.getDisplayName());
				// project folder for each entry
				downloadFile = new File(workspaceFolder + selectedEntry.getDisplayName());
				if(downloadFile.isDirectory())
				{
					if (downloadFile.length() < MAX_FOLDER_SIZE)
					{
						addDirectorytoZip(downloadFile, downloadFile.getName(),
								zipOutputStream, progressDialog);
					}
					else throw new Exception("Cannot compress folders with large sizes (>"
							+ MAX_FOLDER_SIZE + " bytes)");
				}

				entryElements = entryElements.addContent(new Element("entry")
						.setAttribute(new Attribute("name", selectedEntry.getDisplayName()))
						.setAttribute(new Attribute("previousName", selectedEntry.getDisplayName()))
						.setAttribute(new Attribute("type", selectedEntry.getProperty().getType()))
						.setAttribute(new Attribute("version", selectedEntry.getProperty().getVersion() != null ? selectedEntry.getProperty().getVersion() : "1.0"))
						.setAttribute(new Attribute("flat", selectedEntry.getChildren().isEmpty() 
								? "true" : "false")));

			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void addDirectorytoZip(File downloadFile, String relativeZipPath,
			ZipOutputStream zipOutputStream, FileCopyProgressDialog progressDialog) throws Exception
	{
		try
		{
			zipOutputStream.putNextEntry(new ZipEntry(relativeZipPath + "/"));
			zipOutputStream.closeEntry();
			String relativePath = null;
			for (File childFile : downloadFile.listFiles())
			{
				if (childFile.exists())
				{
					relativePath = relativeZipPath == null || relativeZipPath.isEmpty() ? childFile.getName()
							: relativeZipPath + "/" + childFile.getName();
					if (childFile.isDirectory())
					{
						addDirectorytoZip(childFile, relativePath, zipOutputStream, progressDialog);
					}
					else if (childFile.isFile() || (childFile.isHidden() && !childFile.isDirectory()))
					{
						logger.info("adding file to zip " + childFile.getName());
						UtilityZip.addFile(childFile, relativePath, zipOutputStream);

						// update progress for each file copy
						progressDialog.updateProgressBar(1);
					} 
					else
					{
						logger.error("Error zipping " + childFile.getName());
					}
				}
			}

		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	private long getFileCount(List<Entry> selectedEntries)
	{
		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		int totalFileCount = 0;
		for(Entry selectedEntry : selectedEntries)
		{
			totalFileCount += countFiles(new File(workspaceFolder + selectedEntry.getDisplayName()));
		}
		return totalFileCount;
	}

	private int countFiles(File downloadFile)
	{
		// count number of files in directory and for a file return 1
		if(downloadFile.isDirectory())
		{
			int fileCount = 0;
			for(File childFile : downloadFile.listFiles())
			{
				fileCount += countFiles(childFile);
			}
			return fileCount;
		}
		else if(downloadFile.isFile())
			return 1;
		else
			return 0;
	}

	private static void saveDocument(Document document, ZipOutputStream zipOutputStream) throws IOException
	{
		logger.info("write export info xml file");
		FileWriter fileWriter = null;
		try
		{
			zipOutputStream.putNextEntry(new ZipEntry("export_info.xml"));
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(document, zipOutputStream);
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		} finally
		{
			IOUtils.closeQuietly(fileWriter);
		}
	}

	@CanExecute
	public boolean canExecute(IGritsDataModelService gritsDataModelService)
	{
		return gritsDataModelService.getRootEntry().hasChildren();
	}
}