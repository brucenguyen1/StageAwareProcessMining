package org.processmining.sapm.stagemining.plugins;

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
import org.processmining.sapm.stagemining.algorithm.AbstractStageMiningAlgo;
import org.processmining.sapm.stagemining.algorithm.StageMiningExhaustive;
import org.processmining.sapm.stagemining.algorithm.StageMiningHighestModularity;
import org.processmining.sapm.stagemining.algorithm.StageMiningLowestMinCut;
import org.processmining.sapm.stagemining.model.StageDecomposition;
import org.processmining.sapm.utils.LogUtils;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class StageMiningPlugin {

	@Plugin(name = "Stage Mining", 
			parameterLabels = { "Log" }, 
			returnLabels = { "Stage Decomposition"}, 
			returnTypes = { StageDecomposition.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen, F.M. Maggi", 
			email = "huanghuy.nguyen@hdr.qut.edu.au, F.M.Maggi@tue.nl", 
			uiLabel = "Stage Mining", 
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Stage Mining", requiredParameterLabels = { 0 })
	public StageDecomposition mineSP(final UIPluginContext context, XLog log) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()/2];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}
		
		double size[][] = {{200, 400, TableLayoutConstants.FILL}, //columns 
							{25, 5, 25, 5, 25, 5, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #0
		panel.add(SlickerFactory.instance().createLabel("Stage mining algorithm:"), "0,0");
		JComboBox<String> cboAlgo = new JComboBox<String>();
		cboAlgo.addItem("Highest Modularity");
		cboAlgo.addItem("Lowest Cut-value");
		cboAlgo.addItem("Exhaustive");
		cboAlgo.setPreferredSize(new Dimension(300, 25));
		cboAlgo.setSize(new Dimension(300, 25));
		cboAlgo.setMinimumSize(new Dimension(300, 25));
		cboAlgo.setSelectedItem("Highest Modularity");
		panel.add(cboAlgo,"1,0"); //"column,row"
		
		// row #1
		panel.add(SlickerFactory.instance().createLabel("Minimum stage size:"), "0,2");
		JComboBox<Integer> cboStageSize = new JComboBox<Integer>(options);
		cboStageSize.setPreferredSize(new Dimension(100, 25));
		cboStageSize.setSize(new Dimension(100, 25));
		cboStageSize.setMinimumSize(new Dimension(100, 25));
		cboStageSize.setSelectedItem(new Integer(2));
		panel.add(cboStageSize,"1,2"); //column,row
		
		// row #2
//		panel.add(SlickerFactory.instance().createLabel("Ground truth class:"), "0,4");
//		JComboBox<String> cboLogs = new JComboBox<String>();
//		cboLogs.addItem("BPI12");
//		cboLogs.addItem("BPI13");
//		cboLogs.addItem("BPI15");
//		cboLogs.addItem("BPI17");
//		cboLogs.addItem("");
//		cboLogs.setPreferredSize(new Dimension(300, 25));
//		cboLogs.setSize(new Dimension(300, 25));
//		cboLogs.setMinimumSize(new Dimension(300, 25));
//		
//		String logName = log.getAttributes().get("concept:name").toString();
//		if (logName.contains("BPI12")) {
//			cboLogs.setSelectedItem("BPI12");
//		}
//		else if (logName.contains("BPI13")) {
//			cboLogs.setSelectedItem("BPI13");
//		}
//		else if (logName.contains("BPI15")) {
//			cboLogs.setSelectedItem("BPI15");
//		}
//		else if (logName.contains("BPI17")) {
//			cboLogs.setSelectedItem("BPI17");
//		}
//		else {
//			cboLogs.setSelectedItem("");
//		}
//		
//		panel.add(cboLogs,"1,4");

		InteractionResult result = context.showConfiguration("Stage Mining - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		
		try {
			StageDecomposition decomp = mineGeneric(context, log, (String)cboAlgo.getSelectedItem(), 
													(Integer)cboStageSize.getSelectedItem());
			context.getFutureResult(0).setLabel("Stage Decomposition: " + 
					"Modularity=" + String.format("%.2f", decomp.getModularity()) + 
					"; Fowlkes-Mallows=" + String.format("%.2f", decomp.getGroundTruthIndex()));
			return decomp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public StageDecomposition mineGeneric(final UIPluginContext context, XLog log, String algo, int minStageSize) throws Exception {
		AbstractStageMiningAlgo miningAlgo = null;
		if (algo.equals("Highest Modularity")) {
			miningAlgo = new StageMiningHighestModularity();
		}
		else if (algo.equals("Lowest Cut-value")) {
			miningAlgo = new StageMiningLowestMinCut();
		}
		else if (algo.equals("Exhaustive")) {
			miningAlgo = new StageMiningExhaustive();
		}
		
		miningAlgo.setDebug(true);
		StageDecomposition stageDecomp = new StageDecomposition(log, minStageSize, miningAlgo);
		stageDecomp.writeResult(System.getProperty("user.dir") + "//SPM_" + algo + "_" + LogUtils.getConceptName(log) + ".txt");
		
		return stageDecomp;
		
		

	}
}
