package org.processmining.spm.stagemining.fuzzymodel.metrics;

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

// import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.Progress;
import org.processmining.spm.stagemining.fuzzymodel.attenuation.Attenuation;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.BinaryDerivateMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.UnaryDerivateMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.UnaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.util.FMLog;
import org.processmining.spm.stagemining.fuzzymodel.util.FMLogEvents;
import org.processmining.spm.stagemining.fuzzymodel.util.FuzzyMinerLog;

/**
 * @author christian
 * 
 */
public class LogScanner {

	private static final boolean DEBUG = false;
	protected int maxLookBack = 5;
	protected ArrayList<XEvent> lookBack;
	protected ArrayList<Integer> lookBackIndices;

	public void scan(XLog log, MetricsRepository metrics, Attenuation attenuation, int maxLookBack, Progress progress,
			int initialProgressValue) throws IndexOutOfBoundsException {
		if (progress != null) {
			progress.setCaption("Scanning log based metrics...");
		}
		this.maxLookBack = maxLookBack;
		FMLogEvents logEvents = FuzzyMinerLog.getLogEvents(log);

		XEvent referenceAte;
		int referenceIndex, followerIndex;
		double att;
		//Deal with every trace
	//	System.out.println("The total number of Process Instances is  "+ FuzzyMinerLog.getTraces(log).size());
		int num =1;
		for(XTrace pi : FuzzyMinerLog.getTraces(log)){
			//System.out.println("Begin to work on Instance " + num);
			lookBack = new ArrayList<XEvent>(maxLookBack);
			lookBackIndices = new ArrayList<Integer>(maxLookBack);
            for (XEvent followerAte : FuzzyMinerLog.getEvents(pi)){       	
				// update progress, if available
				if (progress != null) {
					initialProgressValue++;
					if (initialProgressValue % 200 == 0) {
						progress.setValue(initialProgressValue);
					}
				}
				// update look back buffer with next audit trail entry
				followerIndex = logEvents.findLogEventNumber(followerAte);
				//for degug
				if (DEBUG) {
					String followeEventname = FMLog.getConceptName(followerAte);
					System.out.println("Now!!! The EventName of the followerAte is " + followeEventname);
					System.out.println("Now!!! The followerIndex is " + followerIndex);

				}
				lookBack.add(0, followerAte);
				lookBackIndices.add(0, followerIndex);
				if (lookBack.size() > (maxLookBack + 1)) {
					// trim look back buffer
					lookBack.remove((maxLookBack + 1));
					lookBackIndices.remove((maxLookBack + 1));
				}
				// transmit event to unary metrics
				for (UnaryLogMetric metric : metrics.getUnaryLogMetrics()) {
					metric.measure(followerAte, followerIndex);
				}
				// iterate over multi-step relations
				for (int k = 1; k < lookBack.size(); k++) {
					referenceAte = lookBack.get(k);
					referenceIndex = lookBackIndices.get(k);
					if (DEBUG) {
						String referenceEventname = FMLog.getConceptName(referenceAte);
						System.out.println("Now!!! The EventName of the referenceAte is " + referenceEventname);
						System.out.println("Now!!! The referenceIndex is " + referenceIndex);

					}
					att = attenuation.getAttenuationFactor(k);
					// transmit relation to all registered metrics
					for (BinaryLogMetric metric : metrics.getBinaryLogMetrics()) {
						metric.measure(referenceAte, followerAte, referenceIndex, followerIndex, att);
					}
				}
			}
			num++;
		}
		// calculate derivate metrics
		List<UnaryDerivateMetric> unaryDerivateMetrics = metrics.getUnaryDerivateMetrics();
		for (UnaryDerivateMetric metric : unaryDerivateMetrics) {
			metric.measure();
		}
		List<BinaryDerivateMetric> binaryDerivateMetrics = metrics.getBinaryDerivateMetrics();
		for (BinaryDerivateMetric metric : binaryDerivateMetrics) {
			metric.measure();
		}
	}
}
