package org.processmining.spm.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.hypergraph.abs.Vertex;
import org.processmining.spm.stagemining.graph.Vertex2;
import org.processmining.spm.stagemining.graph.WeightedDirectedEdge;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
//import com.mxgraph.examples.swing.GraphEditor;
//import com.mxgraph.examples.swing.editor.EditorMenuBar;
//import com.mxgraph.model.mxCell;
//import com.mxgraph.view.mxGraph;

public class GraphUtils {
    /**
     * Build JBPT graph from XLog
     * Events within a case in the log must be ordered by timestamps
     * Only take complete events into account. The relation is directly followed
     * The assumption is an activity is always completed before the next activity is started.
     * NOTE: ALL TRACES IN THE INPUT LOG MUST HAVE BEEN ADDED A COMMON START (named "start") AND END EVENT (named "end")
     * @param log
     * @throws ParseException 
     */
	public static WeightedDirectedGraph buildGraph(XLog log) throws ParseException {
		WeightedDirectedGraph graph = new WeightedDirectedGraph();
		graph.setName("original graph");
		
		//-------------------------------
		// Build the dependency matrix
		// Note that two default events are start and end event of every case.
		// The first index (row/column) of the matrix is for the start event
		// The last index is for the end event
		//-------------------------------
		Map<String,Integer> eventNameCountMap = new HashMap<String,Integer>();
		Map<Integer,String> countEventNameMap = new HashMap<Integer,String>();
		int index = 1; //must start from 1 since index 0 is for the start event 
		for (XTrace trace : log) {
			for (XEvent evt : trace) {
				String eventName = LogUtils.getConceptName(evt); //remove lowerCase: modified 2.7.2017
				if (eventName.equalsIgnoreCase("start") || eventName.equalsIgnoreCase("end")) continue;
				if (!eventNameCountMap.containsKey(eventName)) {
					eventNameCountMap.put(eventName, index);
					countEventNameMap.put(index, eventName);
					index++;
				}
			}
		}
		
		//--------------------------------
		// Create start/end event
		//--------------------------------
    	eventNameCountMap.put("start", 0);
    	countEventNameMap.put(0, "start");
    	eventNameCountMap.put("end", eventNameCountMap.size());
    	countEventNameMap.put(countEventNameMap.size(), "end");
		
		if (eventNameCountMap.size() == 0) return null;
		
		int[][] dependency = new int[eventNameCountMap.size()][eventNameCountMap.size()];
		
		for (XTrace trace : log) {
			XEvent preEvt = null;
			for (XEvent evt : trace) {
				if (!LogUtils.getLifecycleTransition(evt).equalsIgnoreCase("complete")) {
					continue;
				}
				String eventName = LogUtils.getConceptName(evt); //remove lowerCase: modified 2.7.2017
				if (preEvt != null) {
					String preEventName = LogUtils.getConceptName(preEvt);
					int preIndex = eventNameCountMap.get(preEventName);
					int curIndex = eventNameCountMap.get(eventName); //curIndex can be equal to preIndex for one-activity loop
					// This is absolute frequency.
					// For case frequency, need to keep track of cell [preIndex][curIndex] within a trace
					dependency[preIndex][curIndex] = dependency[preIndex][curIndex] + 1;
					preEvt = evt;
				}
				else {
					preEvt = evt;
				}
			}
			
		}
		
//		System.out.println(eventNameCountMap);
//		printMatrix(dependency);

		//-------------------------------
		// Make graph from dependency matrix
		//-------------------------------
		Vertex2[] arrVertex = new Vertex2[eventNameCountMap.size()];
		for (String eventName : eventNameCountMap.keySet()) {
			Vertex2 v = new Vertex2(eventName);
			arrVertex[eventNameCountMap.get(eventName)] = v;
			graph.addVertex(v);
			if (eventName.equalsIgnoreCase("start")) graph.setSource(v); //modified 2.7.2017: remove lowercase
			if (eventName.equalsIgnoreCase("end")) graph.setSink(v);
		}
		
		for (int i=0;i<eventNameCountMap.size();i++) {
			for (int j=0;j<eventNameCountMap.size();j++) { 
				if (dependency[i][j] > 0) {
					graph.addEdge(arrVertex[i], arrVertex[j], dependency[i][j]);
				}
			}
		}
		
		return graph;
	}
	
