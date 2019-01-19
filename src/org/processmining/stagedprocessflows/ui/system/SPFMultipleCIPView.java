// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space

package org.processmining.stagedprocessflows.ui.system;

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
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.main.NodeView;

public class SPFMultipleCIPView extends NodeView {
	public SPFMultipleCIPView(SPF bpf, String desc) {
		super(bpf, null, desc);
	}

	public XYDataset createWIPDataset(SPFStage stage) {
		TimeSeries timeseries = new TimeSeries(stage.getName());

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_SERVICE_CIP));
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	public XYDataset createTotalWIPDataset() {
		TimeSeries timeseries = new TimeSeries("Total");

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				double totalWIP = 0;
				for (SPFStage stage : bpf.getStages()) {
					totalWIP += stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_SERVICE_CIP);
				}
				timeseries.add((RegularTimePeriod) timeP, totalWIP);
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	@Override
	public JFreeChart createChart(XYDataset xydataset) {
		List<XYDataset> datasetList = new ArrayList<XYDataset>();
		Color[] colors = { Color.red, Color.blue, Color.orange, Color.black, Color.magenta, Color.green, Color.pink,
				Color.cyan, Color.gray, Color.yellow };

		for (SPFStage stage : bpf.getStages()) {
			datasetList.add(createWIPDataset(stage));
		}
		datasetList.add(createTotalWIPDataset());

		if (!datasetList.isEmpty()) {

			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(getLabel() + " - Mutiple stages", "", "Cases",
					datasetList.get(0), true, true, false);
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
			for (int i = 0; i < datasetList.size(); i++) {
				xyplot.setDataset(i, datasetList.get(i));
				StandardXYItemRenderer renderer = new StandardXYItemRenderer();
				renderer.setSeriesPaint(i, colors[i]);
				renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", sdf, df));
				xyplot.setRenderer(i, renderer);
			}

			ChartUtilities.applyCurrentTheme(jfreechart);

			return jfreechart;
		} else {
			return null;
		}
	}
}
