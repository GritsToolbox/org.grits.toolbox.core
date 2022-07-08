/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.property.project.ProjectAction;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

/**
 * 
 *
 */
public class AddEditEventDialog extends TitleAreaDialog
{
	private static final Logger logger = Logger.getLogger(AddEditEventDialog.class);

	private ProjectEvent projectEvent = null;

	private ComboViewer actionCombo = null;
	private Text descriptionText = null;

	public AddEditEventDialog(Shell parentShell)
	{
		super(parentShell);
	}

	public void setProjectEvent(ProjectEvent projectEvent)
	{
		this.projectEvent = projectEvent;
	}

	public ProjectEvent getProjectEvent()
	{
		return this.projectEvent;
	}

	@Override
	public Control createDialogArea(Composite parent)
	{
		super.createDialogArea(parent);

		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.marginTop = 20;
		layout.marginBottom = 20;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		getShell().setText("Event");
		setTitle("Project Event");
		if(projectEvent == null)
		{
			setMessage("Create a new Event");
			projectEvent = new ProjectEvent();
			ProjectAction projectAction = new ProjectAction();
			projectAction.setAction(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.ACTION).getDefaultValue());
			projectEvent.setProjectAction(projectAction);
			projectEvent.setDescription("");
		}
		else if(projectEvent.getProjectAction() == null)
		{
			projectEvent.setProjectAction(new ProjectAction());
		}
		else if(projectEvent.getProjectAction().getAction() != null)
		{
			setMessage(projectEvent.getProjectAction().getAction());
		}

		actionCombo = createComboLine(composite, "Action");
		setInputInCombo(actionCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.ACTION).getAllValues());
		actionCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				projectEvent.getProjectAction().setAction(actionCombo.getCombo().getText());
			}
		});

		descriptionText = createTextLine(composite, "Description");
		descriptionText.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				projectEvent.setDescription(((Text) e.getSource()).getText().trim());
			}
		});

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.grabExcessVerticalSpace = false;
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);

		selectInCombo(actionCombo, projectEvent.getProjectAction().getAction());

		if(projectEvent.getDescription() != null)
			descriptionText.setText(projectEvent.getDescription());

		logger.debug("END   : Creating Event Dialog");
		return composite;
	}

	private void selectInCombo(ComboViewer comboViewer, String value)
	{
		int selectionIndex = -1;
		int i = 0;
		for(String item : comboViewer.getCombo().getItems())
		{
			if(Objects.equals(item, value))
			{
				selectionIndex = i;
				break;
			}
			i++;
		}
		comboViewer.getCombo().select(selectionIndex);
	}

	private void setInputInCombo(ComboViewer comboViewer, Set<String> values) {

		List<String> valueList = new ArrayList<String>();
		valueList.addAll(values);
		Collections.sort(valueList);
		String[] items = new String[valueList.size()];
		int i = 0;
		for(String value : valueList)
		{
			items[i++] = value;
		}
		comboViewer.getCombo().setItems(items);
	}

	private Text createTextLine(Composite composite, String label)
	{
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(label);
		GridData createNewData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		createNewData.horizontalSpan = 1;
		createNewData.verticalSpan = 1;
		textLabel.setLayoutData(createNewData);

		Text text = new Text(composite, SWT.BORDER|SWT.V_SCROLL|SWT.WRAP);
		GridData newEventData = new GridData(GridData.FILL_HORIZONTAL);
		newEventData.grabExcessHorizontalSpace = true;
		newEventData.horizontalSpan = 1;
		newEventData.verticalSpan = 1;
		newEventData.heightHint = 80;
		newEventData.widthHint = 200;
		text.setLayoutData(newEventData);
		return text;
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
		GridData newEventData = new GridData(GridData.FILL_HORIZONTAL);
		newEventData.grabExcessHorizontalSpace = true;
		newEventData.horizontalSpan = 1;
		newEventData.verticalSpan = 1;
		comboViewer.getCombo().setLayoutData(newEventData);
		return comboViewer;
	}

}
