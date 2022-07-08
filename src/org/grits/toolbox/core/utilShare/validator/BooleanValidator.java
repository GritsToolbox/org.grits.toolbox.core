/**
 * 
 */
package org.grits.toolbox.core.utilShare.validator;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;

/**
 * 
 *
 */
public class BooleanValidator implements ICellEditorValidator
{

	private ControlDecoration controlDecoration;

	public BooleanValidator(ControlDecoration controlDecoration)
	{
		this.controlDecoration = controlDecoration;
		Image errorImage = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		this.controlDecoration.setMarginWidth(2);
		this.controlDecoration.setImage(errorImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	@Override
	public String isValid(Object value)
	{
		String inValidMessage = null;
		this.controlDecoration.hide();
		if(value instanceof String)
		{
			String stringValue = ((String) value);
			if(!stringValue.isEmpty())
			{
				if(!(stringValue.equalsIgnoreCase("True")
						|| stringValue.equalsIgnoreCase("False")))
				{
					inValidMessage = "Not a boolean value";
				}
			}
			else
			{
				inValidMessage = "Boolean value is empty";
			}
		}
		else
		{
			inValidMessage = "Cannot get the boolean value (not valid string)";
		}

		if(inValidMessage != null)
		{
			controlDecoration.setDescriptionText(inValidMessage);
			controlDecoration.show();
		}
		return inValidMessage;
	}

}
