package org.processmining.sapm.stagemining.vis;

import java.awt.Color;

import javax.swing.SwingConstants;

import org.jgraph.graph.GraphConstants;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;

public class StageEdge<S extends StageNode, T extends StageNode> extends AbstractDirectedGraphEdge<S, T> {

	private double weight;
	private double relativeWeight;

	public StageEdge(S source, T target, double weight, double relativeWeight) {
		super(source, target);
		this.weight = weight;
		this.relativeWeight = relativeWeight;
		
		getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_SPLINE);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		
		String newlabel = new Double(weight).intValue()+"";
		getAttributeMap().put(AttributeMap.LABEL, newlabel);
		getAttributeMap().put(AttributeMap.EDGECOLOR, StageGraphColors.getEdgeColor((float) relativeWeight));
		getAttributeMap().put(AttributeMap.LABELCOLOR, Color.RED);
		getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.NORTH);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		getAttributeMap().put(AttributeMap.SHOWLABEL,true);
		getAttributeMap().put(AttributeMap.LINEWIDTH, ((new Float(relativeWeight+""))*10));
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		String newlabel = new Double(weight).intValue()+"";
		getAttributeMap().put(AttributeMap.LABEL, newlabel);
	}
	
	public double getRelativeWeight() {
		return relativeWeight;
	}

	public void setRelativeWeight(double weight) {
		this.relativeWeight = weight;
		getAttributeMap().put(AttributeMap.LINEWIDTH, ((new Float(relativeWeight+""))*10));
	}	

	public int hashCode() {
		return (source.hashCode() << 2) + target.hashCode();
	}

	public String toString() {
		return "Edge " + source.id() + " -> " + target.id();
	}

	/**
	 * update GUI appearance of this edge
	 */
//	public void updateEdgeInterface() {
//		getAttributeMap().put(AttributeMap.LINEWIDTH, new Float(this.getWeight() / 2f));
//		getAttributeMap().put(AttributeMap.EDGECOLOR, StageGraphColors.getEdgeColor((float) this.getWeight()));
//		getAttributeMap().put(AttributeMap.LABEL, getEdgeLabel());
//
//	}
//	
//	public String[] getEdgeLabel() {
//		return new String[] { StageGraph.format(this.relativeWeight)};
//	}

};
