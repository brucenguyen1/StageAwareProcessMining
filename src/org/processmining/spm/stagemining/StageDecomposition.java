package org.processmining.spm.stagemining;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.spm.stagemining.algorithm.AbstractStageMiningAlgo;
import org.processmining.spm.stagemining.algorithm.DecompositionTree;
import org.processmining.spm.stagemining.graph.Vertex2;
import org.processmining.spm.stagemining.graph.WeightedDirectedEdge;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.spm.utils.GraphUtils;
import org.processmining.spm.utils.LogUtils;

import com.aliasi.cluster.LinkDendrogram;

/**
 * A stage model contains a stage list model, the original graph, filtered graph
 * original log. Thus, it represents the full stage model mined from logs
 * @author Bruce
 *
 */
public class StageDecomposition {
	private StageListModel stageList = null;
	private WeightedDirectedGraph baseGraph = null;
	private XLog baseLog;
	private double groundTruthIndex = 0.0;
	private long miningTime=0;
	
	
	public StageDecomposition(XLog originalLog, int minStageSize, AbstractStageMiningAlgo miningAlgo) throws Exception {
		this.baseLog = originalLog;
		this.mine(minStageSize, miningAlgo);
	}
	
	/*
	 * First stage has start->{stageitem,stageitem...}->endmielstone
	 * Middlestage: startmilestone->{stageitems,stageitems...}->endmielstone
	 * Last stage: startmilestone->{stageitems,stageitems...}->end
	 */
	private void mine(int minStageSize, AbstractStageMiningAlgo miningAlgo) throws Exception {
		long startTime = System.currentTimeMillis();
		XLog startEndLog = LogUtils.addStartEndEvents(baseLog, "start", "end");
		//-------------------------------
		// Build graph from log
		//-------------------------------
		System.out.println("Build graph from log");
		baseGraph = GraphUtils.buildGraph(startEndLog);
		if (baseGraph==null) {
			throw new Exception("Bulding graph from log failed.");
		}			
		GraphUtils.removeSelfLoops(baseGraph);

		//---------------------------------------------
		// Mining stages
		//---------------------------------------------
		DecompositionTree decomposeTree = miningAlgo.mine(baseGraph, minStageSize);
		List<LinkDendrogram<IVertex>> bottomLevel = new ArrayList<LinkDendrogram<IVertex>>(decomposeTree.getBottomLevel());
		
		//---------------------------------------------
		// Populate stage model
		//---------------------------------------------
		XEventClasses eventClasses = XLogInfoFactory.createLogInfo(baseLog, XLogInfoImpl.NAME_CLASSIFIER).getEventClasses();
		stageList = new StageListModel();
		for (LinkDendrogram<IVertex> d : bottomLevel) {
			Set<IVertex> realMembers = new HashSet<IVertex>(d.getMemberSet());
			realMembers.remove(d.getSource());
			realMembers.remove(d.getSink());
			
			Stage stage = new Stage(stageList);
			
			Milestone startmilestone;
			int index = bottomLevel.indexOf(d);
			if (index == 0) {
				startmilestone = new Milestone(d.getSource().getLabel(), d.getSource(), 
						eventClasses.getByIdentity(d.getSource().getLabel()), d.getSource() != baseGraph.getSource());
			}
			else {
				startmilestone = stageList.get(index-1).getEndMilestone();
			}
			stage.setStartMilestone(startmilestone);
			
			for (IVertex v : realMembers) {
				StageItem item = new StageItem(v.getLabel(), v, eventClasses.getByIdentity(v.getLabel()));
				stage.add(item);
			}
			
			Milestone endmilestone = new Milestone(d.getSink().getLabel(), d.getSink(), 
												eventClasses.getByIdentity(d.getSink().getLabel()), d.getSink() != baseGraph.getSink());
			stage.setEndMilestone(endmilestone);
			
			stageList.add(stage);
		}
		
		// Clean stages as each may contain the end milestones of other stages
		// since during decomposition the cutpoints are included in the preceding stage
		List<String> endMilestones = stageList.getEndMilestoneLabels();
		for (Stage stage : stageList) {
			Set<StageItem> tobeRemoved = new HashSet<>();
			for (StageItem stageItem : stage) {
				if (endMilestones.contains(stageItem.getLabel())) {
					tobeRemoved.add(stageItem);
				}
			}
			for (StageItem item : tobeRemoved) {
				stage.remove(item);
			}
		}
		
		//---------------------------------------------
		// Extract stage logs
		//---------------------------------------------
//		this.extractSubLogs();
        for (Stage stage : stageList) {
        	stage.extractStageLog(this.baseLog);
        }
		
		this.miningTime = System.currentTimeMillis() - startTime;
	}
	
