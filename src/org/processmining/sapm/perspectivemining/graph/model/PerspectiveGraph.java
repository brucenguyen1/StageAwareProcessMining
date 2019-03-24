package org.processmining.sapm.perspectivemining.graph.model;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.project.api.Workspace;
import org.processmining.sapm.perspectivemining.graph.annotation.Annotation;
import org.processmining.sapm.perspectivemining.graph.annotation.PGAnnotation;
import org.processmining.sapm.perspectivemining.graph.settings.GraphType;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.WeightType;
import org.processmining.sapm.perspectivemining.graph.settings.WeightValueType;

/**
 * BaseGraph is used to abstract directed or undirected graphs
 * using Gephi library
 * @author Bruce
 *
 */
public class PerspectiveGraph {
	private PerspectiveSettingObject setting = null;
	private Workspace wp=null;
	private GraphModel gm=null;
	private PGAnnotation apg = null;
	
	public PerspectiveGraph(Workspace workspace, GraphModel graphModel) {
		this.wp = workspace;
		this.gm = graphModel;
		this.apg = new PGAnnotation();
	}
	
	public Workspace getWorkspace() {
		return this.wp;
	}
	
	public PerspectiveSettingObject getSetting() {
		return this.setting;
	}
	
	public void setSetting(PerspectiveSettingObject settings) {
		this.setting = settings;
	}
	
	public PGAnnotation getPGAnnotation() {
		return this.apg;
	}
	
	public Graph getGraph() {
        if (this.isDirected()) {
        	return gm.getDirectedGraph();
        }
        else {
        	return gm.getUndirectedGraph();
//        	return gm.getDirectedGraph();
        }
	}
	
	public boolean isDirected() {
		if (setting.getGraphType().equals(GraphType.INTER_FRAGMENT)) { 
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Get or create new node if not exist
	 * @param nodeName
	 * @return
	 */
	public Node getOrCreateNode(String nodeId) {
		GraphFactory factory = gm.factory();
		Node node = this.getGraph().getNode(nodeId);
		if (node == null) {
			node = factory.newNode(nodeId);
			this.getGraph().addNode(node);
		}
		return node;
	}
	
	public Node getNode(String nodeId) {
		return this.getGraph().getNode(nodeId);
	}
	
	public NodeIterable getNodes() {
		return this.getGraph().getNodes();
	}
	
	/**
	 * Get edge or create new edge if not exist
	 * @param node1
	 * @param node2
	 * @return Edge
	 */
	public Edge getOrCreateEdge(Node node1, Node node2) {
		Edge edge = this.getGraph().getEdge(node1, node2);
		if (edge == null) {
			edge = gm.factory().newEdge(node1, node2, this.isDirected());
			this.getGraph().addEdge(edge);
//			System.out.println("Created new edge");
		}
		return edge;
	}
	
	public Edge getOrCreateEdge(String node1Id, String node2Id) {
		Node node1 = this.getNode(node1Id);
		Node node2 = this.getNode(node2Id);
		if (node1 != null && node2 != null) {
			Edge edge = this.getGraph().getEdge(node1, node2);
			if (edge == null) {
				edge = gm.factory().newEdge(node1, node2, this.isDirected());
				this.getGraph().addEdge(edge);
			}
			return edge;
		}
		else {
			return null;
		}
	}
	
	public Edge getEdge(Node node1, Node node2) {
		return this.getGraph().getEdge(node1, node2);
	}
	
	public Edge getEdge(String node1Id, String node2Id) {
		Node node1 = this.getNode(node1Id);
		Node node2 = this.getNode(node2Id);
		if (node1 != null && node2 != null) {
			return this.getEdge(node1, node2);
		}
		else {
			return null;
		}
	}
	
	public Edge getEdge(Edge edge) {
		return this.getEdge(edge.getSource(), edge.getTarget());
	}
	
	public EdgeIterable getEdges() {
		return this.getGraph().getEdges();
	}
	
	public double[] getWeightValues(Object obj) throws Exception {
		Annotation annotation = this.getPGAnnotation().getAnnotation(obj);
		if (this.setting.getWeightType() == WeightType.FREQUENCY) {
			if (this.setting.getWeightValueType() == WeightValueType.FREQUENCY_ABSOLUTE) {
				return annotation.getElement(WeightValueType.FREQUENCY_ABSOLUTE.name()).getValues();
			}
			else if (this.setting.getWeightValueType() == WeightValueType.FREQUENCY_RELATIVE) {
				return annotation.getElement(WeightValueType.FREQUENCY_RELATIVE.name()).getValues();
			}
			else {
				throw new Exception("Unsupported WeightValueType!");
			}
		}
		else if (this.setting.getWeightType() == WeightType.DURATION) {
			if (this.setting.getWeightValueType() == WeightValueType.DURATION_MEAN) {
				return annotation.getElement(WeightValueType.DURATION_MEAN.name()).getValues();
			}
			else if (this.setting.getWeightValueType() == WeightValueType.DURATION_MEDIAN) {
				return annotation.getElement(WeightValueType.DURATION_MEDIAN.name()).getValues();
			}
			else if (this.setting.getWeightValueType() == WeightValueType.DURATION_MIN) {
				return annotation.getElement(WeightValueType.DURATION_MIN.name()).getValues();
			}
			else if (this.setting.getWeightValueType() == WeightValueType.DURATION_MAX) {
				return annotation.getElement(WeightValueType.DURATION_MAX.name()).getValues();
			}
			else {
				throw new Exception("Unsupported WeightValueType!");
			}
		}
		else {
			throw new Exception("Unsupported WeightType!");
		}
	}

} 
