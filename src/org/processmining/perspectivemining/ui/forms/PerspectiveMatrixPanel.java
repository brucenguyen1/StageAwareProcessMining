package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.perspectivemining.graph.settings.WeightType;
import org.processmining.perspectivemining.utils.LogUtils;

import example.matrix.renderers.ColourRenderer;
import example.matrix.renderers.StringHeaderRenderer;
import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.Node;
import model.graph.impl.DirectedGraphInstance;
import model.matrix.DefaultMatrixTableModel;
import swingPlus.graph.JGraph;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.JMatrix;
import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;
import util.ui.VerticalLabelUI;

public class PerspectiveMatrixPanel extends JPanel {
	protected transient GraphModel graph;
	protected JMatrix table;
	protected JGraph jgraph;
	protected PerspectiveInputObject input1;
	protected PerspectiveInputObject input2;
	protected PerspectiveSettingObject setting;
	
	public PerspectiveMatrixPanel (GraphModel newGraph, 
									PerspectiveInputObject input1,
									PerspectiveInputObject input2,
									PerspectiveSettingObject setting) {	
		this.graph = newGraph;
		this.input1 = input1;
		this.input2 = input2;
		this.setting = setting;
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		
		final TableModel mtm = new DefaultMatrixTableModel (graph);
		table = new JMatrix (mtm);
	
		for (int i = 0; i < table.getColumnCount(); i++) {
		    final TableColumn column = table.getColumnModel().getColumn(i);
		    column.setMinWidth (1); 
		    column.setMaxWidth (1000); 
		    column.setPreferredWidth (table.getRowHeight()); 
		}

		table.setDefaultRenderer (Color.class, new ColourRenderer ());
		table.setGridColor (ColorUtilities.mixColours (table.getBackground(), Color.black, 0.8f));
		final JHeaderRenderer stringHeader = new StringHeaderRenderer ();
		final JHeaderRenderer stringHeader2 = new StringHeaderRenderer ();
		table.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
		table.getRowHeader().setDefaultRenderer (String.class, stringHeader);
		table.getRowHeader().getColumnModel().getColumn(0).setMinWidth(220);
		
		table.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
		table.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
		((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
		stringHeader.setSelectionBackground (table.getRowHeader());
		stringHeader2.setSelectionBackground (table.getColumnHeader());
		table.setDefaultRenderer (String.class, stringHeader);
		table.getColumnHeader().setRowHeight (220);
		
		table.getColumnHeader().addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        int row = table.getColumnHeader().rowAtPoint(evt.getPoint());
		        int col = table.getColumnHeader().columnAtPoint(evt.getPoint());
		        if (row >= 0 && col >= 0) {
		        	Node node = (Node)table.getColumnHeader().getValueAt (row, col);
		        	
		        	double[] node1Weight = null, node1Trend = null, node1Seasonality = null;
		        	double[] node2Weight = null, node2Trend = null, node2Seasonality = null;
		        	double[] diffWeight = null, diffTrend = null, diffSeasonality = null;
		        	DirectedGraphInstance graphModel = (DirectedGraphInstance)((DefaultMatrixTableModel)table.getColumnHeader().getModel()).getData();

		        	if (graphModel.getGraph1NodeWeights().containsKey(node)) {
		        		node1Weight = graphModel.getGraph1NodeWeights().get(node); 
		        	}
		        	
		        	if (graphModel.getGraph1NodeWeightTrend().containsKey(node)) {
		        		node1Trend = graphModel.getGraph1NodeWeightTrend().get(node); 
		        		node1Seasonality = graphModel.getGraph1NodeWeightSeasonality().get(node);
		        	}

		        	if (graphModel.getGraph2NodeWeights().containsKey(node)) {
		        		node2Weight = graphModel.getGraph2NodeWeights().get(node); 
		        	}
		        	
		        	if (graphModel.getGraph2NodeWeightTrend().containsKey(node)) {
		        		node2Trend = graphModel.getGraph2NodeWeightTrend().get(node); 
		        		node2Seasonality = graphModel.getGraph2NodeWeightSeasonality().get(node);
		        	}
		        	
		        	if (graphModel.getNodeDiffWeights().containsKey(node)) {
		        		diffWeight = graphModel.getNodeDiffWeights().get(node); 
		        	}
		        	
		        	if (graphModel.getNodeDiffWeightTrend().containsKey(node)) {
		        		diffTrend = graphModel.getNodeDiffWeightTrend().get(node); 
		        		diffSeasonality = graphModel.getNodeDiffWeightSeasonality().get(node);
		        	}
		        	
		        	if (node1Weight == null && node2Weight == null) {
			        	JOptionPane.showMessageDialog(null, "There are no weight values for the node '" + node.toString() + 
			        										"' due to low frequency.");
		        	}
		        	else {
		        		showNodeEdgeDetailPanel("Node: " + node.toString(), 
		        							node1Weight, node1Trend, node1Seasonality,
		        							node2Weight, node2Trend, node2Seasonality,
		        							diffWeight, diffTrend, diffSeasonality, setting.getWeightType());
		        	}
		        }
		    }
		});
		
