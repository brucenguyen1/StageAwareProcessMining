package org.processmining.perspectivemining.ui.timeseries;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;

public class MultipleOfMillisecond extends Millisecond {
	private static final long serialVersionUID = 1L;
	  private int periodMs = 100;

	  public MultipleOfMillisecond(int periodMs){
	    super();
	    this.periodMs = periodMs;
	  }

	  public MultipleOfMillisecond(int periodMs, int millisecond, Second second){
	    super(millisecond, second);
	    this.periodMs = periodMs;
	  }

	  @Override
	  public RegularTimePeriod next() {

	    RegularTimePeriod result = null;
	    if(getMillisecond() + periodMs <= LAST_MILLISECOND_IN_SECOND){
	        result = new MultipleOfMillisecond( periodMs, (int)(getMillisecond() + periodMs), getSecond());
	    }else{
	        Second next = (Second)getSecond().next();
	        if(next != null){
	            result = new MultipleOfMillisecond(periodMs, (int)(getMillisecond() + periodMs - LAST_MILLISECOND_IN_SECOND - 1), next);
	        }
	    }
	    return result;

	  }

	  @Override
	  public RegularTimePeriod previous() {

	    RegularTimePeriod result = null;
	    if(getMillisecond() - periodMs >= FIRST_MILLISECOND_IN_SECOND){
	        result = new MultipleOfMillisecond(periodMs, (int)getMillisecond() - periodMs, getSecond());
	    }else{
	        Second previous = (Second)getSecond().previous();
	        if(previous != null){
	            result = new MultipleOfMillisecond(periodMs, (int)(getMillisecond() - periodMs + LAST_MILLISECOND_IN_SECOND + 1), previous);
	        }
	    }
	    return result;

	  } 
}
