package org.processmining.perspectivemining.log.attribute;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeID;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeIDImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.processmining.perspectivemining.graph.model.EventRow;

/**
 * Modified from XESImporter class in ProcessCube Implementation
 * @author Alfredo Bolt
 * @modifier Bruce
 *
 */
public class AttributeExtraction {
	public static boolean MISSING_BOOLEAN = false;
	public static String MISSING_LITERAL = "Missing";
	public static double MISSING_CONTINUOS = -1.0;
	public static long MISSING_DISCREET = -1;
	public static long MISSING_TIMESTAMP = 0;
	public static XID MISSING_XID = new XID();
	
	private XLog log = null;

	public AttributeExtraction(XLog log) {
		this.log = log;
	}

	public List<AttributeRow> getAttributeList(List<XEvent> events) {

		Set<String> attributeNamesSet = new HashSet<String>();
		Map<String, Set<String>> attributes = new HashMap<String, Set<String>>(); //attribute name, set of values

		// defines the set of attributes
		for (XEvent event : events) {
			attributeNamesSet.addAll(event.getAttributes().keySet());
		}
		// create sample value sets and the corresponding MappingRows
		int counter = 0;
		for (String att : attributeNamesSet) {
			Set<String> values = new HashSet<String>();
			counter = 5;
			for (XEvent event : events) {
				if (counter == 0)
					break;
				if (event.getAttributes().containsKey(att) && !event.getAttributes().get(att).toString().isEmpty())
					if (!values.contains(event.getAttributes().get(att).toString())) {
						values.add(event.getAttributes().get(att).toString());
						counter--;
					}
			}

			attributes.put(att, values);
		}

		List<AttributeRow> attributeObjects = new ArrayList<>();
		for (String att : attributes.keySet()) {
			attributeObjects.add(new AttributeRow(att, attributes.get(att), Attribute.IGNORE));
		}
		return attributeObjects;
	}

