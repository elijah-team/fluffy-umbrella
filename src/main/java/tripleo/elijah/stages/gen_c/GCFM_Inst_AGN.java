package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_c.statements.ReasonedStringListStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;

import java.util.function.Supplier;

class GCFM_Inst_AGN implements GenerateC_Statement {

	private final GenerateC                 gc;
	private final Generate_Code_For_Method  generateCodeForMethod;
	private final WhyNotGarish_BaseFunction yf;
	private final Instruction               instruction;
	private final BaseEvaFunction           gf;
	private       boolean                   _calculated;

	private String _calculatedText;

	public GCFM_Inst_AGN(final Generate_Code_For_Method aGenerateCodeForMethod,
						 final GenerateC aGc,
						 final WhyNotGarish_BaseFunction aGf,
						 final Instruction aInstruction) {
		generateCodeForMethod = aGenerateCodeForMethod;
		gc                    = aGc;
		yf                    = aGf;
		instruction           = aInstruction;

		this.gf = yf.cheat();
	}

	@Override
	public String getText() {
		if (!_calculated) {
			final InstructionArgument target = instruction.getArg(0);
			final InstructionArgument value  = instruction.getArg(1);

			if (target instanceof IntegerIA) {
				final String realTarget      = gc.getRealTargetName(gf, (IntegerIA) target, Generate_Code_For_Method.AOG.ASSIGN);
				final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);

				var z = new ReasonedStringListStatement();
				z.append(Emit.emit("/*267*/"), "emit-code");
				z.append(realTarget, "real-target");
				z.append(" = ", "equals-sign");
				z.append(assignmentValue, "assignment-value");
				z.append(";", "closing-semi");

				_calculatedText = z.getText();
			} else {
				final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);

				var zz = new ReasonedStringListStatement();
				zz.append(Emit.emit("/*501*/"), "emit-code");
				zz.append(()->gc.getRealTargetName(gf, (IdentIA) target, Generate_Code_For_Method.AOG.ASSIGN, assignmentValue), "real-target");

				var z = new ReasonedStringListStatement();
				z.append(Emit.emit("/*249*/"), "emit-code");
				z.append(zz, "real-target");
				z.append(" = ", "equals-sign");
				z.append(assignmentValue, "assignment-value");
				z.append(";", "closing-semi");

				_calculatedText = z.getText();
			}
			_calculated = true;
		}

		assert _calculatedText != null;
		return _calculatedText;
	}

	public String getText2() {
		if (!_calculated) {
			var z = new ReasonedStringListStatement();

			final InstructionArgument target = instruction.getArg(0);
			final InstructionArgument value  = instruction.getArg(1);

			if (target instanceof IntegerIA integerIA) {
				final Supplier<String> realTargetSupplier      = () -> gc.getRealTargetName(gf, integerIA, Generate_Code_For_Method.AOG.ASSIGN);
				final Supplier<String> assignmentValueSupplier = () -> gc.getAssignmentValue(gf.getSelf(), value, gf);

				z.append(realTargetSupplier, "real-target-name");
				z.append(assignmentValueSupplier, "assignment-value");

				_calculatedText = String.format(Emit.emit("/*267*/") + "%s = %s;", realTargetSupplier.get(), assignmentValueSupplier.get());
			} else {
				final Supplier<String> assignmentValueSupplier = () -> gc.getAssignmentValue(gf.getSelf(), value, gf);
				final Supplier<String> s                       = () -> gc.getRealTargetName(gf, (IdentIA) target, Generate_Code_For_Method.AOG.ASSIGN, assignmentValueSupplier.get());

				final String realTargetName = s.get();

				final String s2             = Emit.emit("/*501*/") + realTargetName;

				_calculatedText = String.format(Emit.emit("/*249*/") + "%s = %s;", s2, assignmentValueSupplier.get());
			}
			_calculated = true;
		}

		return _calculatedText;
	}

	@Override
	public @NotNull GCR_Rule rule() {
		return GCR_Rule.withMessage("GCFM_Inst_AGN");
	}
}
