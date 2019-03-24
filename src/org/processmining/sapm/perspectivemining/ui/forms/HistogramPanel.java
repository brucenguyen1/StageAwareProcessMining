package org.processmining.sapm.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

import flanagan.analysis.ProbabilityPlot;
import layout.TableLayout;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class HistogramPanel extends JPanel implements ActionListener {
	
	private JButton btnPlotSeries1 = null;
	private JComboBox<BinningRuleEnum> cboBinRule1 = null;
	JPanel panelMain = null;
	JPanel panelHistorgramChart = null;
	private double[] valueSeries;
	private String title = "";
	private BinningRuleEnum binRule1 = BinningRuleEnum.Sturges;
	private String ACTION_PLOT1 = "normal_probability_plot1";
	private String ACTION_BIN_RULE1 = "bin_rule1";
	
    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public HistogramPanel(String title, double[] valueSeries) {
    	this.title = title;
    	this.valueSeries = valueSeries;
    	initialize();
    	cboBinRule1.setSelectedIndex(0);
    }
    
    private void initialize() {
    	this.setLayout(new BorderLayout());
    	this.add(new JLabel("<html><h3>" + title, SwingConstants.CENTER), BorderLayout.NORTH);

    	// Panel 1
    	panelMain = new JPanel(new BorderLayout());
    	
    	double panelTopLayout[][] = {{TableLayout.FILL},{25, TableLayout.FILL}};
    	JPanel panelTop = new JPanel(new TableLayout(panelTopLayout));
    	
    	panelTop.add(new JLabel("Sample population (" + valueSeries.length + "): "),"0,0");
    	
    	JTextArea textField = new JTextArea();
    	textField.setEditable(false); // as before
    	textField.setBackground(null); // this is the same as a JLabel
    	textField.setBorder(null); // remove the border
    	textField.setText(this.getSeriesString(valueSeries));
    	textField.setRows(10);
    	textField.setColumns(15);
    	textField.setLineWrap(true);
    	JScrollPane scrollPaneText = new JScrollPane(textField);
    	panelTop.add(scrollPaneText, "0,1");
    	
//    	DescriptiveStatistics stats = new DescriptiveStatistics(this.valueSeries);
//		DescriptiveStatistics statsOther; 
//		double effectSize = 0;
//		if (this.otherSeries != null) {
//			statsOther = new DescriptiveStatistics(this.otherSeries);
//			effectSize = StatisticsUtils.getEffectSizeTwoSampleTest(stats, statsOther);
//		}
//    	String[] columnNames = {"Min", "Max", "Mean", "Variance", "Std. Dev.", "Effect Size"};
//		Object[][] data = {
//			    {	String.format(java.util.Locale.US,"%.2f", stats.getMin()), 
//			    	String.format(java.util.Locale.US,"%.2f", stats.getMax()), 
//			    	String.format(java.util.Locale.US,"%.2f", stats.getMean()), 
//			    	String.format(java.util.Locale.US,"%.2f", stats.getVariance()), 
//			    	String.format(java.util.Locale.US,"%.2f", stats.getStandardDeviation()),
//			    	String.format(java.util.Locale.US,"%.2f", effectSize)}
//			};
//		JTable table = new JTable(data, columnNames);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		table.setPreferredSize(new Dimension(500, 100));
//		JScrollPane scrollPaneTable = new  JScrollPane(table);
//		panelTop.add(scrollPaneTable, "0,2");
		
    	panelMain.add(panelTop, BorderLayout.NORTH);
    	
    	panelHistorgramChart = createChartPanel(this.createDataset(valueSeries, binRule1));
        panelMain.add(panelHistorgramChart, BorderLayout.CENTER);
        
        JPanel panelButtons = new JPanel(new FlowLayout());
        
        cboBinRule1 = new JComboBox<>();
//      cboBinRule1.setPreferredSize(new Dimension(300, 30));
        cboBinRule1.addItem(BinningRuleEnum.Sturges);
        cboBinRule1.addItem(BinningRuleEnum.Freedman_Diaconis);
		cboBinRule1.addItem(BinningRuleEnum.Rice);
		cboBinRule1.addItem(BinningRuleEnum.Scott);
		cboBinRule1.addItem(BinningRuleEnum.Square_root);
		cboBinRule1.setActionCommand(ACTION_BIN_RULE1);
		cboBinRule1.addActionListener(this);
		panelButtons.add(cboBinRule1);
      
        btnPlotSeries1 = new JButton("Normal Probability Plot...");
        btnPlotSeries1.setActionCommand(ACTION_PLOT1);
        btnPlotSeries1.addActionListener(this);
        panelButtons.add(btnPlotSeries1);
        
        panelMain.add(panelButtons, BorderLayout.SOUTH);
        
        this.add(panelMain, BorderLayout.CENTER);
    }
    
    private double[] calculateHistogram(double[] values, BinningRuleEnum binRule) {
    	double min = Double.MAX_VALUE;
    	double max = 0;
    	double numBins = 0;
    	for (int i=0;i<values.length;i++) {
    		if (values[i] < min) min = values[i];
    		if (values[i] > max) max = values[i];
    	}
    	
    	if (binRule.equals(BinningRuleEnum.Sturges)) {
    		numBins = Math.ceil(1 + 3.322*Math.log(values.length));
    	}
    	else if (binRule.equals(BinningRuleEnum.Freedman_Diaconis)) {
    		DescriptiveStatistics da = new DescriptiveStatistics(values);
        	double IQR = da.getPercentile(75) - da.getPercentile(25);
        	numBins = Math.ceil((max-min)/(2.0*IQR/Math.pow(values.length, 0.33333)));
    	}
    	else if (binRule.equals(BinningRuleEnum.Rice)) {
    		numBins = Math.ceil(2*Math.pow(values.length, 1.0/3.0));
    	}
    	else if (binRule.equals(BinningRuleEnum.Scott)) {
    		DescriptiveStatistics da = new DescriptiveStatistics(values);
    		double binWidth = 3.5*da.getStandardDeviation()/Math.pow(values.length, 1.0/3.0);
    		numBins = Math.ceil((max-min)/binWidth);
    	}
    	else if (binRule.equals(BinningRuleEnum.Square_root)) {
    		numBins = Math.sqrt(values.length);
    	}
    	
    	double[] result = new double[3];
    	result[0] = numBins;
    	result[1] = min;
    	result[2] = max;
    	
    	return result;
    }

    /**
     * Creates a sample {@link HistogramDataset}.
     *
     * @return the dataset.
     */
    private IntervalXYDataset createDataset(double[] values, BinningRuleEnum binRule) {
    	if (values != null) {
    		double[] params = calculateHistogram(values, binRule);
	        HistogramDataset dataset = new HistogramDataset();
	        dataset.addSeries("", values, (int)params[0], params[1], params[2]);
//	        dataset.addSeries("H1", values, 100, 2.0, 8.0);
	        return dataset;
    	}
    	else {
    		return null;
    	}
    }
    
    private static double[] getTestSeries() {
    	double[] values = new double[200];
        Random generator = new Random(12345678L);
        for (int i = 0; i < 200; i++) {
            values[i] = generator.nextGaussian() + 5;
        }
//        dataset.addSeries("H1", values, 100, 2.0, 8.0);
//        values = new double[1000];
//        for (int i = 0; i < 1000; i++) {
//            values[i] = generator.nextGaussian() + 7;
//        }
        
        return values;
    }
    
    private String getSeriesString(double[] values) {
    	if (values == null) return "";
    	
//    	final String html1 = "<html><body style='width:100%'>";
//    	final String html2 = "</body></html>";
//    	String series = html1;
    	String series = "";
    	
    	double[] copyvalues = Arrays.copyOf(values,values.length);
//    	Arrays.sort(copyvalues);
    	
    	int maxNumbers = copyvalues.length;
    	for (int i=0;i<maxNumbers;i++) {
    		if (i<=(maxNumbers-2)) {
    			series += String.format(java.util.Locale.US,"%.3f", copyvalues[i]) + ", ";
    		}
    		else {
    			series += String.format(java.util.Locale.US,"%.3f", copyvalues[i]);
    		}
    	}
//    	series += html2;
    	series += ".";
    	return series;
    }

    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset) {
        JFreeChart chart = ChartFactory.createHistogram(
            "",
            null,
            null,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        // flat bars look best...
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        return chart;
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public JPanel createChartPanel(IntervalXYDataset dataset) {
    	if (dataset != null) {
	        JFreeChart chart = createChart(dataset);
	        ChartPanel panel = new ChartPanel(chart);
	        panel.setMouseWheelEnabled(true);
	        panel.setPreferredSize(new java.awt.Dimension(500, 270));
	        return panel;
    	}
    	else {
    		return new JPanel();
    	}
    }

    /**
     * The starting point for the demo.
     *
     * @param args  ignored.
     *
     * @throws IOException  if there is a problem saving the file.
     */
    public static void main(String[] args) throws IOException {

//        JFrame diaglog = new JFrame();
    	JDialog dialog = new JDialog();
    	dialog.setTitle("Test");
//    	dialog.setModal(true);
//    	dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        //Create and set up the content pane.
        HistogramPanel newContentPane = new HistogramPanel("Title", HistogramPanel.getTestSeries());
        newContentPane.setOpaque(true); //content panes must be opaque
        dialog.setContentPane(newContentPane);

        //Display the window.
        dialog.setPreferredSize(new Dimension(800, 600));
        dialog.pack();
        dialog.setVisible(true);

    }

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand() == ACTION_PLOT1) {
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	showPlot1();
			    }
			});
			
		}
		else if (evt.getActionCommand() == ACTION_BIN_RULE1) {
			BinningRuleEnum newBinRule = (BinningRuleEnum)cboBinRule1.getSelectedItem();
			if (!newBinRule.equals(binRule1)) {
				binRule1 = (BinningRuleEnum)cboBinRule1.getSelectedItem();
				panelMain.remove(panelHistorgramChart);
				panelHistorgramChart = createChartPanel(this.createDataset(valueSeries, binRule1));
				panelMain.add(panelHistorgramChart, BorderLayout.CENTER);
				this.repaint();
			}
		}
	}
	
	private void showPlot1() {
		ProbabilityPlot plotter = new ProbabilityPlot(this.valueSeries);
	    plotter.gaussianProbabilityPlot();
	}
	
	private enum BinningRuleEnum {
		Square_root, Sturges, Rice, Scott, Freedman_Diaconis
	}

}