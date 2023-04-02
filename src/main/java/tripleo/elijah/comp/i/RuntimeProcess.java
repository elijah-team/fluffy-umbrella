package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;

public interface RuntimeProcess {
	void run(final Compilation aComp);

	void postProcess();

	void prepare() throws Exception;
}
