package org.processmining.sapm.processdiscovery.minerwrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedminer.plugins.DecomposedDiscoveryPlugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.processdiscovery.DivideAndConquerMiner;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.bridge.SPMDecomposedDiscoveryParameters;
import org.processmining.sapm.processdiscovery.result.MiningResult;
import org.processmining.sapm.processdiscovery.result.PetrinetResult;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;
import org.processmining.sapm.processdiscovery.utils.PetriNetUtils;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class DecomposedMinerWrapper extends DivideAndConquerMiner {
	private Miner baseMiner = null;
	
	public DecomposedMinerWrapper(boolean writeModelFiles, boolean writeResultFiles, boolean soundModel, Miner baseMiner) {
		super(writeModelFiles, writeResultFiles, soundModel);
		this.baseMiner = baseMiner;
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
		Miner baseMiner = null;
		if (args[3].equals("0")) {
			baseMiner = new InductiveMinerWrapper(false, false, Boolean.valueOf(args[2]), true);
		}
		else if (args[3].equals("1")) {
			baseMiner = new FodinaWrapper(false, false, Boolean.valueOf(args[2]), true);
		}
		else {
			System.out.println("Unknown base miner option: " + args[3]);
			return;
		}
		DecomposedMinerWrapper miner = new DecomposedMinerWrapper(
												Boolean.valueOf(args[0]), 
												Boolean.valueOf(args[1]), 
												Boolean.valueOf(args[2]), 
												baseMiner);
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
						miner.mineBestModel(log, ModelMetrics.FSCORE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    });
		    
		    System.out.println("DONE!");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		long startTime   = System.currentTimeMillis();
		String logName = LogUtils.getConceptName(log).toString();
		
		//XLog newLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		XLog copyLog = LogUtils.copyLog(log);
		
		// Call command
		System.out.println("Execute miner");
		SortedMap<String,String> argMap = new TreeMap<>();
		List<MiningResult> results = new ArrayList<>();
		FakePluginContext context = new FakePluginContext();
		MiningResult bestMiningResult = null;

		DecomposedDiscoveryPlugin minerPlugin = new DecomposedDiscoveryPlugin();
		SPMDecomposedDiscoveryParameters params = new SPMDecomposedDiscoveryParameters(copyLog);
		params.setTryConnections(false);
		params.setClassifier(XLogInfoImpl.NAME_CLASSIFIER);
		params.setMinerWrapper(this.baseMiner);
		
		AcceptingPetriNet net = minerPlugin.run(context, copyLog, params);
		//PetriNetUtils.invisibleStartEndActivities(net.getNet());
		
		
		// for debug only
//		PetrinetResult petrinetRes1 = new PetrinetResult(net.getNet(), net.getInitialMarking(),net.getFinalMarkings());
//		BPMNDiagram bpmn1 = BPMNUtils.PetriNetToBPMN_Dirk(context, petrinetRes1.getPetrinet(), petrinetRes1.getInitialMarking(), petrinetRes1.getFinalMarkings().iterator().next());	
//		MiningResult result1 = new MiningResult(log, argMap, bpmn1, petrinetRes1, 0);
//		if (this.writeModelFiles) this.writeModelToFile(context, result1, logName, this.getCodeName(), "beforeToWFNet");
		
		System.out.println("Convert merged Petri Net to Workflow Net");
		List<Marking> newMarkings = PetriNetUtils.convertToWorkflowNet(net.getNet(), net.getInitialMarking(), net.getFinalMarkings());
		PetrinetResult petrinetRes = new PetrinetResult(net.getNet(), newMarkings.get(0), 
														new HashSet<>(Arrays.asList(newMarkings.get(1))));
		System.out.println("Convert merged Petri Net to BPMN diagram");
		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Dirk(context, petrinetRes.getPetrinet(), 
				petrinetRes.getInitialMarking(), petrinetRes.getFinalMarkings().iterator().next());
//		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinetRes.getPetrinet(), 
//					petrinetRes.getInitialMarking(), 
//					petrinetRes.getFinalMarkings().iterator().next());		
		MiningResult result = new MiningResult(log, argMap, bpmn, petrinetRes, 0);
		
		// Evaluate
		this.evaluate(context, result, log);
		if (!this.selectSoundModel()) this.evaluateSoundness(context, result); //force to evaluate soundness as only one result is returned
		result.setMiningTime(System.currentTimeMillis() - startTime);
		bestMiningResult = result;
		
		results.add(result);
		if (this.writeModelFiles) this.writeModelToFile(context, bestMiningResult, logName, this.getCodeName(), "");
		if (this.writeResultFiles) this.writeResultsToFile(results, logName, this.getCodeName(), "");
		this.miningResult = bestMiningResult;
		
		return bestMiningResult;
	}

	public String getCodeName() {
		return "DCM_" + this.baseMiner.getCodeName();
	}
}
