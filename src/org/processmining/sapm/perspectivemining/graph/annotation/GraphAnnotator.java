package org.processmining.sapm.perspectivemining.graph.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.processmining.sapm.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.sapm.perspectivemining.graph.model.AbstractFragment;
import org.processmining.sapm.perspectivemining.graph.model.EventRow;
import org.processmining.sapm.perspectivemining.graph.model.Fragment;
import org.processmining.sapm.perspectivemining.graph.model.PerspectiveGraph;
import org.processmining.sapm.perspectivemining.graph.model.ProcessAbstraction;
import org.processmining.sapm.perspectivemining.graph.model.ProcessObject;
import org.processmining.sapm.perspectivemining.graph.settings.PerspectiveSettingObject;
import org.processmining.sapm.perspectivemining.graph.settings.SamplingType;
import org.processmining.sapm.perspectivemining.graph.settings.WeightValueType;

public abstract class GraphAnnotator {
	
	public static double NO_EXIST = Double.NaN;
	
	public abstract void annotateGraph(PerspectiveGraph pGraph, ProcessAbstraction processAbs, PerspectiveInputObject input, PerspectiveSettingObject setting) throws Exception;
	
	public abstract EventRow selectNode1Event(AbstractFragment fragment);
	
	public abstract EventRow selectNode2Event(AbstractFragment fragment);
	
	protected void updateNodeAnnotation(Node node, PGAnnotation graphAnnotation, String annotationType, double value) throws Exception {
		Annotation annotation = graphAnnotation.getAnnotation(node);
		if (!annotation.containsKey(annotationType))
			annotation.addElement(new AnnotationElement(annotationType, AnnotationElement.Type.Number));
		annotation.getElement(annotationType).addValue(value);
	}
	
	protected void updateEdgeAnnotation(Edge edge, PGAnnotation graphAnnotation, String annotationType, double value) throws Exception {
		Annotation annotation = graphAnnotation.getAnnotation(edge);
		if (!annotation.containsKey(annotationType))
			annotation.addElement(new AnnotationElement(annotationType, AnnotationElement.Type.Number));
		
		annotation.getElement(annotationType).addValue(value);
	}
	
	/*
	 * Update node and edge annotations in one observation
	 * Thus, just call this method once for every observation
	 */
	protected void updateAnnotationsFromObservation(PerspectiveGraph pGraph, 
											Map<Node,Integer> nodeObservations, 
											Map<Edge,Integer> edgeObservations,
											Map<Edge, DescriptiveStatistics> edgeTimeObservations) throws Exception {
		
		PGAnnotation apg = pGraph.getPGAnnotation();
		
		if (nodeObservations != null) {
			int totalNodeOccurs = this.sumSetValues(nodeObservations.values());
	 		for (Node node: pGraph.getNodes()) {
	 			if (nodeObservations.keySet().contains(node)) {
	 				updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), 1.0*nodeObservations.get(node));
	 				updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_RELATIVE.name(), 1.0*nodeObservations.get(node)/totalNodeOccurs);
	 			}
	 			else { // non-existent
	 				if (pGraph.getSetting().getSamplingType() == SamplingType.CASE_BASED) {
	 					updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), 0);
		 				updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_RELATIVE.name(), 0);
	 				}
	 				else {
	 					updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), GraphAnnotator.NO_EXIST);
		 				updateNodeAnnotation(node, apg, WeightValueType.FREQUENCY_RELATIVE.name(), GraphAnnotator.NO_EXIST);
	 				}
	 			}
	 		}
		}
 		
		if (edgeObservations != null) {
			int totalEdgeOccurs = this.sumSetValues(edgeObservations.values());
	 		for (Edge edge: pGraph.getEdges()) { 
	 			if (edgeObservations.keySet().contains(edge)) {
	 				updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), 1.0*edgeObservations.get(edge));
	 				updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_RELATIVE.name(), 1.0*edgeObservations.get(edge)/totalEdgeOccurs);
	 			}
	 			else { // non-existent
	 				if (pGraph.getSetting().getSamplingType() == SamplingType.CASE_BASED) {
	 					updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), 0);
	 					updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_RELATIVE.name(), 0);
	 				}
	 				else {
	 					updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_ABSOLUTE.name(), GraphAnnotator.NO_EXIST);
	 					updateEdgeAnnotation(edge, apg, WeightValueType.FREQUENCY_RELATIVE.name(), GraphAnnotator.NO_EXIST);
	 				}
	 			}
	 		}
		}
		
		if (edgeTimeObservations != null) {
	 		for (Edge edge: pGraph.getEdges()) { 
	 			if (edgeTimeObservations.keySet().contains(edge)) {
	 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEAN.name(), edgeTimeObservations.get(edge).getMean());
	 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEDIAN.name(), edgeTimeObservations.get(edge).getPercentile(50));
	 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MIN.name(), edgeTimeObservations.get(edge).getMin());
	 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MAX.name(), edgeTimeObservations.get(edge).getMax());
	 			}
	 			else { // non-existent
	 				if (pGraph.getSetting().getSamplingType() == SamplingType.CASE_BASED) {
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEAN.name(), 0);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEDIAN.name(), 0);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MIN.name(), 0);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MAX.name(), 0);	 					
	 				}
	 				else {
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEAN.name(), GraphAnnotator.NO_EXIST);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MEDIAN.name(), GraphAnnotator.NO_EXIST);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MIN.name(), GraphAnnotator.NO_EXIST);
		 				updateEdgeAnnotation(edge, apg, WeightValueType.DURATION_MAX.name(), GraphAnnotator.NO_EXIST);
	 				}
	 			}
	 		}
		}
	}
	
	private int sumSetValues(Collection<Integer> values) {
		int sum = 0;
		for (Integer value : values) {
			sum += value;
		}
		return sum;
	}
	
	/*
	 * Combine all fragments of the same object into one big fragment 
	 */
	protected Map<ProcessObject, Fragment> getSameObjectFragments(List<Fragment> fragments) {
		Map<ProcessObject, Fragment> mapObjectFragment = new HashMap<>();
		for (Fragment fragment : fragments) {
			ProcessObject object = fragment.getProcessObject();
			if (!mapObjectFragment.containsKey(object)) mapObjectFragment.put(object, new Fragment(object));
			mapObjectFragment.get(object).addAll(fragment);
		}
		return mapObjectFragment;
	}
	
	protected long calculateDuration(EventRow source, EventRow target) throws Exception {
		if (target.getTime() < source.getTime()) {
			return 0;
		}
		else {
			return (target.getTime() - source.getTime());
		}
	}
