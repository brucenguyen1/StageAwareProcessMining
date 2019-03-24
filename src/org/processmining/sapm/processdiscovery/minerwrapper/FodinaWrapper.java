package org.processmining.sapm.processdiscovery.minerwrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmnminer.converter.CausalNetToPetrinet;
import org.processmining.plugins.bpmnminer.miner.BPMNMiner;
import org.processmining.plugins.bpmnminer.types.MinerSettings;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.filter.FilterParams;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.result.MiningResult;
import org.processmining.sapm.processdiscovery.result.PetrinetResult;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;
import org.processmining.sapm.processdiscovery.utils.PetriNetUtils;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class FodinaWrapper extends AbstractMiner {
	
	public FodinaWrapper(boolean writeModelFiles, boolean writeResultFiles, boolean soundModel, boolean isSubMiner) {
		super(writeModelFiles, writeResultFiles, soundModel, isSubMiner);
	}
	
	public FodinaWrapper(boolean writeModelFiles, boolean writeResultFiles, FilterParams filterParams) {
		super(writeModelFiles, writeResultFiles, filterParams);
	}
	
	public String getCodeName() {
		return "FO";
	}
	
	/**
	 * @param args
	 * Input:
	 * 		- All files in the "logs" subfolder
	 * Output:
	 * 		- BPMN diagram file: .bpmn
	 * 		- Petrinet diagram file: .pnml
	 * 		- staged log file: .xes where artificial gate events are added to every trace
	 * 		- sublog files for every stage: .xes, these sublogs are mined for process models
	 */
	public static void main(String[] args) {
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            System.out.println(filePath.toString());
		    		String fileName = filePath.getFileName().toString();
		    		FodinaWrapper miner = new FodinaWrapper(
								    				Boolean.valueOf(args[0]), 
													Boolean.valueOf(args[1]), 
													Boolean.valueOf(args[2]),
													Boolean.valueOf(args[3]));
		    		System.out.println("========================================");
		    		System.out.println(fileName);
		    		
					try {
						System.out.println("Import log file");
						OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
						XLog log = null;
						log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "logs" + 
																File.separator + fileName));
						miner.mineBestModel(log, ModelMetrics.FSCORE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    });
		    System.out.println("DONE!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		long startTime   = System.currentTimeMillis();
		MiningResult bestMiningResult = null;
		String logName = LogUtils.getConceptName(log).toString();
		
		System.out.println("Execute miner");
		FakePluginContext context = new FakePluginContext();
		List<MiningResult> results = new ArrayList<>();
		int cUse = 0;
		int preferAndToL2l = 0;
		int preventL2lWithL1l = 0;
		
		//Add start/end events to help identify the final marking
		XLog preprocessLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		
		for (double dependencyThreshold=0.2;dependencyThreshold<=0.8;dependencyThreshold+=0.2) {
			for (double patternThreshold=0.2;patternThreshold<=0.8;patternThreshold+=0.2) {
				context.clear();
				System.out.println("------------------------------------");
				System.out.println("dependencyThreshold = " + dependencyThreshold + ", patternThreshold = " + patternThreshold);

				SortedMap<String,String> paramValueMap = new TreeMap<>();
				paramValueMap.put("1-dependencyThreshold", dependencyThreshold + "");
				paramValueMap.put("2-patternThreshold", patternThreshold + "");
				paramValueMap.put("3-cUSE", String.valueOf(cUse));
				paramValueMap.put("4-preferAndToL2l", String.valueOf(preferAndToL2l));
				paramValueMap.put("5-preventL2lWithL1l", String.valueOf(preventL2lWithL1l));
				
				/////// MINE A MODEL from the preprocessed log
				MiningResult mineResult = this.mine(context, preprocessLog, paramValueMap);
				if (this.writeModelFiles) this.writeModelToFile(context, mineResult, logName, this.getCodeName(), "");
				this.evaluate(context, mineResult, log);
				results.add(mineResult);
				bestMiningResult = this.compareMiningResult(bestMiningResult, mineResult, metrics, this.selectSoundModel());
			}
		}
		
		if (bestMiningResult != null) {
			// must evaluate soundness for the final model
			if (!this.selectSoundModel() && !this.isSubMiner()) this.evaluateSoundness(context, bestMiningResult);
			bestMiningResult.setMiningTime(System.currentTimeMillis() - startTime);
		}
		
		if (this.writeResultFiles) this.writeResultsToFile(results, logName, this.getCodeName(), "");
		this.miningResult = bestMiningResult;
		
		return bestMiningResult;
	}
	
	/**
	 * 1st param: noise threshold
	 * Return: 1st:PetrinetRes, 2nd:bpmn diagram, 3rd: mining time 
	 * @throws Exception 
	 */
	@Override
	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> paramValueMap) throws Exception {
		long startTime   = System.currentTimeMillis();
		
		double dependencyThreshold = Double.valueOf(paramValueMap.get("1-dependencyThreshold"));
		double patternThreshold = Double.valueOf(paramValueMap.get("2-patternThreshold"));
		boolean cUSE = Boolean.getBoolean("3-cUSE");
		boolean preferAndToL2l = Boolean.getBoolean("4-preferAndToL2l");
		boolean preventL2lWithL1l = Boolean.getBoolean("5-preventL2lWithL1l");
		
		MinerSettings settings = this.getSettings(dependencyThreshold, patternThreshold, cUSE, 
														preferAndToL2l, preventL2lWithL1l);
        
        System.out.println("Execute Miner");
        
        BPMNMiner miner = new BPMNMiner(log, settings);
        miner.setDeriveOriginatorSwimLanes(false);
        miner.mine();
        BPMNDiagram bpmn = miner.getBPMNDiagram();
        BPMNUtils.removeStartEndActivities(bpmn); 
        
        Object[] petriNetResult = CausalNetToPetrinet.convert(context, miner.getCausalNet());
        Petrinet petrinet = (Petrinet) petriNetResult[0];
        Marking initialMarking = (Marking) petriNetResult[1];
        Set<Marking> finalMarkings = new HashSet<>();
		finalMarkings.add(PetriNetUtils.findFinalMarking(petrinet));
		PetriNetUtils.invisibleStartEndActivities(petrinet);
        PetrinetResult petrinetRes = new PetrinetResult(petrinet,initialMarking,finalMarkings);
		
        long miningTime   = System.currentTimeMillis() - startTime;
//		Object[] result = new Object[2];
//		result[0] = petrinetRes;
//		result[1] = bpmn;
		
		this.miningResult = new MiningResult(log, paramValueMap, bpmn, petrinetRes, miningTime);
		
		return this.miningResult;
	}		
	
	private MinerSettings getSettings(double dependencyThreshold, double patternThreshold, 
										boolean cUSE, boolean preferAndToL2l, boolean preventL2lWithL1l) { 
		MinerSettings settings = new MinerSettings();
		settings.dependencyThreshold = (dependencyThreshold);
		settings.l1lThreshold = (dependencyThreshold);
		settings.l2lThreshold = (dependencyThreshold);
		settings.longDistanceThreshold = 0.90D;
		settings.patternThreshold = (patternThreshold);
		settings.dependencyDivisor = 1;
		settings.duplicateThreshold = 0.10D;
		
		settings.useAllConnectedHeuristics = true;
		settings.backwardContextSize = 0;
		settings.forwardContextSize = 0;
		settings.useLongDistanceDependency = false;
		settings.useUniqueStartEndTasks = cUSE;
		settings.collapseL1l =  true;
		settings.preferAndToL2l = preferAndToL2l;
		settings.preventL2lWithL1l = preventL2lWithL1l;
		
		settings.classifier = XLogInfoImpl.NAME_CLASSIFIER;

		return settings; 
	}
}