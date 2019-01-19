package org.processmining.perspectivemining.utils;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.processmining.perspectivemining.graph.annotation.GraphAnnotator;
import org.processmining.perspectivemining.graph.settings.SamplingType;
import org.processmining.perspectivemining.graph.settings.StatisticalTestType;
import org.processmining.perspectivemining.graph.settings.WeightType;

import model.graph.Edge;
import model.graph.Node;
import model.graph.impl.DirectedGraphInstance;

public class StatisticsUtils {

	public static void highlightDifferences(DirectedGraphInstance visGraphModel, WeightType weightType, SamplingType sampleType, 
											StatisticalTestType statType, double alpha) throws Exception {

		if (weightType == WeightType.FREQUENCY) {
			analyzeNodes(visGraphModel, sampleType, statType, alpha);
		}
		analyzeEdges(visGraphModel, sampleType, statType, alpha);

	}
	
	/**
	 * This class analyzes the annotations in each node, performa a statistical
	 * test, and based on the test decides whether to highlight the node or not
	 * (depending on the effect size value (d)
	 * 
	 * @param graph
	 * @param results
	 * @param comparator
	 * @param alpha
	 * @param nodes
	 * @return the same Dot graph that was provided as input
	 * @throws Exception 
	 */
	private static void analyzeNodes(DirectedGraphInstance visGraphModel, SamplingType sampleType, StatisticalTestType statType, double alpha) throws Exception {
		int maxGraph1NodeOccurs = 0;
		int maxGraph2NodeOccurs = 0;
		
		for (Node node : (Collection<Node>)(Collection<?>)visGraphModel.getNodes()) {
			// Strong occurrence in both graph 1 and graph 2
			if (visGraphModel.getNodeOccurrences().get(node) == 3) {
				boolean testResult = false;
				if (sampleType == SamplingType.WINDOW_BASED) {
					List<double[]> pairedValues = GraphAnnotator.getPairedValues(visGraphModel.getGraph1NodeWeights().get(node),
																				visGraphModel.getGraph2NodeWeights().get(node));
					double[] values1 = GraphAnnotator.getExistentValues(pairedValues.get(0));
					double[] values2 = GraphAnnotator.getExistentValues(pairedValues.get(1));
					
					if (statType == StatisticalTestType.PARAMETRIC) {
						testResult = twoSampleDependentParamTest(values1, values2, alpha);
					}
					else {
						testResult = twoSampleDependentNonParamTest(values1, values2, alpha);
					}
					
					if (testResult) {
						double d = commonLangEffectSize(new DescriptiveStatistics(values1), new DescriptiveStatistics(values2));
						GraphVisualizer.highlightDifferentialNode(node, d);
					}
					else {
						GraphVisualizer.highlightSimilarNode(node);
					}
				}
				else { //Case-Based
					DescriptiveStatistics values1=null, values2=null;
					values1 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(visGraphModel.getGraph1NodeWeights().get(node)));
					values2 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(visGraphModel.getGraph2NodeWeights().get(node)));
					if (statType == StatisticalTestType.PARAMETRIC) {
						testResult = twoSampleIndependentParamTest(values1, values2, alpha);
					}
					else {
						testResult = twoSampleIndependentNonParamTest(values1, values2, alpha);
					}
					
					if (testResult) {
						double d = commonLangEffectSize(values1, values2);
						GraphVisualizer.highlightDifferentialNode(node, d);
					}
					else {
						GraphVisualizer.highlightSimilarNode(node);
					}
				}				
			}
			// Strong occurrence in graph 1 but no or low-frequent occurrence in graph 2
			else if (visGraphModel.getNodeOccurrences().get(node) == 1) { 
				maxGraph1NodeOccurs += GraphAnnotator.getExistentValues(visGraphModel.getGraph1NodeWeights().get(node)).length;
			}
			// No or low-frequent occurrence in graph 1 but strong occurrence in graph 2
			else if (visGraphModel.getNodeOccurrences().get(node) == 2) { 
				maxGraph2NodeOccurs += GraphAnnotator.getExistentValues(visGraphModel.getGraph2NodeWeights().get(node)).length;
			}
			// No occurrence or low-frequent occurrence in both graph 1 or graph 2
			else { 
				GraphVisualizer.highlightLowFrequentNode(node);
			}
		}
		
		for (Node node : (Collection<Node>)(Collection<?>)visGraphModel.getNodes()) {
			if (visGraphModel.getNodeOccurrences().get(node) == 1) { 
				double occurs = GraphAnnotator.getExistentValues(visGraphModel.getGraph1NodeWeights().get(node)).length;
				GraphVisualizer.highlightNode(node, occurs/maxGraph1NodeOccurs, GraphVisualizer.COLOR_SCALES, GraphVisualizer.GRAPH1_COLOR);
			}
			else if (visGraphModel.getNodeOccurrences().get(node) == 2) { 
				double occurs = GraphAnnotator.getExistentValues(visGraphModel.getGraph2NodeWeights().get(node)).length;
				GraphVisualizer.highlightNode(node, occurs/maxGraph2NodeOccurs, GraphVisualizer.COLOR_SCALES, GraphVisualizer.GRAPH2_COLOR);
			}
		}
	}
	
	private static void analyzeEdges(DirectedGraphInstance visGraphModel, SamplingType sampleType, StatisticalTestType statType, double alpha) throws Exception {
		int maxGraph1EdgeOccurs = 0;
		int maxGraph2EdgeOccurs = 0;
		
		for (Edge edge : (Collection<Edge>)(Collection<?>)visGraphModel.getEdges()) {
			if (visGraphModel.getEdgeOccurrences().get(edge) == 3) {
				boolean testResult = false;
				if (sampleType == SamplingType.WINDOW_BASED) {
					List<double[]> pairedValues = GraphAnnotator.getPairedValues(visGraphModel.getGraph1EdgeWeights().get(edge),
																				visGraphModel.getGraph2EdgeWeights().get(edge));
					double[] values1 = GraphAnnotator.getExistentValues(pairedValues.get(0));
					double[] values2 = GraphAnnotator.getExistentValues(pairedValues.get(1));
					
					if (statType == StatisticalTestType.PARAMETRIC) {
						testResult = twoSampleDependentParamTest(values1, values2, alpha);
					}
					else {
						testResult = twoSampleDependentNonParamTest(values1, values2, alpha);
					}
					
					if (testResult) {
						double d = commonLangEffectSize(new DescriptiveStatistics(values1), new DescriptiveStatistics(values2));
						GraphVisualizer.highlightDifferentialEdge(edge, d);
					}
					else {
						GraphVisualizer.highlightSimilarEdge(edge);
					}
				}
				else { //Case-Based
					DescriptiveStatistics values1=null, values2=null;
					values1 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(visGraphModel.getGraph1EdgeWeights().get(edge)));
					values2 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(visGraphModel.getGraph2EdgeWeights().get(edge)));
					if (statType == StatisticalTestType.PARAMETRIC) {
						testResult = twoSampleIndependentParamTest(values1, values2, alpha);
					}
					else {
						testResult = twoSampleIndependentNonParamTest(values1, values2, alpha);
					}
					
					if (testResult) {
						double d = commonLangEffectSize(values1, values2);
						GraphVisualizer.highlightDifferentialEdge(edge, d);
					}
					else {
						GraphVisualizer.highlightSimilarEdge(edge);
					}
				}
			}
			else if (visGraphModel.getEdgeOccurrences().get(edge) == 1) { //only graph 1
				maxGraph1EdgeOccurs += GraphAnnotator.getExistentValues(visGraphModel.getGraph1EdgeWeights().get(edge)).length;
			} else if (visGraphModel.getEdgeOccurrences().get(edge) == 2) { // only graph 2
				maxGraph2EdgeOccurs += GraphAnnotator.getExistentValues(visGraphModel.getGraph2EdgeWeights().get(edge)).length;
			}
			else { // neither in graph 1 or graph 2 due to low frequency 
				GraphVisualizer.highlightLowFrequentEdge(edge);
			}
		}
		
		for (Edge edge : (Collection<Edge>)(Collection<?>)visGraphModel.getEdges()) {
			if (visGraphModel.getEdgeOccurrences().get(edge) == 1) { 
				double occurs = GraphAnnotator.getExistentValues(visGraphModel.getGraph1EdgeWeights().get(edge)).length;
				GraphVisualizer.highlightEdge(edge, occurs/maxGraph1EdgeOccurs, GraphVisualizer.COLOR_SCALES, GraphVisualizer.GRAPH1_COLOR);
			}
			else if (visGraphModel.getEdgeOccurrences().get(edge) == 2) { 
				double occurs = GraphAnnotator.getExistentValues(visGraphModel.getGraph2EdgeWeights().get(edge)).length;
				GraphVisualizer.highlightEdge(edge, occurs/maxGraph2EdgeOccurs, GraphVisualizer.COLOR_SCALES, GraphVisualizer.GRAPH2_COLOR);
			}
		}
	}

	
	/**
	 * The T test (Parametric): this is Welch's t-test
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	public static boolean twoSampleIndependentParamTest(DescriptiveStatistics a, DescriptiveStatistics b, double alpha) {
		//if there is no variance, the means are different
		if (a == null || b == null)
			return true;

		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		double t = (a.getMean() - b.getMean()) / Math.sqrt((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()));
		double degrees_of_freedom = Math.rint(Math.pow((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()), 2)
				/ ((Math.pow(a.getVariance(), 2) / (Math.pow(a.getN(), 2) * (a.getN() - 1)))
						+ (Math.pow(b.getVariance(), 2) / (Math.pow(b.getN(), 2) * (b.getN() - 1)))));

		if (t > 0)
			t = 0 - Math.abs(t);

		TDistribution t_dist = new TDistribution(degrees_of_freedom);

		double p_value = t_dist.cumulativeProbability(t);

		if (p_value <= (alpha / 2)) //two tailed check
		{
			//System.out.println(p_value);
			return true;
		}

		else
			return false;
	}
	
	/**
	 * The Mann-Whitney U test (Non-Parametric)
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	private static boolean twoSampleIndependentNonParamTest(DescriptiveStatistics a, DescriptiveStatistics b, double alpha) {
		if (a == null || b == null)
			return true;

		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		MannWhitneyUTest test = new MannWhitneyUTest();
		double p = 1;
		try {
			p = test.mannWhitneyUTest(a.getValues(), b.getValues());
		} catch (NoDataException e) {
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (p <= alpha)
			return true;
		return false;

	}
	
	/**
	 * this method returns the effect size in terms of Cohen's d value:
	 * Breakpoints are at d = 0.2 (small), 0.5 (medium) and 0.8 (large)
	 * 
	 */
