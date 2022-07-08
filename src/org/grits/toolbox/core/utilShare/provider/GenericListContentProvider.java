/**
 * 
 */
package org.grits.toolbox.core.utilShare.provider;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 *
 */
public class GenericListContentProvider implements IStructuredContentProvider
{

    @Override
    public void dispose()
    {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {

    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        if(inputElement instanceof List)
        {
            List<?> inputList = ((List<?>) inputElement);
            Object[] array = new Object[inputList.size()];
            int i = 0;
            for(Object input : inputList)
            {
                array[i++] = input;
            }
            return array;
        }
        return null;
    }

}
