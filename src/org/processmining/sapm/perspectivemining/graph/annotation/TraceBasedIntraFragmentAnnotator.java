package org.processmining.sapm.perspectivemining.graph.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.processmining.sapm.perspectivemining.graph.model.EventRow;
import org.processmining.sapm.perspectivemining.graph.model.Fragment;
import org.processmining.sapm.perspectivemining.graph.model.ObjectUtils;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessInstance;
import org.processmining.sapm.perspectivemining.graph.model.ProcessObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;

public class TraceBasedIntraFragmentAnnotator extends TraceBasedAnnotator {
	
	/*
	 * Node: the number of occurrences in a window
	 * Edge: the number of intra-fragment relations in a window
	 * Normalization: by the number of fragments in a window
	 */
	public void annotateFromObservation(PerspectiveGraph pGraph, ProcessInstance instance) throws Exception {
		Map<Node, Integer> nodeObservations = new HashMap<Node, Integer>();
		Map<Edge, Integer> edgeObservations = new HashMap<Edge, Integer>();
		
		PerspectiveSettingObject pConfig = pGraph.getSetting();
		
		for (Fragment fragment : instance.getAbstractFragments()) {
			List<Node> fragmentNodes = new ArrayList<>();
			for (EventRow event : fragment) {
				ProcessObject object1 = ObjectUtils.project(pConfig.getNodeSchema1(), event);
				Node node1 = pGraph.getNode(object1.getValueString());
				if (node1 ==  null) throw new Exception("Cannot find node with Id = " + object1.getValueString() + 
														" on the graph for annotation");
				fragmentNodes.add(node1);
				if (nodeObservations.containsKey(node1)) {
					nodeObservations.put(node1, nodeObservations.get(node1) + 1);
				}
				else {
					nodeObservations.put(node1, 1);
				}
				
				if (!pConfig.isNode1Node2SameSchema()) {
					ProcessObject object2 = ObjectUtils.project(pConfig.getNodeSchema2(), event);
					
					Node node2 = pGraph.getNode(object2.getValueString()); 
					if (node2 ==  null) throw new Exception("Cannot find node with Id = " + object2.getValueString() + 
							" on the graph for annotation");
					
					fragmentNodes.add(node2);
					if (nodeObservations.containsKey(node2)) {
						nodeObservations.put(node2, nodeObservations.get(node2) + 1);
					}
					else {
						nodeObservations.put(node2, 1);
					}
				}
			}
			
			for (Node node1 : fragmentNodes) {
				for (Node node2 : fragmentNodes) {
					if (node1 == node2) {
						continue;
					}

					Edge edge = pGraph.getEdge(node1, node2);
					if (edge ==  null) throw new Exception("Cannot find edge: " + node1.getId() + " - " + node2.getId() +  
							" on the graph for annotation");

					if (!edgeObservations.containsKey(edge)) {
						edgeObservations.put(edge, 1);
					}
					else {
						edgeObservations.put(edge, edgeObservations.get(edge) + 1);
					}
				}
			}
		}
		
		// Just call once for one instance
		this.updateAnnotationsFromObservation(pGraph, nodeObservations, edgeObservations, null);
		
	}

}
