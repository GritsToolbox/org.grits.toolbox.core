/**
 * 
 */
package org.grits.toolbox.core.utilShare.validator;

import org.eclipse.jface.viewers.ICellEditorValidator;

/**
 * 
 *
 */
public class NumericValidator implements ICellEditorValidator
{

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
     */
    @Override
    public String isValid(Object value)
    {
        String  inValidMessage = null;
       if(value instanceof String)
       {
           String valueString = ((String) value);
           for(char eachCharacter : valueString.toCharArray())
           {
               if(eachCharacter<48 || eachCharacter > 57)
               {
                   inValidMessage = "Not a Number";
                   break;
               }
           }
       }
       return inValidMessage;
    }

}
