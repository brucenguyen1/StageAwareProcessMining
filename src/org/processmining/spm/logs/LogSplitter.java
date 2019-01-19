package org.processmining.spm.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.spm.fakecontext.FakePluginContext;
import org.processmining.spm.utils.LogUtils;
import org.processmining.spm.utils.OpenLogFilePlugin;

public class LogSplitter {
	private Random random = null;
	
	/**
	 * 1st argument: log file
	 * 2nd argument: percent of testing log
	 * 3rd argument: number of folds
	 * @param args
	 */
	public static void main(String[] args) {
		// Import Log
		System.out.println("Import log file");
		OpenLogFilePlugin logImporter = new OpenLogFilePlugin();
		try {
			XLog log = (XLog)logImporter.importFile(new FakePluginContext(), new File(System.getProperty("user.dir") + File.separator + args[0]));
			double splitPercent = Double.valueOf(args[1]);
			int fold = Integer.valueOf(args[2]);
			LogSplitter splitter = new LogSplitter();
			splitter.split(log, splitPercent, fold);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void seed() {
		random = new Random();
	}
	
	/**
	 * 
	 * @param log: original log
	 * @param splitPercent: e.g. 20%, one sublog is 80%, one is 20%
	 * @param fold: number of folds
	 * @throws IOException 
	 */
	public void split(XLog log, double splitPercent, int fold) throws IOException {
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		int TOTAL_TRACES = summary.getNumberOfTraces();
		System.out.println("Total traces: " + TOTAL_TRACES);
		for (int i=0;i<fold;i++) {
			System.out.println("Split log for fold " + i);
			List<XLog> pair = this.splitOnce(log, splitPercent, TOTAL_TRACES);
			System.out.println("Write logs to files for fold " + i);
			if (pair != null) this.writeToFiles(pair.get(0), pair.get(1), i);
		}
		System.out.println("DONE!");
	}
	
	public List<XLog> splitOnce(XLog log, double splitPercent, int TOTAL_TRACES) {
		this.seed(); //new random seed for every fold
		
		List<Integer> traceIndexes = new ArrayList<>();
		for (int i=0;i<TOTAL_TRACES;i++) {
			traceIndexes.add(Integer.valueOf(i));
		}
		
		if (traceIndexes.isEmpty()) return null;
		
		XFactory factory = new XFactoryNaiveImpl();
		XLog testLog = factory.createLog(log.getAttributes());
		System.out.print("Selected testing indexes: ");
		while (1.0*testLog.size()/TOTAL_TRACES < splitPercent) {
			int randomIndex = this.random(0, (traceIndexes.size()-1));
			testLog.add(log.get(traceIndexes.get(randomIndex)));
			traceIndexes.remove(randomIndex);
			System.out.print(randomIndex + ",");
		}
		System.out.println();
		System.out.println("Training traces: " + traceIndexes.size() + 
							". Test traces: " + (TOTAL_TRACES-traceIndexes.size()));
		
		XLog trainLog = factory.createLog(log.getAttributes());
		for (int i=0;i<traceIndexes.size();i++) {
			trainLog.add(log.get(traceIndexes.get(i)));
		}
		
		List<XLog> logs = new ArrayList<>();
		logs.add(trainLog);
		logs.add(testLog);
		
		return logs;
	}
	
	public void writeToFiles(XLog trainLog, XLog testLog, int fold) throws IOException {
		XesXmlSerializer writer = new XesXmlGZIPSerializer();
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + 
        							LogUtils.getConceptName(trainLog) + "_train_" + fold + ".xes.gz");
        writer.serialize(trainLog, fos);
        fos.close();
        
        fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + 
									LogUtils.getConceptName(testLog) + "_test_" + fold + ".xes.gz");
		writer.serialize(testLog, fos);
		fos.close();
	}
	
	private int random(int min, int max) {
		return random.nextInt(max + 1 - min) + min;
	}
}
