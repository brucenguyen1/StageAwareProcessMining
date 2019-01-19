package org.processmining.perspectivemining.plugins;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.ui.controller.DifferentialMainController;
import org.processmining.perspectivemining.ui.forms.PerspectivePanel;

@Plugin(name = "Multi-Perspective Process Comparator", 
level = PluginLevel.PeerReviewed, 
parameterLabels = { "Log 1", "Log 2"}, 
returnLabels = { "Multi-Perspective Process Comparator" }, 
returnTypes = { PerspectivePanel.class })
public class VariantAnalysisPlugin {
	
	@UITopiaVariant(affiliation = "Queensland University of Technology", 
					author = "Hoang Nguyen", 
					email = "huanghuy.nguyen@hdr.qut.edu.au",
					uiLabel = "Multi-Perspective Process Comparator", 
					pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Multi-Perspective Process Comparator", requiredParameterLabels = {0,1})
	public PerspectivePanel runDefault(UIPluginContext context, XLog log1, XLog log2) {
		// Apply the algorithm 
	    try {
			return run(context, log1, log2);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}
	}
	
	public PerspectivePanel run(UIPluginContext context, XLog log1, XLog log2) throws Exception {
		PerspectiveInputObject input1 = new PerspectiveInputObject(log1);
		PerspectiveInputObject input2 = new PerspectiveInputObject(log2);
		
		DifferentialMainController mainController = new DifferentialMainController(context, input1, input2);
		
		return  mainController.getPanel();
	}

}
