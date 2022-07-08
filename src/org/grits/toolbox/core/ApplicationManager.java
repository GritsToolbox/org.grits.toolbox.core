package org.grits.toolbox.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.GritsDataModelService;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.MissingReaderException;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.preference.GritsPreferenceStore;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utils.ProjectEntry;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;
import org.grits.toolbox.core.workspace.WorkspaceDialog;
import org.grits.toolbox.core.workspace.WorkspaceHistoryFileHandler;
import org.osgi.framework.FrameworkUtil;

public class ApplicationManager
{
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ApplicationManager.class);
	private static final int OK = 0;
	private static final int CLOSE = 1;
	private static final int EXIT = 2;

	@Inject IEclipseContext eclipseContext;

	@PostContextCreate
	void postContextCreate(IApplicationContext applicationContext)
	{
		// start logging
		//GRITSLog.startLog();

		logger.info("Starting GRITS Application....");

		// set configuration variables
		setConfigVariables();

		int result = createWorkspace();

		if (result == CLOSE || result == EXIT)
		{
			System.exit(-1);
		}

		result = this.loadDataModel();
		//1 --Cancel button is clicked
		//0 --OK button is clicked
		if (result == CLOSE)
		{
			System.exit(-1);
		}

		//log config folder and xml file locations
		logger.info("Configuration Directory \t: " +
				PropertyHandler.getVariable(IGritsConstants.CONFIG_LOCATION));
		logger.info("Workspace Location \t: " +
				PropertyHandler.getVariable(IGritsConstants.WORKSPACE_LOCATION)+".workspace.xml");
		logger.info("History File \t\t: " +
				PropertyHandler.getVariable(IGritsConstants.CONFIG_LOCATION) + "/history.xml");

		disableGeneralMenuItems();
		applicationContext.applicationRunning();

	}
	
	private void disableGeneralMenuItems () {
		Menu systemMenu = Display.getDefault().getSystemMenu();
		if (systemMenu != null) {
		    MenuItem sysItem = getSystemItem(systemMenu, SWT.ID_PREFERENCES);
		    if (sysItem != null)
		    	sysItem.setEnabled(false);
		  //  MenuItem aboutItem = getSystemItem(systemMenu, SWT.ID_ABOUT);
		  //  aboutItem.setEnabled(false);
		} 
	}
		
	static MenuItem getSystemItem(Menu menu, int id) {
	    for (MenuItem item : menu.getItems()) {
	        if (item.getID() == id) return item;
	    }
	    return null;
	}

	private void setConfigVariables()
	{
		logger.info("Setting configuration variables for grits.");

		eclipseContext.set(DataModelHandler.class, 
				ContextInjectionFactory.make(DataModelHandler.class, eclipseContext));
		eclipseContext.set(PropertyHandler.class, 
				ContextInjectionFactory.make(PropertyHandler.class, eclipseContext));
		eclipseContext.set(IGritsPreferenceStore.class,
				ContextInjectionFactory.make(GritsPreferenceStore.class, eclipseContext));
		
		eclipseContext.set(ProjectPreferenceStore.class,
				ContextInjectionFactory.make(ProjectPreferenceStore.class, eclipseContext));
		
		// get the installation configuration folder
		Location configLoc = Platform.getConfigurationLocation();
		eclipseContext.set(IGritsConstants.CONFIG_LOC_INSTALL,
				configLoc.getURL().getPath() + "org.grits.toolbox");
		PropertyHandler.setVariable(IGritsConstants.CONFIG_LOC_INSTALL,
				configLoc.getURL().getPath() + "org.grits.toolbox");

		logger.info("Installation Configuration Directory : " +
				PropertyHandler.getVariable(IGritsConstants.CONFIG_LOC_INSTALL));

		try
		{
			// get the home directory ".grits-toolbox" folder
			String homeDirectory = System.getProperty("user.home");
			if (homeDirectory != null && homeDirectory.trim().length() > 0)
			{
				String gritsFolderName = homeDirectory + File.separator + ".grits-toolbox";
				File gritsFolder = new File(gritsFolderName);
				if ( !gritsFolder.exists() )
				{
					gritsFolder.mkdirs();
				}
				eclipseContext.set(IGritsConstants.CONFIG_LOCATION, gritsFolderName);
				PropertyHandler.setVariable(IGritsConstants.CONFIG_LOCATION , gritsFolderName);
			}
		} catch (Exception e)
		{
			logger.error("Unable to load \".grits-toolbox\" folder from home directory\n" + e.getMessage(), e);
			eclipseContext.set(IGritsConstants.CONFIG_LOCATION,
					configLoc.getURL().getPath() + "org.grits.toolbox");
			PropertyHandler.setVariable(IGritsConstants.CONFIG_LOCATION, 
					configLoc.getURL().getPath() + "org.grits.toolbox");
		}

		logger.info("Configuration Directory : " +
				PropertyHandler.getVariable(IGritsConstants.CONFIG_LOCATION));

		logger.info("Variables Set for Grits Application");
	}

	@PreDestroy
	private void dispose(IApplicationContext applicationContext)
	{
		logger.info("Removing services");

		try
		{
			logger.info("Removing GritsUIService");
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().ungetService(
					Platform.getBundle(Activator.PLUGIN_ID).getBundleContext()
					.getServiceReference(IGritsUIService.class));
		} catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
		}

		try
		{
			logger.info("Removing GritsDataModelService");
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().ungetService(
					Platform.getBundle(Activator.PLUGIN_ID).getBundleContext()
					.getServiceReference(IGritsDataModelService.class));
		} catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
		}

		logger.info("Services removed");
	}

	private int createWorkspace()
	{
		logger.info("Initializing Grits workspace..");

		int result = 0;
		Shell shell = PropertyHandler.getModalDialog(Display.getCurrent().getActiveShell());
		result = this.createWorkspaceDialog();

		//1 --Cancel button is clicked
		//0 --OK button is clicked
		//2 --also need to exit 
		if (result == CLOSE || result == EXIT)
		{
			return result; 
		}

		//before opening the application
		//we need to check whether this workspace contains .workspace.xml file or not
		//if not then create a new workspace xml file
		try
		{
			if (!WorkspaceXMLHandler.CheckWorkspaceXMLExist())
			{
				// then keep going and create a new workspace xml file
				WorkspaceXMLHandler.createNewWorkspaceXMLFile();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			ErrorUtils.createErrorMessageBoxReturn(shell,
					"Unable to create workspace xml file",e);
		}

		//check if workspace file is correct 
		try
		{
			WorkspaceXMLHandler.checkWorkspaceXMLfile();
		} catch (IOException e)
		{
			logger.error(e.getMessage(),e);
			result = ErrorUtils.createErrorMessageBoxReturn(shell,
					"Invalid workspace file",new IOException(
							"Missing or invalid tag\nPlease choose another workspace"));
			result = createWorkspace();
		} catch (Exception e)
		{
			logger.error(e.getMessage(),e);
			result = ErrorUtils.createErrorMessageBoxReturn(shell,
					"Invalid workspace file",new IOException(
							"Missing or invalid tag\nPlease choose another workspace"));
			result = createWorkspace();
		}
		return result;
	}

	/**
	 * first dialog before the main display!
	 * @return
	 * @throws IOException
	 */
	private int createWorkspaceDialog()
	{
		File configDir = new File(PropertyHandler.getVariable("configuration_location"));
		Shell shell = PropertyHandler.getModalDialog(Display.getCurrent().getActiveShell());
		//find the center of a main monitor
		locateCenterOfMonitor(shell);
		if (!configDir.exists())
		{
			//then create the folder and file
			boolean created = new File(PropertyHandler.getVariable("configuration_location")).mkdir();
			if(!created)
			{
				//need to close
				return ErrorUtils.createErrorMessageBoxReturn(shell, "Error", new IOException("Configuration folder cannot be created."));
			}
			//then create the history xml file
			try {
				WorkspaceHistoryFileHandler.createHistoryXMLFile();
			} catch (Exception e) {
				//need to log the error
				logger.error(e.getMessage(),e);
				//need to close
				return ErrorUtils.createErrorMessageBoxReturn(shell,"Error: Cannot create history xml",e);
			}
		}
		else
		{
			//then check if xml file exists or not
			if (!WorkspaceHistoryFileHandler.isHistoryExists())
			{
				//if does not exist, then create a new one
				try {
					WorkspaceHistoryFileHandler.createHistoryXMLFile();
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					//cannot create history xml file
					return ErrorUtils.createErrorMessageBoxReturn(shell,"Error: Cannot create history xml",e);
				}
			}
		}

		WorkspaceDialog dialog = new WorkspaceDialog(shell);
		int result = dialog.open();
		PropertyHandler.setVariable(IGritsConstants.WORKSPACE_LOCATION, dialog.getPath() + File.separator);
		eclipseContext.set(IGritsConstants.WORKSPACE_LOCATION, dialog.getPath() + File.separator);
		return result;
	}

	/**
	 * find a center of a monitor and move shell to that position
	 * @param shell
	 */
	private void locateCenterOfMonitor(Shell shell)
	{
		Monitor primary = shell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
		shell.setSize(500, 300);
	}

	private int loadDataModel() {
		//use Entry object to initiate a tree
		//create workspace entry and add to datamodel
		//get all projects from the .workspace file
		//for each project load . project file and create entries
//		DataModelHandler dataModelHandler = PropertyHandler.getDataModel();

		//for workspace
		Entry workspaceEntry = new Entry();
		workspaceEntry.setDisplayName("workspace");
		WorkspaceProperty wp = new WorkspaceProperty();
		wp.setLocation(PropertyHandler.getVariable("workspace_location"));
		workspaceEntry.setProperty(wp);
//		dataModelHandler.setRoot(workspaceEntry);
		IGritsDataModelService dataModelService = 
				eclipseContext.get(IGritsDataModelService.class);
		dataModelService.setRoot(workspaceEntry);
		eclipseContext.set(IGritsDataModelService.WORKSPACE_ENTRY, workspaceEntry);
		GritsDataModelService.updateMode = false;

		Display display = Display.getCurrent();
		Shell shell = PropertyHandler.getModalDialog(display.getActiveShell());
		ContextInjectionFactory.make(ProjectFileHandler.class, eclipseContext);
		//get project folders
		ProjectEntry[] projectFolders =  null;

		try
		{
			projectFolders = WorkspaceXMLHandler.getProjectFolders();
			Entry projectEntry = null;
			//if projectFolders exist; loop through each project
			for (ProjectEntry projectFolder: projectFolders)
			{
				try
				{
					projectEntry = projectFolder.getOpen() ? 
							ProjectFileHandler.loadProject(projectFolder.getName()) 
							: ProjectFileHandler.loadPartialProject(projectFolder.getName());
							dataModelService.addEntry(workspaceEntry, projectEntry);//, false);
							WorkspaceXMLHandler.setOpenClosed(projectEntry);
				} catch (FileNotFoundException e)
				{
					logger.error(e.getMessage(),e);
					if(e.getMessage().contains("Access is denied"))
					{
						// .project.xml file is not being opened or closed
						try
						{
							projectEntry = ProjectFileHandler.loadPartialProject(projectFolder.getName());
							dataModelService.addEntry(workspaceEntry, projectEntry);
							WorkspaceXMLHandler.closeProject(projectFolder.getName());
						} catch (IOException ex) {
							Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
							logger.error(ex.getMessage(), ex);
							ErrorUtils.createErrorMessageBoxReturn(shell1, "Error", ex);
						} catch (Exception e1) {
							Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
							logger.error(e1.getMessage(),e1);
							ErrorUtils.createErrorMessageBoxReturn(shell1,"Error",e1);
							return CLOSE;// close the workspace
						}
					}
					else
					{
						// remove the project entry from .workspace.xml as project.xml file is missing
						try
						{
							WorkspaceXMLHandler.removeProjectEntry(projectFolder.getName());
						} catch (Exception e1) {
							Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
							logger.error(e1.getMessage(),e1);
							ErrorUtils.createErrorMessageBoxReturn(shell1,"Error",e1);
							return CLOSE;// close the workspace
						}
					}

				} catch (IOException | UnsupportedVersionException | MissingReaderException e) {
					// load closed project ".project.xml" is there but the contents cannot be parsed correctly
					logger.error(e.getMessage(),e);
					try {
						projectEntry = ProjectFileHandler.loadPartialProject(projectFolder.getName());
						dataModelService.addEntry(workspaceEntry, projectEntry);
						WorkspaceXMLHandler.closeProject(projectFolder.getName());
						//					DataModelSearch.removeProjectEntryFromWorkspace(dm.getRoot(), projectFolder.getName());
						//					projectFileHandler.readProjectXMLFile(wp.getLocation() + projectFolder.getName(), dm, false, true);
					} catch (IOException ex) {
						// remove the project entry from workspace as partial load is unsuccessful
						try {
							WorkspaceXMLHandler.removeProjectEntry(projectFolder.getName());
						} catch (Exception e1) {
							Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
							logger.error(e1.getMessage(),e1);
							ErrorUtils.createErrorMessageBoxReturn(shell1,"Error",e1);
							return CLOSE;// close the workspace
						}
					} catch (Exception e1) {
						Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
						logger.error(e1.getMessage(),e1);
						ErrorUtils.createErrorMessageBoxReturn(shell1,"Error",e1);
						return CLOSE;// close the workspace
					}
				} catch (Exception e) {
					// remove the project entry from .workspace.xml
					logger.error(e.getMessage(),e);
					try {
						WorkspaceXMLHandler.removeProjectEntry(projectFolder.getName());
					} catch (Exception e1) {
						Shell shell1 = PropertyHandler.getModalDialog(display.getActiveShell());
						logger.error(e1.getMessage(),e1);
						ErrorUtils.createErrorMessageBoxReturn(shell1,"Error",e1);
						return CLOSE;// close the workspace
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return ErrorUtils.createErrorMessageBoxReturn(shell,"Error",e);
		}

		GritsDataModelService.updateMode = true;
		return OK;
	}

}
