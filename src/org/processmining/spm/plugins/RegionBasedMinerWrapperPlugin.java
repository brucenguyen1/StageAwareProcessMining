package org.processmining.spm.plugins;

import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.minerwrapper.RegionBasedMinerWrapper;
import org.processmining.spm.processdiscovery.result.MiningResult;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class RegionBasedMinerWrapperPlugin {

	@Plugin(name = "Region-Based (Genet) Wrapper", 
			parameterLabels = {"Log"}, 
			returnLabels = {"Petri Net", "BPMN Diagram", "Mining Result"},
			returnTypes = {Petrinet.class, BPMNDiagram.class,  MiningResult.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au")
	@PluginVariant(variantLabel = "Region-Based (Genet) Wrapper", requiredParameterLabels = { 0 })
//	public MutableFuzzyGraph mineSP(final UIPluginContext context, XLog log) {
	public Object[] mine(final UIPluginContext context, XLog log) {	
		double size[][] = {{600, TableLayoutConstants.FILL}, //columns 
							{50, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		
		// row #0
		panel.add(SlickerFactory.instance().createLabel("This plugin calls Genet to mine a Petri Net"), "0,0");

		InteractionResult result = context.showConfiguration("Region-Based Miner", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		RegionBasedMinerWrapper miner = new RegionBasedMinerWrapper(true,true);
		
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