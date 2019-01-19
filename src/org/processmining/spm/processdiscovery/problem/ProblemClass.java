package org.processmining.spm.processdiscovery.problem;

public class ProblemClass extends Problem {
	private String eventClassName = null;
	
	public ProblemClass(String eventClassName, double scale) {
		super(scale);
		this.eventClassName = eventClassName;
	}
	
	public String getEventClassName() {
		return eventClassName;
	}
	
	@Override
	public String toString() {
		return eventClassName;
	}
}
