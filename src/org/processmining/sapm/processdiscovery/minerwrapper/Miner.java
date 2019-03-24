package org.processmining.sapm.processdiscovery.minerwrapper;

import java.util.SortedMap;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.result.MiningResult;

public interface Miner {
	public String getCodeName();
	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> paramValueMap) throws Exception;
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception;
	public MiningResult mineBestModelRepeated(XLog log, ModelMetrics metrics, int times) throws Exception;
}
