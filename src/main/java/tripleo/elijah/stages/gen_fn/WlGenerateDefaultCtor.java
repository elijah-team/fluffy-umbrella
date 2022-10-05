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
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.util.Holder;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/31/21 2:26 AM
 */
public class WlGenerateDefaultCtor implements WorkJob {
	private final GenerateFunctions generateFunctions;
	private final FunctionInvocation functionInvocation;
	private boolean _isDone = false;
	private BaseGeneratedFunction Result;

	@Contract(pure = true)
	public WlGenerateDefaultCtor(@NotNull GenerateFunctions aGenerateFunctions, FunctionInvocation aFunctionInvocation) {
		generateFunctions = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
	}

	@Override
	public void run(WorkManager aWorkManager) {
		if (functionInvocation.generateDeferred().isPending()) {
			final ClassStatement klass = functionInvocation.getClassInvocation().getKlass();
			Holder<GeneratedClass> hGenClass = new Holder<>();
			functionInvocation.getClassInvocation().resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(GeneratedClass result) {
					hGenClass.set(result);
				}
			});
			GeneratedClass genClass = hGenClass.get();
			assert genClass != null;

			ConstructorDef cd = new ConstructorDef(null, klass, klass.getContext());
//			cd.setName(Helpers.string_to_ident("<ctor>"));
			cd.setName(ConstructorDef.emptyConstructorName);
			Scope3 scope3 = new Scope3(cd);
			cd.scope(scope3);
			for (GeneratedContainer.VarTableEntry varTableEntry : genClass.varTable) {
				if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
					IExpression left = varTableEntry.nameToken;
					IExpression right = varTableEntry.initialValue;

					IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
					scope3.add(new WrappedStatementWrapper(e, cd.getContext(), cd, varTableEntry.vs));
				} else {
					if (true || getPragma("auto_construct")) {
						scope3.add(new ConstructStatement(cd, cd.getContext(), varTableEntry.nameToken, null, null));
					}
				}
			}

			OS_Element classStatement = cd.getParent();
			assert classStatement instanceof ClassStatement;
			@NotNull GeneratedConstructor gf = generateFunctions.generateConstructor(cd, (ClassStatement) classStatement, functionInvocation);
//		lgf.add(gf);

			final ClassInvocation ci = functionInvocation.getClassInvocation();
			ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(GeneratedClass result) {
					gf.setCode(generateFunctions.module.parent.nextFunctionCode());
					gf.setClass(result);
					result.constructors.put(cd, gf);
				}
			});

			functionInvocation.generateDeferred().resolve(gf);
			functionInvocation.setGenerated(gf);
			Result = gf;
		} else {
			functionInvocation.generatePromise().then(new DoneCallback<BaseGeneratedFunction>() {
				@Override
				public void onDone(final BaseGeneratedFunction result) {
					Result = result;
				}
			});
		}

		_isDone = true;
	}

	private boolean getPragma(String aAuto_construct) {
		return false;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}

	public BaseGeneratedFunction getResult() {
		return Result;
	}
}

//
//
//
