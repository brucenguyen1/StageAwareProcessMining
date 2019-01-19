package org.processmining.spm.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.baselines.DCDecomposeWrapper;
import org.processmining.spm.stagemining.ui.StagedProcessGraph;

public class DCClusteringPlugin {

	@Plugin(name = "Divide and Conquer Activity Clustering", 
			parameterLabels = { "Log" }, 
			returnLabels = { "Visualization of Activity Cluster Array"}, 
			returnTypes = { StagedProcessGraph.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen, F.M. Maggi", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Divide and Conquer Activity Clustering", 
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Divide and Conquer Activity Clustering", requiredParameterLabels = { 0 })
	public StagedProcessGraph mineSP(final UIPluginContext context, XLog log) {
		String logName = log.getAttributes().get("concept:name").toString();
		String gtClassOption = "";
		if (logName.contains("12")) {
			gtClassOption = "BPI12";
		}
		else if (logName.contains("13")) {
			gtClassOption = "BPI13";
		}
		else if (logName.contains("15")) {
			gtClassOption = "BPI15";
		}
		else {
			gtClassOption = "BPI17";
		}
		
		DCDecomposeWrapper dc = new DCDecomposeWrapper();
		ActivityClusterArray clusters = dc.findBestClustering(context, log);
		
		DCClusteringVisualizer visualizer = new DCClusteringVisualizer();
		StagedProcessGraph graph = visualizer.createVisualGraph(log, clusters, gtClassOption);
		context.getFutureResult(0).setLabel("DC Activity Clustering Visualization. " + "Fowlkes-Mallows=" + String.format("%.2f", graph.getGroundTruthIndex()));
		return graph;
	}	
}
