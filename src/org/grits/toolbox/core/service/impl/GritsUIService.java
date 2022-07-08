/**
 * 
 */
package org.grits.toolbox.core.service.impl;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.CancelableMultiPageEditor;
import org.grits.toolbox.core.editor.ICancelableEditor;
import org.grits.toolbox.core.projectexplorer.part.ProjectExplorerPart;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.osgi.service.event.Event;

/**
 * 
 *
 */
@SuppressWarnings("restriction")
public class GritsUIService implements IGritsUIService
{
	private static final Logger logger = Logger.getLogger(GritsUIService.class);

	@Inject private EModelService modelService;
	@Inject private EPartService partService;
	@Optional @Inject private MApplication application;
	@Optional @Inject private IEclipseContext context;

	/**
	 * {@inheritDoc}
	 */
	public MPart openEntryInPart(Entry entry)
	{
		MPart entryPart = null;
		if(entry != null)
		{
			logger.info("Opening an entry " + entry.getDisplayName() + " in a part");
			MPartDescriptor matchedPartDescriptor = findPartDescriptorForEntry(entry);
			if(matchedPartDescriptor != null) // implies there is an e4 part descriptor for entry
			{
				IEclipseContext projectExplorerContext =
						partService.findPart(ProjectExplorerPart.PART_ID).getContext();
				if (projectExplorerContext == null) { // not rendered currently, should render it
					MPart part = partService.showPart(ProjectExplorerPart.PART_ID, PartState.ACTIVATE);
					projectExplorerContext = part.getContext();
				}
				ESelectionService selectionService = projectExplorerContext.get(ESelectionService.class);
				// for part descriptors to use entry as an active selection through injection
				selectionService.setSelection(entry);

				entryPart = findPartForEntry(entry);
				if(entryPart == null)
				{
					try
					{
						logger.info("Creating a part for the entry from the part descriptor : "
								+ matchedPartDescriptor.getElementId());
						entryPart = partService.createPart(matchedPartDescriptor.getElementId());
						entryPart.setLabel(entry.getDisplayName());

						logger.info("Setting the current entry as its transient data for this part");
						entryPart.getTransientData().put(TRANSIENT_DATA_KEY_PART_ENTRY, entry);
					} catch (Exception ex)
					{
						logger.error("Error creating part for the entry\n" + ex.getMessage(), ex);
						ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(),
								"Error creating part for the entry", ex);
						partService.hidePart(entryPart, true);
					}
				}

				try {
					logger.info("Adding part to partstack - e4.primaryDataStack");
					PartStackImpl partStackImpl = (PartStackImpl) modelService.find(
							IGritsUIService.PARTSTACK_PRIMARY_DATA, application);
					partStackImpl.getChildren().add(entryPart);
				
					MPart openedPart = partService.showPart(entryPart, PartState.ACTIVATE); // resetting perspective is done separately
					// need to check if the opening of the part is canceled by the user
					if (openedPart != null && openedPart.getObject() != null && openedPart.getObject() instanceof ICancelableEditor) {
						if( ( (ICancelableEditor) openedPart.getObject()).isCanceled() ) {
							partService.hidePart(openedPart);
						}
					} else if (openedPart != null && openedPart.getObject() == null) {
						// failed to initialize the object
						logger.error("Failed to initialize the part for the entry");
						partService.hidePart(openedPart);
					}
				} catch (Exception e) {
					// showing the part may fail if the entry is corrupt etc.
					// need to close the part
					logger.error("Failed to show the part for the entry", e);
					partService.hidePart(entryPart, true);
				}
				logger.info("Entry opened in a part");
			}
		}
		return entryPart;
	}
	
	public void setPartService(EPartService partService) {
		this.partService = partService;
	}

	/**
	 * reset perspective when an entry editor is brought on top
	 * @param event
	 */
	@Optional @Inject
	protected void resetPerspective(@UIEventTopic
			(UIEvents.UILifeCycle.BRINGTOTOP) Event event)
	{
		Object element = null;
		if (event != null &&
				(element = event.getProperty(UIEvents.EventTags.ELEMENT)) instanceof MPart)
		{
			MPart part = (MPart) element;
			logger.info("Part brought to top : " + part.getElementId());

			// no perspective change for project explorer part or compatibility editor
			// as 3.x editors are handled in EditorHandler
			if(ProjectExplorerPart.PART_ID.equals(part.getElementId())
					|| "org.eclipse.e4.ui.compatibility.editor".equals(part.getElementId()))
				return;

			MPerspective perspective = findPerspectiveForPartDescriptor(part.getElementId());

			if(perspective != null
					&& getDefaultPerspectiveStack().getSelectedElement() != perspective)
			{
				logger.info("Switching to perspective : " + perspective.getElementId());
				getDefaultPerspectiveStack().setSelectedElement(perspective);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean closePartForEntry(Entry entry)
	{
		boolean closed = false;
		if(entry != null)
		{
			logger.info("Closing part for the entry : " + entry.getDisplayName());
			MPartDescriptor partDescriptor = findPartDescriptorForEntry(entry);
			if(partDescriptor != null)
			{
				MPart part = findPartForEntry(entry);
				if(part != null)
				{
					partService.hidePart(part);
					closed = true;

					logger.info("Part closed : " + part.getElementId() +
							" for entry " + entry.getDisplayName());
				}
			}
		}
		return closed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MPart findPartForEntry(Entry entry)
	{
		if(entry == null)
		{
			logger.error("No part descriptor for a null entry");
			return null;
		}

		logger.info("Finding part for entry " + entry.getDisplayName());
		MPart entryPart = null;
		MPartDescriptor partDescriptor = findPartDescriptorForEntry(entry);
		if(entry.getDisplayName() != null 
				&& partDescriptor != null && partDescriptor.getElementId() != null)
		{
			logger.info("Finding existing part for partDescriptorId : " + partDescriptor.getElementId());

			// get all parts whose ids are same as the part descriptor id
			List<MPart> entryParts = modelService.findElements(application,
					MPart.class, EModelService.ANYWHERE, new Selector()
			{
				@Override
				public boolean select(MApplicationElement element)
				{
					return Objects.equals(element.getElementId(), partDescriptor.getElementId());
				}
			});

			for(MPart ep : entryParts)
			{
				if(entry.getDisplayName().equals(ep.getLabel()) && 
						entryEquals (entry, (Entry) ep.getTransientData().get(TRANSIENT_DATA_KEY_PART_ENTRY)))
						//entry.equals(ep.getTransientData().get(TRANSIENT_DATA_KEY_PART_ENTRY)))
				{
					entryPart = ep;
					logger.info(
							"Existing part with label : \"" + entry.getDisplayName()
							+ "\" and partDescriptorId : \"" + partDescriptor.getElementId()
							+ "\" was found for this entry.");
					break;
				}
			}
		}
		return entryPart;
	}
	
	/**
	 * This method is used to check the equivalence of two Entry objects
	 * if they are the same objects, it returns true. If not, it tries to determine if both refer to the same entry by
	 * checking other parts like display name, property, parent entries (Extracted from ViewInput's equals method)
	 * @param entry first Entry
	 * @param other second Entry
	 * @return true if they point to the same Entry
	 * 
	 * @see {@link http://trac.grits-toolbox.org/ticket/782#ticket}
	 */
	private boolean entryEquals (Entry entry, Entry other) {
		if (entry == other)
			return true;
		if (entry == null || other == null)
			return false;
		boolean bSameType = entry.getProperty().getType().equals(other.getProperty().getType());
		if( ! bSameType && entry.getProperty().getParentProperty() != null && 
				entry.getProperty().getParentProperty().getType().equals(other.getProperty().getType()) ) {
			entry = entry.getParent();
			bSameType = true;
		} 
		if(bSameType)
		{
			//check name..project name should be unique!
			// DBW edited on 03/2013:  equals now uses display name AND parent.equals (so entries can have same name if they don't have same parent
			if(! entry.getDisplayName().equals(other.getDisplayName()))
			{
				return false;
			} else if ( entry.getParent() != null ) {
				return entry.getParent().equals( other.getParent() );				
			}
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MPartDescriptor findPartDescriptorForEntry(Entry entry)
	{
		MPartDescriptor matchedPartDescriptor = null;
		if(entry != null && entry.getProperty() != null)
		{
			logger.info("Finding part descriptor for " + entry.getDisplayName());
			outerloop :
				for(MPartDescriptor descriptor : application.getDescriptors())
				{
					for(MBindingContext bindingContext : descriptor.getBindingContexts())
					{
						if(IGritsConstants.ID_BINDING_CONTEXT_ENTRY.equals(bindingContext.getElementId()) 
								&& descriptor.getCategory().equals(entry.getProperty().getType()))
						{
							logger.info("A part descriptor for entry found : " 
									+ descriptor.getElementId());
							matchedPartDescriptor = descriptor;
							if(matchedPartDescriptor.getTags().contains("default"))
							{
								logger.info("The part descriptor found for the entry"
										+ " has \"default\" tag : " + descriptor.getElementId());
								break outerloop;
							}
						}
					}
				}
		}
		return matchedPartDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectPerspective(String perspectiveId)
	{
		logger.info("Switching to perspective : " + perspectiveId);
		try
		{
			MUIElement uiElement = modelService.find(perspectiveId, application);
			if(uiElement instanceof PerspectiveImpl)
			{
				getDefaultPerspectiveStack().setSelectedElement((PerspectiveImpl) uiElement);
				logger.info("Perspective switched to : " + perspectiveId);
			}
			else
				logger.error("Perspective could not be found : " + perspectiveId);
		} catch (Exception ex)
		{
			logger.error("Error finding/switching perspective\n" + ex.getMessage(), ex);
		}
	}

	private PerspectiveStackImpl getDefaultPerspectiveStack()
	{
		logger.info("Retrieving default perspective stack : "
				+ IGritsConstants.ID_DEFAULT_PERSPECTIVE_STACK);
		return (PerspectiveStackImpl)
				modelService.find(IGritsConstants.ID_DEFAULT_PERSPECTIVE_STACK, application);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MPerspective findPerspectiveForPartDescriptor(String partDescriptorId)
	{
		logger.info("Finding perspective for : " + partDescriptorId);
		MPerspective selectedPerspective = null;
		MPartDescriptor partDescriptor = modelService.getPartDescriptor(partDescriptorId);
		if(partDescriptor != null)
		{
			PerspectiveStackImpl perspectiveStackImpl = (PerspectiveStackImpl)
					modelService.find(IGritsConstants.ID_DEFAULT_PERSPECTIVE_STACK, application);
			for(MPerspective perspective : perspectiveStackImpl.getChildren())
			{
				if(partDescriptor.getTags().contains("perspective:" + perspective.getElementId()))
				{
					logger.info("Perspective found for part descriptor "
							+ partDescriptorId + " : " + perspective.getElementId());
					selectedPerspective = perspective;
					break;
				}
				// when .<$PERSPECTIVE_LABEL> is added to its id
				else if(perspective.getElementId().endsWith("." + perspective.getLabel())
						&& partDescriptor.getTags().contains("perspective:" +
						perspective.getElementId().substring(0,
								perspective.getElementId().indexOf("." + perspective.getLabel()))))
				{
					logger.info("Perspective found for part descriptor "
							+ partDescriptorId + " : " + perspective.getElementId());
					selectedPerspective = perspective;
					break;
				}
			}
		}
		return selectedPerspective;
	}
}
