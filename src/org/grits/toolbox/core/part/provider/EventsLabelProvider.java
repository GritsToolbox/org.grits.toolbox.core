/**
 * 
 */
package org.grits.toolbox.core.part.provider;

import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;

/**
 * 
 *
 */
public class EventsLabelProvider implements ITableLabelProvider
{
    public static final String DATE_FORMAT = "MM/dd/yyyy";//" hh:mm:ss"
    private SimpleDateFormat simpleDateFormat = null;

    @Override
    public void addListener(ILabelProviderListener listener)
    {
        simpleDateFormat  = new SimpleDateFormat(DATE_FORMAT);
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
        String value = null;
        if(element instanceof ProjectEvent)
        {
            ProjectEvent event = (ProjectEvent) element;
            switch(columnIndex)
            {
            case 0 :
                value = event.getProjectAction().getAction();
                break;
            case 1 :
                if(event.getEventDate() != null) 
                {
                    value = simpleDateFormat.format(event.getEventDate());
                }
                break;
            case 2 :
                value = event.getDescription();
                break;
            }
        }
        return value;
    }

}
