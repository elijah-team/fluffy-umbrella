/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import io.reactivex.rxjava3.subjects.Subject;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.DotExpression;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.stages.deduce.DT_Resolvable;
import tripleo.elijah.stages.deduce.DeduceElement;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.OnGenClass;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.deduce.nextgen.DR_Item;
import tripleo.elijah.stages.deduce.nextgen.DR_Variable;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.gen_generic.IDependencyReferent;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.Label;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.Eventual;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.i.LivingFunction;
import tripleo.elijah.world.impl.DefaultLivingFunction;
import tripleo.util.range.Range;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface BaseEvaFunction extends DependencyTracker, EvaNode, DeduceTypes2.ExpectationBase, IDependencyReferent {
	public static @NotNull List<DT_Resolvable> _getIdentIAResolvableList(@NotNull InstructionArgument oo) {
		LinkedList<DT_Resolvable> R = new LinkedList<>();
		while (oo != null) {
			if (oo instanceof IntegerIA integerIA) {
				var vte = integerIA.getEntry();

				if (vte._vs == null) {
					final OS_Element[] el = {null};
					vte._p_elementPromise.then(el1 -> el[0] = el1);

					assert el[0] != null;

					R.addFirst(DT_Resolvable.from(oo, el[0], null));
				} else {
					R.addFirst(DT_Resolvable.from(oo, vte._vs, null));
				}
				oo = null;
			} else if (oo instanceof final IdentIA identIA) {
				final IdentTableEntry ite1 = identIA.getEntry();

				final OS_Element[] el = {null};
				ite1._p_resolvedElementPromise.then(el1 -> el[0] = el1);

				//assert el[0] != null;

				FunctionInvocation cfi = null;
				if (ite1._callable_pte() != null) {
					var cpte = ite1._callable_pte();
					if (cpte.getFunctionInvocation() != null) {
						cfi = cpte.getFunctionInvocation();
					}
				}

				//assert cfi != null;
				// ^^ fails for folders.forEach

				R.addFirst(DT_Resolvable.from(oo, el[0], cfi));
				oo = ite1.getBacklink();
			} else if (oo instanceof ProcIA procIA) {
				var pte = procIA.getEntry();
				assert pte != null;

				final OS_Element[] el = {null};
				pte._p_elementPromise.then(el1 -> el[0] = el1);

				assert el[0] != null;

				FunctionInvocation cfi = null;
				if (pte.getFunctionInvocation() != null) {
					cfi = pte.getFunctionInvocation();
				}

				assert cfi != null;

				R.addFirst(DT_Resolvable.from(oo, el[0], cfi));
				oo = null;
			} else
				throw new IllegalStateException("Invalid InstructionArgument");
		}
		return R;
	}

	String getIdentIAPathNormal(IdentIA ia2);

	@NotNull VariableTableEntry getVarTableEntry(int index);

	@NotNull IdentTableEntry getIdentTableEntry(int index);

	@NotNull ProcTableEntry getProcTableEntry(int index);

	@NotNull List<Instruction> instructions();

	Instruction getInstruction(int anIndex);

	int add(InstructionName aName, List<InstructionArgument> args_, Context ctx);

	@NotNull Label addLabel();

	@NotNull Label addLabel(String base_name, boolean append_int);

	void place(@NotNull Label label);

	@NotNull List<Label> labels();

	@NotNull ConstantTableEntry getConstTableEntry(int index);

	@NotNull TypeTableEntry getTypeTableEntry(int index);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, IExpression expression, TableEntryIV aTableEntryIV);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, IExpression expression);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, TableEntryIV aTableEntryIV);

	void addContext(Context context, Range r);

	Context getContextFromPC(int pc);

	@Nullable InstructionArgument vte_lookup(String text);

	@NotNull InstructionArgument get_assignment_path(@NotNull IExpression expression,
	                                                 @NotNull GenerateFunctions generateFunctions,
	                                                 @NotNull Context context);

	int nextTemp();

	@Nullable Label findLabel(int index);

	IdentTableEntry getIdentTableEntryFor(IExpression expression);

	int addIdentTableEntry(IdentExpression ident, Context context);

	int addVariableTableEntry(String name, VariableTableType vtt, TypeTableEntry type, OS_Element el);

	@Override
	String identityString();

	@Override
	OS_Module module();

	@NotNull BaseFunctionDef getFD();

	int getCode();

	void setCode(int aCode);

	EvaContainerNC getParent();

	void setParent(EvaContainerNC aGeneratedContainerNC);

	EvaNode getGenClass();

	/*
	 * Hook in for EvaClass
	 */
	void onGenClass(OnGenClass aOnGenClass);

	void setClass(@NotNull EvaNode aNode);
	void onType(DoneCallback<GenType> cb);

	void resolveTypeDeferred(@NotNull GenType aType);

	@Override
	String expectationString();

	@Nullable VariableTableEntry getSelf();

	void addElement(OS_Element aElement, DeduceElement aDeduceElement);

	String getFunctionName();

	Dependency getDependency();

	void setLiving(DefaultLivingFunction aLiving);

	void addDependentFunction(FunctionInvocation aFunctionInvocation);

	void addDependentType(GenType aGenType);

	ProcTableEntry fi_pte();

	boolean deferred_calls_contains(int aPc);

	FunctionInvocation fi();

	List<ProcTableEntry> prte_list();

	List<VariableTableEntry> vte_list();

	List<IdentTableEntry> idte_list();

	List<ConstantTableEntry> cte_list();

	List<TypeTableEntry> tte_list();

	@NotNull List<Integer> deferred_calls();

	Subject<GenType> dependentTypesSubject();

	Subject<FunctionInvocation> dependentFunctionSubject();

	@NotNull
	default InstructionArgument get_assignment_path(@NotNull InstructionArgument prev,
	                                                @NotNull IExpression expression,
	                                                @NotNull GenerateFunctions generateFunctions,
	                                                @NotNull Context context) {
		switch (expression.getKind()) {
		case DOT_EXP: {
			final DotExpression                de        = (DotExpression) expression;
			final @NotNull InstructionArgument left_part = get_assignment_path(de.getLeft(), generateFunctions, context);
			if (left_part instanceof IdentIA) {
				((IdentIA) left_part).setPrev(prev);
//				getIdentTableEntry(to_int(left_part)).addStatusListener(new DeduceTypes2.FoundParent());
			} else
				throw new NotImplementedException();
			return get_assignment_path(left_part, de.getRight(), generateFunctions, context);
		}
		case QIDENT:
			throw new NotImplementedException();
		case PROCEDURE_CALL:
			throw new NotImplementedException();
		case GET_ITEM:
			throw new NotImplementedException();
		case IDENT: {
			final IdentExpression ie      = (IdentExpression) expression;
			final int             ite     = addIdentTableEntry(ie, context);
			final IdentIA         identIA = new IdentIA(ite, this);
			identIA.setPrev(prev);
//			getIdentTableEntry(ite).addStatusListener(new DeduceTypes2.FoundParent()); // inject!
			return identIA;
		}
		default:
			throw new IllegalStateException("Unexpected value: " + expression.getKind());
		}
	}

	static void printTables(EvaFunction gf) {
		tripleo.elijah.util.Stupidity.println_out("VariableTable ");
		for (final VariableTableEntry variableTableEntry : gf.vte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + variableTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("ConstantTable ");
		for (final ConstantTableEntry constantTableEntry : gf.cte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + constantTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("ProcTable     ");
		for (final ProcTableEntry procTableEntry : gf.prte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + procTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("TypeTable     ");
		for (final TypeTableEntry typeTableEntry : gf.tte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + typeTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("IdentTable    ");
		for (final IdentTableEntry identTableEntry : gf.idte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + identTableEntry);
		}
	}

	static @NotNull List<InstructionArgument> _getIdentIAPathList(@NotNull InstructionArgument oo) {
		final LinkedList<InstructionArgument> s = new LinkedList<InstructionArgument>();
		while (oo != null) {
			if (oo instanceof IntegerIA) {
				s.addFirst(oo);
				oo = null;
			} else if (oo instanceof IdentIA) {
				final IdentTableEntry ite1 = ((IdentIA) oo).getEntry();
				s.addFirst(oo);
				oo = ite1.getBacklink();
			} else if (oo instanceof ProcIA) {
				s.addFirst(oo);
				oo = null;
			} else
				throw new IllegalStateException("Invalid InstructionArgument");
		}
		return s;
	}

	void addProcTableEntry(ProcTableEntry aPte);

	void addConstantTableEntry(ConstantTableEntry aCte);

	void add_vte(VariableTableEntry aVte);

	Eventual<GenType> typePromise();

	DR_Ident getIdent(IdentTableEntry aIdentTableEntry);

	DR_Ident getIdent(IdentExpression aIdent, VariableTableEntry aVteBl1);

	DR_Ident getIdent(VariableTableEntry aVteBl1);

	DR_Variable getVar(VariableStatement aElement);

	List<? extends DR_Item> drs();

	IdentIaResolveable _getIdentIAResolvable(IdentIA aIdentIA);

	Reactive reactive();

	Map<OS_Element, DeduceElement> elements();

	void setLiving(LivingFunction aLivingFunction);
}
