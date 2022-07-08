/**
 * 
 */
package org.grits.toolbox.core.datamodel;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.StructuredSelection;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.service.IGritsDataModelService;

/**
 * 
 *
 */
@Singleton
public class GritsDataModelService implements IGritsDataModelService
{
	private static final Logger logger = Logger.getLogger(GritsDataModelService.class);

	private StructuredSelection lastSelection = null;
	Map<Entry, Integer> lastIdentifierPerProject = new HashMap<>();
	private Integer lastIdentifier = 0;

	/**
	 * indicates whether follow up changes would require update of project modification time
	 * @param updateMode set to false if the projects modification time need not be updated 
	 * else set it to true 
	 */
	public static boolean updateMode = true;

	@Inject IEventBroker eventBroker;
	@Inject @Optional @Named(IGritsDataModelService.WORKSPACE_ENTRY) Entry workspaceEntry;

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void addProjectEntry(Entry projectEntry) throws Exception
	{
		boolean currentUpdateMode = updateMode;
		try
		{
			updateMode = true;
			addEntry(workspaceEntry, projectEntry);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
		updateMode = currentUpdateMode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntry(Entry parentEntry, Entry childEntry)
	{
		parentEntry.addChild(childEntry);
		childEntry.setParent(parentEntry);
		if(updateMode)
		{
			updateProjectModificationTime(childEntry);
			logger.info("Add Entry: parentEntry[" + parentEntry +"], childEntry["+ childEntry+"].");
		}
		eventBroker.send(EVENT_DATA_MODEL_CHANGED, parentEntry);
	}

	/**
	 * close the project entry, sets the open flag to false 
	 * and collapses the project entry
	 * @param entry
	 * @param newName
	 */
	public void closeProject(Entry entry)
	{
		if(entry.getProperty().getType().equals(ProjectProperty.TYPE))
		{
			removeAllChildren(entry);
			((ProjectProperty) entry.getProperty()).setOpen(false);
			if(updateMode)
			{
				updateProjectModificationTime(entry);
			}
			eventBroker.send(EVENT_DATA_MODEL_CHANGED, entry);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(Entry entry) throws IOException
	{
		if(entry != null && entry.getProperty() != null
				&& !entry.getProperty().getType().equals(WorkspaceProperty.TYPE))
		{
			entry.delete();
			Entry parentEntry = entry.getParent();
			if(parentEntry != null)
			{
				parentEntry.removeChild(entry);
				if(updateMode)
				{
					updateProjectModificationTime(parentEntry);
					logger.info("deleted Entry: entry["+ entry+"], parentEntry[" + parentEntry +"].");
				}
				eventBroker.send(EVENT_DATA_MODEL_CHANGED, parentEntry);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry getRootEntry()
	{
		return workspaceEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAllChildren(Entry parent)
	{
		for(Entry child : parent.getChildren())
		{
			removeEntry(parent, child);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntry(Entry parentEntry, Entry toRemove)
	{
		parentEntry.removeChild(toRemove);
		if(updateMode)
		{
			updateProjectModificationTime(parentEntry);
			logger.info("Removed Entry: parentEntry[" + parentEntry +"], toRemove["+ toRemove+"].");
		}
		eventBroker.send(EVENT_DATA_MODEL_CHANGED, parentEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renameEntry(Entry entry, String newName)
	{
		entry.setDisplayName(newName);
		if(updateMode)
		{
			updateProjectModificationTime(entry);
			logger.info("Renamed Entry: entry[" + entry +"].");
		}
		eventBroker.send(EVENT_DATA_MODEL_CHANGED, entry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRoot(Entry workspaceEntry)
	{
		this.workspaceEntry = workspaceEntry;
		eventBroker.send(EVENT_DATA_MODEL_CHANGED, workspaceEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateProjectModificationTime(Entry entry)
	{
		Entry projectEntry = findParentByType(entry, ProjectProperty.TYPE);
		if(projectEntry != null)
		{
			try
			{
				ProjectDetails projectDetails = ProjectDetailsHandler.getProjectDetails(projectEntry);
				projectDetails.setModificationTime(new Date());
				ProjectDetailsHandler.writeProjectDetails(projectDetails);
			} catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			} catch (Exception e)
			{
				logger.fatal(e.getMessage(), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findParentByType(Entry entry, String type)
	{
		if(entry.getProperty().getType().equals(type))
		{
			return entry;
		}

		Entry parent = entry.getParent();
		if(parent == null || parent.getProperty() == null
				|| parent.getProperty().getType() == null)
		{
			return null;
		}
		else if(parent.getProperty().getType().equals(type))
		{
			return parent;
		}
		else
		{
			return findParentByType(parent, type);
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	public Entry findEntryById (Integer entryId) {
		if (workspaceEntry == null || workspaceEntry.getChildren() == null) 
			return null;
		for (Entry e: workspaceEntry.getChildren()) {
			if (e.getId() != null && e.getId().equals(entryId))
				return e;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLastSelection(StructuredSelection lastSelection)
	{
		this.lastSelection = lastSelection;
	}

	/**
	 * {@inheritDoc}
	 */
	public StructuredSelection getLastSelection()
	{
		return this.lastSelection;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLastIdentifierForProject (Entry projectEntry, Integer lastIdentifier) {
		this.lastIdentifierPerProject.put(projectEntry, lastIdentifier);
	}
	
	@Override
	public Integer getLastIdentifierForProject(Entry projectEntry) {
		return this.lastIdentifierPerProject.get(projectEntry);
	}
}
