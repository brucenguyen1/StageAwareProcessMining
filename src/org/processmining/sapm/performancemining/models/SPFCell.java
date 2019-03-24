package org.processmining.sapm.performancemining.models;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class SPFCell {
	private final DateTime timePoint;
	Map<String, Double> characteristicMap = new HashMap<String, Double>();

	public SPFCell(DateTime timePoint) {
		this.timePoint = timePoint;
	}

	public DateTime getTimePoint() {
		return timePoint;
	}

	public Double getCharacteristic(String characteristicCode) {
		if (characteristicMap.containsKey(characteristicCode)) {
			return characteristicMap.get(characteristicCode);
		} else {
			return 0.0;
		}
	}

	public void setCharacteristic(String characteristicCode, Double newValue) {
		characteristicMap.put(characteristicCode, newValue);
	}

	public void clear() {
		characteristicMap.clear();
	}
}
