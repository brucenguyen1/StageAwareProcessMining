package org.processmining.sapm.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XLog;
import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.settings.DifferenceType;
import org.processmining.sapm.perspectivemining.graph.settings.GraphType;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.SamplingType;
import org.processmining.sapm.perspectivemining.graph.settings.StatisticalTestType;
import org.processmining.sapm.perspectivemining.graph.settings.WeightType;
import org.processmining.sapm.perspectivemining.graph.settings.WeightValueType;
import org.processmining.sapm.perspectivemining.log.attribute.AttributeRow;
import org.processmining.sapm.perspectivemining.ui.fakecontext.FakePluginContext;
import org.processmining.sapm.perspectivemining.utils.OpenLogFilePlugin;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import layout.TableLayout;

public class PerspectiveSettingPanel extends JPanel 
					implements ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 4495195705710597553L;
	
	private final String COMMAND_MODIFY_NODE1 = "modify_node1";
	private final String COMMAND_MODIFY_NODE2 = "modify_node2";
	private final String COMMAND_MODIFY_ABS_SCHEMA = "modify_abstraction_schem";
	private final String COMMAND_WEIGHT_TYPE = "weight_type";
	private final String COMMAND_WEIGHT_VALUE_TYPE = "weight_value_type";
	private final String COMMAND_GRAPH_TYPE = "graph_type";
	private final String COMMAND_DIFFERENCE_TYPE = "view_type";
	private final String COMMAND_UPDATE = "update_perspective";
//	private final String COMMAND_VIEW_EVENTS = "view_events";
	private final String COMMAND_STATISTICAL_TEST = "statistical_test";
	private final String COMMAND_SAMPLING_TYPE = "sampling_type";
	
	private JList<AttributeRow> lstAbsSchema;
	private JButton btnModifyAbsSchema;
	
	private JList<AttributeRow> lstNode1;
	private JButton btnModify1;
	
	private JList<AttributeRow> lstNode2;
	private JButton btnModify2;
	
//	private JList<AttributeRow> lstFixedSchema;
//	private JButton btnModifyFixedAttributes;
	
	private JComboBox<WeightType> cboWeightType;
	private JComboBox<WeightValueType> cboWeightValueType;
	private JComboBox<GraphType> cboGraphType;
	private JComboBox<DifferenceType> cboDiffType;
	private JSlider sldLowObservations;
	
	private JComboBox<SamplingType> cboSamplingType;
	private JSlider sldSamplingWindow;
	private JTextField txtAlpha;
	private JComboBox<StatisticalTestType> cboStatType;
	
	private JButton btnViewEvents;
	private JButton btnUpdate;
	
	private PerspectiveInputObject input1 = null;
	private PerspectiveInputObject input2 = null;
	
	public PerspectiveSettingPanel(PerspectiveInputObject input1, PerspectiveInputObject input2) {
		this.input1 = input1;
		this.input2 = input2;
		
		//-------------------------------------------
		// Process Abstraction Panel
		//-------------------------------------------
		double panelProcessAbsLayout[][] = 
            {{0.2, 0.2, 0.2, 0.2, 0.2},
             {25, TableLayout.FILL, 25}}; //columns, rows
		JPanel panelProcessAbs = new JPanel(new TableLayout(panelProcessAbsLayout));
		Border blackline = BorderFactory.createLineBorder(Color.black);
		panelProcessAbs.setBorder(BorderFactory.createTitledBorder(blackline, ""));
		
		// Abstraction
		panelProcessAbs.add(new JLabel("Abstraction Schema:"), "0,0,4,0"); // column,row,column,row
		lstAbsSchema = new JList<>();
		lstAbsSchema.setLayoutOrientation(JList.VERTICAL);
		lstAbsSchema.setModel(new DefaultListModel<>());
		JScrollPane scrollPaneAbsSchema = new JScrollPane(lstAbsSchema);
		panelProcessAbs.add(scrollPaneAbsSchema, "0,1,4,1");		
		
		btnModifyAbsSchema = new JButton("Modify...");
		btnModifyAbsSchema.setActionCommand(COMMAND_MODIFY_ABS_SCHEMA);
		btnModifyAbsSchema.addActionListener(this);
		panelProcessAbs.add(btnModifyAbsSchema,"2,2");
		
		//-------------------------------------------
		// Node Type 1 Panel
		//-------------------------------------------
		double panelNodeLayout[][] = 
            {{0.2, 0.2, 0.2, 0.2, 0.2},
             {25, TableLayout.FILL, 25}};
		JPanel panelNode1 = new JPanel(new TableLayout(panelNodeLayout));
		panelNode1.setBorder(BorderFactory.createTitledBorder(blackline, "Node 1"));
		panelNode1.add(new JLabel("Object Schema:"), "0,0,4,0"); //rectangle from 0,0 to 4,0 (col,row)
		
		lstNode1 = new JList<>();
		lstNode1.setModel(new DefaultListModel<>());
		JScrollPane scrollPaneNode1 = new JScrollPane(lstNode1);
		panelNode1.add(scrollPaneNode1, "0,1,4,1");
		
		btnModify1 = new JButton("Modify...");
		btnModify1.setActionCommand(COMMAND_MODIFY_NODE1);
		btnModify1.addActionListener(this);
		panelNode1.add(btnModify1,"2,2");
		
		//-------------------------------------------
		// Node Type 2 Panel
		//-------------------------------------------
		JPanel panelNode2 = new JPanel(new TableLayout(panelNodeLayout));
		panelNode2.setBorder(BorderFactory.createTitledBorder(blackline, "Node 2"));
		panelNode2.add(new JLabel("Object Schema:"), "0,0,4,0"); //rectangle from 0,0 to 4,0 (col,row)
		
		lstNode2 = new JList<>();
		lstNode2.setModel(new DefaultListModel<>());
		JScrollPane scrollPaneNode2 = new JScrollPane(lstNode2);
		panelNode2.add(scrollPaneNode2, "0,1,4,1");
		
		btnModify2 = new JButton("Modify...");
		btnModify2.setActionCommand(COMMAND_MODIFY_NODE2);
		btnModify2.addActionListener(this);
		panelNode2.add(btnModify2,"2,2");
		
		//-------------------------------------------
		// View Panel
		//-------------------------------------------
		double panelViewLayout[][] = 
            {{TableLayout.FILL, 10},
             {25, 25, 25, 25, 25, 25, 25, 25, 70, TableLayout.FILL}};
		JPanel panelViewType = new JPanel(new TableLayout(panelViewLayout));
		panelViewType.setBorder(BorderFactory.createTitledBorder(blackline, "View"));

		panelViewType.add(new JLabel("Graph Type:"), "0,0");
		cboGraphType = new JComboBox<>();
		cboGraphType.setPreferredSize(new Dimension(300, 30));
		cboGraphType.addItem(GraphType.INTRA_FRAGMENT);
		cboGraphType.addItem(GraphType.INTER_FRAGMENT);
		cboGraphType.setActionCommand(COMMAND_GRAPH_TYPE);
		cboGraphType.addActionListener(this);
		panelViewType.add(cboGraphType,"0,1");
		
		panelViewType.add(new JLabel("Weight Type:"), "0,2");
		cboWeightType = new JComboBox<>();
		cboWeightType.setPreferredSize(new Dimension(300, 30));
		cboWeightType.addItem(WeightType.FREQUENCY);
		cboWeightType.addItem(WeightType.DURATION);
		cboWeightType.setActionCommand(COMMAND_WEIGHT_TYPE);
		cboWeightType.addActionListener(this);
		panelViewType.add(cboWeightType,"0,3");
		
		panelViewType.add(new JLabel("Weight Value Type:"), "0,4");
		cboWeightValueType = new JComboBox<>();
		cboWeightValueType.setPreferredSize(new Dimension(300, 30));
		cboWeightValueType.addItem(WeightValueType.FREQUENCY_ABSOLUTE);
		cboWeightValueType.addItem(WeightValueType.FREQUENCY_RELATIVE);
		cboWeightValueType.addItem(WeightValueType.DURATION_MEAN);
		cboWeightValueType.addItem(WeightValueType.DURATION_MEDIAN);
		cboWeightValueType.addItem(WeightValueType.DURATION_MIN);
		cboWeightValueType.addItem(WeightValueType.DURATION_MAX);
		cboWeightValueType.setActionCommand(COMMAND_WEIGHT_VALUE_TYPE);
		cboWeightValueType.addActionListener(this);
		panelViewType.add(cboWeightValueType,"0,5");
		
		panelViewType.add(new JLabel("Difference:"), "0,6");
		cboDiffType = new JComboBox<>();
		cboDiffType.setPreferredSize(new Dimension(300, 30));
		cboDiffType.addItem(DifferenceType.ABSOLUTE);
//		cboDiffType.addItem(DifferenceType.TREND);
//		cboDiffType.addItem(DifferenceType.SEASONALITY);
		cboDiffType.setActionCommand(COMMAND_DIFFERENCE_TYPE);
		cboDiffType.addActionListener(this);
		panelViewType.add(cboDiffType,"0,7");
		
		double panelLowObservationsLayout[][] = 
            {{0.2, 0.2, 0.2, TableLayout.FILL,30},
             {25,25}};
		JPanel panelLowObservations = new JPanel(new TableLayout(panelLowObservationsLayout));
		panelLowObservations.add(new JLabel("Infrequent No. of Observations: <= "), "0,0,4,0");
		sldLowObservations = SlickerFactory.instance().createSlider(SwingConstants.HORIZONTAL); 
		sldLowObservations.setMinimum(0);
		sldLowObservations.setMaximum(40);
		sldLowObservations.setValue(20); 
		sldLowObservations.setUI(new SlickerSliderUI(sldSamplingWindow));
		sldLowObservations.setOpaque(false);
		panelLowObservations.add(sldLowObservations, "0,1,3,1");
		JLabel lowObservationLabel = SlickerFactory.instance().createLabel(String.format("%.1f", (float)1));
		lowObservationLabel.setText(20 + "");
		panelLowObservations.add(lowObservationLabel, "4,1");
		sldLowObservations.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lowObservationLabel.setText(String.format("%.1f", (float)sldLowObservations.getValue()));
			}
		});
		
		panelViewType.add(panelLowObservations,"0,8");
		
		//-------------------------------------------
		// Statistics
		//-------------------------------------------
		double panelStatisticsLayout[][] = 
            {{0.2, 0.2, 0.2, TableLayout.FILL,30},
             {25, 25, 25, 25, 25, 25}};
		JPanel panelStatistics = new JPanel(new TableLayout(panelStatisticsLayout));
		panelStatistics.setBorder(BorderFactory.createTitledBorder(blackline, "Statistics"));
		
//		chkWindowBasedSampling = new JCheckBox("Window-based (checked) or Trace-based (unchecked)");
//		chkWindowBasedSampling.setSelected(true);
//		chkWindowBasedSampling.setActionCommand(COMMAND_SAMPLING_METHOD);
//		chkWindowBasedSampling.addActionListener(this);
		cboSamplingType = new JComboBox<>();
		cboSamplingType.setPreferredSize(new Dimension(300, 30));
		cboSamplingType.addItem(SamplingType.CASE_BASED);
		cboSamplingType.addItem(SamplingType.WINDOW_BASED);
		cboSamplingType.setActionCommand(COMMAND_SAMPLING_TYPE);
		cboSamplingType.addActionListener(this);
		panelStatistics.add(cboSamplingType, "0,0,4,0");
		
		panelStatistics.add(new JLabel("Sampling Window (days):"), "0,1,4,1");
		sldSamplingWindow = SlickerFactory.instance().createSlider(SwingConstants.HORIZONTAL); 
		sldSamplingWindow.setMinimum(0);
		sldSamplingWindow.setMaximum(100);
		sldSamplingWindow.setValue(10); 
		sldSamplingWindow.setUI(new SlickerSliderUI(sldSamplingWindow));
		sldSamplingWindow.setOpaque(false);
		panelStatistics.add(sldSamplingWindow, "0,2,3,2");
		JLabel windowLabel = SlickerFactory.instance().createLabel(String.format("%.1f", (float)1));
		windowLabel.setText(10+"");
		panelStatistics.add(windowLabel, "4,2");
		sldSamplingWindow.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				windowLabel.setText(String.format("%.1f", (float)sldSamplingWindow.getValue()));
			}
		});
		
		panelStatistics.add(new JLabel("Statistical Test:"), "0,3,1,3");
		cboStatType = new JComboBox<>();
//		cboStatType.setPreferredSize(new Dimension(300, 30));
		cboStatType.addItem(StatisticalTestType.PARAMETRIC);
		cboStatType.addItem(StatisticalTestType.NON_PARAMETRIC);
		cboStatType.setActionCommand(COMMAND_STATISTICAL_TEST);
		cboStatType.addActionListener(this);
		panelStatistics.add(cboStatType,"2,3,3,3");
		
		panelStatistics.add(new JLabel("Significance level (alpha):"), "0,4,1,4");
		txtAlpha = new JTextField("0.05");
		panelStatistics.add(txtAlpha,"2,4");
		
		//-------------------------------------------
		// Legend Panel
		//-------------------------------------------
//		BufferedImage myPicture;
//		JLabel picLabel = null;
		JPanel imagePanel = null;
		try {
//			myPicture = ImageIO.read(new File(System.getProperty("user.dir") + File.separator + "metrics.png"));
//			picLabel = new JLabel(new ImageIcon(myPicture));
			ImageIcon icon = new ImageIcon(System.getProperty("user.dir") + File.separator + "metrics.png");
			Image image = icon.getImage();
			imagePanel = new JPanel() {
			    @Override
			    protected void paintComponent(Graphics g) {
			        super.paintComponent(g);
			        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
			    }
			};
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//-------------------------------------------
		// Update Button
		//-------------------------------------------
		
//		JPanel panelUpdateButton = new JPanel(new FlowLayout());
//		btnViewEvents = new JButton("View Events");
//		btnViewEvents.setActionCommand(COMMAND_VIEW_EVENTS);
//		btnViewEvents.addActionListener(this);
//		panelUpdateButton.add(btnViewEvents);
		
		btnUpdate = new JButton("Update");
		btnUpdate.setActionCommand(COMMAND_UPDATE);
//		btnUpdate.addActionListener(this);
//		panelUpdateButton.add(btnUpdate);
		
		//-------------------------------------------
		// Main Panel
		//-------------------------------------------
		
		double panelLayout[][] = 
            {{10, TableLayout.FILL, 10},
             {140, 20, 140, 20, 140, 20, TableLayout.FILL, 20, 180, 20, 100, 20, 25}};
		JPanel mainPanel = new JPanel(new TableLayout(panelLayout));
		
		mainPanel.add(panelProcessAbs, "1,0");
		mainPanel.add(panelNode1, "1,2");
		mainPanel.add(panelNode2, "1,4");
		mainPanel.add(panelViewType, "1,6");
		mainPanel.add(panelStatistics, "1,8");
//		mainPanel.add(picLabel,"1,10");
		mainPanel.add(imagePanel,"1,10");
		mainPanel.add(btnUpdate, "1,12");
		
		JScrollPane scrollMainPane = new JScrollPane(mainPanel);
		this.setLayout(new BorderLayout());
		this.add(scrollMainPane, BorderLayout.CENTER);
		
		//-------------------------------------------
		// Initial state
		//-------------------------------------------
		this.sldSamplingWindow.setEnabled(false);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(COMMAND_MODIFY_NODE1)) {
			selectAttributes(lstNode1);
		}
		else if (evt.getActionCommand().equals(COMMAND_MODIFY_NODE2)) {
			selectAttributes(lstNode2);
		}
		if (evt.getActionCommand().equals(COMMAND_MODIFY_ABS_SCHEMA)) {
			selectAttributes(lstAbsSchema);
		}	
//		else if (evt.getActionCommand().equals(COMMAND_VIEW_EVENTS)) {
//			showEventDialog();
//		}
		else if (evt.getActionCommand().equals(COMMAND_GRAPH_TYPE)) {
			GraphType selectedGraphType = (GraphType)this.cboGraphType.getSelectedItem();
			if (selectedGraphType.equals(GraphType.INTRA_FRAGMENT)) {
				this.cboWeightType.setSelectedIndex(0); //Only frequency for intra-fragment graph type
				this.cboWeightValueType.setSelectedIndex(0);
			}
		}
		else if (evt.getActionCommand().equals(COMMAND_WEIGHT_TYPE) || 
				evt.getActionCommand().equals(COMMAND_WEIGHT_VALUE_TYPE)) {
			GraphType selectedGraphType = (GraphType)this.cboGraphType.getSelectedItem();
			WeightType selectedWeightType = (WeightType)this.cboWeightType.getSelectedItem();
			WeightValueType selectedWeightValueType = (WeightValueType)this.cboWeightValueType.getSelectedItem();
			if (selectedWeightType.equals(WeightType.DURATION) && selectedGraphType.equals(GraphType.INTRA_FRAGMENT)) {
				this.cboWeightType.setSelectedIndex(0);
			}
			if (selectedGraphType == GraphType.INTRA_FRAGMENT) {
				if (selectedWeightValueType == WeightValueType.DURATION_MIN ||
						selectedWeightValueType == WeightValueType.DURATION_MAX ||
						selectedWeightValueType == WeightValueType.DURATION_MEDIAN ||
						selectedWeightValueType == WeightValueType.DURATION_MEAN) {
					JOptionPane.showMessageDialog(this, "Incompatible WeightValueType and WeightType");
					this.cboWeightValueType.setSelectedIndex(0);
				}
			}
			else if (selectedGraphType == GraphType.INTER_FRAGMENT) {
//				if (selectedWeightValueType == WeightValueType.FREQUENCY_ABSOLUTE ||
//						selectedWeightValueType == WeightValueType.FREQUENCY_RELATIVE) {
//					JOptionPane.showMessageDialog(this, "Incompatible WeightValueType and WeightType");
//					this.cboWeightValueType.setSelectedIndex(2);
//				}
			}
		}
		else if (evt.getActionCommand().equals(COMMAND_SAMPLING_TYPE)) {
			SamplingType selectedSampleType = (SamplingType)this.cboSamplingType.getSelectedItem();
			if (selectedSampleType == SamplingType.CASE_BASED) {
				this.sldSamplingWindow.setEnabled(false);
			}
			else {
				this.sldSamplingWindow.setEnabled(true);
//				this.cboStatType.setSelectedIndex(0); // parametric only for Diff Window based
			}
		}
		else if (evt.getActionCommand().equals(COMMAND_STATISTICAL_TEST)) {
//			SamplingType selectedSampleType = (SamplingType)this.cboSamplingType.getSelectedItem();
//			StatisticalTestType statType = (StatisticalTestType)this.cboStatType.getSelectedItem();
////			if (selectedSampleType == SamplingType.WINDOW_BASED && statType == StatisticalTestType.NON_PARAMETRIC) {
////				this.cboStatType.setSelectedIndex(0);
////			}
		}
		
	}
	
	private void selectAttributes(JList<AttributeRow> lstView) {
		JDialog diaglog = new JDialog();
        diaglog.setTitle("Select Attributes");
        diaglog.setModal(true);
        diaglog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          
        //Create and set up the content pane.
        List<AttributeRow> selected = new ArrayList<>();
        for (Enumeration<AttributeRow> e = ((DefaultListModel)lstView.getModel()).elements(); e.hasMoreElements();) {
        	selected.add(e.nextElement());
        }
        	
        AttributeListingPanel newContentPane = new AttributeListingPanel(this.getAttributeList(input1,input2), selected);
        newContentPane.setOpaque(true); //content panes must be opaque
        diaglog.setContentPane(newContentPane);

        //Display the window.
        diaglog.setPreferredSize(new Dimension(600, 1000));
        diaglog.pack();
        diaglog.setVisible(true);
          
        if (newContentPane.getUpdatedBack()) {
        	((DefaultListModel)lstView.getModel()).clear();
        	for (AttributeRow row : newContentPane.getSelectedAttributes()) {
        		((DefaultListModel)lstView.getModel()).addElement(row);
        	}
        }
	}
	
