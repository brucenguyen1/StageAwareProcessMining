package org.processmining.sapm.performancemining.ui.service;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFStage;
import org.processmining.sapm.performancemining.ui.main.NodeView;

public class SPFMultipleRateView extends NodeView {
	public SPFMultipleRateView(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	public XYDataset createArrivalRateDataset() {
		TimeSeries timeseries = new TimeSeries("Arrival");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_SERVICE_ARRIVAL_RATE));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	public XYDataset createPassedRateDataset() {
		TimeSeries timeseries = new TimeSeries("Passed");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_SERVICE_PASSED_RATE));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	public List<XYDataset> createExitRateDataset() {
		List<XYDataset> datasetList = new ArrayList<XYDataset>();

		try {
			for (String exitType : stage.getServiceExitSubCounts().keySet()) {
				TimeSeries timeseries = new TimeSeries(exitType);
				for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
					TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
					timeseries.add(
							(RegularTimePeriod) timeP,
							1.0 * stage.getFlowCells().get(i)
									.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE_TYPE + exitType));
				}
				datasetList.add(new TimeSeriesCollection(timeseries));
			}

		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}

		return datasetList;
	}

	@Override
	public JFreeChart createChart(XYDataset xydataset) {
		Color[] colors = { Color.red, Color.magenta, Color.pink, Color.cyan, Color.yellow };
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

		StandardXYItemRenderer arrivalRenderer = new StandardXYItemRenderer();
		arrivalRenderer.setSeriesPaint(0, Color.blue);
		arrivalRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", new SimpleDateFormat(
				"dd.MM.yyyy HH:mm"), new DecimalFormat("0,000.00")));
		xyplot.setRenderer(0, arrivalRenderer);

		xyplot.setDataset(1, createPassedRateDataset());
		StandardXYItemRenderer passedRenderer = new StandardXYItemRenderer();
		passedRenderer.setSeriesPaint(1, Color.orange);
		passedRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", new SimpleDateFormat(
				"dd.MM.yyyy HH:mm"), new DecimalFormat("0,000.00")));
		xyplot.setRenderer(1, passedRenderer);

		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
		DecimalFormat df = new DecimalFormat("0.00");

		List<XYDataset> exitDatasetList = createExitRateDataset();
		int i = 2;
		for (XYDataset dataset : exitDatasetList) {
			xyplot.setDataset(i, dataset);
			StandardXYItemRenderer renderer = new StandardXYItemRenderer();
			renderer.setSeriesPaint(i, colors[i - 1]);
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", sdf, df));
			xyplot.setRenderer(i, renderer);
			i++;
		}
		ChartUtilities.applyCurrentTheme(jfreechart);

		return jfreechart;
	}
}
