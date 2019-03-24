package org.processmining.sapm.perspectivemining.ui.controller;

import javax.swing.JPanel;

/**
 * Controller for handling comparison results shown as a graph
 * 
 * @author abolt
 *
 */
public class DifferentialGraphController implements ResultController<JPanel> {

	private JPanel graphPanel;
	@SuppressWarnings("unused")
	private DifferentialWrapperController resultsWrapperController;

	public DifferentialGraphController(DifferentialWrapperController resultsWrapperController) {
		this.resultsWrapperController = resultsWrapperController;
	}

	public JPanel getPanel() {
		return graphPanel;
	}

	public void setPanel(JPanel panel) {
		graphPanel = panel;
	}

	//TODO add interaction listeners

}
