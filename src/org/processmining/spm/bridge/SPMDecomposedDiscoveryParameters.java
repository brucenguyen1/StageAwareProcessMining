package org.processmining.spm.bridge;

import org.deckfour.xes.model.XLog;
import org.processmining.decomposedminer.configurations.DecomposedDiscoveryConfigurationManager;
import org.processmining.decomposedminer.parameters.DecomposedDiscoveryParameters;
import org.processmining.spm.processdiscovery.minerwrapper.Miner;

/**
 * This parameter extends DecomposedMiner with minerwrapper
 * @author Bruce
 *
 */
public class SPMDecomposedDiscoveryParameters extends DecomposedDiscoveryParameters {
	private Miner minerWrapper = null; //Bruce

	public SPMDecomposedDiscoveryParameters(XLog log) {
		super(log);
		//register SPMDecomposedClusteringDiscoveryConfiguration as default configuration
		DecomposedDiscoveryConfigurationManager.getInstance().register(new SPMDecomposedClusteringDiscoveryConfiguration(), true); 
		//then, call set configuration to override the default configuration set in the super class
		this.setConfiguration(DecomposedDiscoveryConfigurationManager.getInstance().getConfiguration(null).getName()); 
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
