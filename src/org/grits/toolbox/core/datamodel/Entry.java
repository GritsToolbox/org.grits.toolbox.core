package org.grits.toolbox.core.datamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.grits.toolbox.core.datamodel.property.NotImplementedException;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;

/**
 * Entry in the tree
 * @author kitaemyoung
 *
 */
public class Entry 
{
    public static final Integer ENTRY_TYPE_NORMAL = 1;
    public static final Integer ENTRY_TYPE_HIDDEN = 2;
    
    private ArrayList<Entry> children = new ArrayList<Entry>();
    private Integer id = null;
    //name in a tree
    private String displayName = null;
    private Property property = null;
    private Entry parent = null;
    private Date creationDate = new Date();
    private String m_lastEditorId = null;
    private Integer m_entryType = Entry.ENTRY_TYPE_NORMAL;

    @SuppressWarnings("unchecked")
    public List<Entry> getChildren() {
        return (List<Entry>) children.clone();
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public Property getProperty() {
        return property;
    }
    public void setProperty(Property property) {
        this.property = property;
    }
    public Entry getParent() {
        return parent;
    }
    public void setParent(Entry parent) {
        this.parent = parent;
    }
    public Integer getId() {
		return id;
	}
    public void setId(Integer id) {
		this.id = id;
	}
    public boolean removeChild(Entry child)
    {
        return this.children.remove(child);
    }
    public void addChild(Entry child)
    {
        this.children.add(child);
    }
    public boolean hasChildren() {
        return this.children.size() > 0;
    }
    public void delete() throws IOException
    {
        for(Entry child : this.getChildren())
        {
            child.delete();
        }
        this.property.delete(this);
        this.property.setRemoved(true);
    }

    public Entry copyToEntry(Entry destinationParent, IGritsDataModelService gritsDataModelService) throws NotImplementedException, IOException
    {
    	if(destinationParent.getProperty().getType().equals(getParent().getProperty().getType()))
    	{
    		Entry copiedEntry = new Entry();
    		Date cDate = creationDate == null 
    				? null : new Date(creationDate.getTime());
    		copiedEntry.setCreationDate(cDate);
    		copiedEntry.setDisplayName(displayName);
    		//copiedEntry.setId(id);
    		Entry projectEntry = DataModelSearch.findParentByType(destinationParent, ProjectProperty.TYPE);
    		copiedEntry.setId(gritsDataModelService.getLastIdentifierForProject(projectEntry));
    		gritsDataModelService.setLastIdentifierForProject(projectEntry, copiedEntry.getId()+1);
    		copiedEntry.setEntryType(m_entryType.intValue());
    		copiedEntry.setLastEditorId(m_lastEditorId);
    		copiedEntry.setParent(destinationParent);
    		try
    		{
    			property.makeACopy(this, copiedEntry);
    		} catch (NotImplementedException ex)
    		{
    			destinationParent.removeChild(copiedEntry);
    			throw new NotImplementedException("Entry property could not be copied."
    					+ " This might not be functional yet for this type of entry.", ex);
    		}

    		try
    		{
    			for(Entry child : children)
    			{
    				copiedEntry.addChild(child.copyToEntry(copiedEntry, gritsDataModelService));
    			}
    		} catch (NotImplementedException ex)
    		{
    			copiedEntry.delete();
    			destinationParent.removeChild(copiedEntry);
    			throw new NotImplementedException("Entry could not be copied as "
    					+ "one of its child entry does not support copy functionality yet.", ex);
    		}
    		return copiedEntry;
    	}
    	else throw new IOException(". This entry has property " + getProperty().getType()
    			+ "and cannot be copied to an entry with property "
    			+ destinationParent.getProperty().getType() 
    			+ ". It can only be copied to an entry with property " 
    			+ getParent().getProperty().getType());
    }

    @Override
    public String toString()
    {
        return displayName + "(" + this.property.getType() + ")";
    }
    public Date getCreationDate()
    {
        return creationDate;
    }
    public void setCreationDate(Date a_creationDate)
    {
        creationDate = a_creationDate;
    }
    public String getLastEditorId()
    {
        return m_lastEditorId;
    }
    public void setLastEditorId(String a_lastEditorId)
    {
        m_lastEditorId = a_lastEditorId;
    }
    public Integer getEntryType()
    {
        return m_entryType;
    }
    public void setEntryType(Integer a_entryType)
    {
        m_entryType = a_entryType;
    }
}
