package org.processmining.sapm.stagemining.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.sapm.stagemining.vis.StageGraph;

public class StageGraphPanel extends JPanel { //, Provider { if it is necessary, we implement Provider

	private static final long serialVersionUID = 4221149364708440299L;
	protected JPanel rootPanel;
	protected ProMJGraphPanel stageGraphPanel;
	protected ProMJGraphPanel oriGraphPanel;
	protected ProMJGraphPanel oriFilteredGraphPanel;
	protected JTabbedPane tabbedPane;
	
	//Bruce added 10 December 2016
	public StageGraphPanel(PluginContext context, StageGraph graph) {
		graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
		stageGraphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
		
		StageGraph oriGraph = graph.getBaseGraph().getStageGraph();
		oriGraph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);
		oriGraphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, oriGraph);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Staged Process Graph", stageGraphPanel);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		tabbedPane.addTab("Original Graph", oriGraphPanel);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);
		
		tabbedPane.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent e) {
		        System.out.println("Tab: " + tabbedPane.getSelectedIndex());
		    }
		});

		rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(new Color(100, 100, 100));
		rootPanel.setLayout(new BorderLayout());
		
		//rootPanel.add(new JLabel("Modularity = " + graph.getModularity() + ". Fowlkes-Mallows = " + graph.getGroundTruthIndex()), BorderLayout.NORTH);
		rootPanel.add(tabbedPane, BorderLayout.CENTER);
//		rootPanel.add(oriGraphPanel, BorderLayout.CENTER);
//		rootPanel.add(stageGraphPanel, BorderLayout.CENTER);
		rootPanel.revalidate();
		rootPanel.repaint();
		
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(rootPanel, BorderLayout.CENTER);
	}
	
	public JComponent getVisualizationPanel() {
		return oriGraphPanel;
	}
	
	public JComponent getRootGraphPanel() {
		return rootPanel;
	}

}
