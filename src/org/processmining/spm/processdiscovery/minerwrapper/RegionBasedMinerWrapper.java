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

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrify.PetrifyDotG;
import org.processmining.plugins.petrify.PetrifyExportDotSG;
import org.processmining.plugins.petrify.PetrifyImportDotG;
import org.processmining.plugins.transitionsystem.converter.util.TSConversions;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;
import org.processmining.plugins.transitionsystem.miner.TSMinerPlugin;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.evaluation.EvaluationResult;
import org.processmining.spm.processdiscovery.evaluation.Evaluator;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.processdiscovery.result.PetrinetResult;
import org.processmining.spm.processdiscovery.utils.BPMNUtils;
import org.processmining.spm.processdiscovery.utils.GenetUtils;
import org.processmining.spm.processdiscovery.utils.PetriNetUtils;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

/**
 * Run Genet (command-line) to apply graph cut and petrinet synthesis
 * Then, call Petrify to read .g output files and convert
 * it to PetriNet for metrics calculation
 * @author Administrator
 *
 */
public class RegionBasedMinerWrapper extends AbstractMiner  {

	public String getCodeName() {
		return "RB";
	}
	
	public RegionBasedMinerWrapper(boolean writeModelFiles, boolean writeResultFiles) {
		super(writeModelFiles, writeResultFiles);
	}
	
