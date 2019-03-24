package org.processmining.sapm.perspectivemining.ui.timeseries;

import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;

public class MilliDTSC extends DynamicTimeSeriesCollection{
	public MilliDTSC(int nSeries, int nMoments, RegularTimePeriod timeSample) {
        super(nSeries, nMoments, timeSample);
        if (timeSample instanceof Millisecond) {
            this.pointsInTime = new Millisecond[nMoments];
        }
    }
}
