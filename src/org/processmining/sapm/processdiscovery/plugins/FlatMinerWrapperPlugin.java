package org.processmining.sapm.processdiscovery.plugins;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.minerwrapper.FodinaWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.InductiveMinerWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.Miner;
import org.processmining.sapm.processdiscovery.result.MiningResult;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class FlatMinerWrapperPlugin {

	@Plugin(name = "Flat Miner Wrapper with Hyper-optimization", 
			parameterLabels = {"Log"}, 
			returnLabels = {"Petri Net", "BPMN Diagram", "Mining Result"},
			returnTypes = {Petrinet.class, BPMNDiagram.class,  MiningResult.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", author = "Hoang Nguyen", email = "huanghuy.nguyen@hdr.qut.edu.au")
	@PluginVariant(variantLabel = "Flat Miner Wrapper with Hyper-optimization", requiredParameterLabels = { 0 })
	public Object[] mine(final UIPluginContext context, XLog log) {
		double size[][] = {{200, 400, TableLayoutConstants.FILL}, //columns 
							{25, 25, 25, 25, 25, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #0
		panel.add(SlickerFactory.instance().createLabel("Flat Miner:"), "0,0");
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
		
		// row#2
		JCheckBox chkRepeat = SlickerFactory.instance().createCheckBox("Multiple mining iterations (5 times)?", false);
		panel.add(chkRepeat,"1,2");

		InteractionResult result = context.showConfiguration("Flat Miner with Hyper-optimization", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		Miner baseMiner = null;
		if (cboBaseMiner.getSelectedIndex()==0) { //IDM
			baseMiner = new InductiveMinerWrapper(true, true, chkSoundness.isSelected(), false);
		}
		else {
			if (chkRepeat.isSelected()) {
				JOptionPane.showMessageDialog(null, "Fodina does not support multiple mining iterations.");
				return null;
			}
			else {
				baseMiner = new FodinaWrapper(true, true, chkSoundness.isSelected(), false);
			}
		}
		
		try {
			MiningResult mresult = null;
			if (!chkRepeat.isSelected()) {
				mresult = baseMiner.mineBestModel(log, ModelMetrics.FSCORE);
			}
			else {
				mresult = baseMiner.mineBestModelRepeated(log, ModelMetrics.FSCORE, 5);
			}
			
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