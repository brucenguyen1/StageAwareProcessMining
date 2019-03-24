package org.processmining.sapm.processdiscovery.result;

import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public class PetrinetResult {
	private Petrinet m_petrinet = null;
	private Marking m_initialMarking = null;
	private Set<Marking> m_finalMarkings = null;
	
	public PetrinetResult(Petrinet net, Marking initialMarking, Set<Marking> finalMarkings) {
		m_petrinet = net;
		m_initialMarking = initialMarking;
		m_finalMarkings = finalMarkings;
	}
	
	public Petrinet getPetrinet() {
		return m_petrinet;
	}
	
	public void setPetrinet(Petrinet petrinet) {
		m_petrinet = petrinet;
	}
	
	public Marking getInitialMarking() {
		return m_initialMarking;
	}
	
	public void setInitialMarking(Marking initialMarking) {
		m_initialMarking = initialMarking;
	}
	
	public Set<Marking> getFinalMarkings() {
		return m_finalMarkings;
	}
	
	public void setFinalMarkings(Set<Marking> finalMarkings) {
		m_finalMarkings = finalMarkings;
	}	
}
