/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateDefaultCtor;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.stages.gen_fn.WlGenerateNamespace;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 1/21/21 9:04 PM
 */
public class FunctionInvocation {
	private final BaseFunctionDef fd;
	public final ProcTableEntry pte;
	private ClassInvocation classInvocation;
	private           NamespaceInvocation                         namespaceInvocation;
	private final     DeferredObject<BaseEvaFunction, Void, Void> generateDeferred = new DeferredObject<BaseEvaFunction, Void, Void>();
	private @Nullable BaseEvaFunction                             _generated       = null;

	public FunctionInvocation(BaseFunctionDef aFunctionDef, ProcTableEntry aProcTableEntry, @NotNull IInvocation invocation, GeneratePhase phase) {
		this.fd = aFunctionDef;
		this.pte = aProcTableEntry;
		assert invocation != null;
		invocation.setForFunctionInvocation(this);
//		setPhase(phase);
	}

/*
	public void setPhase(final GeneratePhase generatePhase) {
		if (pte != null)
			pte.completeDeferred().then(new DoneCallback<ProcTableEntry>() {
				@Override
				public void onDone(ProcTableEntry result) {
					makeGenerated(generatePhase, null);
				}
			});
		else
			makeGenerated(generatePhase, null);
	}
*/

	void makeGenerated(@NotNull GeneratePhase generatePhase, @NotNull DeducePhase aPhase) {
		@Nullable OS_Module module = null;
		if (fd != null)
			module = fd.getContext().module();
		if (module == null)
			module = classInvocation.getKlass().getContext().module(); // README for constructors
		if (fd == ConstructorDef.defaultVirtualCtor) {
			@NotNull WlGenerateDefaultCtor wlgdc = new WlGenerateDefaultCtor(generatePhase.getGenerateFunctions(module), this);
			wlgdc.run(null);
//			EvaFunction gf = wlgdc.getResult();
		} else {
			@NotNull WlGenerateFunction wlgf = new WlGenerateFunction(generatePhase.getGenerateFunctions(module), this);
			wlgf.run(null);
			EvaFunction gf = wlgf.getResult();
			if (gf.getGenClass() == null) {
				if (namespaceInvocation != null) {
//					namespaceInvocation = aPhase.registerNamespaceInvocation(namespaceInvocation.getNamespace());
					@NotNull WlGenerateNamespace wlgn = new WlGenerateNamespace(generatePhase.getGenerateFunctions(module),
							namespaceInvocation,
							aPhase.generatedClasses);
					wlgn.run(null);
					int y=2;
				}
			}
		}
//		if (generateDeferred.isPending()) {
//			generateDeferred.resolve(gf);
//			_generated = gf;
//		}
	}

	public @Nullable BaseEvaFunction getGenerated() {
		return _generated;
	}

	public BaseFunctionDef getFunction() {
		return fd;
	}

	public void setClassInvocation(@NotNull ClassInvocation aClassInvocation) {
		classInvocation = aClassInvocation;
	}

	public ClassInvocation getClassInvocation() {
		return classInvocation;
	}

	public NamespaceInvocation getNamespaceInvocation() {
		return namespaceInvocation;
	}

	public void setNamespaceInvocation(NamespaceInvocation aNamespaceInvocation) {
		namespaceInvocation = aNamespaceInvocation;
	}

	public @NotNull DeferredObject<BaseEvaFunction, Void, Void> generateDeferred() {
		return generateDeferred;
	}

	public Promise<BaseEvaFunction, Void, Void> generatePromise() {
		return generateDeferred.promise();
	}

	public void setGenerated(BaseEvaFunction aGeneratedFunction) {
		_generated = aGeneratedFunction;
	}

	public List<TypeTableEntry> getArgs() {
		if (pte == null)
			return List_of();
		return pte.args;
	}

	public WlGenerateFunction generateFunction(final DeduceTypes2 aDeduceTypes2, final OS_Element aBest) {
		throw new Error();
	}

	public BaseEvaFunction getEva() {
		return null; // TODO 04/15
	}
}

//
//
//
