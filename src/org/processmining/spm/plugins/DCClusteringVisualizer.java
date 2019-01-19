package org.processmining.spm.plugins;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.stagemining.ui.StagedProcessGraph;
import org.processmining.spm.utils.GraphUtils;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.Measure;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DCClusteringVisualizer {
	@Plugin(name = "Visualize Activity Clustering of Divide and Conquer Plugins", 
			parameterLabels = {"Log", "ActivityClusterArray"}, 
			returnLabels = { "Staged Process Graph Panel"}, 
			returnTypes = { StagedProcessGraph.class}, 
			userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Visualize Activity Clustering of Divide and Conquer Plugins")
	@PluginVariant(variantLabel = "Visualize Activity Clustering of Divide and Conquer Plugins", requiredParameterLabels = { 0,1 })
	public StagedProcessGraph visualize(UIPluginContext context, XLog log, ActivityClusterArray clusterArray) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()/2];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}

		double size[][] = {{200, 600, TableLayoutConstants.FILL}, {300, 25, TableLayoutConstants.FILL}};
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #1
		// ground truth class
		panel.add(SlickerFactory.instance().createLabel("Ground truth class:"), "0,0");
		JComboBox<String> combo1 = new JComboBox<String>();
		combo1.addItem("BPI12");
		combo1.addItem("BPI13");
		combo1.addItem("BPI15");
		combo1.addItem("BPI17");
		combo1.setPreferredSize(new Dimension(300, 25));
		combo1.setSize(new Dimension(300, 25));
		combo1.setMinimumSize(new Dimension(300, 25));
		
		String logName = log.getAttributes().get("concept:name").toString();
		if (logName.contains("BPI12")) {
			combo1.setSelectedItem("BPI12");
		}
		else if (logName.contains("BPI13")) {
			combo1.setSelectedItem("BPI13");
		}
		else if (logName.contains("BPI15")) {
			combo1.setSelectedItem("BPI15");
		}
		else {
			combo1.setSelectedItem("BPI17");
		}
		
		panel.add(combo1,"1,0");

		InteractionResult result = context.showConfiguration("DC Clustering Visualization - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		StagedProcessGraph graph = this.createVisualGraph(log, clusterArray, (String)combo1.getSelectedItem());
		
		return graph;
	}
	
	public StagedProcessGraph createVisualGraph(XLog log, ActivityClusterArray clusterArray, String gtClassOption) {
		String gtClassName = "";
		if (gtClassOption.equals("BPI12")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2012";
		}
		else if (gtClassOption.equals("BPI13")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2013";
		}
		else if (gtClassOption.equals("BPI15")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2015";
		}
		else {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2017";
		}
		
		List<Set<IVertex>> nodeSetList = new ArrayList<Set<IVertex>>();
		List<Set<String>> labelSetList = new ArrayList<>();
		
		Set<IVertex> start  = new HashSet<IVertex>();
		Set<IVertex> end  = new HashSet<IVertex>();
		
		ExampleClass gtClass = null;
		double gtIndex = 0.0;
		WeightedDirectedGraph graph = null;
		
		try {
			XLog startEndLog = LogUtils.addStartEndEvents(log, "start", "end");
			graph = GraphUtils.buildGraph(startEndLog);
			if (graph==null) {
				throw new Exception("Error when constructing grap from the event log.");
			}
			GraphUtils.removeSelfLoops(graph);
			
			start.add(graph.getSource());
			end.add(graph.getSink());
			
			nodeSetList.add(start);
			
			// Add real clusters from the ActivityClusterArray
			for (Set<XEventClass> cluster : clusterArray.getClusters()) {
				Set<IVertex> graphCluster = new HashSet<>();
				Set<String> labelSet = new HashSet<>();
				for (XEventClass eventClass : cluster) {
					graphCluster.add(graph.getVertexByName(eventClass.getId()));
					labelSet.add(eventClass.getId());
				}
				nodeSetList.add(graphCluster);
				labelSetList.add(labelSet);
			}
			
			nodeSetList.add(end);
			
			gtClass = (ExampleClass)Class.forName(gtClassName).newInstance();
			gtIndex = Measure.computeMeasure(labelSetList, gtClass.getGroundTruth(log), 2); //fowls-mallows
		} catch (Exception e) {
			e.printStackTrace();
		}

		//---------------------------------------------
		// Build staged process graph
		//---------------------------------------------
		StagedProcessGraph fg = new StagedProcessGraph(nodeSetList, nodeSetList, graph);
		fg.setGroundTruthIndex(gtIndex);
		
		return fg;
	}
}