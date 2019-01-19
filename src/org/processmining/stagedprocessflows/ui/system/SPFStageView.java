package org.processmining.stagedprocessflows.ui.system;

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
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.main.NodeView;

public class SPFStageView extends NodeView {
	public SPFStageView(SPF bpf, SPFStage stage, String desc) {
		super(bpf, stage, desc);
	}

	@Override
	public XYDataset createDataset() {
		TimeTableXYDataset dataset = new TimeTableXYDataset();

		for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
			TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
			dataset.add(timeP, stage.getServiceDepartureCounts().get(i) - stage.getServiceExitCounts().get(i), 
					stage.getName() + "-passed");
		}

		for (String exitType : stage.getServiceExitSubCounts().keySet()) {
			for (int i = 0; i < bpf.getTimeSeries().size(); i++) {
				TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(i));
				dataset.add(timeP, stage.getServiceExitSubCounts().get(exitType).get(i), stage.getName() + "-"
						+ exitType);
			}
		}
		
		//Service band
		for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
			TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
			dataset.add(timeP, stage.getServiceArrivalCounts().get(j) - stage.getServiceDepartureCounts().get(j),
					stage.getName() + "-service");
		}
		
		//Queue band
		for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
			TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
			dataset.add(timeP, stage.getQueueArrivalCounts().get(j) - stage.getServiceArrivalCounts().get(j),
					stage.getName() + "-queue");
		}

		// Comment this out: we cannot view the queue in actual stage view because the
		// arrival count at a stage can be higher than the pass count at the preceding stage
		// due to jump-forward cases
		//Queue band
//		for (int j = 0; j < bpf.getTimeSeries().size(); j++) {
//			TimePeriod timeP = getTimePeriod(bpf.getTimeSeries().get(j));
//			dataset.add(timeP, stage.getQueueArrivalCounts().get(j) - stage.getServiceArrivalCounts().get(j),
//					stage.getName() + "-queue");
//		}

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

		XYToolTipGenerator xyToolTipGenerator = new SPFStageViewTooltipGenerator(this.stage);
		XYItemRenderer xyitemrenderer = xyplot.getRenderer();
		xyitemrenderer.setBaseToolTipGenerator(xyToolTipGenerator);

		ChartUtilities.applyCurrentTheme(jfreechart);
		return jfreechart;
	}
}