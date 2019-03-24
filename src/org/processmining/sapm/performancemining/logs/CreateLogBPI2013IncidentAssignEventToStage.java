package org.processmining.sapm.performancemining.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.performancemining.util.LogUtils;
import org.processmining.sapm.performancemining.util.OpenLogFilePlugin;

public class CreateLogBPI2013IncidentAssignEventToStage {
	public static final String EVENT_STAGE_KEY = "stage";
	public static final String STAGE0 = "Accept";
	public static final String STAGE1 = "Investigate";
	public static final String STAGE2 = "Solve";
	public static final String STAGE3 = "Close";

	public static final String STATUS_KEY = "status";
	public static final String STATUS_DECLINED = "declined";
	public static final String STATUS_CANCELLED = "cancelled";
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_IN_PROGRESS = "in-progress";

	/**
	 * @param args
	 *            1st param: name of the input log 2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + args[0]);
		XLog inputLog;
		XLog outputLog;
		XTrace outTrace;

		try {
			//-----------------------------------------
			// Read input log and select events for output log
			//-----------------------------------------    		
			inputLog = (XLog) logImporter.importFile(inputFile);
			//    		outputLog = new XLogImpl(inputLog.getAttributes());
			XFactory factory = new XFactoryNaiveImpl();
			outputLog = factory.createLog(inputLog.getAttributes());

			for (XTrace trace : inputLog) {
				//				outTrace = new XTraceImpl(trace.getAttributes());
				outTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
				String traceID = LogUtils.getConceptName(trace);
				boolean startfound = false;
				boolean wrongorder = false;
				int index = 0;
				for (XEvent event : trace) {
					String eventstage = LogUtils.getValue(event.getAttributes().get("stage")).toLowerCase();
					String previoustage = "";

					//-------------------------------
					// Check the series of events so far
					//-------------------------------
					if (eventstage.equals("1st")) {
						startfound = true;
					}

					if (!outTrace.isEmpty()) {
						previoustage = LogUtils.getValue(
								outTrace.get(outTrace.size() - 1).getAttributes().get("stage")).toLowerCase();
						if ((previoustage.equals("1st") && (eventstage.equals("3rd")))
								|| (previoustage.equals("2nd") && eventstage.equals("1st"))
								|| (previoustage.equals("3rd") && eventstage.equals("1st"))
								|| (previoustage.equals("3rd") && eventstage.equals("2nd"))) {
							wrongorder = true;
						}
					}

					if (startfound && !wrongorder) {
						outTrace.add(event);
					}

					if (!outTrace.isEmpty()) {
						if (wrongorder || (trace.indexOf(event) == trace.size() - 1)) {
							//-------------------------------
							// Add a new trace from the series of events so far
							//-------------------------------
							index++;
							LogUtils.setConceptName(outTrace, traceID + "-" + index);
							XEvent lastevent = outTrace.get(outTrace.size() - 1);
							String lasteventstage = LogUtils.getValue(lastevent.getAttributes().get("stage"))
									.toLowerCase();
							String lasttransitionType = LogUtils.getLifecycleTransition(lastevent).toLowerCase();
							if (lasttransitionType.contains("closed")) {
								if (lasteventstage.equals("3rd")) {
									outTrace.getAttributes().put("status",
											factory.createAttributeLiteral("status", "complete", null));
								} else {
									outTrace.getAttributes().put("status",
											factory.createAttributeLiteral("status", "resolved", null));
								}
							} else {
								if (lasteventstage.equals("1st")) {
									outTrace.getAttributes().put("status",
											factory.createAttributeLiteral("status", "jumpto3rd", null));
								} else {
									outTrace.getAttributes().put("status",
											factory.createAttributeLiteral("status", "pushtofront", null));
								}
							}
							outputLog.add(createNewTraceWithInvestigateEvents(outTrace));

							// Reset for new trace
							outTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
							if (wrongorder && eventstage.equals("1st")) {
								outTrace.add(event);
							}
							startfound = false;
							wrongorder = false;
						}
					}
				}
			}

			//-----------------------------------------
			// Write to the output log
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlSerializer();
			String outputFile = System.getProperty("user.dir") + "\\" + args[1];
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(outputLog, fs);
			System.out.println("DONE!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static XTrace createNewTraceWithInvestigateEvents(XTrace trace) {
		XFactory factory = new XFactoryNaiveImpl();
		XTrace newTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
		for (XEvent event : trace) {
			//---------------------------------------
			// Add current event from the stack.
			// Need to modify the transition type to match a standard log
			//---------------------------------------
			String transitionType = LogUtils.getLifecycleTransition(event).toLowerCase();
			String res = LogUtils.getOrganizationalResource(event).toLowerCase();

			if (transitionType.equals("in progress")) {
				if (newTrace.isEmpty()
						|| !LogUtils.getConceptName(newTrace.get(newTrace.size() - 1)).toLowerCase()
								.equals("investigate")) {
					//Add the start 'investigate' event with the same timestamp as the current event
					XEvent newEvent = (XEvent) event.clone();
					LogUtils.setConceptName(newEvent, "investigate");
					LogUtils.setLifecycleTransition(newEvent, "start");
					newTrace.add(newEvent);
				} else if (!LogUtils.getOrganizationalResource(newTrace.get(newTrace.size() - 1)).toLowerCase()
						.equals(res)) { //Change resource
					XEvent newEvent = (XEvent) newTrace.get(newTrace.size() - 1).clone();
					LogUtils.setConceptName(newEvent, "investigate");
					LogUtils.setLifecycleTransition(newEvent, "complete");
					LogUtils.setTimestamp(newEvent, LogUtils.getTimestamp(event).toDate());
					newTrace.add(newEvent);

					newEvent = (XEvent) event.clone();
					LogUtils.setConceptName(newEvent, "investigate");
					LogUtils.setLifecycleTransition(newEvent, "start");
					newTrace.add(newEvent);
				}
			} else if (transitionType.equals("assigned")
					|| //assign the issue to other person
					transitionType.equals("awaiting assignment")
					|| //return the issue to the queue
					transitionType.contains("wait") || transitionType.equals("resolved")
					|| transitionType.equals("cancelled") || transitionType.equals("in call")) {
				if (!newTrace.isEmpty()
						&& LogUtils.getConceptName(newTrace.get(newTrace.size() - 1)).toLowerCase()
								.equals("investigate")) {
					//Add complete 'investigate' event with the same timestamp as the current event 
					XEvent newEvent = (XEvent) newTrace.get(newTrace.size() - 1).clone();
					LogUtils.setConceptName(newEvent, "investigate");
					LogUtils.setLifecycleTransition(newEvent, "complete");
					LogUtils.setTimestamp(newEvent, LogUtils.getTimestamp(event).toDate());
					newTrace.add(newEvent);
				}
				//Add the event
				LogUtils.setConceptName(event, transitionType);
				LogUtils.setLifecycleTransition(event, "complete");
				newTrace.add(event);
			} else if (!transitionType.equals("closed")) {
				LogUtils.setConceptName(event, transitionType);
				LogUtils.setLifecycleTransition(event, "complete");
				newTrace.add(event);
			}

		}

		return newTrace;
	}

}
