package org.processmining.spm.plugins;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.stagemining.ui.StagedProcessGraph;
import org.processmining.spm.utils.GraphUtils;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.Measure;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SPDClusteringVisualizer {
	@Plugin(name = "Visualize Activity Clustering of SPD Miner plugin", 
			parameterLabels = { "Log", "Simple Precedence Diagram (SPD)"}, 
			returnLabels = { "Activity Clustering Visualization"}, 
			returnTypes = { StagedProcessGraph.class}, 
			userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Visualize Activity Clustering of SPD Miner plugin")
	@PluginVariant(variantLabel = "Visualize Activity Clustering of SPD Miner plugin", requiredParameterLabels = { 0,1 })
	public StagedProcessGraph visualize(UIPluginContext context, XLog log, SPD spd) {
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

		InteractionResult result = context.showConfiguration("SPD Clustering Visualization - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		StagedProcessGraph graph = createVisualGraph(log, spd, (String)combo1.getSelectedItem());
		context.getFutureResult(0).setLabel("SPD Activity Clustering. " + "Fowlkes-Mallows=" + String.format("%.2f", graph.getGroundTruthIndex()));
		return graph;
	}
	
	private StagedProcessGraph createVisualGraph(XLog log, SPD spd, String gtClassOption) {
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
			
			// Add real activity clusters from the SPD
			for (SPDNode node: spd.getNodes()) {
				Set<String> activitySet = getActivitySet(node);
				Set<IVertex> graphCluster = new HashSet<>();
				labelSetList.add(activitySet);
				for (String activityName : activitySet) {
					graphCluster.add(graph.getVertexByName(activityName));
				}
				nodeSetList.add(graphCluster);
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
	
	/**
	 * 
	 * @param node: the node label has general format: <html>A<br>B<br>C</html>
	 * @return
	 */
	private Set<String> getActivitySet(SPDNode node) {
		
		String nodeString = node.getLabel();
		String newNodeString = nodeString.substring(6); //remove <html>, e.g. A<br>B<br>C</html>
		newNodeString = newNodeString.substring(0, newNodeString.length() - 7); //remove </html>, e.g. A<br>B<br>C
		String[] activityArray = newNodeString.split("<br>"); // return [A,B,C]
		
		Set<String> activitySet = new HashSet<>();
		for (int i=0;i<activityArray.length;i++) {
			activitySet.add(activityArray[i]);
		}
		return activitySet;
	}
}