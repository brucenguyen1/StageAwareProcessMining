package org.processmining.perspectivemining.plugins;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.ui.controller.PerspectiveMainController;
import org.processmining.perspectivemining.ui.forms.PerspectivePanel;

@Plugin(name = "Multi-Perspective Process Analyzer", 
level = PluginLevel.PeerReviewed, 
parameterLabels = { "Log"}, 
returnLabels = { "Multi-Perspective Process Analyzer" }, 
returnTypes = { PerspectivePanel.class })
public class PerspectiveMiningPlugin {
	
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
					author = "Hoang Nguyen", 
					email = "huanghuy.nguyen@hdr.qut.edu.au",
					uiLabel = "Multi-Perspective Process Analyzer", 
					pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Multi-Perspective Process Analyzer", requiredParameterLabels = { 0})
	public PerspectivePanel runDefault(UIPluginContext context, XLog log) {
		// Apply the algorithm 
	    try {
			return run(context, log);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}
	}
	
	public PerspectivePanel run(UIPluginContext context, XLog log) throws Exception {
		PerspectiveInputObject input = new PerspectiveInputObject(log);
		
		PerspectiveMainController mainController = new PerspectiveMainController(context, input);
		
		return  mainController.getPanel();	
	}

}
