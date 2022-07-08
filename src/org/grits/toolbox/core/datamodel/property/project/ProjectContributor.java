/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 */
public class ProjectContributor
{
    private String name = null;
    private List<ProjectContribution> contributions = new ArrayList<ProjectContribution>();

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public List<ProjectContribution> getContributions() {
		return contributions;
	}

    public void setContributions(List<ProjectContribution> contributions) {
		this.contributions = contributions;
	}

    public void addContribution(ProjectContribution contribution) {
		this.contributions.add(contribution);
	}
}
