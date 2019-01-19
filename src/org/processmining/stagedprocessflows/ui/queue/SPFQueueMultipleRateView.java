package org.processmining.stagedprocessflows.ui.queue;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.main.NodeView;

public class SPFQueueMultipleRateView extends NodeView {
	public SPFQueueMultipleRateView(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	public XYDataset createArrivalRateDataset() {
		TimeSeries timeseries = new TimeSeries("Arrival");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_QUEUE_ARRIVAL_RATE));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	public XYDataset createDepartureRateDataset() {
		TimeSeries timeseries = new TimeSeries("Departure");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_QUEUE_DEPARTURE_RATE));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	@Override
	public JFreeChart createChart(XYDataset xydataset) {
		XYDataset arrivalDataset = createArrivalRateDataset();
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(getLabel() + " - " + getStageName(), "",
				"Cases/hour", arrivalDataset, true, true, false);
		jfreechart.getLegend().setVisible(true);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		DateAxis dateaxis = new DateAxis("");
		xyplot.setDomainAxis(dateaxis);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		dateaxis.setLowerMargin(0.0D);
		dateaxis.setUpperMargin(0.0D);
		dateaxis.setTimeZone(bpf.getConfig().getTimeZone());

		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
		DecimalFormat df = new DecimalFormat("0.00");

		StandardXYItemRenderer arrivalRenderer = new StandardXYItemRenderer();
		arrivalRenderer.setSeriesPaint(0, Color.red);
		arrivalRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", sdf, df));
		xyplot.setRenderer(0, arrivalRenderer);

		xyplot.setDataset(3, createDepartureRateDataset());
		StandardXYItemRenderer cancelledRenderer = new StandardXYItemRenderer();
		cancelledRenderer.setSeriesPaint(3, Color.magenta);
		cancelledRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", sdf, df));
		xyplot.setRenderer(3, cancelledRenderer);

		ChartUtilities.applyCurrentTheme(jfreechart);

		return jfreechart;
	}
}
