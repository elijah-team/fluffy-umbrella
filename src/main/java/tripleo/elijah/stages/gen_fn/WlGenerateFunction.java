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
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/16/21 12:46 AM
 */
public class WlGenerateFunction implements WorkJob {
	private final FunctionDef functionDef;
	private final GenerateFunctions generateFunctions;
	private final FunctionInvocation functionInvocation;
	private boolean _isDone = false;
	private GeneratedFunction result;

	public WlGenerateFunction(final GenerateFunctions aGenerateFunctions, @NotNull final FunctionInvocation aFunctionInvocation) {
		functionDef = (FunctionDef) aFunctionInvocation.getFunction();
		generateFunctions = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
//		if (_isDone) return;

		if (functionInvocation.getGenerated() == null) {
			final OS_Element parent = functionDef.getParent();
			@NotNull final GeneratedFunction gf = generateFunctions.generateFunction(functionDef, parent, functionInvocation);
//			lgf.add(gf);

			if (parent instanceof NamespaceStatement) {
				final NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
				assert nsi != null;
				nsi.resolveDeferred().done(new DoneCallback<GeneratedNamespace>() {
					@Override
					public void onDone(final GeneratedNamespace result) {
						if (result.getFunction(functionDef) == null) {
							gf.setCode(generateFunctions.module.parent.nextFunctionCode());
							result.addFunction(functionDef, gf);
						}
						gf.setClass(result);
					}
				});
			} else {
				final ClassInvocation ci = functionInvocation.getClassInvocation();
				ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						if (result.getFunction(functionDef) == null) {
							gf.setCode(generateFunctions.module.parent.nextFunctionCode());
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
			result = (GeneratedFunction) functionInvocation.getGenerated();
		}
		_isDone = true;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}

	public GeneratedFunction getResult() {
		return result;
	}
}

//
//
//
