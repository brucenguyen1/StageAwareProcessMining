package org.processmining.perspectivemining.graph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;

public class ProcessAbstraction {
	private ObjectSchema absSchema = null;
	private List<ProcessInstance> instances = null;
	
	public ProcessAbstraction(ObjectSchema absSchema, PerspectiveInputObject input) throws Exception {
		this.absSchema = absSchema;
		Map<List<String>,ProcessObject> mapValueObject = new HashMap<>();
		
		this.instances = new ArrayList<>();
		for (ProcessInstance instance: input.getProcessInstances()) {
			instance.getAbstractFragments().clear(); //must clear to avoid fragments created from previous run
			AbstractFragment abstractFragment = null;
			for (EventRow event : instance.getFragment()) {
				List<String> objectValue = event.getObjectValue(absSchema);
				if (!mapValueObject.containsKey(objectValue)) {
					mapValueObject.put(objectValue, new ProcessObject(absSchema, objectValue));
				}
				
				ProcessObject object = mapValueObject.get(objectValue);
				object.getFragment().add(event);
				
				if (abstractFragment == null || abstractFragment.getProcessObject() != object) {
					abstractFragment = new AbstractFragment(object);
					instance.getAbstractFragments().add(abstractFragment);
				}
				abstractFragment.add(event);
			}
			
			this.instances.add(instance);
		}
	}
	
	public ObjectSchema getAbstractionSchema() {
		return this.absSchema;
	}
	
	public List<ProcessInstance> getProcessInstances() {
		return this.instances;
	}
	
}
