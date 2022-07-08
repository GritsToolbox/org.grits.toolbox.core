
package org.grits.toolbox.core.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.handler.OpenPreferenceHandler;
import org.grits.toolbox.core.part.provider.TasklistsLabelProvider;
import org.grits.toolbox.core.part.toolitem.SaveTask;
import org.grits.toolbox.core.preference.project.PersonPreference;
import org.grits.toolbox.core.preference.project.RolePreference;
import org.grits.toolbox.core.preference.project.StatusPreference;
import org.grits.toolbox.core.preference.project.TaskPreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

@SuppressWarnings("restriction")
public class TaskPart
{
	private static Logger logger = Logger.getLogger(TaskPart.class);

	public static final String PART_ID = "org.grits.toolbox.core.part.project.task";

	public static final String EVENT_TOPIC_VALUE_MODIFIED = "EventTopic_EventPart_Modified";

	private static final String ADD_NEW_TO_PREFERENCE = "Add New ...";

	private ComboViewer taskCombo = null;
	private ComboViewer nameCombo = null;
	private ComboViewer statusCombo = null;
	private CDateTime cDateTime = null;
	private ComboViewer roleCombo = null;
	private Text groupPIText = null;
	private Spinner numberOfTasksSpinner = null;
	private Text descriptionText = null;

	@Inject IEventBroker eventBroker;
	@Inject ECommandService commandService;
	@Inject EHandlerService handlerService;

	private ProjectTasklist projectTasklist = null;
	private MPart taskPart = null;

	@Inject
	public TaskPart()
	{

	}

	@Optional
	@Inject
	void setProjectTask(@UIEventTopic
			(ProjectEntryPart.EVENT_TOPIC_FIELD_SELECTION) ProjectTasklist projectTasklist,
			EPartService partService, EModelService modelService)
	{
		MPart taskPart = partService == null ? null : partService.findPart(PART_ID);
		if(taskPart != null && taskPart.getObject() != null)
		{
			clearAll();
			if(projectTasklist != null)
			{
				selectValueInCombo(taskCombo, projectTasklist.getTask());
				selectValueInCombo(nameCombo, projectTasklist.getPerson());
				selectValueInCombo(statusCombo, projectTasklist.getStatus());
				selectValueInCombo(roleCombo, projectTasklist.getRole());

				cDateTime.setSelection(projectTasklist.getDueDate());

				numberOfTasksSpinner.setSelection(projectTasklist.getNumberOfTasks());

				String value = projectTasklist.getGroupOrPIName() == null ?
						"" : projectTasklist.getGroupOrPIName();
				groupPIText.setText(value);
				value = projectTasklist.getDescription() == null ? "" : projectTasklist.getDescription();
				descriptionText.setText(value);

				this.taskPart = taskPart;
				this.projectTasklist  = projectTasklist;
				enableToolItem();
				makeEditable(true);
				modelService.bringToTop(taskPart);
			}
		}
	}

	private void enableToolItem()
	{
		((MDirectToolItem) taskPart.getToolbar()
				.getChildren().iterator().next()).setEnabled(
						SaveTask.isUnique(projectTasklist));
	}

