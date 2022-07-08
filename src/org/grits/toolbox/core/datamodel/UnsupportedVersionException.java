/**
 * 
 */
package org.grits.toolbox.core.datamodel;

public class UnsupportedVersionException extends Exception
{
	private static final long serialVersionUID = 1L;
	private String versionNumber = null;

	/**
	 * It returns the version number that is nut supported, can be null
	 * @return version number that is not supported
	 */
	public String getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * @param message for throwing exception
	 * @param versionNumber the version that is not supported
	 */
	public UnsupportedVersionException(String message, String versionNumber)
	{
		super(message);
		this.versionNumber = versionNumber;
	}

	/**
	 * 
	 * @param exception that is wrapped
	 * @param versionNumber the version that is not supported
	 */
	public UnsupportedVersionException(Exception exception, String versionNumber)
	{
		super(exception);
		this.versionNumber = versionNumber;
	}

	/**
	 * 
	 * @param message for throwing exception
	 * @param exception that is wrapped
	 * @param versionNumber the version that is not supported
	 */
	public UnsupportedVersionException(String message, Exception exception, String versionNumber)
	{
		super(message, exception);
		this.versionNumber = versionNumber;
	}
}
