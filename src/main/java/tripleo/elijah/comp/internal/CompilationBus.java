package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.CD_Item;
import tripleo.elijah.comp.i.CompilationChange;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.i.ILazyCompilerInstructions;

import java.util.HashMap;
import java.util.Map;

public class CompilationBus implements ICompilationBus {

	public final CompilerDriver cd;

	public static class CompilerDriver {
		private final        ICompilationBus                  cb;
		private 		final Map<DriverToken, CompilerDriven> drivens = new HashMap<>();
		private final Map<DriverToken, CompilerDriven> defaults = new HashMap<>();

		private /*static*/ boolean initialized;

		public CompilerDriver(final CompilationBus aCompilationBus) {
			cb = aCompilationBus;

			if (!initialized) {
				defaults.put(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_START, new CD_CompilationRunnerStart_1());
				defaults.put(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_FIND_STDLIB, new CD_FindStdLibImpl());
				initialized = true;
			}
		}

		public Operation<CompilerDriven> get(final DriverToken aToken) {
			final Operation<CompilerDriven> o;

			if (drivens.containsKey(aToken)) {
				o = Operation.success(drivens.get(aToken));
			} else {
				if (defaults.containsKey(aToken)) {
					final CompilerDriven x = defaults.get(aToken);
					o = Operation.success(x);
				} else {
					o = Operation.failure(new Exception("Compiler Driven get failure"));
				}
			}

			return o;
		}
	}
	public interface CompilerDriven extends CD_Item {

	}
	public interface DriverToken {
		static DriverToken makeToken(String s) {
			return new DriverToken() {
				@Override
				public String asString() {
					return s;
				}
			};
		}

		String asString();
	}
	private final Compilation c;

	public CompilationBus(final Compilation aC) {
		c = aC;
		cd = new CompilerDriver(this);
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		System.out.println("** [ci] " + aLazyCompilerInstructions.get());
	}

	@Override
	public void add(final @NotNull CB_Action action) {
		action.execute();
	}

	@Override
	public void add(final @NotNull CB_Process aProcess) {
		aProcess.steps().stream()
		        .forEach(step -> {
			        step.execute();
		        });
	}


}
