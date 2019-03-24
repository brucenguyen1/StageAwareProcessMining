package org.processmining.sapm.stagemining.groundtruth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.sapm.utils.LogUtils;

public class StageModelBPI2015 extends GroundTruth {
	public List<Set<String>> getGroundTruth(XLog log) throws Exception {
		
		Map<String,Set<String>> mapMilestonePhase = new HashMap<String, Set<String>>();
		mapMilestonePhase.put("01_hoofd_0", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_1", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_2", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_3", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_4", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_5", new HashSet<String>());
		mapMilestonePhase.put("01_hoofd_8", new HashSet<String>());
		
		List<Set<String>> phaseModel = new ArrayList<Set<String>>();
		//phaseModel.add(mapMilestonePhase.get("01_hoofd_0"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_0"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_1"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_2"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_3"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_4"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_5"));
		phaseModel.add(mapMilestonePhase.get("01_hoofd_8"));
		
		for (XTrace trace : log) {
			String traceID = LogUtils.getConceptName(trace);
			for (XEvent event : trace) {
				String eventName = LogUtils.getConceptName(event).toLowerCase();
				if (eventName.equals("start") || eventName.equals("end")) continue;
				
				String eventStageName = eventName.substring(0,10);
				if (mapMilestonePhase.keySet().contains(eventStageName)) {
					mapMilestonePhase.get(eventStageName).add(eventName);
				}
				else {
					throw new Exception("Cannot find a stage name " + eventStageName + 
								" in the predefined set of stage names " + mapMilestonePhase.keySet().toString() + 
								". TraceID = " + traceID +  ". Event name = " + eventName);
				}
			}
		}
		
		return phaseModel;
	}
}
