package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_VTE;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.VariableTableType;

import java.io.PrintStream;
import java.util.List;

public class DeduceElement3_VariableTableEntry implements IDeduceElement3 {

	private final VariableTableEntry principal;
	private       DeduceTypes2       deduceTypes2;
	private       GeneratedFunction  generatedfunction;
	private       GenType            genType;

	@Contract(pure = true)
	public DeduceElement3_VariableTableEntry(final VariableTableEntry aVariableTableEntry) {
		principal = aVariableTableEntry;
	}

	DeduceElement3_VariableTableEntry(final OS_Type vte_type_attached) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 aDeduceTypes2) {
		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public OS_Element getPrincipal() {
		return principal.getDeduceElement3().getPrincipal();
	}

	@Override
	public DED elementDiscriminator() {
		return new DED_VTE(principal);
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public BaseGeneratedFunction generatedFunction() {
		return generatedfunction;
	}

	@Override
	public GenType genType() {
		return genType;
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__VTE;
	}

	public Operation2<OS_Type> decl_test_001(final BaseGeneratedFunction gf) {
		final VariableTableEntry vte = principal;

		final OS_Type x = vte.type.getAttached();
		if (x == null && vte.potentialTypes().size() == 0) {
			final Diagnostic diag;
			if (vte.vtt == VariableTableType.TEMP) {
				diag = new Diagnostic_8884(vte, gf);
			} else {
				diag = new Diagnostic_8885(vte);
			}
			return Operation2.failure(diag);
		}

		return Operation2.success(x);
	}

	private static class Diagnostic_8884 implements GCFM_Diagnostic {
		private final VariableTableEntry vte;
		private final BaseGeneratedFunction gf;
		private final int _code = 8884;

		public Diagnostic_8884(final VariableTableEntry aVte, final BaseGeneratedFunction aGf) {
			vte = aVte;
			gf = aGf;
		}

		@Override
		public String code() {
			return "" + _code;
		}

		@Override
		public Severity severity() {
			return Severity.ERROR;
		}

		@Override
		public @NotNull Locatable primary() {
			return null;
		}

		@Override
		public @NotNull List<Locatable> secondary() {
			return null;
		}

		@Override
		public void report(final PrintStream stream) {
			stream.printf(_message());
		}

		@Override
		public String _message() {
			return String.format("%d temp variable has no type %s %s", _code, vte, gf);
		}
	}

	private static class Diagnostic_8885 implements GCFM_Diagnostic {
		private final VariableTableEntry vte;
		private final int _code = 8885;

		public Diagnostic_8885(final VariableTableEntry aVte) {
			vte = aVte;
		}

		@Override
		public String code() {
			return "" + _code;
		}

		@Override
		public Severity severity() {
			return Severity.ERROR;
		}

		@Override
		public @NotNull Locatable primary() {
			return null;
		}

		@Override
		public @NotNull List<Locatable> secondary() {
			return null;
		}

		@Override
		public void report(final @NotNull PrintStream stream) {
			stream.printf(_message());
		}

		@Override
		public String _message() {
			return String.format("%d x is null (No typename specified) for %s%n", _code, vte.getName());
		}
	}
}
