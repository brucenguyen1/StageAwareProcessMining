package org.processmining.sapm.processdiscovery.bridge;

import org.deckfour.xes.model.XLog;
import org.processmining.decomposedminer.configurations.DecomposedDiscoveryConfiguration;
import org.processmining.decomposedminer.configurations.DecomposedDiscoveryConfigurationManager;
import org.processmining.decomposedminer.parameters.DecomposedDiscoveryParameters;
import org.processmining.sapm.processdiscovery.minerwrapper.Miner;

/**
 * This parameter extends DecomposedMiner with minerwrapper
 * @author Bruce
 *
 */
public class SPMDecomposedDiscoveryParameters extends DecomposedDiscoveryParameters {
	private Miner minerWrapper = null; //Bruce

	public SPMDecomposedDiscoveryParameters(XLog log) {
		super(log);
		DecomposedDiscoveryConfiguration config = new SPMDecomposedClusteringDiscoveryConfiguration();
		this.setConfiguration(config.getName());
		this.setTryConnections(false);
		DecomposedDiscoveryConfigurationManager.getInstance().register(config, false); 
	}

	public SPMDecomposedDiscoveryParameters(SPMDecomposedDiscoveryParameters parameters) {
		super(parameters);
	}

	public void setMinerWrapper(Miner minerWrapper) {
		this.minerWrapper = minerWrapper;
	}
	
	public Miner getMinerWrapper() {
		return this.minerWrapper;
	}

}
