/**
 * 
 */
package org.grits.toolbox.core.utilShare.sort;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;

/**
 * 
 *
 */
public class TableViewerComparator  extends ViewerComparator
{
	private int column;
	private boolean ascending;

	/**
	 * meant for tables with a label provider
	 */
	public int compare(Viewer viewer, Object obj1, Object obj2)
	{
		int rc = 0;
		if(!((obj1 instanceof String) || (obj2 instanceof String)))
		{
			if(viewer instanceof CheckboxTableViewer && column == 0)
			{
				CheckboxTableViewer checkboxTableViewer = (CheckboxTableViewer) viewer;
				int value1 = checkboxTableViewer.getChecked(obj1) ? 1 : -1;
				int value2 = checkboxTableViewer.getChecked(obj2) ? 1 : -1;
				rc = value1 - value2;
			}
			else if(viewer instanceof TableViewer && column >= 0)
			{
				TableViewer tableViewer = (TableViewer) viewer;
				ITableLabelProvider labelProvider = 
						(ITableLabelProvider) tableViewer.getLabelProvider();
				if(labelProvider.getColumnText(obj1, column) instanceof String 
						&& labelProvider.getColumnText(obj2, column) instanceof String)
				{
					String text1 = labelProvider.getColumnText(obj1, column) == null 
							? "" : labelProvider.getColumnText(obj1, column);
					String text2 = labelProvider.getColumnText(obj2, column) == null 
							? "" : labelProvider.getColumnText(obj2, column);
					rc = this.getComparisionForIntOrChar(text1, text2);
				}
				else
				{
					String text1 = labelProvider.getColumnText(obj1, column) == null 
							? "" : labelProvider.getColumnText(obj1, column).toString();
					String text2 = labelProvider.getColumnText(obj2, column) == null 
							? "" : labelProvider.getColumnText(obj2, column).toString();
					rc = this.getComparisionForIntOrChar(text1, text2);
				}
			}
			rc = ascending ? rc : -rc;
		}
		return rc;
	}

	private int getComparisionForIntOrChar(String text1, String text2)
	{
		try
		{
			int intValue1 = Integer.parseInt(text1);
			int intValue2 = Integer.parseInt(text2);
			return intValue1 - intValue2;
		} catch (NumberFormatException ex)
		{
			return text1.compareToIgnoreCase(text2);
		}
	}

	public void setColumn(int column)
	{
		this.column = column;
	}

	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	public boolean getAscending()
	{
		return this.ascending;
	}
}
