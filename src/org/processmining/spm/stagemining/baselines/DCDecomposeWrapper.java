package org.processmining.spm.stagemining.baselines;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarraycreator.metrics.ActivityClusterArrayMetric;
import org.processmining.activityclusterarraycreator.metrics.impl.ActivityClusterArrayBalanceMetric;
import org.processmining.activityclusterarraycreator.metrics.impl.ActivityClusterArrayCohesionMetric;
import org.processmining.activityclusterarraycreator.metrics.impl.ActivityClusterArrayCouplingMetric;
import org.processmining.activityclusterarraycreator.metrics.impl.ActivityClusterArrayOverlapMetric;
import org.processmining.activityclusterarraycreator.metrics.impl.ActivityClusterArrayWeightedMetric;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixminer.miners.impl.FuzzyMatrixMiner;
import org.processmining.causalactivitymatrixminer.miners.impl.HAFMidiMatrixMiner;
import org.processmining.causalactivitymatrixminer.miners.impl.HeuristicsMatrixMiner;
import org.processmining.decomposedminer.configurations.impl.DecomposedAbstractClusteringDiscoveryConfiguration;
import org.processmining.decomposedminer.configurations.impl.DecomposedFlexibleDiscoveryConfiguration2;
import org.processmining.decomposedminer.parameters.DecomposedDiscoveryParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.utils.Measure;
import org.processmining.spm.utils.OpenLogFilePlugin;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DCDecomposeWrapper extends DecomposedAbstractClusteringDiscoveryConfiguration {
	private double bestScore = 0.0;
	public final static String NAME = "Decompose";
	
	public static void main(String[] args) {
		try {
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			System.out.println("Import log file");
			String filename = "BPIC12_complete_events_only.xes.gz";
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + "\\logs\\" + filename));
			DCDecomposeWrapper dc = new DCDecomposeWrapper();
			ActivityClusterArray clusters = dc.findBestClustering(new FakePluginContext(), log);
//			System.out.println(clusters.toString());
			ExampleClass example = (ExampleClass)Class.forName("org.processmining.spm.stagemining.groundtruth.StageModelBPI2012").newInstance();
			double gtIndex = Measure.computeMeasure(dc.getActivityLabelSets(clusters), example.getGroundTruth(log), 2);
			System.out.println("Best cluster with ground-truth index = " + gtIndex);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private List<Set<String>> getActivityLabelSets(ActivityClusterArray clusters) {
		List<Set<String>> result = new ArrayList<>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			Set<String> labelSet = new HashSet<>();
			for (XEventClass eventClass : cluster) {
				labelSet.add(eventClass.getId());
			}
			result.add(labelSet);
		}
		System.out.println(result);
		return result;
	}
	
	public ActivityClusterArray findBestClustering (PluginContext context, XLog log) {
		long msecs;
		ActivityClusterArray clusters = null;
		DecomposedDiscoveryParameters parameters = new DecomposedDiscoveryParameters(log);
		DecomposedFlexibleDiscoveryConfiguration2 configuration = new DecomposedFlexibleDiscoveryConfiguration2();
		String[] miners = new String[] {
			(new FuzzyMatrixMiner()).getName(),
			(new HeuristicsMatrixMiner()).getName(),
			(new HAFMidiMatrixMiner()).getName()
		};
		double zeroValues[] = new double[] { -0.6, -0.5, 0.0, 0.5, 0.9 };
		double concurrencyRatios[] = new double[] { 0.0, 0.005 };
				
		ActivityClusterArray bestClusters = null;
		bestScore = 0.0;
		
		for (int m = 0; m < miners.length; m++) {
			configuration.setMiner(miners[m]);
			for (int z = 0; z < zeroValues.length; z++) {
				configuration.setZeroValue(zeroValues[z]);
				for (int c = 0; c < concurrencyRatios.length; c++) {
					configuration.setConcurrencyRatio(concurrencyRatios[c]);
					DecomposedDiscoveryParameters tempParameters = new DecomposedDiscoveryParameters(parameters);
					tempParameters.setConfiguration(configuration.getName());
					tempParameters.setPercentage(getPercentage(parameters));
					msecs = -System.currentTimeMillis();
					CausalActivityMatrix matrix = configuration.getMatrix(context, log, tempParameters);
					context.getProvidedObjectManager().createProvidedObject("Matrix", matrix, CausalActivityMatrix.class,
							context);
					msecs += System.currentTimeMillis();
					parameters.displayMessage("[DecomposedDiscoveryPlugin] Creating matrix took " + msecs + " milliseconds.");
					msecs = -System.currentTimeMillis();
					CausalActivityGraph graph = configuration.getGraph(context, matrix, tempParameters);
					context.getProvidedObjectManager().createProvidedObject("Graph", graph, CausalActivityGraph.class, context);
					msecs += System.currentTimeMillis();
					parameters.displayMessage("[DecomposedDiscoveryPlugin] Creating graph took " + msecs + " milliseconds.");
					msecs = -System.currentTimeMillis();
					clusters = configuration.getClusters(context, graph, tempParameters);
					context.getProvidedObjectManager().createProvidedObject("Clusters", clusters, ActivityClusterArray.class,
							context);
					msecs += System.currentTimeMillis();
					parameters.displayMessage("[DecomposedDiscoveryPlugin] Creating " + clusters.getClusters().size()
							+ " clusters took " + msecs + " milliseconds.");
					msecs = -System.currentTimeMillis();
					clusters = configuration.getModifiedClusters(context, graph, clusters, tempParameters);
					context.getProvidedObjectManager().createProvidedObject("Modified Clusters", clusters, ActivityClusterArray.class, context);
					msecs += System.currentTimeMillis();
					parameters.displayMessage("[DecomposedDiscoveryPlugin] Modifying to " + clusters.getClusters().size()
							+ " clusters took " + msecs + " milliseconds.");
					if (isBetter(context, clusters, graph)) {
						parameters.displayMessage("[DecomposedDiscoveryPlugin] New best score " + bestScore + ", configuration is " + configuration.getName());
						bestClusters = clusters;
					}
				}
			}
		}
		return bestClusters;
	}
	
	private boolean isBetter(PluginContext context, ActivityClusterArray clusters, CausalActivityGraph graph) {
		ActivityClusterArrayMetric weighted = new ActivityClusterArrayWeightedMetric(
				new ActivityClusterArrayOverlapMetric(100),
				new ActivityClusterArrayCohesionMetric(100),
				new ActivityClusterArrayCouplingMetric(100),
				new ActivityClusterArrayBalanceMetric(100));

		double score = clusters.getClusters().size() * weighted.compute(context, clusters, graph);
		if (score > bestScore) {
			bestScore = score;
			return true;
		}
		return false;
	}

	public int getPercentage(DecomposedDiscoveryParameters parameters) {
		return parameters.getPercentage();
	}
	
	public String getName() {
		return NAME;
	}

	public void update(NiceSlider slider) {
		slider.setEnabled(true);
	}
}
