package org.processmining.sapm.processdiscovery.result;

import java.util.SortedMap;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.sapm.processdiscovery.evaluation.EvaluationResult;

public class MiningResult {
	private XLog m_log;
	private SortedMap<String,String> m_argMap; 
	private BPMNDiagram m_bpmn = null;
	private PetrinetResult m_petrinetRes = null;
	private EvaluationResult m_evaluation = null;
	private long m_runtime = 0;
	
	public MiningResult(XLog log, SortedMap<String,String> argMap, BPMNDiagram d, 
						PetrinetResult netRes, long runtime) {
		m_log = log;
		m_argMap = argMap;
		m_bpmn = d;
		m_petrinetRes = netRes;
		m_runtime = runtime;
		m_evaluation = new EvaluationResult();
	}
	
	public XLog getLog() {
		return m_log;
	}
	
	public SortedMap<String,String> getArgMap() {
		return m_argMap;
	}
	
	public BPMNDiagram getBPMN() {
		return m_bpmn;
	}
	
	public PetrinetResult getPetrinetRes() {
		return m_petrinetRes;
	}

	
	public EvaluationResult getEvaluation() {
		return m_evaluation;
	}
	
	public void setEvaluationResult(EvaluationResult evalRes) {
		m_evaluation = evalRes;
	}
	
	public long getMiningRuntime() {
		return m_runtime;
	}
	
	public void setMiningTime(long miningTime) {
		this.m_runtime = miningTime;
	}

	
}
