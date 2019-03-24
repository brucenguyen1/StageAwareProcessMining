package org.processmining.sapm.stagemining.baselines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;

/**
 * This class is created to create non-overlapping clusters
 * The algorithm is modified based on ConvertCausalActivityGraphToActivityClusterArrayAlgorithm
 * The main idea is the same based on identifying dense regions of the graph
 * @author Bruce
 *
 */
public class ConvertCausalActivityGraphToActivityClusterArrayAlgorithm2 {

	public ActivityClusterArray apply(PluginContext context, final CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		ActivityClusterArray clusters = ActivityClusterArrayFactory.createActivityClusterArray();
		List<XEventClass> activities = graph.getActivities();
		clusters.init(graph.getLabel(), new HashSet<XEventClass>(activities));
		List<Pair<XEventClass, XEventClass>> causalities = new ArrayList<Pair<XEventClass, XEventClass>>(
				graph.getSetCausalities());
		/*
		 * Sort the edges from heavy to light. As a result, we
		 * will start clustering with the most heavy edges.
		 */
//		Collections.sort(causalities, new Comparator<Pair<XEventClass, XEventClass>>() {
//			public int compare(Pair<XEventClass, XEventClass> p1, Pair<XEventClass, XEventClass> p2) {
//				Double d1 = graph.getCausality(p1.getFirst(), p1.getSecond());
//				Double d2 = graph.getCausality(p2.getFirst(), p2.getSecond());
//				return d2.compareTo(d1);
//			}
//		});

		while (!causalities.isEmpty()) {
			
			//Bruce: sort the edges to start from the heaviest weight first
			Collections.sort(causalities, new Comparator<Pair<XEventClass, XEventClass>>() {
				public int compare(Pair<XEventClass, XEventClass> p1, Pair<XEventClass, XEventClass> p2) {
					Double d1 = graph.getCausality(p1.getFirst(), p1.getSecond());
					Double d2 = graph.getCausality(p2.getFirst(), p2.getSecond());
					return d2.compareTo(d1);
				}
			});
			
			Pair<XEventClass, XEventClass> selectedCausality = causalities.iterator().next();
			causalities.remove(selectedCausality);
			Set<XEventClass> sources = new HashSet<XEventClass>();
			Set<XEventClass> targets = new HashSet<XEventClass>();
			Set<XEventClass> cluster = new HashSet<XEventClass>();
			
			sources.add(selectedCausality.getFirst()); // contains all source nodes so far
			targets.add(selectedCausality.getSecond()); // contains all target nodes so far
			
			cluster.addAll(sources); //always add the current edge endpoints to the new cluster 
			cluster.addAll(targets);
			
			boolean notReady = true; // notReady = false when cannot find any causalities whose nodes can be added to the current cluster
			//--------------------------------------------------------------
			// Bruce's comment
			// Create a new cluster starting from the selected edge
			// For the current edge, add its two ends to the cluster 
			// Check all edges that connect with its ends (source or target)
			// If the edge is heavy enough, add its two ends to the clusters
			// This is repeated on the list of all edges again and keeps the cluster growing
			// This growth only stops when it cannot find more new heavy enough edges that has its 
			// source and target not yet included in the cluster OR the size of the cluster 
			// exceeds a maximum size.
			// In this way, if the cluster size is not restricted (getClusterThreshold), then all nodes will
			// be added to one single cluster only. So, this threshold is not minimum but maximum size
			// and this makes all clusters must be less than this size.
			// 
			// Those edges that are unrelated to the current cluster (NO COMMON NODES with the current cluster) 
			// will be added to a new set for the next cluster creation round.
			//
			// Those edges that are related to the current cluster by one end but not heavy enough
			// will be left out. They will be considered later and their nodes are called unclustered activities
			// and they all will be added to a separate cluster. This cluster has no overlap with other clusters. 
			//
			// Each cluster created here will not overlap with any previously created clusters. 
			//
			// Overlapping occurs because of the maximum cluster size. A clusters of
			// that size is created, the next cluster is created with edges with one end
			// in the former cluster, but that end will also be included in the latter cluster.
			//--------------------------------------------------------------
			while (notReady && cluster.size() < parameters.getClusterThreshold()) {
				notReady = false;
				List<Pair<XEventClass, XEventClass>> newCausalities = new ArrayList<Pair<XEventClass, XEventClass>>();
				for (Pair<XEventClass, XEventClass> causality : causalities) {
					if (sources.contains(causality.getFirst()) || targets.contains(causality.getSecond())) {
						if (graph.getCausality(causality.getFirst(), causality.getSecond()) > parameters
								.getClusterFactor() * Math.log(1.0 * cluster.size())) {
							//notReady will be true if at least a new node is added to the current cluster
							notReady = sources.add(causality.getFirst()) || notReady; 
							notReady = targets.add(causality.getSecond()) || notReady;
							
							//Bruce: replace code to make non-overlapping clusters
//							cluster.add(causality.getFirst());
//							cluster.add(causality.getSecond());
							if (!this.isContained(clusters, causality.getFirst())) cluster.add(causality.getFirst());
							if (!this.isContained(clusters, causality.getSecond())) cluster.add(causality.getSecond());
						}
//					} else if (parameters.isCheckBackwardArc() && targets.contains(causality.getFirst())
//							&& sources.contains(causality.getSecond())) {
					} else if (parameters.isCheckBackwardArc() && 
							(targets.contains(causality.getFirst()) || sources.contains(causality.getSecond()))) {
						if (graph.getCausality(causality.getFirst(), causality.getSecond()) > parameters
								.getClusterFactor() * Math.log(1.0 * cluster.size())) {
							//notReady will be true if at least a new node is added to the current cluster
							notReady = sources.add(causality.getFirst()) || notReady;
							notReady = targets.add(causality.getSecond()) || notReady;

							//Bruce: replace code to make non-overlapping clusters
//							cluster.add(causality.getFirst());
//							cluster.add(causality.getSecond());
							if (!this.isContained(clusters, causality.getFirst())) cluster.add(causality.getFirst());
							if (!this.isContained(clusters, causality.getSecond())) cluster.add(causality.getSecond());
						}
					} else {
						newCausalities.add(causality); // contains edges with no common nodes with the current cluster
					}
				}
				causalities = newCausalities;
			}
			//			System.out.println(cluster);
			clusters.addCluster(cluster);
			clusters.setInputs(cluster, sources);
			clusters.setOutputs(cluster, targets);
			
			
		}
		
		//----------------------------------------------------------
		// Bruce: add all remaining activities that have not yet
		// selected into any clusters. They are gathered and added to
		// a separate cluster (no overlap with any other clusters)
		//----------------------------------------------------------
		ActivityClusterArray sanitiziedClusters = ActivityClusterArrayFactory.createActivityClusterArray();
		sanitiziedClusters.init(clusters);
		Set<XEventClass> unClusteredActivities = new HashSet<XEventClass>();
		if (parameters.isIncludeAll()) {
			for (XEventClass activity : activities) {
				boolean found = false;
				for (Set<XEventClass> cluster : clusters.getClusters()) {
					if (cluster.contains(activity)) {
						found = true;
					}
				}
				if (!found) {
					unClusteredActivities.add(activity);
				}
			}
			if (!unClusteredActivities.isEmpty()) {
				sanitiziedClusters.addCluster(unClusteredActivities);
				sanitiziedClusters.setInputs(unClusteredActivities, new HashSet<XEventClass>());
				sanitiziedClusters.setOutputs(unClusteredActivities, new HashSet<XEventClass>());
			}
		}
		
		//----------------------------------------------------------
		// Bruce: prevent subclusters and identical clusters
		// Subclusters and identical clusters will not be selected into
		// the final cluster array
		// For an identified subcluster, its inputs and outputs will be added to the inputs
		// and outputs of the containing clusters, respectively.
		//----------------------------------------------------------
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			boolean remove = false;
			// Prevent subclusters.
			for (Set<XEventClass> otherCluster : clusters.getClusters()) {
				if ((cluster != otherCluster) && cluster.size() < otherCluster.size()
						&& otherCluster.containsAll(cluster)) {
					remove = true;
					Set<XEventClass> inputs = clusters.getInputs(otherCluster);
					inputs.addAll(clusters.getInputs(cluster));
					clusters.setInputs(otherCluster, inputs);
					Set<XEventClass> outputs = clusters.getOutputs(otherCluster);
					outputs.addAll(clusters.getOutputs(cluster));
					clusters.setOutputs(otherCluster, outputs);
				}
			}
			// Prevent identical clusters.
			for (Set<XEventClass> otherCluster : sanitiziedClusters.getClusters()) {
				if (cluster.equals(otherCluster) 
						&& clusters.getInputs(cluster).equals(sanitiziedClusters.getInputs(otherCluster)) 
						&& clusters.getOutputs(cluster).equals(sanitiziedClusters.getOutputs(otherCluster))) {
					remove = true;
				}
			}
			if (!remove) {
				sanitiziedClusters.addCluster(cluster);
				sanitiziedClusters.setInputs(cluster, clusters.getInputs(cluster));
				sanitiziedClusters.setOutputs(cluster, clusters.getOutputs(cluster));
			}
		}
		
//		System.out.println("CLUSTERS CREATED");
//		System.out.println(sanitiziedClusters.getClusters().toString());
		
		return sanitiziedClusters;
	}
	
	/**
	 * Bruce
	 * @param clusters
	 * @param activity
	 * @return
	 */
	private boolean isContained(ActivityClusterArray clusters, XEventClass activity) {
		for (Set<XEventClass> c : clusters.getClusters()) {
			if(c.contains(activity)) {
				return true;
			}
		}
		return false;
	}
}
