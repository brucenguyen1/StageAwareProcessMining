package org.processmining.sapm.perspectivemining.graph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deckfour.xes.model.impl.XAttributeTimestampImpl;


/**
 * Represent one event in the flatten event log where 
 * all attributes are flatten to event level. Thus, an event row
 * contains all attributes
 * @author Bruce
 *
 */
public class EventRow {
	private List<String> colNames = null;
	private List<Object> row = null;
	
	public static final String COL_TIMESTAMP = "time:timestamp";
	public static final String COL_CASE_CLASSIFIER = "C:concept:name";
	public static final String COL_EVENT_CLASSIFIER = "concept:name";
	public static final String COL_EVENT_ID = "eventid"; //unique eventid in the whole log
	public static final String COL_LIFECYCLE = "lifecycle:transition";
	public static final String COL_LIFECYCLE_START = "start";
	public static final String COL_LIFECYCLE_COMPLETE = "complete";
	
	public static final String COL_DELIMITER = "@"; //values of event attributes must not contain the delimiter
	
	public EventRow(List<String> colNames, List<Object> row) {
		this.colNames = colNames;
		this.row = row;
	}
	
	public List<String> getColumnNames() {
		return this.colNames;
	}
	
	public List<Object> getUnmodifiableRow() {
		return Collections.unmodifiableList(this.row);
	}
 	
	public Object getValue(String colName) throws Exception {
		if (!colNames.contains(colName)) {
			throw new Exception("Event does not contain attribute with name=" + colName);
		}
		
		int colIndex = colNames.indexOf(colName);
		if (colIndex <0 || colIndex >= row.size()) {
			throw new Exception("Column index " + colIndex + " is out of bounds on rows!");
		}
		else {
			return row.get(colIndex);
		}
	}
	
	public String getValue(List<String> colNames) throws Exception {
		String compositeValue = "";
		for (String colName : colNames) {
			Object colValue = this.getValue(colName);
			if (compositeValue.equals("")) {
				compositeValue = colValue.toString(); 
			}
			else {
				compositeValue += EventRow.COL_DELIMITER + colValue.toString();
			}
			
		}
		return compositeValue;
	}
	
	public List<String> getObjectValue(ObjectSchema objectSchema) throws Exception {
		List<String> listValue = new ArrayList<>();
		List<String> colNames = objectSchema.getAttributeNames();
		for (String colName : colNames) {
			listValue.add(this.getValue(colName).toString());
		}
		return listValue;
	}
	
//	public ObjectInstance project(ObjectSchema objectSchema) throws Exception {
//		ObjectInstance object = new ObjectInstance(objectSchema, this.getValue(objectSchema));
//		object.getFragment().add(this);
//		return object;
//	}
	
	public Object getValue(int colIndex) throws Exception {
		if (colIndex <0 || colIndex >= row.size()) {
			throw new Exception("Column index " + colIndex + " is out of bounds on rows!");
		}
		else {
			return row.get(colIndex);
		}
	}
	
	public long getTime() throws Exception {
		long time = ((XAttributeTimestampImpl)this.getValue(COL_TIMESTAMP)).getValueMillis();
		return time;
	}
	
	@Override
	public int hashCode() {
	    try {
			return this.getValue(EventRow.COL_EVENT_ID).hashCode();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventRow) {
			EventRow otherEvent = (EventRow)obj;
			try {
				if (this.getValue(EventRow.COL_EVENT_ID).equals(otherEvent.getValue(EventRow.COL_EVENT_ID))) {
					return true;
				}
				else {
					return false;
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		else {
			return false;
		}
	}
}