		table.addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        int row = table.rowAtPoint(evt.getPoint());
		        int col = table.columnAtPoint(evt.getPoint());
		        if (row >= 0 && col >= 0) {
//		        	DefaultMatrixTableModel tableModel = (DefaultMatrixTableModel)table.getModel();
//		        	Node node1 = (Node)tableModel.getRowObject(row);
//		        	Node node2 = (Node)tableModel.getColumnObject(col);
//		        	Set<Edge> edgeSet = graphModel.getEdges(node1, node2);
		        	Object obj = table.getValueAt (row, col);
		        	Edge edge = null;
		        	if (obj instanceof Collection) {
		        		final Collection<Object> collection = (Collection<Object>)obj;
						if (collection.size() == 0) {
							JOptionPane.showMessageDialog(null, "No edge objects found at this cell!");
							return;
						}
						else if (collection.size() > 1) {
							JOptionPane.showMessageDialog(null, "Something wrong. There are two or more edge objects found at this cell!");
							return;
						}
						else {
							Object obj2 = collection.iterator().next();
							if (obj2 instanceof Edge) {
								edge = (Edge)obj2;
							}
							else {
				        		JOptionPane.showMessageDialog(null, "Something wrong. The object found at this cell is not of an Edge class!");
				        		return;
				        	}
						}
		        	}
		        	else if (obj instanceof Edge) {
						edge = (Edge)obj;
					}
		        	else {
		        		JOptionPane.showMessageDialog(null, "Something wrong. The object found at this cell is not of an Edge class!");
		        		return;
		        	}
		        	
		        	double[] edge1Weight = null, edge1Trend = null, edge1Seasonality = null;
		        	double[] edge2Weight = null, edge2Trend = null, edge2Seasonality = null;
		        	double[] diffWeight = null, diffTrend = null, diffSeasonality = null;
		        	DirectedGraphInstance graphModel = (DirectedGraphInstance)((DefaultMatrixTableModel)table.getModel()).getData();

		        	if (graphModel.getGraph1EdgeWeights().containsKey(edge)) {
		        		edge1Weight = graphModel.getGraph1EdgeWeights().get(edge); 
		        	}

		        	if (graphModel.getGraph1EdgeWeightTrend().containsKey(edge)) {
		        		edge1Trend = graphModel.getGraph1EdgeWeightTrend().get(edge); 
		        		edge1Seasonality = graphModel.getGraph1EdgeWeightSeasonality().get(edge);
		        	}
		        	
		        	if (graphModel.getGraph2EdgeWeights().containsKey(edge)) {
		        		edge2Weight = graphModel.getGraph2EdgeWeights().get(edge); 
		        	}
		        	
		        	if (graphModel.getGraph2EdgeWeightTrend().containsKey(edge)) {
		        		edge2Trend = graphModel.getGraph2EdgeWeightTrend().get(edge); 
	        			edge2Seasonality = graphModel.getGraph2EdgeWeightSeasonality().get(edge);
		        	}
		        	
		        	if (graphModel.getEdgeDiffWeights().containsKey(edge)) {
		        		diffWeight = graphModel.getEdgeDiffWeights().get(edge); 
		        	}
		        	
		        	if (graphModel.getEdgeDiffWeightTrend().containsKey(edge)) {
		        		diffTrend = graphModel.getEdgeDiffWeightTrend().get(edge); 
		        		diffSeasonality = graphModel.getEdgeDiffWeightSeasonality().get(edge);
		        	}
		        	
		        	if (edge1Weight == null && edge2Weight == null) {
			        	JOptionPane.showMessageDialog(null, "There are no weight values for the edge '" + edge.toString() + 
			        										"' due to low frequency.");
		        	}
		        	else {
		        		showNodeEdgeDetailPanel("Edge: " + edge.toString(), 
		        							edge1Weight, edge1Trend, edge1Seasonality,
		        							edge2Weight, edge2Trend, edge2Seasonality,
		        							diffWeight, diffTrend, diffSeasonality, setting.getWeightType());
		        	}
		        }
		    }
		});
		
		this.setName ("Matrix");
		this.setSize (1024, 768);

		
		final JScrollPane pane = new JScrollPane (table);

		this.setLayout(new BorderLayout());
		String label = "Log 1: " + LogUtils.getConceptName(input1.getLog());
		if (input2 != null) {
			label += " - Log 2: " + LogUtils.getConceptName(input2.getLog());
		}
		this.add(new JLabel(label), BorderLayout.NORTH);
		this.add (pane, BorderLayout.CENTER);
		this.setVisible (true);
	}
	
	private void showNodeEdgeDetailPanel(String title1, 
										double[] node1Weight, double[] node1Trend, double[] node1Seasonality,
										double[] node2Weight, double[] node2Trend, double[] node2Seasonality,
										double[] diffWeight, double[] diffTrend, double[] diffSeasonality,
										WeightType weightType) {
		NodeEdgeDetailPanel panel = null;
		if (input2 != null) {
			panel = new NodeEdgeDetailPanel(title1, 
											node1Weight, node1Trend, node1Seasonality,
											node2Weight, node2Trend, node2Seasonality,
											diffWeight, diffTrend, diffSeasonality,
											input1.getLogStartTime(), 
											input2.getLogStartTime(),
											setting.getWindowSize(), weightType);
		}
		else {
			panel = new NodeEdgeDetailPanel(title1, 
											node1Weight, node1Trend, node1Seasonality,
											null, null, null,
											null, null, null,
											input1.getLogStartTime(), 
											-1,
											setting.getWindowSize(), weightType);
		}
		JFrame diaglog = new JFrame();
		diaglog.setTitle("Data");
	    diaglog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    panel.setOpaque(true); //content panes must be opaque
        diaglog.setContentPane(panel);

        //Display the window.
        diaglog.setPreferredSize(new Dimension(1400, 800));
        diaglog.pack();
        diaglog.setVisible(true);
	}
	
	protected void removeRandomNodes (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		model.removeNode (nodeList.get(0));
		model.removeNode (nodeList.get(1));
	}
	
	protected final void addRandomImages (final GraphModel model) {
		final File dir = new File (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ExamplePhotoDir"));
		final File[] files = dir.listFiles (new PhotoFilter ());
		
		if (files != null) {		
			final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
			
			for (int n = 0; n < Math.min (files.length, 15); n++) {
				BufferedImage bufImage = null;
//				LOGGER.info ("reading file "+n+": "+files[n].toString());
				if (files[n] != null) {
					try {
						bufImage = ImageIO.read (files[n]);
					}
					catch (final IOException ioe){
//						LOGGER.error (ioe.toString(), ioe);
					}
				}
	
				if (bufImage != null) {
					final Object node1 = nodeList.get ((int)(Math.random() * nodeList.size()));
					final Object node2 = nodeList.get ((int)(Math.random() * nodeList.size()));	
					model.addEdge (node1, node2, bufImage);
				}
			}
		}
	}
	
	
	protected final void addRandomShapes (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		
		for (int n = 0; n < 100; n++) {
			final Object node1 = nodeList.get ((int)(Math.random() * nodeList.size()));
			final Object node2 = nodeList.get ((int)(Math.random() * nodeList.size()));
			final Shape shape = Math.random() < 0.5 ? new Rectangle2D.Double (0, 0, 100, 20 + (100 * Math.random()))
						: new Ellipse2D.Double (0, 0, 100, 20 + (100 * Math.random()));
			model.addEdge (node1, node2, shape);
		}
	}
	
	
	protected final Object getNode (final String nodeName, final Map<String, Object> nodes) {
		Object node = nodes.get (nodeName);
  		if (node == null) {
  			node = new String (nodeName);
  			nodes.put (nodeName, node);
  		}
  		return node;
	}
	
	static class PhotoFilter implements FilenameFilter {

	    //Accept jpg files.
	    @Override
		public boolean accept (final File file, final String name) {
	    	return name.endsWith (".JPG") || name.endsWith(".jpg");
	    }
	}
}
