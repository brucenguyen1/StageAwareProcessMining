package org.processmining.stagedprocessflows.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.joda.time.DateTime;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.spm.stagemining.StageDecomposition;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFManager;
import org.processmining.stagedprocessflows.parameters.SPFConfig;
import org.processmining.stagedprocessflows.ui.wizard.WizardUI;
import org.processmining.stagedprocessflows.util.LogUtils;

/**
 * This is the main plugin to be installed in PROM
 * 
 * @author Hoang Nguyen
 * @date 21 Oct 2015
 * @since 21 Oct 2015
 * @version 1.0
 * @email huanghuy.nguyen@hdr.qut.edu.au
 * @copyright Hoang Nguyen BPM Discipline Queensland University of Technology
 */

public class SPFPlugin {

	@Plugin(name = "Performance Mining With Staged Process Flows", parameterLabels = { "Log"}, returnLabels = { "Staged Process Flows" }, returnTypes = { SPF.class }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, 
					author = "Hoang Nguyen", 
					email = "huanghuy.nguyen@hdr.qut.edu.au", 
					uiLabel = "Performance Mining With Staged Process Flows", 
					pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Log must contain stage information", requiredParameterLabels = { 0 })
	public static SPF create(UIPluginContext context, final XLog log) {

		SPFManager.getInstance().clear(); //start over a new BPF
		SPFConfig config = new SPFConfig();
		readData(log, config, SPFManager.getInstance());
		config.setContext(context);
		WizardUI bpfUI = new WizardUI(context, config);
		return bpfUI.mine(log);
	}
	
	@Plugin(name = "Performance Mining With Staged Process Flows 2", parameterLabels = { "Log","Stage Decomposition"}, returnLabels = { "Staged Process Flows" }, returnTypes = { SPF.class }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, 
					author = "Hoang Nguyen", 
					email = "huanghuy.nguyen@hdr.qut.edu.au",
					uiLabel = "Performance Mining With Staged Process Flows 2", 
					pack = "Stage-Aware Process Mining")
	@PluginVariant(variantLabel = "Mine flow performance from a log and a stage model", requiredParameterLabels = { 0,1 })
	public static SPF create(UIPluginContext context, final XLog log, final StageDecomposition stageModel) {
		List<String> completedActivities = null;
		CompleteEventSelectionPanel completeEventPanel = new CompleteEventSelectionPanel(context, log, stageModel);
		boolean checkInput = true;
		do {
			InteractionResult result = context.showConfiguration("Complete Event Selection", completeEventPanel);
			if (result.equals(InteractionResult.CANCEL)) {
				context.getFutureResult(0).cancel(true);
				return null;
			}
			completedActivities = completeEventPanel.getCompletedActivities();
			checkInput = (!completedActivities.isEmpty());
			if (!checkInput) {
				JOptionPane.showMessageDialog(null, "Please select at least one activity!");
			}
		} while (!checkInput);
		
		
		try {
			XLog stagedLog = createStagedLog(log, stageModel, completedActivities);
			SPFManager.getInstance().clear(); //start over a new BPF
			SPFConfig config = new SPFConfig();
			readData(stagedLog, config, SPFManager.getInstance());
			config.setContext(context);
			WizardUI bpfUI = new WizardUI(context, config);
			return bpfUI.mine(stagedLog);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
	
	private static XLog createStagedLog(XLog oriLog, StageDecomposition stageModel, List<String> completedActivities) throws Exception {
		XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
		XLog assignedLog = factory.createLog(oriLog.getAttributes());
		
		// Copy the original log to a new log, so the original log remains intact
		// Add an originalID attribute to all traces
		// Add a stage name attribute to all events
		for (XTrace trace : oriLog) {
			XTrace assignedTrace = factory.createTrace();
			String currentTraceID = LogUtils.getConceptName(trace);
    		LogUtils.setConceptName(assignedTrace, currentTraceID);
    		assignedTrace.getAttributes().put("originalID", factory.createAttributeLiteral("originalID", currentTraceID, null));
			for (XEvent event : trace) {
				XEvent newEvent = factory.createEvent();
				LogUtils.setConceptName(newEvent, LogUtils.getConceptName(event));
				LogUtils.setLifecycleTransition(newEvent, LogUtils.getLifecycleTransition(event));
				LogUtils.setTimestamp(newEvent, LogUtils.getTimestamp(event).toDate());			
				int stageIndex = stageModel.getStageListModel().findActualStageIndex(LogUtils.getConceptName(event));
				String stageName = "S" + stageIndex; //note that the stage index starts from 1
				newEvent.getAttributes().put("stage", factory.createAttributeLiteral("stage", stageName, null));
				assignedTrace.add(newEvent);
			}
			assignedLog.add(assignedTrace);
		}
		
		// Process traces when they are not compliant with the order of stages
		XLog stagedLog = factory.createLog(assignedLog.getAttributes());
		for (XTrace trace : assignedLog) {
			List<XTrace> processedTraces = processTrace(trace, stageModel, completedActivities);
			stagedLog.addAll(processedTraces);
		}		
		
		// Write to file for debug
		XesXmlSerializer writer = new XesXmlGZIPSerializer();
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + 
        							LogUtils.getConceptName(stagedLog) + "_staged.xes.gz");
        writer.serialize(stagedLog, fos);
        fos.close();
		
		return stagedLog;
	}
	
	/**
	 * Check a trace to be compliant with a stage model.
	 * For non-compliant situations, process the traces to create new traces
	 * @param inputTrace: the input trace. The trace has an originalID attribute containing its original TraceID. 
	 * Each event in the trace has a stage attribute with format "S" + stageindex, stageindex starts from 1.  
	 * @param numberOfStages: the number of stages
	 * @return: a list of new traces after being processed from the input trace
	 * @throws ParseException when converting stage index from string to integer
	 */
	public static List<XTrace> processTrace(XTrace inputTrace, StageDecomposition stageModel, List<String> completedActivities) throws ParseException {
		int numberOfStages = stageModel.getStageListModel().size();
		XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
		Map<String,Integer> mapTraceIDIndex = new HashMap<>(); //to keep an incremental counting postfix for each traceID 
		String originalID = LogUtils.getValue(inputTrace.getAttributes().get("originalID"));
		if (!mapTraceIDIndex.containsKey(originalID)) mapTraceIDIndex.put(originalID, 0);
		
		List<XTrace> unprocessedTraces = new ArrayList<>();
		unprocessedTraces.add(inputTrace); //starting with the input trace
		List<XTrace> processedTraces = new ArrayList<>();
		
		// Process traces until there are no more unprocessed traces
		while (!unprocessedTraces.isEmpty()) {
			XTrace currentTrace = unprocessedTraces.get(0);
			XTrace processedTrace = factory.createTrace(currentTrace.getAttributes()); 
			XTrace newTrace = null;
			
			// Process one trace
			for (int traceIndex=0;traceIndex < currentTrace.size(); traceIndex++) {
				XEvent event = currentTrace.get(traceIndex);
				int eventStageNo = Integer.valueOf(LogUtils.getValue(event.getAttributes().get("stage")).substring(1));
				
				// Found non-compliance at the start of the trace
	        	if (traceIndex==0 && eventStageNo > 1) { //stage index starts from 1
	        		for (int missingStageNo=1;missingStageNo<eventStageNo;missingStageNo++) {
	        			XEvent fakeEvent = createFakeEvent(event, missingStageNo);
	        			processedTrace.add(fakeEvent);
	        		}
	        	}
	        	
	        	// Add the current event
	        	processedTrace.add(event);
	        	
	        	// Found non-compliance in the middle of the trace
	        	if (traceIndex <= (currentTrace.size()-2)) {
	        		XEvent nextEvent = currentTrace.get(traceIndex+1); 
	        		int nexteventStageNo = Integer.valueOf(LogUtils.getValue(nextEvent.getAttributes().get("stage")).substring(1));
	        		
	        		if (eventStageNo < (nexteventStageNo - 1)) { // these's a jumping forward gap
	        			for (int missingStageNo = eventStageNo+1; missingStageNo < nexteventStageNo; missingStageNo++) { 
	        				XEvent fakeEvent = createFakeEvent(event, missingStageNo);
	        				processedTrace.add(fakeEvent);
	        			}
	        		}
	        		else if (eventStageNo > nexteventStageNo) { // there's a jumping backward gap
	        			// Process if the final stage is not reached
	        			// Create fake events to move the case to the last stage is
	        			// preferable because it will not create fake exit flows as
	        			// in the option of leaving case at this stage. 
//	        			if (eventStageNo < numberOfStages) {
//	        				for (int missingStageNo=eventStageNo+1; missingStageNo<=numberOfStages; missingStageNo++) {
//	    	        			XEvent fakeEvent = createFakeEvent(event, missingStageNo);
//	    	        			processedTrace.add(fakeEvent);
//	    	        		}
//	        			}
	        			
//	        			if (eventStageNo < numberOfStages) {
//	        				processedTrace.getAttributes().put("status", factory.createAttributeLiteral("status", "jumpback", null));
//	        			}
	        			
	        			processedTrace.getAttributes().put("status", factory.createAttributeLiteral("status", "jumpback", null));
	        			
	        			// Create a new trace from the remaining events and stop processing for the current trace
		        		newTrace = factory.createTrace();
		        		LogUtils.setConceptName(newTrace, originalID + "_" + (mapTraceIDIndex.get(originalID) + 1));
		        		mapTraceIDIndex.put(originalID, mapTraceIDIndex.get(originalID) + 1);
		        		newTrace.getAttributes().put("originalID", factory.createAttributeLiteral("originalID", originalID, null));
		        		for (int i=(traceIndex+1);i<currentTrace.size();i++) { //copy the remain of current trace
		        			newTrace.add(currentTrace.get(i));
		        		}
		        		unprocessedTraces.add(newTrace);
		        		break;
	        		}	        		
	        	}
	        	
	        	// Found non-compliance at the end of the trace
//	        	if (traceIndex == (currentTrace.size()-1) && eventStageNo < numberOfStages) {
//	        		for (int missingStageNo=eventStageNo+1; missingStageNo<=numberOfStages; missingStageNo++) {
//	        			XEvent fakeEvent = createFakeEvent(event, missingStageNo);
//	        			processedTrace.add(fakeEvent);
//	        		}
//	        		
//	        	}
	        
	        }
			
			processedTraces.add(processedTrace);
			unprocessedTraces.remove(currentTrace);
		}
		
		// Add casetype and status attributes for cases
		for (XTrace trace : processedTraces) {
			if (processedTraces.size() == 1) {
				trace.getAttributes().put("casetype", factory.createAttributeLiteral("casetype", "original", null));
			}
			else {
				trace.getAttributes().put("casetype", factory.createAttributeLiteral("casetype", "derived", null));
			}
			
			if (!trace.getAttributes().containsKey("status")) { //for those cases without a jumpback status
				XEvent lastEvent = trace.get(trace.size()-1);
	    		if (completedActivities.contains(LogUtils.getConceptName(lastEvent))) {
	    			trace.getAttributes().put("status", factory.createAttributeLiteral("status", "complete", null));
	    		}
	    		else {
	    			trace.getAttributes().put("status", factory.createAttributeLiteral("status", "exit", null));
	    		}
			}
    	}

		return processedTraces;
	}
	
	/**
	 * Create fake event to make trace compliant with stage order
	 * @param refEvent: the reference event to copy some attributes
	 * @param stageNumber: the missing stage number
	 * @return: a fake event
	 */
	public static XEvent createFakeEvent(XEvent refEvent, int stageNumber) {
		XFactory factory = new XFactoryNaiveImpl();
		String stageName = "S" + stageNumber;
		XEvent fakeEvent = factory.createEvent();
    	XAttributeMap eventMap = factory.createAttributeMap();
    	eventMap.put("concept:name", factory.createAttributeLiteral("concept:name", "fake_event", null));
    	eventMap.put("lifecycle:transition", factory.createAttributeLiteral("lifecycle:transition", "complete", null));
    	eventMap.put("time:timestamp", factory.createAttributeTimestamp("time:timestamp", LogUtils.getTimestamp(refEvent).toDate(), null));
    	eventMap.put("stage", factory.createAttributeLiteral("stage", stageName, null));
    	fakeEvent.setAttributes(eventMap);
    	return fakeEvent;
	}

	/**
	 * This method reads the log file and populate data to the Config and
	 * BPFManager object
	 * 
	 * @param log
	 * @param config
	 * @param bpfManager
	 */
	private static void readData(XLog log, SPFConfig config, SPFManager bpfManager) {
		new HashMap<String, Map<String, String>>();
		config.setXLog(log);

		for (XTrace trace : log) {
			DateTime traceStart = new DateTime(9999, 12, 1, 1, 0);
			DateTime traceEnd = new DateTime(0);

			//---------------------------------------
			// Populate the Config object
			//---------------------------------------
			for (XEvent event : trace) {
				String eventName = LogUtils.getConceptName(event).toLowerCase();
				String stage = LogUtils.getValue(event.getAttributes().get("stage")).toLowerCase();
				String transitiontype = LogUtils.getLifecycleTransition(event).toLowerCase();
				DateTime eventTime = LogUtils.getTimestamp(event);

				if (!config.getStageList().contains(stage)) {
					config.getStageList().add(stage);
				}
				
				
				if (eventName.equalsIgnoreCase("fake_event")) {
					if (!config.getFakeStages().contains(stage)) {
						config.getFakeStages().add(stage);
					}
				}
				
				if (!config.getEventStageMap().containsKey(eventName)) {
					config.getEventStageMap().put(eventName, stage);
				}

				if (traceStart.isAfter(eventTime)
						&& (transitiontype.equals("start") || transitiontype.equals("complete"))) {
					traceStart = eventTime;
				}

				if (traceEnd.isBefore(eventTime)
						&& (transitiontype.equals("start") || transitiontype.equals("complete"))) {
					traceEnd = eventTime;
				}
			}

			String traceStatus = LogUtils.getValue(trace.getAttributes().get("status")).toLowerCase();
			if (!config.getCaseStatusList().contains(traceStatus)) {
				config.getCaseStatusList().add(traceStatus);
			}

			//---------------------------------------
			// Populate the BPFManager object
			//---------------------------------------
			String curTraceID = LogUtils.getConceptName(trace);
			Map<String, String> casePropertyMap = new HashMap<String, String>();
			Iterator<XAttribute> attIterator = trace.getAttributes().values().iterator();

			while (attIterator.hasNext()) {
				XAttribute att = attIterator.next();
				if (!att.getKey().equals("concept:name")) {
					casePropertyMap.put(att.getKey(), LogUtils.getValue(att));
				}
			}

			casePropertyMap.put(SPF.CASE_START_TIME, String.valueOf(traceStart.getMillis()));
			casePropertyMap.put(SPF.CASE_END_TIME, String.valueOf(traceEnd.getMillis()));
			casePropertyMap.put(SPF.CASE_EVENT_ACOUNT, String.valueOf(trace.size()));

			bpfManager.getCaseAttributeMap().put(curTraceID, casePropertyMap);
		}
	}

}
