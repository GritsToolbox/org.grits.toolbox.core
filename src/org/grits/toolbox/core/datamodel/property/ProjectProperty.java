package org.grits.toolbox.core.datamodel.property;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.io.project.ProjectPropertyWriter;
import org.grits.toolbox.core.utilShare.DeleteUtils;

/**
 * property of project folder in the tree
 * @author kitaeUser
 *
 */
public class ProjectProperty extends Property
{
	public static final String CURRENT_VERSION = "1.0";
    public static final String TYPE = "org.grits.toolbox.property.project";
    public static final String PROJECT_DETAILS_XML = "project_details.xml";
    public static final String DETAILS_TYPE = "details";
    protected static PropertyWriter writer = new ProjectPropertyWriter();

    // Creative Freedom Ltd ​http://www.creativefreedom.co.uk Creative Commons Attribution BY-ND 3.0
    // ​https://www.creativefreedom.co.uk/free-icons/free-windows-7-icons/
    protected static ImageDescriptor imageDescriptorOpen = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/project.png");

    // Creative Freedom Ltd ​http://www.creativefreedom.co.uk Creative Commons Attribution BY-ND 3.0
    // ​https://www.creativefreedom.co.uk/free-icons/free-windows-7-icons/
    protected static ImageDescriptor imageDescriptorClose = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/folder_closed.png");

    protected boolean isOpen = true;
    public ProjectProperty()
    {
        super();
    }
	public PropertyDataFile getDetailsFile()
	{
		PropertyDataFile detailsFile = null;
		for(PropertyDataFile dataFile : dataFiles)
		{
			if(DETAILS_TYPE.equals(dataFile.getType()))
			{
				detailsFile = dataFile;
				break;
			}
		}
		return detailsFile;
	}
    @Override
    public String getType() {
        return ProjectProperty.TYPE;
    }
    @Override
    public PropertyWriter getWriter() {
        return ProjectProperty.writer;
    }
    public boolean getOpen() {
        return isOpen;
    }
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    public ImageDescriptor getImage() {
        if(getOpen())
        {
            return ProjectProperty.imageDescriptorOpen;
        }
        return ProjectProperty.imageDescriptorClose;
    }

    @Override
    public void delete(Entry entry) throws IOException {
        //need to delete project folder and inside files.
        String workspaceLocation = PropertyHandler.getVariable("workspace_location");
        DeleteUtils.delete(new File(workspaceLocation+entry.getDisplayName()+"/.project.xml"));
        DeleteUtils.delete(new File(workspaceLocation+entry.getDisplayName()));
    }

    @Override
    public Object clone() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public Property getParentProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isOpen()
	{
		return isOpen;
	}
	
	@Override
	public boolean directCopyEnabled() {
		return false;
	}
}