	/*
	 * Create stage from a stage decomposition editor
	 */
	public StageDecomposition(StageDecomposition currentStageModel, StageModelEditConfig modelConfig) {
		// Set key attributes
		this.baseLog = currentStageModel.getBaseLog();
		this.baseGraph = currentStageModel.getBaseGraph();
		this.groundTruthIndex = 0.0; // not applicable to manually edited stage model
		this.miningTime = 0; // not applicable to manually edited stage model
		
		
		// Create a list of stage groups, each group is a merge of stages and will be a new stage
		// E.g. <<stage1>,<stage2>,<stage3,stage4>>: stage3 and stage4 are merged.
		List<List<StageEditConfig>> stageGroups = new ArrayList<>();
		List<StageEditConfig> currentStageGroup = new ArrayList<>();
		if (!modelConfig.isEmpty()) currentStageGroup.add(modelConfig.get(0));
		
		for (int i=1;i<modelConfig.size();i++) {
			StageEditConfig currentStage = modelConfig.get(i);
			if (currentStage.isMergeWithPrevStage()) {
				currentStageGroup.add(currentStage);
			}
			else {
				stageGroups.add(currentStageGroup);
				currentStageGroup = new ArrayList<>();
				currentStageGroup.add(currentStage);
			}
			
			if (i == modelConfig.size()-1) { //Always add the last stage group
				stageGroups.add(currentStageGroup);
			}
		}
		
		
		// Create stage list from the list of stage groups, each group is a new stage
		stageList = new StageListModel();
		for (int i=0;i<stageGroups.size();i++) {
			List<StageEditConfig> currentGroup = stageGroups.get(i);
			Stage stage = new Stage(stageList);
			
			// Start Milestone
			// If this is the first group, the new start milestone must be the source of the base graph
			// Otherwise, the new start milestone is the end milestone of the preceding new stage
			Milestone newStartMilestone = null;
			if (i==0) {
				Milestone currentStartMilestone = currentStageModel.getStageListModel().getFirstMilestone();
				newStartMilestone = new Milestone(currentStartMilestone.getLabel(), 
													currentStartMilestone.getNode(), 
													currentStartMilestone.getEventClass(), false);
			}
			else {
				newStartMilestone = stageList.get(i-1).getEndMilestone();
//				newStartMilestone = new Milestone(precedingEndMilestone.getLabel(), 
//												precedingEndMilestone.getNode(), 
//												precedingEndMilestone.getEventClass(), true);
//				newStartMilestone.setBelongToPrevStage(precedingEndMilestone.isBelongToPrevStage());
			}
			stage.setStartMilestone(newStartMilestone);
			
			// Add StageItem: add from merged stages
			// Remember to also add the intermediary milestone between two consecutive stages
			// But it can be an artificial milestone as a stage is moved to the middle
			for (StageEditConfig currentStage : currentGroup) {
				stage.addAll(currentStage.getStageItems());
				// Add the end milestone of merged stages to the new stage except the last stage
				if (currentGroup.size() >= 2 && currentStage != currentGroup.get(currentGroup.size()-1)) {
					Milestone stageEndMilestone = currentStage.getCurrentStage().getEndMilestone();
					if (stageEndMilestone.getNode() != baseGraph.getSink()) { // do not take the fake 'end' milestone
						stage.add(new StageItem(stageEndMilestone.getLabel(), stageEndMilestone.getNode(), 
													stageEndMilestone.getEventClass()));
					}
				}
			}
			
			// End Milestone
			// If this is the last group, the new end milestone must be the sink of the base graph
			// Otherwise, the new end milestone is the one the user chose for the last stage in the group
			Milestone newEndMilestone = null;
			if (i == stageGroups.size()-1) {
				Milestone currentEndMilestone = currentStageModel.getStageListModel().getLastMilestone();
				newEndMilestone = new Milestone(currentEndMilestone.getLabel(), 
														currentEndMilestone.getNode(), 
														currentEndMilestone.getEventClass(), false);
				newEndMilestone.setBelongToPrevStage(false);
			}
			else {
				StageEditConfig lastStageConfig = currentGroup.get(currentGroup.size()-1);
				StageItem selectedStageItem = lastStageConfig.getEndMilestone();
				newEndMilestone = new Milestone(selectedStageItem.getLabel(), 
												selectedStageItem.getNode(), 
												selectedStageItem.getEventClass(), true);
				newEndMilestone.setBelongToPrevStage(lastStageConfig.isEndMilestoneBelongToPrevStage());
			
			}
			stage.setEndMilestone(newEndMilestone);
			
			// If the milestone of the last stage in the current group has changed, then the previous milestone
			// must be added as a stage item to the stage 
//			StageEditConfig lastStageConfig = currentGroup.get(currentGroup.size()-1);
//			Milestone previousMilestone = lastStageConfig.getCurrentStage().getEndMilestone();
//			if (!previousMilestone.getLabel().equalsIgnoreCase(newEndMilestone.getLabel())) {
//				stage.add(new StageItem(previousMilestone.getLabel(), previousMilestone.getNode(), 
//						previousMilestone.getEventClass()));
//			}
			
			stageList.add(stage);
		}
		
		// Go through the stage list to remove stage item chosen to be milestone
		for (Stage stage : stageList) {
			Set<StageItem> tobeRemoved = new HashSet<>();
			for (StageItem stageItem : stage) {
				if (stageItem.getLabel().equalsIgnoreCase(stage.getStartMilestone().getLabel()) ||
						stageItem.getLabel().equalsIgnoreCase(stage.getEndMilestone().getLabel())) {
					tobeRemoved.add(stageItem);
				}
			}
			for (StageItem item : tobeRemoved) {
				stage.remove(item);
			}
		}
		
		for (Stage stage : stageList) {
        	stage.extractStageLog(this.baseLog);
        }
	}
	
