package org.grits.toolbox.core.datamodel.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;


/**
 * Generally used in property classes
 * @author kitaeUser
 *
 */
public abstract class Property 
{
	public static final String CURRENT_VERSION = "1.0";
	private String version = null;
	private Integer identifier = null;
	private boolean removed = false;
	private boolean renamable = true;
	private int viewerRank = 10000;
	protected List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();

	public abstract String getType();
	public abstract PropertyWriter getWriter();
	public abstract ImageDescriptor getImage();
	public abstract Property getParentProperty();

	public abstract void delete(Entry entry) throws IOException;
	public abstract Object clone();
	

	/**
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
	
	public boolean exists()
	{
		return removed == false;
	}

	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

	/**
	 * it says if this property type is renamable in the project explorer view
	 * @return true if renamable else false
	 */
	public boolean isRenamable()
	{
		return renamable;
	}

	/**
	 * gets the viewerRank of the entry property
	 * @return rank (the higher the value the lower it is 
	 * displayed in the project explorer)
	 */
	public int getViewerRank()
	{
		return viewerRank;
	}

	/**
	 * sets the viewerRank of the entry property
	 * @param viewerRank (the higher the value the lower it is 
	 * displayed in the project explorer)
	 */
	public void setViewerRank(int viewerRank)
	{
		this.viewerRank = viewerRank;
	}

	/**
	 * @return the dataFiles
	 */
	public List<PropertyDataFile> getDataFiles()
	{
		return dataFiles;
	}

	/**
	 * @param dataFiles the dataFiles to set
	 */
	public void setDataFiles(List<PropertyDataFile> dataFiles)
	{
		this.dataFiles = dataFiles;
	}

	public void makeACopy(Entry currentEntry, Entry destinationEntry)
			throws NotImplementedException, IOException
	{
		throw new NotImplementedException("This property \"" 
				+ currentEntry.getProperty().getType() 
				+ "\" has not implemented the copy functionality.");
	}
	
	/**
	 * determines whether the entry can be copied to another parent entry directly
	 * if the entry supports copying only when it is copied together with its parent, do not return true
	 * 
	 * @return true if direct copy is supported, false otherwise
	 */
	public boolean directCopyEnabled() {
		return true;
	}
}
