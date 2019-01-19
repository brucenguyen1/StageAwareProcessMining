package org.processmining.spm.stagemining.logs;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

/*
 * In the BPIC12 enhanced log, there are a number of canceled and 
 * declined cases in which there is a complete workflow event occurring
 * after the cancel or decline event. This is not right  
 */
public class BPIC12_FixLastWorkflowActivityInCanceledCases {
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\BPI2012_enhanced.xes.gz");
		XLog inputLog;
		XLog log1, log2, log3;
		XTrace outTrace;

		try {
			//-----------------------------------------
			// Read input log and select events for output log
			//-----------------------------------------    		
			inputLog = (XLog) logImporter.importFile(new FakePluginContext(), inputFile);
			//    		outputLog = new XLogImpl(inputLog.getAttributes());
			XFactory factory = new XFactoryNaiveImpl();
			
			
			// Take complete events only
			log1 = factory.createLog(inputLog.getAttributes());
			for (XTrace trace : inputLog) {
				outTrace = factory.createTrace(trace.getAttributes());
				for (XEvent event : trace) {
					boolean takeEvent = true;
					String lifecycle = LogUtils.getLifecycleTransition(event);
					if (!lifecycle.equalsIgnoreCase("complete")) takeEvent = false;
					if (takeEvent) outTrace.add(event);
				}
				log1.add(outTrace);
			}

			// Remove incomplete cases
			log2 = factory.createLog(inputLog.getAttributes());
			for (XTrace trace : log1) {
				outTrace = factory.createTrace(trace.getAttributes());
				
				boolean s1_milestone = false;
				boolean s2_milestone = false;
				boolean s3_milestone = false;
				boolean s4_milestone = false;
				
				boolean takeTrace = true;
				for (XEvent event : trace) {
					
					String lifecycle = LogUtils.getLifecycleTransition(event);
					String eventName = LogUtils.getConceptName(event);
					int eventStage = Integer.valueOf(eventName.substring(1, 2));
					int eventIndex = trace.indexOf(event);
					
					if (eventName.equalsIgnoreCase("S1_A_PREACCEPTED") ||
							eventName.equalsIgnoreCase("S1_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S1_A_DECLINED")) {
						s1_milestone = true;
					}
					
					if (eventName.equalsIgnoreCase("S2_A_ACCEPTED") ||
							eventName.equalsIgnoreCase("S2_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S2_A_DECLINED")) {
						s2_milestone = true;
					}
					
					if (eventName.equalsIgnoreCase("S3_O_SENT_BACK") ||
							eventName.equalsIgnoreCase("S3_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S3_A_DECLINED")) {
						s3_milestone = true;
					}
					
					if (eventName.equalsIgnoreCase("S4_A_APPROVED") ||
							eventName.equalsIgnoreCase("S4_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S4_A_DECLINED")) {
						s4_milestone = true;
					}

					// Incomplete trace
					if (eventIndex == trace.size()-1 && (eventName.contains("W_") || eventName.equalsIgnoreCase("S3_O_SENT"))) {
						if (s4_milestone && eventStage > 4) {
							takeTrace = false;
						}
						else if (s3_milestone && eventStage > 3) {
							takeTrace = false;
						}
						else if (s2_milestone && eventStage > 2) {
							takeTrace = false;
						}
						else if (s1_milestone && eventStage > 1) {
							takeTrace = false;
						}
					}
					
					outTrace.add(event);
					
				}
				
				if (takeTrace) log2.add(outTrace);
			}

			// Fix improper ending worklow events
			// Move the milestone event to the end
			int currentStage = 1;
			log3 = factory.createLog(inputLog.getAttributes());
			for (XTrace trace : log2) {
				outTrace = factory.createTrace(trace.getAttributes());
				
				XEvent s1_milestone=null, s2_milestone=null, s3_milestone=null, s4_milestone=null;
				
				for (XEvent event : trace) {
					String lifecycle = LogUtils.getLifecycleTransition(event);
					String eventName = LogUtils.getConceptName(event);
					int eventStage = Integer.valueOf(eventName.substring(1, 2));
					int eventIndex = trace.indexOf(event);
					
					int nextStage = 0;
					if (eventIndex < trace.size()-1) {
						nextStage = Integer.valueOf(LogUtils.getConceptName(trace.get(eventIndex+1)).substring(1, 2));
					}
					else {
						nextStage = eventStage + 1;
					}
					
					if (eventName.equalsIgnoreCase("S1_A_PREACCEPTED") ||
							eventName.equalsIgnoreCase("S1_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S1_A_DECLINED")) {
						s1_milestone = event;
					}
					
					if (eventName.equalsIgnoreCase("S2_A_ACCEPTED") ||
							eventName.equalsIgnoreCase("S2_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S2_A_DECLINED")) {
						s2_milestone = event;
					}
					
					if (eventName.equalsIgnoreCase("S3_O_SENT_BACK") ||
							eventName.equalsIgnoreCase("S3_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S3_A_DECLINED")) {
						s3_milestone = event;
					}
					
					if (eventName.equalsIgnoreCase("S4_A_APPROVED") ||
							eventName.equalsIgnoreCase("S4_A_CANCELLED") ||
							eventName.equalsIgnoreCase("S4_A_DECLINED")) {
						s4_milestone = event;
					}
					
					
					// Add workflow event to trace
					if (eventName.contains("W_")) {
						outTrace.add(event);
					}

					// Add milestone event
					if (eventStage == 1 && eventStage != nextStage) {
						if (s1_milestone != null) {
							outTrace.add(s1_milestone);
						}
						else {
							System.err.println("Missing milestone. Trace:" + LogUtils.getConceptName(trace) + " at stage " + eventStage);
						}
					}
					else if (eventStage == 2 && eventStage != nextStage) {
						if (s2_milestone != null) {
							outTrace.add(s2_milestone);
						}
						else {
							System.err.println("Missing milestone. Trace:" + LogUtils.getConceptName(trace) + " at stage " + eventStage);
						}
					}
					else if (eventStage == 3 && eventStage != nextStage) {
						if (s3_milestone != null) {
							outTrace.add(s3_milestone);
						}
						else {
							System.err.println("Missing milestone. Trace:" + LogUtils.getConceptName(trace) + " at stage " + eventStage);
						}
					}
					else if (eventStage == 4 && eventStage != nextStage) {
						if (s4_milestone != null) {
							outTrace.add(s4_milestone);
						}
						else {
							System.err.println("Missing milestone. Trace:" + LogUtils.getConceptName(trace) + " at stage " + eventStage);
						}
					}
				}
				
				log3.add(outTrace);
			}

			//-----------------------------------------
			// Write to the output log
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlGZIPSerializer();
			String outputFile = System.getProperty("user.dir") + File.separator + "BPI2012_enhanced_complete_only_clean.xes.gz";
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(log3, fs);
			System.out.println("DONE!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
