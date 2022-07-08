package org.grits.toolbox.core;

import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(Application.class);

	/**
	 * Show workspace dialog to get a user's workspace location and 
	 * creates workspace history file and updates it.
	 */
	public Object start(IApplicationContext context)
	{
		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = 0;
			try
			{
				returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			}catch(Exception e)
			{
				logger.error("Error running the workbench", e);
			}
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		}
		finally {
			display.dispose();
		}
	}

	/**
	 * Stop the software
	 */
	public void stop()
	{
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}