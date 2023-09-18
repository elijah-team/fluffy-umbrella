package tripleo.elijah.stages.gen_c;

import static tripleo.elijah.util.DebugFlags.MANUAL_DISABLED;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.nextgen.reactive.Reactivable;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.i.LivingNamespace;

public class WhyNotGarish_Namespace implements WhyNotGarish_Item {
	public class GCFN implements Reactivable {

		@Override
		public void respondTo(final ReactiveDimension aDimension) {
			if (aDimension instanceof GenerateC generateC) {
				fileGenPromise.then(fileGen -> {
					final LivingNamespace livingNamespace = generateC._ce().getCompilation().world().getNamespace(en);

					livingNamespace.getGarish().garish(generateC, fileGen.gr(), fileGen.resultSink());
				});
			}
		}
	}
	private final EvaNamespace                             en;
	private final GenerateC                                generateC;

	private final DeferredObject<GenerateResultEnv, Void, Void> fileGenPromise = new DeferredObject<>();

	private final GCFN gcfn = new GCFN();

	public WhyNotGarish_Namespace(final EvaNamespace aEn, final GenerateC aGenerateC) {
		en        = aEn;
		generateC = aGenerateC;


		en.reactive().add(gcfn);


		fileGenPromise.then(this::onFileGen);
	}

	public String getTypeNameString() {
		return GenerateC.GetTypeName.forGenNamespace(en);
	}

	@Override
	public boolean hasFileGen() {
		return fileGenPromise.isResolved();
	}

	private void onFileGen(final @NotNull GenerateResultEnv aFileGen) {
		NotImplementedException.raise();

		if (!MANUAL_DISABLED) {
			gcfn.respondTo(this.generateC);
		}

		var gn = generateC._ce().getCompilation().world().getNamespace(en).getGarish();

		final @NotNull GenerateResultSink sink = aFileGen.resultSink();

		if (sink != null)
			sink.addNamespace_1(gn, aFileGen.gr(), generateC);
		else
			System.err.println("sink failed");

	}

	@Override
	public void provideFileGen(final GenerateResultEnv fg) {
		fileGenPromise.resolve(fg);
	}
}
