package org.grits.toolbox.core.projectexplorer.filter;

import java.util.List;

public class KeywordFilter extends ProjectFilter {
	List<String> keywords;
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
}
