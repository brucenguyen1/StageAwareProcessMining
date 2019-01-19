package org.processmining.spm.stagemining.ui.wizard;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.stagedprocessflows.parameters.SPFConfig;
import org.processmining.stagedprocessflows.parameters.SPFSettingsListener;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class StageMergingStep extends WizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6770841698008470848L;
	private SPFSettingsListener listener;

	private DefaultTableModel tableModel = null;
	private ProMTable promTable;

	private Map<String, String> eventStageMap = new HashMap<String, String>();
	//	private List<String> gateList = new ArrayList<String>();
	//	private List<String> activityList = new ArrayList<String>();
	private List<String> stageList = new ArrayList<String>();

	public StageMergingStep(boolean isWizardMode, SPFConfig config) {

		eventStageMap = config.getEventStageMap();
		//		this.activityList = config.getActivityList();
		//		this.gateList = config.getGateList();
		stageList = config.getStageList();
		initComponents();
	}

	private void initComponents() {
		double size[][] = { { TableLayoutConstants.FILL }, { 40, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		add(SlickerFactory.instance().createLabel("<html><h1>Event Stage Mapping</h1>"), "0, 0, l, t");

		//--------------------------------------
		// Stage List 
		//--------------------------------------    	

		//--------------------------------------
		// Event stage mapping table
		//--------------------------------------
		Object[][] tableContent = new Object[eventStageMap.size()][2];
		int i = 0;
		for (String eventName : eventStageMap.keySet()) {
			tableContent[i][0] = eventName;
			tableContent[i][1] = eventStageMap.get(eventName);
			i++;
		}

		tableModel = new DefaultTableModel(tableContent, new Object[] { "Event Types", "Stage" }) {
			private static final long serialVersionUID = -6019224467802441949L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};
		promTable = new ProMTable(tableModel);
		promTable.setPreferredSize(new Dimension(700, 200));
		promTable.setMinimumSize(new Dimension(700, 200));

		this.add(promTable, "0,1");
	}

	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		// call to listerner to set values for variables
		//listener.setEventStageMap(this.eventStageMap);
		//listener.setActivityList(this.activityList);
		//listener.setGateList(this.gateList);

		listener.setStageList(stageList);
	}

	public void setListener(SPFSettingsListener listener) {
		this.listener = listener;

	}

}
