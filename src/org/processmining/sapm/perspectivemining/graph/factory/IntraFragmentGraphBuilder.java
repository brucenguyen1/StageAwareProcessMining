package org.processmining.sapm.perspectivemining.graph.factory;

import java.util.ArrayList;
import java.util.List;

import org.gephi.graph.api.Node;
import org.processmining.sapm.perspectivemining.graph.model.AbstractFragment;
import org.processmining.sapm.perspectivemining.graph.model.EventRow;
import org.processmining.sapm.perspectivemining.graph.model.Fragment;
import org.processmining.sapm.perspectivemining.graph.model.ObjectUtils;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessInstance;
import org.processmining.sapm.perspectivemining.graph.model.ProcessObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;

public class IntraFragmentGraphBuilder extends GraphBuilder {
	
	@Override
	public void buildGraphFromInstance(ProcessInstance instance, PerspectiveGraph pGraph) throws Exception {
		PerspectiveSettingObject pConfig = pGraph.getSetting();
		
		for (Fragment fragment : instance.getAbstractFragments()) {
			List<Node> fragmentNodes = new ArrayList<>();
			for (EventRow event : fragment) {
				ProcessObject object1 = ObjectUtils.project(pConfig.getNodeSchema1(), event);
				Node node1 = pGraph.getOrCreateNode(object1.getValueString()); //create node if not exists
				fragmentNodes.add(node1);
				
				if (!pConfig.isNode1Node2SameSchema()) {
					ProcessObject object2 = ObjectUtils.project(pConfig.getNodeSchema2(), event);
					Node node2 = pGraph.getOrCreateNode(object2.getValueString()); //create node if not exists
					fragmentNodes.add(node2);
				}
			}
			
			for (Node node1 : fragmentNodes) {
				for (Node node2 : fragmentNodes) {
					if (node1 == node2) {
						continue;
					}
					pGraph.getOrCreateEdge(node1, node2);
				}
			}
		}
		
	}
	
	@Override
	public EventRow selectNode1Event(AbstractFragment fragment) {
		return null;
	}
	
	@Override
	public EventRow selectNode2Event(AbstractFragment fragment) {
		return null;
	}

}
