package org.processmining.sapm.stagemining.baselines;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarraycreator.connections.ConvertCausalActivityGraphToActivityClusterArrayConnection;
import org.processmining.activityclusterarraycreator.dialogs.ConvertCausalActivityGraphToActivityClusterArrayDialog;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Bruce: this class is created to avoid the use of the original class "ConvertCausalActivityGraphToActivityClusterArrayPlugin"
 * This class will call the modified code to create non-overlapping clusters
 * @author Bruce
 *
 */
public class ConvertCausalActivityGraphToActivityClusterArrayPlugin2 extends
		ConvertCausalActivityGraphToActivityClusterArrayAlgorithm2 {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Clusters, UI", requiredParameterLabels = { 0 })
	public ActivityClusterArray runUI(UIPluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayParameters parameters = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		ConvertCausalActivityGraphToActivityClusterArrayDialog dialog = new ConvertCausalActivityGraphToActivityClusterArrayDialog(
				context, graph, parameters);
		InteractionResult result = context.showWizard("Configure creation", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnection(context, graph, parameters);
	}

	/**
	 * @deprecated Use runUI() instead.
	 */
	@Deprecated
	public ActivityClusterArray convertUI(UIPluginContext context, CausalActivityGraph graph) {
		return runUI(context, graph);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Clusters, Default", requiredParameterLabels = { 0 })
	public ActivityClusterArray runDefault(PluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayParameters parameters = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		return runConnection(context, graph, parameters);
	}

	/**
	 * @deprecated Use runDefault() instead.
	 */
	@Deprecated
	public ActivityClusterArray convertDefault(PluginContext context, CausalActivityGraph graph) {
		return runDefault(context, graph);
	}

	@PluginVariant(variantLabel = "Create Clusters, Parameters", requiredParameterLabels = { 0, 1 })
	public ActivityClusterArray run(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		return runConnection(context, graph, parameters);
	}

	/**
	 * @deprecated Use run() instead.
	 */
	@Deprecated
	public ActivityClusterArray convertParameters(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		return run(context, graph, parameters);
	}

	private ActivityClusterArray runConnection(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
//		if (parameters.isTryConnections()) {
//			Collection<ConvertCausalActivityGraphToActivityClusterArrayConnection> connections;
//			try {
//				connections = context.getConnectionManager().getConnections(
//						ConvertCausalActivityGraphToActivityClusterArrayConnection.class, context, graph);
//				for (ConvertCausalActivityGraphToActivityClusterArrayConnection connection : connections) {
//					if (connection.getObjectWithRole(ConvertCausalActivityGraphToActivityClusterArrayConnection.GRAPH)
//							.equals(graph) && connection.getParameters().equals(parameters)) {
//						return connection
//								.getObjectWithRole(ConvertCausalActivityGraphToActivityClusterArrayConnection.CLUSTERS);
//					}
//				}
//			} catch (ConnectionCannotBeObtained e) {
//			}
//		}
		ActivityClusterArray clusters = apply(context, graph, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new ConvertCausalActivityGraphToActivityClusterArrayConnection(graph, clusters, parameters));
		}
		return clusters;
	}

}
