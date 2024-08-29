/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah_durable_congenial.comp;

<<<<<<<< HEAD:elijah-congenial-robot/elijah-core/src/main/java/tripleo/elijah_durable_congenial/comp/EmptyProcess.java
import tripleo.elijah.comp.i.Compilation;
import tripleo.elijah.comp.i.RuntimeProcess;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;
========
import org.jetbrains.annotations.Contract;
import tripleo.elijah_durable_congenial.comp.i.Compilation;
import tripleo.elijah_durable_congenial.comp.i.ICompilationAccess;
import tripleo.elijah_durable_congenial.comp.i.ProcessRecord;
import tripleo.elijah_durable_congenial.comp.i.RuntimeProcess;
import tripleo.elijah_durable_congenial.comp.internal.CB_Output;
import tripleo.elijah_durable_congenial.comp.internal.CR_State;
>>>>>>>> 2024-congenial-update:elijah-congenial-robot/elijah-core/src/main/java/tripleo/elijah_durable_congenial/comp/CompilationHelp.java

final class EmptyProcess implements RuntimeProcess {
	public EmptyProcess() {
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
	}

	@Override
	public void run(final Compilation aComp, final CR_State st, final CB_Output output) {

	}
}
