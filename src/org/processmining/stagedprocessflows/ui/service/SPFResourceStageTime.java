package org.processmining.stagedprocessflows.ui.service;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.main.NodeView;

public class SPFResourceStageTime extends NodeView {
	public SPFResourceStageTime(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	public XYDataset createResourceTimeRateDataset() {
		TimeSeries timeseries = new TimeSeries("Resource Time");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				getTimePeriod(bpf.getTimeSeries().get(i));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	public XYDataset createStageTimeDataset() {
		TimeSeries timeseries = new TimeSeries("Stage Time");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				getTimePeriod(bpf.getTimeSeries().get(i));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	@Override
	public JFreeChart createChart(XYDataset xydataset) {
		XYDataset resTimeDataset = createResourceTimeRateDataset();
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(getLabel() + " - " + getStageName(), "", "Hour",
				resTimeDataset, true, true, false);
		jfreechart.getLegend().setVisible(true);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		DateAxis dateaxis = new DateAxis("");
		xyplot.setDomainAxis(dateaxis);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		dateaxis.setLowerMargin(0.0D);
		dateaxis.setUpperMargin(0.0D);
		dateaxis.setTimeZone(bpf.getConfig().getTimeZone());

		StandardXYItemRenderer resRenderer = new StandardXYItemRenderer();
		resRenderer.setSeriesPaint(0, Color.red);
		resRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", new SimpleDateFormat(
				"dd.MM.yyyy HH:mm"), new DecimalFormat("0,000.00")));
		xyplot.setRenderer(0, resRenderer);

		xyplot.setDataset(1, createStageTimeDataset());
		StandardXYItemRenderer stageRender = new StandardXYItemRenderer();
		stageRender.setSeriesPaint(1, Color.blue);
		stageRender.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", new SimpleDateFormat(
				"dd.MM.yyyy HH:mm"), new DecimalFormat("0,000.00")));
		xyplot.setRenderer(1, stageRender);

		ChartUtilities.applyCurrentTheme(jfreechart);

		return jfreechart;
	}
}
