package org.grits.toolbox.core.editor;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;

public interface IEntryEditorPart {
	Entry getEntry();
	void setEntry (Entry entry);
	
	void createPartControl(Composite parent);

	void setDirty(boolean b);
	boolean isDirty();
}
