package org.processmining.sapm.performancemining.ui.main;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFStage;
import org.processmining.sapm.performancemining.ui.misc.ChartScrollBar2;

public class NodeView {
	/**
	 * 
	 */
	private static final long serialVersionUID = -202881433419871306L;
	private static final double ZOOM_IN_FACTOR = 0.9; //the new range of axis is 90% of current range
	private static final double ZOOM_OUT_FACTOR = (1.0 / 0.909); //the new range of axis is 110% of current range
	protected String description;
	protected SPF bpf;
	protected SPFStage stage;
	protected ChartPanel chartPanel;
	protected Map<TimePeriod, DateTime> timeMap = new HashMap<TimePeriod, DateTime>(); //to map the time on chart to time point
	ChartScrollBar2 horiScrollbar;
	ChartScrollBar2 vertiScrollbar;
	BoundedRangeModel orihoriScrollbarModel = null;

	public NodeView(SPF bpf, SPFStage stage, String label) {
		description = label;
		this.bpf = bpf;
		this.stage = stage;
	}

	public SPF getBPF() {
		return bpf;
	}
	
	public SPFStage getStage() {
		return this.stage;
	}

	public String getStageName() {
		return stage.getName();
	}

	public String toString() {
		return description;
	}

	protected TimePeriod getTimePeriod(DateTime timePoint) {
		if (bpf.getConfig().getTimeStep() <= 3600) {
			return new Hour(timePoint.toDate());
		} else {
			return new Day(timePoint.toDate());
		}
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return description;
	}

	public JPanel createPanel() {
		JFreeChart jfreechart = createChart(createDataset());
		chartPanel = new ChartPanel(jfreechart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setZoomAroundAnchor(false);
		chartPanel.setZoomInFactor(ZOOM_IN_FACTOR);
		chartPanel.setZoomOutFactor(ZOOM_OUT_FACTOR);

		//		chartPanel.setHorizontalAxisTrace(true);
		//		chartPanel.setVerticalAxisTrace(true);		

		horiScrollbar = new ChartScrollBar2(Adjustable.HORIZONTAL, jfreechart);
		vertiScrollbar = new ChartScrollBar2(Adjustable.VERTICAL, jfreechart);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(chartPanel, BorderLayout.CENTER);
		mainPanel.add(horiScrollbar, BorderLayout.SOUTH);
		mainPanel.add(vertiScrollbar, BorderLayout.EAST);

		return mainPanel;
	}

	public JFreeChart createChart(XYDataset dataset) {
		return null;
	}

	public XYDataset createDataset() {
		return null;
	}

	public void zoomInBoth() {
		if (chartPanel != null) {
			final ChartRenderingInfo info = chartPanel.getChartRenderingInfo();
			info.getPlotInfo().getDataArea();
			//	        this.chartPanel.zoomInBoth(rect.getCenterX(), rect.getCenterY());
			chartPanel.zoomInBoth(0, 0);
		}
	}

	public void zoomOutBoth() {
		if (chartPanel != null) {
			final ChartRenderingInfo info = chartPanel.getChartRenderingInfo();
			info.getPlotInfo().getDataArea();
			//	        this.chartPanel.zoomOutBoth(rect.getCenterX(), rect.getCenterY());
			chartPanel.zoomOutBoth(0, 0);
		}
	}

	public void setSyncView(NodeView syncView) {
		if ((horiScrollbar != null) && (syncView.horiScrollbar != null)) {
			if (orihoriScrollbarModel == null) {
				orihoriScrollbarModel = horiScrollbar.getModel();
			}
			horiScrollbar.setModel(syncView.horiScrollbar.getModel());
		}

		if ((chartPanel != null) && (syncView.chartPanel != null)) {
			chartPanel.setMouseWheelEnabled(false);
			chartPanel.setMouseZoomable(false);
			syncView.chartPanel.setMouseWheelEnabled(false);
			syncView.chartPanel.setMouseZoomable(false);
		}
	}

	public void unsyncView(NodeView syncView) {
		if (orihoriScrollbarModel != null) {
			horiScrollbar.setModel(orihoriScrollbarModel);
		}
		if ((chartPanel != null) && (syncView.chartPanel != null)) {
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.setMouseZoomable(true);
			syncView.chartPanel.setMouseWheelEnabled(true);
			syncView.chartPanel.setMouseZoomable(true);
		}

	}

	public void restoreView() {
		if (chartPanel != null) {
			chartPanel.restoreAutoBounds();
		}
	}
}
