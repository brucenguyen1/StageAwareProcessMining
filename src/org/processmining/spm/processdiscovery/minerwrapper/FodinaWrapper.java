package org.processmining.spm.processdiscovery.minerwrapper;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.filter.FilterParams;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.evaluation.EvaluationResult;
import org.processmining.spm.processdiscovery.evaluation.Evaluator;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.processdiscovery.result.PetrinetResult;
import org.processmining.spm.processdiscovery.utils.BPMNUtils;
import org.processmining.spm.processdiscovery.utils.PetriNetUtils;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

public class FodinaWrapper extends AbstractMiner {
	
	public FodinaWrapper(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
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
		    		FodinaWrapper miner = new FodinaWrapper(true, true);
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
				if (this.writeModelFiles) this.writeModelToFile(context, mineResult, logName);
				
				PetrinetResult petrinetRes = mineResult.getPetrinetRes();
				Petrinet petrinet = petrinetRes.getPetrinet();
				Marking initialMarking = petrinetRes.getInitialMarking();
				Set<Marking> finalMarkings = petrinetRes.getFinalMarkings();
				BPMNDiagram bpmn = mineResult.getBPMN();
				
				// Evaluate
				System.out.println("Check soundness");
				boolean isSoundModel = Evaluator.checkSoundness_Woflan(context, petrinet);
				System.out.println("Model is sound: " + (isSoundModel ? "YES" : "NO"));
				
				double fitness=0.0, precision=0.0, fscore=0.0, generalization=0.0;
				Automaton automaton = null;
				PNRepResult alignments = null;
				CostBasedCompleteParam alignParams = null;
				if (isSoundModel) {
					System.out.println("Compute fitness/precision");
					
					///// EVALUATE THE MODEL ON THE ORIGINAL LOG
					Object[] conformCheck = Evaluator.measureFitnessPrecision(context, log, petrinet, initialMarking, finalMarkings);
					if (conformCheck != null) {
						fitness = (double)conformCheck[0];
						precision = (double)conformCheck[1];
						automaton = (Automaton)conformCheck[2];
						alignments = (PNRepResult)conformCheck[3];
						alignParams = (CostBasedCompleteParam)conformCheck[4];
						fscore = 2.0*fitness*precision/(fitness+precision);
						System.out.println("Fitness = " + fitness + ", Precision = " + precision + ", F-score = " + fscore);
					}
					else {
						System.out.println("No result of fitness/precision/F-score. Only computing complexity.");
					}
				}
				else {
					System.out.println("The model is NOT sound. F-score is not calculated.");
				}
				
				Double[] complexity = new Double[3];
				complexity[0] = 0.0; complexity[1] = 0.0; complexity[2] = 0.0;
				ExecutorService executor = Executors.newSingleThreadExecutor();
				try {
				    SimpleTimeLimiter timeout = new SimpleTimeLimiter(executor);
				    complexity = timeout.callWithTimeout(new Callable<Double[]>() {
				      @Override
				      public Double[] call() throws Exception {
				    	  return Evaluator.measureComplexity(context, bpmn);
				      }
				    }, 200, TimeUnit.SECONDS, true);
				} catch (UncheckedTimeoutException e) {
					  System.err.println("Computing model complexity is too long and has been stopped!");
				} finally {
					  executor.shutdown();
				}
				
//				Double[] complexity = Evaluator.measureComplexity(context, bpmn);
				EvaluationResult evalRes = new EvaluationResult(alignParams, fitness, precision, generalization, 
						complexity[0], complexity[1], complexity[2], isSoundModel, automaton, alignments);
				
				MiningResult result = new MiningResult(log, paramValueMap, bpmn, petrinetRes, evalRes, 0);
				results.add(result);
				bestMiningResult = this.compareMiningResult(bestMiningResult, result, metrics, true);
			}
		}
		
		long endTime   = System.currentTimeMillis();
		if (bestMiningResult != null) bestMiningResult.setMiningTime(endTime - startTime);

		if (this.writeResultFiles) this.writeResultsToFile(results, logName, this.getCodeName(), "");
		System.out.println("DONE!");
		
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
		
		this.miningResult = new MiningResult(log, paramValueMap, bpmn, petrinetRes, null, miningTime);
		
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