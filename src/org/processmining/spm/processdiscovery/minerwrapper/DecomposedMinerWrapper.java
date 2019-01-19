package org.processmining.spm.processdiscovery.minerwrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedminer.plugins.DecomposedDiscoveryPlugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.spm.bridge.SPMDecomposedDiscoveryParameters;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.processdiscovery.DivideAndConquerMiner;
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

public class DecomposedMinerWrapper extends DivideAndConquerMiner {
	public DecomposedMinerWrapper(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
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
		DecomposedMinerWrapper miner = new DecomposedMinerWrapper(true,true);
//		miner.findBestModel("BPI2015 Municipality4_NoiseFiltered.xes");
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		        	System.out.println("=======================================");
		            System.out.println(filePath.toString());
		    		String fileName = filePath.getFileName().toString();
		    		
					System.out.println("Import log file");
					OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
					XLog log;
					try {
						log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "logs" + 
																	File.separator + fileName));
//						miner.mineBestModel(log, new InductiveMinerWrapper(false,false), ModelMetrics.FSCORE);
						miner.mineBestModel(log, new FodinaWrapper(false, false), ModelMetrics.FSCORE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    });
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public MiningResult mineBestModel(XLog log, Miner wrapper, ModelMetrics metrics) throws Exception {
		long startTime   = System.currentTimeMillis();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		String logName = LogUtils.getConceptName(log).toString();
		
		XLog newLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		
		// Call command
		System.out.println("Execute miner");
		SortedMap<String,String> argMap = new TreeMap<>();
		List<MiningResult> results = new ArrayList<>();
		FakePluginContext context = new FakePluginContext();
		MiningResult bestMiningResult = null;

		DecomposedDiscoveryPlugin minerPlugin = new DecomposedDiscoveryPlugin();
		SPMDecomposedDiscoveryParameters params = new SPMDecomposedDiscoveryParameters(newLog);
		params.setTryConnections(false);
		params.setClassifier(XLogInfoImpl.NAME_CLASSIFIER);
		params.setMinerWrapper(wrapper);
		
		AcceptingPetriNet net = minerPlugin.run(context, newLog, params);
		PetriNetUtils.invisibleStartEndActivities(net.getNet());
		PetrinetResult petrinetRes = new PetrinetResult(net.getNet(), net.getInitialMarking(), net.getFinalMarkings());
		System.out.println("Convert merged Petri Net to BPMN diagram");
		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Dirk(context, petrinetRes.getPetrinet(), 
				petrinetRes.getInitialMarking(), petrinetRes.getFinalMarkings().iterator().next());
//		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinetRes.getPetrinet(), 
//					petrinetRes.getInitialMarking(), 
//					petrinetRes.getFinalMarkings().iterator().next());		
		// Evaluate
		double fitness=0.0, precision=0.0, fscore=0.0, generalization = 0.0;
		System.out.println("Check soundness");
		boolean isSoundModel = Evaluator.checkSoundness_Woflan(context, net.getNet());
		System.out.println("Model is sound: " + (isSoundModel ? "YES" : "NO"));
		
		System.out.println("Compute fitness/precision");
		
		Object[] conformCheck = Evaluator.measureFitnessPrecision(context, log, net.getNet(), net.getInitialMarking(), 
																	net.getFinalMarkings());
		if (conformCheck != null) {
			fitness = (Double)conformCheck[0];
			precision = (Double)conformCheck[1];
			fscore = 2.0*fitness*precision/(fitness+precision);
			System.out.println("Fitness = " + fitness + ", Precision = " + precision + ", F-score = " + fscore);
		}
		else {
			System.out.println("Time-out. No result of fitness/precision/F-score.");
		}
		
		System.out.println("Check complexity");
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
		//Double[] complexity = Evaluator.measureComplexity(context, bpmn);
		EvaluationResult evalRes = new EvaluationResult(fitness, precision, generalization, 
											complexity[0],complexity[1],complexity[2], isSoundModel);
		MiningResult result = new MiningResult(log, argMap, bpmn, petrinetRes, evalRes, 0);
		results.add(result);
		bestMiningResult = result;
		
		long endTime   = System.currentTimeMillis();
		if (bestMiningResult != null) bestMiningResult.setMiningTime(endTime - startTime);
		if (this.writeModelFiles) this.writeModelToFile(context, bestMiningResult, logName);
		if (this.writeResultFiles) {
			this.writeResultsToFile(results, logName, this.getCodeName(), "");
			System.out.println("DONE! " + results.size() + " results written to file.");
		}
		
		return bestMiningResult;
	}

	public String getCodeName() {
		return "DCM2";
	}
}
