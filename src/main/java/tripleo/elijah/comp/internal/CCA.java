package tripleo.elijah.comp.internal;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class CCA {
	private final RegistryImpl registry;
	private final CompilationShit cs;
	private final PipelineLogic _pl;

	@Contract(pure = true)
	public CCA(RegistryImpl aRegistry, CompilationShit aCs) {
		registry = aRegistry;
		cs = aCs;

		_pl = new PipelineLogic(aCs);
	}

	public Compilation getComp() {
		return cs.getComp();
	}

	public Consumer<Attachable> getPipelineAcceptor() {
		// TODO stop passing lambdas around
		//  ?? or does he?
		return (att) -> registry.addPipeline((PipelineMember) att);
	}

	public PipelineLogic getPipelineLogic() {
		return _pl;
	}

	public Promise<List<GeneratedNode>, Void, Void> lgcp() {
		return _lgcp;
	}

	private DeferredObject<List<GeneratedNode>, Void, Void> _lgcp = new DeferredObject<>();
	private DeferredObject<List<OS_Module>, Void, Void> _modsP = new DeferredObject<>();

	public void addModuleListener(final ModuleListener aModuleListener) {
		_modsP.then((ms) -> aModuleListener.onModules(ms));
	}

	public void resolveModules(final List<OS_Module> mods) {
		_modsP.resolve(mods);
	}

	public void resolveLGN(final List<GeneratedNode> lgn) {
		_lgcp.resolve(lgn);
	}

	public @NotNull PipelineLogic getPL() {
		return _pl;
	}

	public void addModule(OS_Module aModule) {
		modules.add(aModule);
	}

	private final List<OS_Module> modules = new LinkedList<>();
}
