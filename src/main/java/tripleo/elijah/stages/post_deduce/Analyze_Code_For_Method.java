/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.post_deduce;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.gen_c.CReference;
import tripleo.elijah.stages.gen_c.Emit;
import tripleo.elijah.stages.gen_c.Generate_Code_For_Method;
import tripleo.elijah.stages.gen_fn.ConstantTableEntry;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 6/21/21 5:53 AM
 */
public class Analyze_Code_For_Method {
	public Analyze_Code_For_Method(@NotNull PostDeduce aPostDeduce) {
		pd = aPostDeduce;
	}

	PostDeduce pd;

	void analyzeCodeForMethod(GeneratedFunction gf, WorkList aWorkList) {
		action(gf);
	}

	boolean is_constructor = false, is_unit_type = false;

	private void action(GeneratedFunction gf) {
		Analyze_Method_Header gmh = new Analyze_Method_Header(gf, pd);

		@NotNull List<Instruction> instructions = gf.instructions();
		for (int instruction_index = 0; instruction_index < instructions.size(); instruction_index++) {
			Instruction instruction = instructions.get(instruction_index);
//			pd.log.info(instruction.lispy());
			final Label label = gf.findLabel(instruction.getIndex()); // TODO look
			if (label != null) {
				assert instruction.getIndex() == instruction_index;
				pd.log.info("Label {} at instructionIndex {}", label.getName(), instruction_index);
			}

			switch (instruction.getName()) {
				case E:
					action_E(gf, gmh);
					break;
				case X:
					action_X(gmh);
					break;
				case ES:
					action_ES();
					break;
				case XS:
					action_XS();
					break;
				case AGN:
					action_AGN(gf, instruction);
					break;
				case AGNK:
					action_AGNK(gf, instruction);
					break;
				case AGNT:
					break;
				case AGNF:
					break;
				case JE:
					action_JE(gf, instruction);
					break;
				case JNE:
					action_JNE(gf, instruction);
					break;
				case JL:
					action_JL(gf, instruction);
					break;
				case JMP:
					action_JMP(instruction);
					break;
				case CONSTRUCT:
					action_CONSTRUCT(gf, instruction);
					break;
				case CALL:
					action_CALL(gf, instruction);
					break;
				case CALLS:
					action_CALLS(gf, instruction);
					break;
				case RET:
					break;
				case YIELD:
					throw new NotImplementedException();
				case TRY:
					throw new NotImplementedException();
				case PC:
					break;
				case IS_A:
					action_IS_A(instruction, gf);
					break;
				case DECL:
					action_DECL(instruction, gf);
					break;
				case CAST_TO:
					action_CAST(instruction, gf);
					break;
				case NOP:
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + instruction.getName());
			}
		}
		pd.log.info("End function block");
	}

	private void action_XS() {
		pd.log.info("End ES/XS block");
	}

	private void action_ES() {
		pd.log.info("Begin ES/EX block");
	}

	private void action_X(Analyze_Method_Header aGmh) {
		pd.log.info("End E/X block");
		if (!is_unit_type)
			if (is_constructor || aGmh.tte != null && aGmh.tte.isResolved()) {
				pd.log.info("returning vsr");
			}
	}

	private void action_E(GeneratedFunction gf, Analyze_Method_Header aAmh) {
		int state = 0;

		if (gf.getFD() instanceof ConstructorDef)
			state = 2;
		else if (aAmh.tte == null)
			state = 3;
		else if (aAmh.tte.isResolved())
			state = 1;
		else if (aAmh.tte.getAttached() instanceof OS_Type.OS_UnitType)
			state = 4;

		switch (state) {
			case 0:
				pd.log.error("Function {} TTE_Not_Resolved {} for result", gf, aAmh.tte);
				break;
			case 1:
				String ty = pd.getTypeName(aAmh.tte);
				pd.log.info("Function {} Result type is {}", gf, ty);
				break;
			case 2:
			case 3:
				// Assuming ctor
				is_constructor = true;
				final GeneratedNode genClass = gf.getGenClass();
				String ty2 = pd.getTypeNameForGenClass(genClass);
				pd.log.info("Function {} Result type is {}", gf, ty2);
				break;
			case 4:
				// don't print anything
				is_unit_type = true;
				pd.log.info("Function {} Result type is Unit", gf);
				break;
		}
	}

	private void action_AGN(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
		final InstructionArgument value  = aInstruction.getArg(1);

		final String realTarget;
		if (target instanceof IntegerIA) {
			realTarget = pd.getRealTargetName(gf, (IntegerIA) target);
		} else {
			realTarget = pd.getRealTargetName(gf, (IdentIA) target);
		}
		final String assignmentValue = pd.getAssignmentValue(gf.getSelf(), value, gf);
		String s = String.format(Emit.emit("/*267*/")+"%s = %s;", realTarget, assignmentValue);
		pd.log.info("Assignment resolves to {}", s);
	}

	private void action_AGNK(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
		final InstructionArgument value  = aInstruction.getArg(1);

		final String realTarget = pd.getRealTargetName(gf, (IntegerIA) target);
		final String assignmentValue = pd.getAssignmentValue(gf.getSelf(), value, gf);
		String s = String.format(Emit.emit("/*278*/")+"%s = %s;", realTarget, assignmentValue);
		pd.log.info("Constant assignment resolves to {}", s);
	}

	private void action_CALLS(GeneratedFunction gf, Instruction aInstruction) {
		final StringBuilder sb = new StringBuilder();
		final InstructionArgument x = aInstruction.getArg(0);
		assert x instanceof ProcIA;
		final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
		{
			CReference reference = null;
			if (pte.expression_num == null) {
				final int y = 2;
				final IdentExpression ptex = (IdentExpression) pte.expression;
				String text = ptex.getText();
				@Nullable InstructionArgument xx = gf.vte_lookup(text);
				String xxx;
				if (xx != null) {
					xxx = pd.getRealTargetName(gf, (IntegerIA) xx);
				} else {
					xxx = text;
					pd.log.error("xxx is null " + text);
				}
				sb.append(Emit.emit("/*460*/")+xxx);
			} else {
				final IdentIA ia2 = (IdentIA) pte.expression_num;
				reference = new CReference();
				reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
				final List<String> sl3 = pd.getArgumentStrings(gf, aInstruction);
				reference.args(sl3);
				String path = reference.build();
				sb.append(Emit.emit("/*463*/")+path);
			}
			if (reference == null){
				sb.append('(');
				final List<String> sl3 = pd.getArgumentStrings(gf, aInstruction);
				sb.append(Helpers.String_join(", ", sl3));
				sb.append(");");
			}
		}
		pd.log.info("Special call (CALLS) resolves to {}", sb.toString());
	}

	private void action_CALL(GeneratedFunction gf, Instruction aInstruction) {
		final StringBuilder sb = new StringBuilder();
// 					pd.log.error("9000 "+inst.getName());
		final InstructionArgument x = aInstruction.getArg(0);
		assert x instanceof ProcIA;
		final ProcTableEntry pte = gf.getProcTableEntry(((ProcIA) x).getIndex());
		{
			if (pte.expression_num == null) {
				final IdentExpression ptex = (IdentExpression) pte.expression;
				String text = ptex.getText();
				@Nullable InstructionArgument xx = gf.vte_lookup(text);
				assert xx != null;
				String realTargetName = pd.getRealTargetName(gf, (IntegerIA) xx);
				sb.append(Emit.emit("/*424*/")+realTargetName);
				sb.append('(');
				final List<String> sl3 = pd.getArgumentStrings(gf, aInstruction);
				sb.append(Helpers.String_join(", ", sl3));
				sb.append(");");
			} else {
				final CReference reference = new CReference();
				final IdentIA ia2 = (IdentIA) pte.expression_num;
				reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
				final List<String> sl3 = pd.getArgumentStrings(gf, aInstruction);
				reference.args(sl3);
				String path = reference.build();

				sb.append(Emit.emit("/*427*/")+path+";");
			}
		}
		pd.log.info("Call (CALL) resolves to {}", sb.toString());
	}

	private void action_CONSTRUCT(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument _arg0 = aInstruction.getArg(0);
		assert _arg0 instanceof ProcIA;
		final ProcTableEntry pte = ((ProcIA) _arg0).getEntry();
//		List<TypeTableEntry> x = pte.getArgs();
//		int y = aInstruction.getArgsSize();
		ClassInvocation clsinv = pte.getClassInvocation();
		if (clsinv != null) {
//			final InstructionArgument target = pte.expression_num;
//
//			if (target instanceof IdentIA) {
//				// how to tell between named ctors and just a path?
//			}

			String s = String.format(Emit.emit("/*500*/")+"%s;", pd.getAssignmentValue(gf.getSelf(), aInstruction, clsinv, gf));
			pd.log.info("Constructor call resolved to {}", s); // TODO do more in depth analysis
		}
	}

	private void action_JMP(Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
//					InstructionArgument value  = instruction.getArg(1);

		final Label realTarget = (Label) target;

		pd.log.info("JMP resolves to goto {};", realTarget.getName());
	}

	private void action_JL(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JL resolves to vsb = {} < {}; for constant {}", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf), cte);
			pd.log.info("JL jumps to goto {};", realTarget.getName());
		} else {
			//
			// TODO need to lookup special __lt__ function
			//
			String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JL resolves to vsb = {} < {};", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf));
			pd.log.info("JL jumps to goto {};", realTarget.getName());

			final int y = 2;
		}
	}

	private void action_JNE(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JNE resolves to vsb = {} != {}; for constant {}", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf), cte);
			pd.log.info("JNE jumps to goto {};", realTarget.getName());
		} else {
			//
			// TODO need to lookup special __ne__ function ??
			//
			String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JNE resolves to vsb = {} != {};", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf));
			pd.log.info("JNE jumps to goto {};", realTarget.getName());
		}
	}

	private void action_JE(GeneratedFunction gf, Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA)lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JE resolves to vsb = {} == {}; for constant {}", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf), cte);
			pd.log.info("JE jumps to goto {};", realTarget.getName());
		} else {
			//
			// TODO need to lookup special __eq__ function
			//
			String realTargetName = pd.getRealTargetName(gf, (IntegerIA) lhs);
			pd.log.info("JE resolves to vsb = {} == {};", realTargetName, pd.getAssignmentValue(gf.getSelf(), rhs, gf));
			pd.log.info("JE jumps to goto {};", realTarget.getName());
		}
	}

	private void action_IS_A(Instruction instruction, GeneratedFunction gf) {
		final IntegerIA testing_var_  = (IntegerIA) instruction.getArg(0);
		final IntegerIA testing_type_ = (IntegerIA) instruction.getArg(1);
		final Label target_label      = ((LabelIA) instruction.getArg(2)).label;

		final VariableTableEntry testing_var    = gf.getVarTableEntry(testing_var_.getIndex());
		final TypeTableEntry     testing_type__ = gf.getTypeTableEntry(testing_type_.getIndex());

		GeneratedNode testing_type = testing_type__.resolved();
		final int z = ((GeneratedContainerNC) testing_type).getCode();

		pd.log.info("IS_A resolves to vsb = ZS{}_is_a({});", z, pd.getRealTargetName(gf, testing_var_));
		pd.log.info("IS_A jumps to goto {}};", target_label.getName());
	}

	private void action_CAST(Instruction instruction, GeneratedFunction gf) {
		final IntegerIA  vte_num_ = (IntegerIA) instruction.getArg(0);
		final IntegerIA vte_type_ = (IntegerIA) instruction.getArg(1);
		final IntegerIA vte_targ_ = (IntegerIA) instruction.getArg(2);
		final String target_name = pd.getRealTargetName(gf, vte_num_);
		final TypeTableEntry target_type_ = gf.getTypeTableEntry(vte_type_.getIndex());
		final String target_type = pd.getTypeName(target_type_.getAttached());
		final String source_target = pd.getRealTargetName(gf, vte_targ_);

		pd.log.info("CAST resolves to {} = ({}){};", target_name, target_type, source_target);
	}

	private void action_DECL(Instruction instruction, GeneratedFunction gf) {
		final SymbolIA decl_type = (SymbolIA)  instruction.getArg(0);
		final IntegerIA  vte_num = (IntegerIA) instruction.getArg(1);
		final String target_name = pd.getRealTargetName(gf, vte_num);
		final VariableTableEntry vte = gf.getVarTableEntry(vte_num.getIndex());

		final OS_Type x = vte.type.getAttached();
		if (x == null) {
			if (vte.vtt == VariableTableType.TEMP) {
				pd.log.error("8884 temp variable has no type {} {} {}",vte.getIndex(),vte.type, gf);
			} else {
				pd.log.error("8885 x is null (No typename specified) for {}", vte.getName());
			}
			return;
		}

		final GeneratedNode res = vte.resolvedType();
		if (res instanceof GeneratedClass) {
			pd.log.info("Declaring {} of type {} code {}", target_name, ((GeneratedClass) res).getName(), ((GeneratedClass) res).getCode());
			return;
		}

		if (x.getType() == OS_Type.Type.USER_CLASS) {
			final String z = pd.getTypeName(x);
			pd.log.warn("Declaring {} of type {} (USER_CLASS: no code)", target_name, z);
			return;
		} else if (x.getType() == OS_Type.Type.USER) {
			final TypeName y = x.getTypeName();
			if (y instanceof NormalTypeName) {
				if (((NormalTypeName) y).getName().equals("Any")) {
					pd.log.warn("Declaring {} of type Any (Any: no code)", target_name);
					return;
				} else {
					final String z = pd.getTypeName(y);
					pd.log.warn("Declaring {} of type {} (USER: no code)", target_name, z);
				}
				return;
			} else {
				pd.log.warn("Declaration of {} is not a NormalType: {}", vte.getName(), y);
			}

			if (y != null) {
				//
				// VARIABLE WASN'T FULLY DEDUCED YET
				//
				pd.log.error("8885 VARIABLE WASN'T FULLY DEDUCED YET {}", y.getClass().getName());
				return;
			}
		} else if (x.getType() == OS_Type.Type.BUILT_IN) {
			final Context context = gf.getFD().getContext();
			assert context != null;
			final OS_Type type = x.resolve(context);
			if (type.isUnitType()) {
				// TODO still should not happen
				pd.log.warn("481 {} is declared as the Unit type", target_name);
			} else {
//				pd.log.error("Bad potentialTypes size " + type);
				final String z = pd.getTypeName(type);
				pd.log.info("485 Z<{}> {};", z, target_name);
			}
		}

		//
		// VARIABLE WASN'T FULLY DEDUCED YET
		// MTL A TEMP VARIABLE
		//
		@NotNull final Collection<TypeTableEntry> pt_ = vte.potentialTypes();
		final List<TypeTableEntry> pt = new ArrayList<TypeTableEntry>(pt_);
		if (pt.size() == 1) {
			final TypeTableEntry ty = pt.get(0);
//			pd.log.error("8885 " +ty.attached);
			final OS_Type attached = ty.getAttached();
			final String z;
			if (attached != null)
				z = pd.getTypeName(attached);
			else
				z = Emit.emit("/*763*/")+"Unknown";
			pd.log.warn("505/8890 Z<{}> {};", z, target_name);
		}
		pd.log.error("8886 y is null (No typename specified)");
	}

	static class Analyze_Method_Header {

		private final PostDeduce pd;
		private TypeTableEntry tte;

		public Analyze_Method_Header(@NotNull GeneratedFunction gf, PostDeduce aPostDeduce) {
			pd = aPostDeduce;
			//
			find_return_type(gf);
			find_args_string(gf);
		}

		void find_args_string(GeneratedFunction gf) {
			Collection<VariableTableEntry> x = Collections2.filter(gf.vte_list, new Predicate<VariableTableEntry>() {
				@Override
				public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable VariableTableEntry input) {
					assert input != null;
					return input.vtt == VariableTableType.ARG;
				}
			});

			if (x.size() == 0) {
				pd.log.info("Function {} takes no arguments", gf);
				return;
			}

			for (VariableTableEntry variableTableEntry : x) {
				final PostDeduce.FOR_VTE typeNameForVariableEntry = pd.getTypeNameForVariableEntry(variableTableEntry);
				pd.log.info("Function {} argument {} is {}, {}", gf, variableTableEntry.getName(), typeNameForVariableEntry, variableTableEntry.type.getAttached());
			}
		}

		void find_return_type(GeneratedFunction gf) {
			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.vtt != VariableTableType.RESULT)
					result_index = null;
				if (result_index == null) {
					pd.log.error("Can't find Result or Value for function {}", gf);
					return;
				}
			}

			// Get it from resolved
			tte = gf.getTypeTableEntry(((IntegerIA) result_index).getIndex());
			if (tte.getAttached().isUnitType()) {
				pd.log.info("function {} returns Unit", gf);
				return;
			}

			GeneratedNode res = tte.resolved();
			if (res instanceof GeneratedContainerNC) {
				final GeneratedContainerNC nc = (GeneratedContainerNC) res;
				int code = nc.getCode();
				pd.log.info("function {} returns {} code {}", gf, nc.identityString(), code);
				return;
			} else if (res != null) {
				pd.log.warn("Unknown {} in function {} returns value", res, gf);
				return;
			}

			// Get it from type.attached
			OS_Type type = tte.getAttached();

			pd.log.info("228 "+ type);
			if (type != null) {
				pd.log.warn("function {} returns typename {}", gf, type);
			} else {
				pd.log.error("function {} has null typr for return", gf);
			}
		}
	}
}

//
//
//
