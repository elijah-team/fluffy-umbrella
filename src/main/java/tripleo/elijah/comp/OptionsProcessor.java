package tripleo.elijah.comp;

import java.util.List;

@FunctionalInterface
public interface OptionsProcessor {
	String[] process(final Compilation c, final List<String> args) throws Exception;
}
