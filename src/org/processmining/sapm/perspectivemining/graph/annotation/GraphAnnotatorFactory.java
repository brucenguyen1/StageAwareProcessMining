package org.processmining.sapm.perspectivemining.graph.annotation;

import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.sapm.perspectivemining.graph.settings.GraphType;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.SamplingType;

public class GraphAnnotatorFactory {
	public static void annotateGraph(PerspectiveGraph pGraph, ProcessAbstraction processAbs, PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception {
		GraphAnnotator annotator = selectGraphAnnotator(setting);
		annotator.annotateGraph(pGraph, processAbs, input, setting);
	}
	
	private static GraphAnnotator selectGraphAnnotator(PerspectiveSettingObject setting) throws Exception {
		GraphAnnotator graphAnnotator = null;
		if (setting.getGraphType().equals(GraphType.INTRA_FRAGMENT)) {
			if (setting.getSamplingType() == SamplingType.WINDOW_BASED) {
				graphAnnotator = new WindowBasedIntraFragmentAnnotator();
			}
			else if (setting.getSamplingType() == SamplingType.CASE_BASED) {
				graphAnnotator = new TraceBasedIntraFragmentAnnotator();
			}
			else {
				throw new Exception("Unsupported sampling type!");
			}
		}		
		else if (setting.getGraphType().equals(GraphType.INTER_FRAGMENT)) {
			if (setting.getSamplingType() == SamplingType.WINDOW_BASED) {
				graphAnnotator = new WindowBasedInterFragmentAnnotator();
			}
			else if (setting.getSamplingType() == SamplingType.CASE_BASED) {
				graphAnnotator = new TraceBasedInterFragmentAnnotator();
			}
			else {
				throw new Exception("Unsupported sampling type!");
			}
		}
		else {
			throw new Exception("Unsupported graph type!");
		}
		return graphAnnotator;
	}
}
