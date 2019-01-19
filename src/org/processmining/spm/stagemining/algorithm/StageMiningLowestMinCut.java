package org.processmining.spm.stagemining.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.spm.stagemining.graph.Vertex2;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;

import com.aliasi.cluster.LinkDendrogram;

/**
 * 1st argument: log file
 * 2nd argument: minimum stage size
 * 3rd argument: the fullname of the class to return the ground truth from the input log file
 * @param args
 */
public class StageMiningLowestMinCut extends AbstractStageMiningAlgo {
	/**
	 * 1st argument: log file
	 * 2nd argument: minimum stage size
	 * 3rd argument: the fullname of the class to return the ground truth from the input log file
	 * @param args
	 */
//	public static void main(String[] args) {
//		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
//		try {
//			System.out.println("Import log file");
//			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + "\\" + args[0]));
//			
//			AbstractStageMiningAlgo algo = new StageMiningLowestMinCut();
//			algo.setDebug(true);
//			StageModel miner = new StageModel(log, Integer.valueOf(args[1]), algo);
//			miner.writeResult(System.getProperty("user.dir") + "//SPM_" + algo.getName() + "_" + 
//													LogUtils.getConceptName(log) + ".txt");
//			miner.printDecompositionTree();
//
//			ExampleClass example = (ExampleClass)Class.forName(args[2]).newInstance();
//			System.out.println("Transition nodes from beginning: " + miner.getStageListModel().getEndMilestoneLabels());
//			System.out.println("Stages = " + miner.getStageListModel().getStageLabels());
//			System.out.println("Ground Truth = " + example.getGroundTruth(log).toString());
//			System.out.println("Modularity = " + miner.getModularity());
//			double fowlkes = Measure.computeMeasure(miner.getStageListModel().getStageLabels(), example.getGroundTruth(log), 2);
//			System.out.println("Fowlkes–Mallows Index = " + fowlkes);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public String getName() {
		return "LowestMinCutThenHighestModularity";
	}
	
	@Override
	public DecompositionTree mine(WeightedDirectedGraph graph, int minStageSize) throws Exception {
		//-------------------------------
		// Compute candidate list
		//-------------------------------
		if (this.debug) System.out.println("Search for candidate cut-points");
		SortedSet<Vertex2> candidates = graph.searchCutPoints(graph);
		if (this.debug) System.out.println("Candidate cut points sorted by min-cut: " + candidates.toString());
		
		//-------------------------------
		// Take recursive graph cuts
		//-------------------------------
		if (this.debug) System.out.println("Build dendrograms");
		DecompositionTree tree = new DecompositionTree(graph);
		
		LinkDendrogram<IVertex> root = new LinkDendrogram<IVertex>(null, new HashSet<IVertex>(graph.getActivityVertices()), graph.getSource(), graph.getSink(), 0);
		tree.setRoot(root);
		List<LinkDendrogram<IVertex>> rootLevel = new ArrayList<LinkDendrogram<IVertex>>();
		rootLevel.add(root);
		tree.addBottomLevel(rootLevel, 0.0);
		List<LinkDendrogram<IVertex>> SD_Best = tree.getBottomLevel();
		double SD_Best_Mod = tree.getModularity(SD_Best);
		
		Iterator<Vertex2> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			Vertex2 v = iterator.next();
			if (this.debug) System.out.println("Check node: " + v.getName());
			
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
			if (this.debug) System.out.println("Stage1.size = " + stage1.size() + ", Stage2.size = " + stage2.size());
			
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
				if (newMod > SD_Best_Mod) {
					tree.addBottomLevel(bottomLevelTemp, newMod);
					SD_Best_Mod = newMod;
				}
				else {
					break;
				}
	    	}
			else {
				if (this.debug) {
					System.out.println("Cluster size is smaller than the minimum stage size!");
//		    		System.out.println("Cluster1=" + stage1.toString());
//		    		System.out.println("Cluster2=" + stage2.toString());
				}
	    	}
			
			iterator.remove();
		}
		
		return tree;
	}
	
	
	
	
}
