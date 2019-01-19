package org.processmining.perspectivemining.log.datapreparation;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.perspectivemining.ui.fakecontext.FakePluginContext;
import org.processmining.perspectivemining.utils.LogUtils;
import org.processmining.perspectivemining.utils.OpenLogFilePlugin;

public class BPIC13_add_helpdeskline {
	public static void main(String[] args) {
		
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log;
			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "BPIC13_incidents_orgline_C.xes"));
			XFactory factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("TraceID: " + traceID);
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent event: trace) {
					String groupname = LogUtils.getValue(event.getAttributes().get("org:group")).toString();
					if (groupname.contains("2nd")) { 
						event.getAttributes().put("line", factory.createAttributeLiteral("line", "2nd", null));
					}
					else if (groupname.contains("3rd")) {
						event.getAttributes().put("line", factory.createAttributeLiteral("line", "3rd", null));
					}
					else {
						event.getAttributes().put("line", factory.createAttributeLiteral("line", "1st", null));
					}
					newTrace.add(event);
				}
				newLog.add(newTrace);
			}
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + "BPIC13_incidents_orgline_cases_C_only_20.01_23.05.xes.gz");
			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