	public XLog getBaseLog() {
		return this.baseLog;
	}
	
//	public int getMinStageSize() {
//		return this.minStageSize;
//	}
	
	public final WeightedDirectedGraph getBaseGraph() {
		return this.baseGraph;
	}
	
	public StageListModel getStageListModel() {
		return this.stageList;
	}
	
	public long getMiningTime() {
		return this.miningTime;
	}
	
//	public double getModularity() throws Exception {
//		return decomposeTree.getModularity(decomposeTree.getBestLevelIndex());
//	}
	
	public double getModularity() throws Exception {
		List<Set<IVertex>> oneLevelPhases = new ArrayList<Set<IVertex>>(); // list of phases (or stages) at the current level
		List<IVertex> transitionNodes = new ArrayList<IVertex>();
		for (Stage stage : this.getStageListModel()) {	
			Set<IVertex> oneLevelSet = new HashSet<IVertex>(stage.getStageItemNodes()); // set of nodes in the current phase at the current level
			if (stage.getEndMilestone().getNode() != baseGraph.getSink()) {
				Vertex2 tNode = (Vertex2) stage.getEndMilestone().getNode(); // the cut point (or transition node)
				transitionNodes.add(tNode);
			}
			// Add the source and the sink of the graph
			if (stage.getStartMilestone().getNode() == baseGraph.getSource()) oneLevelSet.add(stage.getEndMilestone().getNode());
			if (stage.getEndMilestone().getNode() == baseGraph.getSink()) oneLevelSet.add(stage.getEndMilestone().getNode());
			
			oneLevelPhases.add(oneLevelSet);
		}

		return GraphUtils.computeModularitySplitTransitionNode(baseGraph, oneLevelPhases, transitionNodes);
	}
	
