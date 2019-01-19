package org.processmining.stagedprocessflows.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.stagedprocessflows.util.LogUtils;
import org.processmining.stagedprocessflows.util.OpenLogFilePlugin;

public class CreateLogBPI2013IncidentAddLineStage {
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
	 * CreateLogBPI2013IncidentAssignEventToStage, then
	 * CreateLogBPI2013IncidentCaseAttribute
	 * 
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
				outTrace = factory.createTrace(trace.getAttributes());
				XEvent preevent = null;
				for (XEvent event : trace) {

					LogUtils.getValue(event.getAttributes().get("org:line")).toLowerCase();
					String transitionType = LogUtils.getLifecycleTransition(event).toLowerCase();
					LogUtils.getConceptName(event).toLowerCase();

					//---------------------------------
					// Determine the line
					//---------------------------------
					String group = LogUtils.getValue(event.getAttributes().get("org:group")).toLowerCase();
					String groupLine = "";
					if (group.contains("2nd")) {
						groupLine = "2nd";
					} else if (group.contains("3rd")) {
						groupLine = "3rd";
					} else {
						groupLine = "1st";
					}
					event.getAttributes().put("org:line", factory.createAttributeLiteral("org:line", groupLine, null));

					//---------------------------------
					// Determine the stage
					//---------------------------------
					String eventstage = "";
					if (transitionType.equals("awaiting assignment")) {
						if (trace.indexOf(event) == 0) {
							eventstage = "request";
						} else {
							if (preevent != null) {
								eventstage = LogUtils.getValue(preevent.getAttributes().get("stage")).toLowerCase();
							} else {
								eventstage = "unidentified";
							}
						}
					} else {
						eventstage = groupLine;
					}

					event.getAttributes().put("stage", factory.createAttributeLiteral("stage", eventstage, null));
					outTrace.add(event);

					preevent = event;
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
