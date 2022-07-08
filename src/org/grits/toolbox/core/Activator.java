package org.grits.toolbox.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator
{
	// The plug-in ID
	public static final String PLUGIN_ID = "org.grits.toolbox.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	//log4J Logger
	private static final Logger logger = Logger.getLogger(Activator.class);

	/**
	 * The constructor
	 */
	public Activator()
	{
		
	}

	public void start(BundleContext context) throws Exception
	{
		//super.start(context);
		plugin = this;
		
		// Setup logging
	    URL confURL = Platform.getBundle(PLUGIN_ID).getEntry("log4j2.xml");
	    LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
	    loggerContext.setConfigLocation (FileLocator.toFileURL(confURL).toURI());
	    final Configuration config = loggerContext.getConfiguration();
	    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd-hh.mm.ss", Locale.ENGLISH);
	    final PatternLayout layout = PatternLayout.newBuilder().withPattern("[%-5p] %d{ISO8601} [%C %M %L] - %m%n").build();
	    FileAppender appender = FileAppender.newBuilder()
	            .withFileName(getLogFolderPath() + File.separator + "GRITS-v" + GritsConfig.VERSION + "-" + format.format(new Date()) + ".log")
	            .withAppend(true)
	            .setName("GRITS-log")
	            .setLayout(layout).build();
	    
        appender.start();
        config.addAppender(appender);
        
        
        loggerContext.getRootLogger().addAppender(appender);
        loggerContext.updateLoggers();
        
	    //PropertyConfigurator.configure( FileLocator.toFileURL(confURL).getFile());
	    logger.info("Logging using log4j and configuration " + FileLocator.toFileURL(confURL).getFile());
	}

	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		//super.stop(context);
		logger.info(PLUGIN_ID + " END");
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}

	public static ImageDescriptor imageDescriptorFromPlugin(String pluginId, String imageFilePath)
	{
		ImageDescriptor imageDescriptor = null;
		if(pluginId != null && imageFilePath != null)
		{
			Bundle bundle = Platform.getBundle(pluginId);
			if(bundle != null)
			{
				URL fullPathString = FileLocator.find(bundle, new Path(imageFilePath), null);
				if(fullPathString == null) {
					try
					{
						fullPathString = new URL(imageFilePath);
					} catch (MalformedURLException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
				if(fullPathString != null)
					imageDescriptor = ImageDescriptor.createFromURL(fullPathString);
			}
		}
		else logger.debug("pluginId or imageFilePath is null - " + pluginId + imageFilePath);

		return imageDescriptor;
	}
	
	private static String getLogFolderPath()
	{
		String logFolderPath = Platform.getConfigurationLocation().getURL().getPath()
				+ "org.grits.toolbox" + File.separator + "log";

		if(isValidFolder(logFolderPath))
		{
			return logFolderPath;
		}

		// try another folder : "${user.home}/.grits-toolbox/log/"
		String homeDirectory = System.getProperty("user.home");
		if (homeDirectory != null && homeDirectory.trim().length() > 0)
		{
			logFolderPath = homeDirectory + File.separator
					+ ".grits-toolbox" + File.separator + "log";

			if(isValidFolder(logFolderPath))
				return logFolderPath;
		}

		return null;
	}

	private static boolean isValidFolder(String logFolderPath)
	{
		try
		{
			File logFolder = new File(logFolderPath);
			if(logFolder.exists()) // if folder exists check write permissions
			{
				if(Files.isExecutable(logFolder.toPath()) && Files.isWritable(logFolder.toPath()))
				{
					return true;
				}
			}
			else if(logFolder.mkdirs()) // otherwise try creating it
			{
				return true;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// log folder is not writable or could not be created
		return false;
	}
}
