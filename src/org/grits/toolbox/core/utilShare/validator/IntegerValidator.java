package org.grits.toolbox.core.utilShare.validator;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;

public class IntegerValidator implements ICellEditorValidator {
	private static final String INVALID_MESSAGE = "Not an Integer Value";
	private ControlDecoration controlDecoration;
	
	public IntegerValidator(ControlDecoration controlDecoration) {
	    this.controlDecoration = controlDecoration;
	    Image errorImage = FieldDecorationRegistry.getDefault()
	            .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
	            .getImage();
	    this.controlDecoration.setMarginWidth(2);
	    this.controlDecoration.setImage(errorImage);
	    this.controlDecoration.setDescriptionText(INVALID_MESSAGE);
	}
	
	@Override
	public String isValid(Object value) {
		String inValidMessage = null;
		this.controlDecoration.hide();
		if (value != null) {
			String stringValue = (String) value;
			if (!stringValue.isEmpty()) {
				try {
					Integer.parseInt(stringValue);
				} catch (NumberFormatException e) {
					inValidMessage = INVALID_MESSAGE;
					this.controlDecoration.show();
				}
			}
		}
		return inValidMessage;
	}

}
