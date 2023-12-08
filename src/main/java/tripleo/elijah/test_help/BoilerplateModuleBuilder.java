package tripleo.elijah.test_help;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;

import java.util.function.Consumer;

public class BoilerplateModuleBuilder {
	private final OS_Module mod;

	public BoilerplateModuleBuilder(final OS_Module aMod) {
		mod = aMod;
	}

//	public BoilerplateClassBuilder addClass(final String aClassNameString) {
//		return new BoilerplateClassBuilder(mod, aClassNameString);
//	}

	public void addClass(final @NotNull Consumer<BoilerplateClassBuilder> aClassBuilderConsumer) {
		final BoilerplateClassBuilder bcb = new BoilerplateClassBuilder();
		bcb.module(mod);

		aClassBuilderConsumer.accept(bcb);

		final ClassStatement cs = bcb.build();
		cs.postConstruct();
	}

}
