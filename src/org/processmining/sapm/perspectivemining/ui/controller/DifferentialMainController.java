package org.processmining.sapm.perspectivemining.ui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.annotation.GraphAnnotatorFactory;
import org.processmining.sapm.perspectivemining.graph.factory.PerspectiveGraphFactory;
import org.processmining.sapm.perspectivemining.graph.model.DifferentialGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingsManager;
import org.processmining.sapm.perspectivemining.graph.settings.SamplingType;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectiveMatrixPanel;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectivePanel;
import org.processmining.sapm.perspectivemining.ui.forms.ProgressBarDialog;
import org.processmining.sapm.perspectivemining.utils.GraphVisualizer;
import org.processmining.sapm.perspectivemining.utils.StatisticsUtils;

import model.graph.impl.DirectedGraphInstance;

/**
 * This class plays as a controller in the Model-View-Controller architecture
 * The controller manages the rules of the component including coordinate views (JPanel)
 * and data objects. It also communicates with other controllers.
 * In this controller, the view is PerspectivePanel, data include PluginContext, 
 * PerspectiveInputObject. It then communicates with PerspectiveWrapperController and 
 * PerspectiveSettingController. PerspectiveWrapperController is the controller for 
 * the mining result. PerspectiveSettingController is the controller for the the settings
 */
public class DifferentialMainController {

	/**
	 * The root panel is the main panel of the plugin. all other panels will be
	 * put on top of this one.
	 */
	private PerspectivePanel root;

	private PerspectiveInputObject input1, input2;
	private PluginContext pluginContext;
	private DifferentialGraph currentResults;

	/**
	 * Child controllers
	 */
	private DifferentialSettingController settingsController;
	private DifferentialWrapperController resultsController;
	
	private PerspectiveSettingObject currentSettings = null;

	private Thread thread;
	
	//One run
	private ProcessAbstraction abs1 = null, abs2 = null;
	private DifferentialGraph dGraph = null;
	private DirectedGraphInstance visGraphModel = null;
	private JPanel graphPanel = null;

	public DifferentialMainController(PluginContext pluginContext, PerspectiveInputObject input1, PerspectiveInputObject input2) throws Exception {
		this.input1 = input1;
		this.input2 = input2;
		this.pluginContext = pluginContext;

		initialize();
	}

	private void initialize() throws Exception {
		root = new PerspectivePanel();

		//now we create the settings panel using that input and the empty results panel
		settingsController = new DifferentialSettingController(this, input1, input2);
		resultsController = new DifferentialWrapperController(this);

		update(settingsController.getStoredSetting()); //default values

	}

