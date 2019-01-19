package org.processmining.spm.stagemining.fuzzymodel.util;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;

public class FMLog {

	public static String getConceptName(XAttributable attrib) {
		String name = XConceptExtension.instance().extractName(attrib);
		return (name != null ? name : "<no name>");
	}

	public static void setConceptName(XAttributable attrib, String name) {
		XConceptExtension.instance().assignName(attrib, name);
	}

	public static String getLifecycleTransition(XEvent event) {
		String name = XLifecycleExtension.instance().extractTransition(event);
		return (name != null ? name : "<no transition>");
	}

	public static void setLifecycleTransition(XEvent event, String transition) {
		XLifecycleExtension.instance().assignTransition(event, transition);
	}

	public static String getOrganizationalResource(XEvent event) {
		String name = XOrganizationalExtension.instance().extractResource(event);
		return (name != null ? name : "<no resource>");
	}

	public static Date getTimestamp(XEvent event) {
		Date date = XTimeExtension.instance().extractTimestamp(event);
		return date;
	}
	
	public static String getValue(XAttribute attr) {
		if (attr instanceof XAttributeBoolean) {
			Boolean b = ((XAttributeBoolean) attr).getValue();
			return b.toString();
		} else if (attr instanceof XAttributeContinuous) {
			Double d = ((XAttributeContinuous) attr).getValue();
			return d.toString();
		} else if (attr instanceof XAttributeDiscrete) {
			Long l = ((XAttributeDiscrete) attr).getValue();
			return l.toString();
		} else if (attr instanceof XAttributeLiteral) {
			String s = ((XAttributeLiteral) attr).getValue();
			return s;
		} else if (attr instanceof XAttributeTimestamp) {
			Date d = ((XAttributeTimestamp) attr).getValue();
			return d.toString();
		}
		return "";
	}
}
