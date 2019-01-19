package org.processmining.perspectivemining.graph.annotation;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.model.AbstractFragment;
import org.processmining.perspectivemining.graph.model.EventRow;
import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.perspectivemining.graph.model.ProcessInstance;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;

public abstract class TraceBasedAnnotator extends GraphAnnotator {

	@Override
	public void annotateGraph(PerspectiveGraph pGraph, ProcessAbstraction processAbs, 
										PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception {
		//Clear all current annotations
		pGraph.getPGAnnotation().clear();
		
		for (ProcessInstance instance : processAbs.getProcessInstances()) {
			this.annotateFromObservation(pGraph, instance);
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
	
	public abstract void annotateFromObservation(PerspectiveGraph pGraph, ProcessInstance instance) throws Exception;

}
