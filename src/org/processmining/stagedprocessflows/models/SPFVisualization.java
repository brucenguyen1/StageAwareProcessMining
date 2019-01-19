package org.processmining.stagedprocessflows.models;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stagedprocessflows.ui.main.SPFFrame;

@Plugin(name = "Business Process Flow Characterization", returnLabels = { "Visualizion of BPF" }, returnTypes = { JComponent.class }, parameterLabels = { "BpfFrame" }, userAccessible = false)
@Visualizer
public class SPFVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, SPFFrame frame) {
		return frame;
	}
}
