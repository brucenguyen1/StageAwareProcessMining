package org.processmining.perspectivemining.ui.controller;

import javax.swing.JPanel;

/**
 * Controller for handling comparison results shown as a graph
 * 
 * @author abolt
 *
 */
public class PerspectiveGraphController implements ResultController<JPanel> {

	private JPanel graphPanel;
	@SuppressWarnings("unused")
	private PerspectiveWrapperController resultsWrapperController;

	public PerspectiveGraphController(PerspectiveWrapperController resultsWrapperController) {
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
