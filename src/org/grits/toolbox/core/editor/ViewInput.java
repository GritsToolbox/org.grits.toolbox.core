package org.grits.toolbox.core.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.grits.toolbox.core.datamodel.Entry;

@Deprecated
public class ViewInput implements IEditorInput {

	private Entry entry = null;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean equals(Object obj) {
//		if (this == obj)  // DBW commented out...safe equals check here???
//			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewInput other = (ViewInput) obj;
		//check type first
		Entry thisEntry = this.entry;
		
		boolean bSameType = thisEntry.getProperty().getType().equals(other.getEntry().getProperty().getType());
		if( ! bSameType && thisEntry.getProperty().getParentProperty() != null && 
				thisEntry.getProperty().getParentProperty().getType().equals(other.getEntry().getProperty().getType()) ) {
			thisEntry = thisEntry.getParent();
			bSameType = true;
		} 
		if(bSameType)
		{
			//check name..project name should be unique!
			// DBW edited on 03/2013:  equals now uses display name AND parent.equals (so entries can have same name if they don't have same parent
			if(! this.entry.getDisplayName().equals(other.getEntry().getDisplayName()))
			{
				return false;
			} else if ( thisEntry.getParent() != null ) {
				return thisEntry.getParent().equals( other.getEntry().getParent() );				
			}
			return true;
		}
		return false;
	}
	
}
