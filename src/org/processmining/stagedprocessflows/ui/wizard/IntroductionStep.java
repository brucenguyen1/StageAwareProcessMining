package org.processmining.stagedprocessflows.ui.wizard;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import org.processmining.stagedprocessflows.parameters.SPFSettingsListener;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class IntroductionStep extends WizardStep {
	SPFSettingsListener listener;

	public IntroductionStep() {
		initComponents();
	}

	private void initComponents() {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		String body = "<p>This plug-in accepts logs with a stage-based enhancement which consists of a set of stages, "
				+ "a set of case statuses, a mapping to assign stages to events, and <br> a "
				+ "mapping to assign statuses to cases.</p>";

		body += "<p>The log must have the followig attributes:<ol>";
		body += "<li>Every trace must have a 'status' attribute. One value is 'complete' "
				+ "indicating a complete case. Others are user-defined for cases that exit the process prematurely.</li>";
		body += "<li>Every event must have a 'stage' attribute.</li></ol>";
		body += "</p>";

		body += "<p>The stage-based enhancement must satisfy the following requirements:<ol>";
		body += "<li>First of all, if a case covers a stage, i.e. there is at least one event belonging to "
				+ "that stage, there must be events associated with all preceding stages in <br> the defined set of stages.</li>";
		body += "<li>The stages covered by a case must observe the defined order over the set of stages.</li>";
		body += "<li>Events of the same activity must belong to the same stage.</li>";
		body += "<li>Finally, if a case has a complete status, then it should have gone through all the stages.</li></ol>";
		body += "</p>";

		add(SlickerFactory.instance().createLabel("<html><h1>Introduction</h1>" + body), "0, 0, l, t");

		//    	JOptionPane.showMessageDialog(new JFrame(), "Hello, World");
	}

	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		// TODO Auto-generated method stub

	}

	public void setListener(SPFSettingsListener listener) {
		this.listener = listener;
	}

}
