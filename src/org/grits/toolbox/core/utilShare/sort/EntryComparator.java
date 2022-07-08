package org.grits.toolbox.core.utilShare.sort;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.grits.toolbox.core.datamodel.Entry;

public class EntryComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object object1, Object object2)
	{
		int comparision = 0;
		if(object1 != null && object1 instanceof Entry 
				&& object2 != null && object2 instanceof Entry)
		{
			Entry entry1 = (Entry) object1;
			Entry entry2 = (Entry) object2;
			if(entry1.getProperty() == null)
			{
				comparision = entry2.getProperty() == null ? 0 : 1;
			}
			else if(entry2.getProperty() == null)
			{
				comparision = -1;
			}
			else
			{
				comparision = entry1.getProperty().getViewerRank() 
						- entry2.getProperty().getViewerRank();
			}
			if(comparision == 0)
				comparision = compareStringValue(entry1.getDisplayName(), entry2.getDisplayName());
		}
		return comparision;
	}

	private int compareStringValue(String value1, String value2)
	{
		int comparision = 0;
		if(value1 == null)
		{
			comparision = value2 == null ? 0 : 1;
		}
		else if(value2 == null)
		{
			comparision = -1;
		}
		else
		{
			comparision = value1.compareToIgnoreCase(value2);
		}
		return comparision;
	}
}