	/**
	 * 
	 * @param size:
	 *            defines the max number of events in the list. -1 means
	 *            infinite.
	 * @return: array with 2 elements 
	 * 	1st element: An ordered list of events as described in the input file.
	 *  2nd element: Map of trace/event to a set of missing attribute values
	 */
	public Object[] getEventList(long size, List<Attribute> a) {
		Map<XAttributable,Set<String>> missingValueMap = new HashMap();
		
		// Normalize all trace attributes because traces may have different
		// set of attributes
		Map<String, XAttribute> traceAttMap = new HashMap<>(); //name -> first XAttribute having this name
		for (XTrace trace : log) {
			for (XAttribute attribute : trace.getAttributes().values()){
				if (!traceAttMap.containsKey(attribute.getKey())) {
					traceAttMap.put(attribute.getKey(), attribute);
				}
			}
		}
		for (XTrace trace : log) {
			for (String attributeName : traceAttMap.keySet()) {
				if (!trace.getAttributes().containsKey(attributeName)) {
					if (!missingValueMap.containsKey(trace)) missingValueMap.put(trace, new HashSet<>());
					missingValueMap.get(trace).add(attributeName);
					XAttribute newAtt = createMissingValueAttribute(traceAttMap.get(attributeName));
					trace.getAttributes().put(newAtt.getKey(), newAtt);
				}
			}
		}
		
		// Normalize all event attributes because events may have different
		// set of attributes
		Map<String, XAttribute> eventAttMap = new HashMap<>(); //name -> first XAttribute having this name
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (XAttribute attribute : event.getAttributes().values()){
					if (!eventAttMap.containsKey(attribute.getKey())) {
						eventAttMap.put(attribute.getKey(), attribute);
					}
				}
			}
		}
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (String attributeName : eventAttMap.keySet()) {
					if (!event.getAttributes().containsKey(attributeName)) {
						if (!missingValueMap.containsKey(event)) missingValueMap.put(event, new HashSet<>());
						missingValueMap.get(event).add(attributeName);
						XAttribute newAtt = createMissingValueAttribute(eventAttMap.get(attributeName));
						event.getAttributes().put(newAtt.getKey(), newAtt);
					}
				}
				
			}
		}

		// pass trace level attributes to events
		for (XTrace trace : log) {
			List<XAttribute> traceAttributes = new ArrayList<XAttribute>();
			for (XAttribute traceAtt : trace.getAttributes().values())
				traceAttributes.add(createAttributeWithPrefix("C:", traceAtt));

			for (XEvent event : trace)
				for (XAttribute att : traceAttributes)
					event.getAttributes().put(att.getKey(), att);
		}
		
		

		// now create the final list of events
		List<XEvent> events = new ArrayList<XEvent>();
		int index = 0;
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				// Add eventID attribute
				event.getAttributes().put(EventRow.COL_EVENT_ID, new XAttributeLiteralImpl(EventRow.COL_EVENT_ID, index+""));
				index++;
				events.add(event);
			}
		}
		
		Object[] result = new Object[2];
		result[0] = events;
		result[1] = missingValueMap;
		
		return result;
	}
	
	/**
	 * 
	 * @param attribute: the representative attribute
	 * @return the new attribute with 'zero' value
	 */
	private XAttribute createMissingValueAttribute(XAttribute attribute) {
		// can use it with E: or T:
		XAttribute newAtt = null;

		if (attribute instanceof XAttributeBoolean)
			newAtt = new XAttributeBooleanImpl(attribute.getKey(), MISSING_BOOLEAN);
		else if (attribute instanceof XAttributeLiteral)
			newAtt = new XAttributeLiteralImpl(attribute.getKey(), MISSING_LITERAL);
		else if (attribute instanceof XAttributeContinuous)
			newAtt = new XAttributeContinuousImpl(attribute.getKey(), MISSING_CONTINUOS);
		else if (attribute instanceof XAttributeDiscrete)
			newAtt = new XAttributeDiscreteImpl(attribute.getKey(), MISSING_DISCREET);
		else if (attribute instanceof XAttributeTimestamp)
			newAtt = new XAttributeTimestampImpl(attribute.getKey(), MISSING_TIMESTAMP);
		else if (attribute instanceof XAttributeID)
			newAtt = new XAttributeIDImpl(attribute.getKey(), MISSING_XID);

		return newAtt;
	}

	/**
	 * 
	 * @param prefix:
	 *            the string prefix that we want to add to an attribute
	 * @param attribute:
	 *            the attribute to be modeified
	 * @return the modified attribute
	 */
	private XAttribute createAttributeWithPrefix(String prefix, XAttribute attribute) {
		// can use it with E: or T:
		XAttribute newAtt = null;

		if (attribute instanceof XAttributeBoolean)
			newAtt = new XAttributeBooleanImpl(prefix + attribute.getKey(), ((XAttributeBoolean) attribute).getValue());
		else if (attribute instanceof XAttributeLiteral)
			newAtt = new XAttributeLiteralImpl(prefix + attribute.getKey(), ((XAttributeLiteral) attribute).getValue());
		else if (attribute instanceof XAttributeContinuous)
			newAtt = new XAttributeContinuousImpl(prefix + attribute.getKey(),
					((XAttributeContinuous) attribute).getValue());
		else if (attribute instanceof XAttributeDiscrete)
			newAtt = new XAttributeDiscreteImpl(prefix + attribute.getKey(),
					((XAttributeDiscrete) attribute).getValue());
		else if (attribute instanceof XAttributeTimestamp)
			newAtt = new XAttributeTimestampImpl(prefix + attribute.getKey(),
					((XAttributeTimestamp) attribute).getValue());
		else if (attribute instanceof XAttributeID)
			newAtt = new XAttributeIDImpl(prefix + attribute.getKey(), ((XAttributeID) attribute).getValue());

		return newAtt;
	}
	
	/**
	 * Update the UseAs field for MappingRow
	 */
	public void parseAttributes(List<AttributeRow> attributeObjects) {
		for (AttributeRow row : attributeObjects) {

			boolean isParseable = false;
			/**
			 * First check if the value is a timestamp
			 */
			for (Object value : row.getValueSet()) {
				if (detectTimestampParser((String) value) != null)
					isParseable = true;
				else {
					isParseable = false;
					break;
				}
			}
			if (isParseable) {
				row.setUseAs(Attribute.DATE_TIME);
				continue;
			}

			/**
			 * Check if the value is a discrete number
			 */
			for (Object value : row.getValueSet()) {
				try {
					Integer.parseInt((String) value);
					isParseable = true;
				} catch (NumberFormatException e) {
					isParseable = false;
					break;
				}

			}
			if (isParseable) {
				row.setUseAs(Attribute.DISCRETE);
				continue;
			}

			/**
			 * Check if the value is a decimal number
			 */
			for (Object value : row.getValueSet()) {
				try {
					Double.parseDouble((String) value);
					isParseable = true;
				} catch (Exception e) {
					isParseable = false;
					break;
				}

			}
			if (isParseable) {
				row.setUseAs(Attribute.CONTINUOUS);
				continue;
			}

			/**
			 * If is not any of the above, we consider it as text
			 */
			row.setUseAs(Attribute.TEXT);
		}
	}
	
	public static DateFormat detectTimestampParser(String input) {
		DateFormat df;
		Date aux = null;
		try {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return df;
		} catch (Exception e) {
		}
		return null;
	}


}
