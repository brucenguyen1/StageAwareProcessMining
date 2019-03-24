package org.processmining.sapm.perspectivemining.graph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An ObjectInstance represents one instance of an object schema
 * projected on an event. An ObjectInstance is also associated with
 * a set of events called Fragment.
 * Note: two ObjectInstance are equal if they have the same schema and instance value
 * @author Bruce
 *
 */
public class ProcessObject {
	public static final String COL_DELIMITER = "_"; //values of event attributes must not contain the delimiter	
	protected ObjectSchema schema = null;
	protected List<String> objectValue = null;
	protected Fragment fragment = null;
	
	public ProcessObject(ObjectSchema schema, List<String> objectValue) {
		this.schema = schema;
		this.objectValue = objectValue;
		fragment = new Fragment(this);
	}
	
	public ObjectSchema getSchema() {
		return schema;
	}
	
	public List<String> getValue() {
		return new ArrayList<>(objectValue);
	}
	
	public String getValueString() {
		String compositeValue = "";
		for (String attValue : objectValue) {
			compositeValue += compositeValue.equals("") ? attValue : (this.COL_DELIMITER + attValue);
		}
		return compositeValue;
	}
	
	public Fragment getFragment() {
		return this.fragment;
	}
	
//	public void setFragment(Fragment newFragment) {
//		this.fragment = newFragment;
//	}
//	
//	@Override
//	public int hashCode() {
//	    return this.schema.hashCode()*this.instanceValue.hashCode();
//	}
//	
//	@Override
//	public boolean equals(Object otherObject) {
//		if (otherObject instanceof ProcessObject) {
//			if (this.getSchema().equals(((ProcessObject) otherObject).getSchema())) {
//				if (this.getValue().equals(((ProcessObject) otherObject).getValue())) {
//					return true;
//				}
//				else {
//					return false;
//				}
//			}
//			else {
//				return false;
//			}
//		}
//		else {
//			return false;
//		}
//	}
}
