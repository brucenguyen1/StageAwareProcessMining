package org.processmining.sapm.processdiscovery.evaluation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.multietc.automaton.Automaton;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.Woflan2;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
//import org.processmining.plugins.uma.Uma_UnfoldPN;
//import org.processmining.plugins.uma.Uma_UnfoldPN.UnfoldingConfiguration;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.sapm.processdiscovery.bpmn.metrics.ComplexityCalculator;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

public class Evaluator {
	public static Object[] measureFitnessPrecision(UIPluginContext context, XLog log, Petrinet net, 
						Marking initialMarking, Set<Marking> finalMarkings) throws Exception {
		Object[] result = new Object[5];

		context.clear();
		context.addConnection(new InitialMarkingConnection(net, initialMarking));
		for (Marking m : finalMarkings) {
			context.addConnection(new FinalMarkingConnection(net, m));
		}
		EvClassLogPetrinetConnection con = PNetReplayerParamSetting.createConnection(log, net);
		context.addConnection(con);
		
		// Align Petrinet with the log
		PetrinetReplayerWithILP algo = new PetrinetReplayerWithILP();
		TransEvClassMapping mapping = (TransEvClassMapping)con.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
		PNLogReplayer replayer = new PNLogReplayer();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		PNRepResult replayResult=null;
		CostBasedCompleteParam alignParams = PNetReplayerParamSetting.constructReplayParameter(context, log, net, mapping);
		try {
		    SimpleTimeLimiter timeout = new SimpleTimeLimiter(executor);
		    replayResult = timeout.callWithTimeout(new Callable<PNRepResult>() {
		      @Override
		      public PNRepResult call() throws Exception {
		    	  return replayer.replayLog(context, net, log, mapping, algo, alignParams);
		      }
		    }, 200, TimeUnit.SECONDS, true);
		} catch (UncheckedTimeoutException e) {
			  System.err.println("Aligning log and model has been timed out!");
			  executor.shutdown();
			  return null;
		} finally {
			  executor.shutdown();
		}	
		
		// Extract fitness measure
		double fitness = getFitnessValue(replayResult);
		
		// Compute alignment-based precision
		double precision = -1;
		Automaton automaton = null;
		if (replayResult != null) { 
			MultiETCSettings etcsetting = new MultiETCSettings();
			etcsetting.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);
			etcsetting.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
			
			MultiETCPlugin etcplugin = new MultiETCPlugin();
			Object[] preResult = etcplugin.checkMultiETCAlign1(context, log, net, etcsetting, replayResult);
			if (preResult != null) {
				MultiETCResult etcRes = (MultiETCResult)preResult[0];
				precision = (Double)etcRes.getAttribute(MultiETCResult.PRECISION);
				automaton = (Automaton)preResult[1];
			}
		}
		
		result[0] = fitness;
		result[1] = precision;
		result[2] = automaton;
		result[3] = replayResult;
		result[4] = alignParams;
		return result;
	}

	public static PNRepResult align(UIPluginContext context, XLog log, Petrinet net, 
			Marking initialMarking, Set<Marking> finalMarkings) throws Exception {
//		try {
//			context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net);
//		} catch (ConnectionCannotBeObtained e) {
//			context.addConnection(new InitialMarkingConnection(net, initialMarking));
//		}
//		try {
//			context.getConnectionManager().getConnections(FinalMarkingConnection.class, context, net);
//		} catch (ConnectionCannotBeObtained e) {
//			for (Marking m : finalMarkings) {
//				context.addConnection(new FinalMarkingConnection(net, m));
//			}
//		}

		context.clear();
		context.addConnection(new InitialMarkingConnection(net, initialMarking));
		for (Marking m : finalMarkings) {
			context.addConnection(new FinalMarkingConnection(net, m));
		}
		EvClassLogPetrinetConnection con = PNetReplayerParamSetting.createConnection(log, net);
		context.addConnection(con);

		// Align Petrinet with the log
		PetrinetReplayerWithILP algo = new PetrinetReplayerWithILP();
		TransEvClassMapping mapping = (TransEvClassMapping)con.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
		PNLogReplayer replayer = new PNLogReplayer();
		//System.out.println("Start replaying log and model using alignment");
		PNRepResult replayResult = replayer.replayLog(context, net, log, mapping, algo, 
											PNetReplayerParamSetting.constructReplayParameter(context, log, net, mapping));
		return replayResult;
	}
	
	public static double measureFitness(PNRepResult replayResult) {
		// Extract fitness measure
		double fitness = -1;
		if (replayResult != null) {
			Map<String, Object> allInfo = replayResult.getInfo();
			if ((allInfo != null) && (allInfo.size() > 0)) {
				Set<Entry<String, Object>> entrySet = replayResult.getInfo().entrySet();
				for (Entry<String, Object> entry : entrySet) {
					if (entry.getKey().equals(PNRepResult.TRACEFITNESS)) {
						fitness = (Double) entry.getValue();
						break;
					}
				}
			}
		}
		return fitness;
	}
	
	public static double measurePrecision(UIPluginContext context, XLog log, Petrinet net, PNRepResult replayResult) 
							throws ConnectionCannotBeObtained {
		double precision = -1;
		MultiETCSettings etcsetting = new MultiETCSettings();
		etcsetting.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);
		etcsetting.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
		
		MultiETCPlugin etcplugin = new MultiETCPlugin();
		Object[] preResult = etcplugin.checkMultiETCAlign1(context, log, net, etcsetting, replayResult);
		if (preResult != null) {
			MultiETCResult etcRes = (MultiETCResult)preResult[0];
			precision = (Double)etcRes.getAttribute(MultiETCResult.PRECISION);
		}		
		return precision;
	}


	
	/**
	 * 
	 * @param context
	 * @param log
	 * @param bpmn
	 * @return: 1st - Size, 2nd - CND, 3rd: Structuredness, 4th: Separability, 5th Sequentiality
	 */
	public static Double[] measureComplexity(BPMNDiagram bpmn) {
		// Compute complexity metrics
		ComplexityCalculator calc = new ComplexityCalculator();
		Map<String, String> complexityMap = calc.computeComplexity(bpmn, true, true, false, false, false, false, true, false, false);
		
		Double[] result = new Double[3];
		result[0] = complexityMap.get("Size").equals("n/a") ? Double.NaN : Double.valueOf(complexityMap.get("Size"));
		result[1] = complexityMap.get("CFC").equals("n/a") ? Double.NaN : Double.valueOf(complexityMap.get("CFC"));
		result[2] = complexityMap.get("Structuredness").equals("n/a") ? Double.NaN : Double.valueOf(complexityMap.get("Structuredness"));
		
		return result;
	}
	
	public static boolean checkSoundness_Woflan(UIPluginContext context, Petrinet net) throws ConnectionCannotBeObtained, Exception  {
		Woflan2 woflan = new Woflan2();
		WoflanDiagnosis diagnosis = woflan.diagnose(context, net);
		return diagnosis.isSound();
	}
	
    private static double getFitnessValue(PNRepResult pnRepResult) {
        int unreliable = 0;
        if(pnRepResult == null) return Double.NaN;
        for(SyncReplayResult srp : pnRepResult) {
            if(!srp.isReliable()) {
                unreliable += srp.getTraceIndex().size();
            }
        }
        if(unreliable > pnRepResult.size() / 2) {
            return Double.NaN;
        }else {
            return (Double) pnRepResult.getInfo().get(PNRepResult.TRACEFITNESS);
        }
    }
}

