package org.processmining.perspectivemining.graph.model;

import org.joda.time.Interval;

/*
 * This class represents a fragment in a process instance
 */
public class AbstractFragment extends Fragment {
	
	public AbstractFragment(ProcessObject object) {
		super(object);
	}
	
	public EventRow getStartEvent() {
		if (!this.isEmpty()) {
			return this.get(0);
		}
		else {
			return null;
		}
	}
	
	public EventRow getEndEvent() {
		if (!this.isEmpty()) {
			return this.get(this.size()-1);
		}
		else {
			return null;
		}
	}
	
	public long getStartTime() throws Exception {
		if (this.getStartEvent() != null) {
			return this.getStartEvent().getTime();
		}
		else {
			return -1;
		}
	}
	
	public long getEndTime() throws Exception {
		if (this.getEndEvent() != null) {
			return this.getEndEvent().getTime();
		}
		else {
			return -1;
		}
	}
	
	/*
	 * Split this fragment into subfragments if its events
	 * are present in the other fragment. There can be events
	 * not present in the middle of this fragment, thus the returning
	 * result is a list of subfragments.
	 * 
	 * NOTE: the order of events in a fragment must preserve the order of events in the case.
	 * Assume that the events in this fragment has already been in timestamp order, the resulting
	 * fragment list will preserve that order.
	 * DO NOT apply any sort on events in the resulting fragment
	 */
//	public List<AbstractFragment> split(Fragment other) throws Exception {
//		List<AbstractFragment> splitFragments = new ArrayList<>();
//		
//		// Non-overlapping
//		if (this.getEndTime() < other.getStartTime() || this.getStartTime() > other.getEndTime()) {
//			return splitFragments;
//		}
//		
//		AbstractFragment splitFragment = null;
//		for (EventRow event : this) {
//			if (other.contains(event)) {
//				if (splitFragment == null) {
//					splitFragment = new AbstractFragment(other.getObjectInstance());
//				}
//				splitFragment.add(event);
//				
//				if (event == this.get(this.size()-1)) {
//					splitFragments.add(splitFragment);
//				}
//			}
//			else {
//				if (splitFragment != null) {
//					splitFragments.add(splitFragment);
//					splitFragment = null;
//				}
//			}
//		}
//		
//		return splitFragments;
//	}
	
	/*
	 * Return null if the fragment has no overlapping with the window
	 * or return the overlapping fragment.
	 * Should not convert start and end timestamp to Interval
	 * because events in the fragment may have problem of equal timestamps
	 * NOTE: the order of events in a fragment must preserve the order of events in the case.
	 * DO NOT apply any sort on events in the resulting fragment
	 */
	public AbstractFragment getSubFragmentInWindow(Interval window) throws Exception {
		AbstractFragment windowFragment = new AbstractFragment(this.getProcessObject());	
		for (EventRow event : this) {
			if (window.contains(event.getTime())) {
				windowFragment.add(event);
			}
		}
		
		if (windowFragment.isEmpty()) {
			return null;
		}
		else {
			//windowFragment.sort(); // NO SORT
			return windowFragment;
		}
	}
}