//	public static double cohenEffectSize(DescriptiveStatistics a, DescriptiveStatistics b) {
//
//		//if there is no variance, the means are different
//		if (a == null || b == null)
//			return 0;
//
//		//if there is no variance, don't even bother calculating stuff
//		if (a.getVariance() == 0 && b.getVariance() == 0) {
//			if (a.getMean() > b.getMean())
//				return +4.0;
//			else if (a.getMean() < b.getMean())
//				return -4.0;
//			else
//				return 0;
//		}
//
//		double pooledStdDev = Math
//				.sqrt(((a.getN() - 1) * a.getVariance() + (b.getN() - 1) * b.getVariance()) / (a.getN() + b.getN()));
//
//		return (a.getMean() - b.getMean()) / pooledStdDev;
//	}
	
	public static double commonLangEffectSize(DescriptiveStatistics a, DescriptiveStatistics b) {
		//if there is no variance, the means are different
		if (a == null || b == null)
			return 0;

		//if there is no variance, so mean is constant
		//just compare mean to know the sign, it always happens a>b or a<b
		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() > b.getMean()) 
				return 1.0;
			else if (a.getMean() < b.getMean())
				return -1.0;
			else
				return 0;
		}
		
		double mean = Math.abs(a.getMean() - b.getMean());
		double sd = Math.sqrt(a.getVariance() + b.getVariance()); 
		NormalDistribution dist = new NormalDistribution(mean, sd);
		if (a.getMean() > b.getMean()) {
			return 1-dist.cumulativeProbability(0); // if a.getMean > b.getMean, the more positive value, the more a >> b
		}
		else {
			return -(1-dist.cumulativeProbability(0)); // if a.getMean < b.getMean, the more negative value, the more b >> a (or a << b)
		}
	}
	
	
	/**
	 * The T test (Parametric): Student's t-test
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
//	public static boolean oneSampleParametricTest(DescriptiveStatistics a, double alpha) {
//		//if there is no variance, the means are different
//		if (a == null)
//			return true;
//
//		if (a.getVariance() == 0) {
//			if (a.getMean() != 0)
//				return true;
//			else
//				return false;
//		}
//
//		TTest t_test = new TTest();
//		double t = t_test.t(0, a);
//		if (t > 0) t = 0 - Math.abs(t); //use directional test, i.e. t < 0 
//		TDistribution t_dist = new TDistribution(a.getN()-1); // generate a t-statistic distribution
//		double p_value = t_dist.cumulativeProbability(t); // P(x <= t)
//		
//		if (p_value <= (alpha / 200.0)) //two tailed check but check p_value with the one-sided value
//		{
//			//System.out.println(p_value);
//			return true;
//		}
//
//		else
//			return false;
//	}
	
	public static boolean twoSampleDependentParamTest(double[] a, double[] b, double alpha) {
		if (a == null || b == null)
			return true;

		DescriptiveStatistics aStat = new DescriptiveStatistics(a);
		DescriptiveStatistics bStat = new DescriptiveStatistics(b);
		if (aStat.getVariance() == 0 && bStat.getVariance() == 0) {
			if (aStat.getMean() != bStat.getMean())
				return true;
			else
				return false;
		}
		
		return TestUtils.pairedTTest(a, b, alpha);
	}
	
//	public static double oneSampleDependentParamEffectSize(double[] a, double[] b) {
//		if (a.length == 0) return 0;
//		TTest t_test = new TTest();
//		double t = t_test.pairedT(a, b);;
//		return 1.0*t/Math.sqrt(a.length);
//	}
	
	/*
	 * Wilconxon
	 */
	public static boolean twoSampleDependentNonParamTest(double[] a, double[] b, double alpha) {
		if (a == null || b == null)
			return true;

		DescriptiveStatistics aStat = new DescriptiveStatistics(a);
		DescriptiveStatistics bStat = new DescriptiveStatistics(b);
		if (aStat.getVariance() == 0 && bStat.getVariance() == 0) {
			if (aStat.getMean() != bStat.getMean())
				return true;
			else
				return false;
		}
		
		WilcoxonSignedRankTest test = new WilcoxonSignedRankTest();
		double p = test.wilcoxonSignedRankTest(a, b, false);
		if (p <= alpha) {
			return true;
		}
		return false;
	}

//	public static double twoSampleDependentNonParamEffectSize(double[] a, double[] b) {
//		WilcoxonSignedRankTest test = new WilcoxonSignedRankTest();
//		double w_stat = test.wilcoxonSignedRank(a, b);
//		return 1.0*w_stat/Math.sqrt(a.length*2);
//	}

	

}
