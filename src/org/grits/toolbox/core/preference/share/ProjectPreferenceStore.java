/**
 * 
 */
package org.grits.toolbox.core.preference.share;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectEvent;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.preference.project.ActionPreference;
import org.grits.toolbox.core.preference.project.CollaboratorPreference;
import org.grits.toolbox.core.preference.project.CollaboratorTableColumn;
import org.grits.toolbox.core.preference.project.CollaboratorTablePreference;
import org.grits.toolbox.core.preference.project.CountryPreference;
import org.grits.toolbox.core.preference.project.EventPreference;
import org.grits.toolbox.core.preference.project.FundingPreference;
import org.grits.toolbox.core.preference.project.KeywordPreference;
import org.grits.toolbox.core.preference.project.PersonPreference;
import org.grits.toolbox.core.preference.project.PositionPreference;
import org.grits.toolbox.core.preference.project.RolePreference;
import org.grits.toolbox.core.preference.project.StatusPreference;
import org.grits.toolbox.core.preference.project.TasklistPreference;
import org.grits.toolbox.core.preference.project.TasklistTableColumn;
import org.grits.toolbox.core.preference.project.TasklistTablePreference;
import org.jdom.Element;

/**
 * 
 *
 */
public class ProjectPreferenceStore
{
	private static final Logger logger = Logger.getLogger(ProjectPreferenceStore.class);

	@Inject private static IGritsPreferenceStore gritsPreferenceStore;
	private static Map<String, SingleChoicePreference> singleSelectionTypeMap =
			new HashMap<String, SingleChoicePreference>();
	private static Map<String, MultiChoicePreference<?>> multiSelectionTypeMap =
			new HashMap<String, MultiChoicePreference<?>>();

	public static SingleChoicePreference getSingleChoicePreference(Preference preference)
	{
		String preferenceName = preference.getPreferenceName();
		logger.info("Getting preference : " + preferenceName);
		SingleChoicePreference preferenceObject = singleSelectionTypeMap.get(preferenceName);
		if(preferenceObject == null)
		{
			preferenceObject = new SingleChoicePreference(gritsPreferenceStore, preference);
			singleSelectionTypeMap.put(preferenceName, preferenceObject);
		}
		return preferenceObject;
	}

	@SuppressWarnings("unchecked")
	public static <T> MultiChoicePreference <T> getMultiChoicePreference(
			ParameterizedPreference parameterizedPreference)
	{
		String preferenceName = parameterizedPreference.getPreferenceName();
		logger.info("Getting preference : " + preferenceName);
		MultiChoicePreference<T> preferenceObject = (MultiChoicePreference<T>) multiSelectionTypeMap.get(preferenceName);
		if(preferenceObject == null)
		{
			preferenceObject = new MultiChoicePreference<T>(gritsPreferenceStore, parameterizedPreference);
			multiSelectionTypeMap.put(preferenceName, preferenceObject);
		}
		return preferenceObject;
	}

	public static MultiChoiceInteger getMultiChoiceInteger(IntegerPreference integerPreference)
	{
		String preferenceName = integerPreference.getPreferenceName();
		logger.info("Getting preference : " + preferenceName);
		MultiChoiceInteger preferenceObject = (MultiChoiceInteger) multiSelectionTypeMap.get(preferenceName);
		if(preferenceObject == null)
		{
			preferenceObject = new MultiChoiceInteger(gritsPreferenceStore, integerPreference);
			multiSelectionTypeMap.put(preferenceName, preferenceObject);
		}
		return preferenceObject;
	}

	/**
	 * {@link Preference} are preferences with String values and are also
	 * {@link SingleChoicePreference} i.e. only one of them can be selected
	 */
	public enum Preference
	{
		// These enum values correspond to preference list of type string with 
		// single default value. Enum values contain preference file name,
		// current variable name and its previous names
		ACTION("action_preference.xml", ActionPreference.class.getName() + ".all",
				ActionPreference.class + ".all"),
		FUNDING("funding_preference.xml", FundingPreference.class.getName() + ".all",
				FundingPreference.class + ".all"),
		PERSON("person_preference.xml", PersonPreference.class.getName() + ".all",
				PersonPreference.class + ".all"),
		POSITION("position_preference.xml", PositionPreference.class.getName() + ".all",
				PositionPreference.class + ".all"),
		ROLE("role_preference.xml", RolePreference.class.getName() + ".all",
				RolePreference.class + ".all"),
		STATUS("status_preference.xml", StatusPreference.class.getName() + ".all",
				StatusPreference.class + ".all"),
		TASK("task_preference.xml", TasklistPreference.class.getName() + ".all",
				TasklistPreference.class + ".all");

		String defaultFileName = null;
		String preferenceName = null;
		String[] previousNames = new String[0];
		private Preference(String defaultFileName, 
			String preferenceName, String ... previousNames)
		{
			this.defaultFileName = defaultFileName;
			this.preferenceName = preferenceName;
			this.previousNames = previousNames;
		}

		public String getDefaultFileName()
		{
			return defaultFileName;
		}

