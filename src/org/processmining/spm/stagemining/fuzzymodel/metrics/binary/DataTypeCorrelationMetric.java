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

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XEvent;

/**
 * @author christian
 * @author Li Jiafei
 * 
 */

public class DataTypeCorrelationMetric extends CorrelationBinaryLogMetric {

	/**
	 * @param eventCount
	 */
	public DataTypeCorrelationMetric(int eventCount) {
		super("Data type correlation",
				"Measures the correlation of two events by their relative overlap of attribute types.", eventCount);
	}

	/**
	 * 
	 */
	public DataTypeCorrelationMetric() {
		super("Data type correlation",
				"Measures the correlation of two events by their relative overlap of attribute types.");
	}

	protected boolean isStandardKey(String key) {
		if (key.contains("concept") || key.contains("lifecycle") || key.contains("org") || key.contains("time")
				|| key.contains("semantic")) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryLogMetric#measure(
	 * org.processmining.framework.log.AuditTrailEntry,
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	protected double measure(XEvent reference, XEvent follower) {
		Set<String> refKeySet = reference.getAttributes().keySet();
		Set<String> folKeySet = follower.getAttributes().keySet();
		Set<String> refDataKeySet = new HashSet<String>();
		Set<String> folDataKeySet = new HashSet<String>();

		//omit the standard XES Extentions,keep the data type extensions only
		for (String key : refKeySet) {
			if (!isStandardKey(key)) {
				refDataKeySet.add(key);
			}
		}
		for (String key : folKeySet) {
			if (!isStandardKey(key)) {
				folDataKeySet.add(key);
			}
		}

		if ((refDataKeySet.size() == 0) || (folDataKeySet.size() == 0)) {
			return 0.0;
		}
		int overlap = 0;
		for (String key : refDataKeySet) {
			if (folDataKeySet.contains(key)) {
				overlap++;
			}
		}
		return (overlap / refDataKeySet.size());

	}

}
