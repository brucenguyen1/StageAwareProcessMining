package org.processmining.sapm.stagemining.groundtruth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;

public class StageModelBPI2017 extends GroundTruth {
	public List<Set<String>> getGroundTruth(XLog log) throws Exception {
		
		Map<String,Set<String>> mapMilestonePhase = new HashMap<String, Set<String>>();
		//mapMilestonePhase.put("01_hoofd_0", new HashSet<String>());
		mapMilestonePhase.put("preassess", new HashSet<String>());
		mapMilestonePhase.put("assess", new HashSet<String>());
		mapMilestonePhase.put("validate", new HashSet<String>());
		
		List<Set<String>> phaseModel = new ArrayList<Set<String>>();
		//phaseModel.add(mapMilestonePhase.get("01_hoofd_0"));
		phaseModel.add(mapMilestonePhase.get("preassess"));
		phaseModel.add(mapMilestonePhase.get("assess"));
		phaseModel.add(mapMilestonePhase.get("validate"));
		
		//preassess phase
		phaseModel.get(0).add("A_Create Application".toLowerCase());
		phaseModel.get(0).add("A_Submitted".toLowerCase());
		phaseModel.get(0).add("W_Handle leads".toLowerCase());
		phaseModel.get(0).add("A_Concept".toLowerCase());
		phaseModel.get(0).add("A_Accepted".toLowerCase());
		
		//offer phase
		phaseModel.get(1).add("O_Create Offer".toLowerCase());
		phaseModel.get(1).add("O_Created".toLowerCase());
		phaseModel.get(1).add("O_Sent(mail and online)".toLowerCase());
		phaseModel.get(1).add("O_Sent(online only)".toLowerCase());
		phaseModel.get(1).add("W_Complete application".toLowerCase());
		phaseModel.get(1).add("A_Complete".toLowerCase());
		phaseModel.get(1).add("W_Call after offers".toLowerCase());
		
		//validate phase
		phaseModel.get(2).add("A_Validating".toLowerCase());
		phaseModel.get(2).add("O_Accepted".toLowerCase());
		phaseModel.get(2).add("O_Returned".toLowerCase());
		phaseModel.get(2).add("O_Refused".toLowerCase());
		phaseModel.get(2).add("O_Cancelled".toLowerCase());
		phaseModel.get(2).add("A_Pending".toLowerCase());
		phaseModel.get(2).add("A_Denied".toLowerCase());
		phaseModel.get(2).add("A_Cancelled".toLowerCase());
		phaseModel.get(2).add("A_Incomplete".toLowerCase());
		phaseModel.get(2).add("W_Validate application".toLowerCase());
		phaseModel.get(2).add("W_Assess potential fraud".toLowerCase());
		phaseModel.get(2).add("W_Call incomplete files".toLowerCase());
		
		return phaseModel;
	}
}
