package org.processmining.perspectivemining.graph.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.model.ObjectSchema;
import org.processmining.perspectivemining.log.attribute.AttributeRow;
import org.processmining.perspectivemining.ui.controller.DifferentialSettingController;
import org.processmining.perspectivemining.ui.controller.PerspectiveSettingController;

/**
 * Represent configuration for creating a perspective
 * @author Bruce
 *
 */
public class PerspectiveSettingObject {
	private String name = "New Perspective";
	
	private ObjectSchema abstrationSchema = null;
	private ObjectSchema nodeType1 = null;
	private ObjectSchema nodeType2 = null;
	
	private GraphType graphType = GraphType.INTRA_FRAGMENT;
	private WeightType weightType = WeightType.FREQUENCY;
	private WeightValueType weightValueType = WeightValueType.FREQUENCY_RELATIVE;
	private DifferenceType diffType = DifferenceType.ABSOLUTE;
	private int lowNumberOfObservations = 20;
	private boolean dynamicView = false;
	private ObjectSchema fixedSchema = null;
	
	private StatisticalTestType statType = StatisticalTestType.PARAMETRIC;
	private long windowSize = 24*3600; //seconds (one day)
	private double significanceLevel = 0.05;
	private SamplingType sampleType = SamplingType.CASE_BASED;

	public PerspectiveSettingObject(String name) {
		this.name = name;
	}
	
	//Constructor with default values
	public PerspectiveSettingObject(PerspectiveInputObject input) throws Exception {
		this.abstrationSchema = new ObjectSchema (new ArrayList<>());
		this.nodeType1 = new ObjectSchema (new ArrayList<>());
		this.nodeType2 = new ObjectSchema (new ArrayList<>());
		this.fixedSchema = new ObjectSchema (new ArrayList<>());
	}
	
	public PerspectiveSettingObject(PerspectiveInputObject input1, PerspectiveInputObject input2) throws Exception {
		this.abstrationSchema = new ObjectSchema (new ArrayList<>());
		this.nodeType1 = new ObjectSchema (new ArrayList<>());
		this.nodeType2 = new ObjectSchema (new ArrayList<>());
		this.fixedSchema = new ObjectSchema (new ArrayList<>());
		List<AttributeRow> atts = getCommonAttributeList(input1, input2);
	}
	
	public PerspectiveSettingObject(PerspectiveSettingController controller) throws Exception {
		this.abstrationSchema = new ObjectSchema (new ArrayList<>());
		this.nodeType1 = new ObjectSchema (new ArrayList<>());
		this.nodeType2 = new ObjectSchema (new ArrayList<>());
		this.fixedSchema = new ObjectSchema (new ArrayList<>());
		
		this.nodeType1.getAttributes().addAll(controller.getPanel().getNode1Attributes());
		
		this.nodeType2.getAttributes().addAll(controller.getPanel().getNode2Attributes());
		
		this.abstrationSchema.getAttributes().clear();
		this.abstrationSchema.getAttributes().addAll(controller.getPanel().getAbsSchemaAttributes());
		
		this.weightType = controller.getPanel().getWeightType();
		
		this.weightValueType = controller.getPanel().getWeightValueType();
		
		this.diffType = controller.getPanel().getDifferenceType();
		
		this.graphType = controller.getPanel().getGraphType();
		
		this.statType = controller.getPanel().getStatisticalTestType();
		
		this.sampleType = controller.getPanel().getSamplingType();
		
		this.windowSize = controller.getPanel().getWindowSize();
		
		this.significanceLevel = controller.getPanel().getSignificanceLevel();
		
//		this.dynamicView = controller.getPanel().getDynamicView();
		
		this.lowNumberOfObservations = controller.getPanel().getLowNumberOfObservations();
	}
	
	public PerspectiveSettingObject(DifferentialSettingController controller) {
		this.abstrationSchema = new ObjectSchema (new ArrayList<>());
		this.nodeType1 = new ObjectSchema (new ArrayList<>());
		this.nodeType2 = new ObjectSchema (new ArrayList<>());
		this.fixedSchema = new ObjectSchema (new ArrayList<>());
		
		this.nodeType1.getAttributes().addAll(controller.getPanel().getNode1Attributes());
		
		this.nodeType2.getAttributes().addAll(controller.getPanel().getNode2Attributes());
		
		this.abstrationSchema.getAttributes().clear();
		this.abstrationSchema.getAttributes().addAll(controller.getPanel().getAbsSchemaAttributes());
		
		this.weightType = controller.getPanel().getWeightType();
		
		this.weightValueType = controller.getPanel().getWeightValueType();
		
		this.diffType = controller.getPanel().getDifferenceType();
		
		this.graphType = controller.getPanel().getGraphType();
		
		this.statType = controller.getPanel().getStatisticalTestType();
		
		this.sampleType = controller.getPanel().getSamplingType();
		
		this.windowSize = controller.getPanel().getWindowSize();
		
		this.significanceLevel = controller.getPanel().getSignificanceLevel();
		
//		this.dynamicView = controller.getPanel().getDynamicView();
		
		this.lowNumberOfObservations = controller.getPanel().getLowNumberOfObservations();
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public ObjectSchema getNodeSchema1() {
		return nodeType1;
	}
	
	public void setNodeSchema1(ObjectSchema nodeType) {
		nodeType1 = nodeType;
	}
	
	public ObjectSchema getNodeSchema2() {
		return nodeType2;
	}
	
	public void setNodeSchema2(ObjectSchema nodeType) {
		nodeType2 = nodeType;
	}
	
	public ObjectSchema getAbstractionSchema() {
		return abstrationSchema;
	}
	
	public void setAbstractionSchema(ObjectSchema newSchema) {
		this.abstrationSchema = newSchema;
	}
	
	public WeightType getWeightType() {
		return this.weightType;
	}
	
	public void setWeightType(WeightType weightType) {
		this.weightType = weightType;
	}
	
	public WeightValueType getWeightValueType() {
		return this.weightValueType;
	}
	
	public void setWeightValueType(WeightValueType weightValueType) {
		this.weightValueType = weightValueType;
	}
	
	public GraphType getGraphType() {
		return this.graphType;
	}
	
	public void setGraphType(GraphType graphType) {
		this.graphType = graphType;
	}
	
	public int getLowNumberOfObservations() {
		return this.lowNumberOfObservations;
	}
	
	public void setLowNumberOfObservations(int lowNumber) {
		this.lowNumberOfObservations = lowNumber;
	}
	
	public ObjectSchema getFixedSchema() {
		return this.fixedSchema;
	}
	
	public double getSignificanceLevel() {
		return this.significanceLevel;
	}
	
	public void setSignificanceLevel(double significanceLevel) {
		this.significanceLevel = significanceLevel;
	}
	
	public DifferenceType getDifferenceType() {
		return this.diffType;
	}
	
	public void setDifferenceType(DifferenceType visType) {
		this.diffType = visType;
	}
	
	public StatisticalTestType getStatisticalTestType() {
		return this.statType;
	}
	
	public void setStatisticalTestType(StatisticalTestType statType) {
		this.statType = statType;
	}
	

	/*
	 * window is seconds
	 */
	public long getWindowSize() {
		return this.windowSize;
	}
	
	public void setWindowSize(long windowSize) {
		this.windowSize = windowSize;
	}
	
	public SamplingType getSamplingType() {
		return this.sampleType;
	}
	
	public void setSamplingType(SamplingType newSamplingType) {
		this.sampleType = newSamplingType;
	}
	
	public boolean isNode1Node2SameSchema() {
		if (this.getNodeSchema1().getAttributeNames().isEmpty() || this.getNodeSchema2().getAttributeNames().isEmpty()) {
			return false;
		}
		else {
			// Must compare using sets since the two schemas might be the same but just different order
			// of elements
			Set<String> node1Columns = new HashSet<String>(this.getNodeSchema1().getAttributeNames());
			Set<String> node2Columns = new HashSet<String>(this.getNodeSchema2().getAttributeNames());
			return (node1Columns.equals(node2Columns));
		}
	}
	
	public boolean isDynamicView() {
		return this.dynamicView;
	}
	
	public void setDynamicView(boolean dynamicView) {
		this.dynamicView = dynamicView;
	}
	
	public static List<AttributeRow> getCommonAttributeList(PerspectiveInputObject input1, PerspectiveInputObject input2) {
		if (input2 == null) {
			return input1.getAttributeList();
		}
		else {
			Set<String> attNames = new HashSet<>();
			for (AttributeRow att : input2.getAttributeList()) {
				attNames.add(att.getName());
			}
			List<AttributeRow> attList = new ArrayList<>();
			for (AttributeRow att : input1.getAttributeList()) {
				if (attNames.contains(att.getName())) {
					attList.add(att);
				}
			}
			return attList;
		}
		
		
	}
	
	/*
	 * Compare two PerspectiveSettingObjects
	 * return: 0 if no difference, 1 if difference requires to rerun everything
	 */
//	public int compareTo(PerspectiveSettingObject alternative) {
//		if (nodeType1.getAttributes() != alternative.getNodeType1().getAttributes()) {
//			return 1;
//		}
//		if (nodeType2.getAttributes() != alternative.getNodeType2().getAttributes()) {
//			return 1;
//		}
//		if (edgeType != alternative.getEdgeType()) {
//			return 1;
//		}
//		if (abstrationSchema != alternative.getAbstractionSchema()) {
//			return 1;
//		}
//		if (unitOrderType != alternative.getDirectFollowType()) {
//			return 1;
//		}
//		if (weightType != alternative.getWeightType()) {
//			return 1;		
//		}
//		
//		return 0;
//	}
	
}
