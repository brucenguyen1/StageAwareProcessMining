package org.processmining.spm.stagemining.fuzzymodel.impl;

import java.awt.Color;

import javax.swing.SwingConstants;

import org.jgraph.graph.GraphConstants;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.spm.stagemining.fuzzymodel.FMNode;


public class StageFMEdgeImpl extends FMEdgeImpl {
	public StageFMEdgeImpl(FMNode source, FMNode target, double weight, double label) {
		super(source, target, weight, Math.round(label*10000.0)/10000.0);
		
		getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_SPLINE);
		//	GraphConstants.setRouting(getAttributeMap(), GraphConstants.ROUTING_SIMPLE);
//		getAttributeMap().put(AttributeMap.SHOWLABEL, false);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		String newlabel = new Double(weight).intValue()+"";
//		String label = significance+"/"+correlation;
//		String label = "<html>" + MutableFuzzyGraph.format(significance) + "<br>"
//				+ MutableFuzzyGraph.format(correlation) + "<html>";
		getAttributeMap().put(AttributeMap.LABEL, newlabel);
		getAttributeMap().put(AttributeMap.LABELCOLOR, Color.RED);
		getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.NORTH);
		
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		getAttributeMap().put(AttributeMap.SHOWLABEL,true);
		getAttributeMap().put(AttributeMap.LINEWIDTH, ((new Float(label+""))*10));
	}

	public String toString() {
		String sourceLabel = source.getElementName();// + "(" + source.getEventType() + ")";
		String targetLabel = target.getElementName();// + "(" + target.getEventType() + ")";
		String edgeLabel = sourceLabel + "-->" + targetLabel;
		return edgeLabel;
	}
}
