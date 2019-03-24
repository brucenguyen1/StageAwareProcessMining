package org.processmining.sapm.processdiscovery.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.swing.event.InternalFrameListener;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.graph.abs.IDirectedGraph;
import org.jbpt.graph.abs.IFragment;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.sapm.processdiscovery.graph.BPMNDirectedEdge;
import org.processmining.sapm.processdiscovery.graph.BPMNDirectedGraph;
import org.processmining.sapm.processdiscovery.graph.BPMNVertex;

import de.hpi.bpt.graph.algo.rpst.RPSTNode;

public class RPSTHelper {
	private BPMNDiagram diagram = null;
	private RPST<BPMNDirectedEdge, BPMNVertex> rpst = null;
	private BidiMap<BPMNNode, BPMNVertex> mapping = new DualHashBidiMap<>();	
	
	public void initialize(BPMNDiagram diagram) {
		this.mapping.clear();
		this.diagram = diagram;
		this.rpst = null;
		this.createRPST(diagram);
	}
	
	/**
	 * 1st element: RPST built from the BPMN diagram
	 * 2nd element: bidirectional mapping between the RPST node and the BPMN node
	 * @param bpmnModel
	 * @return
	 */
	private void createRPST(BPMNDiagram bpmnModel) {
		IDirectedGraph<BPMNDirectedEdge, BPMNVertex> graph = new BPMNDirectedGraph();
		
		for (Flow f : bpmnModel.getFlows()) {
        	BPMNNode src = f.getSource();
            BPMNVertex srcRPST;
            if (!mapping.containsKey(src)) {
            	srcRPST = new BPMNVertex(src);
            	mapping.put(src, srcRPST);
            	graph.addVertex(srcRPST);
            }
            else {
            	srcRPST = mapping.get(src);
            }
            
            BPMNNode tgt = f.getTarget();
            BPMNVertex tgtRPST;
            if (!mapping.containsKey(tgt)) {
            	tgtRPST = new BPMNVertex(tgt);
            	mapping.put(tgt, tgtRPST);
            	graph.addVertex(tgtRPST);
            }
            else {
            	tgtRPST = mapping.get(tgt);
            }

            BPMNDirectedEdge edge = graph.addEdge(srcRPST, tgtRPST);
            edge.setName(f.getLabel());
            edge.setSequenceFlow(f);
        }
		
		rpst = new RPST<BPMNDirectedEdge, BPMNVertex>(graph);
	}
	
	public List<BPMNNode> findContainingANDFragment(BPMNNode n) {
		BPMNVertex v = mapping.get(n);
		
		//----------------------------------------------------
		// Traverse breadth-first the RPST from bottom up to 
		// search for AND structure that contains the input vertex
		//----------------------------------------------------
		Queue<IRPSTNode<BPMNDirectedEdge, BPMNVertex>> queue = new LinkedList<IRPSTNode<BPMNDirectedEdge,BPMNVertex>>();
		Stack<IRPSTNode<BPMNDirectedEdge, BPMNVertex>> stack = new Stack<IRPSTNode<BPMNDirectedEdge,BPMNVertex>>();
		queue.add(rpst.getRoot());
		while (!queue.isEmpty()) {
			IRPSTNode<BPMNDirectedEdge, BPMNVertex> node = queue.poll();
			for (IRPSTNode<BPMNDirectedEdge, BPMNVertex> child : rpst.getChildren(node)) {
				queue.add(child);
			}
			stack.push(node);
		}
		
		//Now traverse bottom-up 
		List<BPMNNode> andNodes = new ArrayList<>();
		while (!stack.isEmpty()) {
			IRPSTNode<BPMNDirectedEdge, BPMNVertex> node = stack.pop();
			if (node.getType() == TCType.BOND && node.getEntry().isANDSplit() && node.getExit().isANDJoin()) {
				if (this.contains(node, v)) {
					andNodes.add(node.getEntry().getFlowNode());
					andNodes.add(node.getExit().getFlowNode());
					break;
				}
			}
		}
		
		return andNodes;
	}
	
	public BidiMap<BPMNNode, BPMNVertex> getRPSTMapping() {
		return this.mapping;
	}
	
	/**
	 * Check if a fragment associated with an RPST node contains a graph vertex
	 * @param node: RPST node
	 * @param v: graph vertex
	 * @return: true/false
	 */
	private boolean contains(IRPSTNode<BPMNDirectedEdge, BPMNVertex> node, BPMNVertex v) {
		IFragment<BPMNDirectedEdge, BPMNVertex> fragment = node.getFragment();
		for (BPMNDirectedEdge e : fragment) {
			if (e.getSource() == v || e.getTarget() == v) {
				return true;
			}
		}
		return false;
	}
}
