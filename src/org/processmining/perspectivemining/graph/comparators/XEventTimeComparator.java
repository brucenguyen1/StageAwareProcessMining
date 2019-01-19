package org.processmining.perspectivemining.graph.comparators;

import java.util.Comparator;

import org.deckfour.xes.model.XEvent;
import org.processmining.perspectivemining.utils.LogUtils;

public class XEventTimeComparator implements Comparator<XEvent> {
	  @Override
	  public int compare(XEvent x, XEvent y) {
		  long xTime = LogUtils.getTimestamp(x).getMillis();
		  long yTime = LogUtils.getTimestamp(y).getMillis();
		  if (xTime < yTime) {
			  return -1;
		  }
		  else if (xTime > yTime) {
			  return +1;
		  }
		  else {
			  return 0;
		  }
	  }
}