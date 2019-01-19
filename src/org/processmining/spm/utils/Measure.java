package org.processmining.spm.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Measure {
	
	/*
	 * Argument:
	 * 1 : Rand Index
	 * 2 : Fowlkes–Mallows
	 * 3 : Jaccard
	 */
	public static double computeMeasure(List<Set<String>> phaseModel, List<Set<String>> truth, int method) throws Exception {
		//Conver all to lowercase
		List<Set<String>> lowercasePhases = new ArrayList<>();
		for (Set<String> phase : phaseModel) {
			Set<String> newPhase = new HashSet<>();
			for (String phaseItem : phase) {
				newPhase.add(phaseItem.toLowerCase());
			}
			lowercasePhases.add(newPhase);
		}
		List<Set<String>> lowercaseTruth = new ArrayList<>();
		for (Set<String> cluster : truth) {
			Set<String> newCluster = new HashSet<>();
			for (String clusterItem : cluster) {
				newCluster.add(clusterItem.toLowerCase());
			}
			lowercaseTruth.add(newCluster);
		}
		
		// Get all distinct labels
		Set<String> labels = new HashSet<String>();
		for (Set<String> cluster : lowercasePhases) {
			for (String label : cluster) {
				if (!labels.contains(label)) labels.add(label);
			}
 		}
		for (Set<String> cluster : lowercaseTruth) {
			for (String label : cluster) {
				if (!labels.contains(label)) labels.add(label);
			}
 		}
		
		// Set up distinct pair strings, delimited by @
		Set<String> pairs = new HashSet<String>();
		for (String label1 : labels) {
			for (String label2 : labels) {
				if (!label1.equals(label2) && !pairs.contains(label1 + "@" + label2) && !pairs.contains(label2 + "@" + label1)) {
					pairs.add(label1 + "@" + label2);
				}
			}
		}
		
		// Compute the a,b,c,d for the Rand Index formula
		int n11=0,n00=0,n01=0,n10=0;
		for (String pair : pairs) {
			String[] split = pair.split("@");
			Set<String> phase0 = null;
			Set<String> phase1 = null;
			Set<String> truth0 = null;
			Set<String> truth1 = null;
			
			for (Set<String> phase : lowercasePhases) {
				if (phase.contains(split[0])) {
					phase0 = phase;
				}
				if (phase.contains(split[1])) {
					phase1 = phase;
				}
			}
			
			for (Set<String> truthSet : lowercaseTruth) {
				if (truthSet.contains(split[0])) {
					truth0 = truthSet;
				}
				if (truthSet.contains(split[1])) {
					truth1 = truthSet;
				}
			}
			
			if (phase0 == phase1 && truth0 == truth1) {
				n11++;
			}
			else if (phase0 != phase1 && truth0 != truth1) {
				n00++;
			}
			else if (phase0 == phase1 && truth0 != truth1) {
				n10++;
			}
			else if (phase0 != phase1 && truth0 == truth1) {
				n01++;
			}
		}
		
		double measure = 0; 
		if (method==1) { //Rand Index
			measure = 1.0*(n11+n00)/(n11+n10+n01+n00);
		}
		else if (method==2) { //Fowlkes–Mallows
			measure = 1.0*n11/Math.sqrt((n11+n10)*(n11+n01));
		}
		else if (method==3) { //Jaccard
			measure = 1.0*(n11)/(n11+n10+n01);
		}
		
		return measure;	
	}
	

	
	private double factorial(int n) {
		double result = 1;
		for (int i=2;i<=n;i++) {
			result = result*i;
		}
		return result;
	}
}
