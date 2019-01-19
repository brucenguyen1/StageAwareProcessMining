package org.processmining.stagedprocessflows.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.stagedprocessflows.util.LogUtils;
import org.processmining.stagedprocessflows.util.OpenLogFilePlugin;

public class CreateLogBPI2012 {
	//Note that A_FINALIZED is not used as a stage since A_FINALIZED and A_ACCEPTED are closely linked.
	//For example, some activities have started before A_ACCEPTED and only completed after A_FINALIZED event
	//Therefore, it is hard to separate A_ACCEPTED and A_FINALIZED as two stages.
	public static final String EVENT_STAGE_KEY = "stage";
	public static final String STAGE1 = "S1"; //From START to A_PREACCEPTED: Pre-Assess
	public static final String STAGE2 = "S2"; //From A_PREACCEPTED TO A_ACCEPTED: Assess
	public static final String STAGE3 = "S3"; //From A_ACCEPTED to the last O_SENT_BACK: Negotiate
	public static final String STAGE4 = "S4"; //From the last O_SENT_BACK to A_ACTIVATED: Validate
	public static final String STAGE5 = "S5"; //From A_ACTIVATED: DONE

	public static final String STATUS_KEY = "status";
	public static final String STATUS_DECLINED = "declined";
	public static final String STATUS_CANCELLED = "cancelled";
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_IN_PROGRESS = "in-progress";

	public static final String EVENT_TYPE_KEY = "type";
	public static final String EVENT_TYPE_GATE = "gate";
	public static final String EVENT_TYPE_ACT = "activity";