//	private void showEventDialog() {
//		JDialog diaglog = new JDialog();
//        diaglog.setTitle("All Events");
//        diaglog.setModal(true);
//        diaglog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//          
//        EventListingPanel newContentPane = new EventListingPanel(input1);
//        newContentPane.setOpaque(true); //content panes must be opaque
//        diaglog.setContentPane(newContentPane);
//
//        //Display the window.
//        diaglog.setPreferredSize(new Dimension(1600, 1000));
//        diaglog.pack();
//        diaglog.setVisible(true);
//	}
	
	private void setGenerateButtonEnabled() {
		if (lstNode1.getModel().getSize() > 0 && lstNode2.getModel().getSize() > 0) {
			btnUpdate.setEnabled(true);
		}
		else {
			btnUpdate.setEnabled(false);
		}
	}
	
	public JButton getButton() {
		return this.btnUpdate;
	}


	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public List<AttributeRow> getNode1Attributes() {
		List<AttributeRow> attributes = new ArrayList<>();
		for(int i = 0; i< lstNode1.getModel().getSize();i++){
			attributes.add(lstNode1.getModel().getElementAt(i));
        }
		return attributes;
	}
	
	public List<AttributeRow> getNode2Attributes() {
		List<AttributeRow> attributes = new ArrayList<>();
		for(int i = 0; i< lstNode2.getModel().getSize();i++){
			attributes.add(lstNode2.getModel().getElementAt(i));
        }
		return attributes;
	}
	
	public List<AttributeRow> getAbsSchemaAttributes() {
		List<AttributeRow> attributes = new ArrayList<>();
		for(int i = 0; i< lstAbsSchema.getModel().getSize();i++){
			attributes.add(lstAbsSchema.getModel().getElementAt(i));
        }
		return attributes;
	}
	
	public GraphType getGraphType() {
		return (GraphType)this.cboGraphType.getSelectedItem();
	}
	
	public WeightType getWeightType() {
		return (WeightType)this.cboWeightType.getSelectedItem();
	}
	
	public WeightValueType getWeightValueType() {
		return (WeightValueType)this.cboWeightValueType.getSelectedItem();
	}
	
	public DifferenceType getDifferenceType() {
		return (DifferenceType)this.cboDiffType.getSelectedItem();
	}
	
	public SamplingType getSamplingType() {
		return (SamplingType)this.cboSamplingType.getSelectedItem();
	}
	
	public StatisticalTestType getStatisticalTestType() {
		return (StatisticalTestType)this.cboStatType.getSelectedItem();
	}
	
	public double getSignificanceLevel() {
		return Double.valueOf(this.txtAlpha.getText());
	}
	
	public int getWindowSize() {
		return this.sldSamplingWindow.getValue();
	}
	
//	public boolean getDynamicView() {
//		return this.chkDynamicView.isSelected();
//	}
	
	public int getLowNumberOfObservations() {
		return this.sldLowObservations.getValue();
	}
	
	private List<AttributeRow> getAttributeList(PerspectiveInputObject input1, PerspectiveInputObject input2) {
		return PerspectiveSettingObject.getCommonAttributeList(input1, input2);
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
		  													File.separator + "BPI15_1_small.xes"));
		          PerspectiveInputObject appdata = new PerspectiveInputObject(log);
		
		      	//Create and set up the window.
		          JFrame diaglog = new JFrame();
		          diaglog.setTitle("Test");
//		          diaglog.setModal(true);
		          diaglog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		          
		          //Create and set up the content pane.
		          PerspectiveSettingPanel newContentPane = new PerspectiveSettingPanel(appdata, null);
		          newContentPane.setOpaque(true); //content panes must be opaque
		          diaglog.setContentPane(newContentPane);
		
		          //Display the window.
		          diaglog.setPreferredSize(new Dimension(600, 1000));
		          diaglog.pack();
		          diaglog.setVisible(true);
		  		}
		  		catch (Exception e) {
		  			e.printStackTrace();
		  		}
		      }
		  });
		}
}
