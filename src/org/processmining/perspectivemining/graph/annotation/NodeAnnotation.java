package org.processmining.perspectivemining.graph.annotation;

import org.gephi.graph.api.Node;

public class NodeAnnotation extends Annotation {

	public NodeAnnotation(Node owner) {
		super(owner);
	}

	public Node getNode() {
		return (Node) owner;
	}

}
