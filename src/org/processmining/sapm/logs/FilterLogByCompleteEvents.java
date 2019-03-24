package org.processmining.sapm.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class FilterLogByCompleteEvents {
	public static void main(String[] args) {
		String fileName = args[0];
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + fileName));
			
			System.out.println("Copy to new log with complete events only");
			XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			
			for (XTrace trace : log) {
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent e : trace) {
					if (LogUtils.getLifecycleTransition(e).toLowerCase().equals("complete")) {
						newTrace.add(e);
					}
				}
				if (!newTrace.isEmpty() ) {
					newLog.add(newTrace);
				}
			}
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + 
														fileName + "_complete_events_only.xes.gz");
			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
