/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;

/**
 * 
 *
 */
public class AddEditTaskDialog extends TitleAreaDialog
{
	private static final Logger logger = Logger.getLogger(AddEditTaskDialog.class);

	private ProjectTasklist tasklist = null;

	private ComboViewer taskCombo = null;
	private ComboViewer personCombo = null;
	private ComboViewer statusCombo = null;
	private ComboViewer roleCombo = null;
	private Text groupPIText = null;

	public AddEditTaskDialog(Shell parentShell)
	{
		super(parentShell);
	}

	public void setTasklist(ProjectTasklist tasklist)
	{
		this.tasklist = tasklist;
	}

	public ProjectTasklist getTasklist()
	{
		return this.tasklist;
	}

	@Override
	public Control createDialogArea(Composite parent)
	{
		super.createDialogArea(parent);

		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		layout.marginTop = 20;
		layout.marginBottom = 20;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		getShell().setText("Task");
		setTitle("Project Task");
		if(tasklist == null)
		{
			setMessage("Create a new Task");
			tasklist = new ProjectTasklist();
			tasklist.setTask(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.TASK).getDefaultValue());
			tasklist.setPerson(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.PERSON).getDefaultValue());
			tasklist.setStatus(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.STATUS).getDefaultValue());
			tasklist.setRole(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.ROLE).getDefaultValue());
			tasklist.setGroupOrPIName("");
		}
		else if(tasklist.getTask() != null)
		{
			setMessage(tasklist.getTask());
		}

		taskCombo = createComboLine(composite, "Task");
		taskCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(taskCombo, ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.TASK).getAllValues());
		taskCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				tasklist.setTask(taskCombo.getCombo().getText());
			}
		});

		personCombo = createComboLine(composite, "Person");
		personCombo.setContentProvider(new ArrayContentProvider());
		setInputInCombo(personCombo, ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.PERSON).getAllValues());
		personCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				tasklist.setPerson(personCombo.getCombo().getText());
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
				tasklist.setStatus(statusCombo.getCombo().getText());
				tasklist.setModifiedTime(new Date());
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
				tasklist.setRole(roleCombo.getCombo().getText());
			}
		});

		groupPIText = createTextLine(composite, "Group/P.I.");
		groupPIText.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				tasklist.setGroupOrPIName(groupPIText.getText().trim());
			}
		});

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.grabExcessVerticalSpace = false;
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);

		selectInCombo(taskCombo, tasklist.getTask());
		selectInCombo(personCombo, tasklist.getPerson());
		selectInCombo(statusCombo, tasklist.getStatus());
		selectInCombo(roleCombo, tasklist.getRole());

		if(tasklist.getGroupOrPIName() != null)
			groupPIText.setText(tasklist.getGroupOrPIName());
		
		logger.debug("END   : Creating Tasklist Dialog");
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

		Text text = new Text(composite, SWT.BORDER);
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		text.setLayoutData(newTasklistData);
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
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		comboViewer.getCombo().setLayoutData(newTasklistData);
		return comboViewer;
	}

}
