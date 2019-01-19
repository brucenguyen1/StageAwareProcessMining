package org.processmining.perspectivemining.utils;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.processmining.perspectivemining.graph.annotation.Annotation;
import org.processmining.perspectivemining.graph.annotation.GraphAnnotator;
import org.processmining.perspectivemining.graph.model.DifferentialGraph;
import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.graph.settings.SamplingType;
import org.processmining.perspectivemining.graph.settings.WeightType;

import model.graph.GraphModel;
import model.graph.impl.DirectedGraphInstance;
import model.graph.impl.EdgeInstance;
import model.graph.impl.SymmetricGraphInstance;

public class GraphVisualizer {
	public final static float[] COLOR_SCALES = new float[]{0f,0.2f,0.4f,0.6f,0.8f,1f};
//    public final static Color[] COLORS = new Color[]{Color.LIGHT_GRAY, Color.CYAN, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.RED};
    
    public final static Color[] GRAPH1_COLOR = new Color[]{
											    		new Color(255, 255, 204),
														new Color(255, 255, 179),
														new Color(255, 255, 128),
														new Color(255, 255, 128),
														new Color(255, 255, 51),
														new Color(230, 230, 0)}; // Yellow shades
    
    public final static Color[] GRAPH2_COLOR = new Color[]{
														new Color(255, 250, 230),
														new Color(255, 240, 179),
														new Color(255, 230, 128),
														new Color(255, 219, 77),
														new Color(255, 209, 26),
														new Color(230, 204, 0)}; // Orange shades
    
    public final static Color NO_DIFFERENCE_COLOR = new Color(204, 255, 204); //Color.GREEN;
    public final static Color LOW_FREQUENCY_COLOR = Color.LIGHT_GRAY;
    
    /*
     * Create a visual graph for a perspective graph
     * Just a structure and data, not yet coloring nodes/edges
     */
	public static GraphModel createPerspectiveVisualization(PerspectiveGraph pGraph) throws Exception {
		DirectedGraphInstance visGraphModel = null;
		visGraphModel = pGraph.isDirected() ? new DirectedGraphInstance () : new SymmetricGraphInstance();
		Map<Node, model.graph.Node> nodeMap = new HashMap<>(); //store all the nodes
		
		for (Node node: pGraph.getNodes()) {
			model.graph.Node visNode = new model.graph.Node(node.getId().toString()); //not yet set node weight
			visGraphModel.addNode(visNode);
			nodeMap.put(node, visNode);
			Annotation annotation = pGraph.getPGAnnotation().getAnnotation(node);
			visGraphModel.getGraph1NodeWeights().put(visNode, pGraph.getWeightValues(node));
			
			//Debug only
//			System.out.println("Node: " + node.getId());
//			Annotation annotation = pGraph.getPGAnnotation().getAnnotation(node);
//			System.out.println("Values (frequency) = " + Arrays.toString(annotation.getElement(AnnotationElement.frequency).getSortedValues()));
//			System.out.println("Values (duration) = " + Arrays.toString(annotation.getElement(AnnotationElement.duration).getSortedValues()));
		}
		
		for (Edge edge: pGraph.getEdges()) {
			Node node1 = edge.getSource();
			Node node2 = edge.getTarget();
			Set<model.graph.Edge> edges = visGraphModel.getEdges(nodeMap.get(node1), nodeMap.get(node2));
			if (edges.isEmpty()) {
				model.graph.Edge visEdge = new EdgeInstance (nodeMap.get(node1), nodeMap.get(node2), 0.0);		
				visGraphModel.addEdge (visEdge);
				visGraphModel.getGraph1EdgeWeights().put(visEdge, pGraph.getWeightValues(edge));
			}
			
			//Debug only
//			System.out.println("Edge: " + node1.getId() + "-" + node2.getId());
//			Annotation annotation = pGraph.getPGAnnotation().getAnnotation(edge);
//			System.out.println("Values (frequency) = " + Arrays.toString(annotation.getElement(AnnotationElement.frequency).getSortedValues()));
//			System.out.println("Values (duration) = " + Arrays.toString(annotation.getElement(AnnotationElement.duration).getSortedValues()));
		}
        
		return visGraphModel;
	}
	
