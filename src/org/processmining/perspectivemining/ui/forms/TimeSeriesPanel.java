package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class TimeSeriesPanel extends JPanel {
	private long window;
	
	public TimeSeriesPanel(double[] values, long startTimestamp, long window) {
		this.window = window;
		JFreeChart jfreechart = createChart(this.createDataset(values, startTimestamp, window));
		ChartPanel chartPanel = new ChartPanel(jfreechart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(false);
		this.setLayout(new BorderLayout());
		this.add(chartPanel, BorderLayout.CENTER);
	}
	
	/*
	 * window is minutes
	 */
	private XYDataset createDataset(double[] values, long startTime, long window) {
	   TimeSeries timeseries = new TimeSeries("");
		try {
			long periodStart = startTime;
			for (int i = 0; i < values.length; i++) {
				if (i > 0) periodStart += window*24*3600*1000;
				timeseries.add(new CustomDayPeriod(periodStart, window), values[i]);
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
    }
   
	private JFreeChart createChart(XYDataset xydataset) {
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("", "",
				"", xydataset, true, true, false);

		jfreechart.getLegend().setVisible(false);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		DateAxis dateaxis = new DateAxis("");
		xyplot.setDomainAxis(dateaxis);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		dateaxis.setLowerMargin(0.0D);
		dateaxis.setUpperMargin(0.0D);
		dateaxis.setVerticalTickLabels(true);
//		dateaxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
		dateaxis.setDateFormatOverride(sdf);
		
//		dateaxis.setTimeZone(bpf.getConfig().getTimeZone());
//		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
//		DecimalFormat df = new DecimalFormat("0.00");

		ChartUtilities.applyCurrentTheme(jfreechart);
		return jfreechart;
	}
	
	public static void main(String[] args) {
		  //Schedule a job for the event-dispatching thread:
		  //creating and showing this application's GUI.
		  javax.swing.SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		      	XLog log = null;
		  		try {
		      	//Create and set up the window.
		          JFrame diaglog = new JFrame();
		          diaglog.setTitle("Test");
//		          diaglog.setModal(true);
		          diaglog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		          
		          //Create and set up the content pane.
		          double[] values = new double[] {1, 10, 12, 3, 4};
		          TimeSeriesPanel newContentPane = new TimeSeriesPanel(values, (new Date()).getTime(), 2);
		          newContentPane.setOpaque(true); //content panes must be opaque
		          diaglog.setContentPane(newContentPane);
		
		          //Display the window.
		          diaglog.setPreferredSize(new Dimension(600, 1000));
		          diaglog.pack();
		          diaglog.setVisible(true);
		  		}
		  		catch (Exception e) {
		  			e.printStackTrace();
		  		}
		      }
		  });
		}
}
