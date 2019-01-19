package org.processmining.perspectivemining.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
	    put("^\\d{8}$", "yyyyMMdd");
	    
	    put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
	    put("^\\d{1,2}.\\d{1,2}.\\d{4}$", "dd.MM.yyyy");
	    put("^\\d{4}.\\d{1,2}.\\d{1,2}$", "yyyy.MM.dd");
	    put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
	    put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
	    put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
	    
	    put("^\\d{12}$", "yyyyMMddHHmm");
	    put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
	    
	    put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
	    put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
	    put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}$", "dd.MM.yyyy HH:mm");
	    put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\.\\s\\d{1,2}:\\d{2}$", "yyyy.MM.dd HH:mm");
	    put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}$", "MM.dd.yyyy HH:mm");
	    put("^\\d{4}.\\d{1,2}.\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy.MM.dd HH:mm");	    
	    put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
	    put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
	    
	    put("^\\d{14}$", "yyyyMMddHHmmss");
	    put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
	    
	    put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
	    put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd.MM.yyyy HH:mm:ss");
	    put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy.MM.dd HH:mm:ss");	    
	    put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
	    put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
	    put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
	    
	    put("^\\d{1,2}-\\d{1,2}-\\d{4}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "dd-MM-yyyy'T'HH:mm:ssZ");
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "yyyy-MM-dd'T'HH:mm:ssZ");
	    put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "dd.MM.yyyy'T'HH:mm:ssZ");
	    put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "yyyy.MM.dd'T'HH:mm:ssZ");	    
	    put("^\\d{1,2}/\\d{1,2}/\\d{4}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "MM/dd/yyyy'T'HH:mm:ssZ");
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$", "yyyy/MM/dd'T'HH:mm:ssZ");	    
	}};

	/**
	 * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
	 * format is unknown. You can simply extend DateUtil with more formats if needed.
	 * @param dateString The date string to determine the SimpleDateFormat pattern for.
	 * @return The matching SimpleDateFormat pattern, or null if format is unknown.
	 * @see SimpleDateFormat
	 */
	public static String determineDateFormat(String dateString) {
	    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
	        if (dateString.matches(regexp)) {
	            return DATE_FORMAT_REGEXPS.get(regexp);
	        }
	    }
	    return null; // Unknown format.
	}
	
	public static String ConvertSecondToHHMMString(long milisecondtTime) {
		long secondsTime = milisecondtTime / 1000;
		long miliseconds = milisecondtTime % 1000;
		long seconds = secondsTime % 60;
		long minutes = (secondsTime / 60) % 60;
		long hours = (secondsTime / 3600) % 24;
		long days = secondsTime / 86400;
		return "" + days + "d, " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":"
				+ String.format("%02d", seconds) + "." + String.format("%03d", miliseconds);
	}
}
