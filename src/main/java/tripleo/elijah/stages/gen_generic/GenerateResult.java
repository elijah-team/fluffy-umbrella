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
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 4/27/21 1:11 AM
 */
public class GenerateResult {
	private int bufferCounter = 0;

	private final List<GenerateResultItem> _res = new ArrayList<GenerateResultItem>();

//	public void add(final Buffer b, final GeneratedNode n, final TY ty) {
//		_res.add(new GenerateResultItem(ty, b, n, null, null, ++bufferCounter)); // TODO remove nulls
//	}

	public void addConstructor(final GeneratedConstructor aGeneratedConstructor, final Buffer aBuffer, final TY aTY, final LibraryStatementPart aLsp) {
		addFunction(aGeneratedConstructor, aBuffer, aTY, aLsp);
	}

	public void addFunction(final BaseGeneratedFunction aGeneratedFunction, final Buffer aBuffer, final TY aTY, final @NotNull LibraryStatementPart aLsp) {
		add(aBuffer, aGeneratedFunction, aTY, aLsp, aGeneratedFunction.getDependency());
	}

	public void add(final Buffer b, final GeneratedNode n, final TY ty, final LibraryStatementPart aLsp, @NotNull final Dependency d) {
		final GenerateResultItem item = new GenerateResultItem(ty, b, n, aLsp, d, ++bufferCounter);
		_res.add(item);
//		items.onNext(item);
	}

	public void addClass(final TY ty, final GeneratedClass aClass, final Buffer aBuf, final LibraryStatementPart aLsp) {
		add(aBuf, aClass, ty, aLsp, aClass.getDependency());
	}

	public void addNamespace(final TY ty, final GeneratedNamespace aNamespace, final Buffer aBuf, final LibraryStatementPart aLsp) {
		add(aBuf, aNamespace, ty, aLsp, aNamespace.getDependency());
	}

	public void additional(@NotNull final GenerateResult aGgr) {
		_res.addAll(aGgr.results());
	}

	public enum TY {
		HEADER, IMPL, PRIVATE_HEADER
	}

	public List<GenerateResultItem> results() {
		return _res;
	}

}

//
//
//
