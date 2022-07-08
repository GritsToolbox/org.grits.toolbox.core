/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import java.text.SimpleDateFormat;

import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.part.provider.TasklistsLabelProvider;

/**
 * 
 *
 */
public class TasklistTableColumn
{
	private static final String TASK = "Task";
	private static final String PERSON = "Person";
	private static final String STATUS = "Status";
	private static final String DUE_DATE = "Due Date";
	private static final String ROLE = "Role";
	private static final String GROUP_PI = "Group/P.I.";
	private static final String NUMBER_OF_TASKS = "# of Tasks";
	private static final String DESCRIPTION = "Description";

	public static final String[] COLUMNS  =
		{TASK, PERSON, STATUS, DUE_DATE, ROLE, GROUP_PI, NUMBER_OF_TASKS, DESCRIPTION};

	public static String getColumnValue(ProjectTasklist projectTasklist, String columnName)
	{
		String value = null;
		switch(columnName)
		{
			case TasklistTableColumn.TASK : 
				value = projectTasklist.getTask();
				break;
			case TasklistTableColumn.PERSON : 
				value = projectTasklist.getPerson();
				break;
			case TasklistTableColumn.STATUS : 
				value = projectTasklist.getStatus();
				break;
			case TasklistTableColumn.DUE_DATE : 
				if(projectTasklist.getDueDate() != null) 
				{
					value = (new SimpleDateFormat(TasklistsLabelProvider.DATE_FORMAT)
							).format(projectTasklist.getDueDate());
				}
				break;
			case TasklistTableColumn.ROLE : 
				value = projectTasklist.getRole();
				break;
			case TasklistTableColumn.GROUP_PI : 
				value = projectTasklist.getGroupOrPIName();
				break;
			case TasklistTableColumn.NUMBER_OF_TASKS : 
				value = projectTasklist.getNumberOfTasks() + "";
				break;
			case TasklistTableColumn.DESCRIPTION : 
				value = projectTasklist.getDescription();
				break;
		}
		return value;
	}

	public static int getColumnNumber(String columnName)
	{
		int columnNum = -1;
		int index = 0;
		while(index < COLUMNS.length)
		{
			if(COLUMNS[index].equals(columnName))
			{
				columnNum = index;
				break;
			}
			index++;
		}
		return columnNum;
	}
}
