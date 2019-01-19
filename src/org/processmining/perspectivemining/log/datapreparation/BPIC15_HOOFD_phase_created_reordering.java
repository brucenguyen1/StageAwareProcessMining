package org.processmining.perspectivemining.log.datapreparation;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.perspectivemining.ui.fakecontext.FakePluginContext;
import org.processmining.perspectivemining.utils.LogUtils;
import org.processmining.perspectivemining.utils.OpenLogFilePlugin;

public class BPIC15_HOOFD_phase_created_reordering {
	public static void main(String[] args) {
		
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log;
			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "BPIC15_5.xes"));
			XFactory factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("TraceID: " + traceID);
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				boolean accept = true;
				for (XEvent event: trace) {
					String eventName = LogUtils.getConceptName(event);
					if (eventName.contains("01_HOOFD")) { 
						event.getAttributes().put("phase", factory.createAttributeLiteral("phase", eventName.substring(9,10), null));
						newTrace.add(event);
					}
				}
				newLog.add(newTrace);
			}
			
			
			// Clean the order of phases since many events have the same timestamp
			// Assume that events in the original trace has been ordered by timestamps
			// and starts with phase 0.
			XLog newLog2 = factory.createLog(log.getAttributes());
			for (XTrace trace : newLog) {
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				
				for (int i=0;i<trace.size();i++) {
					
					XEvent event = trace.get(i);
					long eventTime = LogUtils.getTimestamp(event).getMillis();
					int eventPhase = Integer.valueOf(LogUtils.getValue(event.getAttributes().get("phase")).toString());
					
					if (!newTrace.contains(event)) {
						newTrace.add(event);
					}
					
					// Look ahead for unselected events with the closest timestamp to that of the current event
					int lookahead = trace.size();
					long smallestTime = Long.MAX_VALUE;
					Set<XEvent> smallestTimeEvents = new HashSet<>();
					for (int j=1;j<=lookahead;j++) {
						if ((i+j) >= trace.size()) {
							break;
						}
						
						XEvent aheadEvent = trace.get(i+j);
						long aheadTime = LogUtils.getTimestamp(aheadEvent).getMillis();
						
//							if (!newTrace.contains(aheadEvent) && 
//									Long.compare(aheadTime, eventTime) == 0 && aheadPhase.equals(eventPhase)) {
//								newTrace.add(aheadEvent);
//							}
						if (!newTrace.contains(aheadEvent)) {
							if (aheadTime < smallestTime) {
								smallestTime = aheadTime;
								smallestTimeEvents.clear();
								smallestTimeEvents.add(aheadEvent);
							}
							else if (Long.compare(aheadTime, smallestTime) == 0) {
								smallestTimeEvents.add(aheadEvent);
							}
						}
					}
					
					// Select events with the smallest phase among the smallest time events
					int smallestPhase = 100;
					Set<XEvent> smallestPhaseEvents = new HashSet<>();
					for (XEvent event2 : smallestTimeEvents) {
						int event2Phase = Integer.valueOf(LogUtils.getValue(event2.getAttributes().get("phase")).toString());
						if (event2Phase < smallestPhase) {
							smallestPhase = event2Phase;
							smallestPhaseEvents.clear();
							smallestPhaseEvents.add(event2);
						}
						else if (Integer.compare(event2Phase,smallestPhase) == 0) {
							smallestPhaseEvents.add(event2);
						}
					}
					
					for (XEvent event3 : smallestPhaseEvents) {
						if (!newTrace.contains(event3)) newTrace.add(event3);
					}
				}
				
				newLog2.add(newTrace);
			}
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + "BPIC15_5_HOOFD_phase_created_phase_reordering.xes.gz");
			writer.serialize(newLog2, fos);
			fos.close();
			
			System.out.println("DONE!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
