// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space

package org.processmining.sapm.performancemining.ui.system;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFStage;
import org.processmining.sapm.performancemining.ui.main.NodeView;

public class SPFMainView extends NodeView {
	public SPFMainView(SPF bpf, String desc) {
		super(bpf, null, desc);
	}

	@Override
	public XYDataset createDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();

		for (int i = bpf.getStages().size() - 1; i >= 0; i--) {
			SPFStage stage = bpf.getStages().get(i);

			if (i == bpf.getStages().size() - 1) {
				for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
					TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
					dataset.add(timeP, stage.getServicePassedCounts().get(j), stage.getName() + "-Complete");
				}
			}

			//Exit band
			for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
				dataset.add(timeP, stage.getServiceDepartureCounts().get(j) - stage.getServicePassedCounts().get(j),
						stage.getName() + "-Exit");
			}

			//Service band
			for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
				dataset.add(timeP, stage.getServiceArrivalCounts().get(j) - stage.getServiceDepartureCounts().get(j),
						stage.getName() + "-Service");
			}

			//Queue band
			for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
				dataset.add(timeP, stage.getQueueArrivalCounts().get(j) - stage.getServiceArrivalCounts().get(j),
						stage.getName() + "-Queue");
			}

		}

		return dataset;
	}

	@Override
	public JFreeChart createChart(XYDataset tablexydataset) {
		JFreeChart jfreechart = ChartFactory.createStackedXYAreaChart(getLabel(), "Time", "Cases",
				(TableXYDataset) tablexydataset, PlotOrientation.VERTICAL, true, true, false);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		DateAxis dateaxis = new DateAxis("");
		xyplot.setDomainAxis(dateaxis);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		dateaxis.setLowerMargin(0.0D);
		dateaxis.setUpperMargin(0.0D);
		dateaxis.setTimeZone(bpf.getConfig().getTimeZone());

		XYToolTipGenerator xyToolTipGenerator = new SPFMainViewTooltipGenerator(bpf);
		XYItemRenderer xyitemrenderer = xyplot.getRenderer();
		xyitemrenderer.setBaseToolTipGenerator(xyToolTipGenerator);

		ChartUtilities.applyCurrentTheme(jfreechart);
		return jfreechart;
	}
}
