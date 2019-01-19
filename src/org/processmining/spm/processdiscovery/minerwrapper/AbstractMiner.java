package org.processmining.spm.processdiscovery.minerwrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;
import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import org.processmining.spm.filter.FilterParams;
import org.processmining.spm.processdiscovery.ModelMetrics;
import org.processmining.spm.processdiscovery.evaluation.EvaluationResult;
import org.processmining.spm.processdiscovery.evaluation.Evaluator;
import org.processmining.spm.processdiscovery.result.MiningResult;
import org.processmining.spm.processdiscovery.result.PetrinetResult;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

public class AbstractMiner implements Miner {
	protected boolean writeModelFiles = false;
	protected boolean writeResultFiles = false;
	protected FilterParams filterParams = null;
	protected MiningResult miningResult = null;
	
	public AbstractMiner() {
		this(false,false);
	}
	
	public AbstractMiner(boolean writeModelFiles, boolean writeResultFiles) {
		this.writeModelFiles = writeModelFiles;
		this.writeResultFiles = writeResultFiles;
	}
	
	public AbstractMiner(boolean writeModelFiles, boolean writeResultFiles, FilterParams filterParams) {
		this.writeModelFiles = writeModelFiles;
		this.writeResultFiles = writeResultFiles;
		this.filterParams = filterParams;
	}
	
	public String getCodeName() {
		// TODO Auto-generated method stub
		return "Abstract";
	}
	
	/**
	 * The latest mining result since the last time
	 * this miner was called to mine a model
	 * @return
	 */
	public MiningResult getMiningResult() {
		return this.miningResult;
	}

	public MiningResult mine(UIPluginContext context, XLog log, SortedMap<String,String> paramValueMap) throws Exception {
		return null;
	}
	
	public MiningResult mineBestModel(XLog log, ModelMetrics metrics) throws Exception {
		return null;
	}
	
	protected EvaluationResult evaluate(UIPluginContext context, MiningResult result, XLog log) throws ConnectionCannotBeObtained, Exception {
		PetrinetResult petrinetRes = result.getPetrinetRes();
		Petrinet petrinet = petrinetRes.getPetrinet();
		Marking initialMarking = petrinetRes.getInitialMarking();
		Set<Marking> finalMarkings = petrinetRes.getFinalMarkings();
		BPMNDiagram bpmn = result.getBPMN();
		
		System.out.println("Check soundness");
		boolean isSoundModel = Evaluator.checkSoundness_Woflan(context, petrinet);
		
		double fitness=0.0, precision=0.0, fscore=0.0, generalization = 0.0;
		Automaton automaton=null;
		PNRepResult alignments = null;
		CostBasedCompleteParam alignParams = null;
		if (isSoundModel) {
			System.out.println("The model is sound. Now evaluate fitness, precision and F-score");
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
				System.out.println("Aligning model and log is time-out!");
			}
			
		}
		else {
			System.out.println("The model is NOT sound. F-score is not calculated.");
		}
		
		System.out.println("Calculating model complexity");
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
		EvaluationResult evalRes = new EvaluationResult(alignParams, fitness, precision, generalization, 
										complexity[0], complexity[1], complexity[2], isSoundModel,
										automaton, alignments);
		result.setEvaluationResult(evalRes);
		return evalRes;
	}
	
	public void writeModelToFile(UIPluginContext context, MiningResult miningResult, String logName) throws IOException {
		Petrinet net = miningResult.getPetrinetRes().getPetrinet();
		BPMNDiagram bpmn = miningResult.getBPMN();
		String argsString = this.getArgString(miningResult.getArgMap());
		
		// Export petrinet to file
		if (net != null) {
			PnmlExportNetToPNML pnExporter = new PnmlExportNetToPNML();
			pnExporter.exportPetriNetToPNMLFile(context, net, 
												new File(System.getProperty("user.dir") + File.separator + 
														logName + "_" + this.getCodeName() + "_PETRINET_" + argsString + ".pnml"));
		}
		// Export BPMN to file
		if (bpmn != null) {
			BpmnExportPlugin bpmnExport = new BpmnExportPlugin();
			bpmnExport.export(context, bpmn, new File(System.getProperty("user.dir") + File.separator + 
														logName + "_" + this.getCodeName() + "_BPMN_" + argsString + ".bpmn"));
		}
	}
	
	public void writeResultsToFile(List<MiningResult> results, String logName, String minerName, String fileNameSuffix) throws IOException {
		List<String> argNames;
		String targetFile = System.getProperty("user.dir") + File.separator + logName + "_" + minerName + 
				"_Eval_" + fileNameSuffix + "_" + System.currentTimeMillis() + ".csv";
		if (!results.isEmpty()) {
			argNames = new ArrayList<>(results.get(0).getArgMap().keySet());
		}
		else {
			argNames = new ArrayList<>();
		}
		
//		String logName = LogUtils.getConceptName(results.get(0).getLog());
//		String minerName = results.get(0).getMiner().getCodeName();

		
		ICsvListWriter listWriter = null;
		try {
			CsvPreference pref = (new CsvPreference.Builder('\"', ',', "\n")).build(); // delimiter is a semicolon
		    listWriter = new CsvListWriter(new FileWriter(targetFile), pref);
		    
		    // write the header
		    List<String> header = new ArrayList<String>();
		    
		    for (String argName : argNames) {
		    	header.add(argName);
		    }
		    header.add("Soundness");
		    
		    header.add("Fitness");
		    header.add("Precision");
		    header.add("F-score");
		    
		    header.add("Size");
		    header.add("CFC");
		    header.add("Structuredness");
		    header.add("Sequentiality");
		    header.add("Separability");
		    header.add("Run-time");
		    
		    String[] headerArray = new String[header.size()];
		    headerArray = header.toArray(headerArray);
		    listWriter.writeHeader(headerArray);
		    
		    // write the customer lists
		    DecimalFormat df = new DecimalFormat("0.00");
		    for (MiningResult result : results) {
		    	List<String> lineItems = new ArrayList<>();
		    	
		    	//Parameter columns
		    	for (String argName : argNames) {
		    		lineItems.add(df.format(Double.valueOf(result.getArgMap().get(argName))));
			    }
		    	
		    	//Soundness
		    	lineItems.add(result.getEvaluation().isSoundModel() ? "Yes" : "No");
		    	
		    	//Fitness, precision, F-score
		    	lineItems.add(df.format(result.getEvaluation().getFitness()));
		    	lineItems.add(df.format(result.getEvaluation().getPrecision()));
		    	lineItems.add(df.format(2*result.getEvaluation().getFitness()*result.getEvaluation().getPrecision()/
		    							(result.getEvaluation().getFitness() + result.getEvaluation().getPrecision())));
		    	
		    	//Size,CFC,Structuredness,Sequentiality,Separability,Run-time
		    	lineItems.add(df.format(result.getEvaluation().getSize()));
		    	lineItems.add(df.format(result.getEvaluation().getCFC()));
		    	lineItems.add(df.format(result.getEvaluation().getStructuredness()));
		    	lineItems.add(df.format(result.getEvaluation().getSequentiality()));
		    	lineItems.add(df.format(result.getEvaluation().getSeparability()));
		    	lineItems.add(df.format(result.getMiningRuntime()));
		    	
		    	listWriter.write(lineItems);
		    }
		        
		}
		finally {
		    if( listWriter != null ) {
		    	listWriter.close();
		    }
		}	
	}
	
	private String getArgString(SortedMap<String,String> argMap) {
		if (argMap == null) return "";
		
		String argsString = "";
		DecimalFormat df = new DecimalFormat("0.00");
		int i=0;
		for (String argkey : argMap.keySet()) {
			argsString += (df.format(Double.valueOf(argMap.get(argkey))) + (i==(argMap.size()-1) ? "" : "_"));
			i++;
		}
		return argsString;
	}
	
	protected MiningResult compareMiningResult(MiningResult currentRes, MiningResult newRes, ModelMetrics metrics, boolean considerSoundness) {
		if (currentRes == null) {
			return newRes;
		}
		else if (considerSoundness && !newRes.getEvaluation().isSoundModel()) {
			return currentRes;
		}
		else if (metrics == ModelMetrics.FSCORE) {
			if (newRes.getEvaluation().getFscore() > currentRes.getEvaluation().getFscore()) {
				return newRes;
			}
		}
		else if (metrics == ModelMetrics.PRECISION) {
			if (newRes.getEvaluation().getPrecision() > currentRes.getEvaluation().getPrecision()) {
				return newRes;
			}
		}
		else if (metrics == ModelMetrics.FITNESS) {
			if (newRes.getEvaluation().getFitness() > currentRes.getEvaluation().getFitness()) {
				return newRes;
			}
		}
		else if (metrics == ModelMetrics.FIT_AND_PRECISE) {
			if (newRes.getEvaluation().getFitness() >= currentRes.getEvaluation().getFitness() &&
					newRes.getEvaluation().getPrecision() > currentRes.getEvaluation().getPrecision()) {
				return newRes;
			}
		}
		
		return currentRes;
	}


}
