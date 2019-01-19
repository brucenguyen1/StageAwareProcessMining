package org.processmining.spm.processdiscovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.spm.processdiscovery.minerwrapper.AbstractMiner;
import org.processmining.spm.processdiscovery.minerwrapper.Miner;
import org.processmining.spm.processdiscovery.problem.Problem;
import org.processmining.spm.processdiscovery.problem.ProblemType;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.stagemining.StageListModel;
import org.processmining.spm.utils.LogUtils;

public class DivideAndConquerMiner extends AbstractMiner {
	protected List<StageSubModelMiner> subMiners;
	
	public DivideAndConquerMiner(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
		subMiners = new ArrayList<>();
	}
	
	public MiningResult mineBestModel(XLog log, Miner baseMiner, ModelMetrics metric) throws Exception {
		return null;
	}
	
	public MiningResult mineBestModel(XLog log, Miner baseMiner, ModelMetrics metric, StageListModel stageModel) throws Exception {
		return null;
	}
	
	public void addSubMiner(StageSubModelMiner subMiner) {
		this.subMiners.add(subMiner);
	}
	
	public StageSubModelMiner getSubMiner(int index) {
		if (index > 0 && index < this.subMiners.size()) {
			return this.subMiners.get(index); 
		}
		else {
			return null;
		}
	}
	
	public int getNumberOfSubMiners() {
		return this.subMiners.size();
	}
	
	/**
	 * @param wholeRe
	 * @param noiseType
	 * @return
	 */
	public Set<Problem> diagnose(MiningResult wholeRes, ProblemType noiseType, double threshold) {
		return null;
	}
	
	public void writeSubMinerLogs(String filePath, String suffix) throws IOException {
		XesXmlSerializer writer = new XesXmlGZIPSerializer();
		for (StageSubModelMiner subMiner : subMiners) {
			FileOutputStream fos = new FileOutputStream(filePath + File.separator + LogUtils.getConceptName(subMiner.getLog()) + suffix + ".xes.gz");
			writer.serialize(subMiner.getLog(), fos);
			fos.close();
		}
	}
	
	public void writeSubMinerPreprocessLogs(String filePath, String suffix) throws IOException {
		XesXmlSerializer writer = new XesXmlGZIPSerializer();
		for (StageSubModelMiner subMiner : subMiners) {
			XLog log = subMiner.getPreprocessLog();
			FileOutputStream fos = new FileOutputStream(filePath + File.separator + LogUtils.getConceptName(log) + suffix + ".xes.gz");
			writer.serialize(log, fos);
			fos.close();
		}
	}
	
	public void writeSubModelToFiles(UIPluginContext context, String suffix) throws IOException {
		for (StageSubModelMiner subMiner : this.subMiners) {
			this.writeModelToFile(context, subMiner.getMiningResult(), LogUtils.getConceptName(subMiner.getLog()) + suffix);
		}
	}
	
}
