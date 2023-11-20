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
import tripleo.elijah.stages.gen_fn.*;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 4/27/21 1:11 AM
 */
public class GenerateResult {
	private int bufferCounter = 0;

	final List<GenerateResultItem> res = new ArrayList<GenerateResultItem>();

	public void add(final Buffer b, final GeneratedNode n, final TY ty) {
		res.add(new GenerateResultItem(ty, b, n, ++bufferCounter));
	}

	public List<GenerateResultItem> results() {
		return res;
	}

	public void addFunction(final BaseGeneratedFunction aGeneratedFunction, final Buffer aBuffer, final TY aTY) {
		add(aBuffer, aGeneratedFunction, aTY);
	}

	public void addConstructor(final GeneratedConstructor aGeneratedFunction, final Buffer aBuffer, final TY aTY) {
		addFunction(aGeneratedFunction, aBuffer, aTY);
	}

	public enum TY {
		HEADER, IMPL, PRIVATE_HEADER
	}

	public void addClass(final TY ty, final GeneratedClass aClass, final Buffer aBuf) {
		add(aBuf, aClass, ty);
	}

	public void addNamespace(final TY ty, final GeneratedNamespace aNamespace, final Buffer aBuf) {
		add(aBuf, aNamespace, ty);
	}

	public void additional(@NotNull final GenerateResult aGgr) {
		res.addAll(aGgr.results());
	}

}

//
//
//
