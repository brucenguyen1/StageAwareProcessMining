package org.processmining.spm.stagemining.algorithm;

import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;

public abstract class AbstractStageMiningAlgo {
	protected boolean debug = false;
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean getDebug() {
		return this.debug;
	}
	
	public String getName() {
		return "AbstractStageMiningAlgo";
	}
	
	public DecompositionTree mine(WeightedDirectedGraph graph, int minStageSize) throws Exception {
		return null;
	}
}
