package org.processmining.spm.processdiscovery;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.spm.processdiscovery.minerwrapper.AbstractMiner;
import org.processmining.spm.processdiscovery.minerwrapper.Miner;
import org.processmining.spm.processdiscovery.problem.Problem;
import org.processmining.spm.processdiscovery.problem.ProblemClass;
import org.processmining.spm.processdiscovery.problem.ProblemRelation;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.stagemining.Stage;
import org.processmining.spm.utils.LogUtils;

/**
 * This miner mines a submodel for each stage
 * @author Bruce
 *
 */
public class StageSubModelMiner extends AbstractMiner {
	protected Miner baseMiner = null;
	protected Stage stage = null;
	protected Set<Problem> problemClasses = null;
	protected Set<Problem> problemRelations = null;
	
	// originally this log is copied from the stage log, but it will
	// be used for noise filtering and mining, not the original stage log. 
	protected XLog subMinerLog = null; 
	private XLog preprocessLog = null;
	
	public StageSubModelMiner(Stage stage, Miner baseMiner) {
		this.stage = stage;
		this.baseMiner = baseMiner;
		this.problemClasses = new HashSet<>();
		this.problemRelations = new HashSet<>();
		this.preprocessLog = this.preprocess(this.stage.getLog());
		
		//Copy log from the stage log
		XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
		this.subMinerLog = factory.createLog();
		LogUtils.setConceptName(this.subMinerLog, LogUtils.getConceptName(stage.getLog()) + "_subminerlog");
		for (XTrace trace : stage.getLog()) {
			XTrace newTrace = factory.createTrace(trace.getAttributes());
			for (XEvent e : trace) {
				newTrace.add(e);
			}
			this.subMinerLog.add(newTrace);
		}
	}
	
	public XLog getPreprocessLog() {
		return this.preprocessLog;
	}
	
	public Stage getStage() {
		return this.stage;
	}
	
	public XLog getLog() {
		return this.subMinerLog;
	}
	
	public Miner getBaseMiner() {
		return this.baseMiner;
	}
	
	public Set<Problem> getproblemClasses() {
		return this.problemClasses;
	}
	
	public boolean addProblemClasses(Set<Problem> problems) {
		boolean found = false;
		for (Problem problem : problems) {
			if (problem instanceof ProblemClass) {
				String className = ((ProblemClass)problem).getEventClassName();
				if (this.getStage().getStageItemLabels().contains(className) || 
						this.getStage().getEndMilestone().getLabel().equals(className)) {
					this.problemClasses.add(problem);
					found = true;
				}
			}
		}
		return found;
	}
	
	public boolean addProblemRelations(Set<Problem> problems) {
		boolean anyFound = false;
		for (Problem problem : problems) {
			if (problem instanceof ProblemRelation) {
				ProblemRelation problemRelation = (ProblemRelation)problem;
				Set<String> stageTasks = this.getStage().getStageItemLabels();
				stageTasks.add(this.getStage().getStartMilestone().getLabel());
				stageTasks.add(this.getStage().getEndMilestone().getLabel());
				boolean problemContained = true;
				for (String task : problemRelation.getRelation()) {
					if (!stageTasks.contains(task)) {
						problemContained = false;
						break;
					}
				}
				if (problemContained) {
					this.problemRelations.add(problem);
					anyFound = true;
				}
			}
		}
		return anyFound;
	}
	
