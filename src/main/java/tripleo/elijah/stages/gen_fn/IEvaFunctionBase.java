package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.DeduceElement;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.instructions.*;
import tripleo.util.range.Range;

import java.util.List;

public interface IEvaFunctionBase {
	String getIdentIAPathNormal(IdentIA ia2);

	@NotNull List<Instruction> instructions();

	Instruction getInstruction(int anIndex);

	int add(InstructionName aName, List<InstructionArgument> args_, Context ctx);

	@NotNull Label addLabel();

	@NotNull Label addLabel(String base_name, boolean append_int);

	void place(@NotNull Label label);

	@NotNull List<Label> labels();

	@NotNull VariableTableEntry getVarTableEntry(int index);

	@NotNull IdentTableEntry getIdentTableEntry(int index);

	@NotNull ConstantTableEntry getConstTableEntry(int index);

	@NotNull TypeTableEntry getTypeTableEntry(int index);

	@NotNull ProcTableEntry getProcTableEntry(int index);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, TableEntryIV aTableEntryIV);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, IExpression expression);

	@NotNull TypeTableEntry newTypeTableEntry(TypeTableEntry.Type type1, OS_Type type, IExpression expression, TableEntryIV aTableEntryIV);

	void addContext(Context context, Range r);

	Context getContextFromPC(int pc);

	@Nullable InstructionArgument vte_lookup(String text);

	@NotNull InstructionArgument get_assignment_path(@NotNull IExpression expression,
													 @NotNull GenerateFunctions generateFunctions,
													 Context context);

	int nextTemp();

	@Nullable Label findLabel(int index);

	VariableTableEntry getSelf();

	IdentTableEntry getIdentTableEntryFor(IExpression expression);

	int addIdentTableEntry(IdentExpression ident, Context context);

	int addVariableTableEntry(String name, VariableTableType vtt, TypeTableEntry type, OS_Element el);

	int getCode();

	void setCode(int aCode);

	EvaContainerNC getParent();

	void setParent(EvaContainerNC aGeneratedContainerNC);

	void setClass(@NotNull EvaNode aNode);

	EvaNode getGenClass();

	BaseFunctionDef getFD();

	Promise<GenType, Void, Void> typePromise();

	DeferredObject<GenType, Void, Void> typeDeferred();

	String expectationString();

	void resolveTypeDeferred(GenType aType);

	void addElement(OS_Element aElement, DeduceElement aDeduceElement);

	String getFunctionName();

	Dependency getDependency();
}