	/**
	 * The return graph has a matrix form
	 * The matrix has equal number of rows and columns, each row/column represents one vertex
	 * A cell represents an edge from the row (source) to the column (target)
	 * The first index is used for the source vertex
	 * The last index is for the sink vertex
	 * The cell that has an edge between the row and column is assigned the total weight between them.
	 * The cell that has no edge between the row and column is assigned 0.
	 * NOTE: the new graph representation has all exit edges removed except one 
	 * that is on the path from the start to the sink with statistically highest 
	 * number of distinct events per trace.
	 * @param g: the input graph
	 * @param vertexMap: to map from a vertex to the matrix column/row index
	 * @return: matrix-based graph
	 */
	public static int[][] buildBidiAdjacencyMatrix(WeightedDirectedGraph g, Map<IVertex,Integer> vertexMap, IVertex lastExitVertex) {
		int size = g.getVertices().size();
		int[][] matrix = new int[size][size];
		
		for (WeightedDirectedEdge<IVertex> e : g.getEdges()) {
			Vertex v1 = e.getSource();
			Vertex v2 = e.getTarget();
			
			if (v2 != v1) { // don't include self-loop
				int totalWeight = 0;
				for (WeightedDirectedEdge<IVertex> edge : g.getEdgesWithSourceAndTarget(v1, v2)) { 
					totalWeight += edge.getWeight();
				}
				for (WeightedDirectedEdge<IVertex> edge : g.getEdgesWithSourceAndTarget(v2, v1)) {
					totalWeight += edge.getWeight();
				}
				matrix[vertexMap.get(v1)][vertexMap.get(v2)] = totalWeight;
				matrix[vertexMap.get(v2)][vertexMap.get(v1)] = totalWeight;
			}
		}
		
		//Remove exit edges not recognized as a mainstream edge
		if (lastExitVertex != null) {
			int last = vertexMap.get(lastExitVertex);
			for (int i=0; i<matrix.length; i++) {
				if (i != last) {
					matrix[i][matrix.length-1] = 0;
					matrix[matrix.length-1][i] = 0;
				}
				else {
					matrix[i][matrix.length-1] = Integer.MAX_VALUE;
					matrix[matrix.length-1][i] = Integer.MAX_VALUE;
				}
			}
		}
		
		return matrix;
	}
	
	/*
	 * The first index is used for the source vertex
	 * The last index is for the sink vertex
	 */
	public static BiMap<IVertex,Integer> buildVertexMap(WeightedDirectedGraph g) {
		BiMap<IVertex,Integer> map = HashBiMap.create();
		map.put(g.getSource(), 0);
		map.put(g.getSink(), g.getVertices().size()-1);
		
		int index = 1;
		for (IVertex v : g.getVertices()) {
			if (v == g.getSource() || v == g.getSink()) continue;
			map.put(v, index);
			index++;
		}
		
		return map;
	}
	
	public static Set<WeightedDirectedEdge<IVertex>> removeSelfLoops(WeightedDirectedGraph graph) {
		Set<WeightedDirectedEdge<IVertex>> removedEdges = new HashSet<WeightedDirectedEdge<IVertex>>();
		
		for (Vertex v : graph.getVertices()) {
			WeightedDirectedEdge selfLoop = graph.getDirectedEdge((Vertex)v, (Vertex)v);
			if (selfLoop != null) removedEdges.add(selfLoop);
		}
		
        for (WeightedDirectedEdge e : removedEdges) {
        	graph.removeEdge(e);
        	graph.removeEdge2(e);
        }
        
		return removedEdges;
	}
	
	
	/**
	 * Return a matrix with two rows.
	 * The first row is the original row in the matrix-based graph 
	 * The second row is the original column in the matrix-based graph 
	 * @param g: the matrix-based graph
	 * @param v: the index of the vertex
	 * @return
	 */
	public static int[][] removeVertex(int[][] g, int v) {
		int[][] removes = new int[g[0].length][g[0].length];
		for (int i=0;i<g[0].length;i++) {
			removes[0][i] = g[v][i];
			removes[1][i] = g[i][v];
			g[v][i] = 0;
			g[i][v] = 0;
		}
		return removes;
	}
	
