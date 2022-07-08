/**
 * 
 */
package org.grits.toolbox.core.part.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.preference.project.TasklistTableColumn;

/**
 * 
 *
 */
public class TasklistsLabelProvider implements ITableLabelProvider
{
    public static final String DATE_FORMAT = "MM/dd/yyyy";//" hh:mm:ss"
    private TableViewer tableViewer = null;

    public TasklistsLabelProvider(TableViewer tableViewer)
    {
        this.tableViewer  = tableViewer;
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
        if(element instanceof ProjectTasklist)
        {
            return TasklistTableColumn.getColumnValue((ProjectTasklist) element, 
                    tableViewer.getTable().getColumn(columnIndex).getText());
        }
        return null;
    }

}
