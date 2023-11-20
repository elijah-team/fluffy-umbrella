/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/3/21 3:46 AM
 */
public class ElLog {
	private final String fileName;
	private final Verbosity verbose;
	private final String phase;
	private final List<LogEntry> entries = new ArrayList<>();

	public enum Verbosity {
		SILENT, VERBOSE
	}

	public ElLog(final String aFileName, final Verbosity aVerbose, final String aPhase) {
		fileName = aFileName;
		verbose = aVerbose;
		phase = aPhase;
	}

	public void err(final String aMessage) {
		final long time = System.currentTimeMillis();
		entries.add(new LogEntry(time, LogEntry.Level.ERROR, aMessage));
		if (verbose == Verbosity.VERBOSE)
			System.err.println(aMessage);
	}

	public void info(final String aMessage) {
		final long time = System.currentTimeMillis();
		entries.add(new LogEntry(time, LogEntry.Level.INFO, aMessage));
		if (verbose == Verbosity.VERBOSE)
			System.out.println(aMessage);
	}

	public String getFileName() {
		return fileName;
	}

	public List<LogEntry> getEntries() {
		return entries;
	}

	public String getPhase() {
		return phase;
	}
}

//
//
//
