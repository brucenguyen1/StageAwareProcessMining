package org.processmining.sapm.processdiscovery.plugins;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sapm.processdiscovery.result.MiningResult;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

@Plugin(name = "Visualize Process Model Mining Result", 
returnLabels = { "Visualization of Process Mining Result" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "Process Mining Result" }, userAccessible = true)
@Visualizer
public class MiningResultVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, MiningResult mResult) {
		double size[][] = {{50, 120, 400, TableLayoutConstants.FILL}, //columns 
				{25, 25, 25, 25, 25, 25, 25, 25, 25, 25, TableLayoutConstants.FILL}}; //rows
		JPanel panel = new JPanel(new TableLayout(size));
		panel.add(SlickerFactory.instance().createLabel("Fitness:"), "1,0");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getFitness())), "2,0");
		panel.add(SlickerFactory.instance().createLabel("Precision:"), "1,1");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getPrecision())), "2,1");
		panel.add(SlickerFactory.instance().createLabel("F-Score:"), "1,2");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getFscore())), "2,2");
		panel.add(SlickerFactory.instance().createLabel("Size:"), "1,3");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getSize())), "2,3");
		panel.add(SlickerFactory.instance().createLabel("CFC:"), "1,4");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getCFC())), "2,4");
		panel.add(SlickerFactory.instance().createLabel("Struct.:"), "1,5");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getEvaluation().getStructuredness())), "2,5");
		panel.add(SlickerFactory.instance().createLabel("Sound?:"), "1,6");
		
		int sound = mResult.getEvaluation().getSoundCheck();
		panel.add(SlickerFactory.instance().createLabel(sound<0 ? "Timed-out" : (sound==1 ? "Yes" : "No")), "2,6");
		
		panel.add(SlickerFactory.instance().createLabel("Runtime(ms):"), "1,7");
		panel.add(SlickerFactory.instance().createLabel(String.valueOf(mResult.getMiningRuntime())), "2,7");
		panel.add(SlickerFactory.instance().createLabel("Initial Marking:"), "1,8");
		panel.add(SlickerFactory.instance().createLabel(mResult.getPetrinetRes().getInitialMarking().toString()), "2,8");
		panel.add(SlickerFactory.instance().createLabel("Final Markings:"), "1,9");
		panel.add(SlickerFactory.instance().createLabel(mResult.getPetrinetRes().getFinalMarkings().toString()), "2,9");
		
//		String[] columnNames = {"Fitness",
//                				"Precision",
//                				"F-Score",
//                				"Size",
//                				"CFC",
//                				"Struct.",
//                				"Sound?",
//                				"Runtime(ms)"};
//		
//		Object[][] data = new Object[1][8];
//		data[0][0] = mResult.getEvaluation().getFitness();
//		data[0][1] = mResult.getEvaluation().getPrecision();
//		data[0][2] = mResult.getEvaluation().getFscore();
//		data[0][3] = mResult.getEvaluation().getSize();
//		data[0][4] = mResult.getEvaluation().getCFC();
//		data[0][5] = mResult.getEvaluation().getStructuredness();
//		data[0][6] = mResult.getEvaluation().isSoundModel() ? "Yes" : "No";
//		data[0][7] = mResult.getMiningRuntime();
//		
//		JTable table = new JTable(data, columnNames);
		
//		UIManager.put("Label.font", UIManager.getFont("Label.font").deriveFont(20));
//		SwingUtilities.updateComponentTreeUI(panel);
		
		context.getFutureResult(0).setLabel("Process Discovery - Model Quality Measures");
		
		return panel;
	}
}
