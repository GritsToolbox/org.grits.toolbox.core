/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.util.Date;

/**
 * 
 *
 */
public class ProjectContribution
{
    public static final String DATE_FORMAT = "yyyy/MM/dd";
    private String projectName = null;
    private String projectCollaborator = null;
    private String groupPI = null;
    private String role = null;    
    private int numberOfTasks = 0;    
    private String task = null;
    private Date dueDate = null;
    private String status = null;

    public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectCollaborator() {
		return projectCollaborator;
	}

	public void setProjectCollaborator(String projectCollaborator) {
		this.projectCollaborator = projectCollaborator;
	}

	public int getNumberOfTasks() {
		return numberOfTasks;
	}

	public void setNumberOfTasks(int numberOfTasks) {
		this.numberOfTasks = numberOfTasks;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGroupPI() {
		return groupPI;
	}

	public void setGroupPI(String groupPI) {
		this.groupPI = groupPI;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
