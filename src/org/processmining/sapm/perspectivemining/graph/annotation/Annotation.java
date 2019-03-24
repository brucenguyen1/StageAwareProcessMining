package org.processmining.sapm.perspectivemining.graph.annotation;

/**
 * This class represents one annotation, which is generated for one system node
 * (state or transition). One annotation can contain multiple annotation
 * properties (e.g., soujourn time, remaining time, elapsed time...)
 */

import java.util.HashMap;
import java.util.Set;

public class Annotation {
	private int DEFAULT_NUM_VALUES = 10;
	private final HashMap<String, AnnotationElement> elements;
	protected Object owner;

	public Annotation(Object owner) {
		elements = new HashMap<String, AnnotationElement>();
		this.owner = owner;
	}

	public void addElement(AnnotationElement element) {
		elements.put(element.getName(), element);
	}

	public AnnotationElement getElement(String name) {
		// No presence means this node/edge does not contain this element
		// Thus, it is set to a series of 0s for the statistical test
//		if (!elements.containsKey(name)) {
//			this.addElement(new AnnotationElement(name, AnnotationElement.Type.Number));
//			for (int i=1;i<=DEFAULT_NUM_VALUES;i++) {
//				this.getElement(name).addValue(0);
//			}
//		}
		return elements.get(name);
	}

	public Set<String> getElementNames() {
		return elements.keySet();
	}
	
	public boolean containsKey(String key){
		if(!elements.isEmpty() && elements.containsKey(key))
			return true;
		else
			return false;
	}
}
