package org.processmining.perspectivemining.ui.controller;

import javax.swing.JPanel;

import org.processmining.perspectivemining.graph.model.DifferentialGraph;
import org.processmining.perspectivemining.ui.forms.PerspectiveWrapperPanel;

public class DifferentialWrapperController {

	private PerspectiveWrapperPanel resultsWrapperPanel;

	// Results Object
	@SuppressWarnings("unused")
	private DifferentialGraph results;

	// Controllers
	@SuppressWarnings("unused")
	private DifferentialMainController mainController;
	private DifferentialGraphController graphController;

	public DifferentialWrapperController(DifferentialMainController mainController) {
		this.mainController = mainController;
		resultsWrapperPanel = new PerspectiveWrapperPanel(null);
		graphController = new DifferentialGraphController(this);
	}

	public PerspectiveWrapperPanel getPanel() {
		return resultsWrapperPanel;
	}

	public void updateContents(JPanel panel) {
		graphController.setPanel(panel);
		resultsWrapperPanel.setContents(graphController.getPanel());
		
		resultsWrapperPanel.doLayout();
		resultsWrapperPanel.repaint();		
	}

}
