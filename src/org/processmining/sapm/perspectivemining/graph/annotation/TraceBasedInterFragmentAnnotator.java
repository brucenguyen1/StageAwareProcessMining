package org.processmining.sapm.perspectivemining.graph.annotation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.processmining.sapm.perspectivemining.graph.model.AbstractFragment;
import org.processmining.sapm.perspectivemining.graph.model.EventRow;
import org.processmining.sapm.perspectivemining.graph.model.ObjectUtils;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessInstance;
import org.processmining.sapm.perspectivemining.graph.model.ProcessObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;

public class TraceBasedInterFragmentAnnotator extends TraceBasedAnnotator {
	
	/*
	 * Node: the number of occurrences in a window
	 * Edge: the number of intra-fragment relations in a window
	 * Normalization: by the number of fragments in a window
	 */
	public void annotateFromObservation(PerspectiveGraph pGraph, ProcessInstance instance) throws Exception {
		Map<Node, Integer> nodeObservations = new HashMap<Node, Integer>();
		Map<Edge, Integer> edgeObservations = new HashMap<Edge, Integer>();
		Map<Edge, DescriptiveStatistics> edgeTimeObservations = new HashMap<>();
		
		PerspectiveSettingObject pConfig = pGraph.getSetting();
		
		int i = 0;
		while ((i+1) < instance.getAbstractFragments().size()) {
			// node1 and node2 are consecutive
			EventRow sourceEvent = this.selectNode1Event(instance.getAbstractFragments().get(i));
			ProcessObject object1 = ObjectUtils.project(pConfig.getNodeSchema1(), sourceEvent);
 			
			Node node1 = pGraph.getNode(object1.getValueString());
			if (node1 ==  null) throw new Exception("Cannot find node with Id = " + object1.getValueString() + 
					" on the graph for annotation");

			if (nodeObservations.containsKey(node1)) {
				nodeObservations.put(node1, nodeObservations.get(node1) + 1);
			}
			else {
				nodeObservations.put(node1, 1);
			}
			
 			EventRow targetEvent = this.selectNode2Event(instance.getAbstractFragments().get(i+1));
 			ProcessObject object2 = ObjectUtils.project(pConfig.getNodeSchema2(), targetEvent);
 			
 			Node node2 = pGraph.getNode(object2.getValueString());
 			if (node2 ==  null) throw new Exception("Cannot find node with Id = " + object2.getValueString() + 
					" on the graph for annotation");
 			
			if (nodeObservations.containsKey(node2)) {
				nodeObservations.put(node2, nodeObservations.get(node2) + 1);
			}
			else {
				nodeObservations.put(node2, 1);
			}
 			
 			Edge edge = pGraph.getEdge(node1, node2);
 			if (edge ==  null) throw new Exception("Cannot find edge: " + node1.getId() + " -> " + node2.getId() +  
					" on the graph for annotation");
			
			// Edge frequency
			if (edgeObservations.containsKey(edge)) {
				edgeObservations.put(edge, edgeObservations.get(edge) + 1);
			}
			else {
				edgeObservations.put(edge, 1);
			}
			
			// Edge duration
			if (!edgeTimeObservations.containsKey(edge)) {
				edgeTimeObservations.put(edge, new DescriptiveStatistics());
			}
			edgeTimeObservations.get(edge).addValue((double)this.calculateDuration(sourceEvent, targetEvent));
			
			i++;
 		}
					
		this.updateAnnotationsFromObservation(pGraph, nodeObservations, edgeObservations, edgeTimeObservations);
	}
	
	@Override
	public EventRow selectNode1Event(AbstractFragment fragment) {
		return fragment.getEndEvent();
	}
	
	@Override
	public EventRow selectNode2Event(AbstractFragment fragment) {
		return fragment.getStartEvent();
	}
	


}
