package org.processmining.sapm.processdiscovery.bridge;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetArrayFactory;
import org.processmining.acceptingpetrinet.parameters.PackPetrinetParameters;
import org.processmining.acceptingpetrinet.parameters.UndoubleAcceptingPetriNetParameters;
import org.processmining.acceptingpetrinet.plugins.PackPetrinetPlugin;
import org.processmining.acceptingpetrinet.plugins.UndoubleAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinetclassicalreductor.algorithms.ReduceUsingMurataRulesAlgorithm;
import org.processmining.acceptingpetrinetclassicalreductor.parameters.ReduceUsingMurataRulesParameters;
import org.processmining.decomposedminer.configurations.impl.DecomposedClusteringDiscoveryConfiguration;
import org.processmining.decomposedminer.parameters.DecomposedDiscoveryParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.result.MiningResult;

/**
 * This class extends DecomposedMiner to apply minerwrapper
 * @author Bruce
 *
 */
public class SPMDecomposedClusteringDiscoveryConfiguration extends DecomposedClusteringDiscoveryConfiguration {
	private double bestScore = 0.0;
	
	@Override
	public String getName() {
		return "SPMDecomposedClusteringDiscoveryConfiguration";
	}
	
	@Override
	public AcceptingPetriNetArray getNets(PluginContext context, EventLogArray logs,
			DecomposedDiscoveryParameters parameters) {
		//This method must be always called with SPMDecomposedDiscoveryParameters
		SPMDecomposedDiscoveryParameters parameters2 = (SPMDecomposedDiscoveryParameters)parameters;
		AcceptingPetriNetArray nets = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray();
		nets.init();
		try {
			for (int i=0;i<logs.getSize();i++) {
				System.out.println("+++++++++++++++  Mine Best SubModel for Cluster #" + (i+1) + "    +++++++++++++");
				MiningResult result = parameters2.getMinerWrapper().mineBestModel(logs.getLog(i), ModelMetrics.FSCORE);
				
				//Convert from Petrinet to AcceptingPetrinet
				PackPetrinetParameters packParams = new PackPetrinetParameters();
				packParams.setTryConnections(false);
				AcceptingPetriNet apn = (new PackPetrinetPlugin()).runDefault(context, result.getPetrinetRes().getPetrinet());
				UndoubleAcceptingPetriNetParameters undoubleParams = new UndoubleAcceptingPetriNetParameters();
				undoubleParams.setTryConnections(false);
				AcceptingPetriNet acceptingNet = (new UndoubleAcceptingPetriNetPlugin()).run(context, apn, undoubleParams);
				
				//Reduce the net
				ReduceUsingMurataRulesAlgorithm reductor = new ReduceUsingMurataRulesAlgorithm();
				ReduceUsingMurataRulesParameters reductorParameters = new ReduceUsingMurataRulesParameters();
				AcceptingPetriNet reducedNet = reductor.apply(null, acceptingNet, reductorParameters);
				if (reducedNet != null) {
					acceptingNet = reducedNet;
				}
				
				nets.addNet(acceptingNet);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return nets;
	}
}
