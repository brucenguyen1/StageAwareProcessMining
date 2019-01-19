package org.processmining.perspectivemining.ui.controller;

import javax.swing.JPanel;

import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.ui.forms.PerspectiveWrapperPanel;

public class PerspectiveWrapperController {

	private PerspectiveWrapperPanel resultsWrapperPanel;

	// Results Object
	@SuppressWarnings("unused")
	private PerspectiveGraph results;

	// Controllers
	@SuppressWarnings("unused")
	private PerspectiveMainController mainController;
	private PerspectiveGraphController graphController;

	public PerspectiveWrapperController(PerspectiveMainController mainController) {
		this.mainController = mainController;
		resultsWrapperPanel = new PerspectiveWrapperPanel(null);
		graphController = new PerspectiveGraphController(this);
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