	private void clearAll()
	{
		this.projectTasklist = null;

		taskCombo.getCombo().deselectAll();
		nameCombo.getCombo().deselectAll();
		statusCombo.getCombo().deselectAll();
		roleCombo.getCombo().deselectAll();
		cDateTime.setSelection(null);
		groupPIText.setText("");
		numberOfTasksSpinner.setSelection(numberOfTasksSpinner.getMinimum());
		descriptionText.setText("");

		makeEditable(false);
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.debug("START : Creating Tasklist View");
		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		layout.marginTop = 30;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		taskCombo = createComboLine(composite, "Task");
		taskCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(taskCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.TASK).getAllValues());
		taskCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(projectTasklist != null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String task = (String) selection.getFirstElement();
						if(Objects.equals(task, ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									TaskPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(taskCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.TASK).getAllValues());

							taskCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(taskCombo, TaskPreference.lastSelection);
							if(comboSelection >= 0)
								taskCombo.setSelection(new StructuredSelection(TaskPreference.lastSelection));
						}
						else if(!Objects.equals(projectTasklist.getTask(), task))
						{
							projectTasklist.setTask(task);
							enableToolItem();
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
						}
					}
				}
			}
		});

		nameCombo = createComboLine(composite, "Person");
		nameCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(nameCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.PERSON).getAllValues());
		nameCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(projectTasklist != null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String name = (String) selection.getFirstElement();
						if(Objects.equals(name, ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									PersonPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(nameCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.PERSON).getAllValues());

							nameCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(nameCombo, PersonPreference.lastSelection);
							if(comboSelection >= 0)
								nameCombo.setSelection(new StructuredSelection(PersonPreference.lastSelection));
						}
						else if(!Objects.equals(projectTasklist.getPerson(), name))
						{
							projectTasklist.setPerson(name);
							enableToolItem();
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
						}
					}
				}
			}
		});

		statusCombo = createComboLine(composite, "Status");
		statusCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(statusCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.STATUS).getAllValues());
		statusCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(projectTasklist != null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String status = (String) selection.getFirstElement();
						if(Objects.equals(status, ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									StatusPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(statusCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.STATUS).getAllValues());

							statusCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(statusCombo, StatusPreference.lastSelection);
							if(comboSelection >= 0)
								statusCombo.setSelection(new StructuredSelection(StatusPreference.lastSelection));
						}
						else if(!Objects.equals(projectTasklist.getStatus(), status))
						{
							projectTasklist.setStatus(status);
							projectTasklist.setModifiedTime(new Date());
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
						}
					}
				}
			}
		});

		createLabel(composite, "Date");
		cDateTime = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN);
		cDateTime.setPattern(TasklistsLabelProvider.DATE_FORMAT);
		GridData calendarComboData = new GridData(GridData.FILL_HORIZONTAL);
		calendarComboData.grabExcessHorizontalSpace = true;
		calendarComboData.horizontalSpan = 1;
		calendarComboData.verticalSpan = 1;
		cDateTime.setLayoutData(calendarComboData);
		cDateTime.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				changeDate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				changeDate();
			}

			private void changeDate()
			{
				if(projectTasklist != null)
				{
					if(!Objects.equals(projectTasklist.getDueDate(), cDateTime.getSelection()))
					{
						projectTasklist.setDueDate(cDateTime.getSelection());
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
					}
				}
			}
		});

		roleCombo = createComboLine(composite, "Role");
		roleCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(roleCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.ROLE).getAllValues());
		roleCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(projectTasklist != null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String role = (String) selection.getFirstElement();
						if(Objects.equals(role, ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
									RolePreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(roleCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.ROLE).getAllValues());

							roleCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(roleCombo, RolePreference.lastSelection);
							if(comboSelection >= 0)
								roleCombo.setSelection(new StructuredSelection(RolePreference.lastSelection));
						}
						else if(!Objects.equals(projectTasklist.getRole(), role))
						{
							projectTasklist.setRole(role);
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
						}
					}
				}
			}
		});

		groupPIText = createTextLine(composite, "Group/P.I.", SWT.BORDER, 20);
		groupPIText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(projectTasklist != null)
				{
					String groupOrPIName = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(projectTasklist.getGroupOrPIName(), groupOrPIName))
					{
						projectTasklist.setGroupOrPIName(groupOrPIName);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
					}
				}
			}
		});

		createLabel(composite, "# of Tasks");
		numberOfTasksSpinner = new Spinner(composite, SWT.BORDER);
		numberOfTasksSpinner.setMinimum(1);
		GridData spinnerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		spinnerLayoutData.grabExcessHorizontalSpace = true;
		spinnerLayoutData.horizontalSpan = 1;
		spinnerLayoutData.verticalSpan = 1;
		numberOfTasksSpinner.setLayoutData(spinnerLayoutData);
		numberOfTasksSpinner.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setNumberOfTasks();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				setNumberOfTasks();
			}

			private void setNumberOfTasks()
			{
				if(projectTasklist != null)
				{
					if(projectTasklist.getNumberOfTasks() !=
							numberOfTasksSpinner.getSelection())
					{
						projectTasklist.setNumberOfTasks(numberOfTasksSpinner.getSelection());
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
					}
				}
			}
		});

		descriptionText = createTextLine(composite, "Description", 
				SWT.BORDER|SWT.WRAP|SWT.H_SCROLL|SWT.V_SCROLL, 350);
		descriptionText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if(projectTasklist != null)
				{
					String description = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(projectTasklist.getDescription(), description))
					{
						projectTasklist.setDescription(description);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectTasklist);
					}
				}
			}
		});

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.grabExcessVerticalSpace = false;
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);

		makeEditable(false);

		logger.debug("END   : Creating Tasklist View");
	}

	private ComboViewer createComboLine(Composite composite, String label)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		comboViewer.getCombo().setLayoutData(newTasklistData);
		return comboViewer;
	}

	private void setInputInCombo(ComboViewer comboViewer, Set<String> values) 
	{
		List<String> valueList = new ArrayList<String>();
		valueList.addAll(values);
		Collections.sort(valueList);
		String[] items = new String[valueList.size() + 1];
		int i = 0;
		for(String value : valueList)
		{
			items[i++] = value;
		}
		items[i] = ProjectEntryPart.ADD_NEW_TO_PREFERENCE;
		comboViewer.setInput(items);
	}

	private void selectValueInCombo(ComboViewer comboViewer, String value)
	{
		comboViewer.getCombo().deselectAll();
		if(value != null)
		{
			int selectionIndex = getIndexOf(comboViewer, value);
			if(selectionIndex >= 0)
			{
				comboViewer.getCombo().select(selectionIndex);
				comboViewer.setSelection(comboViewer.getSelection());
			}
		}
	}

	private int getIndexOf(ComboViewer comboViewer, String value)
	{
		int selectionIndex = -1;
		if(comboViewer != null 
				&& comboViewer.getCombo().getItems() != null && value != null)
		{
			selectionIndex = comboViewer.getCombo().indexOf(value);
		}
		return selectionIndex;
	}

	private void createLabel(Composite composite, String label)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
						| GridData.VERTICAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);
	}

	private Text createTextLine(Composite composite, String label, 
			int style, int heightHint)
	{
		createLabel(composite, label);
		Text text = new Text(composite, style);
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		newTasklistData.heightHint = heightHint;
		text.setLayoutData(newTasklistData);
		return text;
	}

	private void makeEditable(boolean editable)
	{
		taskCombo.getCombo().setEnabled(editable);
		nameCombo.getCombo().setEnabled(editable);
		statusCombo.getCombo().setEnabled(editable);
		roleCombo.getCombo().setEnabled(editable);
		cDateTime.setEnabled(editable);
		groupPIText.setEnabled(editable);
		numberOfTasksSpinner.setEnabled(editable);
		descriptionText.setEnabled(editable);
	}

	@PreDestroy
	public void preDestroy()
	{

	}

	@Focus
	public void onFocus()
	{

	}

	public ProjectTasklist getProjectTasklist()
	{
		return projectTasklist;
	}
}