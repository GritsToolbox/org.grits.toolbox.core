package org.grits.toolbox.core.projectexplorer.part;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.core.datamodel.Entry;

/**
 * used to provide name and image for an entry
 *
 */
public class ProjectExplorerLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object obj)
	{
		return obj instanceof Entry 
				? ((Entry) obj).getDisplayName() : obj.toString();
	}

	@Override
	public Image getImage(Object obj)
	{
		return obj instanceof Entry 
				? ((Entry) obj).getProperty().getImage().createImage() : null;
	}
}
