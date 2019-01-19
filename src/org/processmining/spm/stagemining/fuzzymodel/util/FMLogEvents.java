/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.spm.stagemining.fuzzymodel.util;

import java.util.ArrayList;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * A list of <code>LogEvent</code> objects.
 * 
 * @author Jiafei Li (jiafei@jlu.edu.cn)
 * @version 1.0
 */

public class FMLogEvents extends ArrayList<XEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8307213970716587839L;
	private final XLogInfo logInfo;
	private final XLog eventLog;
	private int eventsCount;

	public int getEventsCount() {
		return eventsCount;
	}

	public void setEventsCount(int eventsCount) {
		this.eventsCount = eventsCount;
	}

	/**
	 * Constructor for an empty list.
	 */
	public FMLogEvents(XLog log) {
		eventLog = log;
		logInfo = XLogInfoFactory.createLogInfo(log);
		//logEvents = logInfo.

	}

	public XLog getLog() {
		return eventLog;
	}

	public XLogInfo getLogInfo() {
		return logInfo;
	}

	/**
	 * Find a <code>XEvent</code> object by its name and type. Returns null if
	 * there is no such <code>LogEvent</code> in the list.
	 * 
	 * @param name
	 *            the name of the <code>XEvent</code> to find
	 * @param type
	 *            the type of the <code>XEvent</code> to find
	 * @return the <code>XEvent</code> object if it is found, null otherwise
	 */
	public XEvent findLogEvent(String name, String type) {
		String eventName, eventType;
		for (XTrace pi : FuzzyMinerLog.getTraces(eventLog)) {
			for (XEvent evt : FuzzyMinerLog.getEvents(pi)) {
				eventName = FMLog.getConceptName(evt);
				eventType = FMLog.getLifecycleTransition(evt);
				if (eventName.equals(name) && eventType.equals(type)) {
					return evt;
				}
			}
		}
		return null; // not found
	}

	/**
	 * Find the index of a given <code>XEvent</code> object in the list. Returns
	 * -1 if there is no such <code>XEvent</code> in the list.
	 * 
	 * @param name
	 *            the name of the <code>XEvent</code> to find
	 * @param type
	 *            the type of the <code>XEvent</code> to find
	 * @return the index of the <code>XEvent</code> object in the list if it is
	 *         found, -1 otherwise
	 */
	public int findLogEventNumber(String name, String type) {
		/*
		 * XEventClasses logEventClasses = logInfo.getEventClasses(); XEvent
		 * evt= findXEvent(name,type); if(evt!= null){ XEventClass evtClass =
		 * logEventClasses.getClassOf(evt); int index = evtClass.getIndex();
		 * return index; }
		 */

		String eventName, eventType;
		XEvent evt;
		for (int i = 0; i < size(); i++) {
			evt = getEvent(i);
			eventName = FMLog.getConceptName(evt);
			eventType = FMLog.getLifecycleTransition(evt);
			if (eventName.equals(name) && eventType.equals(type)) {
				return i;
			}
		}
		return -1; // not found
	}

	/**
	 * Find the index of a given <code>XEvent</code> object in the list. Returns
	 * -1 if there is no such <code>XEvent</code> in the list.
	 * 
	 * @param event
	 *            the event<code>XEvent</code> to find
	 * @return the index of the <code>XEvent</code> object in the list if it is
	 *         found, -1 otherwise
	 */
	public int findLogEventNumber(XEvent event) {
		String oldEventName, oldEventType;
		String newEventName, newEventType;
		XEvent evt;
		newEventName = FMLog.getConceptName(event);
		newEventType = FMLog.getLifecycleTransition(event);
		for (int i = 0; i < size(); i++) {
			evt = getEvent(i);
			oldEventName = FMLog.getConceptName(evt);
			oldEventType = FMLog.getLifecycleTransition(evt);
			if (newEventName.equals(oldEventName) && newEventType.equals(oldEventType)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the item at the given index.
	 * 
	 * @param i
	 *            the index
	 * @return the item at index i
	 */
	public XEvent getEvent(int i) {
		return get(i);
	}
}
