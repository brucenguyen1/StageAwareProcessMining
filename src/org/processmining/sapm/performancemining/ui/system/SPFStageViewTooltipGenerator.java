package org.processmining.sapm.performancemining.ui.system;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTimeZone;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFCell;
import org.processmining.sapm.performancemining.models.SPFStage;

public class SPFStageViewTooltipGenerator implements XYToolTipGenerator {
	private SPFStage stage = null;

	//	Map<TimePeriod,DateTime> timeMap;

	public SPFStageViewTooltipGenerator(SPFStage stage) { //Map<TimePeriod,DateTime> timeMap) {
		this.stage = stage;
		//		this.timeMap = timeMap;
	}

	public String generateToolTip(XYDataset dataset, int series, int item) {
		StringBuilder stringBuilder = new StringBuilder();
		String seriesName = dataset.getSeriesKey(series).toString();

		stringBuilder.append(String.format("<html><p style='color:#0000ff;'>Stage: '%s'</p>", seriesName));
		SPFCell cell = stage.getFlowCells().get(item);

		if (cell != null) {
			stringBuilder.append(stage.getSPF().getTimeSeries().get(item)
					.withZone(DateTimeZone.forTimeZone(stage.getSPF().getConfig().getTimeZone()))
					.toString("EEE, d MMM yyyy HH:mm:ss Z")
					+ "<br/>");

			int queueArrivalCount = stage.getQueueArrivalCounts().get(item);
			int queueDepartureCount = stage.getServiceArrivalCounts().get(item);
			stringBuilder.append("Queue Arrival(cases): " + queueArrivalCount + "<br/>");
			stringBuilder.append("Queue AR(cases/h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_ARRIVAL_RATE) + "<br/>");
			stringBuilder.append("Queue Departure(cases): " + queueDepartureCount + "<br/>");
			stringBuilder.append("Queue DR(cases/h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_DEPARTURE_RATE) + "<br/>");
			stringBuilder.append("Queue CIP(cases): " + cell.getCharacteristic(SPF.CHAR_QUEUE_CIP) + "<br/>");
			stringBuilder.append("Queue TIS(h): " + cell.getCharacteristic(SPF.CHAR_QUEUE_TIS) + "<br/>") ;

			int serviceArrivalCount = stage.getServiceArrivalCounts().get(item);
			int serviceDepartureCount = stage.getServiceDepartureCounts().get(item);

			stringBuilder.append("Service Arrival(cases): " + serviceArrivalCount + "<br/>");
			stringBuilder.append("Service AR(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_ARRIVAL_RATE) + "<br/>");
			stringBuilder.append("Service Departure(cases): " + serviceDepartureCount + "<br/>");
			stringBuilder.append("Service DR(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_DEPARTURE_RATE) + "<br/>");
			stringBuilder.append("Service CIP(cases): " + cell.getCharacteristic(SPF.CHAR_SERVICE_CIP) + "<br/>");
			stringBuilder.append("Service TIS(h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_TIS) + "<br/>");
			
			int serviceExitCount = stage.getServiceExitCounts().get(item);
			stringBuilder.append("Exits(cases): " + serviceExitCount + "<br/>");
			stringBuilder.append("ER(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE) + "<br/>");
			for (String exitType : stage.getServiceExitSubCounts().keySet()) {
				stringBuilder.append(exitType + "(cases): " + stage.getServiceExitSubCounts().get(exitType).get(item) + "<br/>");
				stringBuilder.append(exitType + "_rate(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_EXIT_RATE_TYPE + exitType) + "<br/>");
			}

			int servicePassedCount = stage.getServicePassedCounts().get(item);
			stringBuilder.append("Completed Cases(cases): " + servicePassedCount + "<br/>");
			stringBuilder.append("Completetion Rate(cases/h): " + cell.getCharacteristic(SPF.CHAR_SERVICE_PASSED_RATE) + "<br/>");
		}
		stringBuilder.append("</html>");
		return stringBuilder.toString();
	}

}
