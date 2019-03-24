package org.processmining.sapm.stagemining.plugins;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.stagemining.model.StageDecomposition;
import org.processmining.sapm.stagemining.ui.StageModelEditingPanel;

public class StageDecompositionEditingPlugin {
	@Plugin(name = "Stage Decomposition Editor", 
			parameterLabels = { "Stage Decomposition" }, 
			returnLabels = { "Stage Decomposition"}, 
			returnTypes = { StageDecomposition.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "Queensland University of Technology",
			author = "Hoang Nguyen", 
			email = "huanghuy.nguyen@hdr.qut.edu.au", 
			uiLabel = "Stage Decomposition Editor", 
			pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Stage Decomposition Editor", requiredParameterLabels = { 0 })
	public StageDecomposition edit(final UIPluginContext context, StageDecomposition stageModel) {
		StageModelEditingPanel modelConfigPanel = new StageModelEditingPanel(context, stageModel);
		int checkInput = 0;
		do {
			InteractionResult result = context.showConfiguration("Stage Decomposition Editor", modelConfigPanel);
			if (result.equals(InteractionResult.CANCEL)) {
				context.getFutureResult(0).cancel(true);
				return null;
			}
			checkInput = modelConfigPanel.checkUserInput();
			if (checkInput == -1) {
				JOptionPane.showMessageDialog(null, "There are unallocated activities!");
			}
			else if (checkInput == -2) {
				JOptionPane.showMessageDialog(null, "The last stage must have `end' as its milestone!");
			}
			else if (checkInput == -3) {
				JOptionPane.showMessageDialog(null, "An intermediary stage has `end' as milestone!");
			}
			else if (checkInput == -4) {
				JOptionPane.showMessageDialog(null, "Two stages have the same milestone!");
			}
		} while (checkInput<0);
		
		StageDecomposition newStageModel = new StageDecomposition(stageModel, modelConfigPanel.getStageModelEditConfig());
		try {
			context.getFutureResult(0).setLabel("Modularity=" + String.format("%.2f", newStageModel.getModularity()) + 
					"; Fowlkes-Mallows=" + String.format("%.2f", newStageModel.getGroundTruthIndex()));
			return newStageModel;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
	}
}
