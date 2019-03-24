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

public class CreateLogBPI2015_HOOFD {
	/**
	 * Note that events in the log has been sorted by timestamp.
	 * @param args
	 *            1st param: name of the input log 
	 *            2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + "BPIC15_1.xes");
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
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("xxxxxxxx Trace: " + traceID + " xxxxxxxxxx");
				
				outTrace = factory.createTrace(trace.getAttributes());
				LogUtils.setConceptName(outTrace, traceID);

				for (XEvent event : trace) {
//					String eventName = event.getAttributes().get("action_code").toString().toLowerCase();
					String eventConcept = LogUtils.getConceptName(event).toLowerCase();
					if (eventConcept.contains("01_hoofd_6")) continue; 
					if (eventConcept.contains("01_hoofd_7")) continue; 
					if (!eventConcept.contains("hoofd")) continue;
					outTrace.add(event);
				}
				outputLog.add(outTrace);
			}

			//-----------------------------------------
			// Write the final log to the output file
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlSerializer();
			String outputFile = System.getProperty("user.dir") + "\\" + "BPIC15_1_HOOFD.xes";
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(outputLog, fs);
			System.out.println("DONE!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
