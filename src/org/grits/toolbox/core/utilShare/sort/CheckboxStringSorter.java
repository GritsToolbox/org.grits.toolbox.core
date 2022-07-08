/**
 * 
 */
package org.grits.toolbox.core.utilShare.sort;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;

/**
 * 
 *
 */
public class CheckboxStringSorter implements SelectionListener 
{
	
	private CheckboxTableViewer checkboxTableViewer;

	public CheckboxStringSorter(CheckboxTableViewer checkboxTableViewer)
	{
		this.checkboxTableViewer = checkboxTableViewer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void widgetSelected(SelectionEvent e)
	{
        TableColumn tableColumn = (TableColumn) e.getSource();
        Table table = tableColumn.getParent();
        boolean ascending = table.getSortDirection() == SWT.UP;
        int newDirection = ascending ? SWT.DOWN : SWT.UP;

        int column = table.indexOf(tableColumn);
        table.setSortDirection(newDirection);
        table.setSortColumn(table.getColumn(column));

        List<String> tableInput = (List<String>) checkboxTableViewer.getInput();
        Collections.sort(tableInput);
        if(!ascending)
        {
        	Collections.reverse(tableInput);
        }
        Object[] checkedElements = checkboxTableViewer.getCheckedElements();
        checkboxTableViewer.setInput(tableInput);
        checkboxTableViewer.setCheckedElements(checkedElements);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}