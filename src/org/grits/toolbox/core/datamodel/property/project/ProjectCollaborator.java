/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

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
@XmlRootElement(name = "projectCollaborator")
@XmlType(propOrder={"name", "groupOrPIName",  
        "position", "department", 
        "institution", "address", "country", 
        "email", "phone", "fax", 
        "fundingAgency", "grantNumber", "addByDefault"})
public class ProjectCollaborator
{
    private boolean addByDefault = false;
    private String name = null;
    private String groupOrPIName = null;
    private String position = null;
    private String department = null;
    private String institution = null;
    private String address = null;
    private String country = null;
    private String email = null;
    private String phone = null;
    private String fax = null;
    private String fundingAgency = null;
    private String grantNumber = null;
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
     * @return the name
     */
    @XmlAttribute(name = "name", required= true)
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
    /**
     * @return the groupOrPIName
     */
    @XmlElement(name = "groupOrPIName", required= false)
    public String getGroupOrPIName()
    {
        return groupOrPIName;
    }
    /**
     * @param groupOrPIName the groupOrPIName to set
     */
    public void setGroupOrPIName(String groupOrPIName)
    {
        this.groupOrPIName = groupOrPIName;
    }
    /**
     * @return the position
     */
    @XmlElement(name = "position", required= false)
    public String getPosition()
    {
        return position;
    }
    /**
     * @param position the position to set
     */
    public void setPosition(String position)
    {
        this.position = position;
    }
    /**
     * @return the department
     */
    @XmlElement(name = "department", required= false)
    public String getDepartment()
    {
        return department;
    }
    /**
     * @param department the department to set
     */
    public void setDepartment(String department)
    {
        this.department = department;
    }
    /**
     * @return the institution
     */
    @XmlElement(name = "institution", required= false)
    public String getInstitution()
    {
        return institution;
    }
    /**
     * @param institution the institution to set
     */
    public void setInstitution(String institution)
    {
        this.institution = institution;
    }
    /**
     * @return the address
     */
    @XmlElement(name = "address", required= false)
    public String getAddress()
    {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(String address)
    {
        this.address = address;
    }
    /**
     * @return the country
     */
    @XmlElement(name = "country", required= false)
    public String getCountry()
    {
        return country;
    }
    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }
    /**
     * @return the email
     */
    @XmlElement(name = "email", required= false)
    public String getEmail()
    {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(String email)
    {
        this.email = email;
    }
    /**
     * @return the phone
     */
    @XmlElement(name = "phone", required= false)
    public String getPhone()
    {
        return phone;
    }
    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    /**
     * @return the fax
     */
    @XmlElement(name = "fax", required= false)
    public String getFax()
    {
        return fax;
    }
    /**
     * @param fax the fax to set
     */
    public void setFax(String fax)
    {
        this.fax = fax;
    }
    /**
     * @return the fundingAgency
     */
    @XmlElement(name = "fundingAgency", required= false)
    public String getFundingAgency()
    {
        return fundingAgency;
    }
    /**
     * @param fundingAgency the fundingAgency to set
     */
    public void setFundingAgency(String fundingAgency)
    {
        this.fundingAgency = fundingAgency;
    }
    /**
     * @return the grantNumber
     */
    @XmlElement(name = "grantNumber", required= false)
    public String getGrantNumber()
    {
        return grantNumber;
    }
    /**
     * @param grantNumber the grantNumber to set
     */
    public void setGrantNumber(String grantNumber)
    {
        this.grantNumber = grantNumber;
    }

    @XmlTransient
    public ProjectCollaborator getACopy()
    {
    	ProjectCollaborator projectCollaborator = new ProjectCollaborator();
    	projectCollaborator.setAddByDefault(addByDefault);
    	projectCollaborator.setAddress(address);
    	projectCollaborator.setCountry(country);
    	projectCollaborator.setDepartment(department);
    	projectCollaborator.setEmail(email);
    	projectCollaborator.setFax(fax);
    	projectCollaborator.setFundingAgency(fundingAgency);
    	projectCollaborator.setGrantNumber(grantNumber);
    	projectCollaborator.setGroupOrPIName(groupOrPIName);
    	projectCollaborator.setInstitution(institution);
    	projectCollaborator.setName(name);
    	projectCollaborator.setPhone(phone);
    	projectCollaborator.setPosition(position);
    	return projectCollaborator;
    }

    public boolean matches(ProjectCollaborator collaborator)
    {
    	return name.equals(collaborator.getName())
    			&& Objects.equals(fundingAgency, collaborator.getFundingAgency())
    			&& Objects.equals(grantNumber, collaborator.getGrantNumber());
    }
}
