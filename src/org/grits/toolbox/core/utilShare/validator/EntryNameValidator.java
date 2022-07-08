package org.grits.toolbox.core.utilShare.validator;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IInputValidator;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;

/**
 * 
 * 
 *
 */
public class EntryNameValidator implements IInputValidator
{
	private static final Logger logger = Logger.getLogger(EntryNameValidator.class);
	private static final String ALLOWED_REGEX = "^[a-zA-Z0-9_-]*";
	private String entryPropertyType = null;
	private Set<String> existingNames = new HashSet<String>();

	public EntryNameValidator(Entry entry)
	{
		if(entry != null)
		{
			if(entry.getProperty() != null)
			{
				entryPropertyType = entry.getProperty().getType();
			}
			if(entry.getParent() != null)
			{
				Entry parentEntry = entry.getParent();
				existingNames = getExistingNames(parentEntry, entryPropertyType, entry.getDisplayName());
			}
		}
	}

	@Override
	public String isValid(String newText)
	{
		newText = newText.trim();
		String errorMessage = null;
		if(newText == null || newText.isEmpty())
		{
			errorMessage = "Name cannot be empty. Please select a unique name.";
		}
		else if(entryPropertyType != null && entryPropertyType == ProjectProperty.TYPE)
		{
			errorMessage = validateProjectName(existingNames, newText);
		}
		else if(existingNames.contains(newText))
		{
			errorMessage = "This name already exists. Please choose a unique name";
		}
		return errorMessage;
	}

	/**
	 * get the existing names to match the entry name against
	 * @param parentEntry parent entry whose siblings are to be checked against
	 * @param entryPropertyType decides it is project type then 
	 * it takes care of other folders inside workspace
	 * @param currentName current name of the entry
	 * @return
	 */
	public static final Set<String> getExistingNames(Entry parentEntry,
			String entryPropertyType, String currentName)
			{
		Set<String> existingNames = new HashSet<String>(); 
		if(entryPropertyType != null && entryPropertyType.equals(ProjectProperty.TYPE))
		{
			try
			{
				File workspaceFolder = new File(
						((WorkspaceProperty) DataModelHandler.instance().getRoot().getProperty()).getLocation());
				for(File file : workspaceFolder.listFiles())
				{
					existingNames.add(file.getName());
				}
			} catch (Exception ex)
			{
				logger.fatal(ex.getMessage(), ex);
			}
		}
		for(Entry siblingEntry : parentEntry.getChildren())
		{
			existingNames.add(siblingEntry.getDisplayName());
		}
		if(currentName != null)
			existingNames.remove(currentName);
		return existingNames;
			}

	/**
	 * validates a project name by returning proper error message (returns null if valid)
	 * @param existingNames the names it cannot choose
	 * @param newText the current new name to be validated
	 * (trim is supposed to be used on newTex before using this method)
	 * @return null error message if the name is correct else returns the error message
	 */
	public static final String validateProjectName(Set<String> existingNames, String newText)
	{
		String errorMessage = null;
		if(newText == null || newText.isEmpty())
		{
			errorMessage = "Name cannot be null or empty.";
		}
		else
		{
			if(!newText.matches(ALLOWED_REGEX))
			{
				errorMessage = "Project name cannot use special characters."
						+ " \"-\" and \"_\" are the only allowed special characters."
						+ " Please use other characters.";
			}
			else if(newText.length() > 32)
			{
				errorMessage = "Name cannot be longer than 32 characters";
				
			}
			else
			{
				for(String name : existingNames)
				{
					if(name.equalsIgnoreCase(newText))
					{
						errorMessage = "This name already exists (case-insensitive or hidden) in the workspace."
								+ " Please choose a unique name";
						break;
					}
				}
			}
		}
		return errorMessage;
	}

}
