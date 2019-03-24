package org.processmining.sapm.perspectivemining.log.datapreparation;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.perspectivemining.ui.fakecontext.FakePluginContext;
import org.processmining.sapm.perspectivemining.utils.LogUtils;
import org.processmining.sapm.perspectivemining.utils.OpenLogFilePlugin;

public class BPIC13_cases_with_A2_only {
	public static void main(String[] args) {
		
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log;
			log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "BPIC13_incidents.xes"));
			XFactory factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				String traceID = LogUtils.getConceptName(trace);
				System.out.println("TraceID: " + traceID);
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				boolean accept = true;
				for (XEvent event: trace) {
					String orgline = LogUtils.getValue(event.getAttributes().get("organization involved")).toString();
					if (!orgline.equalsIgnoreCase("Org line A2")) { // change between A2 and C
						accept = false;
						break;
					}	
					
					String status = LogUtils.getValue(event.getAttributes().get("lifecycle:transition")).toString();
					event.getAttributes().put("status", factory.createAttributeLiteral("status", status, null));
					event.getAttributes().put("lifecycle:transition", factory.createAttributeLiteral("lifecycle:transition", "complete", null));
				}
				if (accept) newLog.add(trace);
			}
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + "BPIC13_incidents_orgline_cases_A2_only.xes.gz");
			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
