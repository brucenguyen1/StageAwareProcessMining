package org.processmining.perspectivemining.graph.model;

public class ObjectUtils {
	public static ProcessObject project(ObjectSchema objectSchema, EventRow event) throws Exception {
		ProcessObject object = new ProcessObject(objectSchema, event.getObjectValue(objectSchema));
		return object;
	}
	
	/*
	 * Create abstract objects. Each object contains all events in the log that share
	 * the same object value. So, objects must have different events, and union of all
	 * objects must be the same as the log.
	 */
//	public static Set<ProcessObject> project(ObjectSchema objectSchema, Collection<EventRow> events) throws Exception {
//		Set<ProcessObject> objects = new HashSet<>();
//		
//		// Create objects, equal objects will be added once only (see ObjectInstance.equal())
//		for (EventRow event : events) {
//			objects.add(ObjectUtils.project(objectSchema, event));
//		}
//		
//		// Now, add events to fragments of each object
//		for (ProcessObject object : objects) {
//			for (EventRow e : events) {
//				if (object.getValue().equals(e.getValue(objectSchema))) {
//					object.getFragment().add(e);
//				}
//			}
////			object.getFragment().sort(); //must be sorted, later fragment.getStartEvent, getEndEvent assume it has been sorted.
//		}
//			
//		return objects;
//	}
}
