package tripleo.elijah.stages.gen_c;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.DefFunctionDef;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.PropertyStatement;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.util.NotImplementedException;

class CReference_getIdentIAPath_IdentIAHelper {
	class CodeResolver {
		private int    code;
		private String reason;
		private boolean is_set;
		private boolean is_anti;

		public void anti_provide(final int _code, final String _reason) {
			code   = _code;
			reason = _reason;
			is_anti = true;
		}

		int getCode() {
			return code;
		}

		public boolean isAnti() {
			return is_anti;
		}

		public boolean isSet() {
			return is_set;
		}

		public void provide(final @NotNull EvaContainerNC aNc, final String aReason) {
			code   = aNc.getCode();
			reason = aReason;
			is_set = true;
		}

		public void provide(int _code, String _reason) {
			code   = _code;
			reason = _reason;
			is_set = true;
		}
	}
	interface ICodeResolver {
		int getCode();
	}
	@Contract(pure = true)
	private static void _act_AliasStatement() {
		final int y = 2;
		NotImplementedException.raise();
		//			text = Emit.emit("/*167*/")+((AliasStatementImpl)resolved_element).name();
		//			return _getIdentIAPath_IdentIAHelper(text, sl, i, sSize, _res)
	}
	private final BaseEvaFunction     generatedFunction;
	private final int                 i;
	private final EvaNode             resolved;
	private final OS_Element          resolved_element;
	private final List<String>        sl;
	private final InstructionArgument ia_next;

	private final int                 sSize;

	private final String              value;

	public        int                 code = -1;

	@Contract(pure = true)
	CReference_getIdentIAPath_IdentIAHelper(final InstructionArgument ia_next, final List<String> sl, final int i, final int sSize, final OS_Element resolved_element, final BaseEvaFunction generatedFunction, final EvaNode aResolved, final String aValue) {
		this.ia_next           = ia_next;
		this.sl                = sl;
		this.i                 = i;
		this.sSize             = sSize;
		this.resolved_element  = resolved_element;
		this.generatedFunction = generatedFunction;
		resolved               = aResolved;
		value                  = aValue;
	}

	private boolean _act_ClassStatement(final @NotNull CReference aCReference, boolean b) {
		// Assuming constructor call
		final int code;
		if (getResolved() != null) {
			code = ((EvaContainerNC) getResolved()).getCode();
		} else {
			code = -1;
			tripleo.elijah.util.Stupidity.println_err("** 31116 not resolved " + getResolved_element());
		}
		// README might be calling reflect or Type or Name
		// TODO what about named constructors -- should be called with construct keyword
		if (getIa_next() instanceof IdentIA) {
			final IdentTableEntry ite  = ((IdentIA) getIa_next()).getEntry();
			final String          text = ite.getIdent().getText();
			if (text.equals("reflect")) {
				b = true;
				final String text2 = String.format("ZS%d_reflect", code);
				aCReference.addRef(text2, CReference.Ref.FUNCTION);
			} else if (text.equals("Type")) {
				b = true;
				final String text2 = String.format("ZST%d", code); // return a TypeInfo structure
				aCReference.addRef(text2, CReference.Ref.FUNCTION);
			} else if (text.equals("Name")) {
				b = true;
				final String text2 = String.format("ZSN%d", code);
				aCReference.addRef(text2, CReference.Ref.FUNCTION); // TODO make this not a function
			} else {
				assert getI() == getsSize() - 1; // Make sure we are ending with a constructor call
				// README Assuming this is for named constructors
				final String text2 = String.format("ZC%d%s", code, text);
				aCReference.addRef(text2, CReference.Ref.CONSTRUCTOR);
			}
		} else {
			assert getI() == getsSize() - 1; // Make sure we are ending with a constructor call
			final String text2 = String.format("ZC%d", code);
			aCReference.addRef(text2, CReference.Ref.CONSTRUCTOR);
		}
		return b;
	}

	private void _act_ConstructorDef(final @NotNull CReference aCReference) {
		assert getI() == getsSize() - 1; // Make sure we are ending with a constructor call
		final int code;
		if (getResolved() != null) {
			code = ((BaseEvaFunction) getResolved()).getCode();
		} else {
			code = -1;
			tripleo.elijah.util.Stupidity.println_err("** 31161 not resolved " + getResolved_element());
		}
		// README Assuming this is for named constructors
		final String text  = ((ConstructorDef) getResolved_element()).name();
		final String text2 = String.format("ZC%d%s", code, text);
		aCReference.addRef(text2, CReference.Ref.CONSTRUCTOR);
	}

	private void _act_DefFunctionDef(final @NotNull CReference aCReference) {
		final OS_Element parent = getResolved_element().getParent();
		int        code   = -100;

		var cr = new CodeResolver();

		if (getResolved() != null) {
			if ((getResolved() instanceof BaseEvaFunction rf)) {
				final EvaNode gc = rf.getGenClass();
				if (gc instanceof final EvaContainerNC nc) // and not another function
				{
					code = nc.getCode();

					cr.provide(nc, "_act_DefFunctionDef:getResolved-instanceof-BaseEvaFunction:genClass-instanceof-EvaContainerNC");
				} else {
					code = -2;

					cr.anti_provide(-2, "_act_DefFunctionDef:getResolved-instanceof-BaseEvaFunction:genClass-NOT-instanceof-EvaContainerNC");
				}
			} else {
				assert false;
			}
		} else {
			if (parent instanceof ClassStatement) {
				code = -3;
			} else if (parent instanceof NamespaceStatement) {
				code = -3;
			} else {
				// TODO what about FunctionDef, etc
				code = -1;
			}
		}
		// TODO what about overloaded functions
		assert getI() == getsSize() - 1; // Make sure we are ending with a ProcedureCall
		getSl().clear();
		if (code == -1) {
//				text2 = String.format("ZT%d_%d", enclosing_function._a.getCode(), closure_index);
		}
		final DefFunctionDef defFunctionDef = (DefFunctionDef) getResolved_element();
		final String         text2          = String.format("z%d%s", code, defFunctionDef.name());
		aCReference.addRef(text2, CReference.Ref.FUNCTION);
	}

	private void _act_FormalArgListItem(final @NotNull CReference aCReference, final @NotNull FormalArgListItem fali) {
		final int    y     = 2;
		final String text2 = "va" + fali.getNameToken().getText();
		aCReference.addRef(text2, CReference.Ref.LOCAL); // TODO
	}

	private void _act_FunctionDef(final @NotNull CReference aCReference) {
		final OS_Element parent        = getResolved_element().getParent();
		int              our_code      = -1;
		final EvaNode    resolved_node = getResolved();

		var cr = new CodeResolver();

		if (resolved_node != null) {
			if (resolved_node instanceof final @NotNull BaseEvaFunction resolvedFunction) {

				resolvedFunction.onGenClass((EvaClass gc) -> {
					EvaNode gc1 = resolvedFunction.getGenClass();
					if (gc instanceof EvaContainerNC) // and not another function
					{
						this.code = gc.getCode();

						cr.provide(gc, "_act_FunctionDef:getResolved-instanceof-BaseEvaFunction:genClass-instanceof-EvaContainerNC");

						assert this.code > 0;
					} else {
						this.code = -2;

						cr.anti_provide(-2, "_act_FunctionDef:getResolved-instanceof-BaseEvaFunction:genClass-NOT-instanceof-EvaContainerNC");
					}
				});

				// TODO 09/06 maybe remove?
				if (resolvedFunction.getGenClass() instanceof final @NotNull EvaNamespace generatedNamespace) {
					// FIXME sometimes genClass is not called so above wont work,
					//  so check if a code was set and use it here
					final int cc = generatedNamespace.getCode();
					if (cc > 0) {
						this.code = cc;

						assert this.code == cc;
					}
				}

			} else if (resolved_node instanceof final @NotNull EvaClass generatedClass) {
				this.code = generatedClass.getCode();

				cr.provide(generatedClass, "_act_DefFunctionDef:getResolved-instanceof-EvaClass");
			}
		}
		// TODO what about overloaded functions
		assert getI() == getsSize() - 1; // Make sure we are ending with a ProcedureCall
		getSl().clear();

		if (!cr.isSet()) {
			// README happens because onGenClass isn't resolved
			our_code = -1;
		} else {
			assert cr.isSet();
			assert !cr.isAnti();

			assert this.code == cr.getCode();

			our_code = cr.getCode(); //this.code;
		}

		// TODO CodeProviderTarget/EG_Statement
		final String text2 = String.format("z%d%s", our_code, ((FunctionDef) getResolved_element()).name());
		aCReference.addRef(text2, CReference.Ref.FUNCTION);
	}

	private void _act_PropertyStatement(final @NotNull CReference aCReference) {
		getSl().clear();  // don't we want all the text including from sl?

		final PropertyStatement ps = (PropertyStatement) getResolved_element();

		final GCS_Property_Get propertyGet = new GCS_Property_Get(ps);
		final String           text2       = propertyGet.getText();

		aCReference.addRef(text2, CReference.Ref.PROPERTY_GET);

		aCReference.__cheat_ret = text2;
	}

	private void _act_VariableStatement(final @NotNull CReference aCReference) {
		final VariableStatement variableStatement = (VariableStatement) getResolved_element();

		GI_VariableStatement givs = (GI_VariableStatement) aCReference._repo().itemFor(variableStatement);
		givs._createReferenceForVariableStatement(aCReference, getGeneratedFunction(), getValue());
	}

	boolean action(final CRI_Ident aCRI_ident, final @NotNull CReference aCReference) {
		boolean          b               = false;
		final OS_Element resolvedElement = getResolved_element();

		if (resolvedElement instanceof ClassStatement) {
			b = _act_ClassStatement(aCReference, b);
		} else if (resolvedElement instanceof ConstructorDef) {
			_act_ConstructorDef(aCReference);
		} else if (resolvedElement instanceof FunctionDef) {
			_act_FunctionDef(aCReference);
		} else if (resolvedElement instanceof DefFunctionDef) {
			_act_DefFunctionDef(aCReference);
		} else if (resolvedElement instanceof VariableStatement) {
			_act_VariableStatement(aCReference);
		} else if (resolvedElement instanceof PropertyStatement) {
			_act_PropertyStatement(aCReference);
		} else if (resolvedElement instanceof AliasStatement) {
			_act_AliasStatement();
		} else if (resolvedElement instanceof final FormalArgListItem fali) {
			_act_FormalArgListItem(aCReference, fali);
		} else {
			// text = idte.getIdent().getText();
			tripleo.elijah.util.Stupidity.println_out("1008 " + resolvedElement.getClass().getName());
			throw new NotImplementedException();
		}
		return b;
	}

	@Contract(pure = true)
	public BaseEvaFunction getGeneratedFunction() {
		return generatedFunction;
	}

	@Contract(pure = true)
	public int getI() {
		return i;
	}

	@Contract(pure = true)
	public InstructionArgument getIa_next() {
		return ia_next;
	}

	@Contract(pure = true)
	public EvaNode getResolved() {
		return resolved;
	}

	@Contract(pure = true)
	public OS_Element getResolved_element() {
		return resolved_element;
	}

	@Contract(pure = true)
	public List<String> getSl() {
		return sl;
	}

	@Contract(pure = true)
	public int getsSize() {
		return sSize;
	}

	@Contract(pure = true)
	public String getValue() {
		return value;
	}
}
