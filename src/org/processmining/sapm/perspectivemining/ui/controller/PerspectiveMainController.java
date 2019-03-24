package org.processmining.sapm.perspectivemining.ui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.annotation.GraphAnnotatorFactory;
import org.processmining.sapm.perspectivemining.graph.factory.PerspectiveGraphFactory;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingsManager;
import org.processmining.sapm.perspectivemining.graph.settings.SamplingType;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectiveMatrixPanel;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectivePanel;
import org.processmining.sapm.perspectivemining.ui.forms.ProgressBarDialog;
import org.processmining.sapm.perspectivemining.utils.GraphVisualizer;

import model.graph.GraphModel;
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
public class PerspectiveMainController {

	/**
	 * The root panel is the main panel of the plugin. all other panels will be
	 * put on top of this one.
	 */
	private PerspectivePanel root;

	private PerspectiveInputObject input;
	private PluginContext pluginContext;
	private PerspectiveGraph currentResults;
	private JPanel graphPanel = null;
	
	/**
	 * Child controllers
	 */
	private PerspectiveSettingController settingsController;
	private PerspectiveWrapperController resultsController;
	
	private PerspectiveSettingObject currentSettings = null;

	private Thread thread;
	
	//One run
	private ProcessAbstraction abs = null;
	private PerspectiveGraph pGraph = null;
	private GraphModel visGraphModel = null;

	public PerspectiveMainController(PluginContext pluginContext, PerspectiveInputObject input) throws Exception {
		this.input = input;
		this.pluginContext = pluginContext;

		initialize();
	}

	private void initialize() throws Exception {
		root = new PerspectivePanel();

		//now we create the settings panel using that input and the empty results panel
		settingsController = new PerspectiveSettingController(this, input);
		resultsController = new PerspectiveWrapperController(this);

		update(settingsController.getStoredSetting()); //default values

	}

	/**
	 * This method gets summoned when the update button is clicked and the
	 * settings changed. This is where the work is done.
	 * @throws Exception 
	 */
	public void requestUpdate() throws Exception {
		//Get new settings from the user form
		PerspectiveSettingObject newSettings = PerspectiveSettingsManager.createSettingsObject(settingsController);
		int settingChange = this.compare(currentSettings, newSettings);
		
		try {
			if (settingChange <= 1) {
				System.out.println("Creating process abstraction");
				abs = new ProcessAbstraction(newSettings.getAbstractionSchema(), input);
				System.out.println("Finished creating process abstraction");
			}
			
			if (settingChange <= 2) {
				System.out.println("Creating perspective graph");
				pGraph = PerspectiveGraphFactory.createPerspectiveGraph(abs, input, newSettings);
				System.out.println("Finished creating perspective graph");
			}
			
			if (settingChange <= 3) {
				System.out.println("Creating annotations");
				pGraph.setSetting(newSettings);
				GraphAnnotatorFactory.annotateGraph(pGraph, abs, input, newSettings);
				System.out.println("Finished creating annotations");
			}
			
			//Note that this step also include coloring nodes/edges
			if (settingChange <= 4) {
				System.out.println("Creating visualization (including coloring nodes/edges)");
				pGraph.setSetting(newSettings);
				visGraphModel = GraphVisualizer.createPerspectiveVisualization(pGraph); //including statistical tests
				GraphVisualizer.highlightPerspectiveGraph((DirectedGraphInstance)visGraphModel);
				System.out.println("Finished creating visualization");
			}
			
			graphPanel = new PerspectiveMatrixPanel(visGraphModel, input, null, newSettings);
			currentSettings = newSettings;
			resultsController.updateContents(graphPanel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		root.setResultsWrapperPanel(resultsController.getPanel());
		root.setSettingPanel(settingsController.getPanel());

		root.revalidate();
		root.doLayout();
		
	}
	
	/*
	 * +1: rerun everything
	 * +2: keep process abstraction, rerun perspective/differential graph creation
	 * +3: keep process abstraction, keep perspective graph, rerun annotations
	 * +4: keep process abstraction, keep perspective graph, keep annotations, rerun visualization
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
		
//		if (currentSetting.getLowNumberOfObservations() != newSetting.getLowNumberOfObservations()) {
//			return +4;
//		}
		
//		if (!currentSetting.getStatisticalTestType().equals(newSetting.getStatisticalTestType())) {
//			return +4;
//		}
		
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
//							pb.setMaximum(5);
//							PerspectiveGraph pGraph = PerspectiveFactory.createPerspectiveGraph(input, settings);
//							resultsController.updateContents(new PerspectiveMatrixPanel(DrawUtils.createPerspectiveVisualization(pGraph)));
//					}
//				 settingsController.setStoredSettings(settings);
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

	public PerspectiveInputObject getInput() {
		return input;
	}

//	public void applyTests(ResultsObject input, PerspectiveSettingObject settings) {
//		resultsController.updateContents(
//				DrawUtils.returnAsPanel(DrawUtils.createGraph(input, settings, root, this.input)));
//	}

	public PerspectiveGraph getCurrentResults() {
		return currentResults;
	}

	public void setCurrentResults(PerspectiveGraph currentResults) {
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
