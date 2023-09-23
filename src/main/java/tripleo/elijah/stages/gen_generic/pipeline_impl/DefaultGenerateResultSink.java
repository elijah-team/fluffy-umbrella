package tripleo.elijah.stages.gen_generic.pipeline_impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.stages.garish.GarishClass;
import tripleo.elijah.stages.garish.GarishNamespace;
import tripleo.elijah.stages.gen_c.C2C_Result;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.OutputStrategyC;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.i.LivingClass;
import tripleo.elijah.world.i.LivingNamespace;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class DefaultGenerateResultSink implements GenerateResultSink {
	public class NG_OutDep {
		String    filename;
		OS_Module module;


		public NG_OutDep(final @NotNull OS_Module aModuleDependency) {
			module   = aModuleDependency;
			filename = aModuleDependency.getFileName();
		}

		public OS_Module module() {
			return module;
		}
	}

	public class NG_OutputClass /*implements NG_OutputItem*/ {
		private GarishClass garishClass;
		private GenerateC   generateC;

//		@Override
		public @NotNull List<NG_OutputStatement> getOutputs() {
			final EvaClass x = garishClass.getLiving().evaNode();

			final BufferTabbedOutputStream tos = garishClass.getClassBuffer(generateC);
			var implText = new NG_OutputClassStatement(tos, x.module(), GenerateResult.TY.IMPL);

			final BufferTabbedOutputStream tosHdr = garishClass.getHeaderBuffer(generateC);
			var headerText = new NG_OutputClassStatement(tosHdr, x.module(), GenerateResult.TY.HEADER);

			return List_of(implText, headerText);
		}

//		@Override
		public EOT_OutputFile.FileNameProvider outName(final @NotNull OutputStrategyC aOutputStrategyC, final GenerateResult.@NotNull TY ty) {
			final EvaClass x = garishClass.getLiving().evaNode();

			return aOutputStrategyC.nameForClass1(x, ty);
		}

		public void setClass(final GarishClass aGarishClass, final GenerateC aGenerateC) {
			garishClass = aGarishClass;
			generateC   = aGenerateC;
		}
	}

	public class NG_OutputClassStatement implements NG_OutputStatement {
		private final          String            text;
		private final          GenerateResult.TY ty;
		private final @NotNull NG_OutDep         moduleDependency;
		private final @NotNull BufferTabbedOutputStream __tos;

		public NG_OutputClassStatement(final @NotNull BufferTabbedOutputStream aText, final @NotNull OS_Module aModuleDependency, final GenerateResult.TY aTy) {
			__tos = aText;

			text = aText.getBuffer().getText();
			ty   = aTy;

			moduleDependency = new NG_OutDep(aModuleDependency);
		}

		@Override
		public @NotNull EX_Explanation getExplanation() {
			return EX_Explanation.withMessage("NG_OutputClassStatement");
		}

		@Override
		@NotNull
		public EIT_ModuleInput getModuleInput() {
			var m = moduleDependency().module();

			final EIT_ModuleInput moduleInput = new EIT_ModuleInput(m, m.getCompilation());
			return moduleInput;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public @NotNull GenerateResult.TY getTy() {
			return ty;
		}

		public NG_OutDep moduleDependency() {
			return moduleDependency;
		}
	}

	public class NG_OutputFunction /*implements NG_OutputItem*/ {
		private List<C2C_Result> collect;
		private GenerateFiles    generateFiles;
		private BaseEvaFunction  gf;

//		@Override
		public @NotNull List<NG_OutputStatement> getOutputs() {
			final List<NG_OutputStatement> r = new ArrayList<>();

			if (collect != null) {
				for (C2C_Result c2c : collect) {
					final EG_Statement      x = c2c.getStatement();
					final GenerateResult.TY y = c2c.ty();

					r.add(new NG_OutputFunctionStatement(c2c));
				}
			}

			return r;
		}

//		@Override
		public EOT_OutputFile.FileNameProvider outName(final @NotNull OutputStrategyC aOutputStrategyC, final GenerateResult.@NotNull TY ty) {
			if (gf instanceof EvaFunction)
				return aOutputStrategyC.nameForFunction1((EvaFunction) gf, ty);
			else
				return aOutputStrategyC.nameForConstructor1((EvaConstructor) gf, ty);
		}

		public void setFunction(final BaseEvaFunction aGf, final GenerateFiles aGenerateFiles, final List<C2C_Result> aCollect) {
			gf            = aGf;
			generateFiles = aGenerateFiles;
			collect       = aCollect;
		}
	}

	public class NG_OutputFunctionStatement implements NG_OutputStatement {
		private final          EG_Statement      x;
		private final          GenerateResult.TY y;
		private final @NotNull NG_OutDep         moduleDependency;
		private final @NotNull C2C_Result   __c2c;

		public NG_OutputFunctionStatement(final @NotNull C2C_Result ac2c) {
			__c2c = ac2c;

			x = __c2c.getStatement();
			y = __c2c.ty();

			moduleDependency = new NG_OutDep(ac2c.getDefinedModule());
		}

		@Override
		public @NotNull EX_Explanation getExplanation() {
			return EX_Explanation.withMessage("NG_OutputFunctionStatement");
		}

		@Override
		@NotNull
		public EIT_ModuleInput getModuleInput() {
			var m = moduleDependency().module();

			final EIT_ModuleInput moduleInput = new EIT_ModuleInput(m, m.getCompilation());
			return moduleInput;
		}

		@Override
		public String getText() {
			return x.getText();
		}

		@Override
		public GenerateResult.TY getTy() {
			return y;
		}

		public NG_OutDep moduleDependency() {
			return moduleDependency;
		}
	}

	public interface NG_OutputStatement extends EG_Statement {

		EIT_ModuleInput getModuleInput();

		GenerateResult.TY getTy();

		// promise filename
		// promise EOT_OutputFile
	}

	private final @NotNull IPipelineAccess pa;

	@Contract(pure = true)
	public DefaultGenerateResultSink(final @NotNull IPipelineAccess pa0) {
		pa = pa0;
	}

	@Override
	public void add(final EvaNode node) {
		throw new IllegalStateException("Error");
	}

	@Override
	public void addClass_0(final GarishClass aGarishClass, final Buffer aImplBuffer, final Buffer aHeaderBuffer) {
		throw new IllegalStateException("Error");
	}

	@Override
	public void addClass_1(final @NotNull GarishClass aGarishClass,
						   final @NotNull GenerateResult gr,
						   final @NotNull GenerateC aGenerateC) {
		NG_OutputClass o = new NG_OutputClass();
		o.setClass(aGarishClass, aGenerateC);
//		pa.addOutput(o);

		System.err.println("5858 [DefaultGenerateResultSink::addClass_1] "+o.getOutputs());
	}

	@Override
	public void addFunction(final BaseEvaFunction aGf, final List<C2C_Result> aRs, final GenerateFiles aGenerateFiles) {
		NG_OutputFunction o = new NG_OutputFunction();
		o.setFunction(aGf, aGenerateFiles, aRs);
//		pa.addOutput(o);

		System.err.println("5866 [DefaultGenerateResultSink::addFunction] "+o.getOutputs());
	}

	@Override
	public void additional(final @NotNull GenerateResult aGenerateResult) {
		//throw new IllegalStateException("Error");
	}

	@Override
	public void addNamespace_0(final @NotNull GarishNamespace aGarishNamespace, final Buffer aImplBuffer, final Buffer aHeaderBuffer) {
		throw new IllegalStateException("Error");
	}

	@Override
	public void addNamespace_1(final @NotNull GarishNamespace aGarishNamespace,
							   final @NotNull GenerateResult gr,
							   final @NotNull GenerateC aGenerateC) {
//		NG_OutputNamespace o = new NG_OutputNamespace();
//		o.setNamespace(aGarishNamespace, aGenerateC);
//		pa.addOutput(o);
		throw new NotImplementedException();
	}

	@Override
	public LivingClass getLivingClassForEva(final EvaClass aEvaClass) {
		return pa.getCompilation().world().getClass(aEvaClass);
	}

	@Override
	public LivingNamespace getLivingNamespaceForEva(final EvaNamespace aEvaNamespace) {
		return pa.getCompilation().world().getNamespace(aEvaNamespace);
	}

}