	/**
	 * Reconnect a vertex v and edges e to a graph
	 * @param g
	 * @param v
	 * @param e: two rows, first is the original row, second is the column
	 */
	public static void reconnect(int[][] g, int v, int[][] e) {
		for (int i=0;i<g[0].length;i++) {
			g[v][i] = e[0][i];
			g[i][v] = e[1][i];
		}
	}
	
	/**
	 * Assume the vertex v on graph g is removed, this method
	 * computes the min-cut on g after removing v based on max flow (Ford-Fulkerson)
	 * The matrix-based graph g must have the first index (0) as
	 * the source and the last index as the sink.
	 * @param bidiAdjacentMatrix: the bi-directional graph (without loops) for min-cut finding
	 * @param v: the vertex in consideration
	 * @return: first element of cutList is the min-cut value,
	 * other elements (if any) are string containing pair of vertices of edges 
	 * delimited by a comma.
	 */
	public static ArrayList<String> computeMinCut(int[][] bidiAdjacentMatrix, int v) {
		ArrayList<String> cutList = null;
		MaxFlow maxFlow = new MaxFlow(bidiAdjacentMatrix[0].length);
		int[][] reBidi = GraphUtils.removeVertex(bidiAdjacentMatrix, v);
		cutList = maxFlow.getMinCut(bidiAdjacentMatrix, 0, bidiAdjacentMatrix[0].length-1);
		GraphUtils.reconnect(bidiAdjacentMatrix, v, reBidi);
		
		return cutList;
	}
	
	public static void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
		    for (int j = 0; j < matrix[0].length; j++) {
		        System.out.print(String.format("%1$,.2f",matrix[i][j]) + "   ");
		    }
		    System.out.print("\n");
		}		
	}
	
	public static void printMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
		    for (int j = 0; j < matrix[0].length; j++) {
		        System.out.print(matrix[i][j] + "   ");
		    }
		    System.out.print("\n");
		}		
	}	
	
	public static Set<WeightedDirectedEdge<IVertex>> removeExitEdges(WeightedDirectedGraph g, IVertex lastExitVertex) {
		Set<WeightedDirectedEdge<IVertex>> removedExitEdges = new HashSet<WeightedDirectedEdge<IVertex>>();
		for (Vertex v : g.getDirectPredecessors((Vertex)g.getSink())) {
			if (v != lastExitVertex) {
				removedExitEdges.addAll(g.getEdgesWithSourceAndTarget(v, (Vertex)g.getSink()));
			}
		}
		g.removeEdges(removedExitEdges);
		return removedExitEdges;
	}
	
	public static Set<WeightedDirectedEdge<IVertex>> removeStartEdges(WeightedDirectedGraph g, IVertex firstStartVertex) {
		Set<WeightedDirectedEdge<IVertex>> removedExitEdges = new HashSet<WeightedDirectedEdge<IVertex>>();
		for (Vertex v : g.getDirectSuccessors((Vertex)g.getSource())) {
			if (v != firstStartVertex) {
				removedExitEdges.addAll(g.getEdgesWithSourceAndTarget((Vertex)g.getSource(), v));
			}
		}
		g.removeEdges(removedExitEdges);
		return removedExitEdges;
	}

	
	/*
	 * Get all edges connecting between set1 and set2
	 * Note that all edges in both directions, from set1 to set2 and vice versa.
	 * If set1 = set2, return all edges with source and target within set1.
	 */
	public static Set<WeightedDirectedEdge<IVertex>> getEdges(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		Set<WeightedDirectedEdge<IVertex>> result = new HashSet<WeightedDirectedEdge<IVertex>>();
		for (WeightedDirectedEdge<IVertex> e : g.getEdges()) {
			if ((set1.contains(e.getSource()) && set2.contains(e.getTarget())) || 
				(set1.contains(e.getTarget()) && set2.contains(e.getSource()))) {
				result.add(e);
			}
		}
		return result;
	}
	
	/**
	 * Get directed edge from set1 to set2
	 * @param g
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static Set<WeightedDirectedEdge<IVertex>> getDirectedEdges(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		Set<WeightedDirectedEdge<IVertex>> result = new HashSet<WeightedDirectedEdge<IVertex>>();
		for (WeightedDirectedEdge<IVertex> e : g.getEdges()) {
			if (set1.contains(e.getSource()) && set2.contains(e.getTarget())) {
				result.add(e);
			}
		}
		return result;
	}
	
	public static double getConnectionSize(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		return GraphUtils.getEdges(g, set1, set2).size();
	}
	
	/**
	 * Return the total weight on all edges between two set of vertices
	 * @param g
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static double getConnectionWeight(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		Set<WeightedDirectedEdge<IVertex>> edges = GraphUtils.getEdges(g, set1, set2);
		double weight = 0.0;
		for (WeightedDirectedEdge<IVertex> e : edges) {
			weight += e.getWeight();
		}
		return weight;
	}
	
	public static double getDirectedConnectionWeight(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		Set<WeightedDirectedEdge<IVertex>> edges = GraphUtils.getDirectedEdges(g, set1, set2);
		double weight = 0.0;
		for (WeightedDirectedEdge<IVertex> e : edges) {
			weight += e.getWeight();
		}
		return weight;
	}
	
	public static double getDirectedConnectionWeightFromSource (WeightedDirectedGraph g, IVertex source, Set<IVertex> stage) {
		double weight = 0.0;
		for (IVertex v : stage) {
			for (WeightedDirectedEdge<IVertex> e : g.getEdgesWithSourceAndTarget((Vertex)source, (Vertex)v)) {
				weight += e.getWeight();
			}
		}
		return weight;
	}
	
	public static double getDirectedConnectionWeightToSink (WeightedDirectedGraph g, Set<IVertex> stage, IVertex sink) {
		double weight = 0.0;
		for (IVertex v : stage) {
			for (WeightedDirectedEdge<IVertex> e : g.getEdgesWithSourceAndTarget((Vertex)v, (Vertex)sink)) {
				weight += e.getWeight();
			}
		}
		return weight;
	}
	
	public static double getDirectedConnectionNoWeight(WeightedDirectedGraph g, Set<IVertex> set1, Set<IVertex> set2) {
		Set<WeightedDirectedEdge<IVertex>> edges = GraphUtils.getDirectedEdges(g, set1, set2);
		return edges.size();
	}
	
	/**
	 * Remember: self-loops have been removed in g
	 * @param g
	 * @param phases
	 * @return
	 */
