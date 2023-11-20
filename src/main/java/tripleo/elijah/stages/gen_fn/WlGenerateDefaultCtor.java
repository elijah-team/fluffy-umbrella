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
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/31/21 2:26 AM
 */
public class WlGenerateDefaultCtor implements WorkJob {
	private final GenerateFunctions generateFunctions;
	private final FunctionInvocation functionInvocation;
	private boolean _isDone = false;

	@Contract(pure = true)
	public WlGenerateDefaultCtor(@NotNull final GenerateFunctions aGenerateFunctions, final FunctionInvocation aFunctionInvocation) {
		generateFunctions = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
		if (functionInvocation.generateDeferred().isPending()) {
			final ClassStatement klass = functionInvocation.getClassInvocation().getKlass();
			final DeduceTypes2.Holder<GeneratedClass> hGenClass = new DeduceTypes2.Holder<>();
			functionInvocation.getClassInvocation().resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					hGenClass.set(result);
				}
			});
			final GeneratedClass genClass = hGenClass.get();
			assert genClass != null;

			final ConstructorDef cd = new ConstructorDef(null, klass, klass.getContext());
//			cd.setName(Helpers.string_to_ident("<ctor>"));
			cd.setName(ConstructorDef.emptyConstructorName);
			final Scope3 scope3 = new Scope3(cd);
			cd.scope(scope3);
			for (final GeneratedContainer.VarTableEntry varTableEntry : genClass.varTable) {
				if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
					final IExpression left = varTableEntry.nameToken;
					final IExpression right = varTableEntry.initialValue;

					final IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
					scope3.add(new WrappedStatementWrapper(e, cd.getContext(), cd, varTableEntry.vs));
				} else {
					if (true || getPragma("auto_construct")) {
						scope3.add(new ConstructStatement(cd, cd.getContext(), varTableEntry.nameToken, null, null));
					}
				}
			}

			final OS_Element classStatement = cd.getParent();
			assert classStatement instanceof ClassStatement;
			@NotNull final GeneratedConstructor gf = generateFunctions.generateConstructor(cd, (ClassStatement) classStatement, functionInvocation);
//		lgf.add(gf);

			final ClassInvocation ci = functionInvocation.getClassInvocation();
			ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					gf.setCode(generateFunctions.module.parent.nextFunctionCode());
					gf.setClass(result);
					result.constructors.put(cd, gf);
				}
			});

			functionInvocation.generateDeferred().resolve(gf);
			functionInvocation.setGenerated(gf);
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
}

//
//
//
