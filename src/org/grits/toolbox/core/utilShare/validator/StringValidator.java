/**
 * 
 */
package org.grits.toolbox.core.utilShare.validator;

/**
 * 
 *
 */
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;

public class StringValidator implements ICellEditorValidator
{
	private ControlDecoration controlDecoration;
	private int validatorType = 0;

	/**
	 * validates the value of type String
	 * @param controlDecoration sets appropriate error message or no message
	 * for correct values.
	 * It uses <strong>validatorType = 0</strong>. 
	 * All string values are valid (including <b>empty values</b>).
	 * For using <b>validatorType</b> = <b>1</b>, i.e.
	 * not allowing empty values use the other constructor
	 * {@link #StringValidator(ControlDecoration, int)}
	 */
	public StringValidator(ControlDecoration controlDecoration)
	{
		this.controlDecoration = controlDecoration;
		Image errorImage = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		this.controlDecoration.setMarginWidth(2);
		this.controlDecoration.setImage(errorImage);
	}

	/**
	 * validates the value of type String
	 * @param controlDecoration sets appropriate error message or
	 * no message for correct values.
	 * @param validatorType decides if the string value is valid in different criteria.
	 * For <strong>validatorType = 0</strong> all string values are valid (including <b>empty values</b>). 
	 * For <b>validatorType = 1</b>, string value cannot be empty.
	 * The <b>default value</b> for <b>validatorType</b> is <b>0</b> (allows empty values).
	 * Passing any integer other than 0 or 1 sets <b>validatorType</b> to 0.
	 */
	public StringValidator(ControlDecoration controlDecoration, int validatorType)
	{
		this(controlDecoration);
		this.validatorType  = validatorType != 1 ? 0 : validatorType;
	}

	@Override
	public String isValid(Object value)
	{
		String inValidMessage = null;
		this.controlDecoration.hide();
		if(value instanceof String)
		{
			String stringValue = (String) value;
			stringValue = stringValue.trim();
			switch(validatorType)
			{
			case 0 :
				break;
			case 1 :
				inValidMessage = stringValue.isEmpty() ?
						" Value cannot be empty " : inValidMessage;
				break;
			}
		}
		else
		{
			inValidMessage = " Value is not a String ";
		}
		if(inValidMessage != null)
		{
			this.controlDecoration.show();
			this.controlDecoration.setDescriptionText(inValidMessage);
		}
		return inValidMessage;
	}

}
