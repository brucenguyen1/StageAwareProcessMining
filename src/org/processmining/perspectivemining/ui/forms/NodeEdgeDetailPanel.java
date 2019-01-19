package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.data.statistics.HistogramDataset;
import org.processmining.perspectivemining.graph.annotation.GraphAnnotator;
import org.processmining.perspectivemining.graph.settings.WeightType;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class NodeEdgeDetailPanel extends JPanel  {
    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public NodeEdgeDetailPanel(String title, 
    							double[] weight1, double[] trend1, double[] seasonality1,
    							double[] weight2, double[] trend2, double[] seasonality2,
    							double[] diffWeight, double[] diffTrend, double[] diffSeasonality, 
    							long log1Start, long log2Start, long window, WeightType weightType) {
    	this.setLayout(new BorderLayout());
    	this.add(new JLabel("<html><h2>" + title, SwingConstants.CENTER), BorderLayout.NORTH);
    	
    	JTabbedPane tabbedPane = new JTabbedPane();
    	SummaryPanel summaryPanel = new SummaryPanel(title, weight1, weight2, diffWeight, weightType);
    	tabbedPane.addTab("Summary", summaryPanel);
    	
    	if (weight1 != null) {
    		JPanel timeseriesPanel11 = new TimeSeriesPanel(GraphAnnotator.getDisplayValues(weight1), log1Start, window);
    		tabbedPane.add("Graph 1 - Weight Series", timeseriesPanel11);
    		tabbedPane.add("Graph 1 - Weight Histogram", new HistogramPanel("", GraphAnnotator.getExistentValues(weight1)));
    	}
    	
    	if (weight2 != null) {
	    	JPanel timeseriesPanel21 = new TimeSeriesPanel(GraphAnnotator.getDisplayValues(weight2), log2Start, window);
	    	tabbedPane.add("Graph 2 - Weight Series", timeseriesPanel21);
	    	tabbedPane.add("Graph 2 - Weight Histogram", new HistogramPanel("", GraphAnnotator.getExistentValues(weight2)));
    	}
    	
    	if (diffWeight != null) {
    		long logStart = log1Start < log2Start ? log1Start : log2Start;
    		JPanel timeseriesPanel31 = new TimeSeriesPanel(GraphAnnotator.getDisplayValues(diffWeight), logStart, window);
    		JPanel histogramPanel1 = new HistogramPanel("", GraphAnnotator.getExistentValues(diffWeight));
    		tabbedPane.add("Weight Difference Series", timeseriesPanel31);
    		tabbedPane.add("Weight Difference Histogram", histogramPanel1);
    	}
        this.add(tabbedPane, BorderLayout.CENTER);
        this.invalidate();
    }

}