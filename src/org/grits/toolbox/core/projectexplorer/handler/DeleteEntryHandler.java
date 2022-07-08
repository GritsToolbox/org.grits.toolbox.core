
package org.grits.toolbox.core.projectexplorer.handler;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.editor.EditorHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;

@SuppressWarnings("deprecation")
public class DeleteEntryHandler
{
	private static final Logger logger = Logger.getLogger(DeleteEntryHandler.class);
	private boolean lastDecision = false;
	private boolean rememberDecision = false;
	private boolean cancelPressed = false;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IGritsDataModelService gritsDataModelService, IGritsUIService gritsUIService)
	{
		if(object instanceof StructuredSelection)
		{
			Iterator<?> iterator = ((StructuredSelection) object).iterator();
			Object nextSelection = null;
			Entry entry = null;
			while(iterator.hasNext() && !cancelPressed)
			{
				nextSelection = iterator.next();
				if(nextSelection instanceof Entry)
				{
					entry = (Entry) nextSelection;
					if(confirmDeleteEntry(entry))
					{
						delete(gritsDataModelService, gritsUIService, entry);
					}
				}
			}
		}
		else if(object instanceof Entry)
		{
			Entry entry = (Entry) object;
			if(confirmDeleteEntry(entry))
			{
				delete(gritsDataModelService, gritsUIService, entry);
			}
		}
//		if(projectExplorerPart != null && projectExplorerPart.getObject() != null)
//		{
//			((ProjectExplorerPart) projectExplorerPart.getObject()).getTreeViewer().refresh();
//		}
		cancelPressed = false;
		rememberDecision = false;
		lastDecision = false;
	}

	private void delete(IGritsDataModelService gritsDataModelService, IGritsUIService gritsUIService, Entry entry)
	{
		closeAllParts(entry, gritsUIService);
		try
		{
			if(ProjectProperty.TYPE.equals(entry.getProperty().getType()))
			{
				try
				{
					WorkspaceXMLHandler.removeEntry(entry.getDisplayName());
				} catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(
							Display.getCurrent().getActiveShell(), "Unable to update workspace file",e);
				}
			}
			else
			{
				try
				{
					ProjectFileHandler.deleteEntryFromProject(entry);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(
							Display.getCurrent().getActiveShell(), "Unable to update project xml",e);
				}
			}
			gritsDataModelService.deleteEntry(entry);
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(),
					"Unable to delete entry " + entry.getDisplayName(), e1);
		}
	}

	private void closeAllParts(Entry entry, IGritsUIService gritsUIService)
	{
		for(Entry childEntry : entry.getChildren())
		{
			closeAllParts(childEntry, gritsUIService);
		}
		closePartForEntry(entry, gritsUIService);
	}

	private void closePartForEntry(Entry entry, IGritsUIService gritsUIService)
	{
		if(!gritsUIService.closePartForEntry(entry)
				&& gritsUIService.findPartDescriptorForEntry(entry) == null)
		{
				handleOldStuff(entry);
		}
	}

	private void handleOldStuff(Entry entry)
	{
		EditorHandler.closeEditorsForEntry(entry);
	}

	private boolean confirmDeleteEntry(Entry entry)
	{
		if(!rememberDecision)
		{
			int result = ErrorUtils.createMultiConfirmationMessageBoxReturn(Display.getCurrent().getActiveShell(),
					"Warning", "\"" + entry.getDisplayName() 
					+ "\" will be deleted. Do you still want to proceed?\n", rememberDecision);

			if(result == SWT.OK)
			{
				return true;
			}
			else if (result == (SWT.OK | SWT.YES))
			{
				rememberDecision = true;
				return lastDecision = true;
			}
			else if (result == (SWT.NO | SWT.YES))
			{
				rememberDecision = true;
				return lastDecision = false;
			}
			else if(result == SWT.CANCEL)
			{
				cancelPressed = true;
				return false;
			}
		}
		return lastDecision;
	}

	private static void showMessage(String title, String errorMessage)
	{
		MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.OK);
		messageBox.setText(title);
		messageBox.setMessage(title + "\n\n" + errorMessage);
		messageBox.open();
	}

	static void removeProject(IGritsDataModelService gritsDataModelService, Entry projectEntry)
	{
		try
		{
			gritsDataModelService.removeAllChildren(projectEntry);
			WorkspaceXMLHandler.removeEntry(projectEntry.getDisplayName());
		} catch (Exception e)
		{
			logger.fatal("Error Removing the project" + "\n" + e.getMessage(), e);
			showMessage("Error Removing the project", e.getMessage());
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object)
	{
		return object instanceof Entry || object instanceof StructuredSelection;
	}
}