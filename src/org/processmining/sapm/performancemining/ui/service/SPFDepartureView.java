package org.processmining.sapm.performancemining.ui.service;

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

public class SPFDepartureView extends NodeView {
	public SPFDepartureView(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	@Override
	public XYDataset createDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();

		for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
			TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
			dataset.add(timeP, stage.getServicePassedCounts().get(i), stage.getName() + "-passed");
		}

		for (String exitType : stage.getServiceExitSubCounts().keySet()) {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				dataset.add(timeP, stage.getServiceExitSubCounts().get(exitType).get(i), stage.getName() + "-"
						+ exitType);
			}
		}

		return dataset;
	}

	@Override
	public JFreeChart createChart(XYDataset tablexydataset) {
		JFreeChart jfreechart = ChartFactory.createStackedXYAreaChart(getLabel() + " - " + getStageName(), "Time",
				"Cases", (TableXYDataset) tablexydataset, PlotOrientation.VERTICAL, true, true, false);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		DateAxis dateaxis = new DateAxis("");
		xyplot.setDomainAxis(dateaxis);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		dateaxis.setLowerMargin(0.0D);
		dateaxis.setUpperMargin(0.0D);
		dateaxis.setTimeZone(bpf.getConfig().getTimeZone());

		XYToolTipGenerator xyToolTipGenerator = new SPFDepartureViewTooltipGenerator(this.stage);
		XYItemRenderer xyitemrenderer = xyplot.getRenderer();
		xyitemrenderer.setBaseToolTipGenerator(xyToolTipGenerator);

		ChartUtilities.applyCurrentTheme(jfreechart);
		return jfreechart;
	}
}
