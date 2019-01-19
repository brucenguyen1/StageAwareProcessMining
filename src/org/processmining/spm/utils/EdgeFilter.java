package org.processmining.spm.utils;

import java.util.BitSet;
import java.util.Iterator;

import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.hypergraph.abs.Vertex;
import org.processmining.spm.stagemining.graph.WeightedDirectedEdge;
import org.processmining.spm.stagemining.graph.WeightedDirectedGraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


/**
 * Filter log based on filtering directly follows graph
 * Only filter out edges but preserve all nodes.
 * The graph remains connected, no orphan nodes
 * Copy this method from Fuzzy Miner
 * @author Bruce
 *
 */
public class EdgeFilter {
	private BitSet preserveMask;
	private WeightedDirectedGraph graph=null;
	private BiMap<Vertex,Integer> indexMap = null;
	
	public EdgeFilter() {
		graph = null;
		preserveMask = null;
		indexMap = HashBiMap.create();
	}
	
	public WeightedDirectedGraph filter(WeightedDirectedGraph graph) {
		this.graph = graph.clone();
		this.graph.setName("filtered graph");
		for (Vertex v : graph.getVertices()) {
			indexMap.put(v, indexMap.size());
		}
		preserveMask = new BitSet(indexMap.size() * indexMap.size());
		buildBitMask();
		filterEdges();
		return this.graph;
	}

	private void buildBitMask() {
		Iterator<Vertex> iterator = graph.getVertices().iterator();
		while (iterator.hasNext()) {
			Vertex node = iterator.next();
			setBitMask(node);
		}
	}

	private void setBitMask(Vertex v) {
		int nodeIndex = indexMap.get(v);
		// find best predecessor and successor
		int bestOutIndex = -1;
		int bestInIndex = -1;
		double bestOutWeight = 0.0;
		double bestInWeight = 0.0;
		double maxOutWeight, maxInWeight;
		for (int x = 0; x < indexMap.size(); x++) {
			if (x == nodeIndex) {
				continue;
			} // skip self
			maxOutWeight = graph.getWeight(v, (Vertex)indexMap.inverse().get(x));
			if (maxOutWeight > bestOutWeight) {
				bestOutWeight = maxOutWeight;
				bestOutIndex = x;
			}
			maxInWeight = graph.getWeight((Vertex)indexMap.inverse().get(x), v);
			if (maxInWeight > bestInWeight) {
				bestInWeight = maxInWeight;
				bestInIndex = x;
			}
		}
		
		// flag best predecessor and successor, if any
		if (bestOutIndex >= 0) {
			setBitMask(nodeIndex, bestOutIndex, true);
		}
		if (bestInIndex >= 0) {
			setBitMask(bestInIndex, nodeIndex, true);
		}
	}
	
	private void filterEdges() {
		for (int x = 0; x < indexMap.size(); x++) {
			for (int y = 0; y < indexMap.size(); y++) {
				if (x == y) {
					// no self-loops handled here..
					continue;
				} else if (getBitMask(x, y) == false) {
					WeightedDirectedEdge<IVertex> edge = graph.getDirectedEdge(indexMap.inverse().get(x),indexMap.inverse().get(y));
					if (edge != null) {
						graph.removeEdge(edge);
						graph.removeEdge2(edge);
					}
				}
			}
		}
	}
	
	private void setBitMask(int x, int y, boolean value) {
		preserveMask.set(translateIndex(x, y), value);
	}

	private boolean getBitMask(int x, int y) {
		return preserveMask.get(translateIndex(x, y));
	}

	private int translateIndex(int x, int y) {
		return ((x * indexMap.size()) + y);
	}
}
