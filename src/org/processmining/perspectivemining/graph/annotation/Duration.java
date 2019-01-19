package org.processmining.perspectivemining.graph.annotation;

public class Duration {

	private static final int SECONDS = 1000;
	private static final int MINUTES = 60 * SECONDS;
	private static final int HOURS = 60 * MINUTES;
	private static final int DAYS = 24 * HOURS;

	private final long time;

	private final long miliseconds;
	private final long seconds;
	private final long minutes;
	private final long hours;
	private final long days;

	public Duration(final long time) {
		super();

		this.time = time;

		long temp = time;
		days = temp / DAYS;
		temp = temp % DAYS;

		hours = temp / HOURS;
		temp = temp % HOURS;

		minutes = temp / MINUTES;
		temp = temp % MINUTES;

		seconds = temp / SECONDS;
		temp = temp % SECONDS;

		miliseconds = temp;
	}

	public Duration(long ms, long s, long m, long h, long d) {
		super();

		time = ms + s * SECONDS + m * MINUTES + h * HOURS + d * DAYS;

		miliseconds = ms;
		seconds = s;
		minutes = m;
		hours = h;
		days = d;

	}

	public double getMiliseconds() {
		return miliseconds;
	}

	public long getSeconds() {
		return seconds;
	}

	public long getMinutes() {
		return minutes;
	}

	public long getHours() {
		return hours;
	}

	public long getDays() {
		return days;
	}

	public long getTime() {
		return time;
	}

	private String getString(long value, String symbol) {
		return (value > 0) ? (Long.toString(value) + symbol) : "";
	}

	public String toString() {
		if ((days == 0) && (hours == 0) && (minutes == 0) && (seconds == 0)) {
			return (Long.toString(miliseconds) + "ms");
		}
		String d = getString(days, "d ");
		String h = getString(hours, "h ");
		if (!d.equals("")) {
			return d + h;
		}
		String m = getString(minutes, "min ");
		if (!h.equals("")) {
			return h + m;
		}
		String s = getString(seconds, "s ");
		if (!m.equals("")) {
			return m + s;
		}
		String ms = getString(miliseconds, "ms");
		if (!s.equals("")) {
			return s + ms;
		}
		return ms;

		//return getString(days,"d ") + getString(hours, "h ") + getString(minutes, "min ") + getString(seconds,"s ") + getString(miliseconds,"ms"); 
	}

}
