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
package org.processmining.spm.stagemining.fuzzymodel.metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.spm.stagemining.fuzzymodel.attenuation.Attenuation;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.AggregateBinaryMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.BinaryDerivateMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.CorrelationBinaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.DataTypeCorrelationMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.DataValueCorrelationMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.DistanceSignificanceMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.EndpointCorrelationMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.FrequencySignificanceMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.OriginatorCorrelationMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.ProximityCorrelationMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.SignificanceBinaryDerivateMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.binary.SignificanceBinaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.trace.TraceMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.AggregateUnaryMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.FrequencySignificanceUnaryMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.RoutingSignificanceMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.UnaryDerivateMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.UnaryLogMetric;
import org.processmining.spm.stagemining.fuzzymodel.metrics.unary.UnaryMetric;

public class MetricsRepository {

	public static MetricsRepository createRepository(XLogInfo logSummary) {
		// set up environment
		int numberOfEvents = logSummary.getEventClasses().size();
		
		MetricsRepository repository = new MetricsRepository(numberOfEvents);
		// add standard list of metrics to repository
		// trace metrics
		// primary / log metrics; unary metrics
		repository.addMetric(new FrequencySignificanceUnaryMetric(numberOfEvents));
		// primary / log metrics; binary metrics significance
		repository.addMetric(new FrequencySignificanceMetric(numberOfEvents));
		// primary / log metrics; binary metrics correlation
		repository.addMetric(new ProximityCorrelationMetric(numberOfEvents));
		repository.addMetric(new EndpointCorrelationMetric(numberOfEvents));
		repository.addMetric(new OriginatorCorrelationMetric(numberOfEvents));
		repository.addMetric(new DataTypeCorrelationMetric(numberOfEvents));
		repository.addMetric(new DataValueCorrelationMetric(numberOfEvents));
		// secondary / derivate metrics; unary metrics
		repository.addMetric(new RoutingSignificanceMetric(repository));
		// secondary / derivate metrics; binary metrics significance
		repository.addMetric(new DistanceSignificanceMetric(repository));
		return repository;
	}
	/*
	 * this is to create metrics without determining the number of events. It is
	 * used for Fuzzy Map Miner which will transform the log first and the
	 * number of events can be decided then.
	 */
	public static MetricsRepository createRepository() {
		// set up environment		
		MetricsRepository repository = new MetricsRepository();
		// add standard list of metrics to repository
		// trace metrics
		// primary / log metrics; unary metrics
		repository.addMetric(new FrequencySignificanceUnaryMetric());
		// primary / log metrics; binary metrics significance
		repository.addMetric(new FrequencySignificanceMetric());
		// primary / log metrics; binary metrics correlation
		repository.addMetric(new ProximityCorrelationMetric());
		repository.addMetric(new EndpointCorrelationMetric());
		repository.addMetric(new OriginatorCorrelationMetric());
		repository.addMetric(new DataTypeCorrelationMetric());
		repository.addMetric(new DataValueCorrelationMetric());
		// secondary / derivate metrics; unary metrics
		repository.addMetric(new RoutingSignificanceMetric(repository));
		// secondary / derivate metrics; binary metrics significance
		repository.addMetric(new DistanceSignificanceMetric(repository));
		return repository;
	}
	
	
	

	// member attributes
	protected XLog log;
	protected int numberOfLogEvents;

	// metrics buckets
	protected ArrayList<TraceMetric> traceMetrics;
	protected ArrayList<UnaryMetric> unaryMetrics;
	protected ArrayList<UnaryLogMetric> unaryLogMetrics;
	protected ArrayList<BinaryLogMetric> binaryLogMetrics;
	protected ArrayList<SignificanceBinaryLogMetric> significanceBinaryLogMetrics;
	protected ArrayList<CorrelationBinaryLogMetric> correlationBinaryLogMetrics;
	protected ArrayList<BinaryDerivateMetric> binaryDerivateMetrics;
	protected ArrayList<UnaryDerivateMetric> unaryDerivateMetrics;
	protected ArrayList<BinaryMetric> significanceBinaryMetrics;
	protected ArrayList<BinaryMetric> correlationBinaryMetrics;

	protected AggregateUnaryMetric aggUnaryMetric;
	protected AggregateUnaryMetric aggUnaryLogMetric;
	protected AggregateBinaryMetric aggSignificanceBinaryLogMetric;
	protected AggregateBinaryMetric aggCorrelationBinaryLogMetric;
	protected AggregateBinaryMetric aggSignificanceBinaryMetric;
	protected AggregateBinaryMetric aggCorrelationBinaryMetric;

	public MetricsRepository() {
		log = null;
		traceMetrics = new ArrayList<TraceMetric>();
		unaryMetrics = new ArrayList<UnaryMetric>();
		unaryLogMetrics = new ArrayList<UnaryLogMetric>();
		binaryLogMetrics = new ArrayList<BinaryLogMetric>();
		significanceBinaryLogMetrics = new ArrayList<SignificanceBinaryLogMetric>();
		correlationBinaryLogMetrics = new ArrayList<CorrelationBinaryLogMetric>();
		binaryDerivateMetrics = new ArrayList<BinaryDerivateMetric>();
		unaryDerivateMetrics = new ArrayList<UnaryDerivateMetric>();
		significanceBinaryMetrics = new ArrayList<BinaryMetric>();
		correlationBinaryMetrics = new ArrayList<BinaryMetric>();
		aggUnaryMetric = null;
		aggUnaryLogMetric = null;
		aggSignificanceBinaryLogMetric = null;
		aggCorrelationBinaryLogMetric = null;
		aggSignificanceBinaryMetric = null;
		aggCorrelationBinaryMetric = null;
	}

	public MetricsRepository(int numberOfLogEvents) {
		this();
		this.numberOfLogEvents = numberOfLogEvents;

	}

	/**
	 * @throws IOException
	 */


		public void apply(XLog log, Attenuation att, int maxRelationDistance, PluginContext context) 
				throws IndexOutOfBoundsException {
			//get LogInfo
			XLogInfo logSummary = XLogInfoFactory.createLogInfo(log);
			int numOfEventClasses = logSummary.getEventClasses().size();
			// prepare progress
			int steps = traceMetrics.size() 
				+ unaryDerivateMetrics.size() 
				+ binaryDerivateMetrics.size() 
				+ numOfEventClasses;
			int currentProgress = 0;
			context.getProgress().setMinimum(0);	
			context.getProgress().setMaximum(steps);
			context.getProgress().setIndeterminate(false);
			context.getProgress().setValue(0);
			context.log("Scanning metrics...");
			
			// set log attribute
			this.log = log;
			// run trace metrics
			for(TraceMetric metric : traceMetrics) {
				context.getProgress().setValue(currentProgress);
				context.log("Calculating " + metric.getName());	
				metric.measure(log);
				currentProgress++;
			}
			
			// derive primary log metrics using scanner
			LogScanner scanner = new LogScanner();
			scanner.scan(log, this, att, maxRelationDistance, context.getProgress(), currentProgress);
			currentProgress += numOfEventClasses;

			// derive secondary derivate metrics
			for(UnaryDerivateMetric metric : unaryDerivateMetrics) {
				context.getProgress().setValue(currentProgress);
				context.log("Calculating " + metric.getName());
				metric.measure();
				currentProgress++;	
						
			}
			for(BinaryDerivateMetric metric : binaryDerivateMetrics) {
				context.getProgress().setValue(currentProgress);
				context.log("Calculating " + metric.getName());
				metric.measure();
				currentProgress++;	
						
			}
		context.getProgress().setValue(currentProgress);
	}

	public XLog getLogReader() {
		return log;
	}

	public int getNumberOfLogEvents() {
		return numberOfLogEvents;
	}

	public void setNumberOfLogEvents(int eventCount) {
		numberOfLogEvents = eventCount;
	}

	public void addMetric(Metric metric) {
		// add metric to all applicable buckets
		if (metric instanceof TraceMetric) {
			traceMetrics.add((TraceMetric) metric);
		} else if (metric instanceof UnaryMetric) {
			unaryMetrics.add((UnaryMetric) metric);
			if (metric instanceof UnaryLogMetric) {
				unaryLogMetrics.add((UnaryLogMetric) metric);
				aggUnaryLogMetric = null;
			} else if (metric instanceof UnaryDerivateMetric) {
				unaryDerivateMetrics.add((UnaryDerivateMetric) metric);
			}
			aggUnaryMetric = null;
		} else if (metric instanceof BinaryLogMetric) {
			binaryLogMetrics.add((BinaryLogMetric) metric);
			if (metric instanceof SignificanceBinaryLogMetric) {
				significanceBinaryLogMetrics.add((SignificanceBinaryLogMetric) metric);
				significanceBinaryMetrics.add((BinaryMetric) metric);
				aggSignificanceBinaryMetric = null;
				aggSignificanceBinaryLogMetric = null;
			} else if (metric instanceof CorrelationBinaryLogMetric) {
				correlationBinaryLogMetrics.add((CorrelationBinaryLogMetric) metric);
				correlationBinaryMetrics.add((BinaryMetric) metric);
				aggCorrelationBinaryMetric = null;
				aggCorrelationBinaryLogMetric = null;
			}
		} else if (metric instanceof BinaryDerivateMetric) {
			binaryDerivateMetrics.add((BinaryDerivateMetric) metric);
			if (metric instanceof SignificanceBinaryDerivateMetric) {
				significanceBinaryMetrics.add((BinaryMetric) metric);
				aggSignificanceBinaryMetric = null;
			}
		}
	}

