package org.processmining.perspectivemining.graph.annotation;

import org.gephi.graph.api.Edge;

public class EdgeAnnotation extends Annotation {

	public EdgeAnnotation(Edge owner) {
		super(owner);
	}

	public Edge getEdge() {
		return (Edge) owner;
	}

}
