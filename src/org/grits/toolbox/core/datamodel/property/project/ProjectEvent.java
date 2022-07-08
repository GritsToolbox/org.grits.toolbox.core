/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 *
 */
@XmlRootElement(name = "event")
@XmlType(propOrder={"projectAction", "eventDate", 
		"description", "addByDefault"})
public class ProjectEvent
{
	private boolean addByDefault = false;
	private ProjectAction projectAction = null;
	private Date eventDate = null;
	private String description = null;

	/**
	 * @return the addByDefault
	 */
	@XmlAttribute(name = "addByDefault", required= true)
	public boolean isAddByDefault()
	{
		return addByDefault;
	}
	/**
	 * @param addByDefault the addByDefault to set
	 */
	public void setAddByDefault(boolean addByDefault)
	{
		this.addByDefault = addByDefault;
	}

	/**
	 * @return the action
	 */
	@XmlElement(name = "projectAction", required= true)
	public ProjectAction getProjectAction()
	{
		return projectAction;
	}
	/**
	 * @param action the action to set
	 */
	public void setProjectAction(ProjectAction projectAction)
	{
		this.projectAction = projectAction;
	}
	/**
	 * @return the eventDate
	 */
	@XmlElement(name = "eventDate", required= true)
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getEventDate()
	{
		return eventDate;
	}
	/**
	 * @param eventDate the eventDate to set
	 */
	public void setEventDate(Date eventDate)
	{
		this.eventDate = eventDate;
	}
	/**
	 * @return the description
	 */
	@XmlAttribute(name = "description", required= false)
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

	@XmlTransient
	public ProjectEvent getACopy()
	{
		ProjectEvent projectEvent = new ProjectEvent();
		projectEvent.setAddByDefault(addByDefault);
		projectEvent.setDescription(description);
		if(eventDate != null)
			projectEvent.setEventDate(new Date(eventDate.getTime()));
		if(projectAction != null)
			projectEvent.setProjectAction(projectAction.getACopy());
		return projectEvent;
	}

	public boolean matches(ProjectEvent projectEvent)
	{
		return (projectAction == null && projectEvent.getProjectAction() == null)
				|| ((projectAction != null && projectEvent.getProjectAction() != null) &&
						Objects.equals(projectAction.getAction(), projectEvent.getProjectAction().getAction()));
	}
}
