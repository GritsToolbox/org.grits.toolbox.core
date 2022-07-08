package org.grits.toolbox.core.utilShare;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ComboPopupSelectionListener implements SelectionListener{

	private Composite parent;
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Combo cb = (Combo)e.getSource();
		//if last index is selected, which is other
		//then show a popup dialog to update combo list and speciesText
		if(cb.getText().equals("other"))
		{
			//update the combo list and order by asc
			//need to remove other item and order by asc and then add it back to the combo list
			//also should avoid duplicate item
			ComboPopupDialog cDialog = new ComboPopupDialog(parent.getShell());
			cDialog.setCb(cb);
			if(cDialog.open() == Window.OK)
			{
				Event event = new Event();
				event.text = "other";//to differ OK from CANCLE  
				cb.notifyListeners(SWT.Modify, null);
			}
			else
			{
				//if cancel was clicked, then
				cb.select(0);//choose empty
			}
		}
		//others update cb
		else
		{
			String[] items = cb.getItems();
			for(int i=0; i<cb.getItemCount(); i++)
			{
				if(items[i].equals(cb.getText()))
				{
					cb.select(i);
				}
			}
			cb.notifyListeners(SWT.Modify, null);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void setParent(Composite parent) {
		this.parent = parent;
	}
}
