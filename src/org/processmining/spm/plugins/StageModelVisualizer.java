package org.processmining.spm.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.spm.stagemining.ui.StagedProcessGraph;
import org.processmining.spm.stagemining.ui.StagedProcessGraphPanel;

/**
 * Visualization support for Staged Process Miner result
 * 
 * @author Hoang Huy Nguyen (huanghuy.nguyen@hdr.qut.edu.au)
 * Created: 10 December 2016
 * 
 */

@Plugin(name = "Visualize Stage Decomposition", 
returnLabels = { "Stage Decomposition Visualization" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "Stage Decomposition" }, 
userAccessible = true)
@Visualizer
public class StageModelVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, StageDecomposition stageModel) {
		return new StagedProcessGraphPanel(context, new StagedProcessGraph(stageModel));
	}
}
