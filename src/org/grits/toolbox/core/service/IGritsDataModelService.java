/**
 * 
 */
package org.grits.toolbox.core.service;

import java.io.IOException;

import javax.inject.Singleton;

import org.eclipse.jface.viewers.StructuredSelection;
import org.grits.toolbox.core.datamodel.Entry;

/**
 * 
 *
 */
@Singleton
public interface IGritsDataModelService
{
	public static final String WORKSPACE_ENTRY = "grits_workspace_entry";
	public static final String EVENT_DATA_MODEL_CHANGED = "grits_data_model_changed";
	public static final String EVENT_SELECT_ENTRY = "grits_entry_selected";

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#addChildren(Entry)
	 * 
	 * @param projectEntry
	 * @throws Exception
	 */
	public void addProjectEntry(Entry projectEntry) throws Exception;

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#addEntry(Entry parent, Entry toAdd, boolean update)
	 * 
	 * @param parentEntry
	 * @param childEntry the entry to be added
	 */
	public void addEntry(Entry parentEntry, Entry childEntry);

	/**
	 * removes all of its child entry, sets the open flag to false
	 * @param entry
	 * @param newName
	 */
	public void closeProject(Entry entry);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#deleteEntry(Entry entry, boolean update)
	 * @param entry the entry to be deleted
	 * @throws IOException
	 */
	public void deleteEntry(Entry entry) throws IOException;

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#getRoot()
	 * @return the root entry, i.e. the workspace entry. It can also be retrieved through injection
	 * using  the name IGritsDataModelService.WORKSPACE_ENTRY
	 */
	public Entry getRootEntry();

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#removeChildren(Entry parent, boolean update)
	 * @param parent
	 * @param update
	 */
	public void removeAllChildren(Entry parent);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#removeEntry(Entry parent, Entry toRemove, boolean update)
	 * @param parent
	 * @param toRemove
	 * @param update
	 */
	public void removeEntry(Entry parent, Entry toRemove);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#renameEntry(Entry entry, String newName)
	 * @param entry
	 * @param newName
	 */
	public void renameEntry(Entry entry, String newName);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#setRoot(Entry workspace)
	 * @param workspaceEntry
	 */
	public void setRoot(Entry workspaceEntry);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#updateProjectModificationTime(Entry toAdd)
	 * @param entry
	 */
	public void updateProjectModificationTime(Entry entry);

	/**
	 * find the parent entry of the given type
	 * @param entry entry whose parent is to be found
	 * @param type (property type of the parent entry)
	 * @return
	 */
	public Entry findParentByType(Entry entry, String type);

	/**
	 * finds the Entry with the given identifier
	 * @param entryId identifier for the Entry to search
	 * @return Entry if matching one is found or null
	 */
	public Entry findEntryById (Integer entryId);
	
	/**
	 * sets the last selection for the data model
	 * @param lastSelection
	 */
	public void setLastSelection(StructuredSelection lastSelection);

	/**
	 * returns the last selection of the data model
	 * @return lastSelection
	 */
	public StructuredSelection getLastSelection();

	/**
	 * set the last identifier for the Entries for a Project (from project.xml file)
	 * @param projectEntry project entry to set the identifier
	 * @param lastIdentifier the last number to use in project.xml file
	 */
	void setLastIdentifierForProject(Entry projectEntry, Integer lastIdentifier);
	
	/**
	 * get the last number used for the given project (in project.xml file)
	 * @param projectEntry project entry to get the identifier
	 * @return the last number used
	 */
	Integer getLastIdentifierForProject (Entry projectEntry);

	/*
	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelChangeListener#addChild(Entry parent, Entry toAdd, boolean show);
	void addChild(Entry parent, Entry childEntry, boolean show);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelChangeListener#removeChild(Entry parent, Entry toAdd);
	void removeChild(Entry parent, Entry childEntry);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelChangeListener#updateName(Entry entry, String newName);
	void updateName(Entry entry, String newName);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#addChildren(Entry projectEntry) throws Exception
	//public void getShow();

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#setShow(boolean show)
	//public void setShow(boolean show);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#subscribe(DataModelChangeListener _listener)
	//public void subscribe(DataModelChangeListener _listener);

	/**
	 * @see org.grits.toolbox.core.datamodel.DataModelHandler#unsubscribe(DataModelChangeListener _listener)
	//public void unsubscribe(DataModelChangeListener _listener);
	 */
}
