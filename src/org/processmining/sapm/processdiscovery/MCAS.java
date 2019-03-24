package org.processmining.sapm.processdiscovery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.multietc.automaton.AutomatonEdge;
import org.processmining.plugins.multietc.automaton.AutomatonNode;
import org.processmining.sapm.fakecontext.FakePluginContext;
import org.processmining.sapm.processdiscovery.minerwrapper.FodinaWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.InductiveMinerWrapper;
import org.processmining.sapm.processdiscovery.minerwrapper.Miner;
import org.processmining.sapm.processdiscovery.problem.Problem;
import org.processmining.sapm.processdiscovery.problem.ProblemClass;
import org.processmining.sapm.processdiscovery.problem.ProblemRelation;
import org.processmining.sapm.processdiscovery.problem.ProblemType;
import org.processmining.sapm.processdiscovery.result.MiningResult;
import org.processmining.sapm.processdiscovery.result.PetrinetResult;
import org.processmining.sapm.processdiscovery.utils.BPMNUtils;
import org.processmining.sapm.processdiscovery.utils.PetriNetUtils;
import org.processmining.sapm.processdiscovery.utils.RPSTHelper;
import org.processmining.sapm.stagemining.algorithm.StageMiningHighestModularity;
import org.processmining.sapm.stagemining.model.Stage;
import org.processmining.sapm.stagemining.model.StageDecomposition;
import org.processmining.sapm.utils.LogUtils;
import org.processmining.sapm.utils.OpenLogFilePlugin;

import au.edu.qut.promplugins.ReplaceIORsPlugin;
import edu.uci.ics.jung.graph.Graph;

/**
 * MCAS = Mine - Chain - Adjust - Stitch
 * @author Bruce
 *
 */
public class MCAS extends DivideAndConquerMiner {
	// Information of jumping edges: source_name@target_name -> frequency in the log 
	private Miner baseMiner = null;
	private boolean writeLogFiles = false;
	private final String CODE = "SPS";
	private String codeName = CODE;
	private MiningResult miningResult = null;
	
	private double imprecisionThreshold = 0.06;
	private double availThreshold = 0.8;
	
	private boolean adjust = false; 
	
	final double CLASS_USE_THRESHOLD = 0.01;
	final double TRACE_FREQ_THRESHOLD = 0.01;
	final double TRACE_FITNESS_THRESHOLD = 0.9;
	final double INTRASTAGE_SYNC_MOVE_THRESHOLD = 0.5;
	
	public MCAS(double imprecisionThreshold, double availThreshold, boolean writeModelFiles, 
				boolean writeResultFiles, boolean writeLogFiles, boolean adjust, boolean soundModel, Miner baseMiner) {
		super(writeModelFiles, writeResultFiles, soundModel);
		this.writeLogFiles = writeLogFiles;
		this.imprecisionThreshold = imprecisionThreshold;
		this.availThreshold = availThreshold;
		this.adjust = adjust;
		this.baseMiner = baseMiner;
		
		System.out.println("imprecisionThreshold: " + imprecisionThreshold);
		System.out.println("availThreshold: " + availThreshold);
		System.out.println("writeModelFiles: " + writeModelFiles);
		System.out.println("writeResultFiles: " + writeResultFiles);
		System.out.println("writeLogFiles: " + writeLogFiles);
		System.out.println("adjust: " + adjust);
		System.out.println("soundModel: " + soundModel);
	}
	
	/**
	 * @param args
	 * Input:
	 * 		- All files in the "logs" subfolder
	 * 		- args[0]: imprecisionThreshold
	 * 		- args[1]: availThreshold
	 * 		- args[2]: writeModelFiles
	 * 		- args[3]: writeResultFiles
	 * 		- args[4]: writeLogFiles
	 * 		- args[5]: adjust
	 * 		- args[6]: base miner (0: InductiveMiner, 1: Fodina)
	 * Output:
	 * 		- BPMN diagram file: .bpmn
	 * 		- Petrinet diagram file: .pnml
	 * 		- staged log file: .xes where artificial gate events are added to every trace
	 * 		- sublog files for every stage: .xes, these sublogs are mined for process models
	 */
	public static void main(String[] args) {
		//MCAS miner = new MCAS(0.06,0.8,true,true, true, true, true);
		Miner baseMiner = null;
		if (args[7].equals("0")) {
			baseMiner = new InductiveMinerWrapper(false, false, false, true);
		}
		else if (args[7].equals("1")) {
			baseMiner = new FodinaWrapper(false, false, false, true);
		}
		else {
			System.out.println("Unknown base miner option: " + args[7]);
			return;
		}
		MCAS miner = new MCAS(
				  Double.valueOf(args[0]), 
				  Double.valueOf(args[1]), 
				  Boolean.valueOf(args[2]), 
				  Boolean.valueOf(args[3]), 
				  Boolean.valueOf(args[4]), 
				  Boolean.valueOf(args[5]),
				  Boolean.valueOf(args[6]),
				  baseMiner);
		
		//miner.findBestModel("BPI2015_Municipality4_NoiseFiltered_removeSTART_END.xes");
		try(Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + File.separator + "logs"))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            System.out.println(filePath.toString());
		    		String fileName = filePath.getFileName().toString();
		    		System.out.println("=================================================");
		    		System.out.println(fileName);
		    		
		    		// Import Log
					System.out.println("Import log file");
					OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
					XLog log;
					try {
						log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + "logs" + 
																File.separator + fileName));
						
						int MinSS = 0;
						if (fileName.contains("BPIC12")) {
							MinSS = 3;
						}
						else if (fileName.contains("BPIC13")) {
							MinSS = 4;
						}
						else if (fileName.contains("BPIC15_1")) {
							MinSS = 6;
						}
						else if (fileName.contains("BPIC15_2")) {
							MinSS = 28;
						}
						else if (fileName.contains("BPIC15_3")) {
							MinSS = 6;
						}
						else if (fileName.contains("BPIC15_4")) {
							MinSS = 7;
						}
						else if (fileName.contains("BPIC15_5")) {
							MinSS = 7;
						}
						else if (fileName.contains("BPIC17")) {
							MinSS = 4;
						}
						else {
							throw new Exception("Unknown log file!");
						}
						StageDecomposition stageModel = new StageDecomposition(log, MinSS, new StageMiningHighestModularity());
						//double gtIndex = Measure.computeMeasure(stageModel.getStageListModel().getActualStageLabels(), example.getGroundTruth(log), 2);
						double gtIndex = LogUtils.compareWithLogGroundTruth(stageModel.getStageListModel().getActualStageLabels(), log);
						System.out.println("Stage Decomposition with MinSS = " + MinSS);
						System.out.println("Ground-truth Index = " + gtIndex);
						
						miner.mineBestModel(log, ModelMetrics.FSCORE, stageModel);
						//miner.mineBestModel(log, new FodinaWrapper(false, false), ModelMetrics.FSCORE, stageModel);
//						miner.mineBestModel(log, new SplitMinerWrapper(false, false), ModelMetrics.FSCORE);
//						Set<AbstractMinerWrapper> miners = new HashSet<>();
//						miners.add(new InductiveMinerWrapper(false, false, false));
//						miners.add(new FodinaWrapper(false, false, false));
//						miner.mineBestModel(log, new AggregateWrapper(false, false, miners), ModelMetrics.FSCORE);
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
	
	public void clear() {
		this.subMiners.clear();
		this.miningResult = null;
	}
	
//	@Override
//	public MiningResult mineBestModel(XLog log, Miner baseMiner, ModelMetrics metric) throws Exception {
//		return null;
//	}
		
	@Override
	public MiningResult mineBestModel(XLog originalLog, ModelMetrics metric, StageDecomposition stageModel) throws Exception {
		long startTime = System.currentTimeMillis();
		
		this.clear();
		
		this.setCodeName(CODE + "_" + baseMiner.getCodeName());

		String logName = LogUtils.getConceptName(originalLog).toString();
		FakePluginContext context = new FakePluginContext();
		List<MiningResult> allResults = new ArrayList<>();
		
		if (this.writeLogFiles) stageModel.getStageListModel().writeSublogs(System.getProperty("user.dir"));
		
		//---------------------------------------------
		// Mine submodels from the original sublogs
		//---------------------------------------------
		System.out.println("Initial mining submodels from sublogs");
		for (Stage stage : stageModel.getStageListModel()) subMiners.add(new StageSubModelMiner(stage, baseMiner));
		List<MiningResult> subResults = new ArrayList<>();
		int stage = 0;
		for (StageSubModelMiner subMiner : subMiners) {
			stage++;
			System.out.println("+++++++++++ MINE SUBMODEL FOR STAGE #" + stage + " ++++++++++++");
			MiningResult subResult = subMiner.mine(metric);
			if (subResult != null) {
				subResults.add(subResult);
			}
			else {
				System.out.println("Cannot find satisfied submodel for Stage #" + stage + ". Mining stops!");
				return null;
			}
		}
		
		if (this.writeLogFiles) {
			this.writeSubMinerPreprocessLogs(System.getProperty("user.dir"), "_preprocessed");
		}

		
		//---------------------------------------------
		// Chain submodels
		//---------------------------------------------
		System.out.println("Chaining step");
		MiningResult chainRes = this.chain(context, originalLog, subResults);
		if (this.writeModelFiles) {
			this.writeSubModelToFiles(context, "for_chaining");
			this.writeModelToFile(context, chainRes, LogUtils.getConceptName(originalLog) + "_chained", this.getCodeName(), "");
		}
		this.evaluateSoundnessAndQuality(context, chainRes, originalLog);
		
		boolean doAdjust = true; 
		boolean doStitch = true;
		if (chainRes.getEvaluation().getFscore() < 0) { // time-out
			System.out.println("Cannot find satisfied chained model! Mining stops.");
			doAdjust = false;
			doStitch = false;
		}
		allResults.add(chainRes);
		
		//---------------------------------------------
		// Adjust step
		//---------------------------------------------
		if (!this.adjust) doAdjust = false;
		List<MiningResult> adjustedSubResults = subResults;
		MiningResult adjustedRes = chainRes;
		int adjustIteration = 1;
		while (doAdjust) {
			System.out.println("Adjust step");
			System.out.println("+++++++++ Adjusting iteration #" + adjustIteration +  "   +++++++++++");
			adjustIteration++;
			List<MiningResult> newSubResults = this.adjust(context, originalLog, ProblemType.RELATION, this.imprecisionThreshold, subMiners, adjustedRes, adjustedSubResults, metric);
			if (!newSubResults.isEmpty()) {
				MiningResult newRes = this.chain(context, originalLog, newSubResults);
				this.evaluateSoundnessAndQuality(context, newRes, originalLog);
				adjustedRes = this.compareMiningResult(adjustedRes, newRes, metric, this.selectSoundModel());
				
				if (adjustedRes == newRes) {
					adjustedSubResults = newSubResults;
					allResults.add(newRes);
					System.out.println("Adjusting improved F-score!");
				}
				else {
					doAdjust = false;
					System.out.println("Adjusting did not improve F-score. Adjust stops!");
				}
				
				if (this.writeLogFiles) {
					this.writeSubMinerLogs(System.getProperty("user.dir"), 
							"_adjusted_" + adjustIteration + "_improved_" + doAdjust);
				}
				if (this.writeModelFiles) {
					this.writeSubModelToFiles(context, "_adjusted_" + adjustIteration + "_improved_" + doAdjust);
					this.writeModelToFile(context, adjustedRes, LogUtils.getConceptName(originalLog) + 
											"_adjusted_" + adjustIteration + "_improved_" + doAdjust, 
											this.getCodeName(), "");
				}
			}
			else {
				doAdjust = false;
				System.out.println("No problems found from diagnosis. Adjust stops!");
			}
		}
		
		//---------------------------------------------
		// Stitch step
		//---------------------------------------------
		if (doStitch) {
			System.out.println("Stitch step");
			MiningResult stitchRes = this.stitchBest(context, adjustedRes, metric, stageModel, originalLog);
			adjustedRes = this.compareMiningResult(adjustedRes, stitchRes, metric, this.selectSoundModel());
			if (adjustedRes == stitchRes) {
				allResults.add(stitchRes);
				System.out.println("Stitching improved F-score!");
				if (this.writeModelFiles) this.writeModelToFile(context, adjustedRes, 
						LogUtils.getConceptName(originalLog) + "_stitched_improved_true",
						this.getCodeName(), "");
			}
			else {
				System.out.println("Stitching did NOT improve F-score!");
				if (this.writeModelFiles) this.writeModelToFile(context, adjustedRes, 
						LogUtils.getConceptName(originalLog) + "_stitched_improved_false",
						this.getCodeName(), "");
			}
		}
		
		//---------------------------------------------
		// Finish
		//---------------------------------------------
		if (adjustedRes != null) {
			if (!this.selectSoundModel()) this.evaluateSoundness(context, adjustedRes); //eval soundness for the final model
			this.evaluateComplexity(adjustedRes);
			adjustedRes.setMiningTime(System.currentTimeMillis() - startTime);
		}
		
		if (this.writeResultFiles) this.writeResultsToFile(allResults, logName, this.getCodeName(), "");
		this.miningResult = adjustedRes;
		return this.miningResult;
	}
	
	/**
	 * Get the whole model from submodels without any interstage edges
	 * The model is supposed to be sound, so no need to check soundness
	 * @param context
	 * @param log
	 * @param stageMiner
	 * @param subMiningResults
	 * @return
	 * @throws Exception
	 */
	private MiningResult chain(UIPluginContext context, XLog log, List<MiningResult> subMiningResults) throws Exception {
		//---------------------------------------------
		// Connect all submodels to have one model
		//---------------------------------------------
		System.out.println("Combine all sub-diagrams into one");
		List<BPMNDiagram> bestSubDiagrams = new ArrayList<>();
		for (MiningResult res : subMiningResults) {
			bestSubDiagrams.add(res.getBPMN());
		}
		BPMNDiagram bpmn = this.combineSubDiagrams(bestSubDiagrams);
		
		BPMNUtils.removeSuperfluousGateways(bpmn);
		
		//------------------------------------------
		// Convert BPMN to Petrinet
		//------------------------------------------
		System.out.println("Convert BPMN diagram to Petrinet");
		PetrinetResult petrinetRes = BPMNUtils.BPMNToPetriNet_Raf(bpmn);

		SortedMap<String,String> paramValueMap = new TreeMap<>();
		paramValueMap.put("1-minStageSize", 0.0 + "");
		paramValueMap.put("2-jumpingStartEdgeThres", 0.0 + "");
		paramValueMap.put("3-jumpingForwardEdgeThres", 0.0 + "");				
		paramValueMap.put("4-jumpingBackwardEdgeThres", 0.0 + "");
		paramValueMap.put("5-jumpingExitEdgeThres", 0.0 + "");
		
		return new MiningResult(log, paramValueMap, bpmn, petrinetRes, 0);
	}
	
	/**
	 * Adjust submodels based on checking an intermediary chain model and checking for possible noises
	 * The new chain model is mined after removing noises
	 * For any issues, return the current result and subresults
	 * @param context
	 * @param log
	 * @param noiseType
	 * @param subMiners
	 * @param currentResult
	 * @param currentSubResults
	 * @param metric
	 * @return: empty list or new list of mining subresults
	 * @throws Exception
	 */
	private List<MiningResult> adjust(UIPluginContext context, XLog log, ProblemType problemType, double problemThreshold, 
									List<StageSubModelMiner> subMiners, 
									MiningResult currentResult, List<MiningResult> currentSubResults, ModelMetrics metric) throws Exception {
		List<StageSubModelMiner> rerunSubMiners = new ArrayList<>();
		MiningResult adjustedRes = currentResult;
		List<MiningResult> adjustedSubResults = new ArrayList<>();
		
		System.out.println("Check with the overall model for model problems");
		
		Set<Problem> problems = this.diagnose(adjustedRes, problemType, problemThreshold);
		
		System.out.println("Found Problems: " + problems.toString());
		if (!problems.isEmpty()) {
			for (StageSubModelMiner subMiner : subMiners) {
				if (problemType == ProblemType.EVENTCLASS) {
					if (subMiner.addProblemClasses(problems)) rerunSubMiners.add(subMiner);
				}
				else if (problemType == ProblemType.RELATION) {
					if (subMiner.addProblemRelations(problems)) rerunSubMiners.add(subMiner);
				}
				else {
					throw new Exception("Unknown problem type selected!");
				}
			}
			
			System.out.println("Mine with adjustments");
			List<MiningResult> newSubResults = new ArrayList<>();
			for (StageSubModelMiner subMiner : subMiners) {
				if (rerunSubMiners.contains(subMiner)) {
					MiningResult newSubResult = subMiner.mine(metric);
					if (newSubResult != null) newSubResults.add(newSubResult);
				}
				else {
					newSubResults.add(currentSubResults.get(subMiners.indexOf(subMiner))); //take the old result
				}
			}
			
			if (newSubResults.size() == subMiners.size()) {
				adjustedSubResults = newSubResults;
			}
			else {
				System.err.println("Some submodels after adjustment are not satisfied, e.g. unsound models. No adjustments were made!");
			}
		}
		
		return adjustedSubResults;
	}
	
	public String getCodeName() {
		return codeName;
	}
	
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
	
	private MiningResult stitchBest(UIPluginContext context, MiningResult currentResult, ModelMetrics metric, 
									StageDecomposition stageModel, XLog originalLog) throws Exception {
		
		MiningResult bestMiningResult = currentResult;
		for (float interStageThres=0.0f;interStageThres<=0.8f;interStageThres+=0.1f) {
			System.out.println("-------------------------------------");
			System.out.println("Stitch submodels with interstage threshold = " + interStageThres);
			
			SortedMap<String,String> paramValueMap = new TreeMap<>();
			paramValueMap.put("1-minStageSize", "0");
			paramValueMap.put("2-jumpingStartEdgeThres", interStageThres + "");
			paramValueMap.put("3-jumpingForwardEdgeThres", interStageThres + "");				
			paramValueMap.put("4-jumpingBackwardEdgeThres", interStageThres + "");
			paramValueMap.put("5-jumpingExitEdgeThres", interStageThres + "");
			
			MiningResult stitchRes = this.stitch(context, currentResult.getBPMN(), paramValueMap, stageModel, originalLog);
			if (stitchRes != null) {
//				if (this.writeModelFiles) this.writeModelToFile(context, stitchRes, LogUtils.getConceptName(originalLog) + "_stitched");
				//this.evaluate(context, stitchRes, originalLog, this.selectSoundModel(), true, true);
				this.evaluateSoundnessAndQuality(context, stitchRes, originalLog);
				bestMiningResult = this.compareMiningResult(bestMiningResult, stitchRes, metric, this.selectSoundModel());
			}
			else {
				break;
			}
		}
		return bestMiningResult;
	}
	
	/**
	 * Stitch stage diagrams in the input BPMN diagram
	 * The stitching edges are managed by a stage miner. The stitch is based on 
	 * frequency thresholds as input parameters.
	 * Note that the input diagram will not be changed. 
	 * @param context
	 * @param diagram
	 * @param paramValueMap
	 * @param stageMiner
	 * @param log
	 * @return
	 * @throws Exception
	 */
	private MiningResult stitch(UIPluginContext context, BPMNDiagram diagram, SortedMap<String,String> paramValueMap, StageDecomposition stageModel, XLog log) throws Exception {
		float jumpingStartEdgeThres = Float.valueOf(paramValueMap.get("2-jumpingStartEdgeThres"));
		float jumpingForwardEdgeThres = Float.valueOf(paramValueMap.get("3-jumpingForwardEdgeThres"));
		float jumpingBackwardEdgeThres = Float.valueOf(paramValueMap.get("4-jumpingBackwardEdgeThres"));
		float jumpingExitEdgeThres = Float.valueOf(paramValueMap.get("5-jumpingExitEdgeThres"));
		
		BPMNDiagram oneDiagram = this.copy(diagram);
		
		//------------------------------------------
		// Check inter-stage and exit edges, obtain pair
		// of nodes exceeding thresholds to be connected later
		//------------------------------------------
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		System.out.println("Jumping start edges: " + stageModel.getJumpingStartEdges().toString());
		System.out.println("Jumping forward edges: " + stageModel.getJumpingForwardEdges().toString());
		System.out.println("Jumping backward edges: " + stageModel.getJumpingBackwardEdges().toString());
		System.out.println("Jumping exit edges: " + stageModel.getJumpingExitEdges().toString());
		List<List<BPMNNode>> jumpingStartPairs = this.checkJumpingStartEdges(oneDiagram, stageModel.getJumpingStartEdges(), jumpingStartEdgeThres, logInfo);
		List<List<BPMNNode>> jumpingExitPairs = this.checkJumpingExitEdges(oneDiagram, stageModel.getJumpingExitEdges(), jumpingExitEdgeThres, logInfo);
		List<List<BPMNNode>> jumpingForwardPairs = checkJumpingInterstageEdges(oneDiagram, stageModel.getJumpingForwardEdges(), stageModel, logInfo, jumpingForwardEdgeThres);
		List<List<BPMNNode>> jumpingBackwardPairs = checkJumpingInterstageEdges(oneDiagram, stageModel.getJumpingBackwardEdges(), stageModel, logInfo, jumpingBackwardEdgeThres);
		
		if (jumpingStartPairs.isEmpty() && jumpingExitPairs.isEmpty() && jumpingForwardPairs.isEmpty() &&
				jumpingBackwardPairs.isEmpty()) {
			System.out.println("No interstage edges are selected. Stitching stops!");
			return null;
		}

		System.out.println("Jumping start edges selected: " + jumpingStartPairs.toString());
		System.out.println("Jumping forward edges selected: " + jumpingForwardPairs.toString());
		System.out.println("Jumping backward edges selected: " + jumpingBackwardPairs.toString());
		System.out.println("Jumping exit edges selected: " + jumpingExitPairs.toString());
		
		//---------------------------------------------
		// Convert all AND fragments affected by inter-stage
		// and exit edge endpoints to OR fragments
		//---------------------------------------------
		List<List<BPMNNode>> connectedNodes = new ArrayList<>();
		connectedNodes.addAll(jumpingStartPairs);
		connectedNodes.addAll(jumpingForwardPairs);
		connectedNodes.addAll(jumpingBackwardPairs);
		connectedNodes.addAll(jumpingExitPairs);
		
		boolean converted = false;
		if (!connectedNodes.isEmpty()) {
			converted = this.convertANDtoOR(connectedNodes, oneDiagram);
			if (converted) System.out.println("Some AND gateways have been converted to ORs!!!");
		}
		else {
			System.out.println("There are no interstage and exit edges selected.");
		}

		//------------------------------------------
		// Connect all jumping edges
		//------------------------------------------
		if (!connectedNodes.isEmpty()) {
			System.out.println("Add inter-stage and exit edges to the diagram");
			for (List<BPMNNode> pair : connectedNodes) {
				this.connect(pair.get(0), pair.get(1), oneDiagram);
			}
		}
		
		//------------------------------------------
		// Simplify the diagram
		//------------------------------------------
		BPMNUtils.removeSuperfluousGateways(oneDiagram);
//		if (this.writeModelFiles) this.writeModelToFile(context, null, oneDiagram, 
//															LogUtils.getConceptName(log) + "_Before_ReplaceIORs", null);
		
		//------------------------------------------
		// Call conversion of all OR to AND/XOR
		//------------------------------------------
		if (converted) {
			System.out.println("Convert all OR gateways in the diagram to AND/XOR");
			oneDiagram = ReplaceIORsPlugin.ReplaceIORs(context, oneDiagram);
		}
		
		//------------------------------------------
		// Convert BPMN to Petrinet
		//------------------------------------------
		System.out.println("Convert BPMN diagram to Petrinet");
		PetrinetResult petrinetRes = BPMNUtils.BPMNToPetriNet_Raf(oneDiagram);
		
		return new MiningResult(log, paramValueMap, oneDiagram, petrinetRes, 0);
	}
	 
	
	/**
	 * Convert all AND gateways containing pair of conntected nodes to OR gateways 
	 * @param connectedNodes: pair of nodes to be connected
	 * @param d: diagram
	 * @throws Exception
	 */
	private boolean convertANDtoOR(List<List<BPMNNode>> connectedNodes, BPMNDiagram d) throws Exception {
		boolean hasANDconverted = false;
		RPSTHelper rpstHelper = new RPSTHelper();
		boolean modelChanged = true;
		for (List<BPMNNode> pair : connectedNodes) {
			if (modelChanged) {
				rpstHelper.initialize(d);
				modelChanged = false;
			}
			List<BPMNNode> andNodes1 = rpstHelper.findContainingANDFragment(pair.get(0));
			List<BPMNNode> andNodes2 = rpstHelper.findContainingANDFragment(pair.get(1));
			
			if (!andNodes1.isEmpty() && andNodes2.isEmpty()) {
				BPMNUtils.convertANDToOR(andNodes1.get(0));
				BPMNUtils.convertANDToOR(andNodes1.get(1));
				modelChanged = true;
				hasANDconverted = true;
			}
			else if (andNodes1.isEmpty() && !andNodes2.isEmpty()) {
				BPMNUtils.convertANDToOR(andNodes2.get(0));
				BPMNUtils.convertANDToOR(andNodes2.get(1));
				modelChanged = true;
				hasANDconverted = true;
			}
			else if (!andNodes1.isEmpty() && !andNodes2.isEmpty() & andNodes1.get(0) != andNodes2.get(0)) {
				BPMNUtils.convertANDToOR(andNodes1.get(0));
				BPMNUtils.convertANDToOR(andNodes1.get(1));
				BPMNUtils.convertANDToOR(andNodes2.get(0));
				BPMNUtils.convertANDToOR(andNodes2.get(1));
				modelChanged = true;
				hasANDconverted = true;
			}
		}		
		return hasANDconverted;
	}
	
	private List<List<BPMNNode>> checkJumpingInterstageEdges(BPMNDiagram d, Map<String,Integer> jumpingEdges, 
											StageDecomposition stageMiner, XLogInfo logInfo, float interStageThres) throws Exception {
		Map<String,Integer> activityTostageMap = new HashMap<>();
		Map<String,Integer> stageTostageMap = new HashMap<>();
		
		//Compute activity to stage and state to stage connection weight (frequency)
		for (String key : jumpingEdges.keySet()) {
			int freq = jumpingEdges.get(key);
			String[] keys = key.split("@");
			int sourceStage = stageMiner.getStageListModel().findStageIndex(keys[0]);
			int targetStage = stageMiner.getStageListModel().findStageIndex(keys[1]);
			String a2skey = keys[0] + "@" + targetStage;
			String s2skey = sourceStage + "@" + targetStage;
			activityTostageMap.put(a2skey, activityTostageMap.containsKey(a2skey) ? (activityTostageMap.get(a2skey)+freq) : freq);
			stageTostageMap.put(s2skey, stageTostageMap.containsKey(s2skey) ? (stageTostageMap.get(s2skey)+freq) : freq);
		}
		
		//Connect inter-stage edges considering the edge weight
		List<List<BPMNNode>> listEndpoints = new ArrayList<>();
		for (String key : jumpingEdges.keySet()) {
			String[] keys = key.split("@");
			BPMNNode source = this.getNodeByName(keys[0], d);
			BPMNNode target = this.getNodeByName(keys[1], d);	
			if (source == null || target == null) {
//				if (!LogUtils.containClass(logInfo, keys[0]) || !LogUtils.containClass(logInfo, keys[1])) {
//					throw new Exception("Cannot find a BPMN node with name=" + keys[0] + " or " + keys[1]);
//				}
			}
			else {	
				if (1.0*jumpingEdges.get(key)/logInfo.getNumberOfTraces() >= interStageThres) {		
					List<BPMNNode> endPoints = new ArrayList<>();
					endPoints.add(source);
					endPoints.add(target);
					listEndpoints.add(endPoints);
				}
			}
		}	
		return listEndpoints;
	}
	
	private List<List<BPMNNode>> checkJumpingExitEdges(BPMNDiagram d, Map<String,Integer> jumpingEdges, 
												float jumpingExitEdgeThres, XLogInfo logInfo) throws Exception {
		List<List<BPMNNode>> listEndpoints = new ArrayList<>();
		for (String key : jumpingEdges.keySet()) {
			BPMNNode node = this.getNodeByName(key, d);
			if (node != null && 1.0*jumpingEdges.get(key)/logInfo.getNumberOfTraces() >= jumpingExitEdgeThres) {
				List<BPMNNode> endPoints = new ArrayList<>();
				endPoints.add(node);
//				endPoints.add(endNode);
				endPoints.add(BPMNUtils.getEndEvent(d));
				listEndpoints.add(endPoints);
			}
		}		
		return listEndpoints;
	}
	
	
	private List<List<BPMNNode>> checkJumpingStartEdges(BPMNDiagram d, Map<String,Integer> jumpingEdges, 
			float jumpingStartEdgeThres, XLogInfo logInfo) throws Exception {
	
	List<List<BPMNNode>> listEndpoints = new ArrayList<>();
	for (String key : jumpingEdges.keySet()) {
		BPMNNode node = this.getNodeByName(key, d);
		if (node != null && 1.0*jumpingEdges.get(key)/logInfo.getNumberOfTraces() >= jumpingStartEdgeThres) {
			List<BPMNNode> endPoints = new ArrayList<>();
//			endPoints.add(startNode);
			endPoints.add(BPMNUtils.getStartEvent(d));
			endPoints.add(node);
			listEndpoints.add(endPoints);
		}
	}		
	return listEndpoints;
}


	/**
	 * Note that submodels will have format:
	 * First submodel:  start->activities->MilestoneNode
	 * Normal submodels: MilestoneNode->activities->MilestoneNode
	 * Last submodel: TransitionNode->activities->end
	 * @param subDiagrams
	 * @return
	 * @throws Exception
	 */
	private BPMNDiagram combineSubDiagrams(List<BPMNDiagram> subDiagrams) throws Exception {
		Map<BPMNNode,BPMNNode> nodeMap = new HashMap<>(); //map from sub-diagram to one diagram
		Map<BPMNNode,BPMNNode> nodePair = new HashMap<>(); // source->target: pair of connected nodes 
		BPMNDiagram oneDiagram = BPMNDiagramFactory.newBPMNDiagram("");

		Event modelStart = oneDiagram.addEvent("ModelStart", Event.EventType.START, Event.EventTrigger.NONE, Event.EventUse.CATCH, true, null);
		Event modelEnd = oneDiagram.addEvent("ModelEnd", Event.EventType.END, Event.EventTrigger.NONE, Event.EventUse.CATCH, true, null);
		
		for (BPMNDiagram d: subDiagrams) {
			this.copy(d, oneDiagram, nodeMap);
		}
		
		//Connect the start event with the first sub-diagram and the last sub-diagram with the end event
		if (subDiagrams.size() > 0) {
			BPMNDiagram firstDiagram = subDiagrams.get(0);
			Event firstDiagramStart = BPMNUtils.getStartEvent(firstDiagram);
			if (firstDiagramStart == null) {
				throw new Exception("Cannot find the start event of the first submodel");
			}
			for (Flow f : firstDiagram.getFlows()) {
				if (f.getSource() == firstDiagramStart) {
					oneDiagram.addFlow(modelStart, nodeMap.get(f.getTarget()), "");
					nodePair.put(modelStart, nodeMap.get(f.getTarget())); //modelStart-start
					oneDiagram.removeNode(nodeMap.get(firstDiagramStart));
					break;
				}
			}
			
			BPMNDiagram lastDiagram = subDiagrams.get(subDiagrams.size()-1);
			Event lastDiagramEnd = BPMNUtils.getEndEvent(lastDiagram);
			if (lastDiagramEnd == null) {
				throw new Exception("Cannot find the end event of the last submodel");
			}
			for (Flow f : lastDiagram.getFlows()) {
				if (f.getTarget() == lastDiagramEnd) {
					oneDiagram.addFlow(nodeMap.get(f.getSource()), modelEnd, "");
					nodePair.put(nodeMap.get(f.getSource()), modelEnd); //end-ModelEnd
					oneDiagram.removeNode(nodeMap.get(lastDiagramEnd));
					break;
				}
			}
		}
		
		// Connect two adjacent sub-diagrams
		for (int i=0;i<subDiagrams.size()-1;i++) {
			if (i<(subDiagrams.size()-1)) {
				this.connect(subDiagrams.get(i), subDiagrams.get(i+1), oneDiagram, nodeMap, nodePair);
			}
		}
		
		// Combine three types of node pairs identified in previous steps
		// Pair1: <modelStart,start>
		// Pair2: <end,modelEnd>
		// Pair3: <MilestoneNode,MilestoneNode> (they are the same milestone node)
		Collection<Flow> flows = new HashSet<>(oneDiagram.getFlows());
		for (BPMNNode n1 : nodePair.keySet()) {
			BPMNNode n2 = nodePair.get(n1); // n1 is connected to n2 (n1 -> n2)
			if (n1 == modelStart) { // n1 is modelStart, n2 is "start" (a fake milestone node of the first submodel)
				//Connect all edges to an XOR split and connect the start event with the XOR split 
				Gateway xorSplit = oneDiagram.addGateway("StageStart", GatewayType.DATABASED);
				for (Flow f : flows) {
					if (f.getSource() == n2) {
						oneDiagram.addFlow(xorSplit, f.getTarget(), "");
					}
				}
				oneDiagram.addFlow(n1, xorSplit, ""); 
				oneDiagram.removeNode(n2);
			}
			else if (n2 == modelEnd) { // n2 is "end" (fake mielstone of the last stage), n1 is modelEnd
				//Connect all edges to an XOR join and connect XOR join with the end event
				Gateway xorJoin = oneDiagram.addGateway("StageEnd", GatewayType.DATABASED);
				for (Flow f : flows) {
					if (f.getTarget() == n1) {
						oneDiagram.addFlow(f.getSource(), xorJoin, "");
					}
				}
				oneDiagram.addFlow(xorJoin, n2, "");
				oneDiagram.removeNode(n1);
			}
			else { 
				// n1 and n2 are the same milestone node, keep one only.
				Gateway xorSplit = oneDiagram.addGateway("StageMilestone", GatewayType.DATABASED);
				for (Flow f : flows) {
					if (f.getSource() == n2) {
						oneDiagram.addFlow(xorSplit, f.getTarget(), "");
					}
				}
				oneDiagram.addFlow(n1, xorSplit, "");
				oneDiagram.removeNode(n2);
				
			}
		}
		
		return oneDiagram;
	}
	
	/**
	 * Connect source and target nodes via XOR split and XOR join
	 * @param source
	 * @param target
	 * @param d
	 * @throws Exception 
	 */
	private void connect(BPMNNode source, BPMNNode target, BPMNDiagram d) throws Exception {
		
		if (BPMNUtils.isConnected(source, target)) return;
		
		Flow sourceOutgoing = null;
		Flow targetIncoming = null;
		if (d.getOutEdges(source).size() > 1 && (source instanceof Activity)) {
			throw new Exception("There are multiple ougoing edges from an activity node: " + source.getLabel());
		}
		if (d.getInEdges(target).size() > 1 && (target instanceof Activity)) {
			throw new Exception("There are multiple incoming edges to an activity node: " + target.getLabel());
		}
		sourceOutgoing = (Flow)d.getOutEdges(source).iterator().next();
		targetIncoming = (Flow)d.getInEdges(target).iterator().next();
		
		// Add flow from the source to a xorSplit
		Gateway xorSplit;
		if (BPMNUtils.isXORSplit(source)) {
			xorSplit = (Gateway)source;
		}
		else if (BPMNUtils.isXORSplit(sourceOutgoing.getTarget())) {
			xorSplit = (Gateway)sourceOutgoing.getTarget();
		}
		else {
			xorSplit = d.addGateway("", GatewayType.DATABASED);
			d.addFlow(source, xorSplit, "");
			d.addFlow(xorSplit, sourceOutgoing.getTarget(), "");
			d.removeEdge(sourceOutgoing);
		}
		
		// Add flow from xorJoin to the target
		Gateway xorJoin;
		if (BPMNUtils.isXORJoin(target)) {
			xorJoin = (Gateway)target;
		}
		else if (BPMNUtils.isXORJoin(targetIncoming.getSource())) {
			xorJoin = (Gateway)targetIncoming.getSource();
		}
		else {
			xorJoin = d.addGateway("", GatewayType.DATABASED);
			d.addFlow(targetIncoming.getSource(), xorJoin, "");
			d.addFlow(xorJoin, target, "");
			d.removeEdge(targetIncoming);
		}
		
		// Connect xorSplit to xorJoin
		d.addFlow(xorSplit, xorJoin, "");
	}
	
	private BPMNNode getNodeByName(String name, BPMNDiagram d) {
		for (BPMNNode n : d.getNodes()) {
			if (n.getLabel().toLowerCase().equals(name.toLowerCase())) {
				return n;
			}
		}
		return null;
	}
	
	private void copy(BPMNDiagram d, BPMNDiagram target, Map<BPMNNode,BPMNNode> nodeMap) {
		//All nodes
		for (BPMNNode n : d.getNodes()) {
			if (n instanceof Activity) {
				nodeMap.put(n, target.addActivity(n.getLabel(), false, false, false, false, false));
			}
			else if (n instanceof Gateway) {
				nodeMap.put(n, target.addGateway(n.getLabel(), ((Gateway)n).getGatewayType()));
			}
			else if (n instanceof Event) {
				nodeMap.put(n, target.addEvent(n.getLabel(), ((Event) n).getEventType(),
								((Event) n).getEventTrigger(),
								((Event) n).getEventUse(), null));
						
			}
		}
		
		//All edges
		for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : d.getEdges()) {
			target.addFlow(nodeMap.get(e.getSource()), nodeMap.get(e.getTarget()), e.getLabel());
		}
	}
	
	private BPMNDiagram copy(BPMNDiagram d) {
		BPMNDiagram newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
		this.copy(d, newDiagram, new HashMap<BPMNNode, BPMNNode>());
		return newDiagram;
	}
	
	/**
	 * Connect d1 and d2 (d1 precedes d2)
	 * @param d1
	 * @param d2
	 * @throws Exception
	 */
	private void connect(BPMNDiagram d1, BPMNDiagram d2, BPMNDiagram oneDiagram, 
							Map<BPMNNode,BPMNNode> nodeMap, Map<BPMNNode,BPMNNode> nodePair) throws Exception {
		Event d1End = BPMNUtils.getEndEvent(d1);
		Event d2Start = BPMNUtils.getStartEvent(d2); //to be connected with d1End
		
		BPMNNode d1LastAct = BPMNUtils.getLastAct(d1); // transition event
		if (d1LastAct == null) throw new Exception("Cannot find the last activity of a diagram while connecting two consecutive diagrams.");
		
		BPMNNode d2FirstAct = BPMNUtils.getFirstAct(d2); // transition event
		if (d2FirstAct == null) throw new Exception("Cannot find the first activity of a diagram while connecting two consecutive diagrams.");
		
		//Connect d1LastAct and d2FirstAct
		oneDiagram.addFlow(nodeMap.get(d1LastAct), nodeMap.get(d2FirstAct), "");
		nodePair.put(nodeMap.get(d1LastAct), nodeMap.get(d2FirstAct)); //save this pair for merging them later
		oneDiagram.removeNode(nodeMap.get(d1End));
		oneDiagram.removeNode(nodeMap.get(d2Start));
	}
	
	@Override
	public Set<Problem> diagnose(MiningResult wholeRes, ProblemType noiseType, double threshold) {
		if (noiseType == ProblemType.EVENTCLASS) {
			return this.findProblemClasses(wholeRes, threshold);
		}
		else if (noiseType == ProblemType.RELATION) {
			return this.findProblemRelations(wholeRes, threshold);
		}
		
		return null;
	}

	
	private Set<Problem> findProblemClasses(MiningResult wholeRes, double taskUseThreshold) {
		Automaton automaton = wholeRes.getEvaluation().getAutomaton();
		Graph<AutomatonNode, AutomatonEdge> g = automaton.getJUNG();
		
		// Search for all actual and avail tasks with their frequencies
		Map<String,Integer> mapAvailTasks = new HashMap<>();
		Map<String,Integer> mapActualTasks = new HashMap<>();
		for( AutomatonNode n: g.getVertices()) {
			for (Transition tran : n.getAvailableTasks()) {
				if (tran.isInvisible()) {
					List<Transition> visibleSuccessors = PetriNetUtils.getVisibleTransitionSuccessors(
																		wholeRes.getPetrinetRes().getPetrinet(), tran);
					for (Transition tran2 : visibleSuccessors) {
						String key2 = tran2.getLabel();
						if (!mapAvailTasks.containsKey(key2)) mapAvailTasks.put(key2, 0);
						mapAvailTasks.put(key2, mapAvailTasks.get(key2) + (int)n.getWeight());
					}
				}
				else {
					String key = tran.getLabel();
					if (!mapAvailTasks.containsKey(key)) mapAvailTasks.put(key, 0);
					mapAvailTasks.put(key, mapAvailTasks.get(key) + (int)n.getWeight());
				}
			}
			
			// Not need to check for hidden transitions since
			// the actual alignment will always reach visible transitions
			for(AutomatonEdge e: g.getOutEdges(n)) {
				String key = e.getTransition().getLabel();
				if (!mapActualTasks.containsKey(key)) mapActualTasks.put(key, 0);
				mapActualTasks.put(key, mapActualTasks.get(key) + (int)e.getTarget().getWeight());
			}
		}
		
		// Calculate task use ratio
		Map<String,Double> mapTaskUseRatio = new HashMap<>();
		for (String key : mapActualTasks.keySet()) {
			if (mapAvailTasks.containsKey(key)) mapTaskUseRatio.put(key, 1.0*mapActualTasks.get(key)/mapAvailTasks.get(key));
		}
		// note that some tasks are on the model but might not actually effective
		// i.e. they appear in the available tasks, but they don't appear in the actual tasks
		for (String key : mapAvailTasks.keySet()) {
			if (!mapActualTasks.containsKey(key)) mapTaskUseRatio.put(key, 0.0);
		}

		Set<Problem> problemClasses = new HashSet<>();
		Set<String> problemClassNames = new HashSet<>();
		for (String taskName : mapTaskUseRatio.keySet()) {
			if (mapTaskUseRatio.get(taskName) <= taskUseThreshold) {
				problemClasses.add(new ProblemClass(taskName, mapTaskUseRatio.get(taskName)));
				problemClassNames.add(taskName);
			}
		}
		
		//-----------------------------------------------
		// Trawl extra problem classes
		//-----------------------------------------------
		Map<String,Integer> mapSuccessors = new HashMap<>();
		for( AutomatonNode n: g.getVertices()) {
			if (g.getInEdges(n).isEmpty()) continue;
			String nodeInEdgeName = g.getInEdges(n).iterator().next().getTransition().getLabel();
			if (problemClassNames.contains(nodeInEdgeName)) {
				Queue<AutomatonNode> bfsQueue = new LinkedList<>();
				bfsQueue.add(n);
				while (!bfsQueue.isEmpty()) {
					AutomatonNode node = bfsQueue.poll();
					for (AutomatonEdge e : g.getOutEdges(node)) {
						String eLabel = e.getTransition().getLabel();
						if (!problemClassNames.contains(eLabel)) {
							if (!mapSuccessors.containsKey(eLabel)) mapSuccessors.put(eLabel, 0);
							mapSuccessors.put(eLabel, mapSuccessors.get(eLabel) + (int)e.getTarget().getWeight());
						}
						bfsQueue.add(e.getTarget());
					}
				}
			}
		}
		XEventClasses eventClasses = XLogInfoFactory.createLogInfo(wholeRes.getLog(), XLogInfoImpl.NAME_CLASSIFIER).getEventClasses();
		for (String successor : mapSuccessors.keySet()) {
			if (eventClasses.getByIdentity(successor) != null) {
				if (1.0*mapSuccessors.get(successor)/mapActualTasks.get(successor) >= this.availThreshold) {
					problemClasses.add(new ProblemClass(successor, 0.0));
				}
			}
		}
		
		return problemClasses;
	}
	
	private Set<Problem> findProblemRelations(MiningResult wholeRes, double threshold) {
		Automaton automaton = wholeRes.getEvaluation().getAutomaton();
		Graph<AutomatonNode, AutomatonEdge> g = automaton.getJUNG();
		
		// Search for all actual and avail relations with their frequencies
		Map<String,Integer> mapAvailRelations = new HashMap<>();
		Map<String,Integer> mapActualRelations = new HashMap<>();
		for( AutomatonNode n: g.getVertices()) {
			if (g.getInEdges(n).isEmpty()) {
				continue; // ignore the root node
			}
			
			// Identify the visible source transition
			AutomatonEdge sourceEdge = g.getInEdges(n).iterator().next();
			while (sourceEdge != null && sourceEdge.getTransition().isInvisible()) {
				AutomatonNode eSource = sourceEdge.getSource();
				if (!g.getInEdges(eSource).isEmpty()) {
					sourceEdge = g.getInEdges(eSource).iterator().next();
				}
				else {
					sourceEdge = null;
				}
			}
			if (sourceEdge == null || sourceEdge.getTransition().isInvisible()) continue; // ignore the first edge after the root node
			Transition sourceTran = sourceEdge.getTransition();
			
			// Update the available relation frequencies
			// Only check nodes with visible transitions because all invisible transitions
			// will be evaluated with visible transitions.
			// Note that some relations could be higher than the total number of traces 
			// in the log because of loops in the model
			if (!g.getInEdges(n).iterator().next().getTransition().isInvisible()) {
				for (Transition tran : n.getAvailableTasks()) {
					if (tran.isInvisible()) {
						List<Transition> visibleSuccessors = PetriNetUtils.getVisibleTransitionSuccessors(
																			wholeRes.getPetrinetRes().getPetrinet(), tran);
						for (Transition successor : visibleSuccessors) {
							String indirectRel = sourceTran.getLabel() + "@" + successor.getLabel();
							if (!mapAvailRelations.containsKey(indirectRel)) mapAvailRelations.put(indirectRel, 0);
							mapAvailRelations.put(indirectRel, mapAvailRelations.get(indirectRel) + (int)n.getWeight());
						}
					}
					else {
						String directRel = sourceTran.getLabel() + "@" + tran.getLabel();
						if (!mapAvailRelations.containsKey(directRel)) mapAvailRelations.put(directRel, 0);
						mapAvailRelations.put(directRel, mapAvailRelations.get(directRel) + (int)n.getWeight());
					}
				}
			}
			
			// Ignore hidden transitions because the actual relation
			// would be found when target nodes are visited 
			for(AutomatonEdge e: g.getOutEdges(n)) {
				if (!e.getTransition().isInvisible()) {
					String actualRel = sourceTran.getLabel() + "@" + e.getTransition().getLabel();
					if (!mapActualRelations.containsKey(actualRel)) mapActualRelations.put(actualRel, 0);
					mapActualRelations.put(actualRel, mapActualRelations.get(actualRel) + (int)e.getTarget().getWeight());
				}
			}
		}
		
		// Calculate relational use ratio
		Map<String,Double> mapRelationRatio = new HashMap<>();
		for (String key : mapActualRelations.keySet()) {
			if (mapActualRelations.containsKey(key)) mapRelationRatio.put(key, 1.0*mapActualRelations.get(key)/mapAvailRelations.get(key));
		}
		// Note that some tasks are on the model but might not actually effective
		// i.e. they appear in the available relations, but they don't appear in the actual relations
		// But they severely affect precision because their availability is high
		// NOTE that base miner may make mistakes if this relation is highly frequent but not used
		// based on the alignment.
		for (String key : mapAvailRelations.keySet()) {
			if (!mapRelationRatio.containsKey(key) && 
					mapAvailRelations.get(key)> this.availThreshold*Collections.max(mapAvailRelations.values())) {
				mapRelationRatio.put(key, 0.0);
			}
		}
		
		Set<Problem> problemRelations = new HashSet<>();
		for (String relation : mapRelationRatio.keySet()) {
			if (mapRelationRatio.get(relation) <= threshold) {
				problemRelations.add(new ProblemRelation(relation));
			}
		}
		
		//For debug
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			File file = new File(System.getProperty("user.dir") + File.separator + 
							LogUtils.getConceptName(wholeRes.getLog()) + "_diagnose_problems.txt");
			// if file doesnt exists, then create it
			if (!file.exists()) file.createNewFile();
			// true = append file
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
			bw.write("Avail relations: " + "\n" + mapAvailRelations.toString() + "\n" + 
					"Relations ratio: " + "\n" + mapRelationRatio.toString() + "\n" + 
					"Problem relations: " + "\n" + problemRelations.toString() + "\n" + 
					"====================================================================" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) bw.close();
				if (fw != null) fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return problemRelations;
	}
	

	
	/*
	 * Note: SyncReplayResult has two corresponding objects
	 * getNodeInstance(): returns list of move steps: it can be a Transition object or XEventClass object
	 * getStepTypes(): returns the type of step types
	 * getTraceIndex(): returns the index of the corresponding trace in the log (a log is a list of trace)
	 * This method return list of step labels in correspondence with the list of step types
	 */
	private String getStepLabel(Object nodeInstance) {
		if (nodeInstance instanceof Transition) {
			return ((Transition)nodeInstance).getLabel();
		} else if (nodeInstance instanceof String) {
			return (String)nodeInstance;
		} else {
			return nodeInstance.toString();
		}
	}
}
