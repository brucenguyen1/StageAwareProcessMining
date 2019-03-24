package org.processmining.sapm.stagemining.plugins;

import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.sapm.stagemining.vis.StageGraph;
import org.processmining.sapm.utils.ClusteringUtils;
import org.processmining.sapm.utils.GraphUtils;
import org.processmining.sapm.utils.LogUtils;

public class DCClusteringVisualizer {
	@Plugin(name = "Visualize Activity Clustering of Divide and Conquer Plugins", 
			parameterLabels = {"Log", "ActivityClusterArray"}, 
			returnLabels = { "Staged Process Graph Panel"}, 
			returnTypes = { StageGraph.class}, 
			userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Visualize Activity Clustering of Divide and Conquer Plugins",
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Visualize Activity Clustering of Divide and Conquer Plugins", requiredParameterLabels = { 0,1 })
	public StageGraph visualize(UIPluginContext context, XLog log, ActivityClusterArray clusterArray) {
		try {
			WeightedDirectedGraph graph = GraphUtils.buildProcessGraph(log);
			List<Set<IVertex>> nodeSetList = ClusteringUtils.getNodeClustering(clusterArray, graph);
			StageGraph stageGraph = new StageGraph(nodeSetList, nodeSetList, graph);
			
			List<Set<String>> activitySetList = ClusteringUtils.getActivityClustering(clusterArray);
			double gtIndex = LogUtils.compareWithLogGroundTruth(activitySetList, log);
			context.getFutureResult(0).setLabel("DC Activity Clustering Visualization. " + "Fowlkes-Mallows=" + String.format("%.2f", gtIndex));
			return stageGraph;			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}
		
	}
}