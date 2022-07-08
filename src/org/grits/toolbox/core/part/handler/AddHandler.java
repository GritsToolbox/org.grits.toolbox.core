
package org.grits.toolbox.core.part.handler;

import java.util.Date;
import java.util.List;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;
import org.grits.toolbox.core.datamodel.property.project.ProjectAction;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.part.AddTaskListDialog;
import org.grits.toolbox.core.part.ProjectEntryPart;
import org.grits.toolbox.core.preference.share.MultiChoicePreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.core.wizard.addcollaborator.AddCollaboratorWizard;

public class AddHandler
{
	private static final Logger logger = Logger.getLogger(AddHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.core.command.entry.project.table.add";
	public static final String PARAMETER_ADD_TYPE =
			"org.grits.toolbox.core.commandparameter.addtotable.type";

	private static MultiChoicePreference<ProjectEvent> eventPreference =
			ProjectPreferenceStore.getMultiChoicePreference(
					ProjectPreferenceStore.ParameterizedPreference.EVENT);

	@Execute
	public void execute(ESelectionService eSelectionService,
			@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named(PARAMETER_ADD_TYPE) String addType)
	{
		if(ProjectEntryPart.PART_ID.equals(part.getElementId()) &&
				eSelectionService.getSelection() instanceof SelectionEvent &&
				((SelectionEvent) eSelectionService.getSelection()).getSource() instanceof ToolItem)
		{
			ToolItem item = (ToolItem) ((SelectionEvent) eSelectionService.getSelection()).getSource();
			TableViewer tableViewer = (TableViewer) item.getParent().getData();
			if(ProjectCollaborator.class.getName().equals(addType))
			{
				logger.info("START : Adding a new Collaborator");
				@SuppressWarnings("unchecked")
				List<ProjectCollaborator> collaborators = 
						(List<ProjectCollaborator>) tableViewer.getInput();

				AddCollaboratorWizard wizard  = new AddCollaboratorWizard();
				wizard.setWindowTitle("Add Collaborator");
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				if(dialog.open() == Window.OK)
				{
					List<ProjectCollaborator> selectedCollaborators = wizard.getCollaborators();
					if(!selectedCollaborators.isEmpty())
					{
						ProjectCollaborator lastAddedCollaborator = null;
						for(ProjectCollaborator newCollaborator : selectedCollaborators)
						{
							collaborators.add(newCollaborator);
							lastAddedCollaborator = newCollaborator;
						}
						tableViewer.refresh();
						part.setDirty(true);
						tableViewer.setSelection(new StructuredSelection(lastAddedCollaborator));
					}
				}
				logger.info("END   : Adding a new Collaborator");
			}
			else if(ProjectTasklist.class.getName().equals(addType))
			{
				logger.info("START : Adding new Tasklist");
				@SuppressWarnings("unchecked")
				List<ProjectTasklist> tasklists = 
						(List<ProjectTasklist>) tableViewer.getInput();

				AddTaskListDialog addTaskDialog = 
						new AddTaskListDialog(Display.getCurrent().getActiveShell());
				if(addTaskDialog.open() == Window.OK)
				{
					if(!addTaskDialog.getTaskLists().isEmpty())
					{
						ProjectTasklist lastAddition = null;
						for(ProjectTasklist tasklist : addTaskDialog.getTaskLists())
						{
							tasklists.add(tasklist);
							lastAddition = tasklist;
						}
						tableViewer.refresh();
						part.setDirty(true);
						tableViewer.setSelection(new StructuredSelection(lastAddition));
					}
				}
				logger.info("END   : Adding new Tasklist");
			}
			else if(ProjectEvent.class.getName().equals(addType))
			{
				logger.info("START : Adding a new Project Event");

				@SuppressWarnings("unchecked")
				List<ProjectEvent> events = 
						(List<ProjectEvent>) tableViewer.getInput();

				ProjectEvent newEvent = null;
				if(!eventPreference.getSelectedValues().isEmpty())
				{
					newEvent = eventPreference.getSelectedValues().iterator().next().getACopy();
				}
				else if(!eventPreference.getOtherValues().isEmpty())
				{
					newEvent = eventPreference.getOtherValues().iterator().next().getACopy();
				}
				else
				{
					// create an event from default action or other actions
					SingleChoicePreference actionPreference =
							ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.ACTION);
					if(actionPreference.getDefaultValue() != null)
					{
						newEvent = new ProjectEvent();
						ProjectAction newAction = new ProjectAction();
						newAction.setAction(actionPreference.getDefaultValue());
						newEvent.setProjectAction(newAction);
					}
					else if(!actionPreference.getAllValues().isEmpty())
					{
						newEvent = new ProjectEvent();
						ProjectAction newAction = new ProjectAction();
						newAction.setAction(actionPreference.getAllValues().iterator().next());
						newEvent.setProjectAction(newAction);
					}
				}

				if(newEvent != null)
				{
					newEvent.setEventDate(new Date());
					events.add(newEvent);
					part.setDirty(true);
					tableViewer.refresh();
					tableViewer.setSelection(new StructuredSelection(newEvent));
				}
				else
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							"No Project Event/Action", 
							"There are no set values for Project Event/Action currently to add."
									+ " Please add them first in your workspace preference.");
				}
				logger.info("END   : Adding a new Project Event");
			}
		}
	}

}