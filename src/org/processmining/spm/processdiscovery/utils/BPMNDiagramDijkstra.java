package org.processmining.spm.processdiscovery.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;

import de.vogella.algorithms.dijkstra.engine.DijkstraAlgorithm;
import de.vogella.algorithms.dijkstra.model.Edge;
import de.vogella.algorithms.dijkstra.model.Graph;
import de.vogella.algorithms.dijkstra.model.Vertex;

public class BPMNDiagramDijkstra {
	private DijkstraAlgorithm dijkstraAlgo = null;
	private BidiMap<BPMNNode,Vertex<BPMNNode>> nodeMap = new DualHashBidiMap<>();
	private BPMNDiagram d;
	
	public BPMNDiagramDijkstra(BPMNDiagram d) {
		this.d = d;
		this.dijkstraAlgo = this.getDijkstraAlgo();
	}
	
    private DijkstraAlgorithm getDijkstraAlgo() {
        //Create Dijistra nodes but keep a mapping from BPMN node to Dijistra node
        //so that we can apply for Dijistra edge in the next step
        for (BPMNNode node : d.getNodes()) {
            nodeMap.put(node, new Vertex<BPMNNode>(node.getId().toString(), node.getLabel(), node));
        }
        
        List<Edge<BPMNNode>> edges = new ArrayList<>();
        BPMNNode source;
        BPMNNode target;
        for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : d.getEdges()) {
            source = (BPMNNode)e.getSource();
            target = (BPMNNode)e.getTarget();
            edges.add(new Edge(source.getId()+"-"+target.getId(),
                               nodeMap.get(source),
                               nodeMap.get(target),
                               1));
        }
        
        return new DijkstraAlgorithm(new Graph<>(new ArrayList(nodeMap.values()),edges));
    }
    
    /*
    * Return empty list if source == target or no path found
    * Return non-empty list if path found
    */
    public ArrayList<BPMNNode> getPath(BPMNNode source, BPMNNode target) {
        ArrayList<BPMNNode> path = new ArrayList();
        if (source == target) {
            return path;
        }
        else {
            DijkstraAlgorithm algo = this.getDijkstraAlgo();
            algo.execute(this.getDijikstraVertex(source));
            ArrayList<Vertex> foundPath = algo.getPath(this.getDijikstraVertex(target));
            if (foundPath != null) {
	            for (Vertex v : foundPath) {
	            	path.add(nodeMap.getKey(v));
	            }
            }
            return path;
        }
    }
    
    private Vertex<BPMNNode> getDijikstraVertex(BPMNNode node) {
        if (nodeMap.containsKey(node)) {
            return nodeMap.get(node);
        } else {
            return null;
        }
    }
}
