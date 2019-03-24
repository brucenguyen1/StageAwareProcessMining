package org.processmining.sapm.stagemining.groundtruth;

import java.util.List;
import java.util.Set;
import org.deckfour.xes.model.XLog;

public abstract class GroundTruth {
	public abstract List<Set<String>> getGroundTruth(XLog log) throws Exception;
}