	/**
	 * @param args
	 *            1st param: name of the input log 2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + args[0]);
		XLog inputLog;
		XLogImpl outputLog;
		XTrace outTrace;

		try {
			File conversionResultFile = new File(System.getProperty("user.dir") + "\\results.log");
			FileWriter fileWriter = new FileWriter(conversionResultFile);
			String newline = System.getProperty("line.separator");

			//-----------------------------------------
			// Read input log and select events for output log
			//-----------------------------------------    		
			inputLog = (XLog) logImporter.importFile(inputFile);
			outputLog = new XLogImpl(inputLog.getAttributes());
			XFactory factory = new XFactoryNaiveImpl();

			String curTraceID = "";
			for (XTrace trace : inputLog) {
				curTraceID = LogUtils.getConceptName(trace);
				outTrace = new XTraceImpl(trace.getAttributes());

				fileWriter.write("TraceID: " + curTraceID + newline);

				//---------------------------------------
				// Look for the last O_SENT_BACK event: end of Negotiate stage
				//---------------------------------------
				XEvent lastSendBack = null;
				for (XEvent event : trace) {
					if (LogUtils.getConceptName(event).equals("O_SENT_BACK")) {
						lastSendBack = event;
					}
				}

				String curStage = STAGE1;
				int eventCount = 0;
				boolean previousActEventCompleted = true;
				boolean stageAboutToEnd = false;
				boolean isCaseExited = false; //declined or cancelled
				boolean isCaseCompleted = false; // activated
				for (XEvent event : trace) {

					//-----------------------------------
					// Add the first S1_START event
					//-----------------------------------
					//					if (eventCount == 0) {
					//						String newEventName = curStage + "_START"; 
					//						XEvent newEvent = (XEvent)event.clone();
					//						LogUtilites.setConceptName(newEvent, newEventName);
					//						LogUtilites.setTimestamp(newEvent, LogUtilites.getTimestamp(event).toDate());
					//						newEvent.getAttributes().put(EVENT_TYPE_KEY, 
					//								factory.createAttributeLiteral(EVENT_TYPE_KEY, EVENT_TYPE_GATE, null));
					//						newEvent.getAttributes().put(EVENT_STAGE_KEY, 
					//								factory.createAttributeLiteral(EVENT_STAGE_KEY, curStage, null));
					//						outTrace.add(newEvent);							
					//					}

					eventCount++;
					String curEventName = LogUtils.getConceptName(event);
					String curEventStatus = LogUtils.getLifecycleTransition(event).toLowerCase();
					boolean isActivityEvent = curEventName.startsWith("W_");

					//---------------------------------------
					// Add events to the current stage
					//---------------------------------------				
					String newEventName = curStage + "_" + curEventName;
					LogUtils.setConceptName(event, newEventName);
					if (isActivityEvent) {
						event.getAttributes().put(EVENT_TYPE_KEY,
								factory.createAttributeLiteral(EVENT_TYPE_KEY, EVENT_TYPE_ACT, null));
					} else {
						event.getAttributes().put(EVENT_TYPE_KEY,
								factory.createAttributeLiteral(EVENT_TYPE_KEY, EVENT_TYPE_GATE, null));
					}
					event.getAttributes().put(EVENT_STAGE_KEY,
							factory.createAttributeLiteral(EVENT_STAGE_KEY, curStage, null));
					outTrace.add(event);

					//---------------------------------------
					// Check the order of the stage movement if this event marks 
					// the end of the current stage.
					//---------------------------------------	
					// Expect A_PREACCEPTED
					if (curStage.equals(STAGE1)
							&& (".A_ACCEPTED.O_SENT_BACK.A_ACTIVATED.".indexOf("." + curEventName + ".") >= 0)) {
						fileWriter.write("Invalid Stage Order. TraceID:" + curTraceID + ". Current Stage: " + curStage
								+ ". Current Event: " + curEventName + newline);
					}
					// Expect A_ACCEPTED
					else if (curStage.equals(STAGE2)
							&& (".A_PREACCEPTED.O_SENT_BACK.A_ACTIVATED.".indexOf("." + curEventName + ".") >= 0)) {
						fileWriter.write("Invalid Stage Order. TraceID:" + curTraceID + ". Current Stage: " + curStage
								+ ". Current Event: " + curEventName + newline);
					}
					// Expect O_SENT_BACK
					else if (curStage.equals(STAGE3)
							&& (".A_PREACCEPTED.A_ACCEPTED.A_ACTIVATED.".indexOf("." + curEventName + ".") >= 0)) {
						fileWriter.write("Invalid Stage Order. TraceID: " + curTraceID + ". Current Stage: " + curStage
								+ ". Current Event: " + curEventName + newline);
					}
					// Expect A_ACTIVATED
					else if (curStage.equals(STAGE4)
							&& (".A_PREACCEPTED.A_ACCEPTED.O_SENT_BACK".indexOf("." + curEventName + ".") >= 0)) {
						fileWriter.write("Invalid Stage Order. TraceID: " + curTraceID + ". Current Stage: " + curStage
								+ ". Current Event: " + curEventName + newline);
					}

					//---------------------------------------
					// Check whether a stage is about to end
					//---------------------------------------	
					if (".A_PREACCEPTED.A_ACCEPTED.A_ACTIVATED.".indexOf("." + curEventName + ".") >= 0) {
						stageAboutToEnd = true;
					} else if ((lastSendBack != null) && (event == lastSendBack)) {
						stageAboutToEnd = true;
					}

					//---------------------------------------
					// Check if the latest activity of the current stage has completed or not
					// Because its start event may occur before a gate-based event
					// while the complete event may occur after. We have to wait until
					// the complete event to move to the next stage
					//---------------------------------------	
					if (curEventName.startsWith("W_")) {
						if (curEventStatus.equals("start")) {
							previousActEventCompleted = false;
						} else if (curEventStatus.equals("complete")) {
							previousActEventCompleted = true;
						}
					}

					//---------------------------------------
					// Make a stage transition: the condition of transition
					// is a stage is about to end and the latest activity of current stage
					// has completed. If it has not completed, we need to 
					// wait until the complete event.
					//---------------------------------------
					if (stageAboutToEnd && previousActEventCompleted) {
						if (curStage.equals(STAGE1)) {
							curStage = STAGE2;
						} else if (curStage.equals(STAGE2)) {
							curStage = STAGE3;
						} else if (curStage.equals(STAGE3)) {
							curStage = STAGE4;
						} else if (curStage.equals(STAGE4)) {
							curStage = STAGE5;
						}

						if (eventCount != trace.size()) {

							/*
							 * newEventName = curStage + "_START"; XEvent
							 * newEvent = (XEvent)event.clone();
							 * LogUtilites.setConceptName(newEvent,
							 * newEventName); LogUtilites.setTimestamp(newEvent,
							 * LogUtilites.getTimestamp(event).toDate());
							 * newEvent.getAttributes().put(EVENT_TYPE_KEY,
							 * factory.createAttributeLiteral(EVENT_TYPE_KEY,
							 * EVENT_TYPE_GATE, null));
							 * newEvent.getAttributes().put(EVENT_STAGE_KEY,
							 * factory.createAttributeLiteral(EVENT_STAGE_KEY,
							 * curStage, null)); outTrace.add(newEvent);
							 */