	/**
	 * This method gets summoned when the update button is clicked and the
	 * settings changed. This is where the work is done.
	 */
	public void requestUpdate() {
		//Get new settings from the user form
		PerspectiveSettingObject newSettings = PerspectiveSettingsManager.createSettingsObject(settingsController);
		int settingChange = this.compare(currentSettings, newSettings);
		
		try {
			if (settingChange <= 1) {
				System.out.println("Creating process abstraction");
				long start = System.currentTimeMillis();
				
				abs1 = new ProcessAbstraction(newSettings.getAbstractionSchema(), input1);
				abs2 = new ProcessAbstraction(newSettings.getAbstractionSchema(), input2);
				System.out.println("Finished creating process abstraction: " + (int)1.0*(System.currentTimeMillis() - start)/1000 + "s");
			}
			
			if (settingChange <= 2) {
				System.out.println("Creating differential graph");
				long start = System.currentTimeMillis();
				dGraph = PerspectiveGraphFactory.createDifferentialGraph(abs1, abs2, input1, input2, newSettings);
				System.out.println("Finished creating differential graph: " + (int)1.0*(System.currentTimeMillis() - start)/1000 + "s");
			}
			
			if (settingChange <= 3) {
				System.out.println("Creating annotations");
				long start = System.currentTimeMillis();
				dGraph.setSetting(newSettings); //remember to update the graph settings before running
				dGraph.getGraph1().setSetting(newSettings); 
				dGraph.getGraph2().setSetting(newSettings);
				GraphAnnotatorFactory.annotateGraph(dGraph.getGraph1(), abs1, input1, newSettings);
				GraphAnnotatorFactory.annotateGraph(dGraph.getGraph2(), abs2, input2, newSettings);
				System.out.println("Finished creating annotations");
			}
			
			//Note that this step is only to create the visualization graph model
			//together with the difference sample population. The coloring nodes/edges is in next step.
			if (settingChange <= 4) {
				System.out.println("Creating visualization");
				long start = System.currentTimeMillis();
				
				dGraph.setSetting(newSettings); //remember to update the graph settings before running
				dGraph.getGraph1().setSetting(newSettings);
				dGraph.getGraph2().setSetting(newSettings);
				visGraphModel = GraphVisualizer.createDifferentialVisualization(dGraph);
				
				System.out.println("Finished creating visualization: " + (int)1.0*(System.currentTimeMillis() - start)/1000 + "s");
			}
			
			//This step performs statistical tests and coloring nodes/edges
			if (settingChange <= 5) {
				System.out.println("Highlighting differences");
				long start = System.currentTimeMillis();
				
				StatisticsUtils.highlightDifferences(visGraphModel, 
														newSettings.getWeightType(), 
														newSettings.getSamplingType(),
														newSettings.getStatisticalTestType(), 
														newSettings.getSignificanceLevel());
				System.out.println("Finished highlighting differences: " + (int)1.0*(System.currentTimeMillis() - start)/1000 + "s");
			}
			
			graphPanel = new PerspectiveMatrixPanel(visGraphModel, input1, input2, newSettings);
			currentSettings = newSettings;
			resultsController.updateContents(graphPanel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		root.setResultsWrapperPanel(resultsController.getPanel());
		root.setSettingPanel(settingsController.getPanel());

		root.revalidate();
		root.doLayout();
		
	}
	
	/*
	 * +1: rerun everything
	 * +2: keep process abstraction, rerun perspective/differential graph creation and all others
	 * +3: keep process abstraction, keep perspective/differential graph, rerun graph annotations and all others
	 * +4: keep process abstraction, keep perspective/differential graph, keep graph annotations, rerun graph visualization and all others
	 * +5: keep process abstraction, keep perspective/differential graph, keep graph visualization, keep visualization, rerun graph highlighting
	 * +10: keep everything
	 */
	private static int compare(PerspectiveSettingObject currentSetting, PerspectiveSettingObject newSetting) {
		if (currentSetting == null) {
			return +1;
		}
		
		if (!currentSetting.getAbstractionSchema().equals(newSetting.getAbstractionSchema())) {
			return +1;
		}
		
		if (!currentSetting.getNodeSchema1().equals(newSetting.getNodeSchema1())) {
			return +2;
		}
		
		if (!currentSetting.getNodeSchema2().equals(newSetting.getNodeSchema2())) {
			return +2;
		}
		
		if (!currentSetting.getGraphType().equals(newSetting.getGraphType())) {
			return +2;
		}
		
		if (currentSetting.getSamplingType() != newSetting.getSamplingType()) {
			return +3;
		}
		
		if ((newSetting.getSamplingType() == SamplingType.WINDOW_BASED) && 
				Double.compare(currentSetting.getWindowSize(), newSetting.getWindowSize()) != 0) {
			return +3;
		}
		
		if (!currentSetting.getWeightType().equals(newSetting.getWeightType())) {
			return +4;
		}
		
		if (currentSetting.getWeightValueType() != newSetting.getWeightValueType()) {
			return +4;
		}
		
		if (!currentSetting.getDifferenceType().equals(newSetting.getDifferenceType())) {
			return +3;
		}
		
		if (!currentSetting.getStatisticalTestType().equals(newSetting.getStatisticalTestType())) {
			return +4;
		}
		
		if (currentSetting.getLowNumberOfObservations() != newSetting.getLowNumberOfObservations()) {
			return +4;
		}
		
		if (Double.compare(currentSetting.getSignificanceLevel(), newSetting.getSignificanceLevel()) != 0) {
			return +5;
		}
		
		return +10;
	}

	private void update(final PerspectiveSettingObject settings) {

		final ProgressBarDialog pb = new ProgressBarDialog(5); //it can have 5 steps
		pb.addButtonListener(new CancelRunner(pb));
		pb.setUndecorated(true);
		pb.setModal(true);

		root.revalidate();
		pb.setLocationRelativeTo(root);
		pb.setAlwaysOnTop(true);
		
//		Utils.hideAllPopups(); //if there were any detailDialogs visible, hide them all

		//run!
		System.out.println("dialog on " + Thread.currentThread());
		
//		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
//			 @SuppressWarnings("fallthrough")
//			@Override
//			 protected Void doInBackground() throws Exception {
//				 switch (settingsController.getStoredSetting().compareTo(settings)) {
//						case 0 :
//							break;
//						case 1 : //everything has to be recalculated
////							pb.setMaximum(5);
////							PerspectiveGraph pGraph = PerspectiveFactory.createPerspectiveGraph(input, settings);
////							resultsController.updateContents(new PerspectiveMatrixPanel(DrawUtils.createVisualization(pGraph)));
//					}
////				 settingsController.setStoredSettings(settings);
//				 return null;
//			 }
//
//			 @Override
//			 protected void done() {
//				 pb.dispose();//close the modal dialog
//			 }
//			};
//
//			sw.execute(); // this will start the processing on a separate thread
//			pb.setVisible(true); //this will block user input as long as the processing task is working
			
//		
//		thread = new Thread(new ComparatorRunner(pb, settingsController, settings));
//		thread.start();
//		
//		pb.setVisible(true);
//
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		root.setResultsWrapperPanel(resultsController.getPanel());
		root.setSettingPanel(settingsController.getPanel());

		root.revalidate();
		root.doLayout();
		root.repaint();
		//now that this finished, we dont need to recalculate the TS.

		//TransitionSystemUtils.createTSSettingsObject(pluginContext, input.getMerged());

	}

	public PerspectivePanel getPanel() {
		return root;
	}

	public PluginContext getPluginContext() {
		return pluginContext;
	}

	public PerspectiveInputObject getInput1() {
		return input1;
	}
	
	public PerspectiveInputObject getInput2() {
		return input2;
	}

//	public void applyTests(ResultsObject input, PerspectiveSettingObject settings) {
//		resultsController.updateContents(
//				DrawUtils.returnAsPanel(DrawUtils.createGraph(input, settings, root, this.input)));
//	}

	public DifferentialGraph getCurrentResults() {
		return currentResults;
	}

	public void setCurrentResults(DifferentialGraph currentResults) {
		this.currentResults = currentResults;
	}

	class CancelRunner implements ActionListener {
		ProgressBarDialog pb;

		public CancelRunner(ProgressBarDialog p) {
			pb = p;
		}

		@SuppressWarnings("deprecation")
		public void actionPerformed(ActionEvent e) {
			pb.setVisible(false);
			pb.dispose();
			thread.stop(); //not safe... but screw you ProM for not letting me do it naturally!!!
		}
	}
}
