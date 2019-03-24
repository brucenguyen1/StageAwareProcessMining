package org.processmining.sapm.processdiscovery.problem;

import java.util.ArrayList;
import java.util.List;

public class ProblemRelation extends Problem {
	private List<String> relation;
	private String relationString;
	
	public ProblemRelation(String relationString) {
		this.relationString = relationString;
		this.relation = new ArrayList<>();
		String[] classes = relationString.split("@");
		for (int i=0;i<classes.length;i++) {
			this.relation.add(classes[i]);
		}
	}
	
	public List<String> getRelation() {
		return this.relation;
	}
	
	public String getRelationString() {
		return this.relationString;
	}
	
	@Override
	public String toString() {
		return relationString;
	}
}
