package org.grits.toolbox.core.workspace;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="workspace_list")
public class WorkspaceHistory {

	List<WorkspaceHistoryEntry> workspaceList = new ArrayList<>();
	
	@XmlElement(name="workspace")
	public List<WorkspaceHistoryEntry> getWorkspaceList() {
		return workspaceList;
	}
	
	public void setWorkspaceList(List<WorkspaceHistoryEntry> workspaceList) {
		this.workspaceList = workspaceList;
	}
}
