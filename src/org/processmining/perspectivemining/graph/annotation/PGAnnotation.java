package org.processmining.perspectivemining.graph.annotation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * @author abolt
 * 
 *         Wrapper for all the perspectives that we can annotate in a transition
 *         system
 *
 */
public class PGAnnotation {

	//list of annotations. Extendable to cost, resource, etc...
	private Map<Node, Annotation> nodeAnnotationMap;
	private Map<Edge, Annotation> edgeAnnotationMap;

	public PGAnnotation() //default constructor
	{
		nodeAnnotationMap = new LinkedHashMap<Node, Annotation>();
		edgeAnnotationMap = new LinkedHashMap<Edge, Annotation>();
	}

	/**
	 * Returns the annotation object for the given element (state or
	 * transition). If the time annotation object for the element does not
	 * exist, a new annotation object is created for the element and returned.
	 * 
	 * @param node
	 *            for which to find the annotation
	 * @return annotation object for the element
	 * @throws Exception 
	 */

//	@SuppressWarnings("cast")
//	public Annotation getAnnotation(Object node) throws Exception {
//		if (node instanceof Node) {
//			if (nodeAnnotationMap.containsKey((Node) node)) {
//				return nodeAnnotationMap.get((Node) node);
//			}
//			else {
//				throw new Exception("Cannot find annotation for node " + ((Node)node).getId().toString());
//			}
//		} else if (node instanceof Edge) {
//			if (edgeAnnotationMap.containsKey((Edge) node)) {
//				return edgeAnnotationMap.get((Edge) node);
//			}
//			else {
//				throw new Exception("Cannot find annotation for edge " + ((Edge)node).toString());
//			}
//		}
//		return null;
//	}
	
	@SuppressWarnings("cast")
	public Annotation getAnnotation(Object node) {
		if (node instanceof Node) {
			if (!nodeAnnotationMap.containsKey((Node) node))
				nodeAnnotationMap.put((Node) node, new Annotation(node));
			return nodeAnnotationMap.get((Node) node);
		} else if (node instanceof Edge) {
			if (!edgeAnnotationMap.containsKey((Edge) node))
				edgeAnnotationMap.put((Edge) node, new Annotation(node));
			return edgeAnnotationMap.get((Edge) node);
		} else
			return null;
	}
	
	public void clear() {
		this.nodeAnnotationMap.clear();
		this.edgeAnnotationMap.clear();
	}

}
