package org.processmining.sapm.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

/**
 * This class is used to clean log for running Genet
 * The event name must not contain these characters:
 * 	special characters: _, !@#$%&*()
 *  starting with numbers
 * @author Administrator
 *
 */
public class CleanLogForGenet {
	public static void main(String[] args) {
		CleanLogForGenet cleanner = new CleanLogForGenet();
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		    		String fileName = filePath.getFileName().toString();
		    		System.out.println("---------------------------------------------");
		    		System.out.println(fileName);
		    		cleanner.clean(filePath);
		        }
		    });
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	public static void clean(Path filePath) {
		// Import Log
		System.out.println("Import log file");
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		try {
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(filePath.toString()));
			
			System.out.println("Clean event names");
			String logName = LogUtils.getConceptName(log).toString();
			XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
			XLog newLog = factory.createLog(log.getAttributes());
			newLog.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
			
			//key: old event name, value: new event name
			Map<String,String> mapping = new HashMap<>();
			
			for (XTrace trace : log) {
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent e : trace) {
					String eventName = LogUtils.getConceptName(e);
					String oldEventName = eventName;
					
					if (mapping.containsKey(oldEventName)) {
						eventName = mapping.get(oldEventName);
					}
					else {
						if (eventName.contains(" ")) {
							eventName = eventName.replaceAll(" ", "");
						}						
						if (eventName.contains("_")) {
							eventName = eventName.replaceAll("_", "x");
						}
						if (eventName.contains("@")) {
							eventName = eventName.replaceAll("@", "x");
						}
						if (eventName.contains("\\")) {
							eventName = eventName.replaceAll("\\", "x");
						}
						if (eventName.contains("'")) {
							eventName = eventName.replaceAll("'", "x");
						}
						if (eventName.contains("`")) {
							eventName = eventName.replaceAll("`", "x");
						}
						if (eventName.contains("\"")) {
							eventName = eventName.replaceAll("\"", "x");
						}
						if (eventName.contains("%")) {
							eventName = eventName.replaceAll("%", "x");
						}
						if (eventName.contains("$")) {
							eventName = eventName.replaceAll("$", "x");
						}
						if (eventName.contains("^")) {
							eventName = eventName.replaceAll("^", "x");
						}
						if (eventName.contains("&")) {
							eventName = eventName.replaceAll("&", "x");
						}
						if (eventName.contains("*")) {
							eventName = eventName.replaceAll("*", "x");
						}
						if (eventName.contains("(")) {
							eventName = eventName.replaceAll("\\(", "x");
						}
						if (eventName.contains(")")) {
							eventName = eventName.replaceAll("\\)", "x");
						}
						if (eventName.contains("-")) {
							eventName = eventName.replaceAll("-", "x");
						}
						if (eventName.contains("+")) {
							eventName = eventName.replaceAll("+", "x");
						}
						if (eventName.contains("=")) {
							eventName = eventName.replaceAll("=", "x");
						}
						if (eventName.contains("[")) {
							eventName = eventName.replaceAll("[", "x");
						}
						if (eventName.contains("]")) {
							eventName = eventName.replaceAll("]", "x");
						}
						if (eventName.contains("{")) {
							eventName = eventName.replaceAll("{", "x");
						}
						if (eventName.contains("}")) {
							eventName = eventName.replaceAll("}", "x");
						}
						if (eventName.contains(":")) {
							eventName = eventName.replaceAll(":", "x");
						}
						if (eventName.contains(";")) {
							eventName = eventName.replaceAll(";", "x");
						}
						if (eventName.contains("?")) {
							eventName = eventName.replaceAll("?", "x");
						}
						if (eventName.contains("/")) {
							eventName = eventName.replaceAll("/", "x");
						}
						if (eventName.contains("<")) {
							eventName = eventName.replaceAll("<", "x");
						}
						if (eventName.contains(">")) {
							eventName = eventName.replaceAll(">", "x");
						}
						if (eventName.contains(",")) {
							eventName = eventName.replaceAll(",", "x");
						}
						if (eventName.contains(".")) {
							eventName = eventName.replaceAll(".", "x");
						}
						if ("0123456789".contains(eventName.substring(0,1))) {
							eventName = "X" + eventName;
						}
						
						if (!oldEventName.equals(eventName)) {
							mapping.put(oldEventName, eventName);
						}
					}
					
					XEvent newEvent = factory.createEvent(e.getAttributes());
					LogUtils.setConceptName(newEvent, eventName);
					newTrace.add(newEvent);
					
				} // each trace
				newLog.add(newTrace);
			} // end log
			
			//Write mapping to file
//			System.out.println("Write mapping file");
//			if (!mapping.isEmpty()) {
//				FileWriter fileWriter = new FileWriter(new File(System.getProperty("user.dir") + File.separator + 
//															args[0] + "_mapping.txt"));
//				for (String oldName : mapping.keySet()) {
//					fileWriter.write(oldName + " -> " + mapping.get(oldName) + "\n");
//				}
//				fileWriter.flush();
//				fileWriter.close();
//			}
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + "logsforgenet" + 
											File.separator + filePath.getFileName() + "_genet.xes.gz");
			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
