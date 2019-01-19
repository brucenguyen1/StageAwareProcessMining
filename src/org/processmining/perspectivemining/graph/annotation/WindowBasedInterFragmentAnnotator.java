package org.processmining.perspectivemining.graph.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.processmining.perspectivemining.graph.model.AbstractFragment;
import org.processmining.perspectivemining.graph.model.EventRow;
import org.processmining.perspectivemining.graph.model.ObjectUtils;
import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.perspectivemining.graph.model.ProcessInstance;
import org.processmining.perspectivemining.graph.model.ProcessObject;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;

public class WindowBasedInterFragmentAnnotator extends WindowBasedAnnotator {
	
	/*
	 * Node weight (frequency): the number of occurrences in an inter-fragment relation within the window
	 * There is no time-based node weight
	 * Edge weight (frequency): the number of inter-fragment relations in a window
	 * Edge weight (time-based): the duration between two connecting events between fragments
	 * Normalization: by the number of fragments in a window
	 */
	@Override
	public void annotateFromObservation(PerspectiveGraph pGraph, org.joda.time.Interval samplingWindow, 
															ProcessAbstraction abstraction) throws Exception {
		Map<Node, Integer> nodeObservations = new HashMap<Node, Integer>();
		Map<Edge, Integer> edgeObservations = new HashMap<Edge, Integer>(); 
		Map<Edge, DescriptiveStatistics> edgeTimeObservations = new HashMap<>();
		
		PerspectiveSettingObject pConfig = pGraph.getSetting(); 
		
		for (ProcessInstance instance : abstraction.getProcessInstances()) {
			List<AbstractFragment> windowFragments = instance.getSubFragmentsInWindow(samplingWindow);
			if (windowFragments.size() > 1) { //can only have edges if there are two fragments or more
				int i = 0;
				while ((i+1) < windowFragments.size()) {
					// node1 and node2 are consecutive
					EventRow sourceEvent = this.selectNode1Event(windowFragments.get(i));
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
					
		 			EventRow targetEvent = this.selectNode2Event(windowFragments.get(i+1));
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
				
			}
			
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
