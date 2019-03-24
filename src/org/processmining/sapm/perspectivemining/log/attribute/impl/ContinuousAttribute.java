//From ProcessCube implementation by Alfredo Bolt
package org.processmining.sapm.perspectivemining.log.attribute.impl;

import org.processmining.sapm.perspectivemining.log.attribute.AbstrNumericalAttribute;
import org.processmining.sapm.perspectivemining.log.attribute.Attribute;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class ContinuousAttribute extends AbstrNumericalAttribute<Double> {

	private static final long serialVersionUID = 4371396611802758036L;

	public ContinuousAttribute(String name, String type, Attribute parent) {
		super(name, type, parent);
	}
	
}
