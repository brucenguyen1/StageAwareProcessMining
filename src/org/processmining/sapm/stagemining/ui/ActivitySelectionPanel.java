package org.processmining.sapm.stagemining.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.processmining.sapm.stagemining.model.StageItem;

public class ActivitySelectionPanel extends JPanel implements ActionListener {
	private List<StageItem> activities;
	private JTable table = null;
	private	JButton btnExitSave = null;
	private	JButton btnExitNotSave = null;
	private boolean isUpdatedBack = false;
	
	public ActivitySelectionPanel(List<StageItem> activities) {
		this.activities = activities;
		
		String[] columnNames = {"Activity", "Selected?"};
        Object[][] data = new Object[activities.size()][2];
        int i=0;
        for (StageItem item : activities) {
        	data[i][0] = item.getLabel();
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
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnExitSave = new JButton("Exit and Update");
        btnExitSave.addActionListener(this);
        buttonPanel.add(btnExitSave);
        btnExitNotSave = new JButton("Exit Without Updating");
        btnExitNotSave.addActionListener(this);
        buttonPanel.add(btnExitNotSave);
        
        this.add(buttonPanel, BorderLayout.SOUTH);
	}

	public List<StageItem> getSelectedActivities() {
		List<StageItem> selected = new ArrayList<>();
		for (int row=0;row<table.getModel().getRowCount();row++) {
			if ((boolean)table.getModel().getValueAt(row, 1)) {
				selected.add(this.activities.get(row));
			}
		}
		
		return selected;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnExitNotSave) {
			this.isUpdatedBack = false;
		}
		else if (evt.getSource() == btnExitSave) {
			this.isUpdatedBack = true;
		}
		Window win = SwingUtilities.getWindowAncestor(this);
	    if (win != null) {
	    	win.dispose();
	    }
	}

	public boolean getUpdatedBack() {
		return this.isUpdatedBack;
	}
}
