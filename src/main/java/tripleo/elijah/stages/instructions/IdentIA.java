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
import tripleo.elijah.lang.Context;
import tripleo.elijah.stages.deduce.nextgen.DN_Resolver;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.Constructable;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;

/**
 * Created 10/2/20 2:36 PM
 */
public class IdentIA implements InstructionArgument, Constructable {
	public final  BaseGeneratedFunction gf;
	private final int                   id;
//	private InstructionArgument prev;

/*
	public IdentIA(int x) {
		this.id = x;
		this.gf = null;  // TODO watch out
	}
*/

	public IdentIA(final int ite, final BaseGeneratedFunction generatedFunction) {
		this.gf = generatedFunction;
		this.id = ite;
	}

	public void setPrev(final InstructionArgument ia) {
		gf.getIdentTableEntry(id).setBacklink(ia);
	}

	@Override
	public String toString() {
		return String.valueOf(getEntry());
//		return "IdentIA{" +
//				"id=" + id +
////				", prev=" + prev +
//				'}';
	}

	public @NotNull IdentTableEntry getEntry() {
		return gf.getIdentTableEntry(getIndex());
	}

	public int getIndex() {
		return id;
	}

	@Override
	public void setConstructable(final ProcTableEntry aPte) {
		getEntry().setConstructable(aPte);
	}

	@Override
	public void resolveTypeToClass(final GeneratedNode aNode) {
		getEntry().resolveTypeToClass(aNode);
	}

	@Override
	public void setGenType(final GenType aGenType) {
		getEntry().setGenType(aGenType, gf);
	}

	@Override
	public Promise<ProcTableEntry, Void, Void> constructablePromise() {
		return getEntry().constructablePromise();
	}

	public DN_Resolver newResolver(final Context aCtx, final BaseGeneratedFunction aGeneratedFunction) {
		return getEntry().newResolver(aCtx, aGeneratedFunction);
	}
}

//
//
//
