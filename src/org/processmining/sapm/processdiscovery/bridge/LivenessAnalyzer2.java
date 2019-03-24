/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2008 TU/e Eindhoven * and is licensed under the * LGPL
 * License, Version 1.0 * by Eindhoven University of Technology * Department of
 * Information Systems * http://www.processmining.org * *
 ***********************************************************/

package org.processmining.sapm.processdiscovery.bridge;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.AbstractSemanticConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.LivenessInfoConnection;
import org.processmining.models.connections.petrinets.behavioral.NonLiveTransitionsConnection;
import org.processmining.models.connections.petrinets.behavioral.StateSpaceConnection;
import org.processmining.models.connections.petrinets.structural.AbstractStructuralAnalysisInformationConnection;
import org.processmining.models.connections.petrinets.structural.FreeChoiceInfoConnection;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation.UnDetBool;
import org.processmining.models.graphbased.directed.petrinet.analysis.NonLiveTransitionsSet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.InhibitorNetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.ResetInhibitorNetSemantics;
import org.processmining.models.semantics.petrinet.ResetNetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.petrinet.behavioralanalysis.AbstractLivenessAnalyzer;
import org.processmining.plugins.petrinet.structuralanalysis.FreeChoiceAnalyzer;

/**
 * 
 * @author arya
 * @modifier bruce: this class is used to avoid Siphons and Traps which run too long
 * Force to run the liveness check based on reachability graph 
 * This class is copied from org.processmining.plugins.petrinet.behavioralanalysis.LivenessAnalyzer
 */
public class LivenessAnalyzer2 extends AbstractLivenessAnalyzer {

	private Object[] finalizeResult(PluginContext context, PetrinetGraph net, Marking state) {
		context.addConnection(new LivenessInfoConnection(net, state, semantics, info));
		context.getFutureResult(0).setLabel("Liveness Analysis of " + net.getLabel());
		context.getFutureResult(1).setLabel("Non-Live Transitions of " + net.getLabel());

		if (nonLiveTrans == null) {
			// means that the net may not be live, reachability graph cannot be constructed, and non live transition cannot be determined
			nonLiveTrans = new NonLiveTransitionsSet();
		}

		context.addConnection(new NonLiveTransitionsConnection(net, nonLiveTrans, state, semantics));

		return new Object[] { info, nonLiveTrans };

	}

	/**
	 * Variant of net and marking
	 */
	// variant with Petrinet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, Petrinet net, Marking state)
			throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, PetrinetSemanticsFactory
				.regularPetrinetSemantics(Petrinet.class));

		return finalizeResult(context, net, state);
	}

	// variant with InhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, InhibitorNet net, Marking state)
			throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, PetrinetSemanticsFactory
				.regularInhibitorNetSemantics(InhibitorNet.class));
		return finalizeResult(context, net, state);
	}

	// variant with ResetNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetNet net, Marking state)
			throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, PetrinetSemanticsFactory
				.regularResetNetSemantics(ResetNet.class));
		return finalizeResult(context, net, state);
	}

	// variant with ResetInhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetInhibitorNet net, Marking state)
			throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, PetrinetSemanticsFactory
				.regularResetInhibitorNetSemantics(ResetInhibitorNet.class));
		return finalizeResult(context, net, state);
	}

	/**
	 * Variant of net, marking, and semantics
	 */
	// variant with Petrinet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 3 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, Petrinet net, Marking state,
			PetrinetSemantics semantics) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, semantics);
		return finalizeResult(context, net, state);
	}

	// variant with InhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 3 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, InhibitorNet net, Marking state,
			InhibitorNetSemantics semantics) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, semantics);
		return finalizeResult(context, net, state);
	}

	// variant with ResetNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 3 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetNet net, Marking state,
			ResetNetSemantics semantics) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, semantics);
		return finalizeResult(context, net, state);
	}

	// variant with ResetInhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 3 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetInhibitorNet net, Marking state,
			ResetInhibitorNetSemantics semantics) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, null, semantics);
		return finalizeResult(context, net, state);
	}

	/**
	 * Variant of net, marking, and reachability graph
	 */
	// variant with Petrinet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 2 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, Petrinet net, Marking state,
			ReachabilityGraph reachabilityGraph) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, reachabilityGraph, null);
		return finalizeResult(context, net, state);
	}

	// variant with InhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 2 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, InhibitorNet net, Marking state,
			ReachabilityGraph reachabilityGraph) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, reachabilityGraph, null);
		return finalizeResult(context, net, state);
	}

	// variant with ResetNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 2 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetNet net, Marking state,
			ReachabilityGraph reachabilityGraph) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, reachabilityGraph, null);
		return finalizeResult(context, net, state);
	}

	// variant with ResetInhibitorNet and marking
	@PluginVariant(variantLabel = "Analyze Liveness", requiredParameterLabels = { 0, 1, 2 })
	public Object[] analyzeLivenessPetriNet(PluginContext context, ResetInhibitorNet net, Marking state,
			ReachabilityGraph reachabilityGraph) throws ConnectionCannotBeObtained {
		analyzeLivenessPetriNetPrivate(context, net, state, reachabilityGraph, null);
		return finalizeResult(context, net, state);
	}
	
	@Override
	protected void analyzeLivenessPetriNetPrivate(PluginContext context, PetrinetGraph net, Marking state,
			ReachabilityGraph reachabilityGraph, Semantics<Marking, Transition> semantics, Marking... finalMarkings)
			throws ConnectionCannotBeObtained {
		this.reachabilityGraph = reachabilityGraph;
		this.semantics = semantics;
		this.finalMarkings = finalMarkings;
		assert ((reachabilityGraph == null) || (semantics == null));

		// check connectivity between marking and net
		context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net, state);

		// check connectivity between reachability graph and the net
		if (reachabilityGraph != null) {
			StateSpaceConnection connection = context.getConnectionManager().getFirstConnection(
					StateSpaceConnection.class, context, net, state, reachabilityGraph);
			semantics = connection.getObjectWithRole(AbstractSemanticConnection.SEMANTICS);
		}

		// check for available freechoice property
		boolean isFreeChoice = false;
		boolean infoFound = false;
		ConnectionManager cm = context.getConnectionManager();
		try {
			for (FreeChoiceInfoConnection analysis : cm.getConnections(FreeChoiceInfoConnection.class, context, net)) {
				NetAnalysisInformation.FREECHOICE info = (NetAnalysisInformation.FREECHOICE) analysis
						.getObjectWithRole(AbstractStructuralAnalysisInformationConnection.NETANALYSISINFORMATION);

				isFreeChoice = info.getValue().equals(UnDetBool.TRUE);
				infoFound = true;
				break;
			}
		} catch (ConnectionCannotBeObtained e) {
			// No connections available
		}

		if (!infoFound) {
			// check free-choice property first
			isFreeChoice = FreeChoiceAnalyzer.getNXFCClusters(net).isEmpty();
		}

		// check in NetAnalysisInformation about Free Choice property
		// Bruce: comment this out since it runs too long. 
		// Liveness will be checked based on reachability graph
//		if (isFreeChoice) {
//			// continue with liveness identification for Free Choice Net
//			SiphonSet siphonSet;
//			try {
//				siphonSet = context.tryToFindOrConstructFirstObject(SiphonSet.class, SiphonConnection.class,
//						AbstractComponentSetConnection.COMPONENTSET, net);
//			} catch (Exception e) {
//				siphonSet = null;
//			}
//
//			TrapSet trapSet;
//			try {
//				trapSet = context.tryToFindOrConstructFirstObject(TrapSet.class, TrapConnection.class,
//						AbstractComponentSetConnection.COMPONENTSET, net);
//			} catch (Exception e) {
//				trapSet = null;
//			}
//
//			if ((siphonSet != null) && (trapSet != null)) {
//
//				// analyze using siphon and traps
//				info = analyzeLivenessOnFreeChoiceNet(state, siphonSet, trapSet);
//				if (info.getValue().equals(UnDetBool.TRUE)) {
//					// add connection
//
//					nonLiveTrans = new NonLiveTransitionsSet();
//					nonLiveSequences = new NonLiveSequences();
//
//					return;
//				}
//				// Else fall through: the system is not live, and we need to compute the non-live
//				// transitions.
//			}
//		}

		// if an execution reaches this part, liveness needs to be decided using reachability graph
		// find or create a new reachability graph
		if (reachabilityGraph == null) {
			try {
				reachabilityGraph = context.tryToFindOrConstructFirstObject(ReachabilityGraph.class,
						StateSpaceConnection.class, StateSpaceConnection.STATEPACE, net, state, semantics);
				if (reachabilityGraph == null) {
					context.log("Liveness cannot be determined due to infinite state space");
				}
				analyzeLivenessOnNonFreeChoicePetriNet(net, state, reachabilityGraph, finalMarkings);
			} catch (ConnectionCannotBeObtained e) {
				// if the reachability graph cannot be constructed, liveness can not be decided
				info = new NetAnalysisInformation.LIVENESS();
				info.setValue(UnDetBool.UNDETERMINED);
				context.log("Statespace construction failed. Maximum number of states reached");
			} catch (Exception exc) {
				context.log("Exception happened. Maximum number of states reached");
			}
		}

	}

}
