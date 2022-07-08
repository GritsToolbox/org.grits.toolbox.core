/**
 * 
 */
package org.grits.toolbox.core.service;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.grits.toolbox.core.datamodel.Entry;

/**
 * 
 *
 */
public interface IGritsUIService
{
	public static final String PARTSTACK_PRIMARY_DATA =
			"org.eclipse.e4.primaryDataStack";

	public static final String TRANSIENT_DATA_KEY_PART_ENTRY =
			"org.grits.toolbox.entry.current";

	/**
	 * opens entry in a part (meant for plugin using e4 architecture
	 * that defines a partDescriptor for each entry)
	 * @param entry the entry to be opened
	 * @return the part displaying the entry 
	 * or null if no part descriptor was found for the entry
	 */
	public MPart openEntryInPart(Entry entry);

	/**
	 * closes part for the given entry if there is one
	 * @param entry the entry whose part is to be closed
	 * @return true if a part was found for the entry and successfully closed
	 */
	public boolean closePartForEntry(Entry entry);

	/**
	 * finds the part for the given entry or returns null
	 * @param entry
	 * @return the part or null
	 */
	public MPart findPartForEntry(Entry entry);

	/**
	 * finds the part descriptor for the given id
	 * @param entry
	 * @return the partDescriptor or null
	 */
	public MPartDescriptor findPartDescriptorForEntry(Entry entry);

	/**
	 * finds the perspective for the given part descriptor id
	 * @param elementId
	 * @return
	 */
	public MPerspective findPerspectiveForPartDescriptor(String partDescriptorId);

	/**
	 * switches to the perspective whose perpectiveId is given
	 * @param perspectiveId
	 */
	public void selectPerspective(String perspectiveId);

	/** 
	 * used to set the partService from the commands/handlers since the injected one 
	 * might become stale and may not have access to the active window
	 * see ticket #799
	 * 
	 * @param partService
	 */
	public void setPartService(EPartService partService);
}
