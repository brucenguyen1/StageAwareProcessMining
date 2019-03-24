package org.processmining.sapm.performancemining.ui.queue;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFStage;
import org.processmining.sapm.performancemining.ui.main.NodeView;

public class SPFQueueArrivalRateView extends NodeView {
	public SPFQueueArrivalRateView(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	@Override
	public XYDataset createDataset() {
		TimeSeries timeseries = new TimeSeries(stage.getName());

		try {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				timeseries.add((RegularTimePeriod) timeP,
						1.0 * stage.getFlowCells().get(i).getCharacteristic(SPF.CHAR_QUEUE_ARRIVAL_RATE));
				//				if (!this.timeMap.containsKey(timeP)) {
				//					this.timeMap.put(timeP, timePoint);
				//				}
			}
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
		}
		return new TimeSeriesCollection(timeseries);
	}

	@Override
	public JFreeChart createChart(XYDataset xydataset) {
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(getLabel() + " - " + getStageName(), "",
				"Cases/hour", xydataset, true, true, false);

		//		jfreechart.getLegend().setPosition(RectangleEdge.RIGHT);
		jfreechart.getLegend().setVisible(false);

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
		xyplot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator("({1}, {2})", sdf, df));

		ChartUtilities.applyCurrentTheme(jfreechart);
		return jfreechart;
	}
}
