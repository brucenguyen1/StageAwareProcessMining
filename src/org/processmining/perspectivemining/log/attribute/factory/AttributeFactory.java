//From ProcessCube implementation by Alfredo Bolt

package org.processmining.perspectivemining.log.attribute.factory;

import org.processmining.perspectivemining.log.attribute.Attribute;
import org.processmining.perspectivemining.log.attribute.impl.ContinuousAttribute;
import org.processmining.perspectivemining.log.attribute.impl.DateTimeAttribute;
import org.processmining.perspectivemining.log.attribute.impl.DiscreteAttribute;
import org.processmining.perspectivemining.log.attribute.impl.TextAttribute;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class AttributeFactory {

	public static Attribute createAttribute(String name, String type, Attribute parent) {

		Attribute result = null;

		if (type.equals(Attribute.CONTINUOUS)) {
			result = new ContinuousAttribute(name, type, parent);
			createAttributeLabelAndQuery(result);
		} else if (type.equals(Attribute.DISCRETE)) {
			result = new DiscreteAttribute(name, type, parent);
			createAttributeLabelAndQuery(result);
		} else if (type.equals(Attribute.DATE_TIME)) {
			result = new DateTimeAttribute(name, type, parent);
			createAttributeLabelAndQuery(result);
			createDerivedAttributes(result);
		} else { // text
			result = new TextAttribute(name, type, parent);
			createAttributeLabelAndQuery(result);
		}

		return result;
	}

	private static void createDerivedAttributes(Attribute parent) {

		if (parent instanceof DateTimeAttribute) {

			Attribute year = new DiscreteAttribute(DateTimeAttribute.YEAR, Attribute.DERIVED, parent);
			year.setLabel(parent.getLabel() + ":" + year.getName());
			year.setQueryString("strftime('%Y'," + parent.getQueryString() + ")");
			parent.getChildren().put(year.getName(), year);

			Attribute month = new DiscreteAttribute(DateTimeAttribute.MONTH, Attribute.DERIVED, parent);
			month.setLabel(parent.getLabel() + ":" + month.getName());
			month.setQueryString("strftime('%m'," + parent.getQueryString() + ")");
			parent.getChildren().put(month.getName(), month);

			Attribute weekOfMonth = new DiscreteAttribute(DateTimeAttribute.WEEK_OF_MONTH, Attribute.DERIVED, parent);
			weekOfMonth.setLabel(parent.getLabel() + ":" + weekOfMonth.getName());
			weekOfMonth.setQueryString("strftime('%W'," + parent.getQueryString() + ")");
			parent.getChildren().put(weekOfMonth.getName(), weekOfMonth);

			Attribute dayOfWeek = new DiscreteAttribute(DateTimeAttribute.DAY_OF_WEEK, Attribute.DERIVED, parent);
			dayOfWeek.setLabel(parent.getLabel() + ":" + dayOfWeek.getName());
			dayOfWeek.setQueryString("strftime('%w'," + parent.getQueryString() + ")");
			parent.getChildren().put(dayOfWeek.getName(), dayOfWeek);

			Attribute day = new DiscreteAttribute(DateTimeAttribute.DAY, Attribute.DERIVED, parent);
			day.setLabel(parent.getLabel() + ":" + day.getName());
			day.setQueryString("strftime('%d'," + parent.getQueryString() + ")");
			parent.getChildren().put(day.getName(), day);

			Attribute hour = new DiscreteAttribute(DateTimeAttribute.HOUR, Attribute.DERIVED, parent);
			hour.setLabel(parent.getLabel() + ":" + hour.getName());
			hour.setQueryString("strftime('%H'," + parent.getQueryString() + ")");
			parent.getChildren().put(hour.getName(), hour);

			Attribute minute = new DiscreteAttribute(DateTimeAttribute.MINUTE, Attribute.DERIVED, parent);
			minute.setLabel(parent.getLabel() + ":" + minute.getName());
			minute.setQueryString("strftime('%M'," + parent.getQueryString() + ")");
			parent.getChildren().put(minute.getName(), minute);

			Attribute second = new DiscreteAttribute(DateTimeAttribute.SECOND, Attribute.DERIVED, parent);
			second.setLabel(parent.getLabel() + ":" + second.getName());
			second.setQueryString("strftime('%S'," + parent.getQueryString() + ")");
			parent.getChildren().put(second.getName(), second);
		}

	}

	private static void createAttributeLabelAndQuery(Attribute attribute) {
		attribute.setLabel(attribute.getName());
		attribute.setQueryString("\"" + attribute.getName() + "\"");
	}
}
