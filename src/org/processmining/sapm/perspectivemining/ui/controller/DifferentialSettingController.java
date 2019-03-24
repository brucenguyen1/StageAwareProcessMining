package org.processmining.sapm.perspectivemining.ui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingsManager;
import org.processmining.sapm.perspectivemining.ui.forms.PerspectiveSettingPanel;

public class DifferentialSettingController {

	//parent controller
	private DifferentialMainController parent;

	//panel
	private PerspectiveSettingPanel panel;

	//current state settings
	private PerspectiveSettingObject current;

	//input
	private PerspectiveInputObject input1, input2;

	public DifferentialSettingController(DifferentialMainController mainController, PerspectiveInputObject input1, 
											PerspectiveInputObject input2) throws Exception {
		this.parent = mainController;
		this.input1 = input1;
		this.input2 = input2;
		initialize(input1, input2);
	}

	private void initialize(PerspectiveInputObject input1, PerspectiveInputObject input2) throws Exception {

		panel = new PerspectiveSettingPanel(input1, input2);
		//add update button behavior
		panel.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				triggerUpdate();
			}
		});
		current = PerspectiveSettingsManager.createSettingsObject(input1, input2);

	}

	public void triggerUpdate() {
		//trigger the mainController to do stuff...
		parent.requestUpdate();
	}

	public PerspectiveSettingPanel getPanel() {
		return panel;
	}
	
	public DifferentialMainController getMainController(){
		return parent;
	}

	public PerspectiveSettingObject getStoredSetting() {
		return current;
	}
	
	public void setStoredSettings1(PerspectiveSettingObject settings){
		current = settings;
	}
	
}
