package org.grits.toolbox.core.datamodel.util;

import org.grits.toolbox.core.datamodel.Entry;

public class DataModelSearch
{
	/**
	 * Find parent of this type
	 * @param entry current entry
	 * @param type parent type
	 * @return
	 */
	public static Entry findParentByType(Entry entry, String type)
	{
		if(entry.getProperty().getType().equals(type))
		{
			return entry;
		}
		Entry parent = entry.getParent();
		if(parent == null)
		{
			return null;
		}
		if(parent.getProperty().getType() == null)
		{
			return null;
		}
		if(parent.getProperty().getType().equals(type))
		{
			return parent;
		}
		else
		{
			return DataModelSearch.findParentByType(parent, type);
		}
	}
}
