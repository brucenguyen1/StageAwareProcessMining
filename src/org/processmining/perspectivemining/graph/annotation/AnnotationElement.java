package org.processmining.perspectivemining.graph.annotation;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * This class represents an annotation element that will be contained in an
 * "Annotation" object, which may contain more AnnotationElements.
 * 
 * Each state and transition will hold a unique Annotation object, but may hold
 * many AnnotationElements
 * 
 * An AnnotationElement can contain value sets and descriptive summaries, thanks
 * to the DescriptiveStatistics class' methods.
 * 
 * @author abolt
 *
 */
public class AnnotationElement extends DescriptiveStatistics {
//	public static final String duration = "duration", frequency = "frequency";
	
	public static enum Type {
		Date, Number
	};

	/**
	 * Dates are parsed differently than numbers: they are shown in a date
	 * format.
	 */
	private static final long serialVersionUID = 6979650922634301708L;
	private String name;
	private Type type;

	public AnnotationElement(String name, Type type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public String getAsString(double number) {
		if (this.type.equals(Type.Number))
			return String.valueOf(number);
		else
			return new Duration((long) number).toString();
	}

}
