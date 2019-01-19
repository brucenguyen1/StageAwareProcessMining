package org.processmining.spm.stagemining.algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.spm.stagemining.graph.Vertex2;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.utils.Measure;
import org.processmining.spm.utils.OpenLogFilePlugin;

import com.aliasi.cluster.LinkDendrogram;
//import com.rapidminer.RapidMiner;

/**
 * This class mine phase models based on min-cut
 * @author Bruce
 *
 */
public class StageMiningHighestModularity extends AbstractStageMiningAlgo {
	
	/**
	 * 1st argument: log file
	 * 2nd argument: minimum stage size
	 * 3rd argument: number of stages
	 * 4th argument: the fullname of the class to return the ground truth from the input log file
	 * @param args
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		try {
			System.out.println("Import log file");
			String fileName = "BPI13incidents_completecases.xes.gz";
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + "\\" + "BPIC15_1_HOOFD_cleantime.xes"));
			
			AbstractStageMiningAlgo algo = new StageMiningHighestModularity();
			algo.setDebug(false);
			
			XEventClasses eventClasses = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER).getEventClasses();
			
			for (int minSS = 2;minSS<(eventClasses.size()/2);minSS++) {
				StageDecomposition stageModel = new StageDecomposition(log, minSS, algo);
//				miner.writeResult(System.getProperty("user.dir") + "//SPM_" + algo.getName() + "_" + 
//														LogUtils.getConceptName(log) + ".txt");
//				miner.printDecompositionTree();
	
				ExampleClass example = (ExampleClass)Class.forName("org.processmining.spm.stagemining.groundtruth.StageModelBPI2015").newInstance();
//				ExampleClass example = (ExampleClass)Class.forName(args[2]).newInstance();
	//			System.out.println("Transition nodes from beginning: " + miner.getStageListModel().getEndMilestoneLabels());
	//			System.out.println("Stages = " + miner.getStageListModel().getStageLabels());
	//			System.out.println("Ground Truth = " + example.getGroundTruth(log).toString());
	//			System.out.println("Modularity = " + miner.getModularity());
				double gtIndex = Measure.computeMeasure(stageModel.getStageListModel().getActualStageLabels(), example.getGroundTruth(log), 2);
	//			System.out.println("Fowlkes–Mallows Index = " + fowlkes);
				System.out.println("MinSS: " + minSS + ", Stages: " + stageModel.getStageListModel().size() + ", Mod: " + stageModel.getModularity() + ", Ground-truth index: " + gtIndex);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getName() {
		return "HighestModularity";
	}
	
	@Override
	public DecompositionTree mine(WeightedDirectedGraph graph, int minStageSize) throws Exception {
		//-------------------------------
		// Compute candidate list
		//-------------------------------
		SortedSet<Vertex2> candidates = graph.searchCutPoints(graph);
		if (this.debug) System.out.println("Candidate cut points sorted by min-cut: " + candidates.toString());
		
		//-------------------------------
		// Take recursive graph cuts
		//-------------------------------
		
		// Initialize the dendrogram with root node
		// NOTE: root node contains all nodes in the graph except the source and the sink
		DecompositionTree tree = new DecompositionTree(graph);
		LinkDendrogram<IVertex> root = new LinkDendrogram<IVertex>(null, new HashSet<IVertex>(graph.getActivityVertices()), graph.getSource(), graph.getSink(), 0);
		tree.setRoot(root);
		List<LinkDendrogram<IVertex>> rootLevel = new ArrayList<LinkDendrogram<IVertex>>();
		rootLevel.add(root);
		tree.addBottomLevel(rootLevel, 0.0);
		List<LinkDendrogram<IVertex>> SD = tree.getBottomLevel();
		
		double SDMod = tree.getModularity(SD);
		Vertex2 SDNode = null;
		Set<Vertex2> currentCandidates = new HashSet<Vertex2>(candidates); 
		Set<Vertex2> bestNodes = new HashSet<Vertex2>();
		
		while (!currentCandidates.isEmpty()) {
			for (Vertex2 v : currentCandidates) {
				if (this.debug) System.out.println();
				if (this.debug) System.out.println("Check node:" + v.getName());
				
				// Find a stage containing the node to cut
		    	LinkDendrogram<IVertex> selected = null;
		    	for (LinkDendrogram<IVertex> d : tree.getBottomLevel()) {
					if (d.getMemberSet().contains(v)) {
						selected = d;
						break;
					}
				}
				if (selected == null) {
					throw new Exception("Cannot find a containing cluster at the bottom level of the decomposition tree for node " + v.getName());
				}
				
				// Take graph cut
				List<Set<IVertex>> cutResult = tree.graphCut(v, selected);
				Set<IVertex> stage1 = cutResult.get(0);
				Set<IVertex> stage2 = cutResult.get(1);
				if (this.debug) {
					System.out.println("Stage1.size = " + stage1.size() + ", Stage2.size = " + stage2.size());
					System.out.println("Stage1=" + stage1.toString());
					System.out.println("Stage2=" + stage2.toString());
				}
				
		    	//Check min stage size
		    	if (stage1.size() >= minStageSize && stage2.size() >= minStageSize) { 
			    	//Create the new bottom level
			    	LinkDendrogram<IVertex> dendro1 = new LinkDendrogram<IVertex>(selected, stage1, selected.getSource(), v, v.getMinCut());
					LinkDendrogram<IVertex> dendro2 = new LinkDendrogram<IVertex>(selected, stage2, v, selected.getSink(), v.getMinCut());
			    	List<LinkDendrogram<IVertex>> bottomLevelTemp = new ArrayList<LinkDendrogram<IVertex>>(tree.getBottomLevel());
			    	int index = bottomLevelTemp.indexOf(selected);
			    	bottomLevelTemp.remove(selected);
			    	bottomLevelTemp.add(index, dendro1);
			    	bottomLevelTemp.add(index+1, dendro2);	    	
					
					//Compute modularity and the best bottom level
					double newMod = tree.computeModularity(bottomLevelTemp);
					if (this.debug) System.out.println("Modularity = " + newMod);
					if (newMod > SDMod) {
						SD = bottomLevelTemp;
						SDNode = v;
						SDMod = newMod;
					}
					else {
						if (this.debug) System.out.println("** Modularity is decreasing");
					}
		    	}
		    	else {
		    		if (this.debug) {
		    			System.out.println("Cluster size is smaller than the minimum stage size!");
//			    		System.out.println("Cluster1=" + stage1.toString());
//			    		System.out.println("Cluster2=" + stage2.toString());
		    		}
		    	}
			}
			
			//Attach the best bottom level to the decomposition tree
			if (SD != tree.getBottomLevel()) {
				if (this.debug) System.out.println("BEST TRANSITION NODE: " + SDNode.getName() + ". Modularity = " + SDMod);
				if (this.debug) System.out.println("**********************************************");
				tree.addBottomLevel(SD, SDMod);
				bestNodes.add(SDNode);
				currentCandidates = new HashSet<Vertex2>(candidates);
				currentCandidates.removeAll(bestNodes);
			}
			else {
				//System.out.println("No more valid cut points found from the candidate set. Current candidate set=" + currentCandidates.toString());
				break;
			}
		}

		return tree;
	}
	
}
