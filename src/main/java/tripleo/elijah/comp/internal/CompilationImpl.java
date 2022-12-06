/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.deduce.fluffy.impl.FluffyCompImpl;
import tripleo.elijah.testing.comp.IFunctionMapHook;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

public class CompilationImpl extends Compilation {

	public CompilationImpl(ErrSink aEee, IO aIo) {
		super(aEee, aIo);
		_fluffyComp = new FluffyCompImpl(this);
	}

	public void testMapHooks(List<IFunctionMapHook> aMapHooks) {
		throw new NotImplementedException();
	}

	private EOT_OutputTree _output_tree = null;

	@Override
	@NotNull
	public EOT_OutputTree getOutputTree() {
		if (_output_tree == null) {
			_output_tree = new EOT_OutputTree();
		}

		assert _output_tree != null;

		return _output_tree;
	}

	@Override
	public @NotNull FluffyComp getFluffy() {
		return _fluffyComp;
	}

	private final @NotNull FluffyCompImpl _fluffyComp;

}

//
//
//