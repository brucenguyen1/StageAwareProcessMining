package org.processmining.stagedprocessflows.ui.service;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTimeZone;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFCell;
import org.processmining.stagedprocessflows.models.SPFStage;

public class SPFDepartureViewTooltipGenerator implements XYToolTipGenerator {
	private SPFStage stage = null;

	//	Map<TimePeriod,DateTime> timeMap;

	public SPFDepartureViewTooltipGenerator(SPFStage stage) {
		this.stage = stage;
		//		this.timeMap = timeMap;
	}

	public String generateToolTip(XYDataset dataset, int series, int item) {
		StringBuilder stringBuilder = new StringBuilder();
		String seriesName = dataset.getSeriesKey(series).toString();
		stringBuilder.append(String.format("<html><p style='color:#0000ff;'>Stage: '%s'</p>", seriesName));

		((TimeTableXYDataset) dataset).getTimePeriod(item);
		SPFCell cell = stage.getFlowCells().get(item);

		if (cell != null) {
			stringBuilder.append(stage.getSPF().getTimeSeries().get(item)
					.withZone(DateTimeZone.forTimeZone(stage.getSPF().getConfig().getTimeZone()))
					.toString("EEE, d MMM yyyy HH:mm:ss Z")
					+ "<br/>");
			for (String exitType : stage.getServiceExitSubCounts().keySet()) {
				stringBuilder.append(exitType + "(cases): " + stage.getServiceExitSubCounts().get(exitType).get(item)
						+ "<br/>");
				stringBuilder.append(exitType + " rate(cases/h): "
						+ cell.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE_TYPE + exitType) + "<br/>");
			}

			int passedCount = stage.getServicePassedCounts().get(item);
			stringBuilder.append("Passed(cases): " + passedCount + "<br/>");
			stringBuilder.append("Passed rate(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_PASSED_RATE));
		}
		stringBuilder.append("</html>");
		return stringBuilder.toString();
	}

	/**
	 * Extract the stage name from the series name The series name must have
	 * format: <StageName>-<Suffix>
	 * 
	 * @param seriesName
	 * @return the stage name. In case the series name is also the stage name
	 *         (without "-") then return the whole series name.
	 */
//	private String getStageName(String seriesName) {
//		if (seriesName.indexOf("-") != -1) {
//			return seriesName.substring(0, seriesName.indexOf("-"));
//		} else {
//			return seriesName;
//		}
//	}

}
