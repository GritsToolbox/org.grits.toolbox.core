/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * 
 *
 */
public class StringLabelProvider implements ITableLabelProvider
{
	@Override
	public void removeListener(ILabelProviderListener listener)
	{

	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void dispose()
	{

	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{

	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(columnIndex > 0 
				&& element instanceof String)
		{
			return (String) element;
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}
}