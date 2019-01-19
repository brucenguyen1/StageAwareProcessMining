package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * This class wraps both result panels (graph + table) and makes it available as
 * a single Jpanel
 * 
 * @author abolt
 *
 */
public class PerspectiveWrapperPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8132228065627634460L;
	
	JPanel graphPanel;

	public PerspectiveWrapperPanel(JPanel graphPanel) {

		this.graphPanel = graphPanel;
		
		setContents(graphPanel);		
	}
	
	public void setContents(JPanel panel1){
		graphPanel = panel1;
		removeAll();
		
		setLayout(new BorderLayout(0, 0));
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		if (graphPanel == null)
			graphPanel = new JPanel();
		tabbedPane.addTab("Graph Visualization", null, graphPanel,
				"Show the comparison results as an interactive and explorable graph");
		tabbedPane.setEnabledAt(0, true);

		tabbedPane.setSelectedIndex(0);
		add(tabbedPane,BorderLayout.CENTER);
		setVisible(true);
	}

}
