/**
 * 
 */
package org.grits.toolbox.core.preference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 *
 */
public class GritsPreferenceNode extends PreferenceNode
{
	private static final Logger logger = Logger.getLogger(GritsPreferenceNode.class);
	private String pluginId = null;
	private String className = null;

	public GritsPreferenceNode(String pluginId,
			String id, String pageName, ImageDescriptor imageDescriptor, String className)
	{
		super(id, pageName, imageDescriptor, className);
		this.pluginId = pluginId;
		this.className = className;
	}

	@Override
	public void createPage()
	{
		setPage(loadPageFromPlugin());
	}

	private PreferencePage loadPageFromPlugin()
	{
		logger.info("Creating class : " + pluginId + "/" + className);
		try
		{
			Class<?> clazz = Platform.getBundle(pluginId).loadClass(className);
			PreferencePage page = null;
			if(PreferencePage.class.isAssignableFrom(clazz))
			{
				page  = (PreferencePage) clazz.newInstance();
				page.setTitle(getLabelText());
				return page;
			}
			else
			{
				String errorMessage = "This class does not extend PreferencePage: "
						+ pluginId + "/" + className;
				logger.fatal(errorMessage);
				throw new InstantiationException(errorMessage);
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			logger.fatal("Error loading page \"" + className + "\" from plugin : "
					+ pluginId + "\n" + e.getMessage(), e);
		}
		return null;
	}
}
