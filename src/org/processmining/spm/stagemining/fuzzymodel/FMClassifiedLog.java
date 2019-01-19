package org.processmining.spm.stagemining.fuzzymodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;

public class FMClassifiedLog {

	private XLog log;
	private XEventClassifier classifier;
	private XLogInfo logInfo;
	
	private Map<String, Integer> classIndexMap;
	private List<String> classList;
	
	public FMClassifiedLog(XLog log) {
		this.log = log;
		this.classifier = new XEventNameClassifier();
		init();
	}

	public FMClassifiedLog(XLog log, XEventClassifier classifier) {
		this.log = log;
		this.classifier = classifier;
		init();
	}
	
	private void init() {
		logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		classIndexMap = new HashMap<String, Integer>();
		classList = new ArrayList<String>();
		for (XEventClass eventClass: logInfo.getEventClasses().getClasses()) {
			classIndexMap.put(eventClass.getId(), classList.size());
			classList.add(eventClass.getId());
		}
	}
}
