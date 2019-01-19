package org.processmining.stagedprocessflows.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.stagedprocessflows.filter.TraceAttributeFilterParameters;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFManager;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueArrivalDepartureDiffView;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueArrivalRateView;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueCIPView;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueDepartureRateView;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueMultipleRateView;
import org.processmining.stagedprocessflows.ui.queue.SPFQueueTISView;
import org.processmining.stagedprocessflows.ui.service.SPFArrivalDepartureDiffView;
import org.processmining.stagedprocessflows.ui.service.SPFArrivalRateView;
import org.processmining.stagedprocessflows.ui.service.SPFCIPView;
import org.processmining.stagedprocessflows.ui.service.SPFDepartureRateView;
import org.processmining.stagedprocessflows.ui.service.SPFDepartureView;
import org.processmining.stagedprocessflows.ui.service.SPFExitRateView;
import org.processmining.stagedprocessflows.ui.service.SPFFlowEfficiencyView;
import org.processmining.stagedprocessflows.ui.service.SPFMultipleRateView;
import org.processmining.stagedprocessflows.ui.service.SPFTISView;
import org.processmining.stagedprocessflows.ui.system.SPFAllExitCountView;
import org.processmining.stagedprocessflows.ui.system.SPFAllFlowEfficiencyView;
import org.processmining.stagedprocessflows.ui.system.SPFComparisonView;
import org.processmining.stagedprocessflows.ui.system.SPFMainView;
import org.processmining.stagedprocessflows.ui.system.SPFMultipleCIPView;
import org.processmining.stagedprocessflows.ui.system.SPFMultipleTISView;
import org.processmining.stagedprocessflows.ui.system.SPFStageView;
import org.processmining.stagedprocessflows.ui.system.SPFSummaryView;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

// Referenced classes of package demo:
// MemoryUsageDemo, DemoPanel, DemoDescriptionP

public class SPFFrame extends JInternalFrame implements ActionListener, TreeSelectionListener, KeyListener,
		ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3032226502188477107L;

	static class DisplayChart implements Runnable {

		private final SPFFrame frame;
		private final NodeView nodeView;

		public void run() {
			frame.secondChartContainer.removeAll();
			frame.secondChartContainer.add(nodeView.createPanel());
			frame.displayPanel.validate();
		}

		public DisplayChart(SPFFrame frame, NodeView nodeView) {
			this.frame = frame;
			this.nodeView = nodeView;
		}
	}

	public static final String EXIT_COMMAND = "EXIT";
	private JPanel displayPanel; //the main display panel on the right consisting of upper and lower pane
	private JPanel primeChartContainer;
	private NodeView primeNodeView;
	private NodeView secondNodeView;
	private JPanel secondChartContainer;
	private JTextPane secondChartPane;
	private TreePath defaultChartPath;
	private JSlider zoomSlider;
	private JCheckBox synCheckbox;
	private boolean isAltDown = false;
	private int previousSliderValue = 0;
	private boolean isSync = false;
	private Map<String, ProMList<String>> attFieldLists;
	private ProMTextField txtFieldFilterName;
	private JMenu jmenuView;

	private SPF bpf = null;
	private final UIPluginContext context;
	private final XLog log;

	public SPFFrame(String s, SPF bpf, UIPluginContext context, XLog log) {
		super(s);
		this.bpf = bpf;
		this.context = context;
		this.log = log;

		setContentPane(createContent());
		//setJMenuBar(createMenuBar());

		pack();
		setVisible(true);
		setMaximizable(true);
		((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null); //maximize
	}

	private JComponent createContent() {
		//Left Pane: Tree
		JTree jtree = new JTree(createTreeModel());
		jtree.addTreeSelectionListener(this);
		jtree.addKeyListener(this);
		JScrollPane treeScrollPane = new JScrollPane(jtree);
		treeScrollPane.setPreferredSize(new Dimension(100, 400));

		//Middle Pane: Chart area
		JPanel middlePanel = createChartDisplayPanel();
		middlePanel.setPreferredSize(new Dimension(600, 400));

		//Right Pane: Case Attributes
		JPanel filterPanel = createFilterPanel();
		filterPanel.setMinimumSize(new Dimension(0, 0));
		filterPanel.setPreferredSize(new Dimension(100, 400));

		//Split pane between Chart area and case attributes
		JSplitPane jsplitpane1 = new JSplitPane(1);
		jsplitpane1.setLeftComponent(middlePanel);
		jsplitpane1.setRightComponent(filterPanel);
		jsplitpane1.setOneTouchExpandable(true);

		//Split Pane between Tree and split pane 1
		JSplitPane jsplitpane2 = new JSplitPane(1);
		jsplitpane2.setLeftComponent(treeScrollPane);
		jsplitpane2.setRightComponent(jsplitpane1);
		jsplitpane2.setOneTouchExpandable(true);

		//Panel
		JPanel jSplitPanel = SlickerFactory.instance().createRoundedPanel();
		jSplitPanel.setLayout(new BorderLayout());
		jSplitPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jSplitPanel.add(jsplitpane2);

		//--------------------------------------
		// Main Panel
		//--------------------------------------	
		JPanel jMainPanel = SlickerFactory.instance().createRoundedPanel();
		jMainPanel.setLayout(new BorderLayout());
		jMainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jMainPanel.add(createControlPanel(), BorderLayout.NORTH);
		jMainPanel.add(jSplitPanel, BorderLayout.CENTER);
		jtree.setSelectionPath(defaultChartPath);
		return jMainPanel;
	}

	private JPanel createControlPanel() {
		double size[][] = { { 200, TableLayoutConstants.FILL, 100, 70, 150 }, { TableLayoutConstants.FILL } };
		JPanel controlPanel = new JPanel(new TableLayout(size));

		controlPanel.add(createMenuBar(), "0,0");

		JButton deleteButton = new JButton("Delete");
		deleteButton.setActionCommand("DELETE_BPF");
		deleteButton.addActionListener(this);
		controlPanel.add(deleteButton, "2,0");

		synCheckbox = new JCheckBox("Synch.");
		synCheckbox.setActionCommand("SYNC_CHARTS");
		synCheckbox.addActionListener(this);
		controlPanel.add(synCheckbox, "3,0");

		zoomSlider = new JSlider(0, 100, 0);
		zoomSlider.addChangeListener(this);
		controlPanel.add(zoomSlider, "4,0");

		return controlPanel;
	}

	private JPanel createFilterPanel() {
		Map<String, List<String>> values = new HashMap<String, List<String>>();
		TraceAttributeFilterParameters attParameters = new TraceAttributeFilterParameters(context, log);
		for (String key : attParameters.getFilter().keySet()) {
			values.put(key, new ArrayList<String>());
			values.get(key).addAll(attParameters.getFilter().get(key));
			Collections.sort(values.get(key), new AlphanumComparator());
		}

		attFieldLists = new HashMap<String, ProMList<String>>();
		JTabbedPane tabbedPane = new JTabbedPane();
		List<String> sortedKeys = new ArrayList<String>();
		sortedKeys.addAll(values.keySet());
		Collections.sort(sortedKeys, new AlphanumComparator());
		for (String key : sortedKeys) {
			DefaultListModel listModel = new DefaultListModel();
			int[] selected = new int[values.get(key).size()];
			int i = 0;
			for (String value : values.get(key)) {
				listModel.addElement(value);
				selected[i] = i;
				i++;
			}
			ProMList<String> list = new ProMList<String>("Select values", listModel);
			attFieldLists.put(key, list);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setSelectedIndices(selected);
			//list.setPreferredSize(new Dimension(100, 100));

			tabbedPane.add(key, list);
		}

		JPanel attPanel = SlickerFactory.instance().createRoundedPanel();
		double size[][] = { { TableLayoutConstants.FILL, 100, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 25, 25 } };
		attPanel.setLayout(new TableLayout(size));
		attPanel.setOpaque(false);

		attPanel.add(tabbedPane, "0,0,2,0");

		txtFieldFilterName = new ProMTextField();
		txtFieldFilterName.setText("");
		attPanel.add(txtFieldFilterName, "0,1,2,1");

		JButton updateButton = new JButton("Update");
		updateButton.setActionCommand("FILTER");
		updateButton.addActionListener(this);

		attPanel.add(updateButton, "1,2");

		return attPanel;
	}

	private JMenuBar createMenuBar() {
		JMenuBar jmenubar = new JMenuBar();

		JMenu jmenuExport = new JMenu("Export", true);
		jmenuExport.setMnemonic('F');

		JMenuItem jmenuitem = new JMenuItem("Export to PDF...");
		jmenuitem.setActionCommand("EXPORT_TO_PDF");
		jmenuitem.addActionListener(this);
		jmenuExport.add(jmenuitem);
		jmenubar.add(jmenuExport);

		jmenuView = new JMenu("View", true);
		jmenuView.setMnemonic('T');
		ButtonGroup groupView = new ButtonGroup();
		for (int i = 0; i < SPFManager.getInstance().size(); i++) {
			JRadioButtonMenuItem menuItem;
			try {
				menuItem = new JRadioButtonMenuItem(i + " - " + SPFManager.getInstance().get(i).getFilter().getName());
				menuItem.setActionCommand("VIEW_BPF");
				menuItem.addActionListener(this);
				menuItem.setMnemonic(i);
				if (i == SPFManager.getInstance().getCurrentIndex()) {
					menuItem.setSelected(true);
				}
				groupView.add(menuItem);
				jmenuView.add(menuItem);
				jmenuView.addSeparator();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		jmenubar.add(jmenuView);

		return jmenubar;
	}

	private JPanel createChartDisplayPanel() {
		displayPanel = SlickerFactory.instance().createRoundedPanel();
		displayPanel.setLayout(new BorderLayout());

		//---------------------------------------
		// Main Panel
		//---------------------------------------
		primeChartContainer = SlickerFactory.instance().createRoundedPanel();
		primeChartContainer.setLayout(new BorderLayout());
		primeChartContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		primeChartContainer.removeAll();
		primeNodeView = new SPFMainView(bpf, "Staged Process Flow");
		primeChartContainer.add(primeNodeView.createPanel());
		primeChartContainer.setPreferredSize(new Dimension(600, 400));

		//---------------------------------------
		// Lower Panel
		//---------------------------------------
		secondChartContainer = SlickerFactory.instance().createRoundedPanel();
		secondChartContainer.setLayout(new BorderLayout());
		secondChartContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		secondChartPane = new JTextPane();
		secondChartPane.setEditable(false);
		JScrollPane jscrollpane = new JScrollPane(secondChartPane, 20, 31);
		secondChartContainer.add(jscrollpane);

		JSplitPane jsplitpane = new JSplitPane(0);
		jsplitpane.setTopComponent(primeChartContainer);
		jsplitpane.setBottomComponent(secondChartContainer);
		displayPanel.add(jsplitpane);
		jsplitpane.setDividerLocation(0.75D);

		return displayPanel;
	}

	private TreeModel createTreeModel() {
		DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("SPF");

		//System nodes
		topNode.add(createNode(new SPFMainView(bpf, "Staged Process Flow")));
		topNode.add(createNode(new SPFSummaryView(bpf, "SPF Summary")));
		topNode.add(createNode(new SPFComparisonView(bpf, "SPF Comparison")));
		topNode.add(createNode(new SPFMultipleCIPView(bpf, "CIP")));
		topNode.add(createNode(new SPFMultipleTISView(bpf, "TIS")));
		topNode.add(createNode(new SPFAllFlowEfficiencyView(bpf, "Flow Efficiency")));
		topNode.add(createNode(new SPFAllExitCountView(bpf, "Departure")));
		//topNode.add(createNode(new BPFMultipleResourceTimeView(this.bpf, "Resource Work Time")));

		for (SPFStage stage : bpf.getStages()) {
			topNode.add(createStageNode(stage));
		}
		
		return new DefaultTreeModel(topNode);
	}

	private MutableTreeNode createStageNode(SPFStage stage) {
		DefaultMutableTreeNode stageNode = new DefaultMutableTreeNode("Stage: " + stage.getName());
		
		MutableTreeNode stageViewNode = createNode(new SPFStageView(bpf, stage, "Stage View"));
		stageNode.add(stageViewNode);

		//Queue Nodes
		DefaultMutableTreeNode queueNode = new DefaultMutableTreeNode("Queue");
		queueNode.add(createNode(new SPFQueueMultipleRateView(bpf, stage, "Queue Multiple Rates")));
		queueNode.add(createNode(new SPFQueueArrivalRateView(bpf, stage, "Queue Arrival Rate")));
		queueNode.add(createNode(new SPFQueueDepartureRateView(bpf, stage, "Queue Departure Rate")));
		queueNode.add(createNode(new SPFQueueArrivalDepartureDiffView(bpf, stage, "Queue DR-AR Diff")));
		queueNode.add(createNode(new SPFQueueCIPView(bpf, stage, "Queue CIP")));
		queueNode.add(createNode(new SPFQueueTISView(bpf, stage, "Queue TIS")));
		stageNode.add(queueNode);

		//Service Nodes
		DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode("Service");
		serviceNode.add(createNode(new SPFMultipleRateView(bpf, stage, "Service Multiple Rates")));
		serviceNode.add(createNode(new SPFArrivalRateView(bpf, stage, "Service Arrival Rate")));
		serviceNode.add(createNode(new SPFDepartureRateView(bpf, stage, "Service Departure Rate")));
		serviceNode.add(createNode(new SPFArrivalDepartureDiffView(bpf, stage, "Service DR-AR Diff")));
		serviceNode.add(createNode(new SPFExitRateView(bpf, stage, "Service Exit Rate")));
		serviceNode.add(createNode(new SPFDepartureView(bpf, stage, "Service Departure")));
		serviceNode.add(createNode(new SPFCIPView(bpf, stage, "Service CIP")));
		serviceNode.add(createNode(new SPFTISView(bpf, stage, "Service TIS")));
		serviceNode.add(createNode(new SPFFlowEfficiencyView(bpf, stage, "Service FE")));
		//serviceNode.add(createNode(new BPFResourceStageTime(this.bpf, stageName,"Resource Time")));
		stageNode.add(serviceNode);
		
		//Actual Stage node
		DefaultMutableTreeNode actualStageNode = null;
		if (stage.getActualStage() != null) {
			SPFStage actualStage = stage.getActualStage();
			actualStageNode = new DefaultMutableTreeNode("Actual Stage");
			
			MutableTreeNode actualStageViewNode = createNode(new SPFStageView(bpf, actualStage, "Actual Stage View"));
			actualStageNode.add(actualStageViewNode);
			
			DefaultMutableTreeNode actualQueueNode = new DefaultMutableTreeNode("Queue");
			actualQueueNode.add(createNode(new SPFQueueMultipleRateView(bpf, actualStage, "Queue Multiple Rates")));
			actualQueueNode.add(createNode(new SPFQueueArrivalRateView(bpf, actualStage, "Queue Arrival Rate")));
			actualQueueNode.add(createNode(new SPFQueueDepartureRateView(bpf, actualStage, "Queue Departure Rate")));
			actualQueueNode.add(createNode(new SPFQueueArrivalDepartureDiffView(bpf, actualStage, "Queue DR-AR Diff")));
			actualQueueNode.add(createNode(new SPFQueueCIPView(bpf, actualStage, "Queue CIP")));
			actualQueueNode.add(createNode(new SPFQueueTISView(bpf, actualStage, "Queue TIS")));

			//Service Nodes
			DefaultMutableTreeNode actualServiceNode = new DefaultMutableTreeNode("Service");
			actualServiceNode.add(createNode(new SPFMultipleRateView(bpf, actualStage, "Service Multiple Rates")));
			actualServiceNode.add(createNode(new SPFArrivalRateView(bpf, actualStage, "Service Arrival Rate")));
			actualServiceNode.add(createNode(new SPFDepartureRateView(bpf, actualStage, "Service Departure Rate")));
			actualServiceNode.add(createNode(new SPFArrivalDepartureDiffView(bpf, actualStage, "Service DR-AR Diff")));
			actualServiceNode.add(createNode(new SPFExitRateView(bpf, actualStage, "Service Exit Rate")));
			actualServiceNode.add(createNode(new SPFDepartureView(bpf, actualStage, "Service Departure")));
			actualServiceNode.add(createNode(new SPFCIPView(bpf, actualStage, "Service CIP")));
			actualServiceNode.add(createNode(new SPFTISView(bpf, actualStage, "Service TIS")));
			actualServiceNode.add(createNode(new SPFFlowEfficiencyView(bpf, actualStage, "Service FE")));
			
			actualStageNode.add(actualQueueNode);
			actualStageNode.add(actualServiceNode);
			
			stageNode.add(actualStageNode);
		}

		return stageNode;
	}

	private MutableTreeNode createNode(NodeView view) {
		return new DefaultMutableTreeNode(view);
	}

	public void valueChanged(TreeSelectionEvent treeselectionevent) {
		TreePath treepath = treeselectionevent.getPath();
		Object selectedObj = treepath.getLastPathComponent();
		if (selectedObj != null) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedObj;
			Object userObject = selectedNode.getUserObject();
			if (userObject instanceof NodeView) {
				NodeView nodeView = (NodeView) userObject;

				if (isAltDown) {
					primeChartContainer.removeAll();
					primeNodeView = nodeView;
					primeChartContainer.add(nodeView.createPanel());
				} else {
					secondChartContainer.removeAll();
					secondNodeView = nodeView;
					secondChartContainer.add(nodeView.createPanel());
				}

				//---------------------------------------
				// Reset sync and zoom settings
				//---------------------------------------
				if ((primeNodeView != null) && (primeNodeView.chartPanel != null)) {
					primeNodeView.chartPanel.setMouseWheelEnabled(true);
					primeNodeView.chartPanel.setMouseZoomable(true);
				}
				if ((secondNodeView != null) && (secondNodeView.chartPanel != null)) {
					secondNodeView.chartPanel.setMouseWheelEnabled(true);
					secondNodeView.chartPanel.setMouseZoomable(true);
				}
				synCheckbox.setSelected(false);
				zoomSlider.setValue(0);

				displayPanel.validate();

				//				SwingUtilities.invokeLater(new DisplayChart(this, view));
			}
		}
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent evt) {
		//		this.isControlDown = ((evt.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0);
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent evt) {
		// TODO Auto-generated method stub
		//		this.isControlDown = ((evt.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0);
	}

	public void mouseReleased(MouseEvent evt) {
		// TODO Auto-generated method stub

	}

	public void keyPressed(KeyEvent evt) {
		// TODO Auto-generated method stub
//		isAltDown = evt.isAltDown();
		isAltDown = (evt.getKeyCode() == KeyEvent.VK_F1); 
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void stateChanged(ChangeEvent e) {

		if (!isSync) {
			return;
		}

		JSlider zoomSlider = (JSlider) e.getSource();
		if (zoomSlider.getValue() == 0) {
			if (primeNodeView != null) {
				primeNodeView.restoreView();
			}
			if (secondNodeView != null) {
				secondNodeView.restoreView();
			}

		} else if (zoomSlider.getValue() > previousSliderValue) { //Zoom in
			if (primeNodeView != null) {
				primeNodeView.zoomInBoth();
			}

			if (secondNodeView != null) {
				secondNodeView.zoomInBoth();
			}
		} else if (zoomSlider.getValue() < previousSliderValue) { //Zoom out
			if (primeNodeView != null) {
				primeNodeView.zoomOutBoth();
			}

			if (secondNodeView != null) {
				secondNodeView.zoomOutBoth();
			}
		}
		previousSliderValue = zoomSlider.getValue();

	}

	private TraceAttributeFilterParameters applyFilter() {
		TraceAttributeFilterParameters params = new TraceAttributeFilterParameters();
		for (String key : attFieldLists.keySet()) {
			if (!params.getFilter().containsKey(key)) {
				params.getFilter().put(key, new HashSet<String>());
			}
			params.getFilter().get(key).addAll(attFieldLists.get(key).getSelectedValuesList());
		}
		params.setName(txtFieldFilterName.getText().trim());
		return params;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("SYNC_CHARTS")) {
			isSync = ((AbstractButton) evt.getSource()).getModel().isSelected();

			if (isSync) {
				if ((primeNodeView != null) && (secondNodeView != null)) {
					primeNodeView.setSyncView(secondNodeView);
				}
			} else {
				if (primeNodeView != null) {
					primeNodeView.unsyncView(secondNodeView);
				}
				zoomSlider.setValue(0);
			}
		} else if (evt.getActionCommand().equals("FILTER")) {
			if (txtFieldFilterName.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(this, "Please enter a name for this filter!", "",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			TraceAttributeFilterParameters selectedParams = applyFilter();
			try {
				//				System.out.println(this.attParameters.getFilter());
				bpf = SPFManager.getInstance().createBPF(bpf.getConfig(), selectedParams);
				setContentPane(createContent());
				revalidate();
				this.repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
				e.printStackTrace();
			}

		} else if (evt.getActionCommand().equals("VIEW_BPF")) {
			int index = ((JRadioButtonMenuItem) evt.getSource()).getMnemonic();
			try {
				bpf = SPFManager.getInstance().get(index);
				SPFManager.getInstance().setCurrentIndex(index);
				setContentPane(createContent());
				revalidate();
				this.repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		else if (evt.getActionCommand().equals("DELETE_BPF")) {
			try {
				SPFManager.getInstance().remove(SPFManager.getInstance().getCurrentIndex());
				bpf = SPFManager.getInstance().getCurrent();
				setContentPane(createContent());
				revalidate();
				this.repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

}
