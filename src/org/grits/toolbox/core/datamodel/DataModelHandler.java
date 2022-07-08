package org.grits.toolbox.core.datamodel;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.grits.toolbox.core.service.IGritsDataModelService;

@Singleton
public class DataModelHandler
{
	@Inject static IGritsDataModelService gritsDataModelService;
	@Inject static IEclipseContext eclipseContext;

	private boolean show = true;

	public static DataModelHandler instance()
	{
		return eclipseContext.get(DataModelHandler.class);
	}

	public Entry getRoot()
	{
		return gritsDataModelService.getRootEntry();
	}
	
	public Entry findEntryById (Integer entryId) {
		return gritsDataModelService.findEntryById(entryId);
	}
 
	/**
	 * @deprecated use {@link GritsDataModelService#deleteEntry(Entry)}
	 * @param entry
	 * @param update
	 * @throws IOException
	 */
	@Deprecated
	public void deleteEntry(Entry entry, boolean update) throws IOException
	{
		boolean lastUpdateMode = GritsDataModelService.updateMode;
		GritsDataModelService.updateMode = update;
		gritsDataModelService.deleteEntry(entry);
		GritsDataModelService.updateMode = lastUpdateMode;
	}

	/** 
	 * @deprecated use {@link GritsDataModelService#renameEntry(Entry, String)}
	 */
	@Deprecated
	public void renameEntry(Entry entry, String newName)
	{
		gritsDataModelService.renameEntry(entry, newName);
	}
	
	/**
	 * Add entry
	 * @param parent
	 * @param toAdd
	 * @param update whether to update project modification time or not
	 * @deprecated use {@link GritsDataModelService#addEntry(Entry, Entry)}
	 */
	@Deprecated
	public void addEntry(Entry parent, Entry toAdd, boolean update) 
	{
		boolean lastUpdateMode = GritsDataModelService.updateMode;
		GritsDataModelService.updateMode = update;
		gritsDataModelService.addEntry(parent, toAdd);
		GritsDataModelService.updateMode = lastUpdateMode;
	}

	/** 
	 * @deprecated use {@link GritsDataModelService#removeAllChildren(Entry)}
	 */
	@Deprecated
	public void removeChildren(Entry parent, boolean update)
	{
		boolean lastUpdateMode = GritsDataModelService.updateMode;
		GritsDataModelService.updateMode = update;
		gritsDataModelService.removeAllChildren(parent);
		GritsDataModelService.updateMode = lastUpdateMode;
	}

	/** 
	 * @deprecated use {@link GritsDataModelService#addProjectEntry(Entry)}
	 */
	@Deprecated
	public void addChildren(Entry projectEntry) throws Exception
	{
		gritsDataModelService.addProjectEntry(projectEntry);
	}

	/** 
	 * @deprecated use {@link GritsDataModelService#getLastSelection()}
	 */
	@Deprecated
	public StructuredSelection getLastSelection()
	{
		return gritsDataModelService.getLastSelection();
	}

	public boolean getShow()
	{
		return show;
	}

	public void setShow(boolean show)
	{
		this.show = show;
	}
}
