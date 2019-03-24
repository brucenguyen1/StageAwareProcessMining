package org.processmining.sapm.filter;

public class LogFilterParams implements FilterParams {
	public double[] getMinMaxStep(String logName) throws Exception {
		double[] params = new double[3];
		if (logName.equals("bpi12")) {
			params[0] = 0.005; //min
			params[1] = 0.01; //max
			params[2] = 0.005; //step
		}
		else if (logName.equals("bpi13")) {
			params[0] = 0.01; //min
			params[1] = 0.1; //max
			params[2] = 0.01; //step
		}
		else if (logName.equals("bpi15-1") || logName.equals("bpi15-2") || logName.equals("bpi15-3")) {
			params[0] = 0.01; //min
			params[1] = 0.2; //max
			params[2] = 0.01; //step
		}
		else if (logName.equals("bpi15-4") || logName.equals("bpi15-5")) {
			params[0] = 0.01; //min
			params[1] = 0.2; //max
			params[2] = 0.01; //step
		}
		else if (logName.equals("bpi17")) {
			params[0] = 0.01; //min
			params[1] = 0.1; //max
			params[2] = 0.01; //step
		}
		else {
			throw new Exception("Unknown log file dudring applying global filtering and adding start/end events.");
		}
		return params;
	}
}