		public String getPreferenceName()
		{
			return preferenceName;
		}

		public String[] getPreviousNames()
		{
			return previousNames;
		}
	}

	/**
	 * {@link ParameterizedPreference} types happen to be {@link MultiChoicePreference}
	 * but the two definitions are different
	 */
	public enum ParameterizedPreference
	{
		// These enum values correspond to preference list of varied class type with single
		// or multiple default values and. Enum values contain list's class type, preference file name,
		// current variable name and its previous names
		COLLABORATOR(ProjectCollaborator.class, null, CollaboratorPreference.class.getName() + ".all",
				CollaboratorPreference.class + ".defaultCollaborators",
				CollaboratorPreference.class + ".otherCollaborators"),
		EVENT(ProjectEvent.class, null, EventPreference.class.getName() + ".all",
				EventPreference.class + ".defaultEvents", EventPreference.class + ".otherEvents"),
		KEYWORD(String.class, "keyword_preference.xml", KeywordPreference.class.getName() + ".all",
				KeywordPreference.class + ".default", KeywordPreference.class + ".other"),
		TASKLIST(ProjectTasklist.class, null, TasklistPreference.class.getName() + ".allTasklists",
				TasklistPreference.class + ".defaultTasklists", TasklistPreference.class + ".otherTasklists"),

		// these enums are not be used outside but are used for extending few other types of preferences
		HIDDEN_ENUM_NOT_TO_BE_USED_COLLABORATOR_TABLE(Integer.class, null,
				CollaboratorTablePreference.class.getName() + ".visible"),
		HIDDEN_ENUM_NOT_TO_BE_USED_TASKLIST_TABLE(Integer.class, null,
				TasklistTablePreference.class.getName() + ".visible");

		String defaultFileName = null;
		String preferenceName = null;
		String[] previousNames = new String[0];
		Class<?> unmarshallerClass;
		private <T> ParameterizedPreference(Class<T> unmarshallerClass, String defaultFileName, 
				String preferenceName, String ... previousNames)
		{
			this.unmarshallerClass  = unmarshallerClass;
			this.defaultFileName = defaultFileName;
			this.preferenceName = preferenceName;
			this.previousNames = previousNames;
		}

		public String getDefaultFileName()
		{
			return defaultFileName;
		}

		public String getPreferenceName()
		{
			return preferenceName;
		}

		public String[] getPreviousNames()
		{
			return previousNames;
		}
	}

	public enum IntegerPreference
	{
		// These enum values correspond to preference list of type integer with
		// multiple default values. Enum values contain preference file name,
		// current variable name and its previous names
		COLLABORATORS_TABLE(CollaboratorTableColumn.COLUMNS.length,
				ParameterizedPreference.HIDDEN_ENUM_NOT_TO_BE_USED_COLLABORATOR_TABLE),
		TASKLIST_TABLE(TasklistTableColumn.COLUMNS.length,
				ParameterizedPreference.HIDDEN_ENUM_NOT_TO_BE_USED_TASKLIST_TABLE);

		private int maxValue;
		ParameterizedPreference parameterizedPreference = null;
		private IntegerPreference(int maxValue,
				ParameterizedPreference parameterizedPreference)
		{
			this.maxValue  = maxValue;
			this.parameterizedPreference  = parameterizedPreference;
		}

		public String getDefaultFileName()
		{
			return parameterizedPreference.getDefaultFileName();
		}

		public String getPreferenceName()
		{
			return parameterizedPreference.getPreferenceName();
		}

		public int getMaxValue()
		{
			return maxValue;
		}
	}

	public enum StringPreference
	{
		// These enum values correspond to a single string preference. 
		// Enum values contain current variable name along with its previous names
		COUNTRY(CountryPreference.class.getName() + ".default", CountryPreference.class + ".default");

		String preferenceName = null;
		String value = null;
		String[] previousNames = new String[0];
		private StringPreference(String preferenceName, String ... previousNames)
		{
			this.preferenceName = preferenceName;
			this.previousNames = previousNames;
			loadValue();
		}

		public String getPreferenceName()
		{
			return preferenceName;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public boolean savePreference()
		{
			PreferenceEntity preferenceEntity = new PreferenceEntity(preferenceName);
			preferenceEntity.setValue(value);
			return gritsPreferenceStore.savePreference(preferenceEntity);
		}

		private void loadValue()
		{
			try
			{
				value = gritsPreferenceStore.getPreferenceEntity(preferenceName).getValue();
			} catch (UnsupportedVersionException e)
			{
				int index = 0;
				Element preferenceElement = null;
				while(index < previousNames.length)
				{
					preferenceElement = gritsPreferenceStore.getPreferenceElement(previousNames[index]);
					if(preferenceElement != null)
					{
						value =  preferenceElement.getAttribute("value") == null 
								? null : preferenceElement.getAttributeValue("value");
						savePreference();
						gritsPreferenceStore.removePreference(previousNames[index]);
						break;
					}
					index++;
				}
			} catch (Exception e)
			{
				logger.fatal(e.getMessage(), e);
			}
		}
	}
}
