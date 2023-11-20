/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.GenericTypeName;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 9/12/20 10:26 PM
 */
public class TypeTableEntry {
	@NotNull
	public final  Type                lifetime;
	@Nullable
	public final  TableEntryIV        tableEntry;
	public final  GenType             genType = new GenType();
	public final  IExpression         expression;
	final         int                 index;
	private final List<OnSetAttached> osacbs  = new ArrayList<OnSetAttached>();
	@Nullable
	private       OS_Type             attached;

	public interface OnSetAttached {
		void onSetAttached(TypeTableEntry aTypeTableEntry);
	}

	public TypeTableEntry(final int index,
	                      @NotNull final Type lifetime,
	                      @Nullable final OS_Type aAttached,
	                      final IExpression expression,
	                      @Nullable final TableEntryIV aTableEntryIV) {
		this.index    = index;
		this.lifetime = lifetime;
		if (aAttached == null || (aAttached.getType() == OS_Type.Type.USER && aAttached.getTypeName() == null)) {
			attached = null;
			// do nothing with genType
		} else {
			attached = aAttached;
			_settingAttached(aAttached);
		}
		this.expression = expression;
		this.tableEntry = aTableEntryIV;
	}

	private void _settingAttached(@NotNull final OS_Type aAttached) {
		switch (aAttached.getType()) {
			case USER:
				if (genType.typeName != null) {
					final TypeName typeName = aAttached.getTypeName();
					if (!(typeName instanceof GenericTypeName))
						genType.nonGenericTypeName = typeName;
				} else
					genType.typeName = aAttached/*.getTypeName()*/;
				break;
			case USER_CLASS:
//			ClassStatement c = attached.getClassOf();
				genType.resolved = aAttached/*attached*/; // c
				break;
			case UNIT_TYPE:
				genType.resolved = aAttached;
			case BUILT_IN:
				if (genType.typeName != null)
					genType.resolved = aAttached;
				else
					genType.typeName = aAttached;
				break;
			case FUNCTION:
				assert genType.resolved == null || genType.resolved == aAttached || /*HACK*/ aAttached.getType() == OS_Type.Type.FUNCTION;
				genType.resolved = aAttached;
				break;
			case FUNC_EXPR:
				assert genType.resolved == null || genType.resolved == aAttached;// || /*HACK*/ aAttached.getType() == OS_Type.Type.FUNCTION;
				genType.resolved = aAttached;
				break;
			default:
//			throw new NotImplementedException();
				tripleo.elijah.util.Stupidity.println_err2("73 " + aAttached);
				break;
		}
	}

	@Override @NotNull
	public String toString() {
		return "TypeTableEntry{" +
				"index=" + index +
				", lifetime=" + lifetime +
				", attached=" + attached +
				", expression=" + expression +
				'}';
	}

	public int getIndex() {
		return index;
	}

	public void resolve(final GeneratedNode aResolved) {
		genType.node = aResolved;
	}

	public GeneratedNode resolved() {
		return genType.node;
	}

	public boolean isResolved() {
		return genType.node != null;
	}

	public OS_Type getAttached() {
		return attached;
	}

	public void setAttached(final OS_Type aAttached) {
		attached = aAttached;
		if (aAttached != null) {
			_settingAttached(aAttached);

			for (final OnSetAttached cb : osacbs) {
				cb.onSetAttached(this);
			}
		}
	}

	public void setAttached(final GenType aGenType) {
		genType.copy(aGenType);
//		if (aGenType.resolved != null) genType.resolved = aGenType.resolved;
//		if (aGenType.ci != null) genType.ci = aGenType.ci;
//		if (aGenType.node != null) genType.node = aGenType.node;

		setAttached(genType.resolved);
	}

	public void addSetAttached(final OnSetAttached osa) {
		osacbs.add(osa);
	}

	public void genTypeCI(final ClassInvocation aClsinv) {
		genType.ci = aClsinv;
	}

	public enum Type {
		SPECIFIED, TRANSIENT
	}

}

//
//
//
