package org.processmining.sapm.perspectivemining.datasource;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

public class SamplingWindowGenerator {
	/*
	 * Note that a window is half-open: inclusive of the left end and exlusive of the right end
	 * This is the way the Interval class is implemented in Joda-Time
	 * So, the window will not use the right end
	 * startTimestamp, endTimestamp: milliseconds
	 * windowSize: seconds
	 */
	public static List<Interval> generate(long startTimestamp, long endTimestamp, long windowSize) {
		List<Interval> intervals = new ArrayList<>();
		long lastBound = 0;
		for (long t=startTimestamp;t<=endTimestamp;t+=windowSize) {
			if (t+windowSize <= endTimestamp) {
				intervals.add(new Interval(t, t+windowSize));
				lastBound = t+windowSize;
			}
		}
		//Get the last interval but smaller than window size
		if (lastBound < endTimestamp) {
			intervals.add(new Interval(lastBound, endTimestamp+1)); //add +1 since an interval is exclusive of the right end
		}
		return intervals;
	}
}
