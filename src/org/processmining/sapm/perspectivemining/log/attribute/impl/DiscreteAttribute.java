package org.processmining.sapm.perspectivemining.log.attribute.impl;

import org.processmining.sapm.perspectivemining.log.attribute.AbstrNumericalAttribute;
import org.processmining.sapm.perspectivemining.log.attribute.Attribute;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public class DiscreteAttribute extends AbstrNumericalAttribute<Integer> {

	private static final long serialVersionUID = -3579913598266729578L;

	public DiscreteAttribute(String name, String type, Attribute parent) {
		super(name, type, parent);
	}
}