	public MiningResult mine(ModelMetrics metric) throws Exception {
		XLog miningLog = this.preprocessLog;
		
		// Filter out problem classes not allowed by the base miner
		Set<String> problemClassesByBaseMiner = new HashSet<>();
		if (this.miningResult != null) {
			Set<String> modelClasses = new HashSet<>();
			for (Transition trans : miningResult.getPetrinetRes().getPetrinet().getTransitions()) {
				modelClasses.add(trans.getLabel());
			}
			
			Set<String> logClasses = new HashSet<>();
			XEventClasses eventClasses = XLogInfoFactory.createLogInfo(miningLog, XLogInfoImpl.NAME_CLASSIFIER).getEventClasses();
			for (XEventClass e : eventClasses.getClasses()) logClasses.add(e.getId());
			
			problemClassesByBaseMiner = new HashSet<String>(logClasses);
			problemClassesByBaseMiner.removeAll(modelClasses);
			
			if (!problemClassesByBaseMiner.isEmpty()) {
				miningLog = LogUtils.filterOutWholeTraceByEventClasses(miningLog, problemClassesByBaseMiner);
			}
		}
		
		//Filter out problem classes identified by stage-based approach
//		Set<String> problemClasses = new HashSet<>();
//		for (Problem problem : this.problemClasses) {
//			if (problem instanceof ProblemClass) problemClasses.add(((ProblemClass)problem).getEventClassName());
//		}
//		if (!problemClasses.isEmpty()) {
//			miningLog = LogUtils.filterOutEventClasses(miningLog, problemClasses);
//		}
		
		// Filter out problem relations identified by stage-based approach
		// Note that this step must be after filtering out problem classes as
		// the problem relations are those after the log is free of problem classes
		Set<String> problemRelations = new HashSet<>();
		for (Problem problem : this.problemRelations) {
			if (problem instanceof ProblemRelation) {
				problemRelations.add(((ProblemRelation)problem).getRelationString());
			}
		}
		if (!problemRelations.isEmpty()) {
			miningLog = LogUtils.filterOutWholeTraceByRelations(miningLog, problemRelations);
//			miningLog = LogUtils.filterOutRelations(miningLog, problemRelations);
		}
		
		if (miningLog.isEmpty()) throw new Exception("A sublog is empty after filering. Mining stops!");
		
		this.subMinerLog = miningLog;
		this.miningResult = baseMiner.mineBestModel(miningLog, metric);
		return this.miningResult;
	}
	
	/**
	 * Traces in the log: MilestoneEvent<some events>MilestoneEvent
	 * If this is the first stage: start<some events>MilestoneEvent
	 * start is a fake start milestone
	 * If this is the last stage: MilestoneEvent<some events>end
	 * end is a fake end milestone
	 * @param log
	 * @return
	 */
	private XLog preprocess(XLog log) {
		XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
		XLog newLog = factory.createLog(log.getAttributes());
		for (XTrace trace : log) {
			XTrace newTrace = factory.createTrace(trace.getAttributes());
			
	    	XAttributeMap attMap = factory.createAttributeMap();
	    	attMap.put("concept:name", factory.createAttributeLiteral("concept:name", this.stage.getStartMilestone().getLabel(), null));
	    	attMap.put("lifecycle:transition", factory.createAttributeLiteral("lifecycle:transition", "complete", null));
	    	attMap.put("stage", factory.createAttributeLiteral("stage", (stage.getIndex()+1)+"", null));
	    	attMap.put("time:timestamp", factory.createAttributeTimestamp("time:timestamp", LogUtils.getTimestamp(trace.get(0)).minus(1000).toDate(), null));
	    	newTrace.add(factory.createEvent(attMap));
			
			for (XEvent e : trace) {
				newTrace.add(e);
			}
			
			XEvent lastEvent = trace.get(trace.size()-1);
			if (!LogUtils.getConceptName(lastEvent).equalsIgnoreCase(stage.getEndMilestone().getLabel())) {
		    	XAttributeMap attMap2 = factory.createAttributeMap();
		    	attMap2.put("concept:name", factory.createAttributeLiteral("concept:name", stage.getEndMilestone().getLabel(), null));
		    	attMap2.put("lifecycle:transition", factory.createAttributeLiteral("lifecycle:transition", "complete", null));
		    	attMap2.put("stage", factory.createAttributeLiteral("stage", (stage.getIndex()+1)+"", null));
		    	attMap2.put("time:timestamp", factory.createAttributeTimestamp("time:timestamp", LogUtils.getTimestamp(lastEvent).plus(1000).toDate(), null));
		    	newTrace.add(factory.createEvent(attMap2));
			}
			newLog.add(newTrace);
		}
		return newLog;
	}
}
