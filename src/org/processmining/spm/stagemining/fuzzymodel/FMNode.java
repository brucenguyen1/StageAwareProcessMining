/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */
package org.processmining.spm.stagemining.fuzzymodel;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingConstants;

import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.shapes.Ellipse;
import org.processmining.models.shapes.Hexagon;
import org.processmining.models.shapes.Rectangle;
import org.processmining.spm.stagemining.fuzzymodel.util.FMColors;
import org.processmining.spm.stagemining.fuzzymodel.util.FMLog;


// public class FMNode extends AbstractDirectedGraphNode implements
// ContainableDirectedGraphElement {
public class FMNode extends AbstractDirectedGraphNode { //implements Decorated{

	//private final AbstractDirectedGraph<FMNode, FMEdge<? extends FMNode, ? extends FMNode>> graph;
	protected MutableFuzzyGraph graph;
	protected int index;
	protected String nodeLabel;
	protected boolean isInnerPatternGraphNode;
	protected final static int STDWIDTH = 80;
	protected final static int STDHEIGHT = 50;
	//	protected LogRelations molecularInnerRelations;

	//	public static final int PADDINGFROMBOXTOTEXT = 10;
	//	public static final int TEXTHEIGHT = 6;


	// For stage node
	public FMNode(MutableFuzzyGraph graph, int index, String label) {
		super();
		this.graph = graph;
		this.index = index;
		nodeLabel = label;
		isInnerPatternGraphNode = true;
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
	public FMNode(MutableFuzzyGraph graph, int index, String label, int nodeType) {
		super();
		this.graph = graph;
		this.index = index;
		nodeLabel = label;
		this.setLabel(label);
		isInnerPatternGraphNode = true;
		
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
			getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
		}
		else {
			getAttributeMap().put(AttributeMap.SHAPE, new Rectangle(true));
			getAttributeMap().put(AttributeMap.SQUAREBB, false);
			getAttributeMap().put(AttributeMap.RESIZABLE, true);
			getAttributeMap().put(AttributeMap.LABEL, label);
			getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.CENTER);
			getAttributeMap().put(AttributeMap.SHOWLABEL, true);
			getAttributeMap().put(AttributeMap.SIZE, new Dimension(STDWIDTH, STDHEIGHT));
			getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
//			getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
		}
	}

	public FMNode(MutableFuzzyGraph graph, int index, String label, boolean isInnerPatternGraphNode) {
		this(graph, index, label);
		this.isInnerPatternGraphNode = isInnerPatternGraphNode;
	}

	public boolean isDirectlyConnectedTo(FMNode other) {
		if (other instanceof FMClusterNode) {
			return other.isDirectlyConnectedTo(this);
		} else {
			return ((graph.getBinarySignificance(index, other.index) > 0.0) || (graph.getBinarySignificance(
					other.index, index) > 0.0));
		}
	}

	public boolean directlyFollows(FMNode other) {
		if (other instanceof FMClusterNode) {
			Set<FMNode> otherPrimitives = ((FMClusterNode) other).getPrimitives();
			for (FMNode n : otherPrimitives) {
				if (directlyFollows(n)) {
					return true;
				}
			}
			return false;
		} else {
			return (graph.getBinarySignificance(index, other.index) > 0.0);
		}
	}

	public Set<FMNode> getPredecessors() {
		HashSet<FMNode> predecessors = new HashSet<FMNode>();
		for (int x = 0; x < graph.getNumberOfInitialNodes(); x++) {
			if (x == index) {
				continue; // ignore self
			} else if (graph.getBinarySignificance(x, index) > 0.0) {
				FMNode pre = graph.getNodeMappedTo(x);
				if (pre != null) {
					predecessors.add(pre);
				}
			}
		}
		return predecessors;
	}

	public Set<FMNode> getSuccessors() {
		HashSet<FMNode> successors = new HashSet<FMNode>();
		for (int y = 0; y < graph.getNumberOfInitialNodes(); y++) {
			if (y == index) {
				continue; // ignore self
			} else if (graph.getBinarySignificance(index, y) > 0.0) {
				FMNode post = graph.getNodeMappedTo(y);
				if (post != null) {
					successors.add(post);
				}
			}
		}
		return successors;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public MutableFuzzyGraph getGraph() {
		return graph;
	}

	public double getSignificance() {
		return graph.getNodeSignificanceMetric().getMeasure(index);
	}

	public void setSignificance(double significance) {
		graph.getNodeSignificanceMetric().setMeasure(index, significance);
	}

	public String id() {
		return "node_" + index;
	}

	public String toString() {
		//return getElementName() + " (" + getEventType() + ")";
		return getElementName();
	}

	public String getElementName() {
		//return graph.getLogEvents().get(index).getModelElementName();
		if (!isInnerPatternGraphNode) {
			String name = FMLog.getConceptName(graph.getLogEvents().get(index));
			return (name != null ? name : "");
		} else {
			//the label of the FMNode in an inner pattern graph is like "<html>eventName<br>eventType</html>"
			//get the eventName
			String nodeEventName = "";
//			if (nodeLabel.contains("<html>") && nodeLabel.contains("<br>")) {
			if (nodeLabel.contains("<html>")) {
				nodeEventName = nodeLabel.replace("<html>", "");
				nodeEventName = nodeEventName.replace("</html>", "");
//				int idx = nodeEventName.indexOf("<//html>");
//				nodeEventName = nodeEventName.substring(0, idx);
			}
			return nodeEventName;
		}
	}

	public void setElementName(String name) {
		if (!isInnerPatternGraphNode) {
			XEvent event = graph.getLogEvents().get(index);
			FMLog.setConceptName(event, name);
			graph.updateLogEvent(index, event);
		}
	}

	public String getEventType() {
		if (!isInnerPatternGraphNode) {
			String type = FMLog.getLifecycleTransition(graph.getLogEvents().get(index));
			return (type != null ? type : "");
		} else {
			//the label of the FMNode in an inner pattern graph is like "<html>eventName<br>eventType</html>"
			//get the event type
			String nodeEventType = "";
			if (nodeLabel.contains("</html>") && nodeLabel.contains("<br>")) {
				nodeEventType = nodeLabel.replace("</html>", "");
				int length = nodeEventType.length();
				int idx = nodeEventType.indexOf("<br>");
				nodeEventType = nodeEventType.substring(idx + 4, length);
			}
			return nodeEventType;
		}
	}

	public void setEventType(String type) {
		XEvent event = graph.getLogEvents().get(index);
		FMLog.setLifecycleTransition(event, type);
		graph.updateLogEvent(index, event);
	}

	public String getToolTipText() {
		//XEvent evt = graph.getLogEvents().get(index);
		return "<html><table><tr colspan=\"2\"><td>" + getElementName() + "</td></tr>" + "<tr><td>Event type:</td><td>"
				+ getEventType() + "</td></tr>" + "<tr><td>Significance:</td><td>"
				+ MutableFuzzyGraph.format(getSignificance()) + "</td></tr>";
	}

	public void setLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
		getAttributeMap().put(AttributeMap.LABEL, nodeLabel);
	}

	/*
	 * After pre-processing the log with patterns,there will be many molecular
	 * node in the new log. The molecular node contains many atomic nodes, this
	 * method is to get the inner graph inside the * molecular node.
	 */
	/*
	 * public JGraphVisualizationPanel getMolecularGraphPanel(PluginContext
	 * context, XLog log, boolean isAtomicNode) { JGraphVisualizationPanel
	 * graphPanel = null; MutableFuzzyGraph molecularInnerGraph = new
	 * MutableFuzzyGraph(log); FMLogEvents logEvents =
	 * FuzzyMinerLog.getLogEvents(log); int eventsCount =
	 * logEvents.getEventsCount(); Map<String,FMNode> eventClassIDFMNodeMap =
	 * new HashMap<String,FMNode>(); try { //create nodes for(int i=0;i<
	 * eventsCount;i++){ XEvent evt = logEvents.get(i); String evtName =
	 * FuzzyMinerLog.getEventName(evt); String evtType =
	 * FuzzyMinerLog.getEventType(evt); String evtLabel = "<html>"
	 * +evtName+"<br>" + evtType + "<br>" + "1.000"+"</html>"; FMNode evtNode =
	 * new FMNode(molecularInnerGraph,i,evtLabel);
	 * molecularInnerGraph.addNode(evtNode, i); String eventClassID =
	 * evtName+"+"+evtType; eventClassIDFMNodeMap.put(eventClassID, evtNode); }
	 * //create arcs if(molecularInnerRelations == null){
	 * molecularInnerRelations =
	 * PatternBasedPreprocessUtil.getLogRelations(context, log); }
	 * Map<Pair<XEventClass,XEventClass>,Double> causalRelations =
	 * molecularInnerRelations.getCausalDependencies();
	 * for(Pair<XEventClass,XEventClass> evtPair: causalRelations.keySet()){
	 * Double relationDegree = causalRelations.get(evtPair);
	 * if(relationDegree>0){ XEventClass first = evtPair.getFirst(); XEventClass
	 * second = evtPair.getSecond();
	 * 
	 * String firstEvtId = first.getId(); String secondEvtId = second.getId();
	 * if(eventClassIDFMNodeMap.containsKey(firstEvtId) &&
	 * eventClassIDFMNodeMap.containsKey(firstEvtId)) { FMNode
	 * firstNode,secondNode; firstNode = eventClassIDFMNodeMap.get(firstEvtId);
	 * secondNode = eventClassIDFMNodeMap.get(secondEvtId);
	 * molecularInnerGraph.addEdge(firstNode, secondNode, 0.5, 0.5); } } }
	 * graphPanel =
	 * ProMJGraphVisualizer.getVisualizationPanel(molecularInnerGraph,
	 * context.getProgress()); return graphPanel;
	 * 
	 * } catch (CancellationException e) { e.printStackTrace(); } catch
	 * (InterruptedException e) { e.printStackTrace(); } catch
	 * (ExecutionException e) { e.printStackTrace(); } return graphPanel; }
	 */
}
