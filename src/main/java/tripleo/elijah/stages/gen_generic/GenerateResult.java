/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 4/27/21 1:11 AM
 */
public class GenerateResult {
	private final List<GenerateResultItem> _res = new ArrayList<GenerateResultItem>();
	private int bufferCounter = 0;

//	public void add(final Buffer b, final GeneratedNode n, final TY ty) {
//		_res.add(new GenerateResultItem(ty, b, n, null, null, ++bufferCounter)); // TODO remove nulls
//	}

	public void addConstructor(final EvaConstructor aEvaConstructor, final Buffer aBuffer, final TY aTY, final LibraryStatementPart aLsp) {
		addFunction(aEvaConstructor, aBuffer, aTY, aLsp);
	}

	public void addFunction(final BaseEvaFunction aGeneratedFunction, final Buffer aBuffer, final TY aTY, final @NotNull LibraryStatementPart aLsp) {
		add(aBuffer, aGeneratedFunction, aTY, aLsp, aGeneratedFunction.getDependency());
	}

	public void add(final Buffer b, final EvaNode n, final TY ty, final LibraryStatementPart aLsp, @NotNull final Dependency d) {
		final GenerateResultItem item = new GenerateResultItem(ty, b, n, aLsp, d, ++bufferCounter);
		_res.add(item);
//		items.onNext(item);
	}

	public void addClass(final TY ty, final EvaClass aClass, final Buffer aBuf, final LibraryStatementPart aLsp) {
		add(aBuf, aClass, ty, aLsp, aClass.getDependency());
	}

	public void addNamespace(final TY ty, final EvaNamespace aNamespace, final Buffer aBuf, final LibraryStatementPart aLsp) {
		add(aBuf, aNamespace, ty, aLsp, aNamespace.getDependency());
	}

	public void additional(@NotNull final GenerateResult aGgr) {
		_res.addAll(aGgr.results());
	}

	public List<GenerateResultItem> results() {
		return _res;
	}

	public enum TY {
		HEADER, IMPL, PRIVATE_HEADER
	}

}

//
//
//
