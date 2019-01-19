package org.processmining.perspectivemining.datasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.processmining.perspectivemining.utils.LogUtils;

/**
 * Represent one window extracted from an event log
 * Contains multiple EventRow each representing an event.
 * It does not store many EventRow objects for not to repeat the
 * column names for every EventRow.
 * @author Bruce
 *
 */
public class WindowRowSet {
	// list of attribute names, each corresponding to an object in a row of rows
	private List<String> colNames = null;
	// list of rows, each is an event
	private List<List<Object>> rows = null;
	
	private double minTimestamp = Double.POSITIVE_INFINITY;
	private double maxTimestamp = 0.0;
	
	public WindowRowSet() {
		colNames = new ArrayList<>();
		rows = new ArrayList<>();
	}
	
	public long numberOfRows() {
		return rows.size();
	}
	
	public long numberOfColumns() {
		return colNames.size();
	}
	
	public double getMinTimestamp() {
		return minTimestamp;
	}
	
	public void setMinTimestamp(double minTimestamp) {
		this.minTimestamp = minTimestamp;
	}
	
	public double getMaxTimestamp() {
		return maxTimestamp;
	}
	
	public void setMaxTimestamp(double maxTimestamp) {
		this.maxTimestamp = maxTimestamp;
	}
	
//	public List<String> getUnmodifiableColumnNames() {
//		return Collections.unmodifiableList(colNames);
//	}
	
	public void setColumnNames(List<String> colNames) {
		this.colNames = colNames;
	}
	
	public void addEvent(List<Object> event) {
		this.rows.add(event);
	}
	
	public void removeEvent(List<Object> event) {
		this.rows.remove(event);
	}
		
	public void addXEvent(XEvent event) {
		// column names
		if (colNames.isEmpty()) {
			Iterator<String> iterator = event.getAttributes().keySet().iterator();
		    while (iterator.hasNext()) {
		    	colNames.add(iterator.next());
		    }
		}
	    
	    // column values
    	List<Object> row = new ArrayList<Object>();
		for (String colName: colNames) {
		    Object val = LogUtils.getValue(event.getAttributes().get(colName));
		    row.add(val);
		}
		rows.add(row);
		  
	    //check timestamp
	    long currentTimestamp = LogUtils.getTimestamp(event).toInstant().getMillis();
	    if ( currentTimestamp > maxTimestamp) maxTimestamp = currentTimestamp;
	    if (currentTimestamp < minTimestamp) minTimestamp = currentTimestamp;		
	}
	
//	private EventRow getEvent(int rowIndex) throws Exception {
//		if (rowIndex <0 || rowIndex >= rows.size()) {
//			throw new Exception("Cannot find an event at index = " + rowIndex);
//		}
//		else {
//			return new EventRow(colNames, rows.get(rowIndex));
//		}
//	}
//	
//	/*
//	 * Get all event rows that contain a ref. column and ref. value
//	 */
//	public Set<EventRow> getEvents(String refColName, String refColValue) throws Exception {
//		Set<EventRow> result = new HashSet<>();
//		for (int i=0;i<this.numberOfRows();i++) {
//			EventRow event = this.getEvent(i);
//			if (event.getValue(refColName).toString().equalsIgnoreCase(refColValue)) {
//				result.add(event);
//			}
//		}
//		return result;
//	}

}
