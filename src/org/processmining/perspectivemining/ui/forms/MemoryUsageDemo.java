package org.processmining.perspectivemining.ui.forms;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.Timer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
 
/**
 *
 * @author Omerta
 */
public class MemoryUsageDemo extends Panel{
 
    private TimeSeries total;
    private TimeSeries free;
 
    public MemoryUsageDemo(int maxAge){
 
        super(new BorderLayout());
 
        this.total = new TimeSeries("Total Memory", Millisecond.class);
        this.total.setMaximumItemAge(maxAge);
        this.free = new TimeSeries("Free Momory", Millisecond.class);
        this.free.setMaximumItemAge(maxAge);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.total);
        dataset.addSeries(this.free);
 
        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis("Memory");
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
 
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.green);
        renderer.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);
 
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
        JFreeChart chart = new JFreeChart("JVM Memory Usage", new Font("SansSerif", Font.BOLD, 24), plot, true);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createLineBorder(Color.black)));
        add(chartPanel);
 
    }
 
    private void addTotalObservation(double y){
     this.total.add(new Millisecond(), y);
    }
 
    private void addFreeObservation(double y){
        this.free.add(new Millisecond(), y);
    }
 
    class DataGenerator extends Timer implements ActionListener{
 
        DataGenerator(int interval){
            super(interval, null);
            addActionListener(this);
        }
        public void actionPerformed(ActionEvent event) {
 
            long f = Runtime.getRuntime().freeMemory();
            long t = Runtime.getRuntime().totalMemory();
            addTotalObservation(t);
            addFreeObservation(f);
        }
    }
 
       public static void main(String[] args) {
 
        JFrame frame = new JFrame("Memory Usage Demo");
        MemoryUsageDemo panel = new MemoryUsageDemo(60000);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setBounds(200, 120, 600, 280);
        frame.setVisible(true);
        panel.new DataGenerator(100).start();
 
        frame.addWindowListener(new WindowAdapter() {
 
        public void windowClosing(WindowEvent e) {
 
        System.exit(0);
        }});
         
        }
}