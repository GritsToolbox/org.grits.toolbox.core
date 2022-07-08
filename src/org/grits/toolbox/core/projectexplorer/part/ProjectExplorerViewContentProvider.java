package org.grits.toolbox.core.projectexplorer.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.core.datamodel.Entry;

/**
 * Need to provide codes to transfer information from datamodel to tree in project explorer
 * @author kitaemyoung
 *
 */
public class ProjectExplorerViewContentProvider implements ITreeContentProvider {

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        return this.getChildren(parent);
    }

    public Object getParent(Object child) {
        if (child instanceof Entry) {
            return ((Entry)child).getParent();
        }
        return null;
    }

    public Object[] getChildren(Object parent) 
    {
        if (parent instanceof Entry) 
        {
            Entry t_parent = (Entry)parent;
            // have to filter out hidden nodes
            List<Entry> t_realChildren = new ArrayList<Entry>();
            for(Entry t_child : t_parent.getChildren())
            {
                if ( (t_child.getEntryType() & Entry.ENTRY_TYPE_HIDDEN) != Entry.ENTRY_TYPE_HIDDEN )
                {
                    t_realChildren.add(t_child);
                }
            }
            return t_realChildren.toArray();
        }
        return new Entry[0];
    }

    public boolean hasChildren(Object parent) {
        if (parent instanceof Entry)
        {
            Entry t_parent = (Entry)parent;
            for (Entry t_child : t_parent.getChildren())
            {
                if ( (t_child.getEntryType() & Entry.ENTRY_TYPE_HIDDEN) != Entry.ENTRY_TYPE_HIDDEN )
                {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}
