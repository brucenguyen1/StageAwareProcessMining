package org.processmining.sapm.perspectivemining.ui.forms;

import java.util.Calendar;
import java.util.Date;

import org.jfree.data.time.RegularTimePeriod;

public class CustomDayPeriod extends RegularTimePeriod{
	private long startMillisecond = 0;
	private long numberOfDays = 1;
	
	private Date firstDate = null;
	
	private Date lastDate = null;

    /** The second. */
    private int second;
	
    public CustomDayPeriod(long startMillisecond, long numberOfDays) {
    	this.startMillisecond = startMillisecond;
    	this.numberOfDays = numberOfDays;
    	this.firstDate = new Date(startMillisecond);
    	this.lastDate = new Date(startMillisecond);
    	
        peg(Calendar.getInstance());
    }

	public int compareTo(Object o1) {
	       int result;
	        long difference;

	        // CASE 1 : Comparing to another Second object
	        // -------------------------------------------
	        if (o1 instanceof CustomDayPeriod) {
	        	CustomDayPeriod t1 = (CustomDayPeriod) o1;
	            difference = this.startMillisecond - t1.startMillisecond;
	            if (difference > 0) {
	                result = 1;
	            }
	            else {
	                if (difference < 0) {
	                   result = -1;
	                }
	                else {
	                    result = 0;
	                }
	            }
	        }

	        // CASE 2 : Comparing to another TimePeriod object
	        // -----------------------------------------------
	        else if (o1 instanceof RegularTimePeriod) {
	            // more difficult case - evaluate later...
	            result = 0;
	        }

	        // CASE 3 : Comparing to a non-TimePeriod object
	        // ---------------------------------------------
	        else {
	            // consider time periods to be ordered after general objects
	            result = 1;
	        }

	        return result;
	}

	public long getFirstMillisecond() {
		return this.startMillisecond;
	}

	public long getFirstMillisecond(Calendar newCalendar) {
//		Calendar cal = Calendar.getInstance();
		newCalendar.setTime(this.firstDate);
		return newCalendar.getTimeInMillis();
//		int year = cal.get(Calendar.YEAR);
//		int month = cal.get(Calendar.MONTH);
//		int day = cal.get(Calendar.DAY_OF_MONTH);
	}

	public long getLastMillisecond() {
		return this.startMillisecond + this.numberOfDays*24*3600*1000 - 1;
	}

	public long getLastMillisecond(Calendar newCalendar) {
		newCalendar.setTime(this.lastDate);
		return newCalendar.getTimeInMillis();
	}

	public long getSerialIndex() {
		return this.startMillisecond;
	}

	public RegularTimePeriod next() {
		long newStartTime = this.startMillisecond + numberOfDays*24*3600*1000;
		return new CustomDayPeriod(newStartTime, this.numberOfDays);
	}

	public void peg(Calendar arg0) {
		// do nothing
		
	}

	public RegularTimePeriod previous() {
		long newStartTime = this.startMillisecond - numberOfDays*24*3600*1000;
		return new CustomDayPeriod(newStartTime, this.numberOfDays);
	}

}
