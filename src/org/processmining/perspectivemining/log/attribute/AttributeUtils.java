package org.processmining.perspectivemining.log.attribute;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.processmining.perspectivemining.log.attribute.impl.ContinuousAttribute;
import org.processmining.perspectivemining.log.attribute.impl.DateTimeAttribute;
import org.processmining.perspectivemining.log.attribute.impl.DiscreteAttribute;
import org.processmining.perspectivemining.log.attribute.impl.TextAttribute;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class AttributeUtils {

	public static void addValue(Attribute attribute, Object value) throws Exception {

		assert value != null;

		if (attribute instanceof ContinuousAttribute) {
			if (value instanceof Number)
				((ContinuousAttribute) attribute).addValue((double) value);
			else if (value instanceof String) {
				if (!((String) value).equalsIgnoreCase("null"))
					((ContinuousAttribute) attribute).addValue(Double.parseDouble((String) value));
			} else
				throw new ClassCastException(
						"The value \"" + value.toString() + "\" does not match the type of the attribute \""
								+ attribute.getLabel() + "\": " + attribute.getType());

		} else if (attribute instanceof DiscreteAttribute) {

			if (value instanceof Integer)
				((DiscreteAttribute) attribute).addValue((Integer) value);
			else if (value instanceof Double)
				((DiscreteAttribute) attribute).addValue(((Double) value).intValue());
			else if (value instanceof String) {
				if (!((String) value).equalsIgnoreCase("null"))
					((DiscreteAttribute) attribute).addValue(Integer.parseInt((String) value));
			} else
				throw new ClassCastException(
						"The value \"" + value.toString() + "\" does not match the type of the attribute \""
								+ attribute.getLabel() + "\": " + attribute.getType());

		} else if (attribute instanceof DateTimeAttribute) {
			if (value instanceof Date)
				((DateTimeAttribute) attribute).addValue((Date) value);
			else if (value instanceof Long)
				((DateTimeAttribute) attribute).addValue(new Date((Long) value));
			else if (value instanceof String) {
				((DateTimeAttribute) attribute).addValue((String) value);
			} else
				throw new ClassCastException(
						"The value \"" + value.toString() + "\" does not match the type of the attribute \""
								+ attribute.getLabel() + "\": " + attribute.getType());

		} else if (attribute instanceof TextAttribute) {
			if (value instanceof String)
				((TextAttribute) attribute).addValue((String) value);
			else if (value instanceof Double)
				((TextAttribute) attribute).addValue(Double.toString((Double) value));
			else if (value instanceof Integer)
				((TextAttribute) attribute).addValue(Integer.toString((Integer) value));
			else
				throw new ClassCastException(
						"The value \"" + value.toString() + "\" does not match the type of the attribute \""
								+ attribute.getLabel() + "\": " + attribute.getType());
		}
	}

	public static void resetFilter(Attribute attribute) {
		if (attribute instanceof TextAttribute) {
			((TextAttribute) attribute).clearSelectedValueSet();
			for (String value : ((TextAttribute) attribute).getValueSet())
				((TextAttribute) attribute).addValueToSelectedValueset(value);
		}
		if (attribute instanceof AbstrNumericalAttribute<?>) {
			((AbstrNumericalAttribute<?>) attribute).resetSelections();
		}
	}

	public static void setMinMax(AbstrNumericalAttribute<?> attribute, Object min, Object max) {
		if (attribute instanceof ContinuousAttribute) {
			if (min instanceof Number && max instanceof Number) {
				((ContinuousAttribute) attribute).setSelectedMin((double) min);
				((ContinuousAttribute) attribute).setSelectedMax((double) max);
			}
		} else if (attribute instanceof DiscreteAttribute) {
			if (min instanceof Integer && max instanceof Integer) {
				((DiscreteAttribute) attribute).setSelectedMin((Integer) min);
				((DiscreteAttribute) attribute).setSelectedMax((Integer) max);
			} else if (min instanceof Double && max instanceof Double) {
				((DiscreteAttribute) attribute).setSelectedMin(((Double) min).intValue());
				((DiscreteAttribute) attribute).setSelectedMax(((Double) max).intValue());
			}
		}
	}
	
	public static DateFormat detectTimestampParser(String input) {
		DateFormat df;
		Date aux = null;
		try {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}
		return null;
	}
}
