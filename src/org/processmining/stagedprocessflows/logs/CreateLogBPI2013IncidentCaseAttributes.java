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

public class CreateLogBPI2013IncidentCaseAttributes {

	/**
	 * @param args
	 *            1st param: name of the input log 2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + args[0]);
		XLog inputLog;
		XLog outputLog;

		try {
			//-----------------------------------------
			// Read input log and select events for output log
			//-----------------------------------------    		
			inputLog = (XLog) logImporter.importFile(inputFile);
			XFactory factory = new XFactoryNaiveImpl();
			outputLog = factory.createLog(inputLog.getAttributes());

			for (XTrace trace : inputLog) {
				String impact = "";
				String country = "";
				String product = "";

				for (XEvent event : trace) {
					impact = LogUtils.getValue(event.getAttributes().get("impact")).toLowerCase();
					country = LogUtils.getValue(event.getAttributes().get("organization country")).toLowerCase();
					product = LogUtils.getValue(event.getAttributes().get("product")).toLowerCase();
					break;
				}

				trace.getAttributes().put("impact", factory.createAttributeLiteral("impact", impact, null));
				trace.getAttributes().put("country", factory.createAttributeLiteral("country", country, null));
				trace.getAttributes().put("product", factory.createAttributeLiteral("product", product, null));

				outputLog.add(trace);
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
