package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.Operation2;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.query.Mode;

import java.util.function.Consumer;

public interface CompilationFlow {
	//static CompilationFlowMember findPrelude() {
	//	return new CF_FindPrelude(aCopm);
	//}

	static CompilationFlowMember findMainClass() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	void add(CompilationFlowMember aFlowMember);

	void run(CompilationImpl aCompilation);

	interface CompilationFlowMember {
		public void doIt(Compilation cc, final CompilationFlow flow);
	}
	public static CompilationFlowMember parseElijah() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				int y=2;
			}
		};
	}
	public static CompilationFlowMember genFromEntrypoints() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlowMember getClasses() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {


			}
		};
	}
	public static CompilationFlowMember runFunctionMapHooks() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlowMember deduceModuleWithClasses() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlowMember finishModule() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlowMember returnErrorCount() {
		return new CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	class CF_FindPrelude implements CompilationFlowMember {
		private final Consumer<Operation2<OS_Module>> copm;

		public CF_FindPrelude(final Consumer<Operation2<OS_Module>> aCopm) {
			copm = aCopm;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {
			// TODO we dont know which prelude to find yet

			final Operation2<OS_Module> prl = cc.findPrelude(Compilation.CompilationAlways.defaultPrelude());
			assert (prl.mode() == Mode.SUCCESS);

			copm.accept(prl);
		}
	}
}
