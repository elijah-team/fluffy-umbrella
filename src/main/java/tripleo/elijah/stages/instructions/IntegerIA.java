/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.instructions;

import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.Constructable;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

/**
 * Created 9/10/20 3:35 PM
 */
public class IntegerIA implements InstructionArgument, Constructable {

	@Override
	public String toString() {
		return "IntegerIA{" +
				"index=" + index +
				'}';
	}

	public final BaseEvaFunction gf;

	private final int index;

	public IntegerIA(final int anIndex, BaseEvaFunction aGeneratedFunction) {
		index = anIndex;
		gf = aGeneratedFunction;
	}

	public int getIndex() {
		return index;
	}

	public @NotNull VariableTableEntry getEntry() {
		return gf.getVarTableEntry(index);
	}

	@Override
	public void setConstructable(ProcTableEntry aPte) {
		getEntry().setConstructable(aPte);
	}

	@Override
	public void resolveTypeToClass(EvaNode aNode) {
		getEntry().resolveTypeToClass(aNode);
	}

	@Override
	public void setGenType(GenType aGenType) {
		getEntry().setGenType(aGenType);
	}

	@Override
	public Promise<ProcTableEntry, Void, Void> constructablePromise() {
		return getEntry().constructablePromise();
	}
}

//
//
//
