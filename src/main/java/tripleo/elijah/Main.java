/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

	public static void main(final String[] args) throws Exception {
		final StdErrSink errSink = new StdErrSink();
		final Compilation cc = new Compilation(errSink, new IO());
		final List<String> ls = new ArrayList<String>();
		ls.addAll(Arrays.asList(args));
		cc.feedCmdLine(ls);
	}
}

//
//
//
