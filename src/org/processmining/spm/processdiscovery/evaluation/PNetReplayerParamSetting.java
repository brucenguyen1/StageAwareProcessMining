package org.processmining.spm.processdiscovery.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * This class provides all standard settings for the PNetReplayer plugin
 * It primarily extract default parameter values and options from 
 * the PNetReplayer plugin code. These values help to run the tool
 * in command-line and background mode instead of selecting them from GUI. 
 * @author Bruce Nguyen
 *
 */
public class PNetReplayerParamSetting {
	public static XEventClassifier getXEventClassifier() {
		return XLogInfoImpl.NAME_CLASSIFIER;
	}
	
	public static XEventClass getDummyXEventClass() {
		return EvClassLogPetrinetConnectionFactoryUI.DUMMY;
	}
	
	public static EvClassLogPetrinetConnection createConnection(XLog log, Petrinet net) throws Exception {
		EvClassLogPetrinetConnection con;	
		con = new EvClassLogPetrinetConnection("Connection between " + net.getLabel() + " and "
				+ XConceptExtension.instance().extractName(log), net, log, XLogInfoImpl.NAME_CLASSIFIER,
				PNetReplayerParamSetting.getMap(log, net));
		return con;
	}
	
	/**
	 * Modified from EvClassLogPetrinetConnectionFactoryUI.getMap()
	 * @param log
	 * @param net
	 * @return
	 * @throws Exception 
	 */
	private static TransEvClassMapping getMap(XLog log, Petrinet net) throws Exception {
		TransEvClassMapping map = new TransEvClassMapping(
				XLogInfoImpl.NAME_CLASSIFIER, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		List<Transition> listTrans = new ArrayList<Transition>(net.getTransitions());
		
		Collections.sort(listTrans, new Comparator<Transition>() {
			public int compare(Transition o1, Transition o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		
		//Bruce added: for activity with empty label due to Divide and Conquer approach
		//if (listTrans.get(0).getLabel().isEmpty()) listTrans.remove(0);
		
		Object[] eventClasses = extractEventClasses(log, XLogInfoImpl.NAME_CLASSIFIER);
		final String invisibleTransitionRegEx = "[a-z][0-9]+|(tr[0-9]+)|(silent)|(tau)|(skip)|(invisible)";
		final Pattern pattern = Pattern.compile(invisibleTransitionRegEx);
		
		for (Transition trans : listTrans) {
			//System.out.println(trans.getLabel());
			XEventClass mappedEVClass = null;
			if (trans.isInvisible()) {
				mappedEVClass = EvClassLogPetrinetConnectionFactoryUI.DUMMY;
			} else {
				int i = preSelectOption(trans, eventClasses, pattern, false); //only use precise match
				if (i==0) { // NONE means no match between the transition and any event classes
					throw new Exception("No match found between visible transition="+trans.getLabel()+" and event classes.");
				}
				else {
					mappedEVClass = (XEventClass)eventClasses[i];
				}
			}
			
			map.put(trans, mappedEVClass);
		}
		
		Set<Transition> unmappedTrans = new HashSet<Transition>();
		for (Entry<Transition, XEventClass> entry : map.entrySet()) {
			if (entry.getValue().equals(map.getDummyEventClass())) {
				if (!entry.getKey().isInvisible()) {
					unmappedTrans.add(entry.getKey());
				}
			}
		}
		for (Transition t : unmappedTrans) {
			t.setInvisible(true);
		}
		
		return map;
	}
	
	/**
	 * Modified from EvClassLogPetrinetConnectionFactoryUI.preSelectOption
	 * 
	 * Returns the Event Option Box index of the most similar event for the
	 * transition.
	 * 
	 * @param transition
	 *            Name of the transitions, assuming low cases
	 * @param events
	 *            Array with the options for this transition
	 * @param approximateMatch
	 * 			  Matching transitions and events approximately
	 * @return Index of option more similar to the transition
	 */
	private static int preSelectOption(Transition transition, Object[] events, Pattern pattern, boolean approximateMatch) {
		String transitionLabel = transition.getLabel().toLowerCase();

//		mapTrans2ComboBox.get(transition).setForeground(WidgetColors.COLOR_LIST_FG);
//		mapTrans2ComboBox.get(transition).setBackground(WidgetColors.COLOR_LIST_BG);

		// try to find precise match
		for (int i = 1; i < events.length; i++) {
			String event = ((XEventClass) events[i]).toString().toLowerCase();
			if (event.equalsIgnoreCase(transitionLabel)) {
				return i;
			}
			;
		}
		
		// if no precisely matches found, try to match invisible transitions
		Matcher matcher = pattern.matcher(transitionLabel);
		if (matcher.find() && matcher.start() == 0) {
			return 0;
		}

		//return the first item if no more matches found
		if (approximateMatch) {
			//The metric to get the similarity between strings
			AbstractStringMetric metric = new Levenshtein();

			int index = 0;
			float simOld = Float.MIN_VALUE;
			for (int i = 1; i < events.length; i++) {
				String event = ((XEventClass) events[i]).toString().toLowerCase();

				if (transitionLabel.startsWith(event)) {
					index = i;
					break;
				}

				float sim = metric.getSimilarity(transitionLabel, event);
				if (simOld < sim) {
					simOld = sim;
					index = i;
				}

			}

//			mapTrans2ComboBox.get(transition).setForeground(Color.YELLOW);

			return index;
		} else {
			return 0; 
		}
	}
	
	public static CostBasedCompleteParam constructReplayParameter(PluginContext context, XLog log, Petrinet net, 
																					TransEvClassMapping mapping) {
		CostBasedCompleteParam paramObj = new CostBasedCompleteParam(getMapEvClassToCost(log, mapping), 
																		getTransitionWeight(net));
		paramObj.setMapSync2Cost(getSyncCost(net));
		paramObj.setMaxNumOfStates(getMaxNumOfStates());
		Marking initMarking = getInitialMarking(context, net);
		paramObj.setInitialMarking(initMarking);
		paramObj.setFinalMarkings(getFinalMarkings(context, net, initMarking));
		paramObj.setUsePartialOrderedEvents(isUsePartialOrderedEvents());
		
//		System.out.println(paramObj.getMapTrans2Cost());
//		System.out.println(paramObj.getMapEvClass2Cost());
//		System.out.println(paramObj.getMapSync2Cost());
		
		return paramObj;
	}
	
	private static Marking getInitialMarking(PluginContext context, PetrinetGraph net) {
		// check connection between petri net and marking
		Marking initMarking = null;
		try {
			initMarking = context.getConnectionManager()
					.getFirstConnection(InitialMarkingConnection.class, context, net)
					.getObjectWithRole(InitialMarkingConnection.MARKING);
		} catch (ConnectionCannotBeObtained exc) {
			initMarking = new Marking();
		}
		return initMarking;
	}
	
	/**
	 * Derive final markings from accepting states
	 * 
	 * @param context
	 * @param net
	 * @param initMarking
	 * @return
	 */
	private static Marking[] getFinalMarkings(PluginContext context, PetrinetGraph net, Marking initMarking) {
		// check if final marking exists
		Marking[] finalMarkings = null;
		try {
			Collection<FinalMarkingConnection> finalMarkingConnections = context.getConnectionManager()
					.getConnections(FinalMarkingConnection.class, context, net);
			if (finalMarkingConnections.size() != 0) {
				Set<Marking> setFinalMarkings = new HashSet<Marking>();
				for (FinalMarkingConnection conn : finalMarkingConnections) {
					setFinalMarkings.add((Marking) conn.getObjectWithRole(FinalMarkingConnection.MARKING));
				}
				finalMarkings = setFinalMarkings.toArray(new Marking[setFinalMarkings.size()]);
			} else {
				finalMarkings = new Marking[0];
			}
		} catch (ConnectionCannotBeObtained exc) {
			// no final marking provided, give an empty marking
			finalMarkings = new Marking[0];
		}
		return finalMarkings;
	}
	
	/**
	 * Modified from EvClassLogPetrinetConnectionFactoryUI.extractEventClasses
	 * get all available event classes using the selected classifier, add with
	 * NONE
	 * 
	 * @param log
	 * @param selectedItem
	 * @return: note that the first item is "NONE" if no matches found
	 */
	private static Object[] extractEventClasses(XLog log, XEventClassifier eventClassifier) {
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses eventClasses = summary.getEventClasses();

		// sort event class
		Collection<XEventClass> classes = eventClasses.getClasses();

		// create possible event classes
		Object[] arrEvClass = classes.toArray();
		Arrays.sort(arrEvClass);
		Object[] notMappedAct = { "NONE" };
		Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, arrEvClass);

		return boxOptions;
	}
	
	/**
	 * Get map from event class to cost of move on log
	 * 
	 * @return
	 */
	private static Map<XEventClass, Integer> getMapEvClassToCost(XLog log, TransEvClassMapping mapping) {
		XEventClassifier classifier = mapping.getEventClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClasses eventClassesName = summary.getEventClasses();
		Collection<XEventClass> evClassCol = new HashSet<XEventClass>(eventClassesName.getClasses());
		evClassCol.add(mapping.getDummyEventClass());
		
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<XEventClass, Integer>();
		for (XEventClass evClass : evClassCol) {
			mapEvClass2Cost.put(evClass, 1);
		}
		return mapEvClass2Cost;
	}
	
	/**
	 * get penalty when move on model is performed
	 * 
	 * @return
	 */
	private static Map<Transition, Integer> getTransitionWeight(Petrinet net) {
		Collection<Transition> transCol = net.getTransitions();
		Map<Transition, Integer> costs = new HashMap<Transition, Integer>();
		for (Transition trans : transCol) {
			costs.put(trans, (trans.isInvisible() ? 0 : 1));
		}
		return costs;
	}
	
	/**
	 * get cost of doing synchronous moves
	 * 
	 * @return
	 */
	private static Map<Transition, Integer> getSyncCost(Petrinet net) {
		Collection<Transition> transCol = net.getTransitions();
		Map<Transition, Integer> costs = new HashMap<Transition, Integer>();
		for (Transition trans : transCol) {
			if (!trans.isInvisible()) {
				costs.put(trans, 0);
			}
		}
		return costs;
	}

	/**
	 * get maximum number of explored states before stop exploration
	 * 
	 * @return
	 */
	private static Integer getMaxNumOfStates() {
		return 200000;
	}

	/**
	 * True if events with same timestamps are treated as partially ordered
	 * events
	 * 
	 * @return
	 */
	private static boolean isUsePartialOrderedEvents() {
		return false;
	}	
	
}
