package org.processmining.sapm.processdiscovery.problem;

public abstract class Problem {
	private double scale = 0.0;
	
	public Problem() {
		
	}
	
	public Problem(double scale) {
		this.scale = scale;
	}
			
	public double getImprovementScale() {
		return this.scale;
	}
}
