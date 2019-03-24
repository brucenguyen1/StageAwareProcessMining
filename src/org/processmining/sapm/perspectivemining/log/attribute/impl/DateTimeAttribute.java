//From ProcessCube implementation by Alfredo Bolt
package org.processmining.sapm.perspectivemining.log.attribute.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.processmining.sapm.perspectivemining.log.attribute.AbstrNumericalAttribute;
import org.processmining.sapm.perspectivemining.log.attribute.Attribute;
import org.processmining.sapm.perspectivemining.log.attribute.AttributeUtils;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class DateTimeAttribute extends AbstrNumericalAttribute<Date> {

	private static final long serialVersionUID = 7733018547589239531L;

	private DateFormat dateFormat;

	public static final String YEAR = "YEAR", QUARTER = "QUARTER", MONTH = "MONTH", WEEK_OF_MONTH = "WEEK OF MONTH",
			DAY_OF_WEEK = "DAY OF WEEK", DAY = "DAY", HOUR = "HOUR", MINUTE = "MINUTE", SECOND = "SECOND";

	public DateTimeAttribute(String name, String type, Attribute parent) {
		super(name, type, parent);

	}

	public void setFormat(DateFormat format) {
		this.dateFormat = format;
	}

	public DateFormat getFormat() {
		return dateFormat;
	}

	public void addValue(String value) throws ParseException {
		if (dateFormat == null)
			dateFormat = AttributeUtils.detectTimestampParser(value);
		if (value != null && !value.equalsIgnoreCase("null") && !value.isEmpty()) {
			addValue(dateFormat.parse(value));
		}
	}

	public void addValue(Date value) {

		valueSet.add(value);
		Calendar cal = new GregorianCalendar();
		cal.setTime(value);

		((DiscreteAttribute) this.getChildren().get(YEAR)).addValue(cal.get(Calendar.YEAR));
		((DiscreteAttribute) this.getChildren().get(MONTH)).addValue(cal.get(Calendar.MONTH));
		((DiscreteAttribute) this.getChildren().get(WEEK_OF_MONTH)).addValue(cal.get(Calendar.WEEK_OF_MONTH));
		((DiscreteAttribute) this.getChildren().get(DAY_OF_WEEK)).addValue(cal.get(Calendar.DAY_OF_WEEK));
		((DiscreteAttribute) this.getChildren().get(DAY)).addValue(cal.get(Calendar.DAY_OF_MONTH));
		((DiscreteAttribute) this.getChildren().get(HOUR)).addValue(cal.get(Calendar.HOUR_OF_DAY));
		((DiscreteAttribute) this.getChildren().get(MINUTE)).addValue(cal.get(Calendar.MINUTE));
		((DiscreteAttribute) this.getChildren().get(SECOND)).addValue(cal.get(Calendar.SECOND));

	}

}
