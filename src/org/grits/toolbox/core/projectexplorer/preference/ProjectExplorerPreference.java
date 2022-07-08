package org.grits.toolbox.core.projectexplorer.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProjectExplorerPreference extends PreferencePage
implements IPropertyChangeListener, IWorkbenchPreferencePage {

	private static final Logger logger = Logger.getLogger(ProjectExplorerPreference.class);
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Control createContents(Composite parent)
	{
        logger.debug("START : Creating Preference Page for Project Explorer");

        logger.debug("END   : Creating Preference Page for Project Explorer");
		return parent;
	}

	@Override
	//when apply button is clicked
	protected void performApply() {
		save();
    }
	
	@Override
	public boolean performOk()
	{
		if(validInput())
		{
			save();
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean validInput()
	{
		return true;
	}

	private void save()
	{
	    
	}

}