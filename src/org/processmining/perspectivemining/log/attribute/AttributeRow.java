//From ProcessCube implementation by Alfredo Bolt

package org.processmining.perspectivemining.log.attribute;

import java.util.Set;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class AttributeRow {

	private String attName;

	private Set<String> valueSet;
	private String useAs;

	public AttributeRow(String name, Set<String> values, String use) {
		attName = name;
		valueSet = values;
		useAs = use;
	}

	public String getName() {
		return attName;
	}

	public Set<String> getValueSet() {
		return valueSet;
	}

	public String getUseAs() {
		return useAs;
	}
	
	public void setUseAs(String use) {
		useAs = use;
	}

//	public void setLogScope(String useAs) {
//		this.useAs = useAs;
//	}
//	
//	public ScopeEnum getLogScope() {
//		return logScope;
//	}

	@Override
	public String toString() {
		return attName;
	}
	
	@Override
	public int hashCode() {
	    return attName.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof AttributeRow) {
			AttributeRow otherAtt = (AttributeRow)other;
			if (this.attName.equals(otherAtt.getName())) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	

}
