package org.processmining.perspectivemining.graph.settings;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.ui.controller.DifferentialSettingController;
import org.processmining.perspectivemining.ui.controller.PerspectiveSettingController;

public class PerspectiveSettingsManager {

	public static PerspectiveSettingObject createSettingsObject(PerspectiveInputObject input) throws Exception {
		return new PerspectiveSettingObject(input);
	}
	
	public static PerspectiveSettingObject createSettingsObject(PerspectiveInputObject input1, PerspectiveInputObject input2) throws Exception {
		return new PerspectiveSettingObject(input1, input2);
	}
	
	public static PerspectiveSettingObject createSettingsObject(PerspectiveSettingController controller) throws Exception {
		//TODO this method should take a settingsController and it should produce an object that reflects the status of the settigns.
		return new PerspectiveSettingObject(controller);
	}
	
	public static PerspectiveSettingObject createSettingsObject(DifferentialSettingController controller) {
		return new PerspectiveSettingObject(controller);
	}

}
