/**
 * 
 */
package org.grits.toolbox.core.datamodel.property.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * 
 *
 */
public class DateAdapter extends XmlAdapter<String, Date>
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public String marshal(Date date)
	{
		return date == null ? null : dateFormat.format(date);
	}

	@Override
	public Date unmarshal(String date) throws ParseException
	{
		return date == null ? null : dateFormat.parse(date);
	}
}
