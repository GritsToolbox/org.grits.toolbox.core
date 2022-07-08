/**
 * 
 */
package org.grits.toolbox.core.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.part.provider.TasklistsLabelProvider;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.TableColumnComparatorListener;
import org.grits.toolbox.core.utilShare.sort.TableViewerComparator;

/**
 * 
 *
 */
public class AddTaskListDialog extends TitleAreaDialog
{
	private static final Logger logger = Logger.getLogger(AddTaskListDialog.class);

	private ComboViewer personCombo = null;
	private ComboViewer roleCombo = null;
	private Text groupPIText = null;
	private ComboViewer statusCombo = null;
	private CDateTime cDateTime = null;
	private CheckboxTableViewer taskTableViewer = null;
	private List<ProjectTasklist> taskLists = new ArrayList<ProjectTasklist>();

	public AddTaskListDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public void create()
	{
		super.create();
		super.setTitle("Add Tasklist");
		super.setMessage("Assign tasks to a person");
		getShell().setText("Project Tasklist");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.debug("START : Creating Tasklist UI");
		Composite comp = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(comp, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);

		personCombo = createComboLine(composite, "Person");
		setInputInCombo(personCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.PERSON).getAllValues());
		if(ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.PERSON).getDefaultValue() != null)
			selectInCombo(personCombo, ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.PERSON).getDefaultValue());

		roleCombo = createComboLine(composite, "Role");
		setInputInCombo(roleCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.ROLE).getAllValues());
		if(ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.ROLE).getDefaultValue() != null)
			selectInCombo(roleCombo, ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.ROLE).getDefaultValue());

		groupPIText = createTextLine(composite, "Group/P.I.");

		statusCombo = createComboLine(composite, "Status");
		setInputInCombo(statusCombo, ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.STATUS).getAllValues());
		if(ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.STATUS).getDefaultValue() != null)
			selectInCombo(statusCombo, ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.STATUS).getDefaultValue());

		createLabel(composite, "Due Date");
		cDateTime = new CDateTime(composite, CDT.BORDER|CDT.DROP_DOWN);
		cDateTime.setPattern(TasklistsLabelProvider.DATE_FORMAT);
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		cDateTime.setLayoutData(newTasklistData);

		Table taskTable = new Table(composite, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 100;
		taskTable.setLayoutData(tableLayouData);
		this.taskTableViewer = new CheckboxTableViewer(taskTable, "Default");

		TableViewerColumn tableColumn2 = new TableViewerColumn(taskTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Task");
		tableColumn2.getColumn().setWidth(200);
		TableViewerColumn tableColumn3 = new TableViewerColumn(taskTableViewer, SWT.FILL, 2);
		tableColumn3.getColumn().setText("Description");
		tableColumn3.getColumn().setWidth(350);
		tableColumn3.setEditingSupport(new EditingSupport(taskTableViewer)
		{
			private TextCellEditor textCellEditor = null;
			@Override
			protected void setValue(Object element, Object value)
			{
				if(element instanceof ProjectTasklist && value instanceof String)
				{
					((ProjectTasklist) element).setDescription((String) value);
					taskTableViewer.refresh();
				}
			}
			
			@Override
			protected Object getValue(Object element)
			{
				String value = "";
				if(element instanceof ProjectTasklist)
				{
					value = ((ProjectTasklist) element).getDescription() == null ?
							value : ((ProjectTasklist) element).getDescription();
				}
				return value;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element)
			{
				if(element instanceof ProjectTasklist)
				{
					if(textCellEditor == null)
					{
						textCellEditor = new TextCellEditor(taskTableViewer.getTable());
					}
					return textCellEditor;
				}
				return null;
			}
			
			@Override
			protected boolean canEdit(Object element)
			{
				return element instanceof ProjectTasklist;
			}
		});
		taskTableViewer.getTable().setHeaderVisible(true);
		taskTableViewer.getTable().setLinesVisible(true);
		taskTableViewer.setContentProvider(new GenericListContentProvider());
		taskTableViewer.setLabelProvider(new ITableLabelProvider()
		{
			@Override
			public void removeListener(ILabelProviderListener listener)
			{

			}

			@Override
			public boolean isLabelProperty(Object element, String property)
			{
				return false;
			}

			@Override
			public void dispose()
			{

			}

			@Override
			public void addListener(ILabelProviderListener listener)
			{

			}

			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				String value = null;
				if(element instanceof ProjectTasklist)
				{
					ProjectTasklist tasklist = (ProjectTasklist) element;
					switch (columnIndex)
					{
					case 1:
						value = tasklist.getTask();
						break;
					case 2:
						value = tasklist.getDescription();
						break;
					default:
						break;
					}
				}
				return value;
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}
		});

		taskTableViewer.setComparator(new TableViewerComparator());
		tableColumn2.getColumn().addSelectionListener(
				new TableColumnComparatorListener(taskTableViewer));
		tableColumn3.getColumn().addSelectionListener(
				new TableColumnComparatorListener(taskTableViewer));
		List<ProjectTasklist> tasklists = new ArrayList<ProjectTasklist>();
		ProjectTasklist projectTasklist = null;
		int i = 0;
		for(String task : ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.TASK).getAllValues())
		{
			projectTasklist = new ProjectTasklist();
			projectTasklist.setTask(task);
			projectTasklist.setAddByDefault(ProjectPreferenceStore.getSingleChoicePreference(
					ProjectPreferenceStore.Preference.TASK).getDefaultValue().equals(task));
			tasklists.add(i, projectTasklist);
		}
		taskTableViewer.setInput(tasklists);
		taskTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				ProjectTasklist projectTasklist = 
						(ProjectTasklist) event.getElement();
				boolean checked = event.getChecked();
				taskTableViewer.setChecked(projectTasklist, checked);
				projectTasklist.setAddByDefault(checked);
			}
		});
		taskTableViewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				if(element instanceof ProjectTasklist)
				{
					return ((ProjectTasklist) element).isAddByDefault();
				}
				return false;
			}
		});
		taskTableViewer.refresh();

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.grabExcessHorizontalSpace = true;
		compositeLayoutData.grabExcessVerticalSpace = false;
		compositeLayoutData.horizontalSpan = 3;
		compositeLayoutData.verticalSpan = 1;
		composite.setLayoutData(compositeLayoutData);

		logger.debug("END   : Creating Tasklist UI");

		return parent;
	}

	private void selectInCombo(ComboViewer comboViewer, String value)
	{
		comboViewer.getCombo().clearSelection();
		comboViewer.setSelection(comboViewer.getSelection());
		if(value != null)
		{
			int index = 0;
			for(String comboItem : comboViewer.getCombo().getItems())
			{
				if(value.equals(comboItem))
				{
					comboViewer.getCombo().select(index);
					break;
				}
				index++;
			}
		}
	}

	private void setInputInCombo(ComboViewer comboViewer, Set<String> values) 
	{
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
		createLabel(composite, label);
		Text text = new Text(composite, SWT.BORDER);
		GridData newTasklistData = new GridData(GridData.FILL_HORIZONTAL);
		newTasklistData.grabExcessHorizontalSpace = true;
		newTasklistData.horizontalSpan = 1;
		newTasklistData.verticalSpan = 1;
		text.setLayoutData(newTasklistData);
		return text;
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

	@Override
	protected void okPressed() 
	{
		if(taskTableViewer.getCheckedElements().length > 0)
		{
			ProjectTasklist projectTaskList = null;
			int i = 0;
			for(Object selectedTask : taskTableViewer.getCheckedElements())
			{
				projectTaskList = (ProjectTasklist) selectedTask;
				projectTaskList.setPerson(personCombo.getCombo().getText());
				projectTaskList.setRole(roleCombo.getCombo().getText());
				if(groupPIText.getText() != null)
					projectTaskList.setGroupOrPIName(groupPIText.getText());
				projectTaskList.setStatus(statusCombo.getCombo().getText());
				projectTaskList.setModifiedTime(new Date());
				projectTaskList.setDueDate(cDateTime.getSelection());
				taskLists.add(i++, projectTaskList);
			}
			super.okPressed();
		}
		else
		{
			super.cancelPressed();
		}
	}

	public List<ProjectTasklist> getTaskLists() {
		return taskLists;
	}
}
