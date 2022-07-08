package org.grits.toolbox.core.utilShare;

import org.eclipse.swt.widgets.Text;

public class TextFieldUtils {
		public static boolean isValidPercent(Text input){
		try{
			double temp = Double.parseDouble(input.getText());
			if(temp > 0 && temp < 100){
				return true;
			}else{

				return false;
			}
		}catch(Exception e){
			return false;
		}
	}

	public static boolean isEmpty(Text input){
		String sText = input.getText();
		boolean bIsEmpty = sText.trim().isEmpty();
		return bIsEmpty;

	}
	public static boolean isDouble(Text input){
		try{
			Double.parseDouble(input.getText());
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean isInteger(Text input){
		try{
			Integer.parseInt(input.getText());
			return true;
		}catch(Exception e){
			return false;
		}
	}
	

	public static boolean isNonZero(Text input){
		try{
			double temp = 0.0;
			temp = Double.parseDouble(input.getText());
			if(temp > 0){
				return true;
			}else{

				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
}
