/**
 * 
 */
package org.grits.toolbox.core.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.preference.PreferenceManager;

/**
 * 
 *
 */
@Singleton
public class PreferenceManagerLoader
{
	private static final Logger logger = Logger.getLogger(PreferenceManagerLoader.class);

	public static final String GRITS_PREFERENCE_MANAGER = "Grits_Preference_Manager";

	private static final String PREFERENCE_EXT_POINT_ID = "org.grits.toolbox.core.preferencePages";

	private static final String ELMT_PAGE = "page";
	private static final String ATTR_ID = "id";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_CATEGORY = "category";

	@Inject
	private IExtensionRegistry extensionRegistry;

	private PreferenceManager gritsPreferenceManager = null;

	public PreferenceManager loadPreferenceManager()
	{
		if(gritsPreferenceManager != null)
			return gritsPreferenceManager;

		gritsPreferenceManager = new PreferenceManager();

		String bundleId = null;
		String pageId = null;
		String name = null;
		String className = null;
		GritsPreferenceNode preferenceNode = null;

		Map<String, GritsPreferenceNode> idToPreferenceNodeMap =
				new HashMap<String, GritsPreferenceNode>();
		Map<String, List<GritsPreferenceNode>> categoryChildrenMap =
				new HashMap<String, List<GritsPreferenceNode>>();

		for (IConfigurationElement configElement :
			extensionRegistry.getConfigurationElementsFor(PREFERENCE_EXT_POINT_ID))
		{
			bundleId = configElement.getNamespaceIdentifier();
			if (!ELMT_PAGE.equals(configElement.getName()))
			{
				logger.error("Missing \"page\" element for preference. found: \""
						+ configElement.getName() + "\" in plugin " + bundleId);
				continue;
			}
			pageId = configElement.getAttribute(ATTR_ID);
			name = configElement.getAttribute(ATTR_NAME);
			if (pageId == null || pageId.trim().isEmpty())
			{
				logger.error("Missing mandatory attribute \"id\" for preference"
						+ " in plugin " + bundleId);
				continue;
			}
			if(name == null || name.trim().isEmpty())
			{
				logger.error("Missing mandatory attribute \"name\" for preference"
						+ " in plugin " + bundleId);
				continue;
			}

			className = configElement.getAttribute(ATTR_CLASS);
			if (className != null)
			{
				try
				{
					name = name.trim();
					pageId = pageId.trim();
					logger.info("Adding preference node for : " + pageId);

					// page = createPage(bundleId, className, name);
					// preferenceNode = new PreferenceNode(pageId, page);
					preferenceNode = new GritsPreferenceNode(bundleId, pageId, name, null, className);
					idToPreferenceNodeMap.put(pageId, preferenceNode);

					String category = configElement.getAttribute(ATTR_CATEGORY);
					if (category == null || category.isEmpty())
					{
						gritsPreferenceManager.addToRoot(preferenceNode);
					}
					else
					{
						List<GritsPreferenceNode> children = categoryChildrenMap.get(category);
						if (children == null)
						{
							children = new ArrayList<GritsPreferenceNode>();
							categoryChildrenMap.put(category, children);
						}
						children.add(preferenceNode);
					}
				} catch (Exception e)
				{
					logger.fatal("Unexpected error while adding preference node for : " + pageId);
					logger.fatal(e.getMessage(), e);
				}
			}
		}

		GritsPreferenceNode categoryPreferenceNode = null;
		for (String categoryId : categoryChildrenMap.keySet())
		{
			categoryPreferenceNode = idToPreferenceNodeMap.get(categoryId);
			if(categoryPreferenceNode == null)
			{
				logger.error("No such category found : " + categoryId);
			}
			else
			{
				for(GritsPreferenceNode child : categoryChildrenMap.get(categoryId))
				{
					categoryPreferenceNode.add(child);
				}
			}
		}

		return gritsPreferenceManager;
	}
}
