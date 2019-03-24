package org.processmining.sapm.stagemining.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

/*
 * This class processes the BPIC12 with stage enhancement
 * used in the Performance Mining With Staged Process Flows
 */
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
			String currentStage = "";
			
			for (XTrace trace : inputLog) {
				outTrace = factory.createTrace(trace.getAttributes());
				XEvent preevent = null;
				for (XEvent event : trace) {
					String currentEventName = LogUtils.getConceptName(event);
					if (!currentEventName.contains("_W_") && 
						!currentEventName.contains("S1_A_SUBMITTED") &&
						!currentEventName.contains("S1_A_PARTLYSUBMITTED")) continue; //jump over milestone events
					
					// Check the current stage
					currentStage = "";
					if (currentEventName.contains("S1")) { //for S1: check also milestone events
						currentStage = "S1";
					} else if (currentEventName.contains("S2_W")) {
						currentStage = "S2";
					} else if (currentEventName.contains("S3_W")) {
						currentStage = "S3";
					} else if (currentEventName.contains("S4_W")) {
						currentStage = "S4";
					}
					else {
						throw new Exception("Unrecognized current stage.");
					}
					
					// Check if stage change, add a milestone event 
					// to mark the end of the previous stage
					if (preevent != null) {
						String preEventName = LogUtils.getConceptName(preevent);
						String preStage = "";
						if (preEventName.contains("S1")) { //for S1: check also milestone events
							preStage = "S1";
						} else if (preEventName.contains("S2_W")) {
							preStage = "S2";
						} else if (preEventName.contains("S3_W")) {
							preStage = "S3";
						} else if (preEventName.contains("S4_W")) {
							preStage = "S4";
						}
						else {
							throw new Exception("Unrecognized previous stage.");
						}
						
						if (!currentStage.equals(preStage)) { 
							XEvent milestone = factory.createEvent();
							LogUtils.setConceptName(milestone, preStage + "_decision");
							LogUtils.setLifecycleTransition(milestone, "complete");
							LogUtils.setTimestamp(milestone, LogUtils.getTimestamp(preevent).plusSeconds(1).toDate());
							outTrace.add(milestone);
						}
					}

					outTrace.add(event);
					preevent = event;
				}
				
				XEvent milestone = factory.createEvent();
				LogUtils.setConceptName(milestone, currentStage + "_decision");
				LogUtils.setLifecycleTransition(milestone, "complete");
				LogUtils.setTimestamp(milestone, LogUtils.getTimestamp(preevent).plusSeconds(1).toDate());
				outTrace.add(milestone);
				
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
