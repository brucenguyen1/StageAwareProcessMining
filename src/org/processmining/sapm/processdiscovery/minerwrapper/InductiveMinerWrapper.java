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

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IM;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.processdiscovery.ModelMetrics;
import org.processmining.sapm.processdiscovery.result.MiningResult;
import org.processmining.sapm.processdiscovery.result.PetrinetResult;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

public class InductiveMinerWrapper extends AbstractMiner {
	private boolean repeated = false;
	
	public InductiveMinerWrapper(boolean writeModelFiles, boolean writeResultFiles, boolean soundModel, boolean repeated, boolean isSubMiner) {
		super(writeModelFiles, writeResultFiles,soundModel,isSubMiner);
		this.repeated = repeated;
	}
	
	public InductiveMinerWrapper(boolean writeModelFiles, boolean writeResultFiles, boolean soundModel, boolean isSubMiner) {
		super(writeModelFiles, writeResultFiles,soundModel,isSubMiner);
		this.repeated = false;
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
		    		InductiveMinerWrapper miner = new InductiveMinerWrapper(
		    												Boolean.valueOf(args[0]), 
		    												Boolean.valueOf(args[1]), 
		    												Boolean.valueOf(args[2]),
		    												Boolean.valueOf(args[3]),
		    												Boolean.valueOf(args[4]));
		    		System.out.println("========================================");
		    		System.out.println(fileName);
		    		
					try {
						System.out.println("Import log file");
						OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
						XLog log = null;
						log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "logs" + 
																File.separator + fileName));
						if (miner.getRepeated()) {
							miner.mineBestModelRepeated(log, ModelMetrics.FSCORE, 5);
						}
						else {
							miner.mineBestModel(log, ModelMetrics.FSCORE);
						}
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
		
//		XLog preprocessLog = LogUtils.addStartEndEvents(log,"startevent","endevent");
		XLog copyLog = LogUtils.copyLog(log);
		
		for (float noiseThreshold=0.1f;noiseThreshold<=0.8f;noiseThreshold+=0.1f) {
			context.clear();
			System.out.println("------------------------------------");
			System.out.println("MINE WITH NOISE THRESHOLD = " + noiseThreshold);
			
			SortedMap<String,String> paramValueMap = new TreeMap<>();
			paramValueMap.put("1-NoiseThreshold", noiseThreshold + "");
			
			//////MINE A MODEL from the preprocessed log
			MiningResult mineResult = this.mine(context, copyLog, paramValueMap);
			if (this.writeModelFiles) this.writeModelToFile(context, mineResult, logName, this.getCodeName(), "");
			this.evaluate(context, mineResult, log);
			results.add(mineResult);
			bestMiningResult = this.compareMiningResult(bestMiningResult, mineResult, metrics, this.selectSoundModel());
		}
		
		if (bestMiningResult != null) {
			// must evaluate soundness for the final model
			if (!this.selectSoundModel() && !this.isSubMiner()) this.evaluateSoundness(context, bestMiningResult);
			bestMiningResult.setMiningTime(System.currentTimeMillis() - startTime);
		}
		
		if (this.writeResultFiles) this.writeResultsToFile(results, LogUtils.getConceptName(log).toString(), this.getCodeName(), "");
		this.miningResult = bestMiningResult;
		
		return bestMiningResult;
	}
	
	public MiningResult mineBestModelRepeated(XLog log, ModelMetrics metrics, int times) throws Exception {
		List<MiningResult> results = new ArrayList<>();
		boolean oriWriteModel = this.writeModelFiles;
		boolean oriWriteResultFiles = this.writeResultFiles;
		
		// Turn off trace files settings in the loop 
		this.writeModelFiles = false;
		//this.writeResultFiles = false;

		MiningResult bestMiningResult = null;		
		for (int i=1; i<=times; i++) {
			System.out.println("========== Mining The Best Model - Iteration #" + i + " ============");
			MiningResult res = this.mineBestModel(log, metrics);
			bestMiningResult = this.compareMiningResult(bestMiningResult, res, metrics, this.selectSoundModel());
			results.add(res);
		}
		
		FakePluginContext context = new FakePluginContext();
		String logName = LogUtils.getConceptName(log).toString();
		if (oriWriteModel) this.writeModelToFile(context, bestMiningResult, logName, this.getCodeName()+"-REPEAT", "");
		if (oriWriteResultFiles) this.writeResultsToFile(results, logName, this.getCodeName()+"-REPEAT", "");
		
		// Restore settings
		this.writeModelFiles = oriWriteModel;
		this.writeResultFiles = oriWriteResultFiles;
		
		return bestMiningResult;
	}
	
	public String getCodeName() {
		return "IDM";
	}
	
	public boolean getRepeated() {
		return this.repeated;
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
//        PetriNetUtils.invisibleStartEndActivities(petrinet); //remove startevent and endevent transitions
        Marking initialMarking = (Marking) petriNetResult[1];
        Set<Marking> finalMarkings = new HashSet<>();
        finalMarkings.add((Marking) petriNetResult[2]);
        PetrinetResult petrinetRes = new PetrinetResult(petrinet,initialMarking,finalMarkings);
		
		// Convert Petrinet to BPMN
        System.out.println("Convert Petrinet to BPMN diagram");
        BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Dirk(context, petrinet, initialMarking, finalMarkings.iterator().next());
        //BPMNDiagram bpmn = BPMNUtils.PetriNetToBPMN_Raf(context, petrinet, initialMarking, finalMarkings.iterator().next());
        
        long miningTime   = System.currentTimeMillis() - startTime;
		
		this.miningResult = new MiningResult(log, paramValueMap, bpmn, petrinetRes, miningTime);
		
		return this.miningResult;
	}

}
