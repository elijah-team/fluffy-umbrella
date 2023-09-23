/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.ParserClosure;

public class Out {

//	private final Compilation compilation;
//	private boolean do_out = false;

	public static void println(final String s) {
		tripleo.elijah.util.Stupidity.println2(s);
	}

/*
	private static TabbedOutputStream getTOSLog() throws FileNotFoundException {
		final @NotNull SimpleDateFormat sdf      = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		final String                    filename = String.format("eljc-%s.out", sdf.format(new Date()));
		return new TabbedOutputStream(new FileOutputStream(filename));
	}
*/

	private final ParserClosure pc;

	public Out(final String fn, final Compilation compilation, final boolean do_out) {
		pc = new ParserClosure(fn, compilation);
//		this.compilation = compilation;
//		this.do_out = do_out;
	}

	public ParserClosure closure() {
		return pc;
	}

	//@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NM_METHOD_NAMING_CONVENTION")
	public void FinishModule() {
/*
		final TabbedOutputStream tos;
		println("** FinishModule");
		try {
*/
//			pc.module.print_osi(tos);
		pc.module.finish();
		//
/*
			if (do_out) {
				tos = getTOSLog();
	    		tos.put_string_ln(pc.module.getFileName());
				Helpers.printXML(pc.module, tos);
				tos.close();
			}
*/
		//
		//
/*
		} catch (final FileNotFoundException fnfe) {
			println("&& FileNotFoundException");
		} catch (final IOException ioe) {
			println("&& IOException");
		}
*/
	}

	public @NotNull OS_Module module() {
		return pc.module;
	}
}

//
//
//