	public static void highlightPerspectiveGraph(DirectedGraphInstance visGraphModel) throws Exception {
		Map<model.graph.Node, Double> mapNodeWeight = new HashMap<>();
		Map<model.graph.Edge, Double> mapEdgeWeight = new HashMap<>();
		
		double maxNodeWeight = 0;
		for (model.graph.Node node : (Collection<model.graph.Node>)(Collection<?>)visGraphModel.getNodes()) {
			DescriptiveStatistics nodeStat = new DescriptiveStatistics(visGraphModel.getGraph1NodeWeights().get(node));
			double nodeWeight = nodeStat.getMean();
			mapNodeWeight.put(node, nodeWeight);
			if (maxNodeWeight < nodeWeight) maxNodeWeight = nodeWeight;
		}
		
		double maxEdgeWeight = 0;
		for (model.graph.Edge edge : (Collection<model.graph.Edge>)(Collection<?>)visGraphModel.getEdges()) {
			DescriptiveStatistics edgeStat = new DescriptiveStatistics(visGraphModel.getGraph1EdgeWeights().get(edge));
			double edgeWeight = edgeStat.getMean();
			mapEdgeWeight.put(edge, edgeWeight);
			if (maxEdgeWeight < edgeWeight) maxEdgeWeight = edgeWeight;
		}
		
		for (model.graph.Node node : (Collection<model.graph.Node>)(Collection<?>)visGraphModel.getNodes()) {
			GraphVisualizer.highlightNode(node, mapNodeWeight.get(node), COLOR_SCALES, GRAPH1_COLOR);
		}
		
		for (model.graph.Edge edge : (Collection<model.graph.Edge>)(Collection<?>)visGraphModel.getEdges()) {
			GraphVisualizer.highlightEdge(edge, mapEdgeWeight.get(edge), COLOR_SCALES, GRAPH1_COLOR);
		}
	}
	
	/*
	 * Create a visual graph for a differential graph.
	 * Statistical test is not applied here yet
	 */
	public static DirectedGraphInstance createDifferentialVisualization(DifferentialGraph dGraph) throws Exception {
		DirectedGraphInstance visGraphModel = dGraph.isDirected() ? new DirectedGraphInstance () : new SymmetricGraphInstance();
		Map<Node, model.graph.Node> nodeMap = new HashMap<>(); //store all the nodes
		Map<Edge, model.graph.Edge> edgeMap = new HashMap<>(); //store all the edges
		int LOW_FREQUENCY = dGraph.getSetting().getLowNumberOfObservations();
		
		for (Node node: dGraph.getNodes()) {
			model.graph.Node visNode = new model.graph.Node(node.getId()); //not yet set node weight
			visGraphModel.addNode(visNode);
			nodeMap.put(node, visNode);
			
			if (dGraph.getSetting().getWeightType() == WeightType.FREQUENCY) {
				boolean graph1Occur = false, graph2Occur = false;
				if ((boolean)node.getAttribute("graph1") == true) {
					double[] nodeValues = dGraph.getGraph1().getWeightValues(dGraph.getMapToGraph1Node().get(node));
					visGraphModel.getGraph1NodeWeights().put(visNode, nodeValues);
					if (GraphAnnotator.getExistentValues(nodeValues).length >= LOW_FREQUENCY) {
						graph1Occur = true;
					}
				}
				
				if ((boolean)node.getAttribute("graph2") == true) {
					double[] nodeValues = dGraph.getGraph2().getWeightValues(dGraph.getMapToGraph2Node().get(node));
					visGraphModel.getGraph2NodeWeights().put(visNode, nodeValues);
					if (GraphAnnotator.getExistentValues(nodeValues).length >= LOW_FREQUENCY) {
						graph2Occur = true;
					}
				}
					
				// low frequency or no occurrence in both graph 1 and 2
				if (!graph1Occur & !graph2Occur) {
					visGraphModel.getNodeOccurrences().put(visNode, 0); 
				}
				// occur in graph1 and low frequency or no occurrence in graph 2
				else if (graph1Occur & !graph2Occur) {
					visGraphModel.getNodeOccurrences().put(visNode, 1);  
				}
				// low frequency or no occurrence in graph 1 but strongly occur in graph 2
				else if (!graph1Occur & graph2Occur) {
					visGraphModel.getNodeOccurrences().put(visNode, 2); 
				}
				// strong occur in both graph 1 and 2
				else if (graph1Occur & graph2Occur) {
					visGraphModel.getNodeOccurrences().put(visNode, 3); //both graph1 and graph2
					if (dGraph.getSetting().getSamplingType() == SamplingType.WINDOW_BASED) { 
						double[] values1 = dGraph.getGraph1().getWeightValues(dGraph.getMapToGraph1Node().get(node));
						double[] values2 = dGraph.getGraph2().getWeightValues(dGraph.getMapToGraph2Node().get(node));
						visGraphModel.getNodeDiffWeights().put(visNode, GraphAnnotator.getPairedDiff(values1, values2));
					}
				}
			}
		}
		
		for (Edge edge: dGraph.getEdges()) {
			Node node1 = edge.getSource();
			Node node2 = edge.getTarget();
			model.graph.Edge visEdge = new EdgeInstance (nodeMap.get(node1), nodeMap.get(node2), null);		
			visGraphModel.addEdge (visEdge);
			edgeMap.put(edge, visEdge);
			
			boolean graph1Occur = false, graph2Occur = false;
			if ((boolean)edge.getAttribute("graph1") == true) {
				double[] edgeValues = dGraph.getGraph1().getWeightValues(dGraph.getMapToGraph1Edge().get(edge));
				visGraphModel.getGraph1EdgeWeights().put(visEdge, edgeValues);
				if (GraphAnnotator.getExistentValues(edgeValues).length >= LOW_FREQUENCY) {
					graph1Occur = true;
				}
			}
			
			if ((boolean)edge.getAttribute("graph2") == true) {
				double[] edgeValues = dGraph.getGraph2().getWeightValues(dGraph.getMapToGraph2Edge().get(edge));
				visGraphModel.getGraph2EdgeWeights().put(visEdge, edgeValues);
				if (GraphAnnotator.getExistentValues(edgeValues).length >= LOW_FREQUENCY) {
					graph2Occur = true;
				}
			}
			
			// low frequency or no occurrence in both graph 1 and 2
			if (!graph1Occur & !graph2Occur) {
				visGraphModel.getEdgeOccurrences().put(visEdge, 0); 
			}
			// occur in graph1 and low frequency or no occurrence in graph 2
			else if (graph1Occur && !graph2Occur) {
				visGraphModel.getEdgeOccurrences().put(visEdge, 1); 
			}
			// low frequency or no occurrence in graph 1 but strongly occur in graph 2
			else if (!graph1Occur && graph2Occur) {
				visGraphModel.getEdgeOccurrences().put(visEdge, 2); 
			}
			// strong occur in both graph 1 and 2
			else if (graph1Occur & graph2Occur) {
				visGraphModel.getEdgeOccurrences().put(visEdge, 3); 
				
				if (dGraph.getSetting().getSamplingType() == SamplingType.WINDOW_BASED) {
					double[] values1 = dGraph.getGraph1().getWeightValues(dGraph.getMapToGraph1Edge().get(edge));
					double[] values2 = dGraph.getGraph2().getWeightValues(dGraph.getMapToGraph2Edge().get(edge));
					visGraphModel.getEdgeDiffWeights().put(visEdge, GraphAnnotator.getPairedDiff(values1, values2));
				}
			}
			
		}
		
		return visGraphModel;
	}
	

	
	public static void highlightNode(model.graph.Node visNode, double relativeWeight, float[] colorScales, Color[] colors) {
		visNode.setWeightObject(ColorFading.blendColors(colorScales, colors, (float)relativeWeight));
	}
	
