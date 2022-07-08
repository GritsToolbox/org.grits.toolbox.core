/**
 * 
 */
package org.grits.toolbox.core.datamodel.io;

public class MissingReaderException extends Exception
{
	private static final long serialVersionUID = 1L;

	public MissingReaderException(String message)
	{
		super(message);
	}

	public MissingReaderException(Exception exception)
	{
		super(exception);
	}

	public MissingReaderException(String message, Exception exception)
	{
		super(message, exception);
	}
}
