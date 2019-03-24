package org.processmining.sapm.stagemining.vis;

import java.awt.Color;

import javax.swing.SwingConstants;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.shapes.Ellipse;
import org.processmining.models.shapes.Hexagon;
import org.processmining.models.shapes.Rectangle;


public class StageNode extends AbstractDirectedGraphNode { //implements Decorated{
	private StageGraph graph;
	private int index;
	private String nodeLabel;
	private final static int STDWIDTH = 80;
	private final static int STDHEIGHT = 50;
	
	public StageNode(StageGraph graph, int index, String label) {
		super();
		this.graph = graph;
		this.index = index;
		this.nodeLabel = label;
		getAttributeMap().put(AttributeMap.SHAPE, new Rectangle(true));
		getAttributeMap().put(AttributeMap.SQUAREBB, false);
		getAttributeMap().put(AttributeMap.RESIZABLE, true);
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.CENTER);
		getAttributeMap().put(AttributeMap.SHOWLABEL, true);
		getAttributeMap().put(AttributeMap.AUTOSIZE, true);
//		getAttributeMap().put(AttributeMap.SIZE, new Dimension(150, 200));
		getAttributeMap().put(AttributeMap.FILLCOLOR, Color.ORANGE);
	}
	
	//For start/end node
	//nodeType: 0-start node, 1-end node, 2-milestone node, else stage node
	public StageNode(StageGraph graph, int index, String label, int nodeType) {
		super();
		this.graph = graph;
		this.index = index;
		this.nodeLabel = label;
		
		if (nodeType==0 || nodeType==1) {
			getAttributeMap().put(AttributeMap.SHAPE, new Ellipse());
			getAttributeMap().put(AttributeMap.SQUAREBB, false);
			getAttributeMap().put(AttributeMap.RESIZABLE, true);
			getAttributeMap().put(AttributeMap.LABEL, label);
			getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.LABELVERTICALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.SHOWLABEL, true);
			getAttributeMap().put(AttributeMap.AUTOSIZE, true);
	//		getAttributeMap().put(AttributeMap.SIZE, new Dimension(STDWIDTH, STDHEIGHT));
			getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
		}
		else if (nodeType==2) {
			getAttributeMap().put(AttributeMap.SHAPE, new Hexagon(0.12));
			getAttributeMap().put(AttributeMap.SQUAREBB, false);
			getAttributeMap().put(AttributeMap.RESIZABLE, true);
			getAttributeMap().put(AttributeMap.LABEL, label);
			getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.LABELVERTICALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.SHOWLABEL, true);
			getAttributeMap().put(AttributeMap.AUTOSIZE, true);
//			getAttributeMap().put(AttributeMap.SIZE, new Dimension(STDWIDTH, STDHEIGHT));
			getAttributeMap().put(AttributeMap.FILLCOLOR, StageGraphColors.getPrimitiveBackgroundColor());
		}
		else {
			getAttributeMap().put(AttributeMap.SHAPE, new Rectangle(true));
			getAttributeMap().put(AttributeMap.SQUAREBB, false);
			getAttributeMap().put(AttributeMap.RESIZABLE, true);
			getAttributeMap().put(AttributeMap.LABEL, label);
			getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.SHOWLABEL, true);
			getAttributeMap().put(AttributeMap.AUTOSIZE, true);
//			getAttributeMap().put(AttributeMap.SIZE, new Dimension(150, 200));
			getAttributeMap().put(AttributeMap.FILLCOLOR, Color.ORANGE);
		}
	}

	public int getIndex() {
		return index;
	}

	public StageGraph getGraph() {
		return graph;
	}

	public String id() {
		return "node_" + index;
	}
	
	public String getLabel() {
		return this.nodeLabel;
	}

	public void setLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
		getAttributeMap().put(AttributeMap.LABEL, nodeLabel);
	}

//	public String toString() {
//		return getElementName();
//	}

//	public String getElementName() {
//		String nodeEventName = "";
//		if (nodeLabel.contains("<html>")) {
//			nodeEventName = nodeLabel.replace("<html>", "");
//			nodeEventName = nodeEventName.replace("</html>", "");
//		}
//		return nodeEventName;
//	}
//
//	public String getEventType() {
//		//the label of the FMNode in an inner pattern graph is like "<html>eventName<br>eventType</html>"
//		//get the event type
//		String nodeEventType = "";
//		if (nodeLabel.contains("</html>") && nodeLabel.contains("<br>")) {
//			nodeEventType = nodeLabel.replace("</html>", "");
//			int length = nodeEventType.length();
//			int idx = nodeEventType.indexOf("<br>");
//			nodeEventType = nodeEventType.substring(idx + 4, length);
//		}
//		return nodeEventType;
//	}
//
//	public String getToolTipText() {
//		//XEvent evt = graph.getLogEvents().get(index);
//		return "<html><table><tr colspan=\"2\"><td>" + getElementName() + "</td></tr>" + "<tr><td>Event type:</td><td>"
//				+ getEventType() + "</td></tr>" + "<tr><td>Significance:</td><td></td></tr>";
//	}
}
