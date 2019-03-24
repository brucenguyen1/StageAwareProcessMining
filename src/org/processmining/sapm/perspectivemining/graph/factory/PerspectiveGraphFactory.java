package org.processmining.sapm.perspectivemining.graph.factory;

import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.model.DifferentialGraph;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.sapm.perspectivemining.graph.settings.GraphType;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;

public class PerspectiveGraphFactory {
	public static PerspectiveGraph createPerspectiveGraph(ProcessAbstraction processAbs, PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception {
		GraphBuilder graphBuilder = selectGraphType(setting);
		return graphBuilder.buildGraph(processAbs, input, setting);
	}
	
	private static GraphBuilder selectGraphType(PerspectiveSettingObject setting) throws Exception {
		GraphBuilder graphBuilder = null;
		if (setting.getGraphType().equals(GraphType.INTRA_FRAGMENT)) {
			graphBuilder = new IntraFragmentGraphBuilder();
		}		
		else if (setting.getGraphType().equals(GraphType.INTER_FRAGMENT)) {
			graphBuilder = new InterFragmentGraphBuilder();
		}
		else {
			throw new Exception("Unsupported graph type!");
		}
		return graphBuilder;
	}
	
	public static DifferentialGraph createDifferentialGraph(ProcessAbstraction abs1, ProcessAbstraction abs2, PerspectiveInputObject input1, PerspectiveInputObject input2, PerspectiveSettingObject setting) throws Exception {
		PerspectiveGraph pGraph1 = createPerspectiveGraph(abs1, input1, setting);
		PerspectiveGraph pGraph2 = createPerspectiveGraph(abs2, input2, setting);
		
		DifferentialGraph dGraph = BaseGraphFactory.createBaseDifferentialGraph();
		dGraph.setSetting(setting);
		dGraph.setGraph1(pGraph1);
		dGraph.setGraph2(pGraph2);
		dGraph.compare();
		return dGraph;
	}
	

}
