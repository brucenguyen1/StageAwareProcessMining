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
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.result.MiningResult;
import org.processmining.sapm.processdiscovery.result.PetrinetResult;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;
import org.processmining.sapm.processdiscovery.utils.GenetUtils;
import org.processmining.sapm.processdiscovery.utils.PetriNetUtils;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

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
	
	public RegionBasedMinerWrapper(boolean writeModelFiles, boolean writeResultFiles, boolean soundModel, boolean isSubMiner) {
		super(writeModelFiles, writeResultFiles, soundModel, isSubMiner);
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
		RegionBasedMinerWrapper miner = new RegionBasedMinerWrapper(
													Boolean.valueOf(args[0]), 
													Boolean.valueOf(args[1]), 
													Boolean.valueOf(args[2]),
													Boolean.valueOf(args[3]));
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
		    System.out.println("DONE!");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
	
	@Override
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		long startTime = System.currentTimeMillis();
		
		String logName = LogUtils.getConceptName(log).toString();
		XLog copyLog = LogUtils.copyLog(log);

		System.out.println("Execute miner");
		FakePluginContext context = new FakePluginContext();
		List<MiningResult> results = new ArrayList<>();
		SortedMap<String,String> argMap = new TreeMap<>();
		MiningResult mineResult = this.mine(context, copyLog, argMap);
		
		// Evaluate
		this.evaluate(context, mineResult, log);
		results.add(mineResult);
		
		if (this.writeModelFiles) this.writeModelToFile(context, mineResult, logName, this.getCodeName(), "");
		if (mineResult != null) {
			if (!this.selectSoundModel()) this.evaluateSoundness(context, mineResult);
			mineResult.setMiningTime(System.currentTimeMillis() - startTime);
		}
		
		if (this.writeResultFiles) this.writeResultsToFile(results, logName, this.getCodeName(), "");		
		this.miningResult = mineResult;
		
		return mineResult;
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
		
		PetrinetResult petrinetRes = new PetrinetResult((Petrinet)result[0], (Marking)result[1], finalMarkings);
		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Dirk(context, petrinetRes.getPetrinet(),
				petrinetRes.getInitialMarking(), 
				petrinetRes.getFinalMarkings().iterator().next());
		
//		BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinetRes.getPetrinet(),
//											petrinetRes.getInitialMarking(), 
//											petrinetRes.getFinalMarkings().iterator().next());
		
		long miningTime   = System.currentTimeMillis() - startTime;
//		Object[] mineResult = new Object[2];
//		mineResult[0] = petrinetRes;
//		mineResult[1] = bpmn;
		this.miningResult = new MiningResult(log, argMap, bpmn, petrinetRes, miningTime);
		
		return this.miningResult;
		
	}

}
