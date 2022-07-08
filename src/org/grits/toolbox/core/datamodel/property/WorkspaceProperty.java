package org.grits.toolbox.core.datamodel.property;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;


public class WorkspaceProperty extends Property
{
	public static final String TYPE = "org.grits.toolbox.property.workspace";
	private String location = null;

	public WorkspaceProperty()
	{
		super();
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	@Override
	public PropertyWriter getWriter()
	{
		return null;
	}

	@Override
	public ImageDescriptor getImage()
	{
		return null;
	}

	@Override
	public void delete(Entry entry)
	{
		//do not need to do anything
	}

	@Override
	public Object clone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public Property getParentProperty()
	{
		// TODO Auto-generated method stub
		return null;
	}
}