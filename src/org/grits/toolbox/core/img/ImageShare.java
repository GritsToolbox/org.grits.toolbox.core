package org.grits.toolbox.core.img;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.Activator;

public class ImageShare
{
	public static final ImageDescriptor GRITS_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "icon16-8.png");
	public static final ImageDescriptor DELETE_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "red-cross-icon.png");

	// Lokas Software ​http://www.awicons.com CC Attribution 4.0​
	// http://www.iconarchive.com/show/vista-artistic-icons-by-awicons/add-icon.html
	public static final ImageDescriptor ADD_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "add_icon.png");

	// Alexey Egorov ​http://www.burlesck.livejournal.com Freeware
	// ​http://findicons.com/icon/232348/checkbox_yes?id=407397
	public static final ImageDescriptor CHECKBOX_ICON_YES =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "checked-yes.png");

	// Alexey Egorov ​http://www.burlesck.livejournal.com Freeware
	// ​http://findicons.com/icon/232314/checkbox_no?id=232367
	public static final ImageDescriptor CHECKBOX_ICON_NO =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "checked-no.png");

	// Yusuke Kamiyamane ​http://www.p.yusukekamiyamane.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/118712/ui_radio_button_uncheck?id=123684
	public static final ImageDescriptor RADIO_ICON_SELECTED =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "radio-selected.png");

	// Yusuke Kamiyamane ​http://www.p.yusukekamiyamane.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/118309/ui_radio_button?id=122817
	public static final ImageDescriptor RADIO_ICON_UNSELECTED =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "radio-unselected.png");

	// Umut Pulat ​http://www.12m3.deviantart.com GNU LGPL license
	// ​http://www.iconarchive.com/show/tulliana-2-icons-by-umut-pulat/3floppy-unmount-icon.html
	public static final ImageDescriptor SAVE_ICON = 
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "save-16.png");


	public static final ImageDescriptor SAVE_ICON_DISABLED =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "save-disabled-16.png");

	// Momenticons ​http://www.momentumdesignlab.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/261250/sort_ascend
	public static final ImageDescriptor SORT_ICON_ASCEND =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "sort_ascend.png");

	// Momenticons ​http://www.momentumdesignlab.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/261568/sort_descend
	public static final ImageDescriptor SORT_ICON_DESCEND =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "sort_descend.png");

	// FamFamFam (Mark James) ​http://www.famfamfam.com CC Attribution 4.0
	// ​http://www.iconarchive.com/show/silk-icons-by-famfamfam/application-view-list-icon.html
	public static final ImageDescriptor VIEW_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "view-collab-icon.png");
	
	public static final ImageDescriptor DOWNLOAD_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "download-arrow.png");
	
	// Bdate Kaspar/Franziska Sponsel ​http://rrze-icon-set.berlios.de/index.html Creative Commons Attribution (by)
	// ​http://www.findicons.com/icon/84660/pen?id=84972
	public static final ImageDescriptor EDIT_ICON =
			Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "pencil_icon.png");
	
	public static final ImageDescriptor SHOW_SELECTED_ICON = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "search.png");
}
