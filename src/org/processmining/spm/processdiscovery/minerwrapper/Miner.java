package org.processmining.spm.processdiscovery.minerwrapper;

import java.util.SortedMap;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.result.MiningResult;

public interface Miner {
	public String getCodeName();
	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> paramValueMap) throws Exception;
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception;
}
