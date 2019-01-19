package org.processmining.spm.stagemining.fuzzymodel.util;

/**
 * Fuzzy Miner Log
 * 
 * Log access for the Fuzzy miner.
 * 
 * @author Li Jiafei
 * @version 0.1
 * 
 */

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class FuzzyMinerLog {

	/**
	 * get all start events of the log
	 * 
	 * @param log
	 * @return
	 */
	static public Set<XEvent> getStartEvents(XLog log) {
		Set<XEvent> startEvents = new HashSet<XEvent>();
		for (XTrace trace : log) {
			XEvent startEvt = trace.get(0);
			if (!(isEventAdded(startEvt, startEvents))) {
				startEvents.add(startEvt);
			}
		}
		return startEvents;
	}

	/**
	 * get all end events of the log
	 * 
	 * @param log
	 * @return
	 */
	static public Set<XEvent> getEndEvents(XLog log) {
		Set<XEvent> endEvents = new HashSet<XEvent>();
		for (XTrace trace : log) {
			int evtCounts = trace.size();
			XEvent endEvt = trace.get(evtCounts - 1);
			if (!(isEventAdded(endEvt, endEvents))) {
				endEvents.add(endEvt);
			}
		}
		return endEvents;
	}

	/*
	 * decide whether the event has been added to the set or not
	 */
	static public boolean isEventAdded(XEvent evt, Set<XEvent> evtSet) {
		boolean isAdded = false;
		String oldEventName, oldEventType;
		String newEventName, newEventType;
		newEventName = FMLog.getConceptName(evt);
		newEventType = FMLog.getLifecycleTransition(evt);
		for (XEvent existingEvt : evtSet) {
			oldEventName = FMLog.getConceptName(existingEvt);
			oldEventType = FMLog.getLifecycleTransition(existingEvt);
			if (newEventName.equals(oldEventName) && newEventType.equals(oldEventType)) {
				isAdded = true;
				break;
			}
		}
		return isAdded;
	}

	/**
	 * Get all traces from a log.
	 * 
	 * @param log
	 *            The log to get the traces from.
	 * @return All traces from the log.
	 */
	static public Collection<XTrace> getTraces(XLog log) {
		return log;
	}

	/**
	 * Get all events from a trace.
	 * 
	 * @param trace
	 *            The trace to get the events from.
	 * @return All events from the trace.
	 */
	static public Collection<XEvent> getEvents(XTrace trace) {
		return trace;
	}

	/**
	 * Get the data attributes of an event.
	 * 
	 * @param event
	 *            The event to get the data attribute sof.
	 * @return The data attributes of the event.
	 */
	static public Map<String, String> getDataAttributes(XEvent event) {
		XAttributeMap attributeMap = event.getAttributes();
		HashMap<String, String> map = new HashMap<String, String>();
		for (String label : attributeMap.keySet()) {
			map.put(label, attributeMap.get(label).toString());
		}
		return map;
	}

	static public FMLogEvents getLogEvents(XLog log) {
		int eventsCount = 0;
		FMLogEvents logEvents = new FMLogEvents(log);
		// if the value of isExistingEvent is -1,there is no such XEvent in the FMLogEvents yet
		// if the value of isExistingEvent is bigger than -1,there has already this XEvent.
		int isExistingEvent = -1;
		for (XTrace pi : FuzzyMinerLog.getTraces(log)) {
			for (XEvent evt : FuzzyMinerLog.getEvents(pi)) {
				// System.out.println("the size of the FMLogEvents is " + logEvents.size());
				isExistingEvent = logEvents.findLogEventNumber(evt);
				if (isExistingEvent == -1) {
					logEvents.add(evt);
					eventsCount++;
				}
			}
		}
		logEvents.setEventsCount(eventsCount);
		//	 System.out.println("the size of the FMLogEvents is " + logEvents.size());
		//	System.out.println("Finishing Setting the value of the FMLogEvent!!!");
		return logEvents;
	}

	static public Date getEventTime(XEvent evt) {
		return FMLog.getTimestamp(evt);
	}

	static public String getEventName(XEvent evt) {
		return FMLog.getConceptName(evt);
	}

	static public String getEventType(XEvent evt) {
		return FMLog.getLifecycleTransition(evt);
	}
}
