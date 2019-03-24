package org.processmining.sapm.stagemining.plugins;

import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.sapm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.sapm.stagemining.vis.StageGraph;
import org.processmining.sapm.utils.ClusteringUtils;
import org.processmining.sapm.utils.GraphUtils;
import org.processmining.sapm.utils.LogUtils;

public class SPDClusteringVisualizer {
	@Plugin(name = "Visualize Activity Clustering of SPD Miner plugin", 
			parameterLabels = {"Simple Precedence Diagram (SPD)","Log"}, 
			returnLabels = { "Activity Clustering Visualization"}, 
			returnTypes = { StageGraph.class}, 
			userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Visualize Activity Clustering of SPD Miner plugin",
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Visualize Activity Clustering of SPD Miner plugin", requiredParameterLabels = { 0,1 })
	public StageGraph visualize(UIPluginContext context, SPD spd, XLog log) {
		try {
			WeightedDirectedGraph graph = GraphUtils.buildProcessGraph(log);
			List<Set<IVertex>> nodeSetList = ClusteringUtils.getNodeClustering(spd, graph);
			StageGraph stageGraph = new StageGraph(nodeSetList, nodeSetList, graph);
			
			List<Set<String>> activitySetList = ClusteringUtils.getActivityClustering(spd);
			double gtIndex = LogUtils.compareWithLogGroundTruth(activitySetList, log);
			context.getFutureResult(0).setLabel("SPD Activity Clustering. " + "Fowlkes-Mallows=" + String.format("%.2f", gtIndex));
			return stageGraph;	
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}
	}
	
//	private StageGraph createVisualGraph(XLog log, SPD spd, String gtClassOption) {
//		String gtClassName = "";
//		if (gtClassOption.equals("BPI12")) {
//			gtClassName = "org.processmining.sapm.stagemining.groundtruth.StageModelBPI2012";
//		}
//		else if (gtClassOption.equals("BPI13")) {
//			gtClassName = "org.processmining.sapm.stagemining.groundtruth.StageModelBPI2013";
//		}
//		else if (gtClassOption.equals("BPI15")) {
//			gtClassName = "org.processmining.sapm.stagemining.groundtruth.StageModelBPI2015";
//		}
//		else {
//			gtClassName = "org.processmining.sapm.stagemining.groundtruth.StageModelBPI2017";
//		}
//		
//		List<Set<IVertex>> nodeSetList = new ArrayList<Set<IVertex>>();
//		List<Set<String>> labelSetList = new ArrayList<>();
//		
//		Set<IVertex> start  = new HashSet<IVertex>();
//		Set<IVertex> end  = new HashSet<IVertex>();
//		
//		ExampleClass gtClass = null;
//		double gtIndex = 0.0;
//		WeightedDirectedGraph graph = null;
//		
//		try {
//			XLog startEndLog = LogUtils.addStartEndEvents(log, "start", "end");
//			graph = GraphUtils.buildGraph(startEndLog);
//			if (graph==null) {
//				throw new Exception("Error when constructing grap from the event log.");
//			}
//			GraphUtils.removeSelfLoops(graph);
//			
//			start.add(graph.getSource());
//			end.add(graph.getSink());
//			
//			nodeSetList.add(start);
//			
//			// Add real activity clusters from the SPD
//			for (SPDNode node: spd.getNodes()) {
//				Set<String> activitySet = getActivitySet(node);
//				Set<IVertex> graphCluster = new HashSet<>();
//				labelSetList.add(activitySet);
//				for (String activityName : activitySet) {
//					graphCluster.add(graph.getVertexByName(activityName));
//				}
//				nodeSetList.add(graphCluster);
//			}
//			
//			nodeSetList.add(end);
//			
//			gtClass = (ExampleClass)Class.forName(gtClassName).newInstance();
//			gtIndex = Measure.computeMeasure(labelSetList, gtClass.getGroundTruth(log), 2); //fowls-mallows
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		//---------------------------------------------
//		// Build staged process graph
//		//---------------------------------------------
//		StageGraph fg = new StageGraph(nodeSetList, nodeSetList, graph);
//		fg.setGroundTruthIndex(gtIndex);
//		
//		return fg;
//	}
	
	/**
	 * 
	 * @param node: the node label has general format: <html>A<br>B<br>C</html>
	 * @return
	 */
//	private Set<String> getActivitySet(SPDNode node) {
//		
//		String nodeString = node.getLabel();
//		String newNodeString = nodeString.substring(6); //remove <html>, e.g. A<br>B<br>C</html>
//		newNodeString = newNodeString.substring(0, newNodeString.length() - 7); //remove </html>, e.g. A<br>B<br>C
//		String[] activityArray = newNodeString.split("<br>"); // return [A,B,C]
//		
//		Set<String> activitySet = new HashSet<>();
//		for (int i=0;i<activityArray.length;i++) {
//			activitySet.add(activityArray[i]);
//		}
//		return activitySet;
//	}
}