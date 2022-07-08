
package org.grits.toolbox.core.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.handler.OpenPreferenceHandler;
import org.grits.toolbox.core.part.provider.EventsLabelProvider;
import org.grits.toolbox.core.preference.project.ActionPreference;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

@SuppressWarnings("restriction")
public class EventPart
{
	private static Logger logger = Logger.getLogger(EventPart.class);

	public static final String PART_ID = "org.grits.toolbox.core.part.project.event";

	private static final String ADD_NEW_TO_PREFERENCE = "Add New ...";

	public static final String EVENT_TOPIC_VALUE_MODIFIED = "EventTopic_EventPart_Modified";

	private ComboViewer actionCombo = null;
	private CDateTime cDateTime = null;
	private Text descriptionText = null;

	@Inject IEventBroker eventBroker;
	@Inject ECommandService commandService;
	@Inject EHandlerService handlerService;

	private ProjectEvent projectEvent = null;

	@Inject
	public EventPart()
	{

	}

	@Optional
	@Inject
	void setProjectEvent(@UIEventTopic
			(ProjectEntryPart.EVENT_TOPIC_FIELD_SELECTION) ProjectEvent projectEvent,
			EPartService partService, EModelService modelService)
	{
		MPart eventPart = partService == null ? null : partService.findPart(PART_ID);
		if(eventPart != null && eventPart.getObject() != null)
		{
			clearAll();
			if(projectEvent != null)
			{
				String action = projectEvent.getProjectAction() == null
						? null : projectEvent.getProjectAction().getAction();
				selectValueInCombo(actionCombo, action);
				String value = projectEvent.getDescription() == null ? "" : projectEvent.getDescription();
				descriptionText.setText(value);
				cDateTime.setSelection(projectEvent.getEventDate());

				this.projectEvent = projectEvent;
				makeEditable(true);
				modelService.bringToTop(eventPart);
			}
		}
	}

	private void clearAll()
	{
		this.projectEvent = null;

		actionCombo.getCombo().deselectAll();
		descriptionText.setText("");
		cDateTime.setSelection(null);

		makeEditable(false);
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.debug("START : Creating Event View");
		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		layout.marginTop = 30;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		actionCombo = createComboLine(composite);
		actionCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(actionCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.ACTION).getAllValues());
		actionCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(projectEvent!= null)
				{
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if(selection.getFirstElement() != null)
					{
						String action = (String) selection.getFirstElement();
						if(action.equals(ADD_NEW_TO_PREFERENCE))
						{
							HashMap<String, Object> preferencePageParams = new HashMap<String, Object>();
							preferencePageParams.put(OpenPreferenceHandler.PARAM_PREFERENCE_PAGE_ID,
											ActionPreference.PREFERENCE_PAGE_ID);
							handlerService.executeHandler(commandService.createCommand(
									OpenPreferenceHandler.COMMAND_ID, preferencePageParams));
							setInputInCombo(actionCombo, ProjectPreferenceStore.getSingleChoicePreference(
									ProjectPreferenceStore.Preference.ACTION).getAllValues());

							actionCombo.getCombo().deselectAll();
							int comboSelection = getIndexOf(actionCombo, ActionPreference.lastSelection);
							if(comboSelection >= 0)
								actionCombo.setSelection(new StructuredSelection(ActionPreference.lastSelection));
						}
						else if(!Objects.equals(action, projectEvent.getProjectAction().getAction()))
						{
							projectEvent.getProjectAction().setAction(action);
							eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectEvent);
						}
					}
				}
			}
		});

		createLabel(composite, "Date");
		cDateTime = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN);
		cDateTime.setPattern(EventsLabelProvider.DATE_FORMAT);
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
				if(projectEvent!= null)
				{
					if(!Objects.equals(projectEvent.getEventDate(), cDateTime.getSelection()))
					{
						projectEvent.setEventDate(cDateTime.getSelection());
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectEvent);
					}
				}
			}
		});

		descriptionText = createTextLine(composite, "Description", 
				SWT.BORDER|SWT.WRAP|SWT.H_SCROLL|SWT.V_SCROLL, 200);
		descriptionText.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				if(projectEvent != null)
				{
					String description = ((Text) e.getSource()).getText().trim();
					if(!Objects.equals(description, projectEvent.getDescription()))
					{
						projectEvent.setDescription(description);
						eventBroker.post(EVENT_TOPIC_VALUE_MODIFIED, projectEvent);
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

		logger.debug("END   : Creating Event View");
	}

	private void createLabel(Composite composite, String label)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);
	}

	private Text createTextLine(Composite composite, String label, 
			int style, int heightHint)
	{
		createLabel(composite, label);
		Text text = new Text(composite, style);
		GridData newEventData = new GridData(GridData.FILL_HORIZONTAL);
		newEventData.grabExcessHorizontalSpace = true;
		newEventData.horizontalSpan = 1;
		newEventData.verticalSpan = 1;
		newEventData.heightHint = heightHint;
		text.setLayoutData(newEventData);
		return text;
	}

	private ComboViewer createComboLine(Composite composite)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Action");
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData newEventData = new GridData(GridData.FILL_HORIZONTAL);
		newEventData.grabExcessHorizontalSpace = true;
		newEventData.horizontalSpan = 1;
		newEventData.verticalSpan = 1;
		comboViewer.getCombo().setLayoutData(newEventData);
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

	private void makeEditable(boolean editable)
	{
		actionCombo.getCombo().setEnabled(editable);
		descriptionText.setEnabled(editable);
		cDateTime.setEnabled(editable);
	}

	@Focus
	public void onFocus()
	{

	}

	@Persist
	public void save()
	{

	}

}