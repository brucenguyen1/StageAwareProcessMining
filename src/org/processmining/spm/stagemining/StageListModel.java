package org.processmining.spm.stagemining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.spm.utils.LogUtils;

/**
 * A stage list model is a list of stages. Each stage contains
 * a start milestone, a set of stage items and an end milestone. Note that the 
 * first stage has a fake start milestone, and the last stage has a fake end milestone 
 * (i.e. not corresponding to any real event class)
 * @author Bruce
 *
 */
public class StageListModel extends ArrayList<Stage> {

	public Milestone getFirstMilestone() {
		if (!this.isEmpty()) {
			return this.get(0).getStartMilestone();
		}
		else {
			return null;
		}
	}
	
	public Milestone getLastMilestone() {
		if (!this.isEmpty()) {
			return this.get(this.size()-1).getEndMilestone();
		}
		else {
			return null;
		}
	}
	
	public List<Milestone> getStartMilestones() {
		List<Milestone> milestones = new ArrayList<>();
		for (Stage s : this) {
			if (s.getStartMilestone().isTrueMilestone()) milestones.add(s.getStartMilestone());
		}
		return milestones;
	}
	
	public List<String> getStartMilestoneLabels() {
		List<String> labels = new ArrayList<>();
		List<Milestone> milestones = this.getStartMilestones();
		for (Milestone m : milestones) {
			if (m.isTrueMilestone()) labels.add(m.getLabel());
		}
		return labels;
	}
	
	public List<IVertex> getStartMilestoneNodes() {
		List<IVertex> nodes = new ArrayList<>();
		List<Milestone> milestones = this.getStartMilestones();
		for (Milestone m : milestones) {
			if (m.isTrueMilestone()) nodes.add(m.getNode());
		}
		return nodes;
	}
	
	public List<Milestone> getEndMilestones() {
		List<Milestone> milestones = new ArrayList<>();
		for (Stage s : this) {
			if (s.getEndMilestone().isTrueMilestone()) milestones.add(s.getEndMilestone());
		}
		return milestones;
	}
	
	public List<String> getEndMilestoneLabels() {
		List<String> labels = new ArrayList<>();
		List<Milestone> milestones = this.getEndMilestones();
		for (Milestone m : milestones) {
			if (m.isTrueMilestone()) labels.add(m.getLabel());
		}
		return labels;
	}
	
	public List<IVertex> getEndMilestoneNodes() {
		List<IVertex> nodes = new ArrayList<>();
		List<Milestone> milestones = this.getEndMilestones();
		for (Milestone m : milestones) {
			if (m.isTrueMilestone()) nodes.add(m.getNode());
		}
		return nodes;
	}
	
	/**
	 * Each stage consists of member items and the true end milestone
	 * Representation format: String
	 * @return
	 */
	public List<Set<String>> getStageLabels() {
		List<Set<String>> stageLabels = new ArrayList<>();
		for (Stage s : this) {
			Set<String> members = new HashSet<>();
			members.addAll(s.getStageItemLabels());
			if (s.getEndMilestone().isTrueMilestone()) members.add(s.getEndMilestone().getLabel());
			stageLabels.add(members);
		}
		return stageLabels;
	}
	
	
	public List<Set<String>> getActualStageLabels() {
		List<Set<String>> stageLabels = new ArrayList<>();
		for (Stage s : this) {
			Set<String> members = new HashSet<>();
			members.addAll(s.getActualStageLabels());
			stageLabels.add(members);
		}
		return stageLabels;
	}
	
	/*
	 * This method looks for a stage index when a stage contains stage
	 * items and all milestones that have been set up as belong to 
	 * the stage.
	 */
	public int findActualStageIndex(String eventName) throws Exception {
		List<Set<String>> stageLabels = this.getActualStageLabels();
		for (int i=0;i<this.size();i++) {
			for (String itemLabel : stageLabels.get(i)) {
				if (itemLabel.equalsIgnoreCase(eventName)) {
					return (i+1);
				}
			}
		}
		throw new Exception("Cannot find event with name=" + eventName + " in the stage model");
	}
	
	/**
	 * This method looks for stage index when each stage has 
	 * a set of stage items and an end milestone 
	 * The stage index = the stage index in the list + 1
	 * @param eventName
	 * @return
	 * @throws Exception
	 */
	public int findStageIndex(String eventName) throws Exception {
		List<Set<String>> stageLabels = this.getStageLabels();
		for (int i=0;i<this.size();i++) {
			for (String itemLabel : stageLabels.get(i)) {
				if (itemLabel.equalsIgnoreCase(eventName)) {
					return (i+1);
				}
			}
		}
		throw new Exception("Cannot find event with name=" + eventName + " in the stage model");
	}
	
	// This is to connect with DecomposedMiner
	public ActivityClusterArray getActivityClusterArray() {
		ActivityClusterArray activityClusters = ActivityClusterArrayFactory.createActivityClusterArray();
		for (Stage s : this) {
			activityClusters.addCluster(s.getStageItemEventClasses());
		}
		return activityClusters;
	}

	

	
	@Override
	public String toString() {
		String toString = "";
		for (Stage s : this) {
			toString += s.toString() + " ==> ";
		}
		toString += "end";
		return toString;
	}
	
	public void writeSublogs(String filePath) throws IOException {
		XesXmlSerializer writer = new XesXmlGZIPSerializer();
		for (Stage stage : this) {
			FileOutputStream fos = new FileOutputStream(filePath + File.separator + LogUtils.getConceptName(stage.getLog()) + ".xes.gz");
			writer.serialize(stage.getLog(), fos);
			fos.close();
		}
	}
	
}
 