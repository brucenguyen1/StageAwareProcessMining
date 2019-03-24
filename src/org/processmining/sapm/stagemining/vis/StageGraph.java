package org.processmining.sapm.stagemining.vis;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.sapm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.sapm.stagemining.model.Stage;
import org.processmining.sapm.stagemining.model.StageDecomposition;
import org.processmining.sapm.utils.GraphUtils;


/**
 * This graph is used to visualize the stage model in ProM 
 * @author Bruce
 *
 */
public class StageGraph extends AbstractDirectedGraph<StageNode, StageEdge<? extends StageNode, ? extends StageNode>> {
	private StageDecomposition stageModel = null;
	private WeightedDirectedGraph baseGraph;
	
	private int numberOfInitialNodes;
	private Set<StageEdge<? extends StageNode, ? extends StageNode>> edges = new HashSet<>();
	private StageNode[] nodeAliasMap;
	
	public StageGraph(int numberOfInitialNodes) {
		this.numberOfInitialNodes = numberOfInitialNodes;
		nodeAliasMap = new StageNode[numberOfInitialNodes];
		edges = new HashSet<StageEdge<? extends StageNode, ? extends StageNode>>();
		getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
	}
	
	/**
	 * Bruce added 10 December 2016
	 * @param nodeSetList: each set of nodes can be the start, stage, transition or end node
	 * @param graph
	 */
	public StageGraph(StageDecomposition stageModel) {
		this.numberOfInitialNodes = stageModel.getStageListModel().size()*2 +1;
		nodeAliasMap = new StageNode[numberOfInitialNodes];
		getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
		
		this.stageModel = stageModel;
		this.baseGraph = stageModel.getBaseGraph();
		List<Set<IVertex>> nodeSetList = this.getNodeClusters(stageModel);
		ArrayList<StageNode> nodeList = new ArrayList<StageNode>();

		//----------------------------------------
		// Convert from the input node set list to stage nodes
		// Note that the node list starts with start node, then a stage node
		// then a transition node, then a stage node, then transition node,...
		// then a stage node, then an end node.
		//----------------------------------------
		int nodeNumber = 0;
		double maxWeight = 0.;
		int stageNumber = 1;
		for(Set<IVertex> nodeSet : nodeSetList){
			if (nodeNumber == 0 || nodeNumber == nodeSetList.size()-1){ // start and end node
				StageNode milestoneNode = new StageNode(this, nodeNumber, nodeSet.iterator().next().getName(), 0);
				this.addNode(milestoneNode, nodeNumber);
				nodeList.add(milestoneNode);
			}
			else if (nodeNumber % 2 == 0){ // Milestone nodes
				StageNode milestoneNode = new StageNode(this, nodeNumber, nodeSet.iterator().next().getName(), 2);
				this.addNode(milestoneNode, nodeNumber);
				nodeList.add(milestoneNode);
			}
			else { // stage nodes
				maxWeight += GraphUtils.getDirectedConnectionWeightFromSource(stageModel.getBaseGraph(), 
												stageModel.getBaseGraph().getSource(), nodeSet);
				String label = "<html>";
				boolean first = true;
				label += "<h2>" + stageNumber + "</h2>";
				for(IVertex element: nodeSet){
					//System.out.println(element);
					label += (first ? "" : "<br>") + element.getName();
					first = false;
				}
				label += "</html>";
				StageNode node = new StageNode(this, nodeNumber, label, 3);
				this.addNode(node, nodeNumber);
				nodeList.add(node);
				stageNumber++;
			}
			nodeNumber++;
		}

		//----------------------------------------
		// Add edges
		//----------------------------------------
		int sourceCounter = 0;
		double weight = 0;
		for(Set<IVertex> stage1 : nodeSetList){
			int targetCounter = 0;
			for(Set<IVertex> stage2 : nodeSetList){
				if(sourceCounter!=targetCounter){ 
				   double origWeight = 0;
				   // Start node
				   if(sourceCounter==0){
						origWeight = GraphUtils.getDirectedConnectionWeightFromSource(stageModel.getBaseGraph(),
														stageModel.getBaseGraph().getSource(), stage2);
				   }
				   // End node
				   else if(targetCounter==nodeSetList.size()-1){
						origWeight = GraphUtils.getDirectedConnectionWeightToSink(stageModel.getBaseGraph(),stage1, 
														stageModel.getBaseGraph().getSink());
				   }
				   // Stage or transition node
				   else{
						origWeight = GraphUtils.getDirectedConnectionWeight(stageModel.getBaseGraph(), stage1, stage2);
				   }
					weight = origWeight/maxWeight;
//					System.out.println("STAGE 1 = "+stage1);
//					System.out.println("STAGE 2 = "+stage2);
//					System.out.println("weight = "+weight);
//					System.out.println("===============");
					
					if (weight>0.){
						this.addEdge(nodeList.get(sourceCounter), nodeList.get(targetCounter), origWeight, weight);
					}

				}
				targetCounter++;
			}
			sourceCounter++;
		}
	}
	
