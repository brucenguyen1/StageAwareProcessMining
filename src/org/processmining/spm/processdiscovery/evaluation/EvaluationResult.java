package org.processmining.spm.processdiscovery.evaluation;

import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class EvaluationResult {
	private double m_fitness = 0.0d;
	private double m_precision = 0.0d;
	private double m_generalization = 0.0d;
	private double m_size = 0.0d;
	private double m_cfc = 0.0d;
	private double m_structuredness = 0.0d;
	private double m_separability = 0.0d;
	private double m_sequentiality = 0.0d;
	private boolean m_isSound = false;
	private Automaton m_automaton = null;
	private PNRepResult m_alignments = null;
	private CostBasedCompleteParam m_alignParams;
	
	public EvaluationResult(double fitness, double precision, double generalization, 
			double size, double cfc, double structuredness, boolean isSound) {
		m_fitness = fitness;
		m_precision = precision;
		m_generalization = generalization;
		m_size = size;
		m_cfc = cfc;
		m_structuredness = structuredness;
		m_isSound = isSound;
	}
	
	public EvaluationResult(CostBasedCompleteParam alignParams, double fitness, double precision, double generalization, 
			double size, double cfc, double structuredness, boolean isSound, Automaton automaton, PNRepResult alignments) {
		m_fitness = fitness;
		m_precision = precision;
		m_generalization = generalization;
		m_size = size;
		m_cfc = cfc;
		m_structuredness = structuredness;
		m_isSound = isSound;
		m_automaton = automaton;
		m_alignments = alignments;
		m_alignParams = alignParams;
	}
	
//	public EvaluationResult(double fitness, double precision, double generalization, 
//							double size, double cfc, double structuredness, double separability,
//							double sequentiality, boolean isSound, Automaton automaton, PNRepResult alignments) {
//		m_fitness = fitness;
//		m_precision = precision;
//		m_generalization = generalization;
//		m_size = size;
//		m_cfc = cfc;
//		m_structuredness = structuredness;
//		m_separability = separability;
//		m_sequentiality = sequentiality;
//		m_isSound = isSound;
//		m_automaton = automaton;
//		m_alignments = alignments;
//	}
	
	public EvaluationResult(double fitness, double precision, double generalization, boolean isSound) {
		m_fitness = fitness;
		m_precision = precision;
		m_generalization = generalization;
		m_isSound = isSound;
	}
	
	public boolean isSoundModel() {
		return m_isSound;
	}
	
	public double getFitness() {
		return m_fitness;
	}
	
	public void setFitness(double fitness) {
		m_fitness = fitness;
	}
	
	public double getPrecision() {
		return m_precision;
	}
	
	public void setPrecision(double precision) {
		m_precision = precision;
	}	
	
	public double getGeneralization() {
		return m_generalization;
	}
	
	public double getFscore() {
		return 2*m_fitness*m_precision/(m_fitness + m_precision);
	}
	
	public void setGeneralization(double generalization) {
		m_generalization = generalization;
	}	
	
	public double getSize() {
		return m_size;
	}
	
	public void setSize(double size) {
		m_size = size;
	}
	
	public double getCFC() {
		return m_cfc;
	}
	
	public void setCFC(double cfc) {
		m_cfc = cfc;
	}
	
	public double getStructuredness() {
		return m_structuredness;
	}
	
	public void setStructuredness(double structuredness) {
		m_structuredness = structuredness;
	}	
	
	public double getSequentiality() {
		return m_sequentiality;
	}
	
	public double getSeparability() {
		return m_separability;
	}
	
	public Automaton getAutomaton() {
		return m_automaton;
	}
	
	public PNRepResult getAlignments() {
		return m_alignments;
	}
	
	public CostBasedCompleteParam getAlignParams() {
		return m_alignParams;
	}
}