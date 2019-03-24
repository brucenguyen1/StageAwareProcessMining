package org.processmining.sapm.stagemining.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.stagemining.ui.StageGraphPanel;
import org.processmining.sapm.stagemining.vis.StageGraph;

/**
 * Visualization support for Staged Process Miner result
 * 
 * @author Hoang Huy Nguyen (huanghuy.nguyen@hdr.qut.edu.au)
 * Created: 10 December 2016
 * 
 */

@Plugin(name = "Visualize Stage Graph", 
returnLabels = { "StageGraphPanel" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "Stage Graph" }, 
userAccessible = true)
@Visualizer
public class StageGraphVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, StageGraph graph) {
		return new StageGraphPanel(context, graph);
	}
}