	/**
	 * @param args
	 * 1st arg: log file name
	 * Output:
	 * 		- BPMN diagram file: .bpmn
	 * 		- Petrinet diagram file: .pnml
	 * 		- Result file: .txt
	 * 		- staged log file: .xes where artificial gate events are added to every trace
	 * 		- sublog files for every stage: .xes, these sublogs are mined for process models
	 */
	public static void main(String[] args) {
		RegionBasedMinerWrapper miner = new RegionBasedMinerWrapper(true,true);
//		miner.findBestModel("BPI13_complete_events_endpoints.xes_genet.xes.gz");
		
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		    		String fileName = filePath.getFileName().toString();
		    		System.out.println(fileName);
		    		
		    		System.out.println("Import log file");
		    		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		    		XLog log;
					try {
						log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + 
										File.separator + "logs" + File.separator + fileName));
						miner.mineBestModel(log, ModelMetrics.FSCORE);
					}
					catch (UncheckedTimeoutException e) {
						System.out.println("Program not terminated! Time-out after 5 minutes!");
					}
					catch (Exception e) {
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
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		long startTime = System.currentTimeMillis();
		
		String logName = LogUtils.getConceptName(log).toString();

		System.out.println("Execute miner");
		FakePluginContext context = new FakePluginContext();
		List<MiningResult> results = new ArrayList<>();
		SortedMap<String,String> argMap = new TreeMap<>();
		
		MiningResult mineResult = this.mine(context, log, argMap);
		
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
		System.out.println("Compute fitness/precision");
		Object[] conformCheck = Evaluator.measureFitnessPrecision(context, log, petrinet, initialMarking, finalMarkings);
		if (conformCheck != null) {
			fitness = (double)conformCheck[0];
			precision = (double)conformCheck[1];
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
//		Double[] complexity = Evaluator.measureComplexity(context, bpmn);
		EvaluationResult evalRes = new EvaluationResult(fitness, precision, generalization, 
				complexity[0], complexity[1], complexity[2], isSoundModel);
		long endTime = System.currentTimeMillis();
		MiningResult result = new MiningResult(log, argMap, bpmn, petrinetRes, evalRes, endTime - startTime);
		results.add(result);
		if (this.writeModelFiles) this.writeModelToFile(context, result, logName);
		
		if (this.writeResultFiles) {
			this.writeResultsToFile(results, logName, this.getCodeName(), "");
			System.out.println("DONE! " + results.size() + " results written to file.");
		}
		
		this.miningResult = result;
		
		return result;
	}

	@Override
	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> argMap) throws Exception {
		long startTime   = System.currentTimeMillis();
		
		// Add artificial start and end event
		XLog newLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		
		//------------------------------------
		// Mine transition system from log
		//------------------------------------
		List<XEventClassifier> classifiers = new ArrayList<>();
		classifiers.add(XLogInfoImpl.NAME_CLASSIFIER);
		TSMinerInput minerInput = new TSMinerInput(context, newLog, classifiers, XLogInfoImpl.NAME_CLASSIFIER);
		
		//first page: direction
		minerInput.getModirSettings(TSDirections.BACKWARD, XLogInfoImpl.NAME_CLASSIFIER).setUse(true);
		minerInput.getModirSettings(TSDirections.FORWARD, XLogInfoImpl.NAME_CLASSIFIER).setUse(true);
		minerInput.setUseAttributes(false);
		
		//second page: abstraction
		minerInput.getModirSettings(TSDirections.FORWARD, XLogInfoImpl.NAME_CLASSIFIER).setAbstraction(TSAbstractions.SET);
		minerInput.getModirSettings(TSDirections.FORWARD, XLogInfoImpl.NAME_CLASSIFIER).setFilteredHorizon(1);
		minerInput.getModirSettings(TSDirections.FORWARD, XLogInfoImpl.NAME_CLASSIFIER).setHorizon(-1);
		minerInput.getModirSettings(TSDirections.BACKWARD, XLogInfoImpl.NAME_CLASSIFIER).setAbstraction(TSAbstractions.SET);
		minerInput.getModirSettings(TSDirections.BACKWARD, XLogInfoImpl.NAME_CLASSIFIER).setFilteredHorizon(1);
		minerInput.getModirSettings(TSDirections.BACKWARD, XLogInfoImpl.NAME_CLASSIFIER).setHorizon(-1);
		
		//third and fourth page: classifier filter and transition filter
		XLogInfo info = minerInput.getLogInfo();
		for (XEventClass eventClass : info.getEventClasses(XLogInfoImpl.NAME_CLASSIFIER).getClasses()) {
			minerInput.getModirSettings(TSDirections.FORWARD, XLogInfoImpl.NAME_CLASSIFIER).getFilter().add(eventClass.toString());
			minerInput.getModirSettings(TSDirections.BACKWARD, XLogInfoImpl.NAME_CLASSIFIER).getFilter().add(eventClass.toString());
			minerInput.getVisibleFilter().add(eventClass.toString());
		}
		
		//fifth page: conversion step
		minerInput.getConverterSettings().setUse(TSConversions.KILLSELFLOOPS, false);
		minerInput.getConverterSettings().setUse(TSConversions.EXTEND, false);
		minerInput.getConverterSettings().setUse(TSConversions.MERGEBYINPUT, false);
		minerInput.getConverterSettings().setUse(TSConversions.MERGEBYOUTPUT, false);
		
		Object[] output = TSMinerPlugin.main(context, newLog, minerInput); //a TransitionSystemConnection is also created
		TransitionSystem ts = (TransitionSystem)output[0];
		
		//------------------------------------
		// Export the transition system to a Petrify .sg file
		//------------------------------------
		String TSFILE = GenetUtils.GENETDIR + File.separator + LogUtils.getConceptName(log) + ".sg";
		File tsFile = new File(TSFILE);
		PetrifyExportDotSG tsExport = new PetrifyExportDotSG();
		tsExport.write(context, ts, tsFile);
		
		//------------------------------------
		// Mine Petrinet from the transition system
		// The petrinet is written to a .g file 
		//------------------------------------
		System.out.println("Start calling genet.exe to mine petri net with decomposition");
		String NETFILE = GenetUtils.GENETDIR + File.separator + LogUtils.getConceptName(log) + ".g"; 
		GenetUtils.genet(TSFILE, NETFILE);
		
		//------------------------------------
		// Import .g file into a Petrinet
		//------------------------------------
		PetrifyDotG dotG = new PetrifyDotG(NETFILE);
		PetrifyImportDotG importDotG = new PetrifyImportDotG();
		Object[] result = importDotG.importFile(context, dotG);
		
		//------------------------------------
		// Prepare result
		//------------------------------------
		Set<Marking> finalMarkings = new HashSet<>();
		finalMarkings.add(PetriNetUtils.findFinalMarking((Petrinet)result[0]));
		PetriNetUtils.invisibleStartEndActivities((Petrinet)result[0]);
//		PetriNetUtils.removeStartEndActivities((Petrinet)result[0]);
		
		PetrinetResult petrinetRes = new PetrinetResult((Petrinet)result[0], (Marking)result[1], 													finalMarkings);
		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinetRes.getPetrinet(),
											petrinetRes.getInitialMarking(), 
											petrinetRes.getFinalMarkings().iterator().next());
		
		long miningTime   = System.currentTimeMillis() - startTime;
//		Object[] mineResult = new Object[2];
//		mineResult[0] = petrinetRes;
//		mineResult[1] = bpmn;
		this.miningResult = new MiningResult(log, argMap, bpmn, petrinetRes, null, miningTime);
		
		return this.miningResult;
		
	}

}