	public StageGraph(List<Set<IVertex>> sources, List<Set<IVertex>> targets, WeightedDirectedGraph graph) {
		this.numberOfInitialNodes = sources.size()+1;
		nodeAliasMap = new StageNode[numberOfInitialNodes];
		getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
		this.baseGraph = graph;
		ArrayList<StageNode> nodeList = new ArrayList<StageNode>();

		int nodeNumber = 0;
		double maxWeight = 0.;
		for(Set<IVertex> nodeSet : sources) {
			if (nodeNumber==0) {
				StageNode startNode = new StageNode(this, nodeNumber, "start", 0);
				this.addNode(startNode, nodeNumber);
				nodeList.add(startNode);
				nodeNumber++;
			} 
			else if(nodeNumber==sources.size()-1) {
				StageNode endNode = new StageNode(this, nodeNumber, "end", 1);
				this.addNode(endNode, nodeNumber);
				nodeList.add(endNode);
			}
			else {
				maxWeight = maxWeight + GraphUtils.getDirectedConnectionWeightFromSource(graph, graph.getSource(), nodeSet);
				String label = "<html>";
				boolean first = true;
				for(IVertex element: nodeSet){
					//System.out.println(element);
					label += (first ? "" : "<br>") + element.getName();
					first = false;
				}
				label += "</html>";
				StageNode node = new StageNode(this, nodeNumber, label, 3);
				this.addNode(node, nodeNumber);
				nodeList.add(node);
				nodeNumber++;
			}
		}

		int sourceCounter = 0;

		double weight = 0;
		for(Set<IVertex> stage1 : sources){
			int targetCounter = 0;
			for(Set<IVertex> stage2 : targets){
				if(sourceCounter!=targetCounter){
				   double origWeight = 0;
					if(sourceCounter==0){
						origWeight = GraphUtils.getDirectedConnectionWeightFromSource(graph,graph.getSource(), stage2);
					}else if(targetCounter==targets.size()-1){
						origWeight = GraphUtils.getDirectedConnectionWeightToSink(graph,stage1, graph.getSink());
					}else{
						origWeight = GraphUtils.getDirectedConnectionWeight(graph, stage1, stage2);
					}
					System.out.println("STAGE 1 = "+stage1);
					System.out.println("STAGE 2 = "+stage2);
					System.out.println("weight = "+weight);
					System.out.println("===============");
//					System.out.println("MAX W = "+maxWeight);
//					System.out.println(stage1+"and"+stage2+" = "+weight);
					weight = origWeight/maxWeight;
					//					System.out.println("Weight: "+ weight);
					if(weight>0.){
						this.addEdge(nodeList.get(sourceCounter), nodeList.get(targetCounter), origWeight, weight);
						System.out.println("STAGE 1 = "+nodeList.get(sourceCounter).getLabel());
						System.out.println("STAGE 2 = "+nodeList.get(targetCounter).getLabel());
						System.out.println("weight = "+weight);
						System.out.println("###############");
					}

				}
				targetCounter++;
			}
			sourceCounter++;
		}
	}
	
	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}
	
	public static String format(double number) {
		return numberFormat.format(number);
	}
	