							stageAboutToEnd = false;
						}

						//previousActEventCompleted = true;
						//stageAboutToEnd = false;
					}

					//---------------------------------------
					// Check the relevant ending status if the current event
					// is one of exit events
					//---------------------------------------					
					if (curEventName.equals("A_DECLINED")) {
						outTrace.getAttributes().put(STATUS_KEY,
								factory.createAttributeLiteral(STATUS_KEY, STATUS_DECLINED, null));
						isCaseExited = true;
					} else if (curEventName.equals("A_CANCELLED")) {
						outTrace.getAttributes().put(STATUS_KEY,
								factory.createAttributeLiteral(STATUS_KEY, STATUS_CANCELLED, null));
						isCaseExited = true;
					} else if (curEventName.equals("A_ACTIVATED")) {
						outTrace.getAttributes().put(STATUS_KEY,
								factory.createAttributeLiteral(STATUS_KEY, STATUS_COMPLETED, null));
						isCaseCompleted = true;
					}
					//					else if (eventCount == trace.size() && curEventStatus.equals("start")) {
					else if ((eventCount == trace.size()) && isActivityEvent && !isCaseExited && !isCaseCompleted) {
						outTrace.getAttributes().put(STATUS_KEY,
								factory.createAttributeLiteral(STATUS_KEY, STATUS_IN_PROGRESS, null));
					}

					//---------------------------------------
					// Check if there is an activity after any exit event
					//---------------------------------------
					boolean newActivityStarted = (curEventStatus.equals("start") && isActivityEvent);
					boolean isCompletedEventGroup = (".O_ACCEPTED.A_REGISTERED.A_APPROVED.A_ACTIVATED.".indexOf("."
							+ curEventName + ".") >= 0);
					boolean isDeclinedOrCancelled = (".A_DECLINED.A_CANCELLED.O_DECLINED.O_CANCELLED.".indexOf("."
							+ curEventName + ".") >= 0);
					if (isCaseExited && ((!isActivityEvent && !isDeclinedOrCancelled) || newActivityStarted)) {
						fileWriter.write("There are active events after a declined or cancelled event. TraceID:"
								+ curTraceID + ". Current Stage: " + curStage + ". Current Event: " + curEventName
								+ newline);
					} else if (isCaseCompleted && ((!isActivityEvent && !isCompletedEventGroup) || newActivityStarted)) {
						fileWriter.write("There are active events after a completed event. TraceID:" + curTraceID
								+ ". Current Stage: " + curStage + ". Current Event: " + curEventName + newline);
					}
				}

				outputLog.add(outTrace);

			}

			//-----------------------------------------
			// Write to the output log
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlSerializer();
			fileWriter.write("Write to log file: " + args[1] + newline);
			String outputFile = System.getProperty("user.dir") + "\\" + args[1];
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(outputLog, fs);

			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
