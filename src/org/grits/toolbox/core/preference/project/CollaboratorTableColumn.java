/**
 * 
 */
package org.grits.toolbox.core.preference.project;

import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;

/**
 * 
 *
 */
public class CollaboratorTableColumn
{
    private static final String NAME = "Name";
    private static final String GROUP_PI = "Group/P.I.";
    private static final String POSITION = "Position";
    private static final String DEPARTMENT = "Department";
    private static final String INSTITUTION = "Institution";
    private static final String ADDRESS = "Address";
    private static final String COUNTRY = "Country";
    private static final String EMAIL = "Email";
    private static final String PHONE = "Phone";
    private static final String FAX = "Fax";
    private static final String FUNDING = "Funding";
    private static final String GRANT = "Grant";

    public static final String[] COLUMNS  = {
        NAME, GROUP_PI, POSITION, DEPARTMENT, INSTITUTION,
        ADDRESS, COUNTRY, EMAIL, PHONE, FAX, FUNDING, GRANT};

    public static String getColumnValue(
            ProjectCollaborator projectCollaborator, 
            String columnName)
    {
        String value = projectCollaborator.getName();
        switch(columnName)
        {
        case CollaboratorTableColumn.NAME : 
            value = projectCollaborator.getName();
            break;
        case CollaboratorTableColumn.GROUP_PI : 
            value = projectCollaborator.getGroupOrPIName();
            break;
        case CollaboratorTableColumn.POSITION : 
            value = projectCollaborator.getPosition();
            break;
        case CollaboratorTableColumn.DEPARTMENT : 
            value = projectCollaborator.getDepartment();
            break;
        case CollaboratorTableColumn.INSTITUTION : 
            value = projectCollaborator.getInstitution();
            break;
        case CollaboratorTableColumn.ADDRESS : 
            value = projectCollaborator.getAddress();
            break;
        case CollaboratorTableColumn.COUNTRY : 
            value = projectCollaborator.getCountry();
            break;
        case CollaboratorTableColumn.EMAIL : 
            value = projectCollaborator.getEmail();
            break;
        case CollaboratorTableColumn.PHONE : 
            value = projectCollaborator.getPhone();
            break;
        case CollaboratorTableColumn.FAX : 
            value = projectCollaborator.getFax();
            break;
        case CollaboratorTableColumn.FUNDING : 
            value = projectCollaborator.getFundingAgency();
            break;
        case CollaboratorTableColumn.GRANT : 
            value = projectCollaborator.getGrantNumber();
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
