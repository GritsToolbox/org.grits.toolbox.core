/**
 * 
 */
package org.grits.toolbox.core.utils;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 *
 */
public class FileCopyProgressDialog
{
	private static Logger logger = Logger.getLogger(FileCopyProgressDialog.class);
	public ProgressBar progressBar = null;
	private long totalByteCopied = 0;
	protected long totalSize = 100l;

	public FileCopyProgressDialog(File fileToArchive)
	{
		if(fileToArchive != null && fileToArchive.exists())
		{
			init();
			totalSize = getFileSize(fileToArchive);
			logger.debug(totalSize);
		}
	}

	private long getFileSize(File file)
	{
		if(file.isFile())
			return file.length();
		else if(file.isDirectory())
		{
			long totalBytes = 0;
			for(File childFile : file.listFiles())
			{
				totalBytes += childFile.isFile() 
						? childFile.length() : getFileSize(childFile);
			}
			return totalBytes;
		}
		else return 0;
	}

	public void init()
	{
		Shell shell = new Shell(Display.getCurrent().getActiveShell());
		shell.setLocation(600, 400);
		progressBar  = new ProgressBar(shell, SWT.SMOOTH);
		progressBar.getShell().setSize(330, 80);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		progressBar.getShell().setText("File Copy (Progress " + 0 + "%)");
		progressBar.setBounds(10, 10, 300, 20);
	}

	public void setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
	}

	public long getCurrentProgress()
	{
		return totalByteCopied;
	}

	public void openProgressBar()
	{
		logger.debug("Opening progress bar");
		progressBar.getShell().open();
	}

	private final String[] copyDots = {".   ", "..  ", "... "};
	public void updateProgressBar(long byteIncrement)
	{
		totalByteCopied += byteIncrement;
		logger.debug("total increments " + totalByteCopied);
		progressBar.getDisplay().syncExec(new Runnable()
		{

			@Override
			public void run() {
				if (!progressBar.isDisposed())
				{
					int copyPercentage = (int) (100*((float) totalByteCopied/totalSize ));
					progressBar.setSelection(copyPercentage);
					progressBar.getShell().setText("File Copy (Copying" + copyDots[copyPercentage%3] + 
							+ copyPercentage + "%)");
					logger.debug(copyPercentage + "%");
				}
			}
		});
	}

	public void closeProgressBar()
	{
		logger.debug("Disposing progress bar");
		progressBar.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if(!progressBar.getShell().isDisposed())
				{
					progressBar.getShell().dispose();
				}
			}
		});
	}
}
