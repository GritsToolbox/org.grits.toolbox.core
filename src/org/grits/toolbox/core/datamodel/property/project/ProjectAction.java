/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name = "projectAction")
@XmlType(propOrder={"action", "addByDefault"})
public class ProjectAction
{
	private boolean addByDefault = false;
	private String action = null;

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
	 * @return the projectAction
	 */
	@XmlAttribute(name = "action", required= true)
	public String getAction()
	{
		return action;
	}

	/**
	 * @param projectAction the projectAction to set
	 */
	public void setAction(String action)
	{
		this.action = action;
	}

	@XmlTransient
	public ProjectAction getACopy()
	{
		ProjectAction projectAction = new ProjectAction();
		projectAction.setAction(action);
		projectAction.setAddByDefault(addByDefault);
		return projectAction;
	}
}
