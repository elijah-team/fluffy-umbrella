/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.Holder;
import tripleo.elijah.util.NotImplementedException;
import tripleo.util.range.Range;

import java.util.*;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 9/10/20 2:57 PM
 */
public abstract class BaseGeneratedFunction extends AbstractDependencyTracker implements GeneratedNode, DeduceTypes2.ExpectationBase {
	public boolean deducedAlready;
	public FunctionInvocation fi;
	private int code = 0;
	private final List<Label> labelList = new ArrayList<Label>();
	public @NotNull List<Instruction> instructionsList = new ArrayList<Instruction>();
	public @NotNull List<Integer> deferred_calls = new ArrayList<Integer>();
	private int instruction_index = 0;
	public @NotNull List<ConstantTableEntry> cte_list = new ArrayList<ConstantTableEntry>();
	public @NotNull List<VariableTableEntry> vte_list = new ArrayList<VariableTableEntry>();
	public @NotNull List<ProcTableEntry> prte_list = new ArrayList<ProcTableEntry>();
	public @NotNull List<TypeTableEntry> tte_list = new ArrayList<TypeTableEntry>();
	public @NotNull List<IdentTableEntry> idte_list = new ArrayList<IdentTableEntry>();
	private int label_count = 0;
	private int _nextTemp = 0;
	private GeneratedNode genClass;
	private GeneratedContainerNC parent;
	Map<OS_Element, DeduceElement> elements = new HashMap<OS_Element, DeduceElement>();
	private DeferredObject<GenType, Void, Void> typeDeferred = new DeferredObject<GenType, Void, Void>();
	private DeferredObject<GeneratedClass, Void, Void> _gcP = new DeferredObject<>();

	//
	// region Ident-IA
	//

