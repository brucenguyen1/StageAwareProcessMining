package org.processmining.spm.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.ui.StagedProcessGraph;
import org.processmining.spm.stagemining.ui.StagedProcessGraphPanel;

/**
 * Visualization support for Staged Process Miner result
 * 
 * @author Hoang Huy Nguyen (huanghuy.nguyen@hdr.qut.edu.au)
 * Created: 10 December 2016
 * 
 */

@Plugin(name = "Visualize Staged Process Graph", 
returnLabels = { "StagedProcessGraph Visualization" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "StagedProcessGraph" }, 
userAccessible = true)
@Visualizer
public class StagedProcessGraphVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, StagedProcessGraph graph) {
		return new StagedProcessGraphPanel(context, graph);
	}
}
