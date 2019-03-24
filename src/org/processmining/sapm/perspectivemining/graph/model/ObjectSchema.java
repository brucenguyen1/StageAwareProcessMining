package org.processmining.sapm.perspectivemining.graph.model;

import java.util.ArrayList;
import java.util.List;

import org.processmining.sapm.perspectivemining.log.attribute.AttributeRow;

/**
 * An ObjectSchema represents the object schema (list of attributes)
 * @author Bruce
 *
 */
public class ObjectSchema {
	protected List<AttributeRow> attributes = null;
	
	public ObjectSchema(List<AttributeRow> attributes) {
		this.attributes = attributes;
	}
	
	public List<AttributeRow> getAttributes() {
		return this.attributes;
	}
	
	public List<String> getAttributeNames() {
		List<String> attNames = new ArrayList<>();
		for (AttributeRow att : attributes) {
			attNames.add(att.getName());
		}
		return attNames;
	}
	
	public boolean isEmpty() {
		return this.attributes.isEmpty();
	}
	
	/*
	 * Project an object schema onto a log
	 */
//	public Set<ObjectInstance> project(List<EventRow> events) throws Exception {
//		Set<ObjectInstance> objects = new HashSet<>();
//		for (EventRow event : events) {
//			objects.add(event.project(this));
//		}
//		return objects;
//	}
	
	@Override
	public int hashCode() {
		int hashCode = 1;
	    for (AttributeRow att : attributes) {
	    	hashCode = 31*hashCode + (att==null ? 0 : att.hashCode());
	    }
	    return hashCode;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ObjectSchema) {
			if (this.getAttributeNames().equals(((ObjectSchema) other).getAttributeNames())) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
}
 