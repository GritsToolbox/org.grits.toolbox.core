/**
 * 
 */
package org.grits.toolbox.core.utilShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.grits.toolbox.core.Activator;

/**
 * 
 *
 */
public class CheckboxTableViewer extends TableViewer implements ICheckable 
{
	// Alexey Egorov ​http://www.burlesck.livejournal.com Freeware
	// ​http://findicons.com/icon/232348/checkbox_yes?id=407397
	public static final Image CHECKBOX_TICKED = 
			Activator.imageDescriptorFromPlugin(
					Activator.PLUGIN_ID, "icons" + File.separator + "checked-yes.png").createImage();
	// Alexey Egorov ​http://www.burlesck.livejournal.com Freeware
	// ​http://findicons.com/icon/232314/checkbox_no?id=232367
	public static final Image CHECKBOX_EMPTY = 
			Activator.imageDescriptorFromPlugin(
					Activator.PLUGIN_ID, "icons" + File.separator + "checked-no.png").createImage();
	// Yusuke Kamiyamane ​http://www.p.yusukekamiyamane.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/118712/ui_radio_button_uncheck?id=123684
	public static final Image RADIO_SELECTED = 
			Activator.imageDescriptorFromPlugin(
					Activator.PLUGIN_ID, "icons" + File.separator + "radio-selected.png").createImage();
	// Yusuke Kamiyamane ​http://www.p.yusukekamiyamane.com Creative Commons Attribution (by)
	// ​http://findicons.com/icon/118309/ui_radio_button?id=122817
	public static final Image RADIO_UNSELECTED = 
			Activator.imageDescriptorFromPlugin(
					Activator.PLUGIN_ID, "icons" + File.separator + "radio-unselected.png").createImage();
	private Image checkedIcon = CHECKBOX_TICKED;
	private Image uncheckedIcon = CHECKBOX_EMPTY;

	private ICheckStateListener checkListener = null;
	private ICheckStateProvider checkStateProvider = null;

	private TableViewerColumn columnViewer0 = null;
	private CheckboxCellEditor checkboxCellEditor = null;
	private String title = "Select";
	private int columnWidth = 70;
	private boolean editable = true;
	private boolean singleSelection = false;
	private HashMap<Object, Boolean> checkedMap = new HashMap<Object, Boolean>();

	public CheckboxTableViewer(Table table)
	{
		super(table);
		setFirstColumn();
	}

	public CheckboxTableViewer(Table table, boolean singleSelection)
	{
		super(table);
		this.singleSelection  = singleSelection;
		setFirstColumn();
	}

	/**
	 * creates a Checkbox Table Viewer with checkboxes (multi-selection)
	 * @param table for which the viewer is created
	 * @param title title of the checkbox column
	 * @param default selection scheme is multi-selection
	 */
	public CheckboxTableViewer(Table table, String title)
	{
		this(table);
		this.setCheckboxTitle(title);
	}

