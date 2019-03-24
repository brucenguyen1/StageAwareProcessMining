package org.processmining.sapm.perspectivemining.graph.model;

import java.util.ArrayList;

/**
 * This class is the set of events equivalent to an object schema 
 * Events in this set are always sorted by timestamp
 * @author Bruce
 *
 */
public class Fragment extends ArrayList<EventRow> {
	private ProcessObject object = null;
	
	public Fragment(ProcessObject object) {
		this.object = object;
	}
	
	public ProcessObject getProcessObject() {
		return this.object;
	}

}
