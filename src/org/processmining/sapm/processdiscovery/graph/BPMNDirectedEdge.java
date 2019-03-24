package org.processmining.sapm.processdiscovery.graph;

import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractMultiDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

public class BPMNDirectedEdge extends AbstractDirectedEdge<BPMNVertex> {
	private boolean isMarked = false;
	private Flow sequenceFlow = null;
	
	
	protected BPMNDirectedEdge(AbstractMultiDirectedGraph<?, BPMNVertex> g, BPMNVertex source, BPMNVertex target) {
		super(g, source, target);
	}
	
	public boolean isMarked() {
		return isMarked;
	}
	
	public void setMarked(boolean isMarked) {
		this.isMarked = isMarked;
	}
	
	public Flow getSequenceFlow() {
		return this.sequenceFlow;
	}
	
	public void setSequenceFlow(Flow sequenceFlow) {
		this.sequenceFlow = sequenceFlow;
		this.setName(sequenceFlow.getLabel());
		this.setId(sequenceFlow.getEdgeID().toString());
	}
}

