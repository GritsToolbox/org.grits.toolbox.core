/**
 * 
 */
package org.grits.toolbox.core.utilShare.validator;

/**
 * 
 *
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;

public class DateValidator implements ICellEditorValidator {

    private static String DATE_FORMAT = "MM/dd/yyyy";
    private SimpleDateFormat simpleDateFormat = null;
    private ControlDecoration controlDecoration = null;

    public DateValidator(ControlDecoration controlDecoration) {
        this.controlDecoration = controlDecoration;
        Image errorImage = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                .getImage();
        this.controlDecoration.setMarginWidth(2);
        this.controlDecoration.setImage(errorImage);
        setSimpleDateFormat();
    }

    public DateValidator(ControlDecoration controlDecoration, String dateFormat)
    {
        this(controlDecoration);
        DATE_FORMAT = dateFormat;
        setSimpleDateFormat();
    }

    private void setSimpleDateFormat()
    {
        simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        simpleDateFormat.setLenient(false);
    }

    @Override
    public String isValid(Object value)
    {
        String inValidMessage = null;
        this.controlDecoration.hide();
        if (value != null) {
            String stringValue = (String) value;
            if (!stringValue.isEmpty()) {
                try
                {
                    simpleDateFormat.parse(stringValue);
                } catch (ParseException e) {
                    inValidMessage = "Not in Correct Date Format - " + DATE_FORMAT;
                    this.controlDecoration.setDescriptionText(inValidMessage);
                    this.controlDecoration.show();
                }
            }
        }
        return inValidMessage;
    }
}
