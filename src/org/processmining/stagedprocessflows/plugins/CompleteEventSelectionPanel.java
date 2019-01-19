package org.processmining.stagedprocessflows.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.spm.stagemining.Stage;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.stagedprocessflows.util.LogUtils;

public class CompleteEventSelectionPanel extends JPanel {
	private JTable table = null;
	
	public CompleteEventSelectionPanel(UIPluginContext context, XLog log, StageDecomposition stageModel) {
		super(new BorderLayout());
		
		// Select all exit activities from the last stage
		List<String> exitActivities = new ArrayList<>();
		Stage lastStage = stageModel.getStageListModel().get(stageModel.getStageListModel().size()-1);
		Set<String> lastStageActivities = lastStage.getStageItemLabels();
		for (XTrace trace: log) {
			XEvent lastEvent = trace.get(trace.size()-1);
			String eventName = LogUtils.getConceptName(lastEvent);
			if (lastStageActivities.contains(eventName)) {
				if (!exitActivities.contains(eventName)) {
					exitActivities.add(eventName);
				}
			}
		}
		
		// Display table
		String[] columnNames = {"Activity", "Selected?"};
        Object[][] data = new Object[exitActivities.size()][2];
        int i=0;
        for (String activity : exitActivities) {
        	data[i][0] = activity;
        	data[i][1] = Boolean.FALSE;
        	i++;
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table = new JTable(model) {

            private static final long serialVersionUID = 1L;

            /*@Override
            public Class getColumnClass(int column) {
            return getValueAt(0, column).getClass();
            }*/
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Boolean.class;
                    default:
                        return String.class;
                }
            }
        };
        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        
        this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public List<String> getCompletedActivities() {
		List<String> selected = new ArrayList<>();
		for (int row=0;row<table.getModel().getRowCount();row++) {
			if ((boolean)table.getModel().getValueAt(row, 1)) {
				selected.add((String)table.getModel().getValueAt(row, 0));
			}
		}
		
		return selected;
	}
}
