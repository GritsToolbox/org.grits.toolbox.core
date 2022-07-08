package org.grits.toolbox.core.utilShare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.dialog.MultiConfirmationDialog;

public class ErrorUtils
{

    /**
     * Create an error msg with title and error msg from exception
     * 
     * @param parent
     *            of the error message box
     * @param errmsg
     *            for title
     * @param e
     *            error object
     */
    public static void createErrorMessageBox(Shell shell, String errmsg, Exception e)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText(errmsg);
        messageBox.setMessage(errmsg + "\nReason: " + e.getMessage());
        messageBox.open();
    }

    public static void createErrorMessageBox(Shell shell, String errmsg)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText(errmsg);
        messageBox.setMessage(errmsg);
        messageBox.open();
    }

    public static void createErrorMessageBox(Shell shell, String title, String errmsg)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText(title);
        messageBox.setMessage(errmsg);
        messageBox.open();
    }

    public static void createWarningMessageBox(Shell shell, String errtitle, String errormsg)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.OK & (~SWT.CLOSE));
        messageBox.setText(errtitle);
        messageBox.setMessage("\nReason: " + errormsg);
        messageBox.open();
    }

    public static void createSimpleMessageBox(Shell shell, String title, String message)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.OK & (~SWT.CLOSE));
        messageBox.setText(title);
        messageBox.setMessage(message);
        messageBox.open();
    }

    public static int createErrorMessageBoxReturn(Shell shell, String errmsg, Exception e)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.OK & (~SWT.CLOSE));
        messageBox.setText(errmsg);
        if (e.getMessage() == null)
        {
            messageBox.setMessage("Argument cannot be null");
        }
        else
        {
            messageBox.setMessage(e.getMessage());
        }
        int response = messageBox.open();
        if (response == SWT.OK)
        {
            // need to keep going
            return 1;
        }
        // then close the program
        return 1;
    }

    /**
     * 1-OK button; 0-Close button
     * 
     * @param shell
     * @param errmsg
     * @param e
     * @return
     */
    public static int createMessageBoxReturn(Shell shell, String errmsg, String msg)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL);
        messageBox.setText(errmsg);
        messageBox.setMessage(msg);
        int response = messageBox.open();
        if (response == SWT.OK)
        {
            // need to keep going
            return 1;
        }
        // then close the program
        return 0;
    }

    public static int createMultiConfirmationMessageBoxReturn(Shell shell, String errmsg, String msg, boolean bSelected)
    {
        MultiConfirmationDialog dialog = new MultiConfirmationDialog(shell);
        dialog.setMessage(msg);
        dialog.setChecked(bSelected);
        dialog.setTitle(errmsg);
        int response = dialog.open();
        return response;
    }

    public static int createSingleConfirmationMessageBoxReturn(Shell shell, String errmsg, String msg)
    {
        MessageBox dialog = new MessageBox(shell, SWT.YES | SWT.NO);
        dialog.setMessage(msg);
        dialog.setText(errmsg);
        int response = dialog.open();
        return response;
    }

}
