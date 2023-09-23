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
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.Deduce_CreationClosure;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/16/21 12:46 AM
 */
public class WlGenerateFunction implements WorkJob {
	private final FunctionDef        functionDef;
	private final GenerateFunctions  generateFunctions;
	private final FunctionInvocation functionInvocation;
	private final ICodeRegistrar     codeRegistrar;
	private       boolean            _isDone = false;
	private       EvaFunction  result;

	public WlGenerateFunction(final GenerateFunctions aGenerateFunctions, @NotNull final FunctionInvocation aFunctionInvocation, final ICodeRegistrar aCodeRegistrar) {
		functionDef        = (FunctionDef) aFunctionInvocation.getFunction();
		generateFunctions  = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
		codeRegistrar      = aCodeRegistrar;
	}

	public WlGenerateFunction(final OS_Module aModule,
	                          final FunctionInvocation aFunctionInvocation,
	                          final Deduce_CreationClosure aCl) {
		functionDef        = (FunctionDef) aFunctionInvocation.getFunction();
		generateFunctions  = aCl.generatePhase().getGenerateFunctions(aModule);
		functionInvocation = aFunctionInvocation;
		codeRegistrar      = aCl.deducePhase().getCodeRegistrar();
	}

	public EvaFunction getResult() {
		return result;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
//		if (_isDone) return;

		if (functionInvocation.getGenerated() == null) {
			final OS_Element                 parent = functionDef.getParent();
			@NotNull final EvaFunction gf     = generateFunctions.generateFunction(functionDef, parent, functionInvocation);

			{
				int i = 0;
				for (final TypeTableEntry tte : functionInvocation.getArgs()) {
					i = i + 1;
					if (tte.getAttached() == null) {
						final String s = String.format("4949 null tte #%d %s in %s%n", i, tte, gf);
						Stupidity.println_err2(s);
					}
				}
			}

//			lgf.add(gf);

			if (parent instanceof NamespaceStatement) {
				final NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
				assert nsi != null;
				nsi.resolveDeferred().done(new DoneCallback<EvaNamespace>() {
					@Override
					public void onDone(final EvaNamespace result) {
						if (result.getFunction(functionDef) == null) {
							codeRegistrar.registerFunction(gf);
							result.addFunction(functionDef, gf);
						}
						gf.setClass(result);
					}
				});
			} else {
				final ClassInvocation ci = functionInvocation.getClassInvocation();
				ci.resolvePromise().done(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(final EvaClass result) {
						if (result.getFunction(functionDef) == null) {
							codeRegistrar.registerFunction(gf);
							result.addFunction(functionDef, gf);
						}
						gf.setClass(result);
					}
				});
			}
			result = gf;
			functionInvocation.setGenerated(result);
			functionInvocation.generateDeferred().resolve(result);
		} else {
			result = (EvaFunction) functionInvocation.getGenerated();
		}
		_isDone = true;
	}
}

//
//
//