//	public synchronized StageEdgeImpl addStageEdge(StageNode source, StageNode target) {
//		checkAddEdge(source, target);
//		StageEdgeImpl edge = new StageEdgeImpl(source, target, significance, correlation);
//		if (StageEdges.add(edge)) {
//			graphElementAdded(edge);
//			return edge;
//		} 
//		return null;
//	}
	
	
	private List<Set<IVertex>> getNodeClusters(StageDecomposition stageModel) {
	    //---------------------------------------------
		// Build the node set list which contains in order
	    // the start node, stage and transition nodes, and end node.
		//---------------------------------------------
		List<Set<IVertex>> nodeClusterList = new ArrayList<Set<IVertex>>();
		
		//Add start, stages, transitions, and end node
		//Note that the last stage has a fake end milestone
		for (Stage stage : stageModel.getStageListModel()) {
			// Only show the start milestone for the first stage 
			if (stage == stageModel.getStageListModel().get(0)) {
				Set<IVertex> startMilestone = new HashSet<>();
				startMilestone.add(stage.getStartMilestone().getNode());
				nodeClusterList.add(startMilestone);
			}
			
			// Stage nodes
			nodeClusterList.add(stage.getStageItemNodes()); 
			
			// The end milestone
			Set<IVertex> endMilestone = new HashSet<>();
			endMilestone.add(stage.getEndMilestone().getNode());
			nodeClusterList.add(endMilestone); 
		}
		
		return nodeClusterList;
	}

	
	public StageDecomposition getStageModel() {
		return this.stageModel;
	}
	
	public WeightedDirectedGraph getBaseGraph() {
		return this.baseGraph;
	}
	
//	public void setGroundTruthIndex(double index) {
//		this.groundTruthIndex = index;
//	}
//	public double getGroundTruthIndex() {
//		return this.groundTruthIndex;
//	}

	public synchronized void addNode(StageNode node, int index) {
		nodeAliasMap[index] = node;
		graphElementAdded(node);
	}
	
	public void removeNode(DirectedGraphNode node) {
		if (node instanceof StageNode) {
			StageNode stageNode = (StageNode) node;
			//First delete node and its ingoing and outgoing edges in JGraph
			removeSurroundingEdges(stageNode);
			graphElementRemoved(node);
			
			int removeIndex = stageNode.getIndex();
			nodeAliasMap[removeIndex] = null;	
		}
	}

	public Set<StageNode> getNodes() {
		HashSet<StageNode> activeNodes = new HashSet<StageNode>();
		for (int i = 0; i < numberOfInitialNodes; i++) {
			if (nodeAliasMap[i] != null) {
				activeNodes.add(nodeAliasMap[i]);
			}
		}
		return activeNodes;
	}

	protected AbstractDirectedGraph<StageNode, StageEdge<? extends StageNode, ? extends StageNode>> getEmptyClone() {
		return null;
	}

	protected Map<? extends DirectedGraphElement, ? extends DirectedGraphElement> cloneFrom(
			DirectedGraph<StageNode, StageEdge<? extends StageNode, ? extends StageNode>> graph) {
		HashMap<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

		for (StageNode node : graph.getNodes()) {
			mapping.put(node, new StageNode((StageGraph) graph, node.getIndex(), node.getLabel(), 3));
		}
		getAttributeMap().clear();
		AttributeMap map = graph.getAttributeMap();
		for (String key : map.keySet()) {
			getAttributeMap().put(key, map.get(key));
		}
		return mapping;
	}
	
	public StageEdge<StageNode, StageNode> addEdge(StageNode source, StageNode target, double weight, double relativeWeight) {
		checkAddEdge(source, target);
		StageEdge<StageNode, StageNode> edge = new StageEdge<StageNode, StageNode>(source, target, weight, relativeWeight);
		if (edges.add(edge)) {
			graphElementAdded(edge);
			return edge;
		} 
		assert (false);
		return null;
	}

	public void removeEdge(DirectedGraphEdge edge) {
		if (edge instanceof StageEdge) {
			edges.remove(edge);
		} else {
			assert (false);
		}
		graphElementRemoved(edge);
	}

	public Set<StageEdge<? extends StageNode, ? extends StageNode>> getEdges() {
		return this.edges;
	}
	
}
