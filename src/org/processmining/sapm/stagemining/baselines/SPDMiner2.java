package org.processmining.sapm.stagemining.baselines;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connections.simpleprecedencediagram.LogSPDConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDFactory;
import org.processmining.models.simpleprecedencediagram.SPDNode;

//Bruce: this class is copied from the SPDMiner class in the Performance package.
//The purpose is to create non-overlapping clusters. 
@Plugin(name = "Mine SPD Model (Stage-Aware Process Mining)", parameterLabels = { "Log", "Summary", "Number of Clusters" }, 
		returnLabels = "Simple Precedence Diagram", returnTypes = SPD.class, userAccessible = true)
public class SPDMiner2 {

	private Map<Pair<XEventClass, XEventClass>, Integer> directFollowsDependencies;
	private Map<Pair<XEventClass, XEventClass>, Long> directFollowsTotalTimes;
	private Map<XEventClass, Long> totalTimeSinceTraceStart;
	/*
	 * [HV] The field SPDMiner.log is never read locally private XLog log;
	 */
	private XEventClasses classes;
	private int maximumDirectSuccession;
	private final static int MAXITER = 1000;
	private final static int STEPS = 100;
	private final static int SIGNIFICANCE = 1; //100;
	private static final double FUZZIFIER = 2.0;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl", pack="Performance")
	@PluginVariant(variantLabel = "Default Summary, user-specified clusters.", requiredParameterLabels = { 0 })
	public SPD computeClusters(UIPluginContext context, XLog log) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);

		Integer[] options = new Integer[summary.getEventClasses().size()];
		for (int i = 0; i < options.length; i++) {
			options[i] = i + 1;
		}

		JPanel panel = new JPanel(new FlowLayout());
		JComboBox combo = new JComboBox(options);
		combo.setPreferredSize(new Dimension(200, 25));
		combo.setSize(new Dimension(200, 25));
		combo.setMinimumSize(new Dimension(200, 25));
		combo.setSelectedItem(new Integer(summary.getEventClasses().size() / 2));
		panel.add(combo);

		InteractionResult result = context.showConfiguration("Select the number of clusters", panel);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
		}

		return computeClusters(context, log, summary, (Integer) combo.getSelectedItem());
	}
	

	@PluginVariant(variantLabel = "Default Summary", requiredParameterLabels = { 0, 2 })
	public SPD computeClusters(PluginContext context, XLog log, Integer clusters) {
		XEventNameClassifier classifier = new XEventNameClassifier();
		return computeClusters(context, log, XLogInfoFactory.createLogInfo(log, classifier), clusters);
	}

	@PluginVariant(variantLabel = "Given Summary", requiredParameterLabels = { 0, 1, 2 })
	public SPD computeClusters(PluginContext context, XLog log, XLogInfo summary, Integer clusters) {
		long startTime = System.currentTimeMillis();

		String logName = log.getAttributes().get("concept:name").toString();
		Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(STEPS);
		progress.setIndeterminate(false);
		initialize(log, summary, progress);
		if (progress.isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		Map<XEventClass, Set<XEventClass>> foundClusters = getFuzzyCMedoidClusters(context, clusters, FUZZIFIER);
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		
		PrintWriter writer=null;
		try {
			writer = new PrintWriter(System.getProperty("user.dir") + "//SPD_Result_" + logName + ".txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	    
	    writer.println("Total Time: " + totalTime + " milliseconds");
	    writer.println("Clusters: ");
	    writer.println(foundClusters.values().toString());
//	    writer.println("Clusters with medoids: ");
//	    writer.println(foundClusters.toString());

		
		//------------- Bruce: ------------------------
		// Print clusters and compute Rand Index
		//---------------------------------------------
		
		// Check overlap
	    writer.println("Check Overlap");
		for (Set<XEventClass> s1 : foundClusters.values()) {
			for (Set<XEventClass> s2 : foundClusters.values()) {
				if (s2 != s1) {
					Set<XEventClass> test = new HashSet<XEventClass>(s2);
					test.retainAll(s1);
					if (!test.isEmpty()) {
						writer.println("OVERLAP!!!!!!!");
						writer.println(test.toString());
					}
				}
			}
		}
		
		List<Set<String>> listClusters = new ArrayList<>();
		int i=0;
		for (Set<XEventClass> ec : foundClusters.values()) {
			Set<String> cluster = new HashSet();
			for (XEventClass e : ec) {
				String id = e.getId();
				if (id.contains("+complete")) {
					cluster.add((id.substring(0, id.indexOf("+complete")).toLowerCase()));
				}
				else {
					cluster.add(id.toLowerCase());
				}
			}
			listClusters.add(cluster);
			
			i++;
			writer.println("==== Cluster " + i + " =====");
			writer.println(ec.toString());
		}
		writer.close();
		
		try {
//			StageModelBPI2012 test = new StageModelBPI2012();
//			StageModelBPI2013 test = new StageModelBPI2013();
//			StageModelBPI2015 test = new StageModelBPI2015();
//			double randIndex = Measure.computeMeasure(listClusters, test.getGroundTruth(log),1);
//			double fowlkes = Measure.computeMeasure(listClusters, test.getGroundTruth(log),2);
//			double jaccard = Measure.computeMeasure(listClusters, test.getGroundTruth(log),3);
//			System.out.println("Rand Index = " + randIndex);
//			System.out.println("Fowlkes–Mallows Index = " + fowlkes);
//			System.out.println("Jaccard Index = " + jaccard);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * Creation of SPD diagram
		 */
		SPD diagram = SPDFactory.newSPD("SPD of " + XConceptExtension.instance().extractName(log) + " (" + clusters
				+ " clusters)");

		Map<XEventClass, SPDNode> cluster2Node = new HashMap<XEventClass, SPDNode>();

		Map<XEventClass, Set<SPDNode>> class2Node = new HashMap<XEventClass, Set<SPDNode>>();
		for (XEventClass eventClass : classes.getClasses()) {
			class2Node.put(eventClass, new HashSet<SPDNode>(1));
		}

		for (XEventClass eventClass : foundClusters.keySet()) {
			String label = "<html>";
			boolean first = true;
			for (XEventClass element : foundClusters.get(eventClass)) {
				label += (first ? "" : "<br>") + element.toString();
				first = false;
			}
			label += "</html>";
			SPDNode node = diagram.addNode(label);

			cluster2Node.put(eventClass, node);
			for (XEventClass element : foundClusters.get(eventClass)) {
				class2Node.get(element).add(node);
			}
		}

		// to store result
		Collection<Pair<SPDNode, XEventClass>> relations = new LinkedList<Pair<SPDNode, XEventClass>>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				XEventClass eventClass = classes.getClassOf(event);
				for (SPDNode node : class2Node.get(eventClass)) {
					Pair<SPDNode, XEventClass> newPair = new Pair<SPDNode, XEventClass>(node, eventClass);
					if (!relations.contains(newPair)) {
						relations.add(newPair);
					}
				}
			}
		}

		int maxSuccession = Integer.MIN_VALUE;
		for (XEventClass fromCluster : foundClusters.keySet()) {
			for (XEventClass toCluster : foundClusters.keySet()) {
				if (fromCluster == toCluster) {
					continue;
				}
				int succession = 0;
				for (XEventClass fromClass : foundClusters.get(fromCluster)) {
					for (XEventClass toClass : foundClusters.get(toCluster)) {
						if (foundClusters.get(fromCluster).contains(toClass)
								|| foundClusters.get(toCluster).contains(fromClass)) {
							// Indetermined if we ever left the cluster.
							continue;
						}
						Pair<XEventClass, XEventClass> pair = new Pair<XEventClass, XEventClass>(fromClass, toClass);
						succession += directFollowsDependencies.containsKey(pair) ? directFollowsDependencies.get(pair)
								: 0;
					}
				}

				if (succession > 0) {
					if (succession > maxSuccession) {
						maxSuccession = succession;
					}
					/*
					 * [HV] The local variable arc is never read
					 * SPDEdge<SPDNode, SPDNode> arc =
					 */diagram.addArc(cluster2Node.get(fromCluster), cluster2Node.get(toCluster));

					// arc.getAttributeMap().put(AttributeMap.LINEWIDTH,
					// new Float(1 + Math.log(Math.E) * Math.log(succession)));
					// arc.getAttributeMap().put(AttributeMap.LABEL, "" + succession);
					// arc.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				}
			}
		}
		/**
		 * End of creation of SPD diagram
		 */
		// add connection
		context.addConnection(new LogSPDConnection(log, classes, diagram, relations));
		context.getFutureResult(0).setLabel(diagram.getLabel());

		progress.inc();
		return diagram;
	}

	public void initialize(XLog log, XLogInfo summary, Progress progress) {
		/*
		 * [HV] The field SPDMiner.log is never read locally this.log = log;
		 */
		classes = summary.getEventClasses();

		directFollowsDependencies = new HashMap<Pair<XEventClass, XEventClass>, Integer>();
		directFollowsTotalTimes = new HashMap<Pair<XEventClass, XEventClass>, Long>();
		totalTimeSinceTraceStart = new HashMap<XEventClass, Long>();

		int iter = 0;
		int max = 0;
		for (XTrace trace : log) {
			if (progress.isCancelled()) {
				return;
			}
			
			//System.out.println("Trace: " + trace.getAttributes().get("concept:name").toString());

			Date start = XTimeExtension.instance().extractTimestamp(trace.get(0));

			for (int i = 0; i < trace.size() - 1; i++) {
				iter++;
				if (iter == (summary.getNumberOfEvents() / STEPS)) {
					iter = 0;
					progress.inc();
				}
				XEventClass fromEvent = classes.getClassOf(trace.get(i));
				Date fromEventTime = XTimeExtension.instance().extractTimestamp(trace.get(i));

				XEventClass toEvent = classes.getClassOf(trace.get(i + 1));
				Date toEventTime = XTimeExtension.instance().extractTimestamp(trace.get(i + 1));

				Pair<XEventClass, XEventClass> pair = new Pair<XEventClass, XEventClass>(fromEvent, toEvent);

				// update direct successions dependencies
				int n = directFollowsDependencies.containsKey(pair) ? directFollowsDependencies.get(pair) : 0;
				directFollowsDependencies.put(pair, n + 1);
				max = Math.max(max, n + 1);

				// update direct successions dependencies
				long l = directFollowsTotalTimes.containsKey(pair) ? directFollowsTotalTimes.get(pair) : 0;
				long dif = (toEventTime != null ? toEventTime.getTime() : 0)
						- (fromEventTime != null ? fromEventTime.getTime() : 0);
				directFollowsTotalTimes.put(pair, l + dif);

				dif = (toEventTime != null ? toEventTime.getTime() : 0) - (start != null ? start.getTime() : 0);
				long old = (totalTimeSinceTraceStart.containsKey(toEvent) ? totalTimeSinceTraceStart.get(toEvent) : 0);
				totalTimeSinceTraceStart.put(toEvent, old + dif);
			}
		}
		maximumDirectSuccession = max;
	}

	/**
	 * Compute the medoids and assign non-medoids to each medoid. 
	 * @param context
	 * @param c
	 * @param m
	 * @return: mapping from a medoid to a set of its cluster members
	 */
	private Map<XEventClass, Set<XEventClass>> getFuzzyCMedoidClusters(PluginContext context, int c, double m) {
		Map<XEventClass, Set<XEventClass>> result;
		Progress progress = context.getProgress();

		Set<XEventClass> nonMedoids = new HashSet<XEventClass>(classes.size());
		nonMedoids.addAll(classes.getClasses());

		// If the number of clusters is greater or equal to the number
		// of event classes, then return each event class in its own cluster
		if (c >= nonMedoids.size()) {
			result = new HashMap<XEventClass, Set<XEventClass>>(nonMedoids.size());
			for (XEventClass eventClass : nonMedoids) {
				Set<XEventClass> cluster = new HashSet<XEventClass>(1);
				cluster.add(eventClass);
				result.put(eventClass, cluster);
			}
			return result;
		}
		result = new HashMap<XEventClass, Set<XEventClass>>(c);

		// Select c random medoids (the first k as returned by the iterator over
		// nonMedoids (in java 6 this is random, in java 5 it is not))
		Set<XEventClass> medoids;
		Set<XEventClass> newMedoids = new HashSet<XEventClass>(c);

		int i = 0;
		do {
			Iterator<XEventClass> it = nonMedoids.iterator();
			XEventClass medoid = it.next();
			if (Math.random() < .75) {
				continue;
			}
			newMedoids.add(medoid);
			it.remove();
			i++;
		} while (i < c);

		Map<Pair<XEventClass, XEventClass>, Double> U;
		int iter = 0;
		int prog_inc = 0;
		do {
			iter++;
			prog_inc++;
			if (prog_inc == (MAXITER / STEPS)) {
				prog_inc = 0;
				progress.inc();
			}
			medoids = newMedoids;

			// Compute the U_{ij} values
			U = computeUValues(medoids, m);
			printUMatrix(context, c, medoids, U);

			// Compute the new medoids
			newMedoids = new HashSet<XEventClass>(c);
			for (XEventClass medoid : medoids) {
				double min = Double.MAX_VALUE;
				XEventClass minMedoid = null;
				for (XEventClass eventClass_k : classes.getClasses()) {
					double sum = 0;
					for (XEventClass eventClass_j : classes.getClasses()) {
						double r_comp = getSimilarity(eventClass_k, eventClass_j);
						double r = 1.0 / r_comp;

						sum += Math.pow(U.get(new Pair<XEventClass, XEventClass>(medoid, eventClass_j)), m) * r;
					}
					if ((sum < min) && !newMedoids.contains(eventClass_k)) {
						min = sum;
						minMedoid = eventClass_k;
					}
				}
				newMedoids.add(minMedoid);
			}
		} while (!newMedoids.equals(medoids) && (iter < SPDMiner2.MAXITER));

		medoids = newMedoids;
		U = computeUValues(medoids, m);

		// compute the clusters by iterating over the nonMediods.
		// for each of them, find the medoid closest to it and add the nonMedoid
		// to the cluster belonging to this medoid.

		// Initialize the clusters
		for (XEventClass medoid : medoids) {
			result.put(medoid, new TreeSet<XEventClass>());
		}

		// Put all eventClasses in the cluster belonging to the
		// medoid for which the probability of that eventClass belonging
		// there is highest (there may be more)
		for (XEventClass eventClass : classes.getClasses()) {

			// First, find the higest probability recorded for any of the medoids
			double max = Double.MIN_VALUE;
			XEventClass maxMedoid = null;
			for (XEventClass medoid : medoids) {
				double val = U.get(new Pair<XEventClass, XEventClass>(medoid, eventClass));
				if (val > max) {
					max = val;
					maxMedoid = medoid;
				}
			}

			// Check all medoids and verify whether the probability is the same as 
			// the maximum probability (precision depends on significance)
//			for (XEventClass medoid : medoids) {
//				double val = U.get(new Pair<XEventClass, XEventClass>(medoid, eventClass));
//				if (Math.round(SIGNIFICANCE * val) == Math.round(SIGNIFICANCE * max)) {	
//					result.get(medoid).add(eventClass);
//				}
//			}
			
			//Bruce: replace the above with this crisp function for non-overlapping clusters
			result.get(maxMedoid).add(eventClass); 
		}

		return result;
	}

	private Map<Pair<XEventClass, XEventClass>, Double> computeUValues(Collection<XEventClass> medoids, double m) {
		HashMap<Pair<XEventClass, XEventClass>, Double> U = new HashMap<Pair<XEventClass, XEventClass>, Double>();
		for (XEventClass eventClass : classes.getClasses()) {
			double sum = 0;
			for (XEventClass medoid_k : medoids) {
				double r_comp = getSimilarity(medoid_k, eventClass);
				sum += Math.pow(r_comp, 1 / (m - 1));
			}
			for (XEventClass medoid_i : medoids) {
				double r_comp = getSimilarity(medoid_i, eventClass);
				U.put(new Pair<XEventClass, XEventClass>(medoid_i, eventClass), Math.pow(r_comp, 1 / (m - 1)) / sum);
			}
		}
		return U;
	}

	private void printUMatrix(PluginContext context, int c, Collection<XEventClass> medoids,
			Map<Pair<XEventClass, XEventClass>, Double> U) {
		ArrayList<XEventClass> cl = new ArrayList<XEventClass>(classes.getClasses());
		String[][] report = new String[c + 1][cl.size() + 1];
		int i = 1;
		for (XEventClass medoid : medoids) {
			report[i][0] = medoid.toString();
			int j = 1;
			for (XEventClass eventClass : cl) {
				report[i][j] = U.get(new Pair<XEventClass, XEventClass>(medoid, eventClass)).toString();
				j++;
			}
			i++;
		}
		int j = 1;
		for (XEventClass eventClass : cl) {
			report[0][j] = eventClass.toString();
			j++;
		}

		for (i = 0; i < report.length; i++) {
			context.log(Arrays.toString(report[i]), MessageLevel.DEBUG);
		}
		context.log("", MessageLevel.DEBUG);
	}

	private double getSimilarity(XEventClass first, XEventClass second) {
		if (first.equals(second)) {
			return 1;
		}
		Pair<XEventClass, XEventClass> p1 = new Pair<XEventClass, XEventClass>(first, second);
		Pair<XEventClass, XEventClass> p2 = new Pair<XEventClass, XEventClass>(second, first);
		int r1 = directFollowsDependencies.containsKey(p1) ? directFollowsDependencies.get(p1) : 0;
		int r2 = directFollowsDependencies.containsKey(p2) ? directFollowsDependencies.get(p2) : 0;
		return (double) (r1 + r2 + 1) / (double) (2 * maximumDirectSuccession + 1);
	}

}