//	public static double computeModularity(WeightedDirectedGraph g, List<Set<IVertex>> phases) {
//		// e[i][j] is the number of edges between phase i and phase j
//		// e[i][i] is the number of edges within phase i
//		double [][] e = new double[phases.size()][phases.size()];
//		double graphTotalWeight = g.getTotalWeight();
//		for (int i=0;i<e.length;i++) {
//			for (int j=0;j<e.length;j++) {
//				e[i][j] = 1.0*GraphUtils.getConnectionWeight(g, phases.get(i), phases.get(j));
//			}
//		}
//		GraphUtils.printMatrix(e);
//		
//		// Compute modularity
//		double mod = 0;
//		for (int i=0;i<e.length;i++) { //for every stage
//			double a_i = 0;
//			for (int j=0;j<e.length;j++) {
////				if (j==i) continue;
//				a_i += e[j][i];
//			}
//			mod += 1.0*(e[i][i]/graphTotalWeight - (a_i*a_i)/(graphTotalWeight*graphTotalWeight)); 
//		}
//		
//		return mod;
//	}
	
	/**
	 * Compute modularity by transforming an original graph into a new graph
     * Every transition node is divided into two nodes
     * All edges connecting from/to the preceding stages are connected to the first node
     * All edges connecting from/to the succeeding stages are connected to the second node
     * The first node is associated with the preceding stage
     * The second node is associated with the succeeding stage
	 * @param oriGraph: the original graph
	 * @param phases: list of sets, each is a set of phase nodes, including transition nodes
	 * @param transitionNodes: the number of transition nodes is the number of phase minus 1 
	 * not including the source and the sink of the graph. 
	 * @return modularity degree
	 */
	public static double computeModularitySplitTransitionNode(WeightedDirectedGraph oriGraph, List<Set<IVertex>> phases, List<IVertex> transitionNodes) {
		List<Set<IVertex>> newPhases = new ArrayList<Set<IVertex>>();
		WeightedDirectedGraph newGraph = new WeightedDirectedGraph();
		
		// Copy the phases
		for (Set<IVertex> phase : phases) {
			Set<IVertex> newPhase = new HashSet<IVertex>();
			newPhase.addAll(phase);
			newPhases.add(newPhase);
		}
		
		// Add all vertices from the original graph
		for (Vertex v : oriGraph.getVertices()) {
				newGraph.addVertex(v);
		}
		
		// Add edges from the original graph
		for (WeightedDirectedEdge<IVertex> e : oriGraph.getEdges()) {
			if (newGraph.getVertices().contains(e.getSource()) && newGraph.getVertices().contains(e.getTarget())) {
				newGraph.addEdge(e.getSource(), e.getTarget(), e.getWeight());
			}
		}
		
		// Split the transition nodes into two nodes
		// Add new nodes to new phases and the new graph 
		// Add edges of the transition nodes to the new graph 
		for (int i=0;i<transitionNodes.size();i++) {
			Vertex transitNode = (Vertex)transitionNodes.get(i);
			
			//Add two new nodes and connect them by an edge with a weight of 0. 
			Vertex2 newNode1 = new Vertex2(transitNode.getName()+".1");
			newPhases.get(i).add(newNode1);
			newGraph.addVertex(newNode1);
			
			Vertex2 newNode2 = new Vertex2(transitNode.getName()+".2");
			newPhases.get(i+1).add(newNode2);
			newGraph.addVertex(newNode2);
			
			newGraph.addEdge(newNode1, newNode2, 0);
			
			//Transfer edges to/from the transition node to the two new nodes
			for (int j=0;j<phases.size();j++) {
				Vertex2 newNode;
				if (j <= i) { // preceding phases of transition node i, including containing phase
					newNode = newNode1;					
				}
				else {
					newNode = newNode2;
				}
				for (WeightedDirectedEdge<IVertex> e : newGraph.getEdgesWithSource(transitNode)) {
					if (newPhases.get(j).contains(e.getTarget())) {
						newGraph.addEdge(newNode, e.getTarget(), e.getWeight());
					}
				}
				for (WeightedDirectedEdge<IVertex> e : newGraph.getEdgesWithTarget(transitNode)) {
					if (newPhases.get(j).contains(e.getSource())) {
						newGraph.addEdge(e.getSource(), newNode, e.getWeight());
					}
				}
			}
			
			//Remove transition nodes and all its edges
			Set<WeightedDirectedEdge<IVertex>> transitEdges = new HashSet<WeightedDirectedEdge<IVertex>>();
			transitEdges.addAll(newGraph.getEdgesWithSource(transitNode));
			transitEdges.addAll(newGraph.getEdgesWithTarget(transitNode));
			newGraph.removeEdges(transitEdges);
			newGraph.removeVertex(transitNode);
			newPhases.get(i).remove(transitNode); 
		}
		
//		System.out.println("Input phases: " + phases.toString());
//		System.out.println("Transition nodes: " + transitionNodes.toString());
//		System.out.println("Total weight of the input graph=" + oriGraph.getTotalWeight());
		
		return GraphUtils.computeDirectedWeightedModularity(newGraph, newPhases);
		
	}
	
	public static double computeDirectedWeightedModularity(WeightedDirectedGraph g, List<Set<IVertex>> phases) {
		// e[i][j] is the number of edges between phase i and phase j
		// e[i][i] is the number of edges within phase i
		double [][] e = new double[phases.size()][phases.size()];
		double graphTotalWeight = g.getTotalWeight();
		//double graphTotalWeight = g.getEdges().size();
		for (int i=0;i<e.length;i++) {
			for (int j=0;j<e.length;j++) {
				e[i][j] = 1.0*GraphUtils.getDirectedConnectionWeight(g, phases.get(i), phases.get(j));
//				System.out.println("e["+i+"]"+"["+j+"]=" + e[i][j]);
			}
		}
		//GraphUtils.printMatrix(e);
		
		// Compute modularity
		double mod = 0;
		for (int i=0;i<e.length;i++) {
			double a_i = 0;
			for (int j=0;j<e.length;j++) {
//				if (j==i) continue;
				a_i += e[j][i];
			}
			mod += 1.0*(e[i][i]/graphTotalWeight  - (a_i*a_i)/(graphTotalWeight*graphTotalWeight));
		}
		
//		System.out.println("TotalWeight=" + graphTotalWeight);
//		System.out.println("Modularity=" + mod);
		
		return mod;
	}
}