package org.processmining.sapm.processdiscovery.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.processmining.sapm.processdiscovery.evaluation.Evaluator;

import com.google.common.util.concurrent.UncheckedTimeoutException;

public class GenetUtils {
	public static final String GENETDIR = System.getProperty("user.dir") + File.separator + "genet" + File.separator + "bin";
	public static void genet(String sgFile, String gFile) throws Exception {
		// calling genet from here
			// Construct path and command line.
//			String line = null, path = null;
			// Run genet
//			line = GENETDIR + File.separator + "genet.exe -pm -rec " + sgFilename + " > " + sgFilename + ".g";
//			System.out.println("Command to execute:" + line);

			// Run genet.
//			Process process = Runtime.getRuntime().exec(cmdLine);
//			process.waitFor();
			
//			CommandLine cmdLine = CommandLine.parse(line);
//			DefaultExecutor executor = new DefaultExecutor();
//			ExecuteWatchdog watchdog = new ExecuteWatchdog(120000);
//			executor.setWatchdog(watchdog);
//			int exitValue = executor.execute(cmdLine);
//			System.out.println("Exit value = " + exitValue);
			
		    ProcessBuilder builder = new ProcessBuilder(GENETDIR + File.separator + "genet.exe", "-pm","-rec",sgFile);
		    builder.directory(new File(GENETDIR));
		    final Process process = builder.start();
		    process.waitFor(Evaluator.TIMEOUT, TimeUnit.SECONDS);
		    if (process.isAlive()) {
		    	process.destroyForcibly();
		    	throw new UncheckedTimeoutException("Program not terminated! Time-out after " + Evaluator.TIMEOUT + " seconds!");
		    }
		    else {
			    //Write output stream to file
			    InputStream is = process.getInputStream();
			    InputStreamReader isr = new InputStreamReader(is);
			    BufferedReader br = new BufferedReader(isr);
			    String line;
			    FileWriter fileWriter = new FileWriter(gFile);
			    boolean writeToFile = false;
			    while ((line = br.readLine()) != null) {
			    	if (line.contains(".outputs") && !writeToFile) writeToFile = true;
			    	if (writeToFile) fileWriter.write(line + "\n");
			    	//System.out.println(line);
			    }
			    fileWriter.flush();
			    fileWriter.close();
			    
			    //Error stream
			    InputStream es = process.getErrorStream();
			    InputStreamReader esr = new InputStreamReader(es);
			    BufferedReader er = new BufferedReader(esr);
			    while ((line = er.readLine()) != null) {
			    	System.out.println(line);
			    }
			    
			    System.out.println("Program terminated!");
		    }
	}
}
