package org.processmining.perspectivemining.graph.model;

import java.util.HashMap;
import java.util.Map;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;

/*
 * Create differential graph from two prespective graphs
 * Just create the graph structure. Common nodes and edges
 * are created accordingly in the differential graph, as well
 * as any uncommon nodes and edges.
 * No graph annotations yet.
 */
public class DifferentialGraph extends PerspectiveGraph {
	private PerspectiveGraph pGraph1 = null;
	private PerspectiveGraph pGraph2 = null;
	private Map<Node,Node> mapToGraph1Node = new HashMap<>(); //map from this graph node to graph1 node
	private Map<Node,Node> mapToGraph2Node = new HashMap<>(); //map from this graph node to graph2 node
	private Map<Edge,Edge> mapToGraph1Edge = new HashMap<>(); //map from this graph edge to graph1 edge
	private Map<Edge,Edge> mapToGraph2Edge = new HashMap<>(); //map from this graph edge to graph2 edge
	
	public DifferentialGraph(Workspace workspace, GraphModel graphModel) {
		super(workspace, graphModel);
	}
	
	public PerspectiveGraph getGraph1() {
		return this.pGraph1;
	}
	
	public void setGraph1(PerspectiveGraph pGraph1) {
		this.pGraph1 = pGraph1;
	}
	
	public PerspectiveGraph getGraph2() {
		return this.pGraph2;
	}
	
	public void setGraph2(PerspectiveGraph pGraph2) {
		this.pGraph2 = pGraph2;
	}
	
	public Map<Node,Node> getMapToGraph1Node() {
		return this.mapToGraph1Node;
	}
	
	public Map<Node,Node> getMapToGraph2Node() {
		return this.mapToGraph2Node;
	}
	
	public Map<Edge,Edge> getMapToGraph1Edge() {
		return this.mapToGraph1Edge;
	}
	
	public Map<Edge,Edge> getMapToGraph2Edge() {
		return this.mapToGraph2Edge;
	}
	
	/*
	 * Calculate comparison between two perspective graphs
	 * As a result, the differential graph will have new nodes and edges
	 * These includes all nodes and edges appearing in one graph but not 
	 * in other graph, and nodes and edges appearing in both graphs
	 */
	public void compare() throws Exception {
		if (pGraph1 == null || pGraph2 == null) {
			throw new Exception("Cannot compare when graphs have not been set!");
		}
		
		// Check graph1 nodes
		for (Node node : pGraph1.getNodes()) {
			Node newNode = this.getOrCreateNode(node.getId().toString());
			if (pGraph2.getNode(node.getId().toString()) == null) { //only occur in graph 1
				newNode.setAttribute("graph1", true);
				newNode.setAttribute("graph2", false);
				mapToGraph1Node.put(newNode, node);
			}
			else {	// common nodes
				newNode.setAttribute("graph1", true);
				newNode.setAttribute("graph2", true);
				mapToGraph1Node.put(newNode, node);
				mapToGraph2Node.put(newNode, pGraph2.getNode(node.getId().toString()));
			}
		}
		
		// Check graph2 nodes
		for (Node node : pGraph2.getNodes()) {	
			if (pGraph1.getNode(node.getId().toString()) == null) { // only occur in graph 2
				Node newNode = this.getOrCreateNode(node.getId().toString());
				newNode.setAttribute("graph1", false);
				newNode.setAttribute("graph2", true);
				mapToGraph2Node.put(newNode, node);
			}
			else {
				// common nodes have been processed above
			}
		}
		
		// Check graph1 edges
		for (Edge graph1Edge : pGraph1.getEdges()) {
			Edge graph2Edge = pGraph2.getEdge(graph1Edge.getSource().getId().toString(),graph1Edge.getTarget().getId().toString());
			Edge newEdge = this.getOrCreateEdge(graph1Edge.getSource().getId().toString(), graph1Edge.getTarget().getId().toString());
			if ( graph2Edge == null) {	// only occur in graph 1
				newEdge.setAttribute("graph1", true);
				newEdge.setAttribute("graph2", false);
				mapToGraph1Edge.put(newEdge, graph1Edge);
			}
			else {	// common edge
				newEdge.setAttribute("graph1", true);
				newEdge.setAttribute("graph2", true);
				mapToGraph1Edge.put(newEdge, graph1Edge);
				mapToGraph2Edge.put(newEdge, graph2Edge);
			}
		}
		
		// Check graph2 edges
		for (Edge graph2Edge : pGraph2.getEdges()) {
			Edge graph1Edge = pGraph1.getEdge(graph2Edge.getSource().getId().toString(),graph2Edge.getTarget().getId().toString());
			if ( graph1Edge == null) {	// only occur in graph 2
				Edge newEdge = this.getOrCreateEdge(graph2Edge.getSource().getId().toString(), graph2Edge.getTarget().getId().toString());
				newEdge.setAttribute("graph1", false);
				newEdge.setAttribute("graph2", true);
				mapToGraph2Edge.put(newEdge, graph2Edge);
			}
			else {
				// common edges have been processed above
			}
		}
	}
	
}
