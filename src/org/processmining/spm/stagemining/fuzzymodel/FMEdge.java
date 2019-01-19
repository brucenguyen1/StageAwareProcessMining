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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.SwingConstants;

/*
 * import java.io.IOException; import java.io.Writer;
 * 
 * import org.processmining.framework.models.DotFileWriter; import
 * org.processmining.plugins.fuzzymodel.miner.graph.Edge; import
 * org.processmining.plugins.fuzzymodel.miner.graph.MutableFuzzyGraph; import
 * org.processmining.plugins.fuzzymodel.miner.graph.Node;
 */
import org.jgraph.graph.GraphConstants;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;
import org.processmining.spm.stagemining.fuzzymodel.util.FMColors;
/**
 * @author christian
 * @author Li Jiafei
 * 
 */
public abstract class FMEdge<S extends FMNode, T extends FMNode> extends AbstractDirectedGraphEdge<S, T> {

	protected double significance;
	protected double correlation;
	protected double attenuationThreshold;

	public FMEdge(S source, T target, double weight, double label) {
		super(source, target);
		this.significance = weight;
		this.correlation = label;

		getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_SPLINE);
		//	GraphConstants.setRouting(getAttributeMap(), GraphConstants.ROUTING_SIMPLE);
		getAttributeMap().put(AttributeMap.SHOWLABEL, true);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_TECHNICAL);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
//		String label = "<html>" + MutableFuzzyGraph.format(significance) + "<br>"
//				+ MutableFuzzyGraph.format(correlation) + "<html>";
		NumberFormat formatter = new DecimalFormat("#0");
		getAttributeMap().put(AttributeMap.LABEL, formatter.format(label));
		getAttributeMap().put(AttributeMap.LABELCOLOR, Color.RED);
		getAttributeMap().put(AttributeMap.LABELHORIZONTALALIGNMENT, SwingConstants.EAST);
		getAttributeMap().put(AttributeMap.EDGECOLOR, FMColors.getEdgeColor((float) weight));
		// getAttributeMap().put(AttributeMap.LINEWIDTH, new Float(2 + Math.log(Math.E) * Math.log(significance*200)));
		double width = 2 + Math.log(Math.E) * Math.log(weight * 100);
		getAttributeMap().put(AttributeMap.LINEWIDTH, new Float(width > 1.0 ? width : 1.0));
		//getAttributeMap().put(AttributeMap.LABEL, source.toString() + " -->>" + target.toString());
	}

	public double getSignificance() {
		return significance;
	}

	public double getCorrelation() {
		return correlation;
	}

	public void setSignificance(double significance) {
		this.significance = significance;
	}

	public void setCorrelation(double correlation) {
		this.correlation = correlation;
	}

	public int hashCode() {
		return (source.hashCode() << 2) + target.hashCode();
	}

	public String toString() {
		return "Edge " + source.id() + " -> " + target.id();
	}

	public void setAttenuationThreshold(double attenuationThreshold) {
		this.attenuationThreshold = attenuationThreshold;
	}

	/**
	 * update GUI appearance of this edge
	 */
	public void updateEdgeInterface() {
		getAttributeMap().put(AttributeMap.LINEWIDTH, new Float(this.getSignificance() / 2f));
		getAttributeMap().put(AttributeMap.EDGECOLOR, FMColors.getEdgeColor((float) this.getCorrelation()));
		getAttributeMap().put(AttributeMap.LABEL, getEdgeLabel());

	}

	public String[] getEdgeLabel() {
		return new String[] { MutableFuzzyGraph.format(this.significance), MutableFuzzyGraph.format(this.correlation) };
	}

};
