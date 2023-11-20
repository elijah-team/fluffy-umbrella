/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.ExpressionBuilder;
import tripleo.elijah.lang.ExpressionKind;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FunctionItem;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.Scope3;
import tripleo.elijah.lang.StatementWrapper;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.Holder;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 7/3/21 6:24 AM
 */
public class WlGenerateCtor implements WorkJob {
	private final GenerateFunctions    generateFunctions;
	private final FunctionInvocation   functionInvocation;
	private final IdentExpression      constructorName;
	private final ICodeRegistrar       codeRegistrar;
	private       boolean              _isDone = false;
	private       GeneratedConstructor result;

	@Contract(pure = true)
	public WlGenerateCtor(@NotNull final GenerateFunctions aGenerateFunctions,
	                      @NotNull final FunctionInvocation aFunctionInvocation,
	                      @Nullable final IdentExpression aConstructorName, final ICodeRegistrar aCodeRegistrar) {
		generateFunctions  = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
		constructorName    = aConstructorName;
		codeRegistrar      = aCodeRegistrar;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
		if (functionInvocation.generateDeferred().isPending()) {
			final ClassStatement                  klass     = functionInvocation.getClassInvocation().getKlass();
			final @NotNull Holder<GeneratedClass> hGenClass = new Holder<>();
			functionInvocation.getClassInvocation().resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					hGenClass.set(result);
				}
			});
			final GeneratedClass genClass = hGenClass.get();
			assert genClass != null;

			ConstructorDef ccc = null;
			if (constructorName != null) {
				final Collection<ConstructorDef> cs = klass.getConstructors();
				for (@NotNull final ConstructorDef c : cs) {
					if (c.name().equals(constructorName.getText())) {
						ccc = c;
						break;
					}
				}
			}

			final ConstructorDef cd;
			if (ccc == null) {
				cd = new ConstructorDef(constructorName, klass, klass.getContext());
				final @NotNull Scope3 scope3 = new Scope3(cd);
				cd.scope(scope3);
				for (final GeneratedContainer.VarTableEntry varTableEntry : genClass.varTable) {
					if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
						final IExpression left  = varTableEntry.nameToken;
						final IExpression right = varTableEntry.initialValue;

						final IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
						scope3.add(new StatementWrapper(e, cd.getContext(), cd));
					} else {
						if (true /*|| getPragma("auto_construct")*/) {
							scope3.add(new ConstructStatement(cd, cd.getContext(), varTableEntry.nameToken, null, null));
						}
					}
				}
			} else
				cd = ccc;

			final OS_Element classStatement_ = cd.getParent();
			assert classStatement_ instanceof ClassStatement;

			final ClassStatement classStatement = (ClassStatement) classStatement_;
			final Collection<ConstructorDef> cs = classStatement.getConstructors();
			ConstructorDef c = null;
			if (constructorName != null) {
				for (final ConstructorDef cc : cs) {
					if (cc.name().equals(constructorName.getText())) {
						c = cc;
						break;
					}
				}
			} else {
				// TODO match based on arguments
				final ProcTableEntry pte = functionInvocation.pte;
				final List<TypeTableEntry> args = pte.getArgs();
				// isResolved -> GeneratedNode, etc or getAttached -> OS_Element
				for (final ConstructorDef cc : cs) {
					final Collection<FormalArgListItem> cc_args = cc.getArgs();
					if (cc_args.size() == args.size()) {
						if (args.size() == 0) {
							c = cc;
							break;
						}
						final int y = 2;
					}
				}
			}

			{
				// TODO what about multiple inheritance?

				// add inherit statement, if any

				// add code from c
				if (c != null && c != cd) {
					final ArrayList<FunctionItem> is = new ArrayList<>(c.getItems());

					// skip initializers (already present in cd)
//				FunctionItem firstElement = is.get(0);
//				if (firstElement instanceof InheritStatement) {
//					cd.insertInherit(firstElement);
//					is.remove(0);
//				}

					for (final FunctionItem item : is) {
						cd.add(item);
					}
				}
			}

			@NotNull final GeneratedConstructor gf = generateFunctions.generateConstructor(cd, (ClassStatement) classStatement_, functionInvocation);
//		lgf.add(gf);

			final ClassInvocation ci = functionInvocation.getClassInvocation();
			ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					codeRegistrar.registerFunction(gf);
					gf.setClass(result);
					result.constructors.put(cd, gf);
				}
			});

			functionInvocation.generateDeferred().resolve(gf);
			functionInvocation.setGenerated(gf);

			result = gf;
		}

		_isDone = true;
	}

	private boolean getPragma(final String aAuto_construct) {
		return false;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}

	public GeneratedConstructor getResult() {
		return result;
	}
}

//
//
//
