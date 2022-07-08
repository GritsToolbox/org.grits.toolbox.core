/**
 * 
 */
package org.grits.toolbox.core.utilShare;

import org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CalendarCellEditor extends CDateTimeCellEditor
{
	public static final String DATE_FORMAT = "MM/dd/yyyy";

	public CalendarCellEditor(Composite composite)
	{
		super(composite, CDT.DROP_DOWN);
	}

	@Override
	protected Control createControl(Composite parent)
	{
		final CDateTime cDateTime = (CDateTime) super.createControl(parent);
		cDateTime.setPattern(DATE_FORMAT);
		return cDateTime;
	}

	@Override
	protected boolean dependsOnExternalFocusListener()
	{
		return false;
	}
}