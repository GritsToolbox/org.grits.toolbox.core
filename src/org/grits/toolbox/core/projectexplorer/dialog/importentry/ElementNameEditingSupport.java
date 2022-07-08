/**
 * 
 */
package org.grits.toolbox.core.projectexplorer.dialog.importentry;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.grits.toolbox.core.utilShare.validator.EntryNameValidator;
import org.jdom.Element;

/**
 * 
 *
 */
public class ElementNameEditingSupport extends EditingSupport
{
	TextCellEditor textCellEditor = null;

	public ElementNameEditingSupport(TableViewer viewer)
	{
		super(viewer);
		textCellEditor = new TextCellEditor(viewer.getTable());
		textCellEditor.setValidator(new ICellEditorValidator()
		{
			Set<String> emptySet = new HashSet<String>();
			@Override
			public String isValid(Object value)
			{
				return value instanceof String ? 
					EntryNameValidator.validateProjectName(emptySet, ((String) value).trim())
					: "only string type is allowed";
			}
		});
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if(element instanceof Element)
		{
			((Element) element).setAttribute("name", ((String) value).trim());
			((ImportEntryLabelProvider) getViewer().getLabelProvider()).resetAllNames();
			getViewer().refresh();
		}
	}

	@Override
	protected Object getValue(Object element)
	{
		return element instanceof Element ? 
				((Element) element).getAttributeValue("name") : null;
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return element instanceof Element ? textCellEditor : null;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return element instanceof Element;
	}
}