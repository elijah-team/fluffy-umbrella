package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;

import java.util.List;

//@FunctionalInterface
public interface OptionsProcessor {
	//String[] process(final Compilation c, final List<String> args) throws Exception;

	String[] process(@NotNull Compilation c,
	                 @NotNull List<String> args,
	                 @NotNull ICompilationBus cb) throws Exception;
}
