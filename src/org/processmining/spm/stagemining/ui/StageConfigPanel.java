package org.processmining.spm.stagemining.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.processmining.spm.stagemining.Milestone;
import org.processmining.spm.stagemining.Stage;
import org.processmining.spm.stagemining.StageEditConfig;
import org.processmining.spm.stagemining.StageItem;
import org.processmining.spm.stagemining.StageListModel;

import layout.TableLayout;

public class StageConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Stage stage = null;
	private StageListModel stageList = null;
	private List<StageItem> removedActivities = null;
	
	private JCheckBox chkMergeWithPrevStage = null;
	private JList<StageItem> lstActivities = null;
	
	private JTextField txtEndMilestone = null;
	private Milestone currentEndMilestone = null; 
	
	private JComboBox<StageItem> cboNewMilestone = null;
	
	JRadioButton rdoMilestonePrevStage = null;
	JRadioButton rdoMilestoneSuccStage = null;
	
	public StageConfigPanel(Stage stage, StageListModel stageList, List<StageItem> removedActivities) {
		this.stage = stage;
		this.stageList = stageList;
		this.removedActivities = removedActivities;
		this.currentEndMilestone = stage.getEndMilestone();
		this.initializeGUI();
	}
	
    private void initializeGUI() {
    	this.setLayout(new BorderLayout());
    	
    	//------------------------------------------
    	// Merge with previous stage
    	//------------------------------------------
    	int stageIndex = stageList.indexOf(stage);
    	if (stageIndex > 0) {
			chkMergeWithPrevStage = new JCheckBox("Merge with previous stage?");
			this.add(chkMergeWithPrevStage, BorderLayout.NORTH);
		}
    	else {
    		this.add(new JLabel("   "), BorderLayout.NORTH);
    	}
    	
    	//------------------------------------------
    	// Activity list
    	//------------------------------------------
    	JPanel activityPanel = new JPanel();
    	
    	lstActivities = new JList<>();
    	lstActivities.setModel(new DefaultListModel<StageItem>());
		JScrollPane scrollPanel = new JScrollPane(lstActivities);
		scrollPanel.setPreferredSize(new Dimension(400, 280));
		activityPanel.add(scrollPanel, BorderLayout.CENTER);
		
		DefaultListModel<StageItem> listModel = (DefaultListModel<StageItem>)lstActivities.getModel();
		for (StageItem activity : stage) {
			listModel.addElement(activity);
		}
		
		JPanel activityButtonPanel = new JPanel(new FlowLayout());
		JButton btnRemoveActivity = new JButton("Remove");
		btnRemoveActivity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lstActivities.getSelectedIndex() >= 0) {
					if (!removedActivities.contains(lstActivities.getSelectedValue())) {
						removedActivities.add(lstActivities.getSelectedValue());
					}
					((DefaultListModel<StageItem>)lstActivities.getModel()).removeElement(lstActivities.getSelectedValue());
				}
			}
		});
		JButton btnAddActivity = new JButton("Add...");
		btnAddActivity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectActivities(lstActivities);
			}
		});
		activityButtonPanel.add(btnRemoveActivity);
		activityButtonPanel.add(btnAddActivity);
		activityPanel.add(activityButtonPanel, BorderLayout.SOUTH);
		
		this.add(activityPanel, BorderLayout.CENTER);

		//------------------------------------------
		// End Milestone
		//------------------------------------------
		double milestonePanelLayout[][] = 
            {{0.1, 0.8, 0.1},
             {25, 25, 25, 25, 60, TableLayout.FILL}}; //columns, rows
		JPanel milestonePanel = new JPanel(new TableLayout(milestonePanelLayout));
		
		milestonePanel.add(new JLabel("Current Milestone:"), "1,0");
		txtEndMilestone = new JTextField(currentEndMilestone.getLabel());
		milestonePanel.add(txtEndMilestone, "1,1");
		
		milestonePanel.add(new JLabel("New Milestone:"), "1,2");
		cboNewMilestone = new JComboBox<>();
		cboNewMilestone.addItem(null);
		for (StageItem item : this.getStageItemsAndEndMilestones()) {
			cboNewMilestone.addItem(item);
		}
		cboNewMilestone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selectedItem = cboNewMilestone.getSelectedItem();
				if (selectedItem != null) {
					StageItem newMilestone = (StageItem)selectedItem;
					if (!newMilestone.getLabel().equalsIgnoreCase(currentEndMilestone.getLabel())) {
						if (!removedActivities.contains(currentEndMilestone) && 
								!currentEndMilestone.getLabel().equalsIgnoreCase("end")) {
							removedActivities.add(currentEndMilestone);
						}
					}
					else {
						removedActivities.remove(currentEndMilestone);
					}
				}
				else {
					removedActivities.remove(currentEndMilestone);
				}
			}
		});
		milestonePanel.add(cboNewMilestone, "1,3");
		
		
		// Containing stage of the milestone
		double radioButtonPanelLayout[][] = 
            {{0.1, 0.8, 0.1},
             {30, 30, TableLayout.FILL}}; //columns, rows
		JPanel radioButtonPanel = new JPanel(new TableLayout(radioButtonPanelLayout));
		rdoMilestonePrevStage = new JRadioButton("Belongs to the preceding stage");
		rdoMilestoneSuccStage = new JRadioButton("Belongs to the succeding stage");
		radioButtonPanel.add(rdoMilestonePrevStage, "1,0");
		radioButtonPanel.add(rdoMilestoneSuccStage, "1,1");
		
		ButtonGroup milestoneRadioBtnGroup = new ButtonGroup();
		milestoneRadioBtnGroup.add(rdoMilestonePrevStage);
		milestoneRadioBtnGroup.add(rdoMilestoneSuccStage);
		if (stage.getEndMilestone().isBelongToPrevStage()) {
			rdoMilestonePrevStage.setSelected(true);
			rdoMilestoneSuccStage.setSelected(false);
		}
		else {
			rdoMilestonePrevStage.setSelected(false);
			rdoMilestoneSuccStage.setSelected(true);
		}
		
		milestonePanel.add(radioButtonPanel, "1,4");
		
		
		this.add(milestonePanel, BorderLayout.EAST);
    }
    
    private List<StageItem> getStageItemsAndEndMilestones() {
    	List<StageItem> allStageItems = new ArrayList<>();
    	for (int i=0;i<stageList.size();i++) {
    		allStageItems.addAll(stageList.get(i));
    	}
    	for (int i=0;i<stageList.size()-1;i++) {
    		allStageItems.add(stageList.get(i).getEndMilestone());
    	}
    	allStageItems.add(stageList.get(stageList.size()-1).getEndMilestone());
    	return allStageItems;
    }
    
	private void selectActivities(JList<StageItem> lstView) {
		JDialog diaglog = new JDialog();
        diaglog.setTitle("Select Actitivites To Add");
        diaglog.setModal(true);
        diaglog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          
        //Create and set up the content pane.
        List<StageItem> selected = new ArrayList<>();
        for (Enumeration<StageItem> e = ((DefaultListModel<StageItem>)lstView.getModel()).elements(); e.hasMoreElements();) {
        	selected.add(e.nextElement());
        }
        	
        ActivitySelectionPanel newContentPane = new ActivitySelectionPanel(this.removedActivities);
        newContentPane.setOpaque(true); //content panes must be opaque
        diaglog.setContentPane(newContentPane);

        //Display the window.
        diaglog.setPreferredSize(new Dimension(600, 400));
        diaglog.pack();
        diaglog.setVisible(true);
          
        if (newContentPane.getUpdatedBack()) {
        	DefaultListModel<StageItem> listItem = ((DefaultListModel<StageItem>)lstView.getModel());
        	for (StageItem row : newContentPane.getSelectedActivities()) {
        		if (!listItem.contains(row)) listItem.addElement(row);
        	}
        	removedActivities.removeAll(newContentPane.getSelectedActivities());
        }
	}
    
    public Stage getCurrentStage() {
    	return this.stage;
    }
    
    public List<StageItem> getNewStageItems() {
		List<StageItem> activities = new ArrayList<>();
		for(int i = 0; i< this.lstActivities.getModel().getSize();i++){
			activities.add(lstActivities.getModel().getElementAt(i));
        }
		return activities;
    }
    
//    public Milestone getCurrentMilestone() {
//    	return this.stage.getEndMilestone();
//    }
    
    public StageItem getEndMilestone() {
    	if (this.cboNewMilestone.getSelectedIndex() > 0) {
    		return (StageItem)this.cboNewMilestone.getSelectedItem();
    	}
    	else {
    		return this.stage.getEndMilestone();
    	}
    }
    
    public boolean getMilestoneBelongToPrevStage() {
    	return (this.rdoMilestonePrevStage.isSelected());
    }
    
    public boolean getMergeWithPrevStage() {
    	return (chkMergeWithPrevStage != null && this.chkMergeWithPrevStage.isSelected());
    }
    
    public StageEditConfig getStageModelEditConfig() {
    	return new StageEditConfig(this.getCurrentStage(), this.getMergeWithPrevStage(), 
    								this.getNewStageItems(), this.getEndMilestone(),
    								this.rdoMilestonePrevStage.isSelected());
    }
    
}
