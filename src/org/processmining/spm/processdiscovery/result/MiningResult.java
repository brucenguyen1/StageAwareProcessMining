package org.processmining.spm.processdiscovery.result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.multietc.automaton.AutomatonEdge;
import org.processmining.plugins.multietc.automaton.AutomatonNode;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.processdiscovery.evaluation.EvaluationResult;
import org.processmining.spm.processdiscovery.minerwrapper.AbstractMiner;
import org.processmining.spm.processdiscovery.minerwrapper.Miner;
import org.processmining.spm.utils.LogUtils;

import edu.uci.ics.jung.graph.Graph;

public class MiningResult {
	private XLog m_log;
//	private AbstractMiner m_miner;
	private SortedMap<String,String> m_argMap; 
	private BPMNDiagram m_bpmn = null;
	private PetrinetResult m_petrinetRes = null;
	private EvaluationResult m_evaluation = null;
	private long m_runtime = 0;
	
	public MiningResult(XLog log, SortedMap<String,String> argMap, BPMNDiagram d, 
						PetrinetResult netRes, EvaluationResult evalRes, long runtime) {
		m_log = log;
//		m_miner = miner;
		m_argMap = argMap;
		m_bpmn = d;
		m_petrinetRes = netRes;
		m_evaluation = evalRes;
		m_runtime = runtime;
	}
	
	public XLog getLog() {
		return m_log;
	}
	
//	public AbstractMiner getMiner() {
//		return m_miner;
//	}
	
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
	
//	public Map<XEventClass,Double> findImprovementPoints(MiningResult wholeRes, double negativeThreshold) {
//		Automaton automaton = this.getEvaluation().getAutomaton();
//		Graph<AutomatonNode, AutomatonEdge> g = automaton.getJUNG();
//		
//		//find the root node
//		AutomatonNode root = null;
//		for( AutomatonNode n: g.getVertices()){
//			if (g.getInEdges(n).isEmpty()) {
//				root = n;
//				break;
//			}
//		}
//		
//		//-------------------------------------------
//		// Breadth-first search on the automaton from the root node
//		// to find points with negative impact on precision 
//		//-------------------------------------------
//		Set<AutomatonNode> candidates = new HashSet<>(); 
//		Queue<AutomatonNode> bfsQueue = new LinkedList<>();
//		bfsQueue.add(root);
//		while (!bfsQueue.isEmpty()) {
//			AutomatonNode currentNode = bfsQueue.poll();
//			double weight = currentNode.getWeight();
//			Collection<AutomatonEdge> outEdges = g.getOutEdges(currentNode);
//			for (AutomatonEdge e : outEdges) {
//				AutomatonNode childNode = e.getTarget();
//				if (childNode.getWeight()/weight <= negativeThreshold) {
//					candidates.add(childNode);
//				}
//				else {
//					bfsQueue.add(childNode);
//				}
//			}
//			
//		}
//		
//		//-------------------------------------------
//		// Breadth-first search on candidate nodes to collect
//		// all child nodes
//		//-------------------------------------------
//		for (AutomatonNode n : candidates) {
//			Set<AutomatonNode> childNodes = new HashSet<>();
//			bfsQueue.clear();
//			bfsQueue.add(n);
//			while (!bfsQueue.isEmpty()) {
//				AutomatonNode node = bfsQueue.poll();
//				for (AutomatonEdge e : g.getOutEdges(node)) {
//					childNodes.add(e.getTarget());
//					bfsQueue.add(e.getTarget());
//				}
//			}
//			candidates.addAll(childNodes);
//		}
//		
//		//-------------------------------------------
//		// Assess the impact of candidate nodes on Fscore
//		//-------------------------------------------	
//		Map<XEventClass,Double> candidateEVClasses = new HashMap<>();
//		XEventClasses eventClasses = XLogInfoFactory.createLogInfo(m_log, XLogInfoImpl.NAME_CLASSIFIER).getEventClasses();
//		for (AutomatonNode node : candidates) {
//			AutomatonEdge e = g.getInEdges(node).iterator().next();
//			XEventClass foundEVClass = eventClasses.getByIdentity(e.getTransition().getLabel());
//			if (foundEVClass != null) {
//				double newFscore = this.evalFscore(node, wholeRes);
//				if ( newFscore > wholeRes.getEvaluation().getFscore()) {
//					candidateEVClasses.put(foundEVClass, newFscore - wholeRes.getEvaluation().getFscore());
//				}
//			}
//		}
//		
//		return candidateEVClasses;
//	}
	
//	private double evalFscore(AutomatonNode node, MiningResult wholeRes) {
//		double fit = this.evalFitness(node, wholeRes);
//		double prec = this.evalPrecision(node, wholeRes);
//		return (2*fit*prec)/(fit + prec);
//	}
//	
//	private double evalFitness(AutomatonNode node, MiningResult wholeRes) {
//		PNRepResult alignments = this.getEvaluation().getAlignments();
//		Graph<AutomatonNode, AutomatonEdge> subGraph = this.getEvaluation().getAutomaton().getJUNG();
//		String transitionToRemove = subGraph.getInEdges(node).iterator().next().getTransition().getLabel();
//		int frequency = 0;
//		double newFit = 0.0;
//		for (SyncReplayResult traceReplay : alignments) {
//			double numerator = traceReplay.getInfo().get(PNRepResult.RAWFITNESSCOST);
//			double maxCost = numerator/traceReplay.getInfo().get(PNRepResult.TRACEFITNESS);
//			for (int i: traceReplay.getTraceIndex()) {
//				XTrace trace = wholeRes.getLog().get(i);
//				for (XEvent e : trace) {
//					if (LogUtils.getConceptName(e).toString().equalsIgnoreCase(transitionToRemove)) {
//						frequency++;
//					}
//				}
//			}
//			newFit += (numerator - frequency)/maxCost;
//		}
//		return newFit;
//	}
//	
//	private double evalPrecision(AutomatonNode node, MiningResult wholeRes) {
//		Graph<AutomatonNode, AutomatonEdge> wholeGraph = wholeRes.getEvaluation().getAutomaton().getJUNG();
//		Graph<AutomatonNode, AutomatonEdge> subGraph = this.getEvaluation().getAutomaton().getJUNG();
//		String transitionToRemove = subGraph.getInEdges(node).iterator().next().getTransition().getLabel();
//		
//		double num=0.0,den=0.0;
//		for( AutomatonNode n: wholeGraph.getVertices()){
//			if(n.getMarking() != null){
//				
//				Collection<Transition> adjustedAvailTasks = new HashSet<>();
//				for (Transition t : n.getAvailableTasks()) {
//					if (!t.getLabel().equalsIgnoreCase(transitionToRemove)) adjustedAvailTasks.add(t);
//				}
//				
//				double weight = n.getWeight();
//				Set<Transition> reflected = new HashSet<Transition>();
//				for(AutomatonEdge e: wholeGraph.getOutEdges(n)){
//					reflected.add(e.getTransition());
//				}
//				
//				Set<Transition> escaping = new HashSet<Transition>(adjustedAvailTasks);
//				escaping.removeAll(reflected);
//				
//				num += (weight * (adjustedAvailTasks.size() - escaping.size()));
//				den += (weight *  adjustedAvailTasks.size());
//			}
//		}
//		return num/den;
//	}
	
}
