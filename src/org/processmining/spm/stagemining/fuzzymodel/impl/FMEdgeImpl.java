package org.processmining.spm.stagemining.fuzzymodel.impl;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.spm.stagemining.fuzzymodel.FMEdge;
import org.processmining.spm.stagemining.fuzzymodel.FMNode;

public class FMEdgeImpl extends FMEdge<FMNode, FMNode> {
	public FMEdgeImpl(FMNode source, FMNode target, double weight, double label) {
		super(source, target, weight, label);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		
//		super(source, target, weight, Math.round(label*10000.0)/10000.0);
//		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
//		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
//		getAttributeMap().put(AttributeMap.SHOWLABEL,true);
//		getAttributeMap().put(AttributeMap.LINEWIDTH, ((new Float(label+""))*10));
	}

	public String toString() {
		String sourceLabel = source.getElementName();// + "(" + source.getEventType() + ")";
		String targetLabel = target.getElementName();// + "(" + target.getEventType() + ")";
		String edgeLabel = sourceLabel + "-->" + targetLabel;
		return edgeLabel;
	}
}
