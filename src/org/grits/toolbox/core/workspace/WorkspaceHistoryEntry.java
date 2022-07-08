package org.grits.toolbox.core.workspace;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class provides information in the history.xml file
 * @author Ki Tae Myoung
 *
 */
public class WorkspaceHistoryEntry {
	//path of a user workspace
	private String path;
	//True or False
	private String lastActive;
	
	public void setPath(String path) {
		this.path = path;
	}
	@XmlAttribute
	public String getPath() {
		return path;
	}
	public void setLastActive(String lastActive) {
		this.lastActive = lastActive;
	}
	@XmlAttribute(name="last_active")
	public String getLastActive() {
		return lastActive;
	}
	
}
