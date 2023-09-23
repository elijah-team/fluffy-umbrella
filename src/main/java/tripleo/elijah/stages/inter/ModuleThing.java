package tripleo.elijah.stages.inter;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.small.ES_Symbol;

public class ModuleThing {
	public record GeneralDescription(ES_Symbol aSymbol, @NotNull List<Object> aObjects) {
	}

	private final @NotNull List<EntryPoint>  entryPoints;
	private final @NotNull List<EvaFunction> evaFunctions = new ArrayList<>();
	private final          OS_Module         mod;

	private GeneralDescription generalDescription;

	public ModuleThing(final OS_Module aMod) {
		mod         = aMod;
		entryPoints = mod.entryPoints();
	}

	public void addFunction(final EvaFunction aGeneratedFunction) {
		evaFunctions.add(aGeneratedFunction);
	}

	public void describe(final GeneralDescription aGeneralDescription) {
		generalDescription = aGeneralDescription;
	}
}
