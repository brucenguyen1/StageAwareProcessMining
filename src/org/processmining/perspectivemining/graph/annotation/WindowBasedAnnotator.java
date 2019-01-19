package org.processmining.perspectivemining.graph.annotation;

import java.util.List;

import org.joda.time.Interval;
import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.datasource.SamplingWindowGenerator;
import org.processmining.perspectivemining.graph.model.AbstractFragment;
import org.processmining.perspectivemining.graph.model.EventRow;
import org.processmining.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;

public abstract class WindowBasedAnnotator extends GraphAnnotator {
	
	@Override
	public void annotateGraph(PerspectiveGraph pGraph, ProcessAbstraction processAbs, 
			PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception {
		
		//Clear all current annotations
		pGraph.getPGAnnotation().clear();
		
		List<Interval> samplingWindows = SamplingWindowGenerator.generate(input.getLogStartTime(), 
																		input.getLogEndTime(), 
																		setting.getWindowSize()*24*3600*1000); //days to millis

		pGraph.setSetting(setting);
		for (Interval window : samplingWindows) {
			this.annotateFromObservation(pGraph, window, processAbs);
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
	
	public abstract void annotateFromObservation(PerspectiveGraph pGraph, org.joda.time.Interval samplingWindow, 
												ProcessAbstraction abstraction) throws Exception;

}
