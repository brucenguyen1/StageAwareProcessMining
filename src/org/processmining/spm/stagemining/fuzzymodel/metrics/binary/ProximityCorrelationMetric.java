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
package org.processmining.spm.stagemining.fuzzymodel.metrics.binary;

import java.util.Date;

import org.deckfour.xes.model.XEvent;
import org.processmining.spm.stagemining.fuzzymodel.util.FMLog;

/**
 * @author christian
 * 
 */
public class ProximityCorrelationMetric extends CorrelationBinaryLogMetric {

	/**
	 * @param eventCount
	 */
	public ProximityCorrelationMetric(int eventCount) {
		super("Proximity correlation", "Measures the correlation of two events by their temporal proximity", eventCount);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public ProximityCorrelationMetric() {
		super("Proximity correlation", "Measures the correlation of two events by their temporal proximity");
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryLogMetric#measure(
	 * org.processmining.framework.log.XEvent,
	 * org.processmining.framework.log.XEvent)
	 */
	protected double measure(XEvent reference, XEvent follower) {
		//Date tsRef = reference.getTimestamp();
		//Date tsFol = follower.getTimestamp();
		Date tsRef = FMLog.getTimestamp(reference);
		Date tsFol = FMLog.getTimestamp(follower);
//		String key = "time:timestamp";
//		XAttributeTimestampImpl tsref_attr = (XAttributeTimestampImpl) reference.getAttributes().get(key);
//		XAttributeTimestampImpl tsFol_attr = (XAttributeTimestampImpl) follower.getAttributes().get(key);
		if ((tsRef != null) && (tsFol != null)) {
//			Date tsRef = tsref_attr.getValue();
//			Date tsFol = tsFol_attr.getValue();
			long tRef = tsRef.getTime();
			long tFol = tsFol.getTime();
			if (tRef != tFol) {
				return (1.0 / (tFol - tRef));
			} else {
				return 1.0;
			}
		} else {
			return 0.0;
		}
	}
}
