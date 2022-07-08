/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.dialog.importentry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jdom.Element;

/**
 * 
 *
 */
public class ImportEntryLabelProvider implements ITableLabelProvider, ITableColorProvider
{
	private Set<String> existingNames = null;
	private Set<String> allNames = null;
	private List<Element> duplicateElements = null;
	private Color HIGHLIGHT_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	public ImportEntryLabelProvider(Set<String> existingNames)
	{
		this.existingNames = existingNames;
		resetAllNames();
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{

	}

	@Override
	public void dispose()
	{

	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;

	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof Element && columnIndex == 1)
		{
			return ((Element) element).getAttributeValue("name");
		}
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		if(element instanceof Element && columnIndex == 1)
		{
			if(allNames.contains(
					((Element) element).getAttributeValue("name")))
			{
				duplicateElements.add(((Element) element));
				return HIGHLIGHT_COLOR;
			}
			// adds this name at the end of displaying row
			allNames.add(((Element) element).getAttributeValue("name"));
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		return null;
	}

	public List<Element> getDuplicateElements()
	{
		return duplicateElements;
	}

	public void resetAllNames()
	{
		this.allNames = new HashSet<String>(existingNames);
		this.duplicateElements = new ArrayList<Element>();
	}
}
