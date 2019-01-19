package org.processmining.perspectivemining.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.perspectivemining.graph.comparators.XEventTimeComparator;
import org.processmining.perspectivemining.graph.model.EventRow;
import org.processmining.perspectivemining.graph.model.ObjectSchema;
import org.processmining.perspectivemining.graph.model.ProcessInstance;
import org.processmining.perspectivemining.log.attribute.AttributeExtraction;
import org.processmining.perspectivemining.log.attribute.AttributeRow;
import org.processmining.perspectivemining.utils.LogUtils;

public class PerspectiveInputObject {
	private XLog log;
	private List<AttributeRow> attributes = new ArrayList<>();
	private Map<XEvent,EventRow> mapEventRows = new HashMap<>();
	private List<ProcessInstance> instances = new ArrayList<>();
	private Map<XAttributable, Set<String>> missingValues = new HashMap<>();
	private long logStartTime = Long.MAX_VALUE;
	private long logEndTime = 0;
//	private Map<String,Set<EventRow>> mapCaseIDToEvents = new HashMap<>();
	
	public PerspectiveInputObject(XLog log) throws Exception {
		this.log = log;
		AttributeExtraction attExtract = new AttributeExtraction(this.log);
		Object[] extract = attExtract.getEventList(-1, null);
		
		List<XEvent> events = (List<XEvent>)extract[0];
		Collections.sort(events, new XEventTimeComparator());
		
		missingValues = (Map<XAttributable, Set<String>>)extract[1];
		attributes = attExtract.getAttributeList(events);
		attExtract.parseAttributes(attributes);
		
		//Create event rows
		List<String> attNames = new ArrayList<>();
		for (AttributeRow att : attributes) {
			if (!attNames.contains(att.getName())) attNames.add(att.getName());
		}
		for (XEvent e : events) {
			List<Object> row = new ArrayList<>();
			for (String attName : attNames) {
				row.add(e.getAttributes().get(attName));
			}
			EventRow eventRow = new EventRow(attNames, row);
			mapEventRows.put(e, eventRow);
			
			long eventTime = LogUtils.getTimestamp(e).getMillis();
			if (eventTime > logEndTime) logEndTime = eventTime;
			if (eventTime < logStartTime) logStartTime = eventTime;
		}
		
		this.createProcessInstances();
	}
	
	public XLog getLog() {
		return log;
	}
	
	public List<AttributeRow> getAttributeList() {
		return attributes;
	}
	
	/**
	 * The event list has been sorted after creation
	 * @return: sorted list of events
	 */
//	public List<XEvent> getEvents() {
//		return this.events;
//	}
	
//	public List<EventRow> getEventRows() {
//		return this.eventRows;
//	}
	
	public Collection<EventRow> getEventRows() {
		return this.mapEventRows.values();
	}
	
	public Set<XAttributable> getLogElementsWithMissingValues() {
		return missingValues.keySet();
	}
	
	public Set<String> getMissingValues(XAttributable logElement) {
		return missingValues.get(logElement);
	}
	
	public long getLogStartTime() {
		return this.logStartTime;
	}
	
	public long getLogEndTime() {
		return this.logEndTime;
	}
	
	public List<ProcessInstance> getProcessInstances() {
		return this.instances;
	}
	
	/*
	 * One process instance corresponds to a case in the log
	 * The process instance fragment must preserve the original order of events in the original case 
	 * A process instance created here has empty abstract fragments which
	 * will be populated in the process abstraction creation step later.
	 */
	private void createProcessInstances() throws Exception {
		AttributeRow caseIDAttribute = this.getCaseIDAttribute();
		ObjectSchema caseIDSchema = this.getCaseIDSchema(caseIDAttribute);
		for (XTrace trace : this.log) {
			String caseID = LogUtils.getConceptName(trace);
			List<String> instanceValue = new ArrayList<>();
			instanceValue.add(caseID);
			ProcessInstance instance = new ProcessInstance(caseIDSchema, instanceValue);
			for (XEvent event : trace) {
				instance.getFragment().add(mapEventRows.get(event));
			}
//			objectInstance.getFragment().sort(); //NO NEED TO SORT because Traces are assumed sorted in log files
			this.instances.add(instance);
		}
	}
	
	private AttributeRow getCaseIDAttribute() throws Exception {
		AttributeRow caseIDAttribute = null;
		for (AttributeRow att : this.attributes) {
			if (att.getName().equals(EventRow.COL_CASE_CLASSIFIER)) {
				caseIDAttribute = att;
				break;
			}
		}
		if (caseIDAttribute == null) {
			throw new Exception("Cannot find caseIDAttribute with name=" + EventRow.COL_CASE_CLASSIFIER);
		}
		return caseIDAttribute;
	}
	
	private ObjectSchema getCaseIDSchema(AttributeRow caseIDAttribute) throws Exception {
		ObjectSchema caseIDSchema = new ObjectSchema (new ArrayList<>());
		caseIDSchema.getAttributes().add(caseIDAttribute);
		return caseIDSchema;
	}

}
