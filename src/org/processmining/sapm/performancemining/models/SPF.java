package org.processmining.sapm.performancemining.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.processmining.sapm.performancemining.database.ConnectionManager;
import org.processmining.sapm.performancemining.filter.TraceAttributeFilterParameters;
import org.processmining.sapm.performancemining.parameters.SPFConfig;
import org.processmining.sapm.performancemining.util.LogUtils;

public class SPF {
	public final static String CHAR_SERVICE_ARRIVAL_RATE = "A";
	public final static String CHAR_SERVICE_DEPARTURE_RATE = "B";
	public final static String CHAR_SERVICE_PASSED_RATE = "C";
	public final static String CHAR_SERVICE_EXIT_RATE = "D";
	public final static String CHAR_SERVICE_CIP = "E";
	public final static String CHAR_SERVICE_TIS = "F";
	public final static String CHAR_SERVICE_FLOW_EFFICIENCY = "G";
	public final static String CHAR_SERVICE_EXIT_RATE_TYPE = "H_";

	public final static String CHAR_QUEUE_ARRIVAL_RATE = "I";
	public final static String CHAR_QUEUE_DEPARTURE_RATE = "J";
	public final static String CHAR_QUEUE_CIP = "K";
	public final static String CHAR_QUEUE_TIS = "L";

	public final static String CASE_START_TIME = "StartTime";
	public final static String CASE_END_TIME = "EndTime";
	public final static String CASE_EVENT_ACOUNT = "EventCount";

	public final static String STATUS_COMPLETED = "completed";
	public final static String STATUS_DECLINED = "declined";
	public final static String STATUS_CANCELLED = "cancelled";

	private DateTime startTimePoint = new DateTime(9999, 12, 1, 1, 0);
	private DateTime endTimePoint = new DateTime(0);
	private final List<DateTime> timeSeries = new ArrayList<DateTime>();
	private List<SPFStage> stageList = null; //keep stage order
	private Map<String, SPFStage> nameStageMap = null; //map stage name to stage object
	private boolean isFullBPF = false;
	private TraceAttributeFilterParameters filter = null;

	private SPFConfig config = null;

	public SPF() {
		//
	}

	/**
	 * Create full BPF
	 * 
	 * @param config
	 * @throws Exception
	 */
	public SPF(SPFConfig config, TraceAttributeFilterParameters filter) throws Exception {
		this.config = config;
		nameStageMap = new HashMap<String, SPFStage>();
		stageList = new ArrayList<SPFStage>();
		isFullBPF = true;
		this.filter = filter;

		// Create stages
		for (String stageLabel : config.getStageList()) {
			SPFStage stage = new SPFStage(stageLabel, this);
			stageList.add(stage);
			nameStageMap.put(stageLabel, stage);
			
			if (config.getFakeStages().contains(stageLabel)) {
				SPFStage actualStage = new SPFStage(stageLabel, this);
				stage.setActualStage(actualStage);
			}
		}

		config.getContext().getProgress().setMinimum(0);
		config.getContext().getProgress().setMaximum(300);
		config.getContext().getProgress().setValue(1);
		config.getContext().getProgress().setIndeterminate(false);

		// First store data of case movement
		createData(SPFManager.getInstance().selectCases(filter.getFilter()));

		// Compute queuing metrics based on the stored data
		compute();
	}

	public DateTime getStartTimePoint() {
		return startTimePoint;
	}

	public DateTime getEndTimePoint() {
		return endTimePoint;
	}

	public DateTime getLastSeriesPoint() {
		return timeSeries.get(timeSeries.size() - 1);
	}

	public SPFConfig getConfig() {
		return config;
	}

	public List<SPFStage> getStages() {
		return stageList;
	}

	/**
	 * Return true if this BPF is a full BPF for the whole log
	 * 
	 * @return true/false
	 */
	public boolean getIsFullBPF() {
		return isFullBPF;
	}

	public void setIsFullBPF(boolean isFullBPF) {
		this.isFullBPF = isFullBPF;
	}

	public TraceAttributeFilterParameters getFilter() {
		return filter;
	}

	public void setFilter(TraceAttributeFilterParameters filter) {
		this.filter = filter;
	}

	/**
	 * Compute stage and stage blocks
	 * 
	 * @throws Exception
	 */
	private void compute() throws Exception {
		config.getContext().getProgress().setCaption("Computing BPF characteristics");

		List<DateTime> timeSeries = getTimeSeries();
		config.getContext().getProgress().setValue(55);
		System.out.println("TIME SERIES");
		System.out.println(timeSeries);

		//-----------------------------------------
		// Compute arrival cumulative cases for stages
		//-----------------------------------------
		System.out.println("Compute arrival counts");
		config.getContext().getProgress().setCaption("Computing BPF characteristics - Cumulative Arrivals");
		for (SPFStage stage : stageList) {
			for (DateTime timePoint : timeSeries) {
//				int count = ConnectionManager.selectCumulativeCount(stage.getName(), timePoint, 
//										ConnectionManager.CUM_TYPE_ARRIVAL, "", false);
				int count = ConnectionManager.selectCumulativeArrivalCount(stage.getName(), timePoint, false);
				stage.getServiceArrivalCounts().add(count);
				
				// The arrival flow for actual stages must remove the count of fake cases
				if (stage.getActualStage() != null) {
					int actualAcount = ConnectionManager.selectCumulativeArrivalCount(stage.getName(), timePoint, true);
					stage.getActualStage().getServiceArrivalCounts().add(actualAcount);
				}
			}
			config.getContext().getProgress().inc();
		}

		//System.out.println(this.stageList.get(0).getArrivalCounts());

		//-----------------------------------------
		// Compute departure cumulative cases for stages
		//-----------------------------------------
		System.out.println("Compute departure counts");
		config.getContext().getProgress().setCaption("Computing BPF characteristics - Cumulative Departure");
		for (SPFStage stage : stageList) {
			for (DateTime timePoint : timeSeries) {
//				int count = ConnectionManager.selectCumulativeCount(stage.getName(), timePoint, 
//										ConnectionManager.CUM_TYPE_DEPARTURE, "", false);
				int count = ConnectionManager.selectCumulativeDepartureCount(stage.getName(), timePoint, false);
				stage.getServiceDepartureCounts().add(count);
				
				// The departure flow for actual stages must remove the count of fake cases
				if (stage.getActualStage() != null) {
					int actualCount = ConnectionManager.selectCumulativeDepartureCount(stage.getName(), timePoint, true);
					stage.getActualStage().getServiceDepartureCounts().add(actualCount);
				}
			}
			config.getContext().getProgress().inc();
		}

		//-----------------------------------------
		// Compute cumulative cases for every exit types
		//-----------------------------------------
		System.out.println("Compute counts for every exit type");
		config.getContext().getProgress().setCaption("Computing BPF characteristics - Cumulative Exit");
		for (SPFStage stage : stageList) {
			for (String caseExitStatus : config.getCaseExitStatusList()) {
				if (!stage.getServiceExitSubCounts().containsKey(caseExitStatus)) {
					stage.getServiceExitSubCounts().put(caseExitStatus, new ArrayList<Integer>());
				}
				for (DateTime timePoint : timeSeries) {
//					int count = ConnectionManager.selectCumulativeCount(stage.getName(), timePoint,
//							ConnectionManager.CUM_TYPE_EXIT, exitType, false);
					int count = ConnectionManager.selectCumulativeExitCount(caseExitStatus, stage.getName(), timePoint);
					stage.getServiceExitSubCounts().get(caseExitStatus).add(count);
					
					// Note that the exit flow for actual stages is not affected by the fake events
					if (stage.getActualStage() != null) {
						if (!stage.getActualStage().getServiceExitSubCounts().containsKey(caseExitStatus)) {
							stage.getActualStage().getServiceExitSubCounts().put(caseExitStatus, new ArrayList<Integer>());
						}
						stage.getActualStage().getServiceExitSubCounts().get(caseExitStatus).add(count);
					}
				}
				config.getContext().getProgress().inc();
			}
		}
		
		//-----------------------------------------
		// Compute enqueuing cumulative cases for stages
		// Enqueuing items are equated to the departure from the previous stage
		// Assumption: enqueueing is immediate after the departure
		//-----------------------------------------		
		System.out.println("Compute queue arrival counts");
		for (SPFStage stage : stageList) {
			SPFStage preStage = previousStage(stage);
			if (preStage != null) {
				stage.setQueueArrivalCounts(preStage.getServicePassedCounts());
				
				//Actual stage is calculated based on actual stages
				if (stage.getActualStage() != null) {
					if (preStage.getActualStage() != null) {
						stage.getActualStage().setQueueArrivalCounts(preStage.getActualStage().getServicePassedCounts());
					}
					else {
						stage.getActualStage().setQueueArrivalCounts(preStage.getServicePassedCounts());
					}
				}
			} else {
				stage.setQueueArrivalCounts(stage.getServiceArrivalCounts());
				
				if (stage.getActualStage() != null) {
					stage.getActualStage().setQueueArrivalCounts(stage.getActualStage().getServiceArrivalCounts());
				}
			}
			
			config.getContext().getProgress().inc();
		}

		//-----------------------------------------
		// Compute case cumulative time
		//-----------------------------------------
		System.out.println("Compute case cumulative time");
		config.getContext().getProgress().setCaption("Computing BPF characteristics - Case Cumulative Time");
		for (SPFStage stage : stageList) {
			for (DateTime timePoint : timeSeries) {
				long totalTime = ConnectionManager.selectCumulativeCaseTime2(stage.getName(), timePoint);
				stage.getCaseCumulativeTime().add(totalTime);
				
				if (stage.getActualStage() != null) {
					stage.getActualStage().getCaseCumulativeTime().add(totalTime);
				}
			}
			config.getContext().getProgress().inc();
		}

		//-----------------------------------------
		// Compute res cumulative time
		//-----------------------------------------
		System.out.println("Compute res cumulative time");
		config.getContext().getProgress().setCaption("Computing BPF characteristics - Res Cumulative Time");
		for (SPFStage stage : stageList) {
			for (DateTime timePoint : timeSeries) {
				long totalTime = ConnectionManager.selectCumulativeResTime2(stage.getName(), timePoint);
				stage.getResCumulativeTime().add(totalTime);
				
				if (stage.getActualStage() != null) {
					stage.getActualStage().getResCumulativeTime().add(totalTime);
				}
			}
			config.getContext().getProgress().inc();
		}

		//-----------------------------------------
		// Compute characteristics at flow cells
		//-----------------------------------------		
		System.out.println("Compute stage characteristics at flow cells");
		config.getContext().getProgress().setCaption("Computing BPF characteristics at flow cells");
		for (SPFStage stage : stageList) {
			stage.computeCharacteristics();
			config.getContext().getProgress().inc();
			
			if (stage.getActualStage() != null) {
				stage.getActualStage().computeCharacteristics();
			}
			config.getContext().getProgress().inc();
		}
	}

	private void computeTimeSeries() {
		timeSeries.clear();
		for (DateTime timePoint = startTimePoint; timePoint.isBefore(endTimePoint); timePoint = timePoint
				.plusSeconds(config.getTimeStep())) {
			timeSeries.add(timePoint);
		}
	}

	public List<DateTime> getTimeSeries() {
		return timeSeries;
	}

	public boolean isFirstStage(SPFStage stage) {
		return (stageList.indexOf(stage) == 0);
	}

	public boolean isLastStage(SPFStage stage) {
		return (stageList.indexOf(stage) == (stageList.size() - 1));
	}

	public SPFStage nextStage(SPFStage stage) {
		int i = stageList.indexOf(stage);
		if ((i >= 0) && (i < (stageList.size() - 1))) {
			return stageList.get(i + 1);
		} else {
			return null;
		}
	}

	public SPFStage previousStage(SPFStage stage) {
		int i = stageList.indexOf(stage);
		if ((i > 0) && (i <= (stageList.size() - 1))) {
			return stageList.get(i - 1);
		} else {
			return null;
		}
	}

	/**
	 * Select the last stage name from a set of stage names. The stage order is
	 * determined by the order of items in stageList
	 * 
	 * @param stageSet
	 * @return
	 */
	public String selectLastStage(List<String> stageNameList) {
		for (String stageName : stageNameList) {
			SPFStage stage = getStageByName(stageName);
			if (stage != null) {
				SPFStage nextStage = nextStage(stage);
				if ((nextStage == null) || ((nextStage != null) && !stageNameList.contains(nextStage.getName()))) {
					return stageName;
				}
			}
		}
		return null;
	}

	public SPFStage getStageByName(String stageName) {
		return nameStageMap.get(stageName);
	}

	public double getMeanArrivalRate(DateTime start, DateTime end) {
		if (!getStages().isEmpty()) {
			return getStages().get(0).getMean(SPF.CHAR_QUEUE_ARRIVAL_RATE, start, end);
		}
		return 0.0;
	}

	public double getMedianArrivalRate(DateTime start, DateTime end) {
		if (!getStages().isEmpty()) {
			return getStages().get(0).getMedian(SPF.CHAR_QUEUE_ARRIVAL_RATE, start, end);
		}
		return 0.0;
	}

	public double getMeanDepartureRate(DateTime start, DateTime end) {
		if (!getStages().isEmpty()) {
			for (int i = getStages().size() - 1; i >= 0; i--) {
				if (getStages().get(i).getServiceDepartureCounts().size() > 0) {
					return getStages().get(i).getMean(SPF.CHAR_SERVICE_DEPARTURE_RATE, start, end);
				}
			}
		}
		return 0.0;
	}

	public double getMedianDepartureRate(DateTime start, DateTime end) {
		if (!getStages().isEmpty()) {
			for (int i = getStages().size() - 1; i >= 0; i--) {
				if (getStages().get(i).getServiceDepartureCounts().size() > 0) {
					return getStages().get(i).getMedian(SPF.CHAR_SERVICE_DEPARTURE_RATE, start, end);
				}
			}
		}
		return 0.0;
	}

	public double getMeanFE(DateTime start, DateTime end) {
		double value = 0.0;
		for (SPFStage stage : getStages()) {
			value += (stage.getMean(SPF.CHAR_SERVICE_FLOW_EFFICIENCY, start, end));
		}
		return 1.0 * value / getStages().size();
	}

	public double getMeanWIP(DateTime start, DateTime end) {
		double wip = 0.0;
		for (SPFStage stage : getStages()) {
			wip += (stage.getMean(SPF.CHAR_QUEUE_CIP, start, end) + stage.getMean(SPF.CHAR_SERVICE_CIP, start, end));
		}
		return wip;
	}

	public double getMedianWIP(DateTime start, DateTime end) {
		double wip = 0.0;
		for (SPFStage stage : getStages()) {
			wip += (stage.getMedian(SPF.CHAR_QUEUE_CIP, start, end) + stage.getMedian(SPF.CHAR_SERVICE_CIP, start, end));
		}
		return wip;
	}

	public double getMeanTIS(DateTime start, DateTime end) {
		double tis = 0.0;
		for (SPFStage stage : getStages()) {
			tis += (stage.getMean(SPF.CHAR_QUEUE_TIS, start, end) + stage.getMean(SPF.CHAR_SERVICE_TIS, start, end));
		}
		return tis;
	}

	public double getMedianTIS(DateTime start, DateTime end) {
		double tis = 0.0;
		for (SPFStage stage : getStages()) {
			tis += (stage.getMedian(SPF.CHAR_QUEUE_TIS, start, end) + stage.getMedian(SPF.CHAR_SERVICE_TIS, start, end));
		}
		return tis;
	}

	public double getMeanExitRate(DateTime start, DateTime end) {
		double mean = 0.0;
		for (SPFStage stage : getStages()) {
			mean += stage.getMean(SPF.CHAR_SERVICE_EXIT_RATE, start, end);
		}
		return mean;
	}

	public double getMedianExitRate(DateTime start, DateTime end) {
		double mean = 0.0;
		for (SPFStage stage : getStages()) {
			mean += stage.getMedian(SPF.CHAR_SERVICE_EXIT_RATE, start, end);
		}
		return mean;
	}

	/**
	 * Create all data for the BPF Update startTimePoint and endTimePoint
	 */
	private void createData(Set<String> selectedCaseIDs) throws ClassNotFoundException, SQLException {
		ConnectionManager.initParametersFromFile();
		config.getContext().getProgress().setCaption("Importing event log into tables");
		String curTraceID = "";

		ConnectionManager.stageActivityIntervalMap.clear();
		ConnectionManager.stageServiceIntervalMap.clear();
		ConnectionManager.stageTotalIntervalMap.clear();
		ConnectionManager.stageServiceIntervalExitMap.clear();
		
		ConnectionManager.fakeStageActivityIntervalMap.clear();
		ConnectionManager.fakeStageServiceIntervalMap.clear();
		ConnectionManager.fakeStageTotalIntervalMap.clear();

		try {
			ConnectionManager.executeStatement("DELETE FROM APP.STAGE");
		} catch (SQLException e) {
			String sql = "";
			sql += "CREATE TABLE APP.STAGE (";
			sql += "CASEID VARCHAR(25),";
			sql += "STAGE VARCHAR(25),";
			sql += "QUEUESTART BIGINT,"; // start queuing 
			sql += "SERVICESTART BIGINT,"; // start working on the case
			sql += "SERVICECOMPLETE BIGINT,"; // complete working on the case
			sql += "STATUS VARCHAR(25),";
			sql += "LASTSTAGE VARCHAR(25),";
			sql += "ISFAKE VARCHAR(25)"; //true indicates that this is a fake stage movement
			sql += ")";
			ConnectionManager.executeStatement(sql);

			ConnectionManager
					.executeStatement("CREATE INDEX APP.STAGE_STAGE_START_IDX ON APP.STAGE(STAGE, SERVICESTART)");
			ConnectionManager
					.executeStatement("CREATE INDEX APP.STAGE_STAGE_COMPLETE_IDX ON APP.STAGE(STAGE, SERVICECOMPLETE)");
			ConnectionManager
					.executeStatement("CREATE INDEX APP.STAGE_STAGE_COMPLETE_STATUS_IDX ON APP.STAGE(STAGE, LASTSTAGE, SERVICECOMPLETE, STATUS)");
		}

		try {
			ConnectionManager.executeStatement("DELETE FROM APP.ACTIVITY");
		} catch (SQLException e) {
			String sql = "";
			sql += "CREATE TABLE APP.ACTIVITY (";
			sql += "CASEID VARCHAR(25),";
			sql += "STAGE VARCHAR(25),";
			sql += "RES VARCHAR(100),"; //resource name
			sql += "START BIGINT,";
			sql += "COMPLETE BIGINT";
			sql += ")";
			ConnectionManager.executeStatement(sql);
			ConnectionManager.executeStatement("CREATE INDEX APP.ACTIVITY_STAGE_IDX ON APP.ACTIVITY(STAGE, START)");
		}

		int counter = 0;
		startTimePoint = new DateTime(9999, 12, 1, 1, 0);
		endTimePoint = new DateTime(0);
		for (XTrace trace : config.getXLog()) {

			counter++;
			if (counter % 100 == 0) {
				config.getContext().getProgress().inc();
			}

			curTraceID = LogUtils.getConceptName(trace);
			System.out.println("TraceID: " + curTraceID);

			if (selectedCaseIDs.isEmpty() || selectedCaseIDs.contains(curTraceID)) {

				//---------------------------------------
				// Store case movement data
				//---------------------------------------
				Map<String, DateTime[]> stageTimePoints = new HashMap<String, DateTime[]>();
				Map<String, DateTime[]> stageRealTimePoints = new HashMap<String, DateTime[]>();
				List<String> stageNameList = new ArrayList<String>();
				//key: stagename, value: key: resName, value: set of res working intervals
				Map<String, Map<String, List<Interval>>> stageResIntervalMap = new HashMap<String, Map<String, List<Interval>>>();
				
				// Extract case movement data through stages from a trace
				computeOneTrace(trace, stageTimePoints, stageNameList, stageResIntervalMap);
				//				System.out.println(stageResIntervalMap);
				for (String stage : stageTimePoints.keySet()) {
					System.out.println("Stage " + stage + ", start/stop:" + stageTimePoints.get(stage)[0].toString()
							+ "/" + stageTimePoints.get(stage)[1].toString());
				}
				
				for (String stageName : stageTimePoints.keySet()) {
					Boolean isFakeStage = this.checkFakeStage(trace, stageName);

					//--------------------------------------------
					// Store activity intervals
					//--------------------------------------------
					if (!ConnectionManager.stageActivityIntervalMap.containsKey(stageName)) {
						ConnectionManager.stageActivityIntervalMap.put(stageName, new ArrayList<Interval>());
					}
					for (String res : stageResIntervalMap.get(stageName).keySet()) {
						ConnectionManager.stageActivityIntervalMap.get(stageName).addAll(
								stageResIntervalMap.get(stageName).get(res));
					}
					if (isFakeStage) {
						if (!ConnectionManager.fakeStageActivityIntervalMap.containsKey(stageName)) {
							ConnectionManager.fakeStageActivityIntervalMap.put(stageName, new ArrayList<Interval>());
						}
						for (String res : stageResIntervalMap.get(stageName).keySet()) {
							ConnectionManager.fakeStageActivityIntervalMap.get(stageName).addAll(
									stageResIntervalMap.get(stageName).get(res));
						}
					}
					
					// Store data in a database for debug purpose
					Map<String, List<Interval>> resIntervalMap = stageResIntervalMap.get(stageName);
					for (String res : resIntervalMap.keySet()) {
						for (Interval interval : resIntervalMap.get(res)) {
							ConnectionManager.insertActivity(curTraceID, stageName, res, interval.getStartMillis(),
									interval.getEndMillis());
						}
					}

					//--------------------------------------------
					// Store stage intervals
					//--------------------------------------------		

					// Calculate queue time
					SPFStage stage = getStageByName(stageName);
					SPFStage preStage = previousStage(stage);
					DateTime queueTime;
					if (preStage != null) {
						queueTime = stageTimePoints.get(preStage.getName())[1]; //service complete time of pre stage
					} else {
						queueTime = stageTimePoints.get(stageName)[0]; //service start time if this is the first stage
					}

					// Store data in memory for fast access
					if (!ConnectionManager.stageServiceIntervalMap.containsKey(stageName)) {
						ConnectionManager.stageServiceIntervalMap.put(stageName, new ArrayList<Interval>());
					}
					Interval stageServiceInterval = new Interval(stageTimePoints.get(stageName)[0], stageTimePoints.get(stageName)[1]);
					ConnectionManager.stageServiceIntervalMap.get(stageName).add(stageServiceInterval);
					
					// For the last stage of incomplete cases
					String caseStatus = trace.getAttributes().get("status").toString();
					if (config.getCaseExitStatusList().contains(caseStatus) &&
						stageName.equalsIgnoreCase(stageNameList.get(stageNameList.size()-1))) {
						if (!ConnectionManager.stageServiceIntervalExitMap.containsKey(caseStatus)) {
							ConnectionManager.stageServiceIntervalExitMap.put(caseStatus, new HashMap<>());
						}
						if (!ConnectionManager.stageServiceIntervalExitMap.get(caseStatus).containsKey(stageName)) {
							ConnectionManager.stageServiceIntervalExitMap.get(caseStatus).put(stageName, new ArrayList<Interval>());
						}
						ConnectionManager.stageServiceIntervalExitMap.get(caseStatus).get(stageName).add(stageServiceInterval);
					}
					
					if (isFakeStage) {
						if (!ConnectionManager.fakeStageServiceIntervalMap.containsKey(stageName)) {
							ConnectionManager.fakeStageServiceIntervalMap.put(stageName, new ArrayList<Interval>());
						}
						ConnectionManager.fakeStageServiceIntervalMap.get(stageName).add(stageServiceInterval);
					}

					if (!ConnectionManager.stageTotalIntervalMap.containsKey(stageName)) {
						ConnectionManager.stageTotalIntervalMap.put(stageName, new ArrayList<Interval>());
					}
					Interval stageTotalInterval = new Interval(queueTime, stageTimePoints.get(stageName)[1]);
					ConnectionManager.stageTotalIntervalMap.get(stageName).add(stageTotalInterval);
					if (isFakeStage) {
						if (!ConnectionManager.fakeStageTotalIntervalMap.containsKey(stageName)) {
							ConnectionManager.fakeStageTotalIntervalMap.put(stageName, new ArrayList<Interval>());
						}
						ConnectionManager.fakeStageTotalIntervalMap.get(stageName).add(stageTotalInterval);
					}

					// Store data in a database for debug purpose
					ConnectionManager.insertStage(curTraceID, stageName, queueTime.getMillis(),
							stageTimePoints.get(stageName)[0].getMillis(),
							stageTimePoints.get(stageName)[1].getMillis(),
							LogUtils.getValue(trace.getAttributes().get("status")).toLowerCase(),
							stageNameList.get(stageNameList.size()-1), 
							isFakeStage.toString());
				}
				ConnectionManager.getConnection().commit();
			}
		}

		//		this.computeLastSeriesPoint();
		computeTimeSeries();

	}
	
	/*
	 * Check whether the movement of a case at a given stage
	 * is artificial or real. Artificial movement is known
	 * by the existence of events with name = 'fake_event'
	 * which was inserted into the case.
	 */
	private boolean checkFakeStage(XTrace trace, String stageName) {
		for (XEvent event : trace) {
			if (LogUtils.getConceptName(event).equalsIgnoreCase("fake_event") && 
					LogUtils.getValue(event.getAttributes().get("stage")).equalsIgnoreCase(stageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extract stages and their timepoints (start and complete timestamp)
	 * 
	 * @param trace
	 * @param stageTimePoints
	 *            : map from stagename to two-element array, 1st is service
	 *            start time, 2nd is service complete time
	 * @param stageRealTimePoints: similar to stageTimePoints but only use
	 * 			events with concept:name != 'fake_event'.         
	 * @param stageSet
	 *            : set of stages in the trace
	 * @param stageResIntervalMap
	 *            : key: stagename, value: key: resName, value: set of res
	 *            working intervals This method also updates the startTimePoint
	 *            and endTimePoint
	 */
	private void computeOneTrace(XTrace trace, Map<String, DateTime[]> stageTimePoints, List<String> stageNameList,
			Map<String, Map<String, List<Interval>>> stageResIntervalMap) {

		Map<String, DateTime> startEvents = new HashMap<String, DateTime>(); //key: eventname + "-" resName, value: datetime of event
		Map<String, List<Interval>> resIntervalMap = null; //key: res name, value: list of intervals when res is working
		String currentStage = "";

		for (XEvent event : trace) {
			String eventName = LogUtils.getConceptName(event).toLowerCase();
			//			String eventStage = config.getEventStageMap().get(eventName).toLowerCase();
			String eventStage = LogUtils.getValue(event.getAttributes().get("stage")).toLowerCase();
			DateTime eventTime = LogUtils.getTimestamp(event);
			String eventTrans = LogUtils.getLifecycleTransition(event).toLowerCase();
			String eventRes = LogUtils.getOrganizationalResource(event).toLowerCase();
			String eventResKey = eventName + "@" + eventRes;

			//-------------------------------------
			// Collect the resource working time by stage
			// Assume that a resource should finish an activity 
			// before starting a new activity, i.e. there must be 
			// a complete event in correspondence with a start event 
			// before there is a new start event of the same event class
			// carried out by the same resource.
			//-------------------------------------
			if (currentStage.equals("")) {
				currentStage = eventStage;
				resIntervalMap = new HashMap<String, List<Interval>>();
			} else if (!currentStage.equals(eventStage)) { // CHANGE STAGE
				stageResIntervalMap.put(currentStage, resIntervalMap);
				resIntervalMap = new HashMap<String, List<Interval>>();
				startEvents.clear();
				currentStage = eventStage;
			}

			if (eventTrans.equals("start")) {
				if (!startEvents.containsKey(eventResKey)) {
					startEvents.put(eventResKey, eventTime);
				}
			} else if (eventTrans.equals("complete")) {
				if (startEvents.containsKey(eventResKey)) {
					if (!resIntervalMap.containsKey(eventRes)) {
						List<Interval> startInterval = new ArrayList<Interval>();
						startInterval.add(new Interval(startEvents.get(eventResKey), eventTime));
						resIntervalMap.put(eventRes, startInterval);
					} else {
						resIntervalMap.get(eventRes).add(new Interval(startEvents.get(eventResKey), eventTime));
					}
					startEvents.remove(eventResKey);
				}
			}

			if (event == trace.get(trace.size() - 1)) { // END OF TRACE
				//System.out.println(resIntervalMap);
				stageResIntervalMap.put(currentStage, resIntervalMap);
				startEvents.clear();
			}

			//-------------------------------------
			// Update the start and end time point for the log
			//-------------------------------------			
			if (startTimePoint.isAfter(eventTime) && (eventTrans.equals("start") || eventTrans.equals("complete"))) {
				startTimePoint = eventTime;
			}

			if (endTimePoint.isBefore(eventTime) && (eventTrans.equals("start") || eventTrans.equals("complete"))) {
				endTimePoint = eventTime;
			}

			//-------------------------------------
			// Collect the start and end time for the stage
			//-------------------------------------			
			if (!stageTimePoints.containsKey(eventStage)) {
				stageTimePoints.put(eventStage, new DateTime[2]);
				stageTimePoints.get(eventStage)[0] = eventTime;
				stageTimePoints.get(eventStage)[1] = eventTime;
			} else {
				if (stageTimePoints.get(eventStage)[0].isAfter(eventTime)) {
					stageTimePoints.get(eventStage)[0] = eventTime;
				}
				if (stageTimePoints.get(eventStage)[1].isBefore(eventTime)) {
					stageTimePoints.get(eventStage)[1] = eventTime;
				}
			}

			//-------------------------------------
			// Collect all the stages of this trace
			//-------------------------------------			
			if (!stageNameList.contains(eventStage)) {
				stageNameList.add(eventStage);
			}
		}

	}

	/**
	 * Clear all PFBs, and Stage Blocks to create an empty BPF
	 */
	public void clear() {
		for (SPFStage stage : stageList) {
			stage.getFlowCells().clear();
		}
		stageList.clear();
	}

}