	public void setLog(XLog log) {
		this.log = log;

	}

	public List<TraceMetric> getTraceMetrics() {
		return traceMetrics;
	}

	public List<UnaryMetric> getUnaryMetrics() {
		return unaryMetrics;
	}

	public List<UnaryLogMetric> getUnaryLogMetrics() {
		return unaryLogMetrics;
	}

	public List<BinaryLogMetric> getBinaryLogMetrics() {
		return binaryLogMetrics;
	}

	public List<UnaryDerivateMetric> getUnaryDerivateMetrics() {
		return unaryDerivateMetrics;
	}

	public List<BinaryDerivateMetric> getBinaryDerivateMetrics() {
		return binaryDerivateMetrics;
	}

	public List<SignificanceBinaryLogMetric> getSignificanceBinaryLogMetrics() {
		return significanceBinaryLogMetrics;
	}

	public List<CorrelationBinaryLogMetric> getCorrelationBinaryLogMetrics() {
		return correlationBinaryLogMetrics;
	}

	public List<BinaryMetric> getCorrelationBinaryMetrics() {
		return correlationBinaryMetrics;
	}

	public List<BinaryMetric> getSignificanceBinaryMetrics() {
		return significanceBinaryMetrics;
	}

	public AggregateUnaryMetric getAggregateUnaryMetric() {
		if (aggUnaryMetric == null) {
			aggUnaryLogMetric = new AggregateUnaryMetric("Aggregate unary metrics",
					"Aggregation of all unary metrics in repository", unaryMetrics);
		}
		return aggUnaryLogMetric;
	}

	public AggregateUnaryMetric getAggregateUnaryLogMetric() {
		if (aggUnaryLogMetric == null) {
			ArrayList<UnaryMetric> tmpUnaryMetrics = new ArrayList<UnaryMetric>(unaryLogMetrics.size());
			tmpUnaryMetrics.addAll(unaryLogMetrics);
			aggUnaryLogMetric = new AggregateUnaryMetric("Aggregate unary log metrics",
					"Aggregation of all unary log metrics in repository", tmpUnaryMetrics);
		}
		return aggUnaryLogMetric;
	}

	public AggregateBinaryMetric getAggregateSignificanceBinaryLogMetric() {
		if (aggSignificanceBinaryLogMetric == null) {
			ArrayList<BinaryMetric> binaryMetrics = new ArrayList<BinaryMetric>(significanceBinaryLogMetrics.size());
			binaryMetrics.addAll(significanceBinaryLogMetrics);
			aggSignificanceBinaryLogMetric = new AggregateBinaryMetric("Aggregate binary significance log metrics",
					"Aggregation of all binary significance log metrics in repository", binaryMetrics);
		}
		return aggSignificanceBinaryLogMetric;
	}

	public AggregateBinaryMetric getAggregateCorrelationBinaryLogMetric() {
		if (aggCorrelationBinaryLogMetric == null) {
			ArrayList<BinaryMetric> binaryMetrics = new ArrayList<BinaryMetric>(correlationBinaryLogMetrics.size());
			binaryMetrics.addAll(correlationBinaryLogMetrics);
			aggCorrelationBinaryLogMetric = new AggregateBinaryMetric("Aggregate binary correlation log metric",
					"Aggregation of all binary correlation log metrics in repository", binaryMetrics);
		}
		return aggCorrelationBinaryLogMetric;
	}

	public AggregateBinaryMetric getAggregateSignificanceBinaryMetric() {
		if (aggSignificanceBinaryMetric == null) {
			ArrayList<BinaryMetric> binaryMetrics = new ArrayList<BinaryMetric>(significanceBinaryMetrics.size());
			binaryMetrics.addAll(significanceBinaryMetrics);
			aggSignificanceBinaryMetric = new AggregateBinaryMetric("Aggregate binary significance metrics",
					"Aggregation of all binary significance metrics in repository", binaryMetrics);
		}
		return aggSignificanceBinaryMetric;
	}

	public AggregateBinaryMetric getAggregateCorrelationBinaryMetric() {
		if (aggCorrelationBinaryMetric == null) {
			ArrayList<BinaryMetric> binaryMetrics = new ArrayList<BinaryMetric>(correlationBinaryMetrics.size());
			binaryMetrics.addAll(correlationBinaryMetrics);
			aggCorrelationBinaryMetric = new AggregateBinaryMetric("Aggregate binary correlation metrics",
					"Aggregation of all binary correlation metrics in repository", binaryMetrics);
		}
		return aggCorrelationBinaryMetric;
	}

}
