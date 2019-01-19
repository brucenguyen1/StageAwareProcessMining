package org.processmining.spm.processdiscovery.minerwrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.deckfour.xes.model.XLog;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.filter.FilterParams;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.evaluation.EvaluationResult;
import org.processmining.spm.processdiscovery.evaluation.Evaluator;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.utils.LogUtils;

public class AggregateWrapper extends AbstractMiner {
	Set<AbstractMiner> miners = new HashSet<>();
	
	public AggregateWrapper(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
	}
	
	public AggregateWrapper(boolean writeModelFiles, boolean writeResultFiles, FilterParams filterParams) {
		super(writeModelFiles, writeResultFiles, filterParams);
		miners.add(new InductiveMinerWrapper(false, false, filterParams));
		miners.add(new FodinaWrapper(false, false, filterParams));
	}
	
	public String getCodeName() {
		return "AM";
	}
	
	@Override
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		long startTime   = System.currentTimeMillis();
		System.out.println("Execute miner");
		FakePluginContext context = new FakePluginContext();
		MiningResult bestMiningRes=null;
		String bestMiner = "";
		
		for (AbstractMiner miner : miners) {
			MiningResult miningRes = miner.mineBestModel(log, metrics);
			bestMiningRes = this.compareMiningResult(bestMiningRes, miningRes, ModelMetrics.FSCORE, true);
		}
		
		if (bestMiningRes != null) {
			long endTime   = System.currentTimeMillis();
			bestMiningRes.setMiningTime(endTime - startTime);
			List<MiningResult> results = new ArrayList<>();
			results.add(bestMiningRes);

			String logName = LogUtils.getConceptName(log).toString();
			if (this.writeModelFiles) this.writeModelToFile(context, bestMiningRes, logName);
			if (this.writeResultFiles) {
				this.writeResultsToFile(results, logName, this.getCodeName(), "_bestMiner_" + bestMiner);
				System.out.println("DONE! " + results.size() + " results written to file.");
			}
		}
		else {
			System.out.println("Cannot find any best mining result!");
		}
		
		return bestMiningRes;
	}
}
