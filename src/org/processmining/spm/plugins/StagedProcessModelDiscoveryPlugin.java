package org.processmining.spm.plugins;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.processmining.spm.processdiscovery.MCAS;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.minerwrapper.FodinaWrapper;
import org.processmining.spm.processdiscovery.minerwrapper.InductiveMinerWrapper;
import org.processmining.spm.processdiscovery.minerwrapper.Miner;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.stagemining.StageDecomposition;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class StagedProcessModelDiscoveryPlugin {
	
	private int DEFAULT_IMPRECISION_THRES = 6;
	private int DEFAULT_AVAIL_THRES = 80;

	@Plugin(name = "Staged Process Model Discovery", 
			parameterLabels = { "Log", "Stage Decomposition"}, 
			returnLabels = {"Petri Net", "BPMN Diagram", "Mining Result"},
			returnTypes = {Petrinet.class, BPMNDiagram.class,  MiningResult.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Staged Process Model Discovery", 
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Staged Process Model Discovery", requiredParameterLabels = {0,1})
	public Object[] mine(final UIPluginContext context, XLog log, StageDecomposition stageModel) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()/2];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}
		
		double size[][] = {{200, 400, 50, TableLayoutConstants.FILL}, //columns 
							{25, 25, 25, 25, 25, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #0
		panel.add(SlickerFactory.instance().createLabel("Imprecision Threshold:"), "0,0");
		JSlider imprecisionSlider = SlickerFactory.instance().createSlider(SwingConstants.HORIZONTAL); 
		imprecisionSlider.setMinimum(0);
		imprecisionSlider.setMaximum(100);
		imprecisionSlider.setValue(DEFAULT_IMPRECISION_THRES); 
		imprecisionSlider.setUI(new SlickerSliderUI(imprecisionSlider));
		imprecisionSlider.setOpaque(false);
		panel.add(imprecisionSlider,"1,0");
		
		JLabel imprecisionLabel = SlickerFactory.instance().createLabel(String.format("%.2f", (float)DEFAULT_IMPRECISION_THRES/100));
		panel.add(imprecisionLabel,"2,0");
		
		imprecisionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				imprecisionLabel.setText(String.format("%.2f", (float)imprecisionSlider.getValue()/100));
			}
		});
		
		// row #1
		panel.add(SlickerFactory.instance().createLabel("Availability Threshold:"), "0,1");
		JSlider availSlider = SlickerFactory.instance().createSlider(SwingConstants.HORIZONTAL);
		availSlider.setMinimum(0);
		availSlider.setMaximum(100);
		availSlider.setValue(DEFAULT_AVAIL_THRES); 
		availSlider.setUI(new SlickerSliderUI(availSlider));
		availSlider.setOpaque(false);
		panel.add(availSlider,"1,1");
		
		JLabel availLabel = SlickerFactory.instance().createLabel(String.format("%.2f", (float)DEFAULT_AVAIL_THRES/100));
		panel.add(availLabel,"2,1");
		
		availSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				availLabel.setText(String.format("%.2f", (float)availSlider.getValue()/100));
			}
		});
		
		// row #2
		panel.add(SlickerFactory.instance().createLabel("Base Miner:"), "0,2");
		JComboBox<String> cboBaseMiner = new JComboBox<String>();
		cboBaseMiner.addItem("Inductive Miner");
		cboBaseMiner.addItem("Fodina");
		cboBaseMiner.setPreferredSize(new Dimension(300, 25));
		cboBaseMiner.setSize(new Dimension(300, 25));
		cboBaseMiner.setMinimumSize(new Dimension(300, 25));
		cboBaseMiner.setSelectedItem("Inductive Miner");
		panel.add(cboBaseMiner,"1,2");

		InteractionResult result = context.showConfiguration("Staged Process Model Discovery - Parameters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		Miner baseMiner = null;
		if (cboBaseMiner.getSelectedIndex()==0) {
			baseMiner = new InductiveMinerWrapper(false, false);
		}
		else {
			baseMiner = new FodinaWrapper(false, false);
		}
		MCAS miner = new MCAS(1.0*imprecisionSlider.getValue()/100, 1.0*availSlider.getValue()/100, true, true, true);
		try {
			MiningResult mresult = miner.mineBestModel(log, baseMiner, ModelMetrics.FSCORE, stageModel);
			
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