package org.processmining.plugins.petrinet.behavioralanalysis.woflan;

import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.collection.MultiSet;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.petrinets.structural.AbstractInvariantMarkingConnection;
import org.processmining.models.connections.petrinets.structural.NotPCoveredNodesConnection;
import org.processmining.models.connections.petrinets.structural.NotSCoveredNodesConnection;
import org.processmining.models.connections.petrinets.structural.PlaceInvariantConnection;
import org.processmining.models.connections.petrinets.structural.ShortCircuitedNetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.analysis.CoverabilitySet;
import org.processmining.models.graphbased.directed.petrinet.analysis.DeadTransitionsSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NonExtendedFreeChoiceClustersSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NonLiveSequences;
import org.processmining.models.graphbased.directed.petrinet.analysis.NonLiveTransitionsSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NotPCoveredNodesSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NotSCoveredNodesSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.PTHandles;
import org.processmining.models.graphbased.directed.petrinet.analysis.PlaceInvariantSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.SComponentSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.TPHandles;
import org.processmining.models.graphbased.directed.petrinet.analysis.UnboundedPlacesSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.UnboundedSequences;
import org.processmining.models.graphbased.directed.petrinet.analysis.WorkflowNetUtils;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.plugins.petrinet.behavioralanalysis.BoundednessAnalyzer;
import org.processmining.plugins.petrinet.behavioralanalysis.CGGenerator;
import org.processmining.plugins.petrinet.behavioralanalysis.DeadTransitionAnalyzer;
import org.processmining.plugins.petrinet.behavioralanalysis.LivenessAnalyzerWithSequence;
import org.processmining.plugins.petrinet.behavioralanalysis.TSGenerator;
import org.processmining.plugins.petrinet.structuralanalysis.FreeChoiceAnalyzer;
import org.processmining.plugins.petrinet.structuralanalysis.PTHandlesGenerator;
import org.processmining.plugins.petrinet.structuralanalysis.SComponentGenerator;
import org.processmining.plugins.petrinet.structuralanalysis.TPHandlesGenerator;
import org.processmining.plugins.petrinet.structuralanalysis.invariants.PlaceInvariantCalculator;
import org.processmining.sapm.processdiscovery.bridge.LivenessAnalyzer2;

/**
 * Woflan
 * 
 * Woflan diagnosis for a Petri net.
 * 
 * @author HVERBEEK
 * 
 */
@Plugin(name = "Analyze with Woflan", level = PluginLevel.PeerReviewed, categories = { PluginCategory.Analytics }, returnLabels = { "Woflan Diagnosis" }, returnTypes = { WoflanDiagnosis.class }, parameterLabels = { "Net" /*
																																	 * ,
																																	 * "Assumptions"
// Bruce: this class is copied from Woflan class in the Woflan package.
// This class does not connect to other plugins via connections which are only available in ProM
// but not available in command line.																																	 */}, userAccessible = true, help = WoflanHelp.TEXT)
public class Woflan2 {

	/**
	 * The current plug-in context.
	 */
	private PluginContext context;

	/**
	 * The connection manager of this context.
	 */
	private ConnectionManager cm;

	/**
	 * The current diagnostic results.
	 */
	private WoflanDiagnosis diagnosis;

	/**
	 * The current state of the diagnosis: to start.
	 */
	private WoflanState state = WoflanState.INIT;

	private Collection<WoflanState> assumptions = new HashSet<WoflanState>();

	/**
	 * The initial marking of the short-circuited net. Note that the net and the
	 * short-circuited net are kept by the diagnosis.
	 * 
	 * Invariant: marking == null || marking == diagnosis.shortCNet
	 */
	private Marking initialMarking;
	private Marking finalMarking;
	
	private CoverabilityGraph coverGraph = null;
	private CoverabilitySet coverSet = null;
	private PlaceInvariantSet piSet = null;
	private ReachabilityGraph reachGraph = null;

	/**
	 * Public constructor of Woflan
	 */
	public Woflan2() {
		initialMarking = null;
		finalMarking = null;
	}
	
//	/**
//	 * Bruce created. 25.03.2017
//	 */
//	public Woflan(UIPluginContext context, Petrinet net) {
//		this.context = context;
//		diagnosis = new WoflanDiagnosis(net);
//	}
//	
//	/**
//	 * Bruce created. 25.03.2017
//	 */
//	public WoflanDiagnosis getDiagnosis() {
//		return this.diagnosis;
//	}

	/**
	 * The Woflan plug-in using no assumptions.
	 * 
	 * @param context
	 *            The context of this plug-in.
	 * @param net
	 *            The net to diagnose.
	 * @return The diagnosis of the net.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws CancellationException
	 * @throws Exception
	 */
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "Woflan")
	@PluginVariant(variantLabel = "Woflan", requiredParameterLabels = { 0 })
	public WoflanDiagnosis diagnose(PluginContext context, Petrinet net) throws Exception {
		return diagnose(context, net, new HashSet<WoflanState>());
	}

	/**
	 * The Woflan plug-in using assumptions.
	 * 
	 * @param context
	 *            The context of this plug-in.
	 * @param net
	 *            The net to diagnose.
	 * @param assumptions
	 *            The assumptions to make.
	 * @return The diagnosis of the net, given the assumptions.
	 * @throws CancellationException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	//TODO: BVD: A plugin variant cannot accept parameters using generics. At runtime,
	// these generics are lost, i.e. every Collection would be OK as a parameter, such as, 
	// for example a Marking, which is a MultiSet<Place> which extends Collection<Place> 
	// A Special wrapping object has to be defined for Collection<WoflanState>	
	//	
	//	@PluginVariant(variantLabel = "Woflan", requiredParameterLabels = { 0, 1 })
	public WoflanDiagnosis diagnose(PluginContext context, Petrinet net, Collection<WoflanState> assumptions)
			throws ConnectionCannotBeObtained {
		/**
		 * Store the context and connection manager for future use.
		 */
		this.context = context;
		cm = context.getConnectionManager();
		/**
		 * Store the assumptions for future use.
		 */
		this.assumptions = assumptions;
		/**
		 * Initialize the diagnosis.
		 */
		diagnosis = null;
		/**
		 * Try whether a diagnosis already exists for this net and these
		 * assumptions.
		 */
		//Bruce: remove this: always run diagnosis
//		try {
//			for (WoflanConnection conn : cm.getConnections(WoflanConnection.class, context, net)) {
//				WoflanDiagnosis object = conn.getObjectWithRole(WoflanConnection.DIAGNOSIS);
//				if (conn.getAssumptions().equals(assumptions)) {
//					/**
//					 * Yes, it exists. Use it.
//					 */
//					diagnosis = object;
//				}
//			}
//		} catch (Exception e) {
			/**
			 * No harm done.
			 */
//		}
		
		if (diagnosis == null) {
			/**
			 * No, it does not exist. create a new one.
			 */
			diagnosis = new WoflanDiagnosis(net);
			
			/**
			 * Do the diagnosis.
			 */
			while (state != WoflanState.DONE) {
				if (context.getProgress().isCancelled()) {
					/*
					 * User has cancelled the diagnosis. Handle it.
					 */
					context.getFutureResult(0).cancel(true);
					context.log("Diagnosis has been cancelled. Good bye.");
					return null;
				}
				state = diagnose(state);
			}
			if (context.getResult() != null && context.getResult().getSize() > 0) {
				context.getFutureResult(0).setLabel("Woflan Diagnosis of net " + net.getLabel());
			}
			context.log(diagnosis.toString());
			context.addConnection(new WoflanConnection(net, diagnosis, assumptions));
		}
		return diagnosis;
	}

	/**
	 * Takes a next diagnosis step.
	 * 
	 * @param state
	 *            The current state.
	 * @return The next state.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws CancellationException
	 * @throws Exception
	 */
	public WoflanState diagnose(WoflanState state) throws ConnectionCannotBeObtained {
		switch (state) {
			case BOUNDED : {
				System.out.println("Net is bounded.");
				System.out.println("Check deadness");
				state = diagnoseDeadness();
				break;
			}
			case FREECHOICE : {
				System.out.println("Net is free-choice.");
				System.out.println("Check P-cover");
				state = diagnosePCover();
				break;
			}
			case INIT : {
				System.out.println("Start of diagnosis.");
				System.out.println("Check if this is Workflow Net");
				state = diagnoseWFNet();
				break;
			}
			case LIVE : {
				context.log("Net is live.");
				state = WoflanState.SOUND;
				break;
			}
			case NONFREECHOICE : {
				System.out.println("Net is not free-choice.");
				System.out.println("Check PT-handles");
				// Bruce: replace checking PTHandles with PCover because
				// it always goes to checking PCover for NonFreeChoice, 
				// no matter the result of checking PTHandles and TPHandles.
				// state = diagnosePTHandles();
				state = diagnosePCover();
				break;
			}
			case NOPCOVER : {
				System.out.println("Net is not covered by P-invariants.");
				System.out.println("Check Boundedness");
				state = diagnoseBoundedness();
				break;
			}
			case NOSCOVER : {
				System.out.println("Net is not covered by S-components.");
				System.out.println("Check free-choice");
				state = diagnoseFC();
				break;
			}
			case NOTDEAD : {
				System.out.println("Net contains no dead transitions.");
				prepareConnections2(); //Bruce added 26.03.2017
				System.out.println("Check Liveness");
				state = diagnoseLiveness();
				break;
			}
			case NOTWELLPTHANDLED : {
				System.out.println("Net contains PT-handles.");
				System.out.println("Check TP-Handles");
				state = diagnoseTPHandles();
				break;
			}
			case NOTWELLTPHANDLED : {
				System.out.println("Net contains TP-handles.");
				System.out.println("Check P-cover");
				state = diagnosePCover();
				break;
			}
			case PCOVER : {
				System.out.println("Net is covered by P-invariants.");
				state = WoflanState.BOUNDED;
				break;
			}
			case SCOVER : {
				System.out.println("Net is covered by S-components.");
				state = WoflanState.BOUNDED;
				break;
			}
			case SOUND : {
				System.out.println("Net is sound.");
				state = WoflanState.DONE;
				diagnosis.setSound();
				break;
			}
			case WELLPTHANDLED : {
				System.out.println("Net contains no PT-handles.");
				state = diagnoseTPHandles();
				break;
			}
			case WELLTPHANDLED : {
				System.out.println("Net contains no TP-handles.");
				state = diagnosePCover();
				break;
			}
			case WFNET : {
				System.out.println("Net is a WF-net.");
				System.out.println("Check S-Cover");
				prepareConnections1(); //Bruce added 26.03.2017
				state = diagnoseSCover();
				break;
			}
			default : {
				System.out.println("Check soundness is done!");
				state = WoflanState.DONE;
				break;
			}
		}
		return state;
	}


	/**
	 * Returns a copy of the net which has an extra transition with (1) all sink
	 * places as inputs and (2) all source places as output. If such a copy
	 * cannot be found, it is created and returned. Otherwise, an existing copy
	 * is returned.
	 */
	private void shortCircuit() {
		diagnosis.shortCNet = null;
		initialMarking = null;
		finalMarking = null;
		/**
		 * Try to find an existing copy.
		 */
		try {
			for (ShortCircuitedNetConnection conn : cm.getConnections(ShortCircuitedNetConnection.class, context,
					diagnosis.net)) {
				diagnosis.shortCNet = conn.getObjectWithRole(ShortCircuitedNetConnection.SCNET);
				finalMarking = conn.getObjectWithRole(ShortCircuitedNetConnection.FINALMARKING);

			}
			if (diagnosis.shortCNet != null) {
				for (InitialMarkingConnection conn : cm.getConnections(InitialMarkingConnection.class, context,
						diagnosis.shortCNet)) {
					initialMarking = conn.getObjectWithRole(InitialMarkingConnection.MARKING);

				}
			}
		} catch (Exception e) {
			/**
			 * No harm done, just continue.
			 */
		}
		/**
		 * If marking != null, the diagnosis.shortCNet != null.
		 */
		if (initialMarking == null) {
			/**
			 * No existing copy found. Create a copy.
			 */
			Object[] objects = WorkflowNetUtils.shortCircuit(context, diagnosis.net);
			diagnosis.shortCNet = (Petrinet) objects[0];
			initialMarking = (Marking) objects[1];
			finalMarking = (Marking) objects[2];
			/**
			 * Add the copy to the object pool.
			 */
			context.getProvidedObjectManager().createProvidedObject(
					"Short-circuited net of " + diagnosis.net.getLabel(), diagnosis.shortCNet, context);
			/**
			 * Add connection from the original net to this copy to the
			 * connection pool. As a result, this copy might be found next time.
			 */
			context.addConnection(new ShortCircuitedNetConnection(diagnosis.net, diagnosis.shortCNet, finalMarking));
			context.getProvidedObjectManager().createProvidedObject(
					"Initial marking of " + diagnosis.shortCNet.getLabel(), initialMarking, context);
			context.addConnection(new InitialMarkingConnection(diagnosis.shortCNet, initialMarking));
		}
	}

	/**
	 * Checks whether the net is a WF-net, using the assumptions.
	 * 
	 * @return WFNET if the net is a WF-net, NOWFNET if the net is not a WF-net,
	 *         DONE otherwise.
	 * @throws Exception
	 */
	public WoflanState diagnoseWFNet() {
		/**
		 * First check the assumptions.
		 */
		if (assumptions.contains(WoflanState.WFNET)) {
			return WoflanState.WFNET;
		} else if (assumptions.contains(WoflanState.NOWFNET)) {
			return WoflanState.NOWFNET;
		}
		/**
		 * No assumptions on this. Proceed.
		 */
		WoflanState state = WoflanState.DONE;
		if (WorkflowNetUtils.isValidWFNet(diagnosis.net)) {
			/**
			 * The net is a workflow net. Short-circuit it.
			 */
			shortCircuit();
			state = WoflanState.WFNET;
		} else {
			/**
			 * The net is not a workflow net. Diagnose the reason.
			 */
			SortedSet<Place> sourcePlaces = WorkflowNetUtils.getSourcePlaces(diagnosis.net);
			if (sourcePlaces.size() != 1) {
				diagnosis.setSourcePlaces(context, sourcePlaces);
			}
			SortedSet<Place> sinkPlaces = WorkflowNetUtils.getSinkPlaces(diagnosis.net);
			if (sinkPlaces.size() != 1) {
				diagnosis.setSinkPlaces(context, sinkPlaces);
			}
			if ((sourcePlaces.size() == 1) && (sinkPlaces.size() == 1)) {
				SortedSet<PetrinetNode> nodes = WorkflowNetUtils.getUnconnectedNodes(diagnosis.net);
				if (nodes.size() != 0) {
					diagnosis.setUnconnectedNodes(context, nodes);
				}
			}
			state = WoflanState.NOWFNET;
		}
		return state;
	}

	public WoflanState diagnoseSCover() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.SCOVER)) {
			return WoflanState.SCOVER;
		} else if (assumptions.contains(WoflanState.NOSCOVER)) {
			return WoflanState.NOSCOVER;
		}
		WoflanState state = WoflanState.DONE;
		SComponentSet sComponents;
		/**
		 * Get the S-components.
		 */
//		sComponents = context.tryToFindOrConstructFirstObject(SComponentSet.class, SComponentConnection.class,
//				AbstractComponentSetConnection.COMPONENTSET, diagnosis.shortCNet);
		//Bruce: comment the above and replace the code with the below for command-line execution, not in ProM
		SComponentGenerator scGenerator = new SComponentGenerator();
		sComponents = scGenerator.calculateSComponentsPetriNet(context, diagnosis.shortCNet);

		NotSCoveredNodesSet nodesSet = null;
		/**
		 * Try whether the set of not-S-covered nodes has already been computed.
		 */
		try {
			for (NotSCoveredNodesConnection conn : cm.getConnections(NotSCoveredNodesConnection.class, context,
					diagnosis.shortCNet)) {
				nodesSet = conn.getObjectWithRole(NotSCoveredNodesConnection.NODES);

			}
		} catch (Exception e) {
		}
		if (nodesSet == null) {
			/**
			 * No it is not. Compute it now.
			 */
			SortedSet<PetrinetNode> coveredNodes = new TreeSet<PetrinetNode>();
			for (SortedSet<PetrinetNode> nodes : sComponents) {
				coveredNodes.addAll(nodes);
			}
			SortedSet<PetrinetNode> uncoveredNodes = new TreeSet<PetrinetNode>(diagnosis.shortCNet.getPlaces());
			uncoveredNodes.addAll(diagnosis.shortCNet.getTransitions());
			uncoveredNodes.removeAll(coveredNodes);
			nodesSet = new NotSCoveredNodesSet();
			nodesSet.add(uncoveredNodes);
			context.addConnection(new NotSCoveredNodesConnection(diagnosis.shortCNet, nodesSet));
		}
		if (nodesSet.isEmpty() || nodesSet.iterator().next().isEmpty()) {
			state = WoflanState.SCOVER;
		} else {
			state = WoflanState.NOSCOVER;
			diagnosis.setNotSCoveredNodes(nodesSet);
		}
		return state;
	}

	public WoflanState diagnosePCover() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.PCOVER)) {
			return WoflanState.PCOVER;
		} else if (assumptions.contains(WoflanState.NOPCOVER)) {
			return WoflanState.NOPCOVER;
		}
		WoflanState state = WoflanState.DONE;
		PlaceInvariantSet pInvariantSet;
		/**
		 * Get the place invariants.
		 */
		pInvariantSet = context.tryToFindOrConstructFirstObject(PlaceInvariantSet.class,
				PlaceInvariantConnection.class, AbstractInvariantMarkingConnection.INVARIANTMARKING,
				diagnosis.shortCNet);
		NotPCoveredNodesSet uncoveredNodes = null;
		try {
			for (NotPCoveredNodesConnection conn : cm.getConnections(NotPCoveredNodesConnection.class, context,
					diagnosis.shortCNet)) {
				uncoveredNodes = conn.getObjectWithRole(NotPCoveredNodesConnection.NODES);

			}
		} catch (Exception e) {
		}
		if (uncoveredNodes == null) {
			SortedSet<PetrinetNode> coveredNodes = new TreeSet<PetrinetNode>();
			for (MultiSet<Place> pInvariant : pInvariantSet) {
				coveredNodes.addAll(pInvariant.baseSet());
			}
			SortedSet<Place> uncoveredPlaces = new TreeSet<Place>(diagnosis.shortCNet.getPlaces());
			uncoveredPlaces.removeAll(coveredNodes);
			uncoveredNodes = new NotPCoveredNodesSet();
			uncoveredNodes.add(uncoveredPlaces);
			context.addConnection(new NotPCoveredNodesConnection(diagnosis.shortCNet, uncoveredNodes));
		}
		if (uncoveredNodes.isEmpty() || (uncoveredNodes.iterator().next().size() == 0)) {
			state = WoflanState.PCOVER;
		} else {
			diagnosis.setNotPCoveredNodes(uncoveredNodes);
			state = WoflanState.NOPCOVER;
		}
		return state;
	}

	public WoflanState diagnoseDeadness() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.DEAD)) {
			return WoflanState.DEAD;
		} else if (assumptions.contains(WoflanState.NOTDEAD)) {
			return WoflanState.NOTDEAD;
		}
		WoflanState state = WoflanState.DONE;
		DeadTransitionsSet deadTransitionsSet;
		/**
		 * Get the dead transitions.
		 */
//		deadTransitionsSet = context.tryToFindOrConstructFirstObject(DeadTransitionsSet.class,
//				DeadTransitionsConnection.class, DeadTransitionsConnection.TRANSITIONS, diagnosis.shortCNet,
//				initialMarking, diagnosis.semantics);
		// Bruce: replace the above code with the one below for command-line execution, not in ProM
		DeadTransitionAnalyzer deadTranAnalyzer = new DeadTransitionAnalyzer();
		deadTransitionsSet = deadTranAnalyzer.analyzeDeadTransitionPetriNet(context, diagnosis.shortCNet, 
														initialMarking, (PetrinetSemantics)diagnosis.semantics);

		if (deadTransitionsSet.isEmpty() || deadTransitionsSet.iterator().next().isEmpty()) {
			state = WoflanState.NOTDEAD;
		} else {
			state = WoflanState.DEAD;
			diagnosis.setDeadTransitions(deadTransitionsSet);
		}
		return state;
	}

	public WoflanState diagnoseLiveness() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.LIVE)) {
			return WoflanState.LIVE;
		} else if (assumptions.contains(WoflanState.NOTLIVE)) {
			return WoflanState.NOTLIVE;
		}
		WoflanState state = WoflanState.DONE;
		NonLiveTransitionsSet nonLiveTransitionsSet;
		/**
		 * Try whether the set of non-live transitions has already been
		 * computed.
		 */
//		nonLiveTransitionsSet = context.tryToFindOrConstructFirstObject(NonLiveTransitionsSet.class,
//				NonLiveTransitionsConnection.class, NonLiveTransitionsConnection.TRANSITIONS, diagnosis.shortCNet,
//				initialMarking, diagnosis.semantics);
		// Bruce: replace the above with the below code for command-line execution.
		LivenessAnalyzer2 liveAnalyzer = new LivenessAnalyzer2();
		Object[] result = liveAnalyzer.analyzeLivenessPetriNet(context, diagnosis.shortCNet, initialMarking,
															(PetrinetSemantics)diagnosis.semantics);
		nonLiveTransitionsSet = (NonLiveTransitionsSet)result[1];

		if (nonLiveTransitionsSet.isEmpty() || nonLiveTransitionsSet.iterator().next().isEmpty()) {
			state = WoflanState.LIVE;
		} else {
			state = WoflanState.NOTLIVE;
			NonLiveSequences nonLiveSequences;
			/**
			 * Get the non-live sequences.
			 */
//			nonLiveSequences = context.tryToFindOrConstructFirstObject(NonLiveSequences.class,
//					NonLiveSequencesConnection.class, NonLiveSequencesConnection.SEQUENCES, diagnosis.shortCNet,
//					initialMarking, diagnosis.semantics, finalMarking);
			//Bruce: replace the above with the below code for command-line execution.
			Marking[] finalMarkings = new Marking[1];
			finalMarkings[0] = finalMarking;
			LivenessAnalyzerWithSequence liveAnalyzerSeq = new LivenessAnalyzerWithSequence();
			Object[] result2 = liveAnalyzerSeq.analyzeLivenessPetriNet(context, diagnosis.shortCNet, initialMarking, 
													(PetrinetSemantics)diagnosis.semantics, finalMarkings);
			nonLiveSequences = (NonLiveSequences)result2[2];

			diagnosis.setNotLiveTransitions(nonLiveTransitionsSet, nonLiveSequences);
		}
		return state;
	}

	public WoflanState diagnoseBoundedness() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.BOUNDED)) {
			return WoflanState.BOUNDED;
		} else if (assumptions.contains(WoflanState.NOTBOUNDED)) {
			return WoflanState.NOTBOUNDED;
		}
		WoflanState state = WoflanState.DONE;
		UnboundedPlacesSet unboundedPlacesSet = null;

//		unboundedPlacesSet = context.tryToFindOrConstructFirstObject(UnboundedPlacesSet.class,
//				UnboundedPlacesConnection.class, UnboundedPlacesConnection.PLACES, diagnosis.shortCNet, initialMarking,
//				diagnosis.semantics);
		// Bruce: replace the above with the below code for command-line not in ProM environment.
		BoundednessAnalyzer boundnessAnalyzer = new BoundednessAnalyzer();
		Object[] result = boundnessAnalyzer.analyzeBoundednessPetriNet(context, diagnosis.shortCNet, 
																	initialMarking, (PetrinetSemantics)diagnosis.semantics);
		unboundedPlacesSet = (UnboundedPlacesSet)result[1];
		UnboundedSequences unboundedSeq = (UnboundedSequences)result[2];

		if (unboundedPlacesSet.isEmpty() || unboundedPlacesSet.iterator().next().isEmpty()) {
			state = WoflanState.BOUNDED;
		} else {
			state = WoflanState.NOTBOUNDED;
			UnboundedSequences unboundedSequences;
			/**
			 * Get the unbounded sequences.
			 */
//			unboundedSequences = context.tryToFindOrConstructFirstObject(UnboundedSequences.class,
//					UnboundedSequencesConnection.class, UnboundedSequencesConnection.SEQUENCES, diagnosis.shortCNet,
//					initialMarking, diagnosis.semantics);
			//Bruce: replace the above with the code below.
			boundnessAnalyzer.analyzeBoundednessPetriNet(context, diagnosis.shortCNet, initialMarking, 
															(PetrinetSemantics)diagnosis.semantics);
			unboundedSequences = unboundedSeq;

			diagnosis.setUnboundedPlaces(unboundedPlacesSet, unboundedSequences);
		}
		return state;
	}

	public WoflanState diagnoseFC() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.FREECHOICE)) {
			return WoflanState.FREECHOICE;
		} else if (assumptions.contains(WoflanState.NONFREECHOICE)) {
			return WoflanState.NONFREECHOICE;
		}
		WoflanState state = WoflanState.DONE;
		NonExtendedFreeChoiceClustersSet clusterSet=null;
		/**
		 * Get the non-extended-free-choice clusters.
		 */
//		clusterSet = context.tryToFindOrConstructFirstObject(NonExtendedFreeChoiceClustersSet.class,
//				NonExtendedFreeChoiceClustersConnection.class, NonExtendedFreeChoiceClustersConnection.CLUSTERS,
//				diagnosis.shortCNet);
		//Bruce: replace the above with the below code for command-line execution.
		FreeChoiceAnalyzer fcAnalyzer = new FreeChoiceAnalyzer();
		Object[] result;
		try {
			result = fcAnalyzer.analyzeFCAndEFCProperty(context, diagnosis.shortCNet);
			clusterSet = (NonExtendedFreeChoiceClustersSet)result[3];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		if (clusterSet.isEmpty()) {
			state = WoflanState.FREECHOICE;
		} else {
			state = WoflanState.NONFREECHOICE;
			diagnosis.setNotFreeChoice(clusterSet);
		}
		return state;
	}

	public WoflanState diagnosePTHandles() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.WELLPTHANDLED)) {
			return WoflanState.WELLPTHANDLED;
		} else if (assumptions.contains(WoflanState.NOTWELLPTHANDLED)) {
			return WoflanState.NOTWELLPTHANDLED;
		}
		WoflanState state = WoflanState.DONE;
		PTHandles handles=null;
		/**
		 * Get the PT-handles.
		 */
