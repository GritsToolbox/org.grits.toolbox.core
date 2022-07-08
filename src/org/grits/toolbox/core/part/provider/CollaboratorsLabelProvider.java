/**
 * 
 */
package org.grits.toolbox.core.part.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.preference.project.CollaboratorTableColumn;

/**
 * 
 *
 */
public class CollaboratorsLabelProvider implements ITableLabelProvider
{
    private TableViewer tableViewer = null;

    public CollaboratorsLabelProvider(TableViewer tableViewer)
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
        if(element instanceof ProjectCollaborator)
        {
            return CollaboratorTableColumn.getColumnValue((ProjectCollaborator) element, 
                    tableViewer.getTable().getColumn(columnIndex).getText());
        }
        return null;
    }

}
