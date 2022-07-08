/**
 * 
 */
package org.grits.toolbox.core.utilShare.sort;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 *
 */
public class TableColumnComparatorListener implements SelectionListener 
{
    private TableViewer tableViewer = null;
    private TableViewerComparator tableViewerComparator;

    public TableColumnComparatorListener(TableViewer tableViewer)
    {
        this.tableViewer = tableViewer;
        this.tableViewerComparator = 
                (TableViewerComparator) tableViewer.getComparator();
    }

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
        tableViewerComparator.setColumn(column);
        tableViewerComparator.setAscending(!ascending);
        tableViewer.refresh();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
        
    }
}
