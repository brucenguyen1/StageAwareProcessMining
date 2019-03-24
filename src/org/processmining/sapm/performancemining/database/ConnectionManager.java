package org.processmining.sapm.performancemining.database;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/**
 * 
 * @author Administrator
 */
public class ConnectionManager {

	public static int CUM_TYPE_ARRIVAL = 1;
	public static int CUM_TYPE_DEPARTURE = 2;
	public static int CUM_TYPE_EXIT = 3;

	private static String driverName = "";
	private static String conURL = "";
	private static String username = "";
	private static String password = "";
	private static Connection con;
	private static ConnectionManager me = null;

	//------------------------------------------
	// Hashmap to store in-memory case movement data
	// These are all types of movement (real and fake)
	//------------------------------------------
	//key:stage,value:list of activity intervals for every activity in a stage
	public static Map<String, List<Interval>> stageActivityIntervalMap = new HashMap<String, List<Interval>>();

	//key:stage,value:list of stage intervals for all cases at a stage (from start to end of service) 
	//For one stage (both fake and actual), it looks like this, each bar is for a case
	// |------------|
	//       |-----------------|
	//            |--------------------|     
	public static Map<String, List<Interval>> stageServiceIntervalMap = new HashMap<String, List<Interval>>();
	
	//first key: case exit status
	//second key:stage,value:list of last stage intervals for all incomplete cases at a stage (from start to end of service)
	//For one exit type, one stage, it looks like this, each bar is for a case
	// |------------|
	//       |-----------------|
	//            |--------------------|  
	public static Map<String, Map<String, List<Interval>>> stageServiceIntervalExitMap = new HashMap<>();

	//key:stage,value:list of intervals for every case at a stage (from queue to end of service)
	public static Map<String, List<Interval>> stageTotalIntervalMap = new HashMap<String, List<Interval>>();
	
	//------------------------------------------
	// Hashmap to store in-memory case movement data
	// These are fake type of movement only relating to
	// inserted events with name = 'fake_event' 
	//------------------------------------------
	public static Map<String, List<Interval>> fakeStageActivityIntervalMap = new HashMap<String, List<Interval>>();
	public static Map<String, List<Interval>> fakeStageServiceIntervalMap = new HashMap<String, List<Interval>>();
	public static Map<String, List<Interval>> fakeStageTotalIntervalMap = new HashMap<String, List<Interval>>();	

	public static ConnectionManager getInstance() {
		if (me == null) {
			me = new ConnectionManager();
		}
		return me;
	}

	public static void close() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void initConnectionProperties(Map<String, String> conParams) {
		driverName = conParams.get("DBDriverName");
		conURL = conParams.get("DBConnectionURL");
		username = conParams.get("DBUsername");
		password = conParams.get("DBPassword");

		if ((conParams.get("DBDriverName") != null) && (conParams.get("DBConnectionURL") != null)
				&& (conParams.get("DBUsername") != null) && (conParams.get("DBPassword") != null)) {
			driverName = conParams.get("DBDriverName");
			conURL = conParams.get("DBConnectionURL");
			username = conParams.get("DBUsername");
			password = conParams.get("DBPassword");
		} else {
			System.out.println("Initialisation failed for DB Connection Parameters");
		}
	}

	public static Connection getConnection() throws ClassNotFoundException, SQLException {

		if (con != null) {
			return con;
		}

		try {
			Class.forName(driverName);
			try {
				System.out.println("jdbc.driver: " + driverName);
				System.out.println("jdbc.url: " + conURL);
				System.out.println("jdbc.username: " + username);
				System.out.println("jdbc.password: " + password);

				con = DriverManager.getConnection(conURL, username, password);
				con.setAutoCommit(false);
				return con;
			} catch (SQLException ex) {
				// log an exception. fro example:
				//                System.out.println("Failed to create the database connection.");
				throw ex;
			}
		} catch (ClassNotFoundException ex) {
			// log an exception. for example:
			//            System.out.println("Driver not found."); 
			throw ex;
		}

	}

	/*
	 * Initialize parameters from an XML file This file is located in the same
	 * folder as the root folder of this application
	 */
	public static void initParametersFromFile() {
		// Read database connection properties
		DBConnectionParamReader paramReader;
		try {
			paramReader = new DBConnectionParamReader(System.getProperty("user.dir") + "\\properties.xml");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage());
			return;
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			return;
		}

		Map<String, String> mapDBParams = new Hashtable<String, String>();
		mapDBParams.put("DBDriverName", paramReader.getDriverName());
		mapDBParams.put("DBConnectionURL", paramReader.getUrl());
		mapDBParams.put("DBUsername", paramReader.getUsername());
		mapDBParams.put("DBPassword", paramReader.getPassword());
		ConnectionManager.initConnectionProperties(mapDBParams);
	}

	public static ResultSet executeSQL(String sql) throws ClassNotFoundException, SQLException {
		ResultSet rs;
		Statement stmt;

		if (con == null) {
			getConnection();
		} else if (con.isClosed()) {
			getConnection();
		}

		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = stmt.executeQuery(sql);

		return rs;
	}

	public static void executeStatement(String sql) throws ClassNotFoundException, SQLException {
		Statement stmt;

		if (con == null) {
			getConnection();
		} else if (con.isClosed()) {
			getConnection();
		}

		stmt = con.createStatement();
		stmt.executeUpdate(sql);
		con.commit();
		stmt.close();

	}


	public static void insertStage(String caseID, String stage, Long queuestart, Long servicestart,
			Long servicecomplete, String status, String lastStage, String isFakeStage) throws ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		try {
			if (con == null) {
				getConnection();
			} else if (con.isClosed()) {
				getConnection();
			}

			String sql = "";
			sql += "INSERT INTO APP.STAGE(CASEID, STAGE, QUEUESTART, SERVICESTART, SERVICECOMPLETE, STATUS, LASTSTAGE, ISFAKE)";
			sql += "VALUES(?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, caseID);
			pstmt.setString(2, stage);
			pstmt.setLong(3, queuestart);
			pstmt.setLong(4, servicestart);
			pstmt.setLong(5, servicecomplete);
			pstmt.setString(6, status);
			pstmt.setString(7, lastStage);
			pstmt.setString(8, isFakeStage);

			pstmt.executeUpdate();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static void insertActivity(String caseID, String stage, String res, Long start, Long complete)
			throws ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		try {
			if (con == null) {
				getConnection();
			} else if (con.isClosed()) {
				getConnection();
			}

			String sql = "";
			sql += "INSERT INTO APP.ACTIVITY(CASEID, STAGE, RES, START, COMPLETE)";
			sql += "VALUES(?,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, caseID);
			pstmt.setString(2, stage);
			pstmt.setString(3, res);
			pstmt.setLong(4, start);
			pstmt.setLong(5, complete);

			pstmt.executeUpdate();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static int selectCumulativeCount(String stageName, DateTime timePoint, 
											int cumulativeType, String exitType,
											boolean forActualStage) throws ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int result = 0;
		try {
			if (con == null) {
				getConnection();
			} else if (con.isClosed()) {
				getConnection();
			}
			
			String sql = "SELECT count(*) as total FROM APP.STAGE ";
			if (cumulativeType == ConnectionManager.CUM_TYPE_ARRIVAL) {
				sql += "WHERE stage = ? AND servicestart <= ?";
				if (forActualStage) sql += " AND isfake = 'false'";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, stageName);
				pstmt.setLong(2, timePoint.getMillis());
			} else if (cumulativeType == ConnectionManager.CUM_TYPE_DEPARTURE) {
				sql += "WHERE stage = ? AND servicecomplete <= ?";
				if (forActualStage) sql += " AND isfake = 'false'";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, stageName);
				pstmt.setLong(2, timePoint.getMillis());
			} else if (cumulativeType == ConnectionManager.CUM_TYPE_EXIT) {
				sql += "WHERE stage = ? AND laststage = ? AND servicecomplete <= ? AND STATUS = '" + exitType + "'";
				if (forActualStage) sql += " AND isfake = 'false'";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, stageName);
				pstmt.setString(2, stageName);
				pstmt.setLong(3, timePoint.getMillis());
			}

			rs = pstmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt("total");
			}
			return result;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	public static int selectCumulativeArrivalCount(String stageName, DateTime timePoint, boolean forActualStage) {
		int totalCount = 0;
		long timePointMillis = timePoint.getMillis();

		// Must check since the SPF filter may return no data for certain stages.
		if (stageServiceIntervalMap.containsKey(stageName)) {
			for (Interval interval : stageServiceIntervalMap.get(stageName)) {
				if (interval.getStartMillis() <= timePointMillis) {
					totalCount++;
				}
			}
		}
		
		if (forActualStage) {
			int fakeCount = 0;
			if (fakeStageServiceIntervalMap.containsKey(stageName)) {
				for (Interval interval : fakeStageServiceIntervalMap.get(stageName)) {
					if (interval.getStartMillis() <= timePointMillis) {
						fakeCount++;
					}
				}
			}
			totalCount = totalCount - fakeCount; 
		}
		
		return totalCount;
	}

	public static int selectCumulativeDepartureCount(String stageName, DateTime timePoint, boolean forActualStage) {
		int totalCount = 0;
		long timePointMillis = timePoint.getMillis();

		// Must check since the SPF filter may return no data for certain stages.
		if (stageServiceIntervalMap.containsKey(stageName)) {
			for (Interval interval : stageServiceIntervalMap.get(stageName)) {
				if (interval.getEndMillis() <= timePointMillis) {
					totalCount++;
				}

			}
		}
		
		if (forActualStage) {
			int fakeCount = 0;
			if (fakeStageServiceIntervalMap.containsKey(stageName)) {
				for (Interval interval : fakeStageServiceIntervalMap.get(stageName)) {
					if (interval.getEndMillis() <= timePointMillis) {
						fakeCount++;
					}
				}
			}
			totalCount = totalCount - fakeCount; 
		}
		
		return totalCount;
	}
	
	public static int selectCumulativeExitCount(String caseExitStatus, String stageName, DateTime timePoint) {
		int totalCount = 0;
		long timePointMillis = timePoint.getMillis();

		// Must check since the SPF filter may return no data for certain stages.
		if (stageServiceIntervalExitMap.containsKey(caseExitStatus) && 
				stageServiceIntervalExitMap.get(caseExitStatus).containsKey(stageName)) {
			for (Interval interval : stageServiceIntervalExitMap.get(caseExitStatus).get(stageName)) {
				if (interval.getEndMillis() <= timePointMillis) {
					totalCount++;
				}

			}
		}
		
		return totalCount;
	}

	/*
	 * This metric does not need to differentiate between actual stages and fake stages
	 * because the fake sages always have zero duration due to only one fake_event is 
	 * added to represent the fake stage.
	 */
	public static long selectCumulativeCaseTime2(String stageName, DateTime timePoint) {
		long totalTime = 0;
		long timePointMillis = timePoint.getMillis();

		// Must check since the SPF filter may return no data for certain stages.
		if (stageTotalIntervalMap.containsKey(stageName)) {
			for (Interval interval : stageTotalIntervalMap.get(stageName)) {
				if (interval.getStartMillis() < timePointMillis) {
					totalTime += (Math.min(timePointMillis, interval.getEndMillis()) - interval.getStartMillis()) / 1000;
				}
			}
		}
		
		return totalTime;
	}

	/*
	 * The same as above. This metric for actual stage has the same value as for all stages
	 * since fake stages always have zero duration.
	 */
	public static long selectCumulativeResTime2(String stageName, DateTime timePoint) {
		long totalTime = 0;
		long timePointMillis = timePoint.getMillis();

		// Must check since the SPF filter may return no data for certain stages.
		if (stageActivityIntervalMap.containsKey(stageName)) {
			for (Interval interval : stageActivityIntervalMap.get(stageName)) {
				if (interval.getStartMillis() < timePointMillis) {
					totalTime += (Math.min(timePointMillis, interval.getEndMillis()) - interval.getStartMillis()) / 1000;
				}
			}
		}
		return totalTime;
	}

}
