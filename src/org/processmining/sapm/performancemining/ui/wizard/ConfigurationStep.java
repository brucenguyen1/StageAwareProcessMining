package org.processmining.sapm.performancemining.ui.wizard;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.processmining.sapm.performancemining.parameters.SPFConfig;
import org.processmining.sapm.performancemining.parameters.SPFSettingsListener;
import org.processmining.sapm.performancemining.ui.misc.JTimeZoneComboBox;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ConfigurationStep extends WizardStep implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3393571904225903475L;
	private SPFSettingsListener listener;
	private TimeZone timezone = TimeZone.getTimeZone("Europe/Amsterdam");
	private List<String> caseStatusList = new ArrayList<String>();
	private final List<JCheckBox> stageCheckBoxList = new ArrayList<JCheckBox>();
	private JCheckBox checkStartCompleteEvent;

	public ConfigurationStep(boolean isWizardMode, SPFConfig config) {
		caseStatusList = config.getCaseStatusList();
		initComponents();
	}

	private void initComponents() {
		double size[][] = { { 280, 200, TableLayoutConstants.FILL },
				{ 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		//Row #0
		add(SlickerFactory.instance().createLabel("<html><h1>BPF Settings</h1>"), "0, 0, 2, 0");

		//Row #1
		add(SlickerFactory.instance().createLabel("Time zone"), "0,1");
		JTimeZoneComboBox timezoneBox = new JTimeZoneComboBox(timezone);
		add(timezoneBox, "1,1,2,1");
		timezoneBox.addActionListener(this);

		//Row #2
		add(SlickerFactory.instance().createLabel("The log has both start and complete events?"), "0,2");
		checkStartCompleteEvent = SlickerFactory.instance().createCheckBox("tick=Yes/untick=No", false);
		add(checkStartCompleteEvent, "1,2");

		//Row #3
		add(SlickerFactory.instance().createLabel("Status"), "0,3");
		add(SlickerFactory.instance().createLabel("Is Exit Status?"), "1,3");

		//Row #4 to the number of stages
		int rowIndex = 4;
		for (String status : caseStatusList) {
			add(SlickerFactory.instance().createLabel(status), "0," + rowIndex);
			JCheckBox checkBox = SlickerFactory.instance().createCheckBox("", false);
			if (status.equals("complete") || status.equals("completed") || status.equals("finish")
					|| status.equals("finised")) {
				checkBox.setEnabled(false);
			} else {
				checkBox.setSelected(true);
			}
			stageCheckBoxList.add(checkBox);
			add(checkBox, "1," + rowIndex);
			rowIndex++;
		}

	}

	public boolean precondition() {
		// TODO Auto-generated method stub
		return true;
	}

	public void readSettings() {
		listener.setTimeZone(timezone);

		List<String> exitStatusList = new ArrayList<String>();
		for (int i = 0; i < caseStatusList.size(); i++) {
			if (stageCheckBoxList.get(i).isSelected()) {
				if (!exitStatusList.contains(caseStatusList.get(i))) {
					exitStatusList.add(caseStatusList.get(i));
				}
			} else {
				if (exitStatusList.contains(caseStatusList.get(i))) {
					exitStatusList.remove(caseStatusList.get(i));
				}
			}
		}
		listener.setCaseExitStatusList(exitStatusList);
		listener.setCheckStartCompleteEvents(checkStartCompleteEvent.isSelected());
		//		System.out.println(exitStatusList);
	}

	public void setListener(SPFSettingsListener listener) {
		this.listener = listener;

	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JComboBox combobox = (JComboBox) e.getSource();
		timezone = (TimeZone) combobox.getSelectedItem();
		//		System.out.println(combobox.getSelectedItem().toString());
		//		System.out.println(combobox.getSelectedIndex());
	}

}
