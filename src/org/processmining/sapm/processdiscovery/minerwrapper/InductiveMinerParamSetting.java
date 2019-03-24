package org.processmining.sapm.processdiscovery.minerwrapper;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;

public class InductiveMinerParamSetting {
	public static XEventClassifier getXEventClassifier() {
		return XLogInfoImpl.NAME_CLASSIFIER;
	}
	
	public static MiningParameters getMiningParameters() {
		return new MiningParametersIMf();
	}
	
}
