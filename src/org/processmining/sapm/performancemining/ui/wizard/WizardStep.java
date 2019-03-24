package org.processmining.sapm.performancemining.ui.wizard;

import javax.swing.JPanel;

import org.processmining.sapm.performancemining.parameters.SPFSettingsListener;

@SuppressWarnings("serial")
public abstract class WizardStep extends JPanel {
	public abstract boolean precondition();

	public abstract void readSettings();

	public abstract void setListener(SPFSettingsListener listener);
}
