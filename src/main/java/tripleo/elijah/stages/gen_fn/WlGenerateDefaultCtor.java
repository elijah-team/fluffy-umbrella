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
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.ExpressionBuilder;
import tripleo.elijah.lang.ExpressionKind;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.Scope3;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.Holder;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/31/21 2:26 AM
 */
public class WlGenerateDefaultCtor implements WorkJob {
	private final GenerateFunctions     generateFunctions;
	private final FunctionInvocation    functionInvocation;
	private final ICodeRegistrar        codeRegistrar;
	private       boolean               _isDone = false;
	private       BaseGeneratedFunction Result;

	@Contract(pure = true)
	public WlGenerateDefaultCtor(@NotNull final GenerateFunctions aGenerateFunctions, final FunctionInvocation aFunctionInvocation, final ICodeRegistrar aCodeRegistrar) {
		generateFunctions  = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
		codeRegistrar      = aCodeRegistrar;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
		if (functionInvocation.generateDeferred().isPending()) {
			final ClassStatement         klass     = functionInvocation.getClassInvocation().getKlass();
			final Holder<GeneratedClass> hGenClass = new Holder<>();
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
					final IExpression left  = varTableEntry.nameToken;
					final IExpression right = varTableEntry.initialValue;

					final IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
					scope3.add(new WrappedStatementWrapper(e, cd.getContext(), cd, varTableEntry.vs));
				} else {
					if (true) {
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
				public void onDone(final @NotNull GeneratedClass result) {
					codeRegistrar.registerFunction(gf);
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

	private boolean getPragma(final String aAuto_construct) {
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
