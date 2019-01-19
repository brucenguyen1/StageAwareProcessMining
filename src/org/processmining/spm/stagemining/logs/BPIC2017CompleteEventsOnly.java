package org.processmining.spm.stagemining.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

public class BPIC2017CompleteEventsOnly {
	public static void main(String[] args) {
		System.out.println("Import log file");
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		XLog log;
		try {
			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "BPIC2017_applications.xes.gz"));
			XFactory factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("TraceID: " + traceID);
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent e : trace) {
					if (LogUtils.getLifecycleTransition(e).equalsIgnoreCase("complete")) {
						newTrace.add(e);
					}
				}
				newLog.add(newTrace);
			}
			
			//-----------------------------------------
			// Write to the output log
			//-----------------------------------------
			XesXmlGZIPSerializer writer = new XesXmlGZIPSerializer();
			String outputFile = System.getProperty("user.dir") + File.separator + "BPIC2017_completeevents.xes.gz";
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(newLog, fs);
			System.out.println("DONE!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
