/**
 * 
 */
package org.grits.toolbox.core.dataShare;

/**
 * 
 *
 */
public interface IGritsConstants
{
	/**
	 * The variable that stores the location of install configuration folder
	 */
	public static final String CONFIG_LOC_INSTALL = "configuration_location_install";

	/**
	 * The variable that stores the configuration location,
	 * generally the <b>".grits-toolbox"</b> folder in the home directory
	 */
	public static final String CONFIG_LOCATION = "configuration_location";

	/**
	 * The variable that stores the workspace location for the current workspace
	 */
	public static final String WORKSPACE_LOCATION = "workspace_location";

	/**
	 * The id of default perspective of GRITS
	 */
	public static final String ID_DEFAULT_PERSPECTIVE =
			"org.grits.toolbox.core.perspective.projectexplorer";

	/**
	 * The id of intro perspective (the perspective containing the welcome page)
	 */
	public static final String ID_INTRO_PERSPECTIVE =
			"org.grits.toolbox.core.perspective.gritsintro";

	/**
	 * The id of binding context entry, used to identify entry part descriptors
	 */
	public static final String ID_BINDING_CONTEXT_ENTRY =
			"org.grits.toolbox.core.bindingcontext.entry";

	/**
	 * The id of default perspective stack that contains all the perspectives
	 */
	public static final String ID_DEFAULT_PERSPECTIVE_STACK =
			"org.grits.toolbox.core.perspectivestack.0";
}
