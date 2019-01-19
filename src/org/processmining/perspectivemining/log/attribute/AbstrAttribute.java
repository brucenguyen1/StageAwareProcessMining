package org.processmining.perspectivemining.log.attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This abstract class contains all the common variables that an attribute
 * should have, and basic functionalities for them as well.
 * 
 * @author Alfredo Bolt
 *
 * @param <T>
 *            (e.g., String, integer)
 */
public abstract class AbstrAttribute<T extends Comparable<T>> implements Attribute {

	private static final long serialVersionUID = -4511938041557436943L;

	// the name of the column in the event base that this attribute references
	protected String name;
	// the visible name that will be shown in the UI
	protected String label;
	// used to query from a DB (may contain function calls)
	protected String queryString;
	// type of attribute
	protected String type;

	// parent, if this attribute is derived
	protected Attribute parent;

	// children attributes that depend on this one
	protected Map<String, Attribute> children;

	// read-only copy of the original valueset, used when resetting the active
	// valueset
	protected final TreeSet<T> valueSet;

	protected AbstrAttribute(String name, String type, Attribute parent) {
		this.name = name;
		this.type = type;
		this.parent = parent;
		this.children = new HashMap<String, Attribute>();
		this.valueSet = new TreeSet<T>();
	}

	@Override
	public Attribute getParent() {
		return parent;
	}

	@Override
	public Map<String, Attribute> getChildren() {
		return children;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		String result = "";
		if (this.parent != null)
			result = this.parent.getLabel() + ":" + label;
		else
			result = label;
		return result;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	@Override
	public Set<T> getValueSet() {
		return Collections.unmodifiableSet(valueSet);
	}

	@Override
	public String toString() {
		return label;
	}

	public void addValue(T value) {
		valueSet.add(value);
	}
	
	public int compareTo(Attribute a){
		return this.getLabel().compareTo(a.getLabel());
	}

}
