package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.CD_FindStdLib;
import tripleo.elijah.comp.i.CompilationClosure;

import java.io.File;
import java.util.function.Consumer;

import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

public class CD_FindStdLibImpl implements CD_FindStdLib {
	@Override
	public void findStdLib(final CR_State crState,
						   final String aPreludeName,
						   final @NotNull Consumer<Operation<CompilerInstructions>> coci) {
		int y = 2;
		try {
			final CompilationRunner compilationRunner = crState.runner();

			@NotNull final Operation<CompilerInstructions> oci = _____findStdLib(aPreludeName, compilationRunner.compilation.getCompilationClosure(), compilationRunner);
			coci.accept(oci);
		} catch (Exception aE) {
			throw new RuntimeException(aE);
		}

	}

	@NotNull
	public Operation<CompilerInstructions> _____findStdLib(final String prelude_name, final @NotNull CompilationClosure c, CompilationRunner cr) {
		final ErrSink errSink = c.errSink();
		final IO      io      = c.io();

		// TODO stdlib path here
		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
		if (local_stdlib.exists()) {
			try {
				final Operation<CompilerInstructions> oci = cr.realParseEzFile(local_stdlib.getName(), io.readFile(local_stdlib), local_stdlib, c.getCompilation());
				if (oci.mode() == SUCCESS) {
					c.getCompilation().pushItem(oci.success());
					return oci;
				}
			} catch (final Exception e) {
				return Operation.failure(e);
			}
		}
		//return false;
		return Operation.failure(new Exception() {
			public String message() {
				return "No stdlib found";
			}
		});
	}
}