	public void setGroundTruthIndex(double index) {
		this.groundTruthIndex = index;
	}
	
	public double getGroundTruthIndex() {
		return this.groundTruthIndex;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Integer> getJumpingForwardEdges() throws Exception {
		Object[] jumpingInterStageEdges = this.getJumpingInterstageEdges();
		return (Map<String,Integer>)jumpingInterStageEdges[0];
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Integer> getJumpingBackwardEdges() throws Exception {
		Object[] jumpingInterStageEdges = this.getJumpingInterstageEdges();
		return (Map<String,Integer>)jumpingInterStageEdges[1];
	}
	
	/**
	 * Extract raw traces for each stage. Most traces start and end with
	 * milestone events, but some exceptional traces will not 
	 * @return
	 * @throws Exception
	 */
//	private List<XLog> extractSubLogs() throws Exception {
//		List<XLog> sublogs = new ArrayList<>();
//        XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
//        List<Set<String>> activitySets = this.getStageListModel().getAllStageLabels();
//
//        for (int i=0;i<activitySets.size();i++) {
//        	XAttributeMap attMap = factory.createAttributeMap();
//        	attMap.put("concept:name", factory.createAttributeLiteral("concept:name", LogUtils.getConceptName(baseLog).toString()+ "_" + (i+1), null));
//        	sublogs.add(factory.createLog(attMap));
//        }
//        
//        // Extract sublogs
//        // Traces in a sublog will have format: Sx-1<some events>Sx-2, 
//        // Sx-1 and Sx-2 are two fake events representing transition nodes
//        for (XTrace trace : this.baseLog) {
//        	String traceID = LogUtils.getConceptName(trace);
//        	XTrace newTrace = factory.createTrace();
//        	int traceCounter = 0;
//            for (int i=0;i<trace.size();i++) {
//            	XEvent e = trace.get(i);
//            	if (!LogUtils.getLifecycleTransition(e).toLowerCase().equals("complete")) continue;
//            	
//            	String eName = LogUtils.getConceptName(e);
//            	int s = this.getStageListModel().findStageIndex(eName);
//            	XEvent newEvent = factory.createEvent(e.getAttributes());
//            	newEvent.getAttributes().put("stage", factory.createAttributeLiteral("stage", s+"", null));
//            	
//            	if (i>0) {
//                	XEvent e_1 = trace.get(i-1);
//            		String e_1_Name = LogUtils.getConceptName(e_1);
//            		int s_1 = this.getStageListModel().findStageIndex(e_1_Name);
//            		
//            		// Extract stage trace if a change of stage is detected
//            		if (s != s_1) {
////	        			newTrace.add(0, this.createFakeEvent("S"+s_1+"-1", LogUtils.getTimestamp(newTrace.get(0)), 
////	        												-1000, factory, s_1+""));
////	            		newTrace.add(this.createFakeEvent("S"+s_1+"-2", LogUtils.getTimestamp(newTrace.get(newTrace.size()-1)), 
////	            											1000, factory, s_1+""));
//	            		
//	            		traceCounter++;
//	            		LogUtils.setConceptName(newTrace, traceID + "-" + traceCounter);
//	        			sublogs.get(s_1-1).add(newTrace);
//	        			newTrace = factory.createTrace();
//            		}
//            	}
//            	newTrace.add(newEvent);
//            	
//            	//Add trace of the last stage 
//            	if (i == trace.size()-1) {
////            		newTrace.add(0, this.createFakeEvent("S"+s+"-1", LogUtils.getTimestamp(newTrace.get(0)), 
////							-1000, factory, s+""));
////            		newTrace.add(this.createFakeEvent("S"+s+"-2", LogUtils.getTimestamp(newTrace.get(newTrace.size()-1)), 
////							1000, factory, s+""));
//					traceCounter++;
//					LogUtils.setConceptName(newTrace, traceID + "-" + traceCounter);
//					sublogs.get(s-1).add(newTrace);
//            	}            	
//            }
//            
//        }
//    
//        
//        return sublogs;
//	}
	
	public Map<String,Integer> getJumpingStartEdges() throws Exception {
		Map<String,Integer> result = new HashMap<>();
		Stage firstStage = this.getStageListModel().get(0);
		for (WeightedDirectedEdge<IVertex> edge : this.getBaseGraph().getEdges()) {
			if (edge.getSource() == this.getBaseGraph().getSource()) {
				// Don't take the edges from source node to the first stage
				// since they have been captured in the first sublog
				if (!firstStage.getStageItemLabels().contains(edge.getTarget().getName()) &&
						!firstStage.getStartMilestone().getLabel().equalsIgnoreCase(edge.getTarget().getName()) &&
						!firstStage.getEndMilestone().getLabel().equalsIgnoreCase(edge.getTarget().getName())) {
					result.put(edge.getTarget().getName(), (int)edge.getWeight());
				}
			}
		}
		return result;
	}
	
	public Map<String,Integer> getJumpingExitEdges() throws Exception {
		Map<String,Integer> result = new HashMap<>();
		Stage lastStage = this.getStageListModel().get(this.getStageListModel().size()-1); 
		for (WeightedDirectedEdge<IVertex> edge : this.getBaseGraph().getEdges()) {
			if (edge.getTarget() == this.getBaseGraph().getSink()) {
				// Don't take the edges from the last stage to the sink node since
				// they have been captured in the last sublog
				if (!lastStage.getStageItemLabels().contains(edge.getSource().getName()) &&
					!lastStage.getStartMilestone().getLabel().equalsIgnoreCase(edge.getSource().getName()) &&
					!lastStage.getEndMilestone().getLabel().equalsIgnoreCase(edge.getSource().getName())) {
					result.put(edge.getSource().getName(), (int)edge.getWeight());
				}
			}
		}
		return result;
	}
	
	private Object[] getJumpingInterstageEdges() throws Exception {
		Map<String,Integer> jumpingInterstageForwardEdges = new HashMap<>();
		Map<String,Integer> jumpingInterstageBackwardEdges = new HashMap<>();
		List<String> endMilestones = this.getStageListModel().getEndMilestoneLabels();
		for (WeightedDirectedEdge<IVertex> e : this.getBaseGraph().getEdges()) {
			if (e.getSource() != this.getBaseGraph().getSource() && e.getTarget() != this.getBaseGraph().getSink()) {
//				int srcStageIndex = -1, tgtStageIndex = -1;
				int srcStageIndex = this.getStageListModel().findStageIndex(e.getSource().getName())-1;
				int tgtStageIndex = this.getStageListModel().findStageIndex(e.getTarget().getName())-1;

				// jumping forward
				String key = e.getSource().getName().toLowerCase() + "@" + e.getTarget().getName();
				if (tgtStageIndex > srcStageIndex) {
					// Jumping forward edges from the end milestone is not taken 
					// since it has been captured in the sublog
					if (!e.getSource().getName().equalsIgnoreCase(endMilestones.get(srcStageIndex)) || 
							tgtStageIndex > (srcStageIndex + 1)) {
						jumpingInterstageForwardEdges.put(key, (int)e.getWeight());
					}
				}
				// jumping backward
				else if (tgtStageIndex < srcStageIndex) {
					// Different from jumping forward edges, all backward edges are 
					// collected because these connections are not captured in sublogs
					jumpingInterstageBackwardEdges.put(key, (int)e.getWeight());
				}
			}
		}
		
		Object[] result = new Object[2];
		result[0] = jumpingInterstageForwardEdges;
		result[1] = jumpingInterstageBackwardEdges;
		
		return result;
	}
	
	public void writeResult(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = null;
		writer = new PrintWriter(fileName, "UTF-8");
		writer.println("Total Time: " + this.getMiningTime() + " milliseconds");
	    writer.println("Clusters: ");
	    writer.println(this.getStageListModel().getActualStageLabels().toString());
	    writer.close();
	}
	
}

