/**
 * 
 */
package org.grits.toolbox.core.datamodel;

public class UnsupportedTypeException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4423740474144821093L;
	//	private static final long serialVersionUID = 2L;
	private String type = null;

	/**
	 * It returns the type that is nut supported, can be null
	 * @return type number that is not supported
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param message for throwing exception
	 * @param type the type that is not supported
	 */
	public UnsupportedTypeException(String message, String type)
	{
		super(message);
		this.type = type;
	}

	/**
	 * 
	 * @param exception that is wrapped
	 * @param type the type that is not supported
	 */
	public UnsupportedTypeException(Exception exception, String type)
	{
		super(exception);
		this.type = type;
	}

	/**
	 * 
	 * @param message for throwing exception
	 * @param exception that is wrapped
	 * @param type the type that is not supported
	 */
	public UnsupportedTypeException(String message, Exception exception, String type)
	{
		super(message, exception);
		this.type = type;
	}
}
