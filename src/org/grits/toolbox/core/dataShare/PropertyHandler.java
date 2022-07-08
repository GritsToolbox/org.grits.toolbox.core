package org.grits.toolbox.core.dataShare;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.core.service.IGritsUIService;

/**
 * This class provides the access to extension points of menus or sub-menus or etc within this package.
 * @author Ki Tae Myoung
 *
 */
public class PropertyHandler
{
	public static final String GRITS_CHARACTER_ENCODING = "UTF-8";
	public static final int LABEL_TEXT_LIMIT = 80;
	public static final int URI_TEXT_LIMIT = 5000;
	public static String descriptionLength = "10000";

	@Inject static IGritsUIService gritsUIService;

	/**
	 * Workspace location
	 */
	protected static String ROOT_ADD = "";

	/**
	 * Default perspective Id
	 */
	protected static String PERSPECTIVE_ID = IGritsConstants.ID_DEFAULT_PERSPECTIVE;

	/**
	 * simianTool location
	 */
	protected static String SIMIAN_CONFIG_LOCATION = "";

	protected static String SIMIAN_CONFIG_LOCATION_INSTALL = "";

	protected static ImageShare _instance = new ImageShare();

	protected static DataModelHandler dataModel = DataModelHandler.instance();


	public enum PropertyLength
	{
		//max length of a description field is 10000 
		DESCRIPTION (10000);

		private final Integer length;

		PropertyLength(int length)
		{
			this.length = length;
		}

		private Integer length()
		{
			return length;
		}
	}

	public static ImageShare getIconRegistry()
	{
		return _instance;
	}

	public static String getVariable(String name)
	{
		if (name.equals("workspace_location"))
		{
			return PropertyHandler.ROOT_ADD;
		}
		else if (name.equals("configuration_location"))
		{
			return PropertyHandler.SIMIAN_CONFIG_LOCATION;
		}
		else if (name.equals("configuration_location_install"))
		{
			return PropertyHandler.SIMIAN_CONFIG_LOCATION_INSTALL;
		}
		else if (name.equals("perspective"))
		{
			return PropertyHandler.PERSPECTIVE_ID;
		}
		else if (name.equals("preference"))
		{
			return PropertyHandler.ROOT_ADD +".preference.xml";
		}
		else if (name.equals("descriptionLength"))
		{
			return PropertyLength.DESCRIPTION.length().toString();
		}
		return null;
	}

	public static void setVariable(String name, String value)
	{
		if (name.equals("workspace_location"))
		{
			PropertyHandler.ROOT_ADD = value;
		}
		else if (name.equals("configuration_location"))
		{
			PropertyHandler.SIMIAN_CONFIG_LOCATION = value;
		}
		else if (name.equals("configuration_location_install"))
		{
			PropertyHandler.SIMIAN_CONFIG_LOCATION_INSTALL = value;
		}
		else if (name.equals("perspective"))
		{
			PropertyHandler.PERSPECTIVE_ID = value;
		}
	}

	public static Shell getModalDialog(Shell parent)
	{
		return new Shell(parent, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.TITLE & (~SWT.RESIZE) & (~SWT.MAX) & (~SWT.MIN));
	}

	public static DataModelHandler getDataModel()
	{
		return PropertyHandler.dataModel;
	}

	/**
	 * @param perspectiveId
	 */
	public static void changePerspective(String perspectiveId)
	{
		//ChangePerspective
		//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		//		IPerspectiveRegistry registry = window.getWorkbench().getPerspectiveRegistry();
		//		IWorkbenchPage page = window.getActivePage();
		//		page.setPerspective(registry.findPerspectiveWithId(perspectiveId));
		//page.resetPerspective();   // we don't want to reset the layout, so removing this for now

		gritsUIService.selectPerspective(perspectiveId);
	}
}
