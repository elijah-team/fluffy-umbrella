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
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static @NotNull PicoContainer newContainer() {
		final MutablePicoContainer pico = new DefaultPicoContainer();

		pico.addComponent(PicoContainer.class, pico);

		pico.addComponent(ErrSink.class);
		pico.addComponent(IO.class);
		//pico.addComponent(ErrSink.class, new StdErrSink());
		//pico.addComponent(IO.class, new IO());

		pico.addComponent(Compilation.class);

		//pico.addComponent(ShowInfoWindowAction.class);
		//pico.addComponent(ShowInfoWindowButton.class);

		return pico;
	}

	public static void main(final String[] args) throws Exception {
		final PicoContainer pico = newContainer();
		final Compilation 	comp = pico.getComponent(Compilation.class);
		final List<String> 	ls1  = new ArrayList<String>();

		ls1.addAll(Arrays.asList(args));

		comp.feedCmdLine(ls1);

		if (false) {
			final StdErrSink errSink = new StdErrSink();
			final Compilation cc = new CompilationImpl(errSink, new IO());
			final List<String> ls = new ArrayList<String>();
			ls.addAll(Arrays.asList(args));

			cc.feedCmdLine(ls/*, new StdErrSink()*/);
		}
	}

}

//
//
//
