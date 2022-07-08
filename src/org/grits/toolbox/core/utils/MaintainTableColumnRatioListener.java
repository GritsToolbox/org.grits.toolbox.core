/**
 * 
 */
package org.grits.toolbox.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * 
 *
 */
public class MaintainTableColumnRatioListener implements ControlListener
{
    private Logger logger = Logger.getLogger(MaintainTableColumnRatioListener.class);

    private int minWidth = 0;
    private final int TABLE_EXTRA_PADDING = 5;
    private static final int COLUMN_FALL_BACK_WIDTH = 10;
    private List<Integer> ratios = null;
    private int ratioDenominator = 0;

    public MaintainTableColumnRatioListener(int minWidth, int ... relativeWidths)
    {
        this.minWidth = minWidth;
        this.ratios = new ArrayList<Integer>();
        int i = 0;
        for(int relativeWidth : relativeWidths)
        {
            relativeWidth = relativeWidth < 1 ? 1 : relativeWidth; 
            this.ratios.add(i++, relativeWidth);
            ratioDenominator += relativeWidth;
        }
    }

    @Override
    public void controlMoved(ControlEvent e)
    {
        
    }

    @Override
    public void controlResized(ControlEvent e)
    {
        logger.debug("- START : Resizing columns.");
        try
        {
            Table table = (Table) e.getSource();
            if(table.getColumns().length == ratios.size())
            {
                int totalWidth = Math.max(table.getSize().x - TABLE_EXTRA_PADDING, minWidth);
                totalWidth -= 20;
                int index = 0;
                TableColumn[] columns = table.getColumns();
                int width = 0;
                int widthTillLastColumn = 0;
                while(index < table.getColumnCount()-1)
                {
                    width = (int) ((totalWidth / ratioDenominator) * ratios.get(index));
                    columns[index].setWidth(width);
                    widthTillLastColumn += width;
                    index++;
                }
                int remainingWidth = totalWidth - widthTillLastColumn;// - TABLE_EXTRA_PADDING;

                if(remainingWidth >= 0)
                {
                    columns[index].setWidth(remainingWidth);
                }
                else
                {
                    columns[index].setWidth(COLUMN_FALL_BACK_WIDTH);
                    logger.error("Negative width values");
                }
            }
        } catch (Exception ex)
        {
            logger.error(ex);
        }
        logger.debug("- END   : Resizing columns.");
    }

}
