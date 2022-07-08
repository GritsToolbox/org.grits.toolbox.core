package org.grits.toolbox.core.utils;

public class ProjectEntry {

	String name;
	Boolean open = true;
	
	public ProjectEntry(String name, boolean open) {
		this.name = name;
		this.open = open;
	}
	
	public Boolean getOpen() {
		return open;
	}
	public void setOpen(Boolean open) {
		this.open = open;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
