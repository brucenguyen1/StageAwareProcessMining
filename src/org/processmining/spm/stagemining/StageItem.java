package org.processmining.spm.stagemining;

import org.deckfour.xes.classification.XEventClass;
import org.jbpt.hypergraph.abs.IVertex;

public class StageItem {
	private String label;
	private IVertex node;
	private XEventClass eventClass;
	
	public StageItem(String label, IVertex node, XEventClass eventClass) {
		this.label = label;
		this.node = node;
		this.eventClass = eventClass;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public IVertex getNode() {
		return this.node;
	}
	
	public XEventClass getEventClass() {
		return this.eventClass;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
}
