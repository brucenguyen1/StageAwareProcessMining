package org.processmining.spm.stagemining;

import org.deckfour.xes.classification.XEventClass;
import org.jbpt.hypergraph.abs.IVertex;

public class Milestone extends StageItem {
	private boolean trueMilestone = false;
	private boolean belongToPrevStage = true;
	
	public Milestone(String label, IVertex node, XEventClass eventClass, boolean trueMilestone) {
		super(label,node,eventClass);
		this.trueMilestone = trueMilestone;
		this.belongToPrevStage = true;
	}
	
	public boolean isTrueMilestone() {
		return trueMilestone;
	}
	
	
	public boolean isBelongToPrevStage() {
		return this.belongToPrevStage;
	}
	
	public void setBelongToPrevStage(boolean belongToPrevStage) {
		this.belongToPrevStage = belongToPrevStage;
	}
}
