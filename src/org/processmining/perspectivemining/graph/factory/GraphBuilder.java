package org.processmining.perspectivemining.graph.factory;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.model.AbstractFragment;
import org.processmining.perspectivemining.graph.model.EventRow;
import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.perspectivemining.graph.model.ProcessInstance;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;

public abstract class GraphBuilder {
	
	public PerspectiveGraph buildGraph(ProcessAbstraction processAbs, PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception {
		//Create annotated perspective graph
		PerspectiveGraph pGraph = BaseGraphFactory.createBasePerspectiveGraph();
		pGraph.setSetting(setting);
		for (ProcessInstance instance : processAbs.getProcessInstances()) {
			this.buildGraphFromInstance(instance, pGraph);
		}
		
		return pGraph;
	}
	
	public abstract void buildGraphFromInstance(ProcessInstance instance, PerspectiveGraph pGraph) throws Exception;
	
	public abstract EventRow selectNode1Event(AbstractFragment fragment);
	
	public abstract EventRow selectNode2Event(AbstractFragment fragment);
	
	protected long calculateDuration(EventRow source, EventRow target) throws Exception {
		if (target.getTime() < source.getTime()) {
			return 0;
		}
		else {
			return (target.getTime() - source.getTime());
		}
	}

}
