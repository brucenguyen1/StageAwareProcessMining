package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.model.XLog;
import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.log.attribute.Attribute;
import org.processmining.perspectivemining.log.attribute.AttributeRow;
import org.processmining.perspectivemining.ui.fakecontext.FakePluginContext;
import org.processmining.perspectivemining.utils.OpenLogFilePlugin;

public class AttributeListingPanel extends JPanel implements ActionListener {

	private List<AttributeRow> attributeRows;
	public static final String[] categories = { Attribute.IGNORE, Attribute.TEXT, Attribute.DISCRETE,
			Attribute.CONTINUOUS, Attribute.DATE_TIME };
	private JTable table = null;
	private	JButton btnExitSave = null;
	private	JButton btnExitNotSave = null;
	private boolean isUpdatedBack = false;
	
	public AttributeListingPanel(List<AttributeRow> attributeRows, List<AttributeRow> selected) {
		this.attributeRows = attributeRows;
		
		String[] columnNames = {"Attribute", "Example Values", "Attribute Type", "Selected?"};
        Object[][] data = new Object[attributeRows.size()][4];
        int i=0;
        for (AttributeRow att : attributeRows) {
        	data[i][0] = att.getName();
        	String valueString = "";
        	for (String value : att.getValueSet()) {
        		valueString += value + ",";
        	}
        	data[i][1] = valueString;
        	data[i][2] = att.getUseAs();
        	data[i][3] = selected.contains(att) ? true : false;
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
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return Boolean.class;
                    default:
                        return Boolean.class;
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

	public List<AttributeRow> getSelectedAttributes() {
		List<AttributeRow> selected = new ArrayList<>();
		for (int row=0;row<table.getModel().getRowCount();row++) {
			if ((boolean)table.getModel().getValueAt(row, 3)) {
				selected.add(this.attributeRows.get(row));
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
	
	public static void main(String[] args) {
	  //Schedule a job for the event-dispatching thread:
	  //creating and showing this application's GUI.
	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	      	XLog log = null;
	  		try {
	  			System.out.println("Import log file");
	  			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
	  			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + 
	  													File.separator + "BPIC15_1.xes"));

	  		
	          PerspectiveInputObject appdata = new PerspectiveInputObject(log);
	
	      	//Create and set up the window.
	          JDialog diaglog = new JDialog();
	          diaglog.setTitle("Test");
	          diaglog.setModal(true);
	          diaglog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	          
	          //Create and set up the content pane.
	          AttributeListingPanel newContentPane = new AttributeListingPanel(appdata.getAttributeList(), new ArrayList<>());
	          newContentPane.setOpaque(true); //content panes must be opaque
	          diaglog.setContentPane(newContentPane);
	
	          //Display the window.
	          diaglog.pack();
	          diaglog.setVisible(true);
	          
	          if (newContentPane.getUpdatedBack()) {
	        	  System.out.println(newContentPane.getSelectedAttributes().toString());
	          }
	          else {
	        	  System.out.println("Exit without Updating");
	          }
	  		}
	  		catch (Exception e) {
	  			e.printStackTrace();
	  		}
	      }
	  });
	}
}
