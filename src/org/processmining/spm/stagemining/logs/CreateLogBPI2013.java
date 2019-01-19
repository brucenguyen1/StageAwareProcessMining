package org.processmining.spm.stagemining.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

public class CreateLogBPI2013 {
	public static final String EVENT_STAGE_KEY = "org:line";
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
	 * This class is used first, then
	 * 
	 * @param args
	 *            1st param: name of the input log 
	 *            2nd param: name of the output
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
			inputLog = (XLog) logImporter.importFile(new FakePluginContext(), inputFile);
			//    		outputLog = new XLogImpl(inputLog.getAttributes());
			XFactory factory = new XFactoryNaiveImpl();
			outputLog = factory.createLog(inputLog.getAttributes());

			for (XTrace trace : inputLog) {
				//				outTrace = new XTraceImpl(trace.getAttributes());
				outTrace = factory.createTrace(trace.getAttributes());
				XEvent preevent = null;
				int counter = 0;
				for (XEvent event : trace) {
					counter++;
					
//					LogUtils.getValue(event.getAttributes().get("org:line")).toLowerCase();
//					String transitionType = LogUtils.getLifecycleTransition(event).toLowerCase();
//					LogUtils.getConceptName(event).toLowerCase();

					// Check the current stage
					String currentGroup = LogUtils.getValue(event.getAttributes().get("org:group")).toLowerCase();
					String currentStage = "";
					if (currentGroup.contains("2nd")) {
						currentStage = "2nd";
					} else if (currentGroup.contains("3rd")) {
						currentStage = "3rd";
					} else {
						currentStage = "1st";
					}
					
					// Update the current event name for the current stage
					//String currentEventName = LogUtils.getConceptName(event).toLowerCase();
					String currentLifecycle = LogUtils.getLifecycleTransition(event).toLowerCase();
					LogUtils.setConceptName(event, currentStage + "_" + currentLifecycle);
					event.getAttributes().put("action", factory.createAttributeLiteral("action", currentLifecycle, null));
					LogUtils.setLifecycleTransition(event, "complete");
					
					// Check if stage change, add a milestone event 
					// to mark the end of the previous stage
//					if (preevent != null) {
//						String preGroup = LogUtils.getValue(preevent.getAttributes().get("org:group")).toLowerCase();
//						String preStage = "";
//						if (preGroup.contains("2nd")) {
//							preStage = "2nd";
//						} else if (preGroup.contains("3rd")) {
//							preStage = "3rd";
//						} else {
//							preStage = "1st";
//						}
//						if (!currentStage.equals(preStage)) { 
//							XEvent milestone = factory.createEvent();
//							LogUtils.setConceptName(milestone, preStage + "_milestone");
//							LogUtils.setLifecycleTransition(milestone, "complete");
//							LogUtils.setTimestamp(milestone, LogUtils.getTimestamp(preevent).plusSeconds(1).toDate());
//							outTrace.add(milestone);
//						}
//					}

					outTrace.add(event);
					preevent = event;
					
//					if (counter == trace.size()) {
//						XEvent milestone = factory.createEvent();
//						LogUtils.setConceptName(milestone, currentStage + "_milestone");
//						LogUtils.setLifecycleTransition(milestone, "complete");
//						LogUtils.setTimestamp(milestone, LogUtils.getTimestamp(event).plusSeconds(1).toDate());
//						outTrace.add(milestone);
//					}
				}
				outputLog.add(outTrace);
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

}
