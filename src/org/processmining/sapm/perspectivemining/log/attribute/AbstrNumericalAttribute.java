package org.processmining.sapm.perspectivemining.log.attribute;

/**
 * From ProcessCube implementation
 * @author Alfredo Bolt
 *
 */
public abstract class AbstrNumericalAttribute<T extends Comparable<T>> extends AbstrAttribute<T> {

	private static final long serialVersionUID = -3317234921454229049L;

	protected T selectedMin, selectedMax;

	public AbstrNumericalAttribute(String name, String type, Attribute parent) {
		super(name, type, parent);
	}

	public T getMin() {
		if (!valueSet.isEmpty())
			return valueSet.first();
		else
			return null;
	}

	public T getMax() {
		if (!valueSet.isEmpty())
			return valueSet.last();
		else
			return null;
	}

	public T getSelectedMin() {
		if (selectedMin != null)
			return selectedMin;
		else
			return getMin();
	}

	public T getSelectedMax() {
		if (selectedMax != null)
			return selectedMax;
		else
			return getMax();
	}

	public void setSelectedMin(T newSelectedMin) {
		selectedMin = newSelectedMin;
	}

	public void setSelectedMax(T newSelectedMax) {
		selectedMax = newSelectedMax;
	}

	public void resetSelections() {
		selectedMin = getMin();
		selectedMax = getMax();
	}

}
