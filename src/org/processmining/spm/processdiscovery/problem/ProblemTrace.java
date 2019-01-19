package org.processmining.spm.processdiscovery.problem;

import org.deckfour.xes.model.XTrace;

public class ProblemTrace extends Problem {
	private XTrace trace;
	
	public ProblemTrace(XTrace trace) {
		super();
		this.trace = trace;
	}
	
	public XTrace getTrace() {
		return this.trace;
	}
	
}
