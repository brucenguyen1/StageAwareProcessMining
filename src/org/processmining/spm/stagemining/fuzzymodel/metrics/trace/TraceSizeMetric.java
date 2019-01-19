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
package org.processmining.spm.stagemining.fuzzymodel.metrics.trace;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.spm.stagemining.fuzzymodel.util.FuzzyMinerLog;

/**
 * @author christian
 * 
 */
public class TraceSizeMetric extends TraceMetric {

	/**
	 * @param aName
	 * @param aDescription
	 * @param log
	 */
	public TraceSizeMetric(XLogInfo logSummary) {
		super("Trace size", "Measures traces by their relative size.", logSummary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.TraceMetric#measure(org.
	 * processmining.framework.log.LogReader)
	 */
	public void measure(XLog log) {
		/*
		 * ProcessInstance pi = null; for(int i=0; i<log.numberOfInstances();
		 * i++) { pi = log.getInstance(i); values[i] =
		 * pi.getAuditTrailEntryList().size(); }
		 */
		int i = 0;
		for (XTrace pi : FuzzyMinerLog.getTraces(log)) {
			values[i] = pi.size();
			i++;
		}
	}

}
