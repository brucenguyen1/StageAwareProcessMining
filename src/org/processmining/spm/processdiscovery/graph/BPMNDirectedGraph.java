package org.processmining.spm.processdiscovery.graph;

import java.util.Collection;
import java.util.Iterator;

import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;

public class BPMNDirectedGraph extends AbstractDirectedGraph<BPMNDirectedEdge,BPMNVertex>
{
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.graph.abs.AbstractDirectedGraph#addEdge(de.hpi.bpt.hypergraph.abs.IVertex, de.hpi.bpt.hypergraph.abs.IVertex)
	 */
	@Override
	public BPMNDirectedEdge addEdge(BPMNVertex s, BPMNVertex t) {
		if (s == null || t == null) return null;
		Collection<BPMNDirectedEdge> es = this.getEdgesWithSourceAndTarget(s, t);
		if (es.size()>0) {
			Iterator<BPMNDirectedEdge> i = es.iterator();
			while (i.hasNext()) {
				BPMNDirectedEdge e = i.next();
				if (e.getVertices().size()==2)
					return null;
			}
		}
		
		BPMNDirectedEdge e = new BPMNDirectedEdge(this, s, t);
		return e;
	}
}
