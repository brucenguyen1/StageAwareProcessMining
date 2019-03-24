package org.processmining.sapm.processdiscovery.graph;

import org.jbpt.hypergraph.abs.IGObject;
import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.hypergraph.abs.Vertex;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;

public class BPMNVertex extends Vertex {
	private BPMNNode node = null;
	
	public BPMNVertex(BPMNNode node) {
		this.node = node;
		this.setName(node.getLabel());
		this.setId(node.getId().toString());
	}
	
	public BPMNNode getFlowNode() {
		return node;
	}
	
	
    public boolean isANDSplit() {
        return BPMNUtils.isANDSplit(node);
    } 

   public boolean isANDJoin() {
	   return BPMNUtils.isANDJoin(node);
    }
    
    public boolean isXORSplit() {
    	return BPMNUtils.isXORSplit(node);
    } 
    
    public boolean isXORJoin() {
    	return BPMNUtils.isXORJoin(node);
    }
    
    public boolean isORSplit() {
    	return BPMNUtils.isORSplit(node);
    }
    
    public boolean isORJoin() {
    	return BPMNUtils.isORJoin(node);
    }   
    
    public boolean isActivity() {
    	return (node instanceof Activity);
    }     
    
}
