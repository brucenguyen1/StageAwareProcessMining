package org.processmining.spm.stagemining.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.spm.stagemining.Stage;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.spm.stagemining.fuzzymodel.FMNode;
import org.processmining.spm.stagemining.fuzzymodel.MutableFuzzyGraph;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.spm.utils.GraphUtils;

//Bruce added December 10, 2016
public class StagedProcessGraph extends MutableFuzzyGraph {
	private StageDecomposition stageModel = null;
	private double groundTruthIndex = 0.0;
	private WeightedDirectedGraph baseGraph;
	
	/**
	 * Bruce added 10 December 2016
	 * @param nodeSetList: each set of nodes can be the start, stage, transition or end node
	 * @param graph
	 */
	public StagedProcessGraph(StageDecomposition stageModel) {
		this(stageModel.getStageListModel().size()*2 +1); //number of graph nodes, each node is either a stage or a milestone
		this.stageModel = stageModel;
		this.baseGraph = stageModel.getBaseGraph();
		List<Set<IVertex>> nodeSetList = this.getNodeClusters(stageModel);
		ArrayList<FMNode> nodeList = new ArrayList<FMNode>();

		//----------------------------------------
		// Convert from the input node set list to Fuzzy node list
		// Note that the node list starts with start node, then a stage node
		// then a transition node, then a stage node, then transition node,...
		// then a stage node, then an end node.
		//----------------------------------------
		int nodeNumber = 0;
		double maxWeight = 0.;
		int stageNumber = 1;
		for(Set<IVertex> nodeSet : nodeSetList){
			if (nodeNumber == 0 || nodeNumber == nodeSetList.size()-1){ // start and end node
				FMNode milestoneNode = new FMNode(this, nodeNumber, nodeSet.iterator().next().getName(), 0);
				this.addNode(milestoneNode, nodeNumber);
				nodeList.add(milestoneNode);
			}
			else if (nodeNumber % 2 == 0){ // Milestone nodes
				FMNode milestoneNode = new FMNode(this, nodeNumber, nodeSet.iterator().next().getName(), 2);
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
				FMNode node = new FMNode(this, nodeNumber, label);
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
						this.addStageEdge(nodeList.get(sourceCounter), nodeList.get(targetCounter), origWeight, weight);
					}

				}
				targetCounter++;
			}
			sourceCounter++;
		}
	}
	
	public StagedProcessGraph(List<Set<IVertex>> sources, List<Set<IVertex>> targets, WeightedDirectedGraph graph) {
		super(sources.size()+1);
		this.baseGraph = graph;
		ArrayList<FMNode> nodeList = new ArrayList<FMNode>();

		int nodeNumber = 0;
		double maxWeight = 0.;
		for(Set<IVertex> nodeSet : sources){
			if(nodeNumber==0){
				FMNode startNode = new FMNode(this, nodeNumber, "start", 0);
				this.addNode(startNode, nodeNumber);
				nodeList.add(startNode);
				nodeNumber++;
			} else{
				if(nodeNumber==sources.size()-1){
					FMNode endNode = new FMNode(this, nodeNumber, "end", 0);
					this.addNode(endNode, nodeNumber);
					nodeList.add(endNode);
				}else{
					maxWeight = maxWeight + GraphUtils.getDirectedConnectionWeightFromSource(graph, graph.getSource(), nodeSet);
					String label = "<html>";
					boolean first = true;
					for(IVertex element: nodeSet){
						//System.out.println(element);
						label += (first ? "" : "<br>") + element.getName();
						first = false;
					}
					label += "</html>";
					FMNode node = new FMNode(this, nodeNumber, label);
					this.addNode(node, nodeNumber);
					nodeList.add(node);
					nodeNumber++;
				}
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
						this.addStageEdge(nodeList.get(sourceCounter), nodeList.get(targetCounter), origWeight, weight);
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
	
	public StagedProcessGraph(int numberOfInitialNodes) {
		super(numberOfInitialNodes);
	}
	
	public StageDecomposition getStageModel() {
		return this.stageModel;
	}
	
	public WeightedDirectedGraph getBaseGraph() {
		return this.baseGraph;
	}
	
	public void setGroundTruthIndex(double index) {
		this.groundTruthIndex = index;
	}
	public double getGroundTruthIndex() {
		return this.groundTruthIndex;
	}
	
}
