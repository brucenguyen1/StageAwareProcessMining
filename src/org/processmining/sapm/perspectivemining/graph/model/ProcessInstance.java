package org.processmining.sapm.perspectivemining.graph.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

/**
 * ProcessInstance represents an abstraction of a case 
 * Note that fragments within one process instance has been sorted
 * by timestamp. So, their relation is based on their order in the list
 * of fragments.
 * @author Bruce
 *
 */
public class ProcessInstance extends ProcessObject {
	private List<AbstractFragment> abstractFragments = null;
	
	public ProcessInstance(ObjectSchema schema, List<String> instanceValue) {
		super(schema, instanceValue);
		abstractFragments = new ArrayList<>();
	}

	
//	public ProcessInstance(ProcessObject objectInstance) {
//		super(objectInstance.getSchema(), objectInstance.getValue());
//		this.setFragment(objectInstance.getFragment());
//		this.abstractFragments = new ArrayList<>();
//	}
	
	/*
	 * Split this fragment based on a reference list of fragments
	 * The events of this fragment are assigned to a new fragment
	 * every time there is a change of containing fragment between consecutive events
	 * Note: the list of abstract fragments created must preserve the order of the events in 
	 * the process instance (i.e. in the fragment of process instance)
	 */
//	public void createAbstraction(Set<ProcessObject> abstractSchemaObjects) throws Exception {
//		this.abstractFragments.clear(); //remember to clear abstract fragments created from the previous abstraction
//		AbstractFragment currentFragment = null;
//		for (EventRow event : this.getFragment()) {
//			for (ProcessObject object: abstractSchemaObjects) {
//				if (object.getFragment().contains(event)) {
//					if (currentFragment == null || object != currentFragment.getProcessObject()) {
//						currentFragment = new AbstractFragment(object);
//						currentFragment.add(event);
//						abstractFragments.add(currentFragment);
//						break;
//					}
//					else {
//						currentFragment.add(event);
//					}
//				}
//			}
//		}
//	}
//	public void createAbstraction(Set<ObjectInstance> abstractSchemaObjects) throws Exception {
//		this.abstractFragments.clear(); //remember to clear abstract fragments created from the previous abstraction 
//		for (ObjectInstance object : abstractSchemaObjects) {
//			abstractFragments.addAll(this.getFragment().split(object.getFragment()));
//		}
//		
//		Collections.sort(abstractFragments, new FragmentComparatorByOrderType(FragmentOrderType.START_TO_START));
//	}
	
	public List<AbstractFragment> getAbstractFragments() {
		return this.abstractFragments;
	}
	
	/*
	 * Note that the fragment list of instance has been sorted by timestamp
	 * So, the extracted list of fragments for a window is also sorted.
	 * Empty list of fragments is returned if the instance has no overlapping with the window.
	 * Note: the order of the resulting fragment list must be the same order
	 * as the full abstract fragments in this process instance.
	 */
	public List<AbstractFragment> getSubFragmentsInWindow(Interval window) throws Exception {
		List<AbstractFragment> windowFragments = new ArrayList<>();
		for (AbstractFragment fragment : this.abstractFragments) {
			AbstractFragment windowFragment = fragment.getSubFragmentInWindow(window);
			if (windowFragment != null) {
				windowFragments.add(windowFragment);
			}
		}
//		if (windowFragments.size() >= 2) {
//			Collections.sort(windowFragments, new FragmentComparatorByOrderType(FragmentOrderType.START_TO_START));
//		}
		return windowFragments;
	}
}
