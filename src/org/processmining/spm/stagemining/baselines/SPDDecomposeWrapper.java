package org.processmining.spm.stagemining.baselines;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.plugins.simpleprecedencediagram.mining.SPDMiner2;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.stagemining.groundtruth.ExampleClass;
import org.processmining.spm.utils.Measure;
import org.processmining.spm.utils.OpenLogFilePlugin;

public class SPDDecomposeWrapper {
	public static void main(String[] args) {
		try {
			OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
			System.out.println("Import log file");
			String filename = "BPIC12_complete_events_only.xes.gz";
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + "\\logs\\" + filename));
			SPDMiner2 spd = new SPDMiner2();
			SPDDecomposeWrapper spdWrapper = new SPDDecomposeWrapper();
			ExampleClass example = (ExampleClass)Class.forName("org.processmining.spm.stagemining.groundtruth.StageModelBPI2012").newInstance();
			for (int numClusters=2;numClusters < 10;numClusters++) {
				SPD clusters = spd.computeClusters(new FakePluginContext(), log, numClusters);
				double gtIndex = Measure.computeMeasure(spdWrapper.getActivityLabelSets(clusters), example.getGroundTruth(log), 2);
				System.out.println("Number of clusters: " + numClusters + ". Best ground-truth index: " + gtIndex);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private List<Set<String>> getActivityLabelSets(SPD spd) {
		List<Set<String>> labelSetList = new ArrayList<>();
		for (SPDNode node: spd.getNodes()) {
			Set<String> activitySet = getActivitySet(node);
			labelSetList.add(activitySet);
		}
		return labelSetList;
	}
	
	private Set<String> getActivitySet(SPDNode node) {
		
		String nodeString = node.getLabel();
		String newNodeString = nodeString.substring(6); //remove <html>, e.g. A<br>B<br>C</html>
		newNodeString = newNodeString.substring(0, newNodeString.length() - 7); //remove </html>, e.g. A<br>B<br>C
		String[] activityArray = newNodeString.split("<br>"); // return [A,B,C]
		
		Set<String> activitySet = new HashSet<>();
		for (int i=0;i<activityArray.length;i++) {
			activitySet.add(activityArray[i]);
		}
		return activitySet;
	}
}
