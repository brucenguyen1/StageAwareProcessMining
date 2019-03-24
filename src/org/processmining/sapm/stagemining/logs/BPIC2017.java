package org.processmining.sapm.stagemining.logs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class BPIC2017 {
	public static void main(String[] args) {
		final String milestones = "a_accepted|a_complete|a_pending|a_cancelled|a_denied"; 
		System.out.println("Import log file");
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		XLog log;
		try {
			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "BPIC2017.xes"));
			XFactory factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("TraceID: " + traceID);
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				List<XEvent> pastEvents = new ArrayList<>();
				String currentStage = "S0";
				
				for (int i=0;i<trace.size();i++) {
					XEvent e = trace.get(i);
					String eName = LogUtils.getConceptName(e);
					//System.out.println("Event name: " + eName);
					if (!milestones.contains(eName.toLowerCase())) {
						pastEvents.add(e);
					}
					else {
						// Stage
						if (eName.equalsIgnoreCase("a_accepted")) { //end of stage S1
							if (!currentStage.equals("S0")) {
								System.err.println("Incorrect stage order. Encounter stage '" + currentStage + "' at an A_Accepted event");
							}
							else {
								currentStage = "S1";
							}
						}
						else if (eName.equalsIgnoreCase("a_complete")) { // end of stage S2
							if (!currentStage.equals("S1")) {
								System.err.println("Incorrect stage order. Encounter stage '" + currentStage + "' at an A_Complete event");
							}
							else {
								currentStage = "S2";
							}
						}
						else if (eName.equalsIgnoreCase("a_pending")) { // end of stage S3
							if (!currentStage.equals("S2")) {
								System.err.println("Incorrect stage order. Encounter stage '" + currentStage + "' at an A_Pending event");
							}
							else {
								currentStage = "S3";
							}
						}
						else if (eName.equalsIgnoreCase("a_cancelled") || eName.equalsIgnoreCase("a_denied")) {
							if (currentStage.equals("S0")) {
								currentStage = "S1";
							}
							else if (currentStage.equals("S1")) {
								currentStage = "S2";
							}
							else if (currentStage.equals("S2")) {
								currentStage = "S3";
							}
							else {
								System.err.println("Incorrect stage order. Encounter stage '" + currentStage + "' at an A_Cancelled/A_Denied event");
							}
						}
						else {
							System.err.println("Cannot identify a stage from event name = " + eName);
						}
						
						// Collect events into new trace
						for (XEvent pastEvent : pastEvents) {
							LogUtils.setConceptName(pastEvent, currentStage + "_" + eName);
							pastEvent.getAttributes().put("stage", factory.createAttributeLiteral("stage", currentStage, null));
							newTrace.add(pastEvent);
						}
						newTrace.add(e);
						pastEvents.clear();
						
						// Check events after a trace has come to finish
						if (eName.equalsIgnoreCase("a_pending") || eName.equalsIgnoreCase("a_cancelled") || eName.equalsIgnoreCase("a_denied")) {
							// Don't take these ending events
							for (int j=i+1;j<trace.size();j++) {
								XEvent eventAfterAppFinished = trace.get(j);
								String eventLabelAfterAppFinished = LogUtils.getConceptName(eventAfterAppFinished);
								if (!"o_cancelled|o_refused".contains(eventLabelAfterAppFinished.toLowerCase())) {
									System.err.println("Encounter invalid events after a_pending/a_cancelled. Event name=" + eventLabelAfterAppFinished);
								}
							}
							
							// Update trace status
							if (eName.equalsIgnoreCase("a_pending")) {
								newTrace.getAttributes().put("status", factory.createAttributeLiteral("status", "completed", null));
							}
							else if (eName.equalsIgnoreCase("a_cancelled")) {
								newTrace.getAttributes().put("status", factory.createAttributeLiteral("status", "cancelled", null));
							}
							else if (eName.equalsIgnoreCase("a_denied")) {
								newTrace.getAttributes().put("status", factory.createAttributeLiteral("status", "denied", null));
							}
						}
						
					}
				}
				newLog.add(newTrace);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
