package org.processmining.spm.processdiscovery.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.spm.processdiscovery.converters.PetriNetToBPMNConverterPlugin;
import org.processmining.spm.processdiscovery.result.PetrinetResult;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;

/**
 * Standard BPMN transformations
 * 
 *
 * @author Anna Kalenkova
 * May 20, 2014
 */
public class BPMNUtils {

	public static final String EMPTY = "Empty";
	
	public static Flow getFlow(BPMNNode n1, BPMNNode n2) {
		BPMNDiagram d = (BPMNDiagram)n1.getGraph();
		for (BPMNEdge e : d.getOutEdges(n1)) {
			if (e.getTarget() == (DirectedGraphNode)n2) {
				return (Flow)e;
			}
		}
		return null;
	}
	
	public static boolean isXORSplit(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.DATABASED) && d.getInEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isXORJoin(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.DATABASED) && d.getOutEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isANDSplit(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.PARALLEL) && d.getInEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isANDJoin(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.PARALLEL) && d.getOutEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isORSplit(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.INCLUSIVE) && d.getInEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isORJoin(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Gateway) {
			Gateway gw = (Gateway)n;
			if (gw.getGatewayType().equals(GatewayType.INCLUSIVE) && d.getOutEdges(n).size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isActivity(BPMNNode n) {
		BPMNDiagram d = (BPMNDiagram)n.getGraph();
		if (n instanceof Activity) {
			return true;
		}
		else {
			return false;
		}
	}
	
    /**
     * Convert from AND-split to OR split, AND-join to OR-join
     * @param and: the AND gateway
     * @return: the OR gateway
     * @throws Exception 
     */
    public static BPMNNode convertANDToOR(BPMNNode and) throws Exception {
    	if (and instanceof Gateway) {
    		Gateway gw = (Gateway)and;
    		if (gw.getGatewayType().equals(GatewayType.PARALLEL)) {
    			BPMNDiagram d = (BPMNDiagram)and.getGraph();
    			Gateway newGw = d.addGateway("OR", GatewayType.INCLUSIVE);
    			
    			for (BPMNEdge f : d.getInEdges(gw)) {
    				d.addFlow((BPMNNode)f.getSource(), newGw, "");
    			}
    			for (BPMNEdge f : d.getOutEdges(gw)) {
    				d.addFlow(newGw, (BPMNNode)f.getTarget(), "");
    			}
    			d.removeGateway(gw);
    			return newGw;
    		}
    	}
    	throw new Exception("The input node is not a valid AND gateway!");
    }
	
	// Assume that there is only one start event
	public static Event getStartEvent(BPMNDiagram diagram) {
		for (Event e : diagram.getEvents()) {
			if (e.getEventType().equals(EventType.START)) {
				return e;
			}
		}
		return null;
	}
	
	// Assume that there is only one end event
	public static Event getEndEvent(BPMNDiagram diagram) {
		for (Event e : diagram.getEvents()) {
			if (e.getEventType().equals(EventType.END)) {
				return e;
			}
		}
		return null;
	}	
	
	// Assume that there is a single activity connecting to the start event
	public static BPMNNode getFirstAct(BPMNDiagram d) {
		Event dStart = BPMNUtils.getStartEvent(d);
		for (Flow f : d.getFlows()) {
			if (f.getSource() == dStart) {
				return f.getTarget();
			}
		}
		return null;
	}
	
	// Assume that there is a single activity connecting to the end event
	public static BPMNNode getLastAct(BPMNDiagram d) {
		Event dEnd = BPMNUtils.getEndEvent(d);
		for (Flow f : d.getFlows()) {
			if (f.getTarget() == dEnd) {
				return f.getSource();
			}
		}
		return null;
	}
	
	public static BPMNNode getNodeByName(BPMNDiagram d, String nodeName) {
		for (BPMNNode n : d.getNodes()) {
			if (n.getLabel().toLowerCase().equals(nodeName.toLowerCase())) {
				return n;
			}
		}
		return null;
	}
	
	public static boolean isConnected(BPMNNode n1, BPMNNode n2) {
		BPMNDiagram d = (BPMNDiagram)n1.getGraph();
		if (d.getOutEdges(n1).iterator().next().getTarget() == n2) {
			return true;
		}
		else if (BPMNUtils.isXORSplit((BPMNNode)d.getOutEdges(n1).iterator().next().getTarget())) {
			BPMNNode xorSplit = (BPMNNode)d.getOutEdges(n1).iterator().next().getTarget();
			for (BPMNEdge e : d.getOutEdges(xorSplit)) {
				if (e.getTarget() == n2) {
					return true;
				}
			}
		}
		else if (BPMNUtils.isXORJoin((BPMNNode)d.getOutEdges(n1).iterator().next().getTarget())) {
			BPMNNode xorJoin = (BPMNNode)d.getOutEdges(n1).iterator().next().getTarget();
			if (d.getOutEdges(xorJoin).iterator().next().getTarget() == n2) {
				return true;
			}
		}
		else if (BPMNUtils.isXORJoin((BPMNNode)d.getOutEdges(n1).iterator().next().getTarget()) &&
				BPMNUtils.isXORSplit((BPMNNode)d.getInEdges(n2).iterator().next().getSource())) {
			BPMNNode xorJoin = (BPMNNode)d.getOutEdges(n1).iterator().next().getTarget();
			BPMNNode xorSplit = (BPMNNode)d.getInEdges(n2).iterator().next().getSource();
			if (d.getOutEdges(xorJoin).iterator().next().getTarget() == xorSplit) {
				return true;
			}
		}
		return false;
	}
	
	public static BPMNDiagram getContainingDiagram(List<BPMNDiagram> diagrams, String nodeName) {
		for (int i=0;i<diagrams.size();i++) {
			BPMNNode n = BPMNUtils.getNodeByName(diagrams.get(i), nodeName);
			if (n!=null) {
				return diagrams.get(i);
			}
		}
		return null;
	}
	
//	public static List<Flow> incomingFlows(BPMNNode n, BPMNDiagram d) {
//		List<Flow> result = new ArrayList<>();
//		for (Flow f : d.getFlows()) {
//			if (f.getTarget() == n) {
//				result.add(f);
//			}
//		}
//		return result;
//	}
//	
//	public static List<Flow> outgoingFlows(BPMNNode n, BPMNDiagram d) {
//		List<Flow> result = new ArrayList<>();
//		for (Flow f : d.getFlows()) {
//			if (f.getSource() == n) {
//				result.add(f);
//			}
//		}
//		return result;
//	}
	
	/**
	 * Simplify BPMN diagram
	 * 
	 * @param conversionMap
	 * @param diagram
	 */
	public static void simplifyBPMNDiagram(Map<String, Activity> conversionMap, BPMNDiagram diagram) {
        if (diagram == null) throw new IllegalArgumentException("'conversionMap' is null");

        removeSilentActivities(conversionMap, diagram);
        reduceGateways(diagram);
		mergeActivitiesAndGateways(diagram);
		removeSuperfluousGateways(diagram);
	}
	
	public static void simplifyBPMNDiagram2(BPMNDiagram diagram) {
        if (diagram == null) throw new IllegalArgumentException("'conversionMap' is null");

        reduceGateways(diagram);
		mergeActivitiesAndGateways(diagram);
		removeSuperfluousGateways(diagram);
	}
	
	/**
	 * Remove superfluous gateways
	 * 
	 * @param diagram
	 */
	public static void removeSuperfluousGateways(BPMNDiagram diagram) {
		Set<Gateway> gatewaysToRemove = new HashSet<Gateway>();
		do {
			gatewaysToRemove.clear();
			for (Gateway gateway : diagram.getGateways()) {
				if ((diagram.getInEdges(gateway).size() == 1) && (diagram.getOutEdges(gateway).size() == 1)) {
					BPMNNode inNode = diagram.getInEdges(gateway).iterator().next().getSource();
					BPMNNode outNode = diagram.getOutEdges(gateway).iterator().next().getTarget();
					gatewaysToRemove.add(gateway);
					diagram.addFlow(inNode, outNode, "");
				}
			}
			for (Gateway gateway : gatewaysToRemove) {
				diagram.removeGateway(gateway);
			}
		} 
		while (!gatewaysToRemove.isEmpty());
		 
	}
	
	/**
	 * Reduce gateways
	 * 
	 * @param diagram
	 */
	public static void reduceGateways(BPMNDiagram diagram) {
		boolean diagramChanged;
		do {
			diagramChanged = false;
			Gateway gatewayToRemove = null;
			for (Gateway gateway : diagram.getGateways()) {
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> flow : diagram.getOutEdges(gateway)) {
					if (flow.getTarget() instanceof Gateway) {
						Gateway followingGateway = (Gateway) flow.getTarget();
						if ((diagram.getOutEdges(gateway).size() == 1)
								|| (diagram.getInEdges(followingGateway).size() == 1)) {
							if (gateway.getGatewayType().equals(followingGateway.getGatewayType())) {
								Collection<BPMNNode> followingNodes = new HashSet<BPMNNode>();
								for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> outFlow : diagram
										.getOutEdges(followingGateway)) {
									BPMNNode followingNode = outFlow.getTarget();
									followingNodes.add(followingNode);
								}
								Collection<BPMNNode> precNodes = new HashSet<BPMNNode>();
								for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> inFlow : diagram
										.getInEdges(followingGateway)) {
									BPMNNode precNode = inFlow.getSource();
									if(!gateway.equals(precNode)) {
										precNodes.add(precNode);
									}
								}
								for (BPMNNode followingNode : followingNodes) {
									addFlow(diagram, gateway, followingNode);
								}
								for (BPMNNode precNode : precNodes) {
									addFlow(diagram, precNode, gateway);
								}
								gatewayToRemove = followingGateway;
								diagramChanged = true;
								break;
							}
						}
					}
				}
				if(diagramChanged) {
					break;
				}
			}
			if(diagramChanged) {
				diagram.removeGateway(gatewayToRemove);
			}
		} while (diagramChanged);
	}

	/**
	 * Merge activities and gateways
	 * 
	 * @param diagram
	 */
	public static void mergeActivitiesAndGateways(BPMNDiagram diagram) {
		for (Activity activity : diagram.getActivities()) {
			//if (numberOfOutgoingSequenceFlows(activity, diagram) == 1) {
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> flow : diagram.getOutEdges(activity)) {
					if (flow.getTarget() instanceof Gateway) {
						Gateway followingGateway = (Gateway) flow.getTarget();
						if (GatewayType.PARALLEL.equals(followingGateway.getGatewayType())) {
							if (diagram.getInEdges(followingGateway).size() == 1) {
								Collection<BPMNNode> followingNodes = new HashSet<BPMNNode>();
								for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> outFlow : diagram
										.getOutEdges(followingGateway)) {
									BPMNNode followingNode = outFlow.getTarget();
									followingNodes.add(followingNode);
								}
								diagram.removeGateway(followingGateway);
								for (BPMNNode followingNode : followingNodes) {
									addFlow(diagram, activity, followingNode);

								}
							}
						}
					}
				//}
			}

			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> flow : diagram.getInEdges(activity)) {
				//if (numberOfIncomingSequenceFlows(activity, diagram) == 1) {
					if (flow.getSource() instanceof Gateway) {
						Gateway precedingGateway = (Gateway) flow.getSource();
						if (GatewayType.DATABASED.equals(precedingGateway.getGatewayType())) {
							if (diagram.getOutEdges(precedingGateway).size() == 1) {
								Collection<BPMNNode> precedingNodes = new HashSet<BPMNNode>();
								for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> inFlow : diagram
										.getInEdges(precedingGateway)) {
									BPMNNode precedingNode = inFlow.getSource();
									precedingNodes.add(precedingNode);
								}
								diagram.removeGateway(precedingGateway);
								for (BPMNNode precedingNode : precedingNodes) {
									addFlow(diagram, precedingNode, activity);

								}
							}
						}
					}
				//}
			}
		}
	}
	
	
	/**
	 * Get the number of outgoing flows
	 * @param node
	 * @param diagram
	 * @return
	 */
	public static int numberOfOutgoingSequenceFlows(BPMNNode node, BPMNDiagram diagram) {
		int result = 0;
		for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> flow : diagram.getOutEdges(node)) {
			if (flow instanceof Flow) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Get the number of incoming flows
	 * @param node
	 * @param diagram
	 * @return
	 */
	public static int numberOfIncomingSequenceFlows(BPMNNode node, BPMNDiagram diagram) {
		int result = 0;
		for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> flow : diagram.getInEdges(node)) {
			if (flow instanceof Flow) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Remove silent activities
	 * 
	 * @param diagram
	 */
	private static void removeSilentActivities(Map<String, Activity> conversionMap, BPMNDiagram diagram) {
		Collection<Activity> allActivities = new HashSet<Activity>();
		allActivities.addAll(diagram.getActivities());
		for (Activity activity : allActivities) {
			if (EMPTY.equals(activity.getLabel())) {
				Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> inEdges = diagram.getInEdges(activity);
				Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> outEdges = diagram.getOutEdges(activity);
				if ((inEdges.iterator().hasNext()) && (outEdges.iterator().hasNext())) {
					BPMNNode source = inEdges.iterator().next().getSource();
					BPMNNode target = outEdges.iterator().next().getTarget();
					addFlow(diagram, source, target);
				}
				diagram.removeActivity(activity);
				
				if (conversionMap != null) {
					Set<String> idToRemove = new HashSet<String>();
					for (String id : conversionMap.keySet()) {
						if (activity == conversionMap.get(id)) {
							idToRemove.add(id);
						}
					}
					for (String id : idToRemove) {
						conversionMap.remove(id);
					}
				}
			}
		}
	}

    private static Flow addFlow(BPMNDiagram diagram, BPMNNode source, BPMNNode target) {
        for (BPMNEdge flow : diagram.getEdges())
            if (source == flow.getSource() && target == flow.getTarget()) return null;
        return diagram.addFlow(source, target, "");
    }
    
    // From Kalenkova and Dirk Farland
//    public static PetrinetResult BPMNToPetriNet_Dirk(BPMNDiagram bpmn) throws Exception {
//		BPMN2PetriNetConverter conv = new BPMN2PetriNetConverter(bpmn, new BPMN2PetriNetConverter_Configuration());
//		boolean success = conv.convert();
//		PetrinetResult petrinetRes;
//		if (success) {
//			Petrinet petrinet = conv.getPetriNet();
//			Marking initialMarking = conv.getMarking();
//			Marking finalMarking = new Marking();
//			for (Place p : petrinet.getPlaces()) {
//	            if (PetriNetUtils.getPlaceSuccessors(petrinet,p).size() == 0) {
//	                p.getAttributeMap().put(AttributeMap.LABEL, "END");
//	                finalMarking.add(p);
//	            }
//	        }
//			Set<Marking> finalMarkings = new HashSet<>();
//			finalMarkings.add(finalMarking);
//			petrinetRes = new PetrinetResult(petrinet,initialMarking,finalMarkings);
//		} else {
//			throw new Exception("Errors when converting from BPMN to Petri Net");
//		}
//		return petrinetRes;
//    }
    
    // From Kalenkova and Dirk Farland
    public static BPMNDiagram PetriNetToBPMN_Dirk(UIPluginContext context, Petrinet net, 
    						Marking initialMarking, Marking finalMarking) throws Exception {
    	context.addConnection(new InitialMarkingConnection(net, initialMarking));
    	context.addConnection(new FinalMarkingConnection(net, finalMarking));
    	
    	//This is for the PetriNetToBPMNConverterPlugin plugin because it will search or
    	//call PNAnalysis plugin to check if the petrinet is free-choice.
    	//We have to call it early here because this is not a ProM environment. Otherwise,
    	//it will throw an exception for not finding the plugin or connection
//    	FreeChoiceAnalyzer analyzer = new FreeChoiceAnalyzer();
//    	Object[] res = analyzer.analyzeFCAndEFCProperty(context, (ResetInhibitorNet)net);
    	
    	PetriNetToBPMNConverterPlugin plugin = new PetriNetToBPMNConverterPlugin();
    	Object[] result = plugin.convert(context, net);
    	return (BPMNDiagram)result[0];
    }
    
    // From Rafaele Conforti
    public static BPMNDiagram PetriNetToBPMN_Raf(UIPluginContext context, Petrinet net, 
			Marking initialMarking, Marking finalMarking) {
    	BPMNDiagram diagram = PetriNetToBPMNConverter.convert(net, initialMarking, finalMarking, true);
    	return diagram;
    }
    
 // From Rafaele Conforti
    public static PetrinetResult BPMNToPetriNet_Raf(BPMNDiagram bpmn) {
    	Object[] result = BPMNToPetriNetConverter.convert(bpmn);
    	Set<Marking> finalMarkings = new HashSet<>();
    	finalMarkings.add((Marking)result[2]);
    	PetrinetResult petrinetRes = new PetrinetResult((Petrinet)result[0], (Marking)result[1], finalMarkings);
    	return petrinetRes;
    }
    
    /**
     * Return true if the path only contains gateways between
     * the two nodes at two ends.
     * @param path
     * @return
     */
    public static boolean isSimplePath(ArrayList<BPMNNode> path) {
    	if (path == null || path.isEmpty()) {
    		return false;
    	}
    	else if (path.size() == 2) {
    		return true;
    	}
    	else {
	    	for (int i=1;i<path.size()-1;i++) {
	    		if (BPMNUtils.isActivity(path.get(i))) {
	    			return false;
	    		}
	    	}
	    	return true;
    	}
    }
    
    /**
     * Remove artificial activities
     * The "startevent" is assumed to be the first activity connecting from the start event to others
     * The start event will be connected to the next node after the "startevent" activity
     * The "endevent" is assumed to be the last activity connecting from others to the end event
     * The end event will be connected to the previous node before the "endevent" activity
     * @param d
     * @throws Exception 
     */
    public static void removeStartEndActivities(BPMNDiagram d) throws Exception {
    	Event startEvent = BPMNUtils.getStartEvent(d);
    	Event endEvent = BPMNUtils.getEndEvent(d);
    	BPMNNode startActivity=null, endActivity=null;
    	for (BPMNNode n : d.getActivities()) {
    		if (n.getLabel().toLowerCase().equals("startevent")) {
    			if (d.getInEdges(n).iterator().next().getSource() != startEvent) {
    				throw new Exception ("Something wrong. "
    						+ "The artificial 'startevent' activity is not connected with the start event");
    			}
    			else {
    				d.addFlow(startEvent, d.getOutEdges(n).iterator().next().getTarget(), "");
    				startActivity = n;
    			}
    		}
    		else if (n.getLabel().toLowerCase().equals("endevent")) {
    			if (d.getOutEdges(n).iterator().next().getTarget() != endEvent) {
    				throw new Exception ("Something wrong. "
    						+ "The artificial 'endevent' activity is not connected with the end event");
    			}
    			else {
    				d.addFlow(d.getInEdges(n).iterator().next().getSource(), endEvent, "");
    				endActivity = n;
    			}
    		}
    		if (startActivity != null && endActivity != null) break;
    	}
    	if (startActivity != null) d.removeNode(startActivity);
    	if (endActivity != null) d.removeNode(endActivity);
    }
}
