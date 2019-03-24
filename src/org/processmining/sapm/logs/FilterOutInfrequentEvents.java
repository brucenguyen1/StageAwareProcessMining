package org.processmining.sapm.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class FilterOutInfrequentEvents {
	/**
	 * Assume that log only contains complete events
	 * @param args
	 */
	public static void main(String[] args) {
		String fileName = args[0];
		double threshold = Double.valueOf(args[1]);
		
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + fileName));
			
			System.out.println("Filter infrequent events");
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
			int totalEvents = logInfo.getNumberOfEvents();
			
			Map<String,Double> mapEvents = new HashMap<>();
			for (XTrace trace : log) {
				for (XEvent e : trace) {
					String eName = LogUtils.getConceptName(e).toLowerCase();
					if (mapEvents.containsKey(eName)) {
						mapEvents.put(eName, mapEvents.get(eName)+1);
					}
					else {
						mapEvents.put(eName, 0.0);
					}
				}
			}
			for (String eName : mapEvents.keySet()) {
				mapEvents.put(eName, 1.0*mapEvents.get(eName)/totalEvents);
			}
			
			XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			for (XTrace trace : log) {
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent e : trace) {
					String eName = LogUtils.getConceptName(e).toLowerCase();
					if (mapEvents.get(eName) >= threshold) newTrace.add(e);
				}
				if (newTrace.size() > 0) newLog.add(newTrace);
			}
			
			System.out.println("Write new log to XES file");
			DecimalFormat df = new DecimalFormat("#.####");
			String newFile = System.getProperty("user.dir") + File.separator + fileName + "_filterEvents_" +
					df.format(threshold) + ".xes.gz";
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(newFile);
			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
