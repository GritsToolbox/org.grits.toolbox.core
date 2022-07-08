/**
 * 
 */
package org.grits.toolbox.core.datamodel.property;

/**
 * 
 *
 */
public class NotImplementedException extends Exception
{
	private static final long serialVersionUID = 1L;

	public NotImplementedException(String message)
	{
		super(message);
	}

	public NotImplementedException(Exception exception)
	{
		super(exception);
	}

	public NotImplementedException(String message, Exception exception)
	{
		super(message, exception);
	}
}
