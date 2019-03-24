package org.processmining.sapm.stagemining.baselines;

import org.deckfour.xes.model.XLog;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixminer.parameters.DiscoverFromEventLogParameters;
import org.processmining.causalactivitymatrixminer.plugins.DiscoverFromEventLogPlugin;
import org.processmining.decomposedminer.configurations.impl.DecomposedAbstractDiscoveryConfiguration;
import org.processmining.decomposedminer.parameters.DecomposedDiscoveryParameters;
import org.processmining.framework.plugin.PluginContext;

import com.fluxicon.slickerbox.components.NiceSlider;

//Bruce: This class is used to replace the class DecomposedFlexibleDiscoveryConfiguration
//Only used for Stage-Based Process Discovery
public class DecomposedFlexibleDiscoveryConfiguration2 extends DecomposedAbstractDiscoveryConfiguration {

	private String miner;
	private double zeroValue;
	private double concurrencyRatio;
	
	public CausalActivityMatrix getMatrix(PluginContext context, XLog log, DecomposedDiscoveryParameters parameters) {
		DiscoverFromEventLogPlugin plugin = new DiscoverFromEventLogPlugin();
		DiscoverFromEventLogParameters params = new DiscoverFromEventLogParameters(log);
		params.setClassifier(parameters.getClassifier());
		params.setMiner(miner);
		return plugin.run(context, log, params);
	}

	public CausalActivityGraph getGraph(PluginContext context, CausalActivityMatrix matrix,
			DecomposedDiscoveryParameters parameters) {
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin plugin = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters params = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		params.setZeroValue(zeroValue);
		params.setConcurrencyRatio(concurrencyRatio);
		return plugin.run(context, matrix, params);
	}
	
	//Bruce: Override this method to call to ConvertCausalActivityGraphToActivityClusterArrayPlugin2
	//and ConvertCausalActivityGraphToActivityClusterArrayAlgorithm2
	@Override
	public ActivityClusterArray getClusters(PluginContext context, CausalActivityGraph graph,
			DecomposedDiscoveryParameters parameters) {
		ConvertCausalActivityGraphToActivityClusterArrayPlugin2 plugin = new ConvertCausalActivityGraphToActivityClusterArrayPlugin2();
		ConvertCausalActivityGraphToActivityClusterArrayParameters params = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		params.setCheckBackwardArc(true); // Bruce: set to true as it is more suitable for process graph
		params.setIncludeAll(true);
		params.setTryConnections(parameters.isTryConnections());
		return plugin.run(context, graph, params);
	}

	public String getMiner() {
		return miner;
	}

	public void setMiner(String miner) {
		this.miner = miner;
	}

	public double getZeroValue() {
		return zeroValue;
	}

	public void setZeroValue(double zeroValue) {
		this.zeroValue = zeroValue;
	}

	public double getConcurrencyRatio() {
		return concurrencyRatio;
	}

	public void setConcurrencyRatio(double concurrencyRatio) {
		this.concurrencyRatio = concurrencyRatio;
	}

	public String getName() {
		return miner;
	}

	public void update(NiceSlider slider) {
	}
}
