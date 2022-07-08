package org.grits.toolbox.core.datamodel.property;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.io.ReportsPropertyWriter;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;

public class ReportsProperty extends Property
{
	public static final String TYPE = "org.grits.toolbox.property.reports";
	private int viewerRank = 11000;
	protected static PropertyWriter writer = new ReportsPropertyWriter();
	private static final String folderName = "reports";
	protected static ImageDescriptor imageDescriptor = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/reports.png");

	public ReportsProperty()
	{
		super();
	}

	@Override
	public String getType()
	{
		return ReportsProperty.TYPE;
	}

	@Override
	public PropertyWriter getWriter()
	{
		return ReportsProperty.writer;
	}

	@Override
	public ImageDescriptor getImage()
	{
		return ReportsProperty.imageDescriptor;
	}

	@Override
	public void delete(Entry entry) throws IOException
	{
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectName = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE).getDisplayName();
		String reportsFolder = workspaceLocation+projectName+"/"+ReportsProperty.getFolder();

		//delete reports folder
		DeleteUtils.delete(new File(reportsFolder));
	}

	public static String getFolder()
	{
		return folderName;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getParentProperty()
	{
		return null;
	}
	@Override
	public boolean isRenamable()
	{
		return false;
	}
	/**
	 * @return the viewerRank
	 */
	public int getViewerRank()
	{
		return viewerRank;
	}

	/**
	 * @param viewerRank the viewerRank to set
	 */
	public void setViewerRank(int viewerRank)
	{
		this.viewerRank = viewerRank;
	}
	
	@Override
	public boolean directCopyEnabled() {
		return false;
	}
}
