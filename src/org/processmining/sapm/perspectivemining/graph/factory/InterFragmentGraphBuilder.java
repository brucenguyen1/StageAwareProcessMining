package org.processmining.sapm.perspectivemining.graph.factory;

import org.gephi.graph.api.Node;
import org.processmining.sapm.perspectivemining.graph.model.AbstractFragment;
import org.processmining.sapm.perspectivemining.graph.model.EventRow;
import org.processmining.sapm.perspectivemining.graph.model.ObjectUtils;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessInstance;
import org.processmining.sapm.perspectivemining.graph.model.ProcessObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;

public class InterFragmentGraphBuilder extends GraphBuilder {

	
	@Override
	public void buildGraphFromInstance(ProcessInstance instance, PerspectiveGraph pGraph) throws Exception { 
		PerspectiveSettingObject pConfig = pGraph.getSetting();
		
		int i = 0;
		while ((i+1) < instance.getAbstractFragments().size()) {
			// node1 and node2 are consecutive
			EventRow sourceEvent = this.selectNode1Event(instance.getAbstractFragments().get(i));
			ProcessObject object1 = ObjectUtils.project(pConfig.getNodeSchema1(), sourceEvent);
 			Node node1 = pGraph.getOrCreateNode(object1.getValueString());

 			EventRow targetEvent = this.selectNode2Event(instance.getAbstractFragments().get(i+1));
 			ProcessObject object2 = ObjectUtils.project(pConfig.getNodeSchema2(), targetEvent);
 			Node node2 = pGraph.getOrCreateNode(object2.getValueString());
 			
 			pGraph.getOrCreateEdge(node1, node2);
			
			i++;
 		}
	}

	@Override
	public EventRow selectNode1Event(AbstractFragment fragment) {
		return fragment.getEndEvent();
	}
	
	@Override
	public EventRow selectNode2Event(AbstractFragment fragment) {
		return fragment.getStartEvent();
	}
	


}
