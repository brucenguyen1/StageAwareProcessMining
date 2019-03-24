package org.processmining.sapm.perspectivemining.plugins.visualizer;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectivePanel;

@Plugin(name = "Visualize Perspective", 
level = PluginLevel.PeerReviewed, 
parameterLabels = { "PerspectivePanel" }, 
returnLabels = {"Visualization" }, returnTypes = { JComponent.class })
@Visualizer
public class PerspectivePanelVisualizer {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, PerspectivePanel output) {
		return output;
	}
}
