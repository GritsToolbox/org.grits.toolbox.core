/**
 *
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 *
 */
@XmlRootElement(name = "projectTasklist")
@XmlType(propOrder = { "task", "templateList", "person", "status", "role", "numberOfTasks", "groupOrPIName", "dueDate",
		"description", "addByDefault", "sampleType", "sampleName" })
public class ProjectTasklist
{
	private boolean			addByDefault	= false;
	private String			person			= null;
	private String			description		= null;
	private String			groupOrPIName	= null;
	private String			role			= null;
	/**
	 * {@link #task} : It is the name of the template. In the excel file it is
	 * under the column Project.
	 */
	private String			task			= null;
	private List<String>	templateList	= new ArrayList<String>();
	private Date			dueDate			= null;
	private String			status			= null;
	private Integer			numberOfTasks	= 1;
	private String			sampleType		= null;
	private String			sampleName		= null;
	private Date         modifiedTime;

	/**
	 * @return the addByDefault
	 */
	@XmlAttribute(name = "addByDefault", required = true)
	public boolean isAddByDefault()
	{
		return addByDefault;
	}

	/**
	 * @param addByDefault
	 *            the addByDefault to set
	 */
	public void setAddByDefault(boolean addByDefault)
	{
		this.addByDefault = addByDefault;
	}

	/**
	 * @return the person
	 */
	@XmlAttribute(name = "person", required = true)
	public String getPerson()
	{
		return person;
	}

	/**
	 * @param person
	 *            the person to set
	 */
	public void setPerson(String person)
	{
		this.person = person;
	}

	/**
	 * @return the person
	 */
	@XmlElement(name = "description", required = false)
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param person
	 *            the person to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the groupOrPIName
	 */
	@XmlElement(name = "groupOrPIName", required = false)
	public String getGroupOrPIName()
	{
		return groupOrPIName;
	}

	/**
	 * @param groupOrPIName
	 *            the groupOrPIName to set
	 */
	public void setGroupOrPIName(String groupOrPIName)
	{
		this.groupOrPIName = groupOrPIName;
	}

	/**
	 * @return the role
	 */
	@XmlElement(name = "role", required = false)
	public String getRole()
	{
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(String role)
	{
		this.role = role;
	}

	/**
	 * @return the task
	 */
	@XmlElement(name = "task", required = false)
	public String getTask()
	{
		return task;
	}

	/**
	 * @param task
	 *            the task to set
	 */
	public void setTask(String task)
	{
		this.task = task;
	}

	/**
	 * @return the dueDate
	 */
	@XmlElement(name = "dueDate", required = false)
	public Date getDueDate()
	{
		return dueDate;
	}

	/**
	 * @param dueDate
	 *            the dueDate to set
	 */
	public void setDueDate(Date dueDate)
	{
		this.dueDate = dueDate;
	}

	/**
	 * @return the status
	 */
	@XmlElement(name = "status", required = false)
	public String getStatus()
	{
		return status;
	}

	/**
	 * sets the status and changes the modification time of the task
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	@XmlElement(name = "numberOfTasks", required = true)
	public int getNumberOfTasks()
	{
		return numberOfTasks;
	}

	public void setNumberOfTasks(int numberOfTasks)
	{
		this.numberOfTasks = numberOfTasks;
	}

	@XmlElement(name = "sampleType", required = true)
	public String getSampleType()
	{
		return this.sampleType;
	}

	public void setSampleType(String a_sampleType)
	{
		this.sampleType = a_sampleType;
	}

	@XmlElement(name = "sampleName", required = true)
	public String getSampleName()
	{
		return this.sampleName;
	}

	public void setSampleName(String a_sampleName)
	{
		this.sampleName = a_sampleName;
	}

	@XmlTransient
	public ProjectTasklist getACopy()
	{
		ProjectTasklist projectTasklist = new ProjectTasklist();
		projectTasklist.setAddByDefault(addByDefault);
		projectTasklist.setDescription(description);
		if (dueDate != null)
			projectTasklist.setDueDate(new Date(dueDate.getTime()));
		projectTasklist.setGroupOrPIName(groupOrPIName);
		projectTasklist.setPerson(person);
		projectTasklist.setRole(role);
		projectTasklist.status = status;     // do not use setStatus since it changes modifiedTime
		projectTasklist.setTask(task);
		projectTasklist.setNumberOfTasks(numberOfTasks);
		return projectTasklist;
	}

	public boolean matches(ProjectTasklist projectTasklist)
	{
		return Objects.equals(task, projectTasklist.getTask()) && Objects.equals(person, projectTasklist.getPerson());
	}

	/**
	 * @return the task
	 */
	@XmlElement(name = "templateList", required = false)
	public List<String> getTemplateList()
	{
		return this.templateList;
	}
	
	public void setTemplateList(List<String> a_templateList)
	{
		this.templateList = a_templateList;
	}
	
	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
	@XmlAttribute
	public Date getModifiedTime() {
		return modifiedTime;
	}

}
