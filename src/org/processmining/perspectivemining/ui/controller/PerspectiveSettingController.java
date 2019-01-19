package org.processmining.perspectivemining.ui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingsManager;
import org.processmining.perspectivemining.ui.forms.PerspectiveSettingPanel;

/**
 * This class controlls the settings panel and keeps the current state of the
 * settings. It also provides a reference to the main controller.
 * 
 * @author abolt
 *
 */
public class PerspectiveSettingController {

	//parent controller
	private PerspectiveMainController parent;

	//panel
	private PerspectiveSettingPanel panel;

	//current state settings
	private PerspectiveSettingObject current;

	//input
	private PerspectiveInputObject input;

	public PerspectiveSettingController(PerspectiveMainController mainController, PerspectiveInputObject input) throws Exception {
		this.parent = mainController;
		this.input = input;
		initialize(input);
	}

	private void initialize(PerspectiveInputObject input) throws Exception {

		panel = new PerspectiveSettingPanel(input, null);

		//add update button behavior
		panel.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					triggerUpdate();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, e1.getMessage());
				}
			}
		});

		current = PerspectiveSettingsManager.createSettingsObject(input);
	}
	
	public PerspectiveInputObject getInputObject() {
		return this.input;
	}

	public void triggerUpdate() throws Exception {
		parent.requestUpdate();
	}

	public PerspectiveSettingPanel getPanel() {
		return panel;
	}
	
	public PerspectiveMainController getMainController(){
		return parent;
	}

	public PerspectiveSettingObject getStoredSetting() {
		return current;
	}
	
	public void setStoredSettings(PerspectiveSettingObject settings){
		current = settings;
	}
}