	public static void highlightLowFrequentNode(model.graph.Node visNode) {
		visNode.setWeightObject(LOW_FREQUENCY_COLOR);
	}
	
	public static void highlightLowFrequentEdge(model.graph.Edge visEdge) {
		visEdge.setEdgeObject(LOW_FREQUENCY_COLOR);
	}
	
	public static void highlightEdge(model.graph.Edge visEdge, double relativeWeight, float[] colorScales, Color[] colors) {
		visEdge.setEdgeObject(ColorFading.blendColors(colorScales, colors, (float)relativeWeight));
	}
	
	public static void highlightDifferentialNode(model.graph.Node node, double d) {
		node.setWeightObject(getColor(d));
	}
	
	public static void highlightSimilarNode(model.graph.Node node) {
		node.setWeightObject(NO_DIFFERENCE_COLOR);
	}
	
	public static void highlightDifferentialEdge(model.graph.Edge edge, double d) {
		edge.setEdgeObject(getColor(d));
	}
	
	public static void highlightSimilarEdge(model.graph.Edge edge) {
		edge.setEdgeObject(NO_DIFFERENCE_COLOR);
	}

	public static Color getColor(double d) {

		String answer = "";

		if (d > 0) {
			if (d < 0.6) {
				answer = "#abd9e9"; //light blue: 171, 217, 233
			}
			else if (d < 0.7) {
				answer = "#74add1"; //116, 173, 209
			} else if (d < 0.8) {
				answer = "#4575b4"; // 69, 117, 180
			} else {
				answer = "#313695"; //dark blue, 49 54 149
			}
		} else if (d < 0) {
			if (d > -0.6) {
				answer = "#fdae61"; //light red: 253, 174, 97
			} else if (d > -0.7) {
				answer = "#f46d43"; //244, 109, 67
			} else if (d > -0.8) {
				answer = "#d73027"; //215, 48, 39
			} else {
				answer = "#a50026"; //dark red: 165, 0, 38
			}
		}
		
	    int  r=  Integer.valueOf( answer.substring( 1, 3 ), 16 );
	    int  g=  Integer.valueOf( answer.substring( 3, 5 ), 16 );
	    int  b=  Integer.valueOf( answer.substring( 5, 7 ), 16 );
	    
	    return new Color(r,g,b);
	}
}
