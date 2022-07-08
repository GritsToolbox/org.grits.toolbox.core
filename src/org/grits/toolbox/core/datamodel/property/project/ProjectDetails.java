/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name = "projectDetails")
@XmlType(propOrder={"version", "entryName", "modificationTime", "description",
		"collaborators", "tasklists", "events", "keywords"})
public class ProjectDetails
{
	public static final String CURRENT_VERSION = "1.0";
	private String version = null;
	private String entryName = null;
	private Date modificationTime = null;
    private String description = null;
    private List<ProjectCollaborator> collaborators = new ArrayList<>();
    private List <ProjectTasklist> tasklists = new ArrayList<>();
    private List<ProjectEvent> events = new ArrayList<>();
    private Set<String> keywords = new HashSet<>();

    public ProjectDetails(String entryName)
    {
        this.entryName = entryName;
		this.version  = CURRENT_VERSION;
    }

    public ProjectDetails()
    {
        
    }

    /**
     * @return the entryName
     */
    @XmlAttribute(name = "entryName", required= true)
    public String getEntryName()
    {
        return entryName;
    }
    /**
     * @param entryName the entryName to set
     */
    public void setEntryName(String entryName)
    {
        this.entryName = entryName;
    }

	/**
	 * gets the version of the project details file
	 * @return
	 */
	@XmlAttribute(name = "version", required= true)
	public String getVersion()
	{
		return version;
	}

	/**
	 * sets the version of the project details file
	 * @param version
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * @return the modificationTime
	 */
	@XmlElement(name = "modificationTime", required= true)
	public Date getModificationTime()
	{
		return modificationTime;
	}

	/**
	 * @param modificationTime the modificationTime to set
	 */
	public void setModificationTime(Date modificationTime)
	{
		this.modificationTime = modificationTime;
	}

    /**
     * @return the description
     */
    @XmlElement(name = "description", required= false)
    public String getDescription()
    {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    /**
     * @return the collaborators
     */
    @XmlElement(name = "collaborators", required= false)
    public List<ProjectCollaborator> getCollaborators()
    {
        return collaborators;
    }
    /**
     * @param collaborators the collaborators to set
     */
    public void setCollaborators(List<ProjectCollaborator> collaborators)
    {
        this.collaborators = collaborators;
    }
    /**
     * @return the tasklists
     */
    @XmlElement(name = "tasklists", required= false)
    public List<ProjectTasklist> getTasklists()
    {
        return tasklists;
    }
    /**
     * @param tasklists the tasklists to set
     */
    public void setTasklists(List<ProjectTasklist> tasklists)
    {
        this.tasklists = tasklists;
    }
    /**
     * @return the events
     */
    @XmlElement(name = "events", required= false)
    public List<ProjectEvent> getEvents()
    {
        return events;
    }
    /**
     * @param events the events to set
     */
    public void setEvents(List<ProjectEvent> events)
    {
        this.events = events;
    }
    /**
     * @return the keywords
     */
    @XmlElement(name = "keywords", required= false)
    public Set<String> getKeywords()
    {
        return keywords;
    }
    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(Set<String> keywords)
    {
        this.keywords = keywords;
    }
}
