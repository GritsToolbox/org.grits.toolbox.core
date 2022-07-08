package org.grits.toolbox.core.preference.share;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "preference")
@XmlType(propOrder={"name", "version", "value"})
public class PreferenceEntity
{
	public static final String CURRENT_VERSION = "1.0";
	private String name = null;
	private String version = null;
	private String value = null;

	public PreferenceEntity()
	{
		
	}

	/**
	 * creates PreferenceEntity object with default version, 
	 * i.e. with version as <code>CURRENT_VERSION = "1.0"</code>
	 * @param name
	 */
	public PreferenceEntity(String name)
	{
		this.name = name;
		this.version = CURRENT_VERSION;
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
	 * @return the version
	 */
	@XmlAttribute(name = "version", required= true)
	public String getVersion()
	{
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
	/**
	 * @return the value
	 */
	@XmlValue
	public String getValue()
	{
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
