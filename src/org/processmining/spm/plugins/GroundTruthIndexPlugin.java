package org.processmining.spm.plugins;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.utils.Measure;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class GroundTruthIndexPlugin {
	@Plugin(name = "Calculate Ground-truth Index for Stage Decomposition", 
			parameterLabels = { "Log", "Stage Decomposition"}, 
			returnLabels = {"Ground-truth Index (Fowlkes-Mallows)"},
			returnTypes = {StageDecomposition.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Calculate Ground-truth Index for Stage Decomposition", 
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Calculate Ground-truth Index for Stage Decomposition", requiredParameterLabels = {0,1})
	public StageDecomposition edit(final UIPluginContext context, XLog log, StageDecomposition stageModel) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()/2];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}
		
		double size[][] = {{200, 400, TableLayoutConstants.FILL}, //columns 
							{25, 5, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #0
		panel.add(SlickerFactory.instance().createLabel("Ground truth class:"), "0,0");
		JComboBox<String> cboLogs = new JComboBox<String>();
		cboLogs.addItem("BPIC12");
		cboLogs.addItem("BPIC13");
		cboLogs.addItem("BPIC15");
		cboLogs.addItem("BPIC17");
		cboLogs.setPreferredSize(new Dimension(300, 25));
		cboLogs.setSize(new Dimension(300, 25));
		cboLogs.setMinimumSize(new Dimension(300, 25));
		
		String logName = log.getAttributes().get("concept:name").toString();
		if (logName.contains("BPI12")) {
			cboLogs.setSelectedItem("BPIC12");
		}
		else if (logName.contains("BPI13")) {
			cboLogs.setSelectedItem("BPIC13");
		}
		else if (logName.contains("BPI15")) {
			cboLogs.setSelectedItem("BPIC15");
		}
		else if (logName.contains("BPI17")) {
			cboLogs.setSelectedItem("BPIC17");
		}
		
		panel.add(cboLogs,"1,0");

		InteractionResult result = context.showConfiguration("Calculate Ground-truth Index - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		String gtClassOption = (String)cboLogs.getSelectedItem();
		
		String gtClassName = "";
		if (gtClassOption.equals("BPIC12")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2012";
		}
		else if (gtClassOption.equals("BPIC13")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2013";
		}
		else if (gtClassOption.equals("BPIC15")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2015";
		}
		else if (gtClassOption.equals("BPIC17")) {
			gtClassName = "org.processmining.spm.stagemining.groundtruth.StageModelBPI2017";
		}
		
		try {
			ExampleClass gtClass = (ExampleClass)Class.forName(gtClassName).newInstance();
			double gtIndex = Measure.computeMeasure(stageModel.getStageListModel().getActualStageLabels(), gtClass.getGroundTruth(log), 2); //fowls-mallows
			stageModel.setGroundTruthIndex(gtIndex);

			context.getFutureResult(0).setLabel("Stage Decomposition: " + 
					"Modularity=" + String.format("%.2f", stageModel.getModularity()) + 
					"; Fowlkes-Mallows=" + String.format("%.2f", stageModel.getGroundTruthIndex()));
			return stageModel;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
