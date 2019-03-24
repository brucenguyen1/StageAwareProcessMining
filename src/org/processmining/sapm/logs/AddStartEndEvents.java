package org.processmining.sapm.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class AddStartEndEvents {
	public static void main(String[] args) {
		String fileName = args[0];
		try {
			System.out.println("Import log file");
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + fileName));
			
			System.out.println("Add start and end events to every trace");
			XLog newLog = LogUtils.addStartEndEvents(log, "startevent", "endevent");
			
			System.out.println("Write new log to XES file");
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + 
														fileName + "_add_endpoints.xes.gz");

			writer.serialize(newLog, fos);
			fos.close();
			
			System.out.println("DONE!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