	public CheckboxTableViewer(Table table, String title, boolean singleSelection)
	{
		this(table, singleSelection);
		this.setCheckboxTitle(title);
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setCheckboxTitle(String title)
	{
		this.title = title == null 
				? this.title : title;
		if(columnViewer0 != null)
		{
			columnViewer0.getColumn().setText(this.title);
		}
	}

	public String getCheckboxTitle()
	{
		return this.title;
	}

	public void setFirstColumnWidth(int columnWidth)
	{
		this.columnWidth = columnWidth > 0 
				? columnWidth : this.columnWidth;
		if(columnViewer0 != null)
		{
			columnViewer0.getColumn().setWidth(this.columnWidth);
		}
	}

	public int getFirstColumnWidth()
	{
		return columnWidth;
	}

	private void setFirstColumn()
	{
		columnViewer0  = new TableViewerColumn(this, SWT.FILL, 0);
		columnViewer0.getColumn().setText(title);
		columnViewer0.getColumn().setWidth(columnWidth);
		if(singleSelection)
		{
			checkedIcon = RADIO_SELECTED;
			uncheckedIcon = RADIO_UNSELECTED;
		}
		else
		{
			checkedIcon = CHECKBOX_TICKED;
			uncheckedIcon = CHECKBOX_EMPTY;
		}

		columnViewer0.setEditingSupport(new EditingSupport(this)
		{
			@Override
			protected void setValue(Object element, Object value)
			{
				boolean checked = (boolean) value;
				setChecked(element, checked);
				if(checkListener != null)
				{
					checkListener.checkStateChanged(
							new CheckStateChangedEvent(
									CheckboxTableViewer.this, element, checked));
				}
			}

			@Override
			protected Object getValue(Object element)
			{
				if (getCellEditor(element) != null) 
				{
					return getChecked(element);
				}
				return false;
			}

			@Override
			protected CellEditor getCellEditor(Object element)
			{
				if(checkboxCellEditor == null)
				{
					checkboxCellEditor = 
							new CheckboxCellEditor(getTable());
				}
				return checkboxCellEditor;
			}

			@Override
			protected boolean canEdit(Object element)
			{
				if (getCellEditor(element) != null) 
				{
					return singleSelection ? 
							editable && !getChecked(element) : editable && true;
				}
				return false;
			}
		});
	}

	@Override
	public void refresh()
	{
		super.refresh();
		TableItem[] items = getTable().getItems();
		HashMap<Object, Boolean> newCheckedMap 
		= new HashMap<Object, Boolean>();
		for (int i = 0; i < items.length; i++)
		{
			TableItem item = items[i];
			Object data = item.getData();
			if (data != null)
			{
				if(checkStateProvider == null)
				{
					if(checkedMap.containsKey(item))
					{
						boolean checked = checkedMap.get(item);
						newCheckedMap.put(item, checked);
					}
					else
					{
						newCheckedMap.put(item, false);
					}
				}
				else
				{
					newCheckedMap.put(item, checkStateProvider.isChecked(data));
				}
				getViewerRowFromItem(item).setImage(0, getImage(newCheckedMap.get(item)));
			}
		}
		checkedMap = newCheckedMap;
	}

	public Object[] getCheckedElements()
	{
		TableItem[] items = getTable().getItems();
		if(items.length !=  checkedMap.size())
		{
			refresh();
		}
		ArrayList<Object> checkedElements = new ArrayList<Object>(items.length);
		for (int i = 0; i < items.length; i++)
		{
			TableItem item = items[i];
			Object data = item.getData();
			if (data != null)
			{
				if(checkStateProvider == null)
				{
					if(checkedMap.containsKey(item) && checkedMap.get(item))
					{
						checkedElements.add(data);
					}
				}
				else if(checkStateProvider.isChecked(data))
				{
					checkedElements.add(data);
				}

			}
		}
		return checkedElements.toArray();
	}

	public void setCheckedElements(Object[] elements)
	{
		assertElementsNotNull(elements);
		List<Object> checkedList = Arrays.asList(elements);
		TableItem[] items = getTable().getItems();
		for (int i = 0; i < items.length; ++i)
		{
			TableItem item = items[i];
			Object element = item.getData();
			if (element != null) 
			{
				boolean checked = checkedList.contains(element);
				checkedMap.put(item, checked);
				getViewerRowFromItem(item).setImage(0, getImage(checked));
			}
		}
	}

	public boolean getChecked(Object element)
	{
		if(checkStateProvider == null)
		{
			Widget widget = findItem(element);
			if (widget instanceof TableItem)
			{
				TableItem item = (TableItem) widget;
				if(checkedMap.containsKey(item))
				{
					return checkedMap.get(item);
				}
				else
				{
					checkedMap.put(item, false);
				}
			}
			return false;
		}
		else
		{
			return checkStateProvider.isChecked(element);
		}
	}

	public boolean setChecked(Object element, boolean state) 
	{
		if(getTable().getItemCount() !=  checkedMap.size())
		{
			refresh();
		}
		Assert.isNotNull(element);
		Widget widget = findItem(element);
		if (widget instanceof TableItem)
		{
			TableItem item = (TableItem) widget;
			checkedMap.put(item, state);
			getViewerRowFromItem(item).setImage(0, getImage(state));
			if(singleSelection && state)
			{
				for(Object eachElement : checkedMap.keySet())
				{
					if(checkedMap.get(eachElement) 
							&& !eachElement.equals(item))
					{
						TableItem thisItem = (TableItem) eachElement;
						checkedMap.put(thisItem, false);
						getViewerRowFromItem(thisItem).setImage(0, getImage(false));
					}
				}
			}
			return true;
		}
		return false;
	}

	private Image getImage(boolean state)
	{
		return state ? checkedIcon : uncheckedIcon;
	}

	public void setCheckStateListener(ICheckStateListener iCheckStateListener)
	{
		this.checkListener = iCheckStateListener;
	}

	@Override
	public void addCheckStateListener(ICheckStateListener listener)
	{
		this.checkListener = listener;
	}

	@Override
	public void removeCheckStateListener(ICheckStateListener listener)
	{
		if(this.checkListener.equals(listener))
		{
			this.checkListener = null;
		}
	}

	public void setCheckStateProvider(ICheckStateProvider iCheckStateProvider)
	{
		this.checkStateProvider = iCheckStateProvider;
	}

}
