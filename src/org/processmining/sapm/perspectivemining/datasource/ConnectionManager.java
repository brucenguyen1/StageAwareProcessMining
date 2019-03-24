package org.processmining.sapm.perspectivemining.datasource;

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
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.processmining.sapm.perspectivemining.log.attribute.Attribute;
import org.processmining.sapm.perspectivemining.log.attribute.AttributeRow;
import org.processmining.sapm.perspectivemining.utils.DateUtils;
import org.processmining.sapm.perspectivemining.utils.LogUtils;

import com.sun.rowset.CachedRowSetImpl;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/**
 * 
 * @author Administrator
 */
public class ConnectionManager {
	private static String driverName = "";
	private static String conURL = "";
	private static String username = "";
	private static String password = "";
	private static Connection con;
	private static ConnectionManager me = null;

	public static void main(String[] args) {
		//        String str="";
		//        try {
		//            getConnection();
		//            str = ConnectionManager.executeSQL("select * from Employees");
		//            
		//        } catch (Exception ex) {
		////            ex.printStackTrace();
		//        }
		//        System.out.println(str);

	}

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
				con.setAutoCommit(true);
				return con;
			} catch (SQLException ex) {
				// log an exception. fro example:
				// System.out.println("Failed to create the database connection.");
				throw ex;
			}
		} catch (ClassNotFoundException ex) {
			// log an exception. for example:
			// System.out.println("Driver not found."); 
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

	public static CachedRowSet executeSQL(String sql) throws ClassNotFoundException, SQLException {
		Statement stmt;

		if (con == null) {
			getConnection();
		} else if (con.isClosed()) {
			getConnection();
		}

		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery(sql);
		CachedRowSetImpl crs = new CachedRowSetImpl();
		crs.populate(rs);
		
		rs.close();
		stmt.close();
		
		return crs;
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
	
	public static void insertEvent(XTrace trace, XEvent event, List<AttributeRow> attributes) throws ClassNotFoundException, SQLException {
		PreparedStatement pstmt = null;
		try {
			if (con == null) {
				getConnection();
			} else if (con.isClosed()) {
				getConnection();
			}

			String sql = "";
			sql += "INSERT INTO APP.EVENT(";
			sql += "CID,EID";
			for (AttributeRow att : attributes) {
				sql += "," + ConnectionManager.cleanColumnName(att.getName());
			}
			sql += ")";
			sql += "VALUES(";
			sql += "?"; // for CID
			sql += ",?"; // for EID
			for (int i=0;i<attributes.size();i++) {
				sql += "," + "?"; ;
			}
			sql += ")";
//			System.out.println(sql);
			
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, LogUtils.getConceptName(trace)); // traceID
			pstmt.setString(2, event.getAttributes().get("E:concept:name").toString()); // event ID
			for (int i=0;i<attributes.size();i++) {
				AttributeRow att = attributes.get(i);
				String value = "";
				if (event.getAttributes().containsKey(att.getName())) value = event.getAttributes().get(att.getName()).toString();
				if (att.getUseAs().equals(Attribute.DATE_TIME)) {
					DateTime datetimeVal;
					if (value.isEmpty()) value = "1970-01-01T00:00:00+01:00";
					
					try {
						String pattern = DateUtils.determineDateFormat(value); 
						DateTimeFormatter df = DateTimeFormat.forPattern(pattern);
						datetimeVal = df.parseDateTime(value);
					}
					catch (IllegalArgumentException e1) {
						String pattern = DateUtils.determineDateFormat("1970-01-01T00:00:00+01:00");
						DateTimeFormatter df = DateTimeFormat.forPattern(pattern);
						datetimeVal = df.parseDateTime("1970-01-01T00:00:00+01:00");
					}
					pstmt.setLong(i+3, datetimeVal.getMillis());
				}
				else if (att.getUseAs().equals(Attribute.CONTINUOUS) || att.getUseAs().equals(Attribute.DISCRETE)) {
					if (value.isEmpty()) value = "0";
					pstmt.setDouble(i+3, Double.valueOf(value));
				}
				else {
					pstmt.setString(i+3, value);
				}
			}
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
	
    public static String cleanColumnName(String oriName) {
    	String cleanName = oriName;
    	cleanName = cleanName.replace(":", "_");
    	return cleanName;
    }
    
    public static String getDataType(String useAs) {
    	if (useAs.equals(Attribute.DATE_TIME)) {
    		return "BIGINT";
    	}
    	else if (useAs.equals(Attribute.CONTINUOUS) || useAs.equals(Attribute.DISCRETE)) {
    		return "DOUBLE";
    	}
    	else {
    		return "VARCHAR(200)";
    	}
    }
    
    public static boolean compare(CachedRowSet rs, int index, String tupleValue, String useAs) {
    	try {
	    	if (useAs.equals(Attribute.DATE_TIME)) {
	    		String pattern = DateUtils.determineDateFormat(tupleValue); 
				DateTimeFormatter df = DateTimeFormat.forPattern(pattern);
				Date dateValue = df.parseDateTime(tupleValue).toDate();
	    		return rs.getDate(index).equals(dateValue);
	    	}
	    	else if (useAs.equals(Attribute.CONTINUOUS) || useAs.equals(Attribute.DISCRETE)) {
	    		return (rs.getDouble(index) == Double.valueOf(tupleValue));
	    	}
	    	else {
	    		return (rs.getString(index).equals(tupleValue));
	    	}
    	}
    	catch (SQLException ex) {
    		return false;
    	}
    	catch (NumberFormatException ex) {
    		return false;
    	}
    }
}