//	
//	public static int countComparableValues(double[] values) {
//		int count = 0;
//		for (int i=0;i<values.length;i++) {
//			if (!Double.isNaN(values[i])) {
//				count++;
//			}
//		}
//		return count;
//	}
	
	public static double[] getExistentValues(double[] values) {
		List<Double> valueArray = new ArrayList<>();
		for (int i=0;i<values.length;i++) {
			if (!Double.isNaN(values[i])) valueArray.add(values[i]);
		}
		Double[] diffValues = valueArray.toArray(new Double[valueArray.size()]);
		double[] doubleValues = new double[diffValues.length];
		for (int i=0;i<diffValues.length;i++) {
			doubleValues[i] = diffValues[i];
		}
		return doubleValues;
	}
	
	public static double[] getDisplayValues(double[] values) {
		double[] displayValues = new double[values.length];
		for (int i=0;i<values.length;i++) {
			displayValues[i] = (Double.isNaN(values[i])) ? 0 : values[i];
		}
		return displayValues;
	}
	
	/*
	 * diffWeights contain Double.NaN to mark non-existent samples
	 * The comparison must remove this value prior comparison
	 * The time series display must turn this value to zeros before display
	 */
	public static double[] getPairedDiff(double[] values1, double[] values2) {
		int compareCount = Math.min(values1.length, values2.length);
		
		double[] diffValues = new double[compareCount];
		for (int i=0;i<compareCount;i++) {
			if (Double.isNaN(values1[i]) && Double.isNaN(values2[i])) {
				diffValues[i] = GraphAnnotator.NO_EXIST;
			}
			else {
				double value1 = !Double.isNaN(values1[i]) ? values1[i] : 0;
				double value2 = !Double.isNaN(values2[i]) ? values2[i] : 0;
				diffValues[i] = value1 - value2;
			}
		}
		return diffValues;
	}
	
	public static List<double[]> getPairedValues(double[] values1, double[] values2) {
		int compareCount = Math.min(values1.length, values2.length);
		double[] values12 = new double[compareCount];
		double[] values22 = new double[compareCount];
		for (int i=0;i<compareCount;i++) {
			if (Double.isNaN(values1[i]) && Double.isNaN(values2[i])) {
				values12[i] = GraphAnnotator.NO_EXIST;
				values22[i] = GraphAnnotator.NO_EXIST;
			}
			else {
				values12[i] = !Double.isNaN(values1[i]) ? values1[i] : 0;
				values22[i] = !Double.isNaN(values2[i]) ? values2[i] : 0;
			}
		}
		
		List<double[]> result = new ArrayList<>();
		result.add(values12);
		result.add(values22);
		return result;
	}
	
}
