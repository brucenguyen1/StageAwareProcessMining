package org.processmining.sapm.performancemining.ui.system;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTimeZone;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFCell;
import org.processmining.sapm.performancemining.models.SPFStage;

public class SPFMainViewTooltipGenerator implements XYToolTipGenerator {
	private SPF bpf = null;

	//	Map<TimePeriod,DateTime> timeMap;

	public SPFMainViewTooltipGenerator(SPF bpf) { //Map<TimePeriod,DateTime> timeMap) {
		this.bpf = bpf;
		//		this.timeMap = timeMap;
	}

	public String generateToolTip(XYDataset dataset, int series, int item) {
		StringBuilder stringBuilder = new StringBuilder();
		String seriesName = dataset.getSeriesKey(series).toString();

		stringBuilder.append(String.format("<html><p style='color:#0000ff;'>Stage: '%s'</p>", seriesName));

		String stageName = getStageName(seriesName);
		SPFStage stage = bpf.getStageByName(stageName);
		SPFCell cell = bpf.getStageByName(stageName).getFlowCells().get(item);

		if (cell != null) {
			stringBuilder.append(bpf.getTimeSeries().get(item)
					.withZone(DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone()))
					.toString("EEE, d MMM yyyy HH:mm:ss Z")
					+ "<br/>");
			if (seriesName.endsWith("Queue")) {
				int queueArrivalCount = bpf.getStageByName(stageName).getQueueArrivalCounts().get(item);
				int queueDepartureCount = bpf.getStageByName(stageName).getServiceArrivalCounts().get(item);
				stringBuilder.append("Arrival(cases): " + queueArrivalCount + "<br/>");
				stringBuilder.append("AR(cases/h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_ARRIVAL_RATE) + "<br/>");
				stringBuilder.append("Departure(cases): " + queueDepartureCount + "<br/>");
				stringBuilder.append("DR(cases/h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_DEPARTURE_RATE) + "<br/>");
				stringBuilder.append("CIP(cases): " + cell.getCharacteristic(SPF.CHAR_QUEUE_CIP) + "<br/>");
				stringBuilder.append("TIS(h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_TIS));
			} else if (seriesName.endsWith("Service")) {
				int serviceArrivalCount = bpf.getStageByName(stageName).getServiceArrivalCounts().get(item);
				int serviceDepartureCount = bpf.getStageByName(stageName).getServiceDepartureCounts().get(item);

				stringBuilder.append("Arrival(cases): " + serviceArrivalCount + "<br/>");
				stringBuilder.append("AR(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_ARRIVAL_RATE) + "<br/>");
				stringBuilder.append("Departure(cases): " + serviceDepartureCount + "<br/>");
				stringBuilder.append("DR(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_DEPARTURE_RATE)
						+ "<br/>");
				stringBuilder.append("CIP(cases): " + cell.getCharacteristic(SPF.CHAR_SERVICE_CIP) + "<br/>");
				stringBuilder.append("TIS(h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_TIS));
			} else if (seriesName.endsWith("Exit")) {
				int serviceExitCount = bpf.getStageByName(stageName).getServiceExitCounts().get(item);
				stringBuilder.append("Exits(cases): " + serviceExitCount + "<br/>");
				stringBuilder.append("ER(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE) + "<br/>");
				for (String exitType : stage.getServiceExitSubCounts().keySet()) {
					stringBuilder.append(exitType + "(cases): "
							+ stage.getServiceExitSubCounts().get(exitType).get(item) + "<br/>");
					stringBuilder.append(exitType + "_rate(cases/h): "
							+ cell.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE_TYPE + exitType) + "<br/>");
				}
			} else if (seriesName.endsWith("Complete")) {
				int servicePassedCount = bpf.getStageByName(stageName).getServicePassedCounts().get(item);
				stringBuilder.append("Completed Cases(cases): " + servicePassedCount + "<br/>");
				stringBuilder.append("Completetion Rate(cases/h): "
						+ cell.getCharacteristic(SPF.CHAR_SERVICE_PASSED_RATE) + "<br/>");
			}
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
	private String getStageName(String seriesName) {
		if (seriesName.lastIndexOf("-") != -1) {
			return seriesName.substring(0, seriesName.lastIndexOf("-"));
		} else {
			return seriesName;
		}
	}

}
