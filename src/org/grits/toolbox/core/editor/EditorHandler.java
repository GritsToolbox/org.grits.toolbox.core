package org.grits.toolbox.core.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.grits.toolbox.core.Activator;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.impl.GritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utilShare.SaveUtils;
/**
 * @deprecated
 * use {@link GritsUIService} methods for opening, closing part etc.
 */
@Deprecated
public class EditorHandler
{
	private static final Logger logger = Logger.getLogger(EditorHandler.class);

	@Inject static private IEventBroker eventBroker;

	/**
	 * 
	 * @param a_entry
	 * @param forceClose if set to true, it will close the existing editor, if exits (without saving) before opening it again
	 * @return ID of the editor used to display the entry, null if no editor can be found 
	 * @throws PartInitException
	 */
	private static String openInEditor(Entry a_entry, boolean forceClose) throws PartInitException 
	{
		//get active window
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		ViewInput input = new ViewInput();
		input.setEntry(a_entry);

		//takes editorId and editorInput.
		String t_editorId = EditorHandler.findEditorId(a_entry);
		if ( t_editorId != null )
		{
			if (t_editorId.contains("experimentdesigner"))
			{
				// switch to the correct perspective
				logger.debug("Switching into org.grits.toolbox.editor.experimentdesigner.designPerspective1" );
				String perspectiveId = "org.grits.toolbox.editor.experimentdesigner.designPerspective1";
				try
				{
					PlatformUI.getWorkbench().showPerspective(
							perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e)
				{
					logger.error(e.getMessage(), e);
					PropertyHandler.changePerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
					throw new PartInitException(e.getMessage(), e);
				}
			}
			else if (t_editorId.contains("glycanarray"))
			{
				// switch to the correct perspective
				String perspectiveId = "uk.ac.imperial.glycosciences.glycanarray.perspective";
				try
				{
					PlatformUI.getWorkbench().showPerspective(
							perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e)
				{
					logger.error(e.getMessage(), e);
					PropertyHandler.changePerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
					throw new PartInitException(e.getMessage(), e);
				}
			}
			else if (t_editorId.contains("qrtpcr"))
			{
				// switch to the correct perspective
				String perspectiveId = "org.grits.toolbox.entry.qrtpcr.perspective";
				try
				{
					PlatformUI.getWorkbench().showPerspective(
							perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e)
				{
					logger.error(e.getMessage(), e);
					PropertyHandler.changePerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
					throw new PartInitException(e.getMessage(), e);
				}
			}
			else
			{
				// switch to default perspective
				PropertyHandler.changePerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);
			}
			if (forceClose) {
				IEditorPart editor = page.findEditor(input);
				if (editor != null) 
					page.closeEditor(editor, false);   // ignore all changes
			}
			IEditorPart editorPart = page.openEditor(input, t_editorId, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
			if( editorPart instanceof ICancelableEditor ) {
				if( ( (ICancelableEditor) editorPart).isCanceled() ) {
					IEditorPart editor = page.findEditor(input);
					page.closeEditor(editor, false);   // ignore all changes
					return ICancelableEditor.CANCELED_EDITOR_ID;
				}
			}

		}
		return t_editorId;
	}

	private static String findEditorId(Entry a_entry)
	{
		String t_editorId = null;
		IExtension[] t_extentions = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,"entryeditor").getExtensions();
		for (IExtension t_extention : t_extentions)
		{
			IConfigurationElement[] t_configElements = t_extention.getConfigurationElements();
			for (IConfigurationElement t_configurationElement : t_configElements)
			{
				if ( t_configurationElement.getName().equals("editor") )
				{
					if ( t_configurationElement.getAttribute("propertyId") == null )
					{
						logger.fatal("entry editor misses propertyId attibute: " + t_configurationElement.toString());
					}
					else if ( t_configurationElement.getAttribute("propertyId").equals(a_entry.getProperty().getType()) )
					{
						String t_newEditorId = t_configurationElement.getAttribute("editorId");
						if ( t_newEditorId == null )
						{
							logger.fatal("entry editor misses editorId attibute: " + t_configurationElement.toString());
						}
						else if ( a_entry.getLastEditorId() != null && a_entry.getLastEditorId().equals(t_editorId) )
						{
							return t_newEditorId;
						}
						else 
						{
							String t_defaultAttribute = t_configurationElement.getAttribute("default");
							boolean t_newDefault = false;
							if ( t_defaultAttribute != null )
							{
								if ( t_defaultAttribute.equalsIgnoreCase("true") )
								{
									t_newDefault = true;
								}
							}
							if ( t_newDefault )
							{
								t_editorId = t_newEditorId;
							}
							else if ( t_editorId == null )
							{
								t_editorId = t_newEditorId;
							}
						}
					}
				}
			}
		}
		return t_editorId;
	}

	/**
	 * 
	 * @param a_entry
	 * @deprecated use {@link GritsUIService#openEntryInPart(Entry)} instead
	 */
	public static void openEditorForEntry(Entry a_entry) {
		openEditorForEntry(a_entry, false);
	}

	/**
	 * 
	 * @param a_entry
	 * @param forceClose
	 * @deprecated use {@link GritsUIService#openEntryInPart(Entry)} instead
	 */
	public static void openEditorForEntry(Entry a_entry, boolean forceClose)
	{
		try
		{
			String t_editor = EditorHandler.openInEditor(a_entry, forceClose);
			if ( t_editor == null )
			{
				logger.debug("Unable to find editor for property type: " + a_entry.getProperty().getType());
				ErrorUtils.createErrorMessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Unable to find editor for property type: " + a_entry.getProperty().getType());
			} else if ( t_editor.equals(ICancelableEditor.CANCELED_EDITOR_ID) ) {
				return;
			}
			else
			{
				a_entry.setLastEditorId(t_editor);
				try 
				{
					SaveUtils.saveProjectWithEntry(a_entry);
				} 
				catch (IOException e) 
				{
					logger.error("Error updating the editor: " + e.getMessage(),e);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
			ErrorUtils.createErrorMessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), e.getMessage(),e);
		}
	}

	/**
	 * 
	 * @param a_entry
	 */
	public static void selectEntry(Entry a_entry)
	{
		try
		{
			if(a_entry != null)
			{
				eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, a_entry);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
			ErrorUtils.createErrorMessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), e.getMessage(),e);
		}
	}

	/**
	 * @deprecated use {@link GritsUIService#closePartForEntry(Entry)} instead
	 * @param entry
	 */
	public static void closeEditorsForEntry(Entry entry)
	{
		//need to close all related children in editor
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		List<IEditorReference> toCloseEditor = new ArrayList<IEditorReference>();
		for(IEditorReference ref: page.getEditorReferences())
		{
			try {
				ViewInput input = (ViewInput)ref.getEditorInput();
				Entry found = DataModelSearch.findParentByType(
						input.getEntry(), entry.getProperty().getType());
				if(found != null)
				{
					if(found.equals(entry))
					{
						toCloseEditor.add(ref);
					}
				}
			} catch (PartInitException e) {
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), e.getMessage(),e);
			}
		}

		IEditorReference[] closeEditor = new IEditorReference[toCloseEditor.size()];
		toCloseEditor.toArray(closeEditor);

		//close all
		page.closeEditors(closeEditor, true);
	}
}
