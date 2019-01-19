package org.processmining.spm.stagemining.ui.wizard;

import javax.swing.JPanel;

import org.processmining.stagedprocessflows.parameters.SPFSettingsListener;

@SuppressWarnings("serial")
public abstract class WizardStep extends JPanel {
	public abstract boolean precondition();

	public abstract void readSettings();

	public abstract void setListener(SPFSettingsListener listener);
}
