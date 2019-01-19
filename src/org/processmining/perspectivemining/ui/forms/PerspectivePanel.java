package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * This class acts like a visual wrapper, holding both the settings panel and
 * the results panel. This class was created to use a specific renderer that
 * only works with this class.
 * 
 * @author abolt
 *
 */
public class PerspectivePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4341311269659817514L;

	private PerspectiveSettingPanel settingsPanel;
	private PerspectiveWrapperPanel resultsWrapperPanel;

	public PerspectivePanel() {
		setLayout(new BorderLayout());

	}

	public PerspectivePanel(PerspectiveSettingPanel settings, PerspectiveWrapperPanel results) {
		this();
		setSettingPanel(settings);
		setResultsWrapperPanel(results);
	}

	public PerspectiveSettingPanel getSettingPanel() {
		return settingsPanel;
	}

	public void setSettingPanel(PerspectiveSettingPanel settingsPanel) {
		if (this.settingsPanel != null)
			this.remove(this.settingsPanel);
		this.settingsPanel = settingsPanel;
		add(this.settingsPanel, BorderLayout.EAST);
		update();
	}

	public PerspectiveWrapperPanel getResultsWrapperPanel() {
		return resultsWrapperPanel;
	}

	public void setResultsWrapperPanel(PerspectiveWrapperPanel resultsWrapperPanel) {

		if (this.resultsWrapperPanel != null)
			this.remove(this.resultsWrapperPanel);
		this.resultsWrapperPanel = resultsWrapperPanel;
		add(this.resultsWrapperPanel, BorderLayout.CENTER);
		update();
	}
	
	private void update()
	{		
		doLayout();
		repaint();
	}
}
