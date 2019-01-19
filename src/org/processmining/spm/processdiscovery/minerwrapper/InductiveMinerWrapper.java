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

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IM;
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

public class InductiveMinerWrapper extends AbstractMiner {
	
	public InductiveMinerWrapper(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
	}
	
	public InductiveMinerWrapper(boolean writeModelFiles, boolean writeResultFiles, FilterParams filterParams) {
		super(writeModelFiles, writeResultFiles, filterParams);
	}
	
	/**
	 * @param args
	 * Input:
	 * 		- All files in the "logs" subfolder
	 * Output:
	 * 		- BPMN diagram file: .bpmn
	 * 		- Petrinet diagram file: .pnml
	 * 		- Result file: .txt
	 */
	public static void main(String[] args) {
		
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            System.out.println(filePath.toString());
		    		String fileName = filePath.getFileName().toString();
		    		InductiveMinerWrapper miner = new InductiveMinerWrapper(true, true);
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
		
		XLog preprocessLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		
		for (float noiseThreshold=0.0f;noiseThreshold<=0.8f;noiseThreshold+=0.1f) {
			context.clear();
			System.out.println("------------------------------------");
			System.out.println("MINE WITH NOISE THRESHOLD = " + noiseThreshold);
			
			SortedMap<String,String> paramValueMap = new TreeMap<>();
			paramValueMap.put("1-NoiseThreshold", noiseThreshold + "");
			
			//////MINE A MODEL from the preprocessed log
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
			
			double fitness=0.0, precision=0.0, fscore=0.0, generalization = 0.0;
			Automaton automaton = null;
			PNRepResult alignments = null;
			CostBasedCompleteParam alignParams = null;
			if (isSoundModel) {
				System.out.println("The model is sound. Now evaluate fitness, precision and complexity");
				
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
					System.out.println("No result of fitness/precision/F-score. Only compute complexity.");
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
			
//			Double[] complexity = Evaluator.measureComplexity(context, bpmn);
			EvaluationResult evalRes = new EvaluationResult(alignParams, fitness, precision, generalization, 
					complexity[0], complexity[1], complexity[2], isSoundModel, automaton, alignments);
			MiningResult result = new MiningResult(log, paramValueMap, bpmn, petrinetRes, evalRes, 0);
			results.add(result);
			bestMiningResult = this.compareMiningResult(bestMiningResult, result, metrics, true);
		}
		
		long endTime   = System.currentTimeMillis();
		if (bestMiningResult != null) bestMiningResult.setMiningTime(endTime - startTime);
		
		if (this.writeResultFiles) this.writeResultsToFile(results, LogUtils.getConceptName(log).toString(), this.getCodeName(), "");
		System.out.println("DONE!");
		this.miningResult = bestMiningResult;
		
		return bestMiningResult;
	}
	
	public String getCodeName() {
		return "IDM";
	}
	
	/**
	 * 1st param: noise threshold
	 * Return: 1st:PetrinetRes, 2nd:bpmn diagram, 3rd: mining time 
	 * @throws Exception 
	 */
	@Override
	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> paramValueMap) throws Exception {
		long startTime   = System.currentTimeMillis();
		float noiseThreshold = Float.valueOf(paramValueMap.get("1-NoiseThreshold"));
        
		MiningParameters miningParams = InductiveMinerParamSetting.getMiningParameters();
        miningParams.setClassifier(InductiveMinerParamSetting.getXEventClassifier());
        miningParams.setNoiseThreshold(noiseThreshold);
        
        System.out.println("Execute Inductive Miner");
//        IM inductiveMiner = new IM();
        Object[] petriNetResult = IM.minePetriNet(context, log, miningParams);
        Petrinet petrinet = (Petrinet) petriNetResult[0];
//        PetriNetUtils.removeStartEndActivities(petrinet); //remove startevent and endevent transitions
        PetriNetUtils.invisibleStartEndActivities(petrinet); //remove startevent and endevent transitions
        Marking initialMarking = (Marking) petriNetResult[1];
        Set<Marking> finalMarkings = new HashSet<>();
        finalMarkings.add((Marking) petriNetResult[2]);
        PetrinetResult petrinetRes = new PetrinetResult(petrinet,initialMarking,finalMarkings);
		
		// Convert Petrinet to BPMN
        System.out.println("Convert Petrinet to BPMN diagram");
//		BPMNDiagram bpmn = PetriNetToBPMNConverter.convert(petrinet, initialMarking, finalMarking, true);
        BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinet, initialMarking, finalMarkings.iterator().next());
        
        long miningTime   = System.currentTimeMillis() - startTime;
		
		this.miningResult = new MiningResult(log, paramValueMap, bpmn, petrinetRes, null, miningTime);
		
		return this.miningResult;
	}

}
