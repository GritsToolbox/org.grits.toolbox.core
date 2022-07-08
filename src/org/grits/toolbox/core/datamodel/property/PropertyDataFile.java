package org.grits.toolbox.core.datamodel.property;

import javax.xml.bind.annotation.XmlAttribute;

public class PropertyDataFile
{
	public static final String DEFAULT_VERSION = "1.0";
	public static final String DEFAULT_TYPE = "default";
	private String name = null;
	private String version = null;
	private String type = null;

	public PropertyDataFile()
	{
		
	}

	public PropertyDataFile(String name)
	{
		this.name = name;
		this.version = DEFAULT_VERSION;
		this.type = DEFAULT_TYPE;
	}

	public PropertyDataFile(String name, String version, String type)
	{
		this.name = name;
		this.version = version;
		this.type = type;
	}

	/**
	 * @return the name
	 */
	@XmlAttribute
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
	@XmlAttribute
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
	 * @return the type
	 */
	@XmlAttribute
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

}