//		handles = context.tryToFindOrConstructFirstObject(PTHandles.class, PTHandleConnection.class,
//				AbstractNetHandleConnection.HANDLES, diagnosis.shortCNet);
		// Bruce: replace the above with the below code for command-line not in ProM.
		PTHandlesGenerator ptHandleGen = new PTHandlesGenerator();
		try {
			handles = ptHandleGen.analyzePTHandles(context, diagnosis.shortCNet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (handles.isEmpty()) {
			state = WoflanState.WELLPTHANDLED;
		} else {
			state = WoflanState.NOTWELLPTHANDLED;
			diagnosis.setPTHandles(handles);
		}
		return state;
	}

	public WoflanState diagnoseTPHandles() throws ConnectionCannotBeObtained {
		/**
		 * Check assumptions.
		 */
		if (assumptions.contains(WoflanState.WELLTPHANDLED)) {
			return WoflanState.WELLTPHANDLED;
		} else if (assumptions.contains(WoflanState.NOTWELLTPHANDLED)) {
			return WoflanState.NOTWELLTPHANDLED;
		}
		WoflanState state = WoflanState.DONE;
		TPHandles handles=null;
		/**
		 * Get the TP-handles.
		 */
//		handles = context.tryToFindOrConstructFirstObject(TPHandles.class, TPHandleConnection.class,
//				AbstractNetHandleConnection.HANDLES, diagnosis.shortCNet);
		// Bruce: replace the above with the below code for command-line execution, not in ProM.
		TPHandlesGenerator tpHandleGen = new TPHandlesGenerator();
		try {
			handles = tpHandleGen.analyzeTPHandles(context, diagnosis.shortCNet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (handles.isEmpty()) {
			state = WoflanState.WELLTPHANDLED;
		} else {
			state = WoflanState.NOTWELLTPHANDLED;
			diagnosis.setTPHandles(handles);
		}
		return state;
	}
	
	//Bruce: prepare connections for later uses in various analyzers
	private void prepareConnections1() throws ConnectionCannotBeObtained {
		System.out.println("Prepare connections 1");
		CGGenerator cgGenerator = new CGGenerator();
		Object[] cgResult = cgGenerator.petrinetetToCoverabilityGraph(context, diagnosis.shortCNet, initialMarking, 
																		(PetrinetSemantics)diagnosis.semantics);
		this.coverGraph = (CoverabilityGraph)cgResult[0];
		this.coverSet = (CoverabilitySet)cgResult[1];
		
		PlaceInvariantCalculator placeVariantPlugin = new PlaceInvariantCalculator();
		this.piSet = placeVariantPlugin.calculatePlaceInvariant(context, diagnosis.shortCNet);
	}
	
	//Bruce: prepare connections for later uses in various analyzers, mostly for Liveness
	private void prepareConnections2() throws ConnectionCannotBeObtained {
		System.out.println("Prepare connections 2");
		
		TSGenerator tsGenerator = new TSGenerator();
		Object[] tsResult = tsGenerator.calculateTS(context, diagnosis.shortCNet, initialMarking, 
														(PetrinetSemantics)diagnosis.semantics);
		this.reachGraph = (ReachabilityGraph)tsResult[0];

		//Do not use siphon and trap since taking very long to create siphon
		//Liveness will be checked based on reachability graph instead
//		SiphonGenerator siphonGen = new SiphonGenerator();
//		try {
//			siphonGen.calculateSiphons(context, diagnosis.shortCNet);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		TrapGenerator trapGen = new TrapGenerator();
//		try {
//			trapGen.calculateTraps(context, diagnosis.shortCNet);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
