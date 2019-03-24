package org.processmining.sapm.processdiscovery.evaluation;

import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class EvaluationResult {
	private double m_fitness = -1.0d;
	private double m_precision = -1.0d;
	private double m_size = 0.0d;
	private double m_cfc = 0.0d;
	private double m_structuredness = 0.0d;
	private int m_checkSound = -2;
	private Automaton m_automaton = null;
	private PNRepResult m_alignments = null;
	private CostBasedCompleteParam m_alignParams = null;
	
	public EvaluationResult() {
		
	}
	
	public int getSoundCheck() {
		return m_checkSound;
	}
	
	public void setSoundCheck(int soundness) {
		m_checkSound = soundness;
	}	
	
	public boolean isSound() {
		return (m_checkSound > 0);
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
	
	// initial value is -1.
	public double getFscore() {
		return 2*m_fitness*m_precision/(m_fitness + m_precision);
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
	
	public Automaton getAutomaton() {
		return m_automaton;
	}
	
	public void setAutomaton(Automaton automaton) {
		m_automaton = automaton;
	}
	
	public PNRepResult getAlignments() {
		return m_alignments;
	}
	
	public void setAlignments(PNRepResult alignments) {
		m_alignments = alignments;
	}
	
	public CostBasedCompleteParam getAlignParams() {
		return m_alignParams;
	}
	
	public void setAlignParams(CostBasedCompleteParam alignParams) {
		m_alignParams = alignParams;
	}
}