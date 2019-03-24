package org.processmining.sapm.processdiscovery.plugins;

import java.awt.Dimension;

import javax.swing.JCheckBox;
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
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.minerwrapper.DecomposedMinerWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.FodinaWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.InductiveMinerWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.Miner;
import org.processmining.sapm.processdiscovery.result.MiningResult;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DecomposedDiscoveryUIPlugin {

	@Plugin(name = "Decomposed Miner Wrapper with Hyper-optimization", 
			parameterLabels = { "Log"}, 
			returnLabels = {"Petri Net", "BPMN Diagram", "Mining Result"},
			returnTypes = {Petrinet.class, BPMNDiagram.class,  MiningResult.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au")
	@PluginVariant(variantLabel = "Decomposed Miner Wrapper with Hyper-optimization", requiredParameterLabels = { 0 })
	public Object[] mine(final UIPluginContext context, XLog log) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()/2];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}
		
		double size[][] = {{200, 400, TableLayoutConstants.FILL}, //columns 
							{25, 25, 25, 25, 25, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));

		// row #0
		panel.add(SlickerFactory.instance().createLabel("Base Miner:"), "0,0");
		JComboBox<String> cboBaseMiner = new JComboBox<String>();
		cboBaseMiner.addItem("Inductive Miner");
		cboBaseMiner.addItem("Fodina");
		cboBaseMiner.setPreferredSize(new Dimension(300, 25));
		cboBaseMiner.setSize(new Dimension(300, 25));
		cboBaseMiner.setMinimumSize(new Dimension(300, 25));
		cboBaseMiner.setSelectedItem("Inductive Miner");
		panel.add(cboBaseMiner,"1,0");
		
		// row #1
		JCheckBox chkSoundness = SlickerFactory.instance().createCheckBox("Select sound models?", false);
		panel.add(chkSoundness,"1,1");

		InteractionResult result = context.showConfiguration("Staged Process Model Discovery - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		Miner baseMiner = null;
		if (cboBaseMiner.getSelectedIndex()==0) {
			baseMiner = new InductiveMinerWrapper(false, false, false, true);
		}
		else {
			baseMiner = new FodinaWrapper(false, false, false, true);
		}
		DecomposedMinerWrapper miner = new DecomposedMinerWrapper(true, true, chkSoundness.isSelected(), baseMiner);
		
		try {
			MiningResult mresult = miner.mineBestModel(log, ModelMetrics.FSCORE);
			
			Object[] res = new Object[3];
			res[0] = mresult.getPetrinetRes().getPetrinet();
			res[1] = mresult.getBPMN();
			res[2] = mresult;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}