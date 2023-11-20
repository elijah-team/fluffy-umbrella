/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import tripleo.elijah.gen.ICodeGen;
import tripleo.elijah.lang.*;

import java.util.List;


/**
 * @author Tripleo
 *
 * Created 	Mar 26, 2020 at 6:04:02 AM
 */
public class ClassContext extends Context {

	private final ClassStatement carrier;
	private final Context _parent;

	public ClassContext(final Context aParent, final ClassStatement cls) {
		_parent = aParent;
		carrier = cls;
	}

	@Override public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
		alreadySearched.add(carrier.getContext());
		for (final ClassItem item: carrier.getItems()) {
			if (!(item instanceof ClassStatement) &&
				!(item instanceof NamespaceStatement) &&
				!(item instanceof BaseFunctionDef) &&
				!(item instanceof VariableSequence) &&
				!(item instanceof AliasStatement) &&
				!(item instanceof PropertyStatement)
			) continue;
			if (item instanceof OS_Element2) {
				if (((OS_Element2) item).name().equals(name)) {
					Result.add(name, level, item, this);
				}
			}
			if (item instanceof VariableSequence) {
//				System.out.println("102 "+item);
				for (final VariableStatement vs : ((VariableSequence) item).items()) {
					if (vs.getName().equals(name))
						Result.add(name, level, vs, this);
				}
			}
		}
		for (final TypeName tn1 : carrier.classInheritance().tns) {
//			System.out.println("1001 "+tn);
			final NormalTypeName tn = (NormalTypeName)tn1;
			final OS_Element best;
			if (!tn.hasResolvedElement()) {
				final LookupResultList tnl = tn.getContext().lookup(tn.getName());
//	    		System.out.println("1002 "+tnl.results());
				best = tnl.chooseBest(null);
			} else
				best = tn.getResolvedElement();
			if (best != null) {
				tn.setResolvedElement(best);
				final LookupResultList lrl2 = best.getContext().lookup(name);
				final OS_Element best2 = lrl2.chooseBest(null);
				if (best2 != null)
					Result.add(name, level, best2, this);
			}
//			System.out.println("1003 "+name+" "+Result.results());
		}
		for (final TypeName tn1 : carrier.getGenericPart()) {
			if (tn1 instanceof NormalTypeName) {
				final NormalTypeName tn = (NormalTypeName) tn1;
				final String name1 = tn.getName(); // TODO this may return a string with DOTs in it.
				if (name1.equals(name)) {
//					LookupResultList lrl = tn.getContext().lookup(name);
//					OS_Element best = lrl.chooseBest(null);
//					if (best == null) {
//						throw new AssertionError();
//					} else
						Result.add(name, level, new OS_TypeNameElement(tn1), this);
				}
			} else {
				// TODO probable error
			}
		}
		if (getParent() != null) {
			final Context context = getParent();
			if (!alreadySearched.contains(context) || !one)
				return context.lookup(name, level + 1, Result, alreadySearched, false);
		}
		return Result;
	}

	@Override public Context getParent() {
		return _parent;
	}

	public class OS_TypeNameElement implements OS_Element {
		private final TypeName typeName;

		public TypeName getTypeName() {
			return typeName;
		}

		public OS_TypeNameElement(final TypeName aTypeName) {
			typeName = aTypeName;
		}

		@Override
		public void visitGen(final ICodeGen visit) {
			visit.visitTypeNameElement(this);
		}

		@Override
		public OS_Element getParent() {
			return carrier;
		}

		@Override
		public Context getContext() {
			return ClassContext.this;
		}
	}
}

//
//
//
