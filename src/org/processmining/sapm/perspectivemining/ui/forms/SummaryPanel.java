package org.processmining.sapm.perspectivemining.ui.forms;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.processmining.sapm.perspectivemining.graph.annotation.GraphAnnotator;
import org.processmining.sapm.perspectivemining.graph.settings.WeightType;
import org.processmining.sapm.perspectivemining.utils.DateUtils;
import org.processmining.sapm.perspectivemining.utils.StatisticsUtils;

import layout.TableLayout;

public class SummaryPanel extends JPanel {
	
	public SummaryPanel(String objectTitle, double[] weight1, double[] weight2, double[] diffWeight, WeightType weightType) {
		double panelLayout[][] = {{TableLayout.FILL},{25, 25, TableLayout.FILL}};
    	this.setLayout(new TableLayout(panelLayout));
    	
    	DescriptiveStatistics stats1 = null;
    	if (weight1 != null) {
    		stats1 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(weight1));
    	}
    	
		DescriptiveStatistics stats2 = null;
		if (weight2 != null) {
			stats2 = new DescriptiveStatistics(GraphAnnotator.getExistentValues(weight2));
		}
		
		DescriptiveStatistics diffStat = null;
		if (diffWeight != null) {
			diffStat = new DescriptiveStatistics(GraphAnnotator.getExistentValues(diffWeight));
		}
		
		String[] columnNames = {"Graph", "Min", "Mean", "Max", "Variance", "Std. Dev."};
		Object[][] data = new Object[3][6];
		if (weightType == WeightType.FREQUENCY) {
			data[0][0] = "Graph 1 Weight";
			data[0][1] = stats1!=null ? String.format(java.util.Locale.US,"%.3f", stats1.getMin()) : "NA";
			data[0][2] = stats1!=null ? String.format(java.util.Locale.US,"%.3f", stats1.getMean()) : "NA";
			data[0][3] = stats1!=null ? String.format(java.util.Locale.US,"%.3f", stats1.getMax()) : "NA";
			data[0][4] = stats1!=null ? String.format(java.util.Locale.US,"%.3f", stats1.getVariance()) : "NA";
			data[0][5] = stats1!=null ? String.format(java.util.Locale.US,"%.3f", stats1.getStandardDeviation()) : "NA";
				    	 
			data[1][0] = "Graph 2 Weight";
			data[1][1] = stats2!=null ? String.format(java.util.Locale.US,"%.3f", stats2.getMin()) : "NA"; 
			data[1][2] = stats2!=null ? String.format(java.util.Locale.US,"%.3f", stats2.getMean()) : "NA"; 
			data[1][3] = stats2!=null ? String.format(java.util.Locale.US,"%.3f", stats2.getMax()) : "NA";
			data[1][4] = stats2!=null ? String.format(java.util.Locale.US,"%.3f", stats2.getVariance()) : "NA"; 
			data[1][5] = stats2!=null ? String.format(java.util.Locale.US,"%.3f", stats2.getStandardDeviation()) : "NA";
			
			data[2][0] = "Diff. Graph Weight";
			data[2][1] = diffStat!=null ? String.format(java.util.Locale.US,"%.3f", diffStat.getMin()) : "NA"; 
			data[2][2] = diffStat!=null ? String.format(java.util.Locale.US,"%.3f", diffStat.getMean()) : "NA";
			data[2][3] = diffStat!=null ? String.format(java.util.Locale.US,"%.3f", diffStat.getMax()) : "NA"; 
			data[2][4] = diffStat!=null ? String.format(java.util.Locale.US,"%.3f", diffStat.getVariance()) : "NA"; 
			data[2][5] = diffStat!=null ? String.format(java.util.Locale.US,"%.3f", diffStat.getStandardDeviation()) : "NA";
		}
		else {
			data[0][0] = "Graph 1 Weight";
			data[0][1] = stats1!=null ? DateUtils.ConvertSecondToHHMMString((long)stats1.getMin()) : "NA";
			data[0][2] = stats1!=null ? DateUtils.ConvertSecondToHHMMString((long)stats1.getMean()) : "NA";
			data[0][3] = stats1!=null ? DateUtils.ConvertSecondToHHMMString((long)stats1.getMax()) : "NA";
			data[0][4] = stats1!=null ? DateUtils.ConvertSecondToHHMMString((long)stats1.getVariance()) : "NA";
			data[0][5] = stats1!=null ? DateUtils.ConvertSecondToHHMMString((long)stats1.getStandardDeviation()) : "NA";
				    	 
			data[1][0] = "Graph 2 Weight";
			data[1][1] = stats2!=null ? DateUtils.ConvertSecondToHHMMString((long)stats2.getMin()) : "NA"; 
			data[1][2] = stats2!=null ? DateUtils.ConvertSecondToHHMMString((long)stats2.getMean()) : "NA"; 
			data[1][3] = stats2!=null ? DateUtils.ConvertSecondToHHMMString((long)stats2.getMax()) : "NA";
			data[1][4] = stats2!=null ? DateUtils.ConvertSecondToHHMMString((long)stats2.getVariance()) : "NA"; 
			data[1][5] = stats2!=null ? DateUtils.ConvertSecondToHHMMString((long)stats2.getStandardDeviation()) : "NA";
			
			data[2][0] = "Diff. Graph Weight";
			data[2][1] = diffStat!=null ? DateUtils.ConvertSecondToHHMMString((long)diffStat.getMin()) : "NA"; 
			data[2][2] = diffStat!=null ? DateUtils.ConvertSecondToHHMMString((long)diffStat.getMean()) : "NA";
			data[2][3] = diffStat!=null ? DateUtils.ConvertSecondToHHMMString((long)diffStat.getMax()) : "NA"; 
			data[2][4] = diffStat!=null ? DateUtils.ConvertSecondToHHMMString((long)diffStat.getVariance()) : "NA"; 
			data[2][5] = diffStat!=null ? DateUtils.ConvertSecondToHHMMString((long)diffStat.getStandardDeviation()) : "NA";;
			
		}
		JTable table = new JTable(data, columnNames);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredSize(new Dimension(700, 100));
		JScrollPane scrollPaneTable = new  JScrollPane(table);
		
		double effectSize = StatisticsUtils.commonLangEffectSize(stats1, stats2);
		JLabel effectSize2SampleLabel = new JLabel("<html><h2>Common Language Effect Size: " + 
													String.format(java.util.Locale.US,"%.1f", Math.abs(effectSize)*100) + 
													"% chance that " +
													(effectSize > 0 ? "Log 1's weight &gt Log 2's weight" : "Log 1's weight &lt Log 2's weight") + 
													"</h2></html>");
		this.add(effectSize2SampleLabel, "0,0");
//		
//		if (diffWeight != null) {
//			double effectSizeDiff = StatisticsUtils.getEffectSizeOneSampleTest(diffStat);
//			JLabel effectSizeDiffLabel = new JLabel("<html><h2>Effect Size (diff. population, 1-sample t-test): " + 
//										String.format(java.util.Locale.US,"%.2f", effectSizeDiff) + "</h2></html>");
//			this.add(effectSizeDiffLabel, "0,1");
//		}
		
		this.add(scrollPaneTable, "0,2");
		
	}
}
