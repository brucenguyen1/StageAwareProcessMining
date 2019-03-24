package org.processmining.sapm.processdiscovery.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class PetriNetUtils {
   public static List<Place> getPlaceSuccessors(Petrinet net, Transition transition) {
        List<Place> predecessors = new ArrayList<Place>();
        for (Object node : getSuccessors(net, transition)) {
            predecessors.add((Place) node);
        }
        return predecessors;
    }

   public static List<Transition> getTransitionSuccessors(Petrinet net, Place place) {
        List<Transition> predecessors = new ArrayList<Transition>();
        for (Object node : getSuccessors(net, place)) {
            predecessors.add((Transition) node);
        }
        return predecessors;
    }
   
   public static List<Transition> getVisibleTransitionSuccessors(Petrinet net, Transition tran) {
       List<Transition> visibleSuccessors = new ArrayList<Transition>();
       Set<Object> visited = new HashSet<>();
       List<Object> tobevisited = getSuccessors(net, tran);
       for (int i=0;i<tobevisited.size();i++) {
    	   Object node = tobevisited.get(i);
    	   visited.add(node);
           if (node instanceof Transition) { 
        	   if (((Transition)node).isInvisible()) {
        		   for (Object nextNode : getSuccessors(net,(PetrinetNode)node)) {
        			   if (!visited.contains(nextNode)) tobevisited.add(nextNode);
        		   }
        	   }
        	   else {
        		   visibleSuccessors.add((Transition)node);
        	   }
           }
           else {
        	   for (Object nextNode : getSuccessors(net,(PetrinetNode)node)) {
    			   if (!visited.contains(nextNode)) tobevisited.add(nextNode);
    		   }
           }
       }
       return visibleSuccessors;
   }

   public static List<Object> getSuccessors(Petrinet net, PetrinetNode node) {
        List<Object> successors = new ArrayList();
        DirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> graph = (DirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>) net.getGraph();
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : graph.getEdges()) {
            if (edge.getSource().equals(node)) {
                successors.add(edge.getTarget());
            }
        }
        return successors;
    }

   public static List<Place> getPlacePredecessors(Petrinet net, Transition transition) {
        List<Place> predecessors = new ArrayList<Place>();
        for (Object node : getPredecessors(net, transition)) {
            predecessors.add((Place) node);
        }
        return predecessors;
    }

   public static List<Transition> getTransitionPredecessors(Petrinet net, Place place) {
        List<Transition> predecessors = new ArrayList<Transition>();
        for (Object node : getPredecessors(net, place)) {
            predecessors.add((Transition) node);
        }
        return predecessors;
    }

   public static List getPredecessors(Petrinet net, PetrinetNode node) {
        List successors = new ArrayList();
        DirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> graph = (DirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>) net.getGraph();
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : graph.getEdges()) {
            if (edge.getTarget().equals(node)) {
                successors.add(edge.getSource());
            }
        }
        return successors;
    }
   
   /**
    * Search for the final marking assuming that the model
    * has a special transition labelled "end". This is usually 
    * an artificial event added to the end of every traces in the log
    * to make it easier to locate final marking in this way.
    * @param net
    * @return
    */
   public static Marking findFinalMarking(Petrinet net) {
		Marking m = new Marking();
		for (Transition t : net.getTransitions()) {
			if (t.getLabel().toLowerCase().contains("endevent")) {
				for (Place p : net.getPlaces()) {
					Arc a = net.getArc(t, p);
					if (a != null) {
						m.add(p);
						return m;
					}
				}
			}
		}
		return m;
	}
   
   
   /**
    * Turn the artificial start and end transitions in the net to hidden,
    * including change their name to empty
    * Assume that they are labelled as "startevent" and "endevent"
    * They are usually added to event logs as artificial events.
    * @param net
    */
   public static void invisibleStartEndActivities(Petrinet net) {
		for (Transition t : net.getTransitions()) {
			if (t.getLabel().toLowerCase().equals("startevent") ||
				t.getLabel().toLowerCase().equals("endevent")) {
				t.setInvisible(true);
			}
		}
   }
   
   public static void removeStartEndActivities(Petrinet net) {
	   Place removedPlace1=null, removedPlace2 = null;
	   Transition removedTran1=null, removedTran2=null;
	   
	   for (Transition t : net.getTransitions()) {
		   if (t.getLabel().toLowerCase().equals("startevent")) {
			   Place startPlace = (Place)net.getInEdges(t).iterator().next().getSource(); //only one
			   Place successivePlace = (Place)net.getOutEdges(t).iterator().next().getTarget(); //only one
			   Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> iterator = net.getOutEdges(successivePlace).iterator();
			   while (iterator.hasNext()) {
				   Transition successiveTran = (Transition)iterator.next().getTarget();
				   net.addArc(startPlace, successiveTran);
			   }
			   removedPlace1 = successivePlace;
			   removedTran1 = t;
		   }
		   else if (t.getLabel().toLowerCase().equals("endevent")) {
			   Place endPlace = (Place)net.getOutEdges(t).iterator().next().getTarget(); //only one
			   Place precedingPlace = (Place)net.getInEdges(t).iterator().next().getSource(); // only one
			   Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> iterator = net.getInEdges(precedingPlace).iterator();
			   while (iterator.hasNext()) {
				   Transition precedingTran = (Transition)iterator.next().getSource();
				   net.addArc(precedingTran, endPlace);
			   }
			   removedPlace2 = precedingPlace;
			   removedTran2 = t;
		   }
	   }
	   
	   net.removePlace(removedPlace1);
	   net.removePlace(removedPlace2);
	   net.removeTransition(removedTran1);
	   net.removeTransition(removedTran2);
   }
   
   /*
    * Convert from a petri net to a workflow net by making the petri net 
    * only has a single input place and a single output place
    * Assume that every place in a marking contains only ONE token
    * Add a starting place connecting to a hidden transition connecting to all places in the initialMarking
    * For each finalMarking, add a hidden transition connected from all places in the marking, then 
    * connect the new transition to a new ending place. Do the same for other finalMarking but use the 
    * same ending place.
    * The input Petri Net is modified to become a Workflow Net
    * Return an array: 	1st element: the new initial marking
    * 					2nd element: the new final marking
    */
   public static List<Marking> convertToWorkflowNet(Petrinet net, Marking initialMarking, Set<Marking> finalMarkings) {
	   Marking newInitialMarking = null, newFinalMarking = null;   
	   
	   if (initialMarking.size() > 1) {
		   Set<Place> initialM = initialMarking.baseSet();
		   Place startingPlace = net.addPlace("source 0");
		   newInitialMarking = new Marking(Arrays.asList(startingPlace));
		   Transition startingTran = net.addTransition("tau from " + startingPlace.getLabel());
		   startingTran.setInvisible(true);
		   net.addArc(startingPlace, startingTran);
		   for (Place p : initialM) {
			   net.addArc(startingTran, p);
		   }
	   }
	   else {
		   newInitialMarking = initialMarking;
	   }
	   
	   if (finalMarkings.size() > 1 || finalMarkings.iterator().next().size() > 1) {
		   Place endingPlace = net.addPlace("sink 0");
		   int count = 1;
		   for (Marking finalMarking: finalMarkings) {
			   Transition endingTran = net.addTransition("tau " + count + " to " + endingPlace.getLabel());
			   endingTran.setInvisible(true);
			   count++;
			   net.addArc(endingTran, endingPlace);
			   for (Place p : finalMarking) {
				   net.addArc(p, endingTran);
			   }
		   }	
		   newFinalMarking = new Marking(Arrays.asList(endingPlace));
	   }
	   else {
		   newFinalMarking = finalMarkings.iterator().next();
	   }
	   
	   return Arrays.asList(newInitialMarking, newFinalMarking);
   }
}
