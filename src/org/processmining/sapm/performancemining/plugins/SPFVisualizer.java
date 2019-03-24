package org.processmining.sapm.performancemining.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.ui.main.SPFFrame;

/**
 * Visualization support for Staged Process Flows
 * 
 * @author Hoang Huy Nguyen (huanghuy.nguyen@hdr.qut.edu.au)
 * Created: July 21 2018
 * 
 */

@Plugin(name = "Visualize Staged Process Flows", 
returnLabels = { "SPF Visualization" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "Stage Process Flow" }, 
userAccessible = true)
@Visualizer
public class SPFVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, SPF spf) {
		SPFFrame bpfFrame = new SPFFrame("Business Process Flow", spf, context, spf.getConfig().getXLog());
		return bpfFrame;
	}
}