	public static void printTables(GeneratedFunction gf) {
		tripleo.elijah.util.Stupidity.println_out("VariableTable ");
		for (VariableTableEntry variableTableEntry : gf.vte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + variableTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("ConstantTable ");
		for (ConstantTableEntry constantTableEntry : gf.cte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + constantTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("ProcTable     ");
		for (ProcTableEntry procTableEntry : gf.prte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + procTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("TypeTable     ");
		for (TypeTableEntry typeTableEntry : gf.tte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + typeTableEntry);
		}
		tripleo.elijah.util.Stupidity.println_out("IdentTable    ");
		for (IdentTableEntry identTableEntry : gf.idte_list) {
			tripleo.elijah.util.Stupidity.println_out("\t" + identTableEntry);
		}
	}

	public static @NotNull List<InstructionArgument> _getIdentIAPathList(@NotNull InstructionArgument oo) {
		LinkedList<InstructionArgument> s = new LinkedList<InstructionArgument>();
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


	// endregion

	//
	// region INSTRUCTIONS
	//

	public @NotNull List<Instruction> instructions() {
		return instructionsList;
	}

	public Instruction getInstruction(final int anIndex) {
		return instructionsList.get(anIndex);
	}

	public int add(final InstructionName aName, final List<InstructionArgument> args_, final Context ctx) {
		final Instruction i = new Instruction();
		i.setIndex(instruction_index++);
		i.setName(aName);
		i.setArgs(args_);
		i.setContext(ctx);
		instructionsList.add(i);
		return i.getIndex();
	}

	// endregion

	//
	// region LABELS
	//

	public @NotNull Label addLabel() {
		return addLabel("__label", true);
	}

	public @NotNull Label addLabel(final String base_name, final boolean append_int) {
		final Label label = new Label(this);
		final String name;
		if (append_int) {
			label.setNumber(label_count);
			name = String.format("%s%d", base_name, label_count++);
		} else {
			name = base_name;
			label.setNumber(label_count);
		}
		label.setName(name);
		labelList.add(label);
		return label;
	}

	public void place(@NotNull final Label label) {
		label.setIndex(instruction_index);
	}

	public @NotNull List<Label> labels() {
		return labelList;
	}

	// endregion

	//
	// region get-entries
	//

	@NotNull public VariableTableEntry getVarTableEntry(final int index) {
		return vte_list.get(index);
	}

	@NotNull public IdentTableEntry getIdentTableEntry(final int index) {
		return idte_list.get(index);
	}

	@NotNull public ConstantTableEntry getConstTableEntry(final int index) {
		return cte_list.get(index);
	}

	@NotNull public TypeTableEntry getTypeTableEntry(final int index) {
		return tte_list.get(index);
	}

	@NotNull
	public ProcTableEntry getProcTableEntry(final int index) {
		return prte_list.get(index);
	}

	// endregion

	public @NotNull TypeTableEntry newTypeTableEntry(final TypeTableEntry.Type type1, final OS_Type type) {
		return newTypeTableEntry(type1, type, null, null);
	}

	/**
	 * Returns a string that represents the path encoded by ia2.
	 * Does not transform the string into target language (ie C).
	 * Called from {@link DeduceTypes2#do_assign_call(BaseGeneratedFunction, Context, IdentTableEntry, FnCallArgs, int)}
	 * or {@link DeduceTypes2#deduce_generated_function(GeneratedFunction)}
	 * or {@link DeduceTypes2#resolveIdentIA_(Context, IdentIA, BaseGeneratedFunction, FoundElement)}
	 *
	 * @param ia2 the path
	 * @return a string that represents the path encoded by ia2
	 */
	public String getIdentIAPathNormal(final IdentIA ia2) {
		final List<InstructionArgument> s = _getIdentIAPathList(ia2);

		//
		// TODO NOT LOOKING UP THINGS, IE PROPERTIES, MEMBERS
		//
		List<String> sl = new ArrayList<String>();
		for (final InstructionArgument ia : s) {
			final String text;
			if (ia instanceof IntegerIA) {
				final VariableTableEntry vte = getVarTableEntry(to_int(ia));
				text = vte.getName();
			} else if (ia instanceof IdentIA) {
				final IdentTableEntry idte = getIdentTableEntry(((IdentIA) ia).getIndex());
				text = idte.getIdent().getText();
			} else if (ia instanceof ProcIA) {
				final ProcTableEntry prte = getProcTableEntry(to_int(ia));
				assert prte.expression instanceof ProcedureCallExpression;
				text = ((ProcedureCallExpression) prte.expression).printableString();
			} else
				throw new NotImplementedException();
			sl.add(text);
		}
		return Helpers.String_join(".", sl);
	}

	public @NotNull TypeTableEntry newTypeTableEntry(final TypeTableEntry.Type type1, final OS_Type type, final IExpression expression) {
		return newTypeTableEntry(type1, type, expression, null);
	}

	public @NotNull TypeTableEntry newTypeTableEntry(final TypeTableEntry.Type type1, final OS_Type type, TableEntryIV aTableEntryIV) {
		return newTypeTableEntry(type1, type, null, aTableEntryIV);
	}

	public void addContext(final Context context, final Range r) {
//		contextToRangeMap.put(r, context);
	}

	public Context getContextFromPC(final int pc) {
//		for (Map.Entry<Range, Context> rangeContextEntry : contextToRangeMap.entrySet()) {
//			if (rangeContextEntry.getKey().has(pc))
//				return rangeContextEntry.getValue();
//		}
//		return null;
		return instructionsList.get(pc).getContext();
	}

//	Map<Range, Context> contextToRangeMap = new HashMap<Range, Context>();

	/**
	 *
	 * @param text variable name from the source file
	 * @return {@link IntegerIA} or {@link ConstTableIA} or null if not found, meaning not a local variable
	 */
	public @Nullable InstructionArgument vte_lookup(final String text) {
		int index = 0;
		for (final VariableTableEntry variableTableEntry : vte_list) {
			final String variableTableEntryName = variableTableEntry.getName();
			if (variableTableEntryName != null) // null when temp
				if (variableTableEntryName.equals(text))
					return new IntegerIA(index, this);
			index++;
		}
		index = 0;
		for (final ConstantTableEntry constTableEntry : cte_list) {
			final String constTableEntryName = constTableEntry.getName();
			if (constTableEntryName != null) // null when not assigned
				if (constTableEntryName.equals(text))
					return new ConstTableIA(index, this);
			index++;
		}
		return null;
	}

	public @NotNull TypeTableEntry newTypeTableEntry(final TypeTableEntry.Type type1, final OS_Type type, final IExpression expression, TableEntryIV aTableEntryIV) {
		final TypeTableEntry typeTableEntry = new TypeTableEntry(tte_list.size(), type1, type, expression, aTableEntryIV);
		typeTableEntry.setAttached(type); // README make sure tio call callback
		tte_list.add(typeTableEntry);
		return typeTableEntry;
	}

	private @NotNull InstructionArgument get_assignment_path(@NotNull final InstructionArgument prev,
															 @NotNull final IExpression expression,
															 @NotNull final GenerateFunctions generateFunctions,
															 @NotNull final Context context) {
		switch (expression.getKind()) {
			case DOT_EXP: {
				final DotExpression de = (DotExpression) expression;
			final InstructionArgument left_part = get_assignment_path(de.getLeft(), generateFunctions, context);
			if (left_part instanceof IdentIA) {
				((IdentIA)left_part).setPrev(prev);
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
		case IDENT:
		{
			final IdentExpression ie = (IdentExpression) expression;
			final int ite = addIdentTableEntry(ie, context);
			final IdentIA identIA = new IdentIA(ite, this);
			identIA.setPrev(prev);
//			getIdentTableEntry(ite).addStatusListener(new DeduceTypes2.FoundParent()); // inject!
			return identIA;
		}
		default:
			throw new IllegalStateException("Unexpected value: " + expression.getKind());
		}
	}

	public int nextTemp() {
		return ++_nextTemp;
	}

	public @Nullable Label findLabel(final int index) {
		for (final Label label : labelList) {
			if (label.getIndex() == index)
				return label;
		}
		return null;
	}

	public abstract VariableTableEntry getSelf();

	public @NotNull InstructionArgument get_assignment_path(@NotNull final IExpression expression,
															@NotNull final GenerateFunctions generateFunctions,
															Context context) {
		switch (expression.getKind()) {
			case DOT_EXP: {
				final DotExpression de = (DotExpression) expression;
				final InstructionArgument left_part = get_assignment_path(de.getLeft(), generateFunctions, context);
				return get_assignment_path(left_part, de.getRight(), generateFunctions, context);
			}
			case QIDENT:
				throw new NotImplementedException();
			case PROCEDURE_CALL: {
				ProcedureCallExpression pce = (ProcedureCallExpression) expression;
				if (pce.getLeft() instanceof IdentExpression) {
					final IdentExpression identExpression = (IdentExpression) pce.getLeft();
					int idte_index = addIdentTableEntry(identExpression, identExpression.getContext());
					final IdentIA identIA = new IdentIA(idte_index, this);
					final List<TypeTableEntry> args_types = generateFunctions.get_args_types(pce.getArgs(), this, context);
					int i = generateFunctions.addProcTableEntry(pce, identIA, args_types, this);
					return new ProcIA(i, this);
				}
				return get_assignment_path(pce.getLeft(), generateFunctions, context); // TODO this looks wrong. what are we supposed to be doing here?
			}
			case GET_ITEM:
				throw new NotImplementedException();
			case IDENT: {
				final IdentExpression ie = (IdentExpression) expression;
				final String text = ie.getText();
				final InstructionArgument lookup = vte_lookup(text); // IntegerIA(variable) or ConstTableIA or null
				if (lookup != null)
					return lookup;
				final int ite = addIdentTableEntry(ie, context);
				return new IdentIA(ite, this);
			}
			default:
				throw new IllegalStateException("Unexpected value: " + expression.getKind());
		}
	}

	public int addIdentTableEntry(final IdentExpression ident, final Context context) {
		for (int i = 0; i < idte_list.size(); i++) {
			if (idte_list.get(i).getIdent() == ident && idte_list.get(i).getPC() == context)
				return i;
		}
		final IdentTableEntry idte = new IdentTableEntry(idte_list.size(), ident, context);
		idte_list.add(idte);
		return idte.getIndex();
	}

	/**
	 * Returns first {@link IdentTableEntry} that matches expression
	 * Only works for IdentExpressions
	 *
	 * @param expression {@link IdentExpression} to test for
	 * @return IdentTableEntry or null
	 */
	public IdentTableEntry getIdentTableEntryFor(IExpression expression) {
		for (IdentTableEntry identTableEntry : idte_list) {
			// TODO make this work for Qualidents and DotExpressions
			if (identTableEntry.getIdent().getText().equals(((IdentExpression) expression).getText()) && identTableEntry.getBacklink() == null) {
				return identTableEntry;
			}
		}
		return null;
	}

	@Override
	public OS_Module module() {
		return getFD().getContext().module();
	}

	public int getCode() {
		return code;
	}

	public int addVariableTableEntry(final String name, final VariableTableType vtt, final TypeTableEntry type, OS_Element el) {
		final VariableTableEntry vte = new VariableTableEntry(vte_list.size(), vtt, name, type, el);
		vte_list.add(vte);
		return vte.getIndex();
	}

	public void setCode(int aCode) {
		code = aCode;
	}

	public GeneratedContainerNC getParent() {
		return parent;
	}

	public void setParent(GeneratedContainerNC aGeneratedContainerNC) {
		parent = aGeneratedContainerNC;
	}

	public GeneratedNode getGenClass() {
		return genClass;
	}

	public abstract BaseFunctionDef getFD();

	public Promise<GenType, Void, Void> typePromise() {
		return typeDeferred.promise();
	}

	public DeferredObject<GenType, Void, Void> typeDeferred() {
		return typeDeferred;
	}

	@Override
	public String expectationString() {
		return toString();
	}

	public void setClass(@NotNull GeneratedNode aNode) {
		assert aNode instanceof GeneratedClass || aNode instanceof GeneratedNamespace;
		genClass = aNode;

		if (aNode instanceof GeneratedClass) {
			_gcP.resolve((GeneratedClass) aNode);
		}
//		else
//			throw new RuntimeException("not implemented");
	}

	public void resolveTypeDeferred(final GenType aType) {
		if (typeDeferred.isPending())
			typeDeferred.resolve(aType);
		else {
			final Holder<GenType> holder = new Holder<GenType>();
			typeDeferred.then(new DoneCallback<GenType>() {
				@Override
				public void onDone(final GenType result) {
					holder.set(result);
				}
			});
			tripleo.elijah.util.Stupidity.println_err(String.format("Trying to resolve function twice 1) %s 2) %s", holder.get().asString(), aType.asString()));
		}
	}

	public void addElement(final OS_Element aElement, final DeduceElement aDeduceElement) {
		elements.put(aElement, aDeduceElement);
	}

	/*
	 * Hook in for GeneratedClass
	 */
	public void onGenClass(OnGenClass aOnGenClass) {
		_gcP.then(aOnGenClass::accept);
	}
}

//
//
//
