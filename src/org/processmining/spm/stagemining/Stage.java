package org.processmining.spm.stagemining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.hypergraph.abs.IVertex;
import org.processmining.spm.utils.LogUtils;

/**
 * A stage has a start milestone, a set of nodes/labels/eventclasses (stage item)
 * and an end milestone.
 * @author Bruce
 *
 */
public class Stage extends HashSet<StageItem> {
	private Milestone startMilestone;
	private Milestone endMilestone;
	private StageListModel stageList = null;
	private XLog stageLog;
	
	public Stage(StageListModel model) {
		this.stageList = model;
	}
	
	public Milestone getStartMilestone() {
		return this.startMilestone;
	}
	
	public void setStartMilestone(Milestone milestone) {
		this.startMilestone = milestone;
	}
	
	public Milestone getEndMilestone() {
		return this.endMilestone;
	}
	
	public void setEndMilestone(Milestone milestone) {
		this.endMilestone = milestone;
	}
	
	public int getIndex() {
		return this.stageList.indexOf(this);
	}
	
	/**
	 * Only get the item labels, excluding start and end milestone nodes
	 * @return
	 */
	public Set<String> getStageItemLabels() {
		Set<String> labelSet = new HashSet<>();
		for (StageItem item : this) {
			labelSet.add(item.getLabel());
		}
		
		return labelSet;
	}
	
	/**
	 * Only get the intra-stage nodes excluding start and end milestone nodes
	 * @return
	 */
	public Set<IVertex> getStageItemNodes() {
		Set<IVertex> nodeSet = new HashSet<>();
		for (StageItem item : this) {
			nodeSet.add(item.getNode());
		}
		
		return nodeSet;
	}
	
	public Set<XEventClass> getStageItemEventClasses() {
		Set<XEventClass> eventClassSet = new HashSet<>();
		for (StageItem item : this) {
			eventClassSet.add(item.getEventClass());
		}
		
		return eventClassSet;
	}
	
	public Set<String> getActualStageLabels() {
		Set<String> labelSet = new HashSet<>();
		for (StageItem item : this) {
			labelSet.add(item.getLabel());
		}
		
		if (this.getStartMilestone().isTrueMilestone() && !this.getStartMilestone().isBelongToPrevStage()) {
			labelSet.add(this.getStartMilestone().getLabel());
		}
		if (this.getEndMilestone().isTrueMilestone() && this.getEndMilestone().isBelongToPrevStage()) {
			labelSet.add(this.getEndMilestone().getLabel());
		}
		
		return labelSet;
	}
	
	public Set<IVertex> getActualStageNodes() {
		Set<IVertex> nodeSet = new HashSet<>();
		for (StageItem item : this) {
			nodeSet.add(item.getNode());
		}
		
		if (this.getStartMilestone().isTrueMilestone() && !this.getStartMilestone().isBelongToPrevStage()) {
			nodeSet.add(this.getStartMilestone().getNode());
		}
		if (this.getEndMilestone().isTrueMilestone() && this.getEndMilestone().isBelongToPrevStage()) {
			nodeSet.add(this.getEndMilestone().getNode());
		}
		
		return nodeSet;
	}
	
	public Set<XEventClass> getActualStageEventClasses() {
		Set<XEventClass> eventClassSet = new HashSet<>();
		for (StageItem item : this) {
			eventClassSet.add(item.getEventClass());
		}
		
		if (this.getStartMilestone().isTrueMilestone() && !this.getStartMilestone().isBelongToPrevStage()) {
			eventClassSet.add(this.getStartMilestone().getEventClass());
		}
		if (this.getEndMilestone().isTrueMilestone() && this.getEndMilestone().isBelongToPrevStage()) {
			eventClassSet.add(this.getEndMilestone().getEventClass());
		}
		
		return eventClassSet;
	}
	
	/**
	 * This is the raw traces of a stage
	 * @return
	 */
	public XLog getLog() {
		return this.stageLog;
	}
	
	/**
	 * Extract list of event labels belonging to this stage from a trace
	 * A new trace has this format: e1e2...endmilestone
	 * @param trace
	 * @return
	 */
	public List<XTrace> extractStageTraces(XTrace trace) {
		List<XTrace> stageTraces = new ArrayList<>();
		if (!this.isEmpty()) {
			Set<String> stageLabels = this.getStageItemLabels();
			if (this.getEndMilestone().isTrueMilestone()) stageLabels.add(this.getEndMilestone().getLabel());
			
			XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
			String traceID = LogUtils.getConceptName(trace);
			int counter = 1;
			XTrace stageTrace = factory.createTrace();
			LogUtils.setConceptName(stageTrace, traceID + "-" + counter);
			for (int i=0;i<trace.size();i++) {
	        	XEvent e = trace.get(i);
	        	String eName = LogUtils.getConceptName(e);

	        	if (stageLabels.contains(eName)) {
	        		stageTrace.add(e);
	        		if (i == trace.size()-1) stageTraces.add(stageTrace); //the last stage trace
	        	}
	        	else if (!stageTrace.isEmpty()) { // stage changed
	        		stageTraces.add(stageTrace);
	        		counter++;
	        		stageTrace = factory.createTrace();
	        		LogUtils.setConceptName(stageTrace, traceID + "-" + counter);
	        	}
	        }
		}
		return stageTraces;
	}
	
	public XLog extractStageLog(XLog fullLog) {
		XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
		stageLog = factory.createLog();
		LogUtils.setConceptName(stageLog, LogUtils.getConceptName(fullLog) + "_sublog_" + (this.getIndex()+1));
		for (XTrace trace : fullLog) {
			List<XTrace> stageTraces = this.extractStageTraces(trace);
			stageLog.addAll(stageTraces);
		}
		return this.stageLog;
	}
	
	@Override
	public String toString() {
		return this.getStartMilestone().getLabel() + "->" + this.getStageItemLabels().toString() + "->" + this.getEndMilestone().getLabel();
	}
}
