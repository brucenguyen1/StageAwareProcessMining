package org.processmining.stagedprocessflows.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.processmining.stagedprocessflows.util.LogUtils;
import org.processmining.stagedprocessflows.util.OpenLogFilePlugin;

public class CreateLogBPI2015_1 {
	//Note that A_FINALIZED is not used as a stage since A_FINALIZED and A_ACCEPTED are closely linked.
	//For example, some activities have started before A_ACCEPTED and only completed after A_FINALIZED event
	//Therefore, it is hard to separate A_ACCEPTED and A_FINALIZED as two stages.
	public static final String EVENT_STAGE_KEY = "stage";

	public static final String STAGE1 = "accept";
	public static final String STAGE1_MILESTONE_EVENT = "phase application received";

	public static final String STAGE2 = "review";
	public static final String STAGE2_MILESTONE_EVENT = "phase application receptive";
	public static final String STAGE2_MILESTONE_EVENT_2 = "set decision status";

	public static final String STAGE3 = "issue";
	public static final String STAGE3_MILESTONE_EVENT = "phase decision taken";
	public static final String STAGE3_MILESTONE_EVENT_2 = "phase permanently suspended";

	public static final String STAGE4 = "close";
	//	public static final String STAGE4_MILESTONE_EVENT = "phase archived case";
	public static final String STAGE4_MILESTONE_EVENT = "close case";

	public static final String STATUS_KEY = "status";
	public static final String STATUS_EARLY_FINISHED = "early-finished";
	public static final String STATUS_CANCELLED = "cancelled";
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_IN_PROGRESS = "open";

	/**
	 * @param args
	 *            1st param: name of the input log 2nd param: name of the output
	 *            log
	 */
	public static void main(String[] args) {
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		File inputFile = new File(System.getProperty("user.dir") + "\\" + args[0]);
		XLog inputLog;
		new HashMap<String, List<String>>();

		try {
			File conversionResultFile = new File(System.getProperty("user.dir") + "\\results.log");
			FileWriter fileWriter = new FileWriter(conversionResultFile);
			String newline = System.getProperty("line.separator");

			//-----------------------------------------
			// Read input log and select events for output log
			//-----------------------------------------    		
			inputLog = (XLog) logImporter.importFile(inputFile);
			XFactory factory = new XFactoryNaiveImpl();

			//-----------------------------------------
			// Build a stage - events mapping
			//-----------------------------------------
			//    		for (XTrace trace : inputLog) {
			//    			Map<String,DateTime> stageDateTimeMap = new HashMap<String,DateTime>();
			//    			for (XEvent event : trace) {
			//    				String eventName = LogUtilites.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
			//    				DateTime eventTime = LogUtilites.getTimestamp(event);
			//    				if (eventName.equals(STAGE1_MILESTONE_EVENT)) {
			//    					stageDateTimeMap.put(STAGE1_MILESTONE_EVENT, eventTime);
			//    				}
			//    				else if (eventName.equals(STAGE2_MILESTONE_EVENT)) {
			//    					stageDateTimeMap.put(STAGE2_MILESTONE_EVENT, eventTime);
			//    				}
			//    				else if (eventName.equals(STAGE3_MILESTONE_EVENT)) {
			//    					stageDateTimeMap.put(STAGE3_MILESTONE_EVENT, eventTime);
			//    				}
			//    				else if (eventName.equals(STAGE4_MILESTONE_EVENT)) {
			//    					if (stageDateTimeMap.containsKey(STAGE3_MILESTONE_EVENT)) {
			//    						stageDateTimeMap.put(STAGE4_MILESTONE_EVENT, eventTime);
			//    					}
			//    				}
			//    			}
			//    			
			//				if (stageDateTimeMap.containsKey(STAGE1_MILESTONE_EVENT) && 
			//						stageDateTimeMap.containsKey(STAGE2_MILESTONE_EVENT) &&
			//						stageDateTimeMap.containsKey(STAGE3_MILESTONE_EVENT) &&
			//						stageDateTimeMap.containsKey(STAGE4_MILESTONE_EVENT) &&
			//						stageDateTimeMap.get(STAGE1_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE2_MILESTONE_EVENT)) &&
			//						stageDateTimeMap.get(STAGE1_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE3_MILESTONE_EVENT)) &&
			//						stageDateTimeMap.get(STAGE1_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE4_MILESTONE_EVENT)) &&
			//						stageDateTimeMap.get(STAGE2_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE3_MILESTONE_EVENT)) &&
			//						stageDateTimeMap.get(STAGE2_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE4_MILESTONE_EVENT)) &&
			//						stageDateTimeMap.get(STAGE3_MILESTONE_EVENT).isBefore(stageDateTimeMap.get(STAGE4_MILESTONE_EVENT))) {
			//					
			//					System.out.println("Non-Overlapping Trace ID: " + LogUtilites.getConceptName(trace).toLowerCase());
			//					
			//					for (XEvent event : trace) {
			//	    				String eventName = LogUtilites.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
			//	    				DateTime eventTime = LogUtilites.getTimestamp(event);
			//	    				if (eventName.equals(STAGE1_MILESTONE_EVENT) || 
			//	    						(eventTime.isBefore(stageDateTimeMap.get(STAGE1_MILESTONE_EVENT)) || 
			//	    						eventTime.isEqual(stageDateTimeMap.get(STAGE1_MILESTONE_EVENT)))) {
			//	    					if (!stageEventsMap.containsKey(STAGE1_MILESTONE_EVENT)) {
			//	    						stageEventsMap.put(STAGE1_MILESTONE_EVENT, new ArrayList<String>());
			//	    					}
			//	    					if (!stageEventsMap.get(STAGE1_MILESTONE_EVENT).contains(eventName)) {
			//	    						stageEventsMap.get(STAGE1_MILESTONE_EVENT).add(eventName);
			//	    					}
			//	    				}
			//	    				else if (eventName.equals(STAGE2_MILESTONE_EVENT) ||
			//	    						(eventTime.isBefore(stageDateTimeMap.get(STAGE2_MILESTONE_EVENT)) || 
			//	    						eventTime.isEqual(stageDateTimeMap.get(STAGE2_MILESTONE_EVENT)))) {
			//	    					if (!stageEventsMap.containsKey(STAGE2_MILESTONE_EVENT)) {
			//	    						stageEventsMap.put(STAGE2_MILESTONE_EVENT, new ArrayList<String>());
			//	    					}
			//	    					if (!stageEventsMap.get(STAGE2_MILESTONE_EVENT).contains(eventName)) {
			//	    						stageEventsMap.get(STAGE2_MILESTONE_EVENT).add(eventName);
			//	    					}
			//	    				}
			//	    				else if (eventName.equals(STAGE3_MILESTONE_EVENT) || 
			//	    						(eventTime.isBefore(stageDateTimeMap.get(STAGE3_MILESTONE_EVENT)) || 
			//	    						eventTime.isEqual(stageDateTimeMap.get(STAGE3_MILESTONE_EVENT)))) {
			//	    					if (!stageEventsMap.containsKey(STAGE3_MILESTONE_EVENT)) {
			//	    						stageEventsMap.put(STAGE3_MILESTONE_EVENT, new ArrayList<String>());
			//	    					}
			//	    					if (!stageEventsMap.get(STAGE3_MILESTONE_EVENT).contains(eventName)) {
			//	    						stageEventsMap.get(STAGE3_MILESTONE_EVENT).add(eventName);
			//	    					}
			//	    				}
			//	    				else if (eventName.equals(STAGE4_MILESTONE_EVENT) ||
			//	    						(eventTime.isBefore(stageDateTimeMap.get(STAGE4_MILESTONE_EVENT)) || 
			//	    						eventTime.isEqual(stageDateTimeMap.get(STAGE4_MILESTONE_EVENT)))) {
			//	    					if (!stageEventsMap.containsKey(STAGE4_MILESTONE_EVENT)) {
			//	    						stageEventsMap.put(STAGE4_MILESTONE_EVENT, new ArrayList<String>());
			//	    					}
			//	    					if (!stageEventsMap.get(STAGE4_MILESTONE_EVENT).contains(eventName)) {
			//	    						stageEventsMap.get(STAGE4_MILESTONE_EVENT).add(eventName);
			//	    					}
			//	    				}
			// 
			//					}
			//				}
			//    		}

			//-----------------------------------------
			// Assign stage to event
			//-----------------------------------------
			for (XTrace trace : inputLog) {
				System.out.println("Trace ID: " + LogUtils.getConceptName(trace).toLowerCase());
				Map<String, DateTime> stageDateTimeMap = new HashMap<String, DateTime>();

				//--------------------------------------
				// Collect stage timestamp of the trace
				//--------------------------------------
				for (XEvent event : trace) {
					String eventName = LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					DateTime eventTime = LogUtils.getTimestamp(event);

					if (eventName.equals(STAGE1_MILESTONE_EVENT)) {
						stageDateTimeMap.put(STAGE1_MILESTONE_EVENT, eventTime);
					} else if (eventName.equals(STAGE2_MILESTONE_EVENT)) {
						stageDateTimeMap.put(STAGE2_MILESTONE_EVENT, eventTime);
					} else if (eventName.equals(STAGE2_MILESTONE_EVENT_2)) {
						stageDateTimeMap.put(STAGE2_MILESTONE_EVENT_2, eventTime);
					} else if (eventName.equals(STAGE3_MILESTONE_EVENT)) {
						stageDateTimeMap.put(STAGE3_MILESTONE_EVENT, eventTime);
					} else if (eventName.equals(STAGE3_MILESTONE_EVENT_2)) {
						stageDateTimeMap.put(STAGE3_MILESTONE_EVENT_2, eventTime);
					} else if (eventName.equals(STAGE4_MILESTONE_EVENT)) {
						if (stageDateTimeMap.containsKey(STAGE3_MILESTONE_EVENT)) {
							stageDateTimeMap.put(STAGE4_MILESTONE_EVENT, eventTime);
						}
					}
				}

				//--------------------------------------
				// Determine case status
				//--------------------------------------
				String status = STATUS_IN_PROGRESS;
				for (XEvent event : trace) {
					String eventName = LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					LogUtils.getTimestamp(event);

					if (eventName.contains("procedure aborted")
							|| eventName.contains("procedure prematurely terminated")) {
						status = STATUS_CANCELLED;
						break;
					} else if (eventName.contains("no permit") && !stageDateTimeMap.containsKey(STAGE4_MILESTONE_EVENT)) {
						status = STATUS_EARLY_FINISHED;
						break;
					} else if (eventName.equals(STAGE4_MILESTONE_EVENT)
							&& stageDateTimeMap.containsKey(STAGE3_MILESTONE_EVENT)) {
						status = STATUS_COMPLETED;
						break;
					}
				}
				trace.getAttributes().put(STATUS_KEY, factory.createAttributeLiteral(STATUS_KEY, status, null));

				//--------------------------------------
				// Assign stage to events
				//--------------------------------------
				String stage2_Milestone = "";
				String stage3_Milestone = "";
				List<String> stage3Events = new ArrayList<String>();
				if (!stageDateTimeMap.containsKey(STAGE3_MILESTONE_EVENT_2)) {
					stage2_Milestone = STAGE2_MILESTONE_EVENT;
					stage3_Milestone = STAGE3_MILESTONE_EVENT;
					stage3Events.add("register objection and appeal periods");
					stage3Events.add("creating cover letter decision");
					stage3Events.add("enter senddate decision");
					stage3Events.add("transcript decision environmental permit to stakeholders");
					stage3Events.add("generating decision environmental permit");
					stage3Events.add("set decision phase decision permitting decided");
					stage3Events.add("enter senddate decision environmental permit");
					stage3Events.add("phase decision sent");
					stage3Events.add("phase decision taken");
					stage3Events.add("start decision phase decision permitting sent");
					stage3Events.add("record date of decision environmental permit");
					stage3Events.add("enter senddate decision environmental permit");
				} else {
					stage2_Milestone = STAGE2_MILESTONE_EVENT_2;
					stage3_Milestone = STAGE3_MILESTONE_EVENT_2;
					stage3Events.add("create cover letter permanent suspension");
					stage3Events.add("enter senddate decision permanent suspension");
					stage3Events.add("start decision phase permanently suspended decided");
					stage3Events.add("creating decision permanently suspended");
					stage3Events.add("start decision phase permanent suspension sent");
					stage3Events.add("decision keep permanently suspended");
					stage3Events.add("enter date decision permanent suspension");
					stage3Events.add("start decision phase permanently suspended decided");
					stage3Events.add("enter senddate decision permanent suspension");
				}

				//Stage 1
				for (XEvent event : trace) {
					String eventName = LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					DateTime eventTime = LogUtils.getTimestamp(event);
					event.getAttributes().put(EVENT_STAGE_KEY,
							factory.createAttributeLiteral(EVENT_STAGE_KEY, "", null));
					LogUtils.setConceptName(event, eventName);

					if (stageDateTimeMap.containsKey(STAGE1_MILESTONE_EVENT)) {
						// If stage 1 is the last stage: all events will be assigned to stage 1
						if (!stageDateTimeMap.containsKey(stage2_Milestone)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE1, null));
						}
						// If stage 1's and stage 2's milestone exist but on different days
						// Those events on the previous or same day as stage 1's milestone are assigned to stage 1 
						else if (DateTimeComparator.getDateOnlyInstance().compare(
								stageDateTimeMap.get(STAGE1_MILESTONE_EVENT), stageDateTimeMap.get(stage2_Milestone)) != 0) {
							if (DateTimeComparator.getDateOnlyInstance().compare(eventTime,
									stageDateTimeMap.get(STAGE1_MILESTONE_EVENT)) <= 0) {
								event.getAttributes().put(EVENT_STAGE_KEY,
										factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE1, null));
							}
						}
						// If stage 1's and stage 2's milestone exist but on the same day
						// Those events with less or same timestamp with stage 1's milestone are assigned to stage 1
						// Except the stage 2's milestone event
						else if (!eventTime.isAfter(stageDateTimeMap.get(STAGE1_MILESTONE_EVENT))
								&& !eventName.equals(stage2_Milestone)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE1, null));
						}
					}
				}

				//Stage 2
				for (XEvent event : trace) {
					String eventName = LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					DateTime eventTime = LogUtils.getTimestamp(event);
					String eventStage = LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).toLowerCase();

					// Note: only check remaining events not yet assigned stage 1
					if (stageDateTimeMap.containsKey(stage2_Milestone) && eventStage.equals("")) {
						// If stage 2 is the last stage: all remaining events will be assigned to stage 2
						if (!stageDateTimeMap.containsKey(stage3_Milestone)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE2, null));
						}
						// If both stage 2's and stage 3's milestone exist but on different days
						// Those events on the previous or same day as stage 2's milestone are assigned to stage 2 
						else if (DateTimeComparator.getDateOnlyInstance().compare(
								stageDateTimeMap.get(stage2_Milestone), stageDateTimeMap.get(stage3_Milestone)) != 0) {
							if (DateTimeComparator.getDateOnlyInstance().compare(eventTime,
									stageDateTimeMap.get(stage2_Milestone)) <= 0) {
								event.getAttributes().put(EVENT_STAGE_KEY,
										factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE2, null));
							}
						}
						// If both stage 2 and stage 3 exist but on the same day
						// Those events with less or same timestamp with stage 2's milestone are assigned to stage 2
						// Except the stage 3's milestone event
						else if (!eventTime.isAfter(stageDateTimeMap.get(stage2_Milestone))
								&& !eventName.equals(stage3_Milestone)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE2, null));
						}
					}
				}

				//Stage 3
				for (XEvent event : trace) {
					String eventName = LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					DateTime eventTime = LogUtils.getTimestamp(event);
					String eventStage = LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).toLowerCase();

					// Note: only check remaining events not yet assigned stage 1 and 2
					if (stageDateTimeMap.containsKey(stage3_Milestone)
							&& (eventStage.equals("") || stage3Events.contains(eventName))) {
						if (!stageDateTimeMap.containsKey(STAGE4_MILESTONE_EVENT)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE3, null));
						} else if (stage3Events.contains(eventName)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE3, null));
						} else if (DateTimeComparator.getDateOnlyInstance().compare(
								stageDateTimeMap.get(stage3_Milestone), stageDateTimeMap.get(STAGE4_MILESTONE_EVENT)) != 0) {
							if (DateTimeComparator.getDateOnlyInstance().compare(eventTime,
									stageDateTimeMap.get(stage3_Milestone)) <= 0) {
								event.getAttributes().put(EVENT_STAGE_KEY,
										factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE3, null));
							}
						} else if (!eventTime.isAfter(stageDateTimeMap.get(stage3_Milestone))
								&& !eventName.equals(STAGE4_MILESTONE_EVENT)) {
							event.getAttributes().put(EVENT_STAGE_KEY,
									factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE3, null));
						}
					}
				}

				//Stage 4
				for (XEvent event : trace) {
					LogUtils.getValue(event.getAttributes().get("activityNameEN")).toLowerCase();
					LogUtils.getTimestamp(event);
					String eventStage = LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).toLowerCase();

					// Note: remaining events not yet assigned as stage are stage 4's events.
					if (stageDateTimeMap.containsKey(STAGE4_MILESTONE_EVENT) && eventStage.equals("")) {
						event.getAttributes().put(EVENT_STAGE_KEY,
								factory.createAttributeLiteral(EVENT_STAGE_KEY, STAGE4, null));
					}
				}

				//outputLog.add(trace);
			}

			//----------------------------------------
			// Rearrange events in trace by stages
			//----------------------------------------
			XLogImpl outputLog = new XLogImpl(inputLog.getAttributes());
			XTrace outTrace;
			for (XTrace trace : inputLog) {
				outTrace = new XTraceImpl(trace.getAttributes());

				//Stage 1
				for (XEvent event : trace) {
					if (LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).equals(STAGE1)) {
						outTrace.add(event);
					}
				}
				//Stage 2
				for (XEvent event : trace) {
					if (LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).equals(STAGE2)) {
						outTrace.add(event);
					}
				}
				//Stage 3
				for (XEvent event : trace) {
					if (LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).equals(STAGE3)) {
						outTrace.add(event);
					}
				}
				//Stage 4
				for (XEvent event : trace) {
					if (LogUtils.getValue(event.getAttributes().get(EVENT_STAGE_KEY)).equals(STAGE4)) {
						outTrace.add(event);
					}
				}

				outputLog.add(outTrace);
			}

			//-----------------------------------------
			// Write to the output log
			//-----------------------------------------
			XesXmlSerializer writer = new XesXmlSerializer();
			fileWriter.write("Write to log file: " + args[1] + newline);
			String outputFile = System.getProperty("user.dir") + "\\" + args[1];
			File file = new File(outputFile);
			FileOutputStream fs = new FileOutputStream(file);
			writer.serialize(outputLog, fs);

			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
