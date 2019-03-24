package org.processmining.sapm.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.sapm.stagemining.graph.WeightedDirectedGraph;

public class ClusteringUtils {
	public static List<Set<IVertex>> getNodeClustering(ActivityClusterArray clusterArray, WeightedDirectedGraph graph) {
		List<Set<IVertex>> nodeSetList = new ArrayList<Set<IVertex>>();
		Set<IVertex> start  = new HashSet<IVertex>();
		Set<IVertex> end  = new HashSet<IVertex>();
		
		start.add(graph.getSource());
		nodeSetList.add(start);
		
		for (Set<XEventClass> cluster : clusterArray.getClusters()) {
			Set<IVertex> graphCluster = new HashSet<>();
			for (XEventClass eventClass : cluster) {
				graphCluster.add(graph.getVertexByName(eventClass.getId()));
			}
			nodeSetList.add(graphCluster);
		}		
		
		end.add(graph.getSink());
		nodeSetList.add(end);
		
		return nodeSetList;
	}
	
	public static List<Set<IVertex>> getNodeClustering(SPD spd, WeightedDirectedGraph graph) {
		List<Set<IVertex>> nodeSetList = new ArrayList<Set<IVertex>>();
		Set<IVertex> start  = new HashSet<IVertex>();
		Set<IVertex> end  = new HashSet<IVertex>();
		
		start.add(graph.getSource());
		nodeSetList.add(start);
		
		for (SPDNode node: spd.getNodes()) {
			Set<String> activitySet = ClusteringUtils.getActivitySet(node);
			Set<IVertex> graphCluster = new HashSet<>();
			for (String activityName : activitySet) {
				graphCluster.add(graph.getVertexByName(activityName));
			}
			nodeSetList.add(graphCluster);
		}	
		
		end.add(graph.getSink());
		nodeSetList.add(end);
		
		return nodeSetList;
	}
	
	/**
	 * 
	 * @param node: the node label has general format: <html>A<br>B<br>C</html>
	 * @return
	 */
	public static Set<String> getActivitySet(SPDNode node) {
		String nodeString = node.getLabel();
		String newNodeString = nodeString.substring(6); //remove <html>, e.g. A<br>B<br>C</html>
		newNodeString = newNodeString.substring(0, newNodeString.length() - 7); //remove </html>, e.g. A<br>B<br>C
		String[] activityArray = newNodeString.split("<br>"); // return [A,B,C]
		Set<String> activitySet = new HashSet<>();
		for (int i=0;i<activityArray.length;i++) {
			activitySet.add(activityArray[i]);
		}
		return activitySet;
	}
	
	public static List<Set<String>> getActivityClustering(ActivityClusterArray clusterArray) {
		List<Set<String>> labelSetList = new ArrayList<>();
		for (Set<XEventClass> cluster : clusterArray.getClusters()) {
			Set<String> labelSet = new HashSet<>();
			for (XEventClass eventClass : cluster) {
				labelSet.add(eventClass.getId());
			}
			labelSetList.add(labelSet);
		}	
		return labelSetList;
	}
	
	public static List<Set<String>> getActivityClustering(SPD spd) {
		List<Set<String>> labelSetList = new ArrayList<>();
		for (SPDNode node: spd.getNodes()) {
			Set<String> activitySet = ClusteringUtils.getActivitySet(node);
			labelSetList.add(activitySet);
		}		
		return labelSetList;
	}	
}
