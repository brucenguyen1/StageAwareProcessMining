package org.processmining.spm.stagemining.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.joda.time.DateTime;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

public class CreateLogBPI2015 {
	/**
	 * Note that events in the log has been sorted by timestamp.
	 * @param args
	 *            1st param: name of the input log 
	 *            2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + "BPIC15_1_HOOFD.xes");
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

			//------------------------------------------
			// Clean the original log
			//------------------------------------------
			System.out.println("CLEAN THE ORIGINAL LOG");
			for (XTrace trace : inputLog) {
				//				outTrace = new XTraceImpl(trace.getAttributes());
				// String traceID = trace.getAttributes().get("activityNameEN").toString().toLowerCase();
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("xxxxxxxx Trace: " + traceID + " xxxxxxxxxx");
				outTrace = factory.createTrace(trace.getAttributes());
				LogUtils.setConceptName(outTrace, traceID);

				SortedSet<XEvent> eventSortedSet = new TreeSet<XEvent>( //to sort the nodes by their min-cut values
		                new Comparator<XEvent>() {
		                    @Override
		                    public int compare(XEvent e1, XEvent e2) {
		                    	String code1 = e1.getAttributes().get("action_code").toString();
		                    	String code2 = e2.getAttributes().get("action_code").toString();
		                    	if (code1.compareToIgnoreCase(code2) < 0) {
		                    		return -1;
		                    	}
		                    	else {
		                    		return 1;
		                    	}
		                    }
		                });
				
				int counter = 0;
				XEvent preEvent = null;
				for (XEvent event : trace) {
					counter++;
					String eventName = event.getAttributes().get("activityNameEN").toString().toLowerCase();
					String eventConcept = LogUtils.getConceptName(event).toLowerCase();
					if (eventConcept.contains("01_hoofd_6")) continue; 
					if (eventConcept.contains("01_hoofd_7")) continue; 
					
					//LogUtils.setConceptName(event, eventName);
					System.out.println("Event: " + eventName);
					DateTime eventTimestamp = LogUtils.getTimestamp(event);
					
					if (preEvent == null || eventTimestamp.isEqual(LogUtils.getTimestamp(preEvent))) {
						eventSortedSet.add(event);
					}
					else { // When the timestamp changes
						for (XEvent e : eventSortedSet) {
							outTrace.add(e);
						}
						eventSortedSet.clear();
						eventSortedSet.add(event);
					}
					
					// When hit the last event of the trace
					if (counter == trace.size() && !eventSortedSet.isEmpty()) { 
						for (XEvent e : eventSortedSet) {
							outTrace.add(e);
						}
						eventSortedSet.clear();
					}
					
					preEvent = event;
				}
				outputLog.add(outTrace);
			}

			//-----------------------------------------
			// Write the final log to the output file
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlSerializer();
			String outputFile = System.getProperty("user.dir") + "\\" + "BPIC15_1_HOOFD_cleantime.xes";
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(outputLog, fs);
			System.out.println("DONE!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
