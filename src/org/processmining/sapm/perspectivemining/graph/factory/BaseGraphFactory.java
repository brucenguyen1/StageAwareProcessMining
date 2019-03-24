package org.processmining.sapm.perspectivemining.graph.factory;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.processmining.sapm.perspectivemining.graph.model.DifferentialGraph;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;

public class BaseGraphFactory {
	public static final String ATT_FREQUENCY = "frequency";
	public static final String ATT_TIMESTAMP_START = "start";
	public static final String ATT_TIMESTAMP_COMPLETE = "complete";
	public static final String ATT_DURATION = "duration";
	public static final String ATT_IS_NODE1 = "is_node1";
	public static final String ATT_IS_NODE2 = "is_node2";
	
	public static final String ATT_GRAPH1_PRESENCE = "graph1";
	public static final String ATT_GRAPH2_PRESENCE = "graph2";
	
	public static ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
	public static Project project = null; //projectController.getCurrentProject(); //the new project has a new default workspace
	
	public static PerspectiveGraph createBasePerspectiveGraph() {
		if (project == null) {
			projectController.newProject();
			project = projectController.getCurrentProject();
		}
		Workspace wp = projectController.newWorkspace(project);
		GraphModel graphModel = ((GraphController) Lookup.getDefault().lookup(GraphController.class)).getGraphModel(wp);
		
		//Do this first as graph model must be empty when setting its configuration
		Configuration config = graphModel.getConfiguration();
		config.setTimeRepresentation(TimeRepresentation.INTERVAL);
		config.setEdgeWeightColumn(true);
		config.setEdgeWeightType(IntervalDoubleMap.class); // must be IntervalDoubleMap to be dynamic
		graphModel.setConfiguration(config);
		
		graphModel.getNodeTable().addColumn(ATT_FREQUENCY, IntervalDoubleMap.class); // IntervalDoubleMap for dynamic column
		graphModel.getNodeTable().addColumn(ATT_DURATION, IntervalDoubleMap.class); // milliseconds from epoch start time
		graphModel.getNodeTable().addColumn(ATT_IS_NODE1, Boolean.class); // whether this node is Node1 Type
		graphModel.getNodeTable().addColumn(ATT_IS_NODE2, Boolean.class); // whether this node is Node2 Type

		
		return new PerspectiveGraph(wp, graphModel);
	}
	
	public static DifferentialGraph createBaseDifferentialGraph() {
		Workspace wp = projectController.newWorkspace(project);
		GraphModel graphModel = ((GraphController) Lookup.getDefault().lookup(GraphController.class)).getGraphModel(wp);
		graphModel.getNodeTable().addColumn(ATT_FREQUENCY, Double.class);
		graphModel.getNodeTable().addColumn(ATT_TIMESTAMP_START, Long.class); // milliseconds from epoch start time
		graphModel.getNodeTable().addColumn(ATT_TIMESTAMP_COMPLETE, Long.class); // milliseconds from epoch start time
		graphModel.getNodeTable().addColumn(ATT_GRAPH1_PRESENCE, Boolean.class);
		graphModel.getNodeTable().addColumn(ATT_GRAPH2_PRESENCE, Boolean.class);
		
		graphModel.getEdgeTable().addColumn(ATT_GRAPH1_PRESENCE, Boolean.class);
		graphModel.getEdgeTable().addColumn(ATT_GRAPH2_PRESENCE, Boolean.class);
		graphModel.getConfiguration().setEdgeWeightColumn(true);
		graphModel.getConfiguration().setEdgeWeightType(Double.class);
		
		return new DifferentialGraph(wp, graphModel);
	}
}
