/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.types.OS_GenericTypeNameType;
import tripleo.elijah.lang.types.OS_UnitType;
import tripleo.elijah.nextgen.outputstatement.EG_DottedStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Rule;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.GCFM_Diagnostic;
import tripleo.elijah.stages.gen_c.c_ast1.C_HeaderString;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.ConstantTableEntry;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.Label;
import tripleo.elijah.stages.instructions.LabelIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.SymbolIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;
import tripleo.util.buffer.Buffer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 6/21/21 5:53 AM
 */
public class Generate_Code_For_Method {
	private final ElLog LOG;

	final BufferTabbedOutputStream tos = new BufferTabbedOutputStream();

	GenerateC gc;

	final BufferTabbedOutputStream tosHdr = new BufferTabbedOutputStream();

	public Generate_Code_For_Method(@NotNull final GenerateC aGenerateC, final ElLog aLog) {
		gc  = aGenerateC;
		LOG = aLog; // use log from GenerateC
	}

	void generateCodeForMethod(final BaseGeneratedFunction gf, final GenerateResult gr, final WorkList aWorkList) {
		final Generate_Method_Header gmh = new Generate_Method_Header(gf, gc, LOG);

		tos.put_string_ln(String.format("%s {", gmh.header_string));
		tosHdr.put_string_ln(String.format("%s;", gmh.header_string));

		action_invariant(gf, gmh);

		tos.flush();
		tos.close();
		tosHdr.flush();
		tosHdr.close();
		final Buffer buf = tos.getBuffer();
//		LOG.info(buf.getText());
		final LibraryStatementPart lsp = gf.module().getLsp();
		gr.addFunction(gf, buf, GenerateResult.TY.IMPL, lsp);
		final Buffer bufHdr = tosHdr.getBuffer();
//		LOG.info(bufHdr.getText());
		gr.addFunction(gf, bufHdr, GenerateResult.TY.HEADER, lsp);
	}

	private void action_invariant(final @NotNull BaseGeneratedFunction gf, final Generate_Method_Header aGmh) {
		tos.incr_tabs();
		//
		@NotNull final List<Instruction> instructions = gf.instructions();
		for (int instruction_index = 0; instruction_index < instructions.size(); instruction_index++) {
			final Instruction instruction = instructions.get(instruction_index);
//			LOG.err("8999 "+instruction);
			final Label label = gf.findLabel(instruction.getIndex());
			if (label != null) {
				tos.put_string_ln_no_tabs(label.getName() + ":");
			}

			switch (instruction.getName()) {
				case E:
					action_E(gf, aGmh);
					break;
				case X:
					action_X(aGmh);
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
					action_IS_A(instruction, tos, gf);
					break;
				case DECL:
					action_DECL(instruction, tos, gf);
					break;
				case CAST_TO:
					action_CAST(instruction, tos, gf);
					break;
				case NOP:
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + instruction.getName());
			}
		}
		tos.dec_tabs();
		tos.put_string_ln("}");
	}

	boolean is_constructor = false, is_unit_type = false;

	private void action_E(final @NotNull BaseGeneratedFunction gf, final Generate_Method_Header aGmh) {
		tos.put_string_ln("bool vsb;");
		int state = 0;

		if (gf.getFD() instanceof ConstructorDef)
			state = 2;
		else if (aGmh.tte == null)
			state = 3;
		else if (aGmh.tte.isResolved())
			state = 1;
		else if (aGmh.tte.getAttached() instanceof OS_UnitType)
			state = 4;

		switch (state) {
			case 0:
				tos.put_string_ln("Error_TTE_Not_Resolved " + aGmh.tte);
				break;
			case 1:
				final String ty = gc.getTypeName(aGmh.tte);
				tos.put_string_ln(String.format("%s* vsr;", ty));
				break;
			case 2:
				is_constructor = true;
				break;
			case 3:
				// TODO don't know what this is for now
				// Assuming ctor
				is_constructor = true;
				final GeneratedNode genClass = gf.getGenClass();
				final String ty2 = gc.getTypeNameForGenClass(genClass);
				tos.put_string_ln(String.format("%s vsr;", ty2));
				break;
			case 4:
				// don't print anything
				is_unit_type = true;
				break;
		}
		tos.put_string_ln("{");
		tos.incr_tabs();
	}

	private void action_XS() {
		tos.dec_tabs();
		tos.put_string_ln("}");
	}

	private void action_ES() {
		tos.put_string_ln("{");
		tos.incr_tabs();
	}

	private void action_X(final Generate_Method_Header aGmh) {
		// TODO functions are being marked as constructor when they are not

		if (is_constructor) {
			tos.dec_tabs();
			tos.put_string_ln("}");
			return;
		}

		tos.dec_tabs();
		tos.put_string_ln("}");
		if (!is_unit_type) {
			if (aGmh.tte != null && aGmh.tte.isResolved()) {
				tos.put_string_ln("return vsr;");
			}
		}
	}

	private void action_AGN(final BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
		final InstructionArgument value  = aInstruction.getArg(1);

		final String realTarget, s;
		if (target instanceof IntegerIA) {
			realTarget = gc.getRealTargetName(gf, (IntegerIA) target, AOG.ASSIGN);
			final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
			s = String.format(Emit.emit("/*267*/") + "%s = %s;", realTarget, assignmentValue);
		} else {
			final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
			s = gc.getRealTargetName(gf, (IdentIA) target, AOG.ASSIGN, assignmentValue);
		}
		tos.put_string_ln(s);
	}

	private void action_AGNK(final BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
		final InstructionArgument value  = aInstruction.getArg(1);

		final String realTarget      = gc.getRealTargetName(gf, (IntegerIA) target, AOG.ASSIGN);
		final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
		final String s               = String.format(Emit.emit("/*278*/") + "%s = %s;", realTarget, assignmentValue);
		tos.put_string_ln(s);
	}

	private void action_JE(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte            = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String             realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s == %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));
		} else {
			//
			// TODO need to lookup special __eq__ function
			//
			final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s == %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));

			final int y = 2;
		}
	}

	private void action_JNE(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte            = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String             realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s != %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));
		} else {
			//
			// TODO need to lookup special __ne__ function ??
			//
			final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s != %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));

			final int y = 2;
		}
	}

	private void action_JL(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument lhs    = aInstruction.getArg(0);
		final InstructionArgument rhs    = aInstruction.getArg(1);
		final InstructionArgument target = aInstruction.getArg(2);

		final Label realTarget = (Label) target;

		final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) lhs).getIndex());
		assert rhs != null;

		if (rhs instanceof ConstTableIA) {
			final ConstantTableEntry cte            = gf.getConstTableEntry(((ConstTableIA) rhs).getIndex());
			final String             realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s < %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));
		} else {
			//
			// TODO need to lookup special __lt__ function
			//
			final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) lhs, AOG.GET);
			tos.put_string_ln(String.format("vsb = %s < %s;", realTargetName, gc.getAssignmentValue(gf.getSelf(), rhs, gf)));
			tos.put_string_ln(String.format("if (!vsb) goto %s;", realTarget.getName()));

			final int y = 2;
		}
	}

	private void action_JMP(final @NotNull Instruction aInstruction) {
		final InstructionArgument target = aInstruction.getArg(0);
//					InstructionArgument value  = instruction.getArg(1);

		final Label realTarget = (Label) target;

		tos.put_string_ln(String.format("goto %s;", realTarget.getName()));
		final int y = 2;
	}

	private void action_CONSTRUCT(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final InstructionArgument _arg0 = aInstruction.getArg(0);
		assert _arg0 instanceof ProcIA;
		final ProcTableEntry       pte = gf.getProcTableEntry(((ProcIA) _arg0).getIndex());
		final List<TypeTableEntry> x   = pte.getArgs();
		final int                  y   = aInstruction.getArgsSize();
//					InstructionArgument z = instruction.getArg(1);
		final ClassInvocation clsinv = pte.getClassInvocation();
		if (clsinv != null) {

			final InstructionArgument target = pte.expression_num;
//						final InstructionArgument value  = instruction;

			if (target instanceof IdentIA) {
				// how to tell between named ctors and just a path?
			}

//						final String realTarget;
//						if (target instanceof IntegerIA) {
//							realTarget = getRealTargetName(gf, (IntegerIA) target);
//						} else if (target instanceof IdentIA) {
//							realTarget = getRealTargetName(gf, (IdentIA) target);
//						} else {
//							throw new NotImplementedException();
//						}
//						String s = String.format(Emit.emit("/*500*/")+"%s = %s;", realTarget, getAssignmentValue(gf.getSelf(), instruction, clsinv, gf));
			final String s = String.format(Emit.emit("/*500*/") + "%s;", gc.getAssignmentValue(gf.getSelf(), aInstruction, clsinv, gf));
			tos.put_string_ln(s);
		}
	}

	private void action_CALL(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final StringBuilder sb = new StringBuilder();
// 					LOG.err("9000 "+inst.getName());
		final InstructionArgument x = aInstruction.getArg(0);
		assert x instanceof ProcIA;
		final ProcTableEntry pte = gf.getProcTableEntry(((ProcIA) x).getIndex());
		{
			if (pte.expression_num == null) {
				final IdentExpression               ptex = (IdentExpression) pte.expression;
				final String                        text = ptex.getText();
				@Nullable final InstructionArgument xx   = gf.vte_lookup(text);
				assert xx != null;
				final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) xx, AOG.GET);
				sb.append(Emit.emit("/*424*/") + realTargetName);
				sb.append('(');
				final List<String> sl3 = gc.getArgumentStrings(gf, aInstruction);
				sb.append(Helpers.String_join(", ", sl3));
				sb.append(");");
			} else {
				final CReference reference = new CReference();
				final IdentIA    ia2       = (IdentIA) pte.expression_num;
				reference.getIdentIAPath(ia2, AOG.GET, null);
				final List<String> sl3 = gc.getArgumentStrings(gf, aInstruction);
				reference.args(sl3);
				final String path = reference.build();

				sb.append(Emit.emit("/*427*/") + path + ";");
			}
		}
		tos.put_string_ln(sb.toString());
	}

	private void action_CALLS(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction aInstruction) {
		final StringBuilder       sb = new StringBuilder();
		final InstructionArgument x  = aInstruction.getArg(0);
		assert x instanceof ProcIA;
		final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
		{
			CReference reference = null;
			if (pte.expression_num == null) {
				final int                           y    = 2;
				final IdentExpression               ptex = (IdentExpression) pte.expression;
				final String                        text = ptex.getText();
				@Nullable final InstructionArgument xx   = gf.vte_lookup(text);
				final String                        xxx;
				if (xx != null) {
					xxx = gc.getRealTargetName(gf, (IntegerIA) xx, AOG.GET);
				} else {
					xxx = text;
					LOG.err("xxx is null " + text);
				}
				sb.append(Emit.emit("/*460*/") + xxx);
			} else {
				final IdentIA ia2 = (IdentIA) pte.expression_num;
				reference = new CReference();
				reference.getIdentIAPath(ia2, AOG.GET, null);
				final List<String> sl3 = gc.getArgumentStrings(gf, aInstruction);
				reference.args(sl3);
				final String path = reference.build();
				sb.append(Emit.emit("/*463*/") + path);
			}
			if (reference == null) {
				sb.append('(');
				final List<String> sl3 = gc.getArgumentStrings(gf, aInstruction);
				sb.append(Helpers.String_join(", ", sl3));
				sb.append(");");
			}
		}
		tos.put_string_ln(sb.toString());
	}

	private void action_IS_A(final @NotNull Instruction instruction, final @NotNull BufferTabbedOutputStream tos, final @NotNull BaseGeneratedFunction gf) {
		final IntegerIA testing_var_  = (IntegerIA) instruction.getArg(0);
		final IntegerIA testing_type_ = (IntegerIA) instruction.getArg(1);
		final Label     target_label  = ((LabelIA) instruction.getArg(2)).label;

		final VariableTableEntry testing_var    = gf.getVarTableEntry(testing_var_.getIndex());
		final TypeTableEntry     testing_type__ = gf.getTypeTableEntry(testing_type_.getIndex());

		final GeneratedNode testing_type = testing_type__.resolved();
		final int           z            = ((GeneratedContainerNC) testing_type).getCode();

		tos.put_string_ln(String.format("vsb = ZS%d_is_a(%s);", z, gc.getRealTargetName(gf, testing_var_, AOG.GET)));
		tos.put_string_ln(String.format("if (!vsb) goto %s;", target_label.getName()));
	}

	private void action_DECL(final Instruction instruction, final BufferTabbedOutputStream tos, final BaseGeneratedFunction gf) {
		final Operation2<EG_Statement> op = _action_DECL(instruction, gf);

		if (op.mode() == Mode.SUCCESS) {
			tos.put_string_ln(op.success().getText());
		} else {
			//throw new
			// ignore
		}
	}

	private Operation2<EG_Statement> _action_DECL(final @NotNull Instruction instruction, final BaseGeneratedFunction gf) {
		final SymbolIA           decl_type   = (SymbolIA) instruction.getArg(0);
		final IntegerIA          vte_num     = (IntegerIA) instruction.getArg(1);
		final String             target_name = gc.getRealTargetName(gf, vte_num, AOG.GET);
		final VariableTableEntry vte         = gf.getVarTableEntry(vte_num.getIndex());

		final DeduceElement3_VariableTableEntry de_vte = (DeduceElement3_VariableTableEntry) vte.getDeduceElement3();
		final Operation2<OS_Type>               diag1  = de_vte.decl_test_001(gf);

		if (diag1.mode() == Mode.FAILURE) {
			final Diagnostic      diag_ = diag1.failure();
			final GCFM_Diagnostic diag  = (GCFM_Diagnostic) diag_;

			switch (diag.severity()) {
			case INFO:
				LOG.info(diag._message());
				break;
			case ERROR:
				LOG.err(diag._message());
				break;
			case LINT:
			case WARN:
			default:
				throw new NotImplementedException();
			}

			return Operation2.failure(diag_);
		}

		final OS_Type x = diag1.success(); //vte.type.getAttached();

		final GeneratedNode res = vte.resolvedType();
		if (res instanceof GeneratedClass) {
			final String z = GenerateC.GetTypeName.forGenClass((GeneratedClass) res);
			final String s = String.format("%s* %s;", z, target_name);
			return Operation2.success(new EG_SingleStatement(s, null));
		}

		if (x != null) {
			switch (x.getType()) {
			case USER_CLASS:
				final String z = GenerateC.GetTypeName.forOSType(x, LOG);
				final String s = String.format("%s* %s;", z, target_name);
				return Operation2.success(new EG_SingleStatement(s, null));
			case USER:
				final TypeName typeName = x.getTypeName();
				if (typeName instanceof NormalTypeName) {
					final String z2;
					if (((NormalTypeName) typeName).getName().equals("Any"))
						z2 = "void *";  // TODO Technically this is wrong
					else
						z2 = GenerateC.GetTypeName.forTypeName(typeName, gc.errSink);
					final String s1 = String.format("%s %s;", z2, target_name);
					return Operation2.success(new EG_SingleStatement(s1, null));
				}

				if (typeName != null) {
					//
					// VARIABLE WASN'T FULLY DEDUCED YET
					//
					return Operation2.failure(new Diagnostic_8887(typeName));
				}
				break;
			case BUILT_IN:
				final Context context = gf.getFD().getContext();
				assert context != null;
				final OS_Type type = x.resolve(context);
				final String s1;
				if (type.isUnitType()) {
					// TODO still should not happen
					s1 = String.format("/*%s is declared as the Unit type*/", target_name);
				} else {
					// LOG.err("Bad potentialTypes size " + type);
					final String z3 = gc.getTypeName(type);
					s1 = String.format("/*535*/Z<%s> %s; /*%s*/", z3, target_name, type.getClassOf());
				}
				return Operation2.success(new EG_SingleStatement(s1, null));
			}
		}

		//
		// VARIABLE WASN'T FULLY DEDUCED YET
		// MTL A TEMP VARIABLE
		//
		@NotNull final Collection<TypeTableEntry> pt_ = vte.potentialTypes();
		final List<TypeTableEntry>                pt  = new ArrayList<TypeTableEntry>(pt_);
		if (pt.size() == 1) {
			final TypeTableEntry ty = pt.get(0);
			if (ty.genType.node != null) {
				final GeneratedNode node = ty.genType.node;
				if (node instanceof GeneratedFunction) {
					final int y = 2;
//					((GeneratedFunction)node).typeDeferred()
					// get signature
					final String z = Emit.emit("/*552*/") + "void (*)()";
					final String s = String.format("/*8889*/%s %s;", z, target_name);
					return Operation2.success(new EG_SingleStatement(s, null));
				}
			} else {
//				LOG.err("8885 " +ty.attached);
				final @Nullable OS_Type attached = ty.getAttached();
				final String            z;
				if (attached != null)
					z = gc.getTypeName(attached);
				else
					z = Emit.emit("/*763*/") + "Unknown";
				final String s = String.format("/*8890*/Z<%s> %s;", z, target_name);
				return Operation2.success(new EG_SingleStatement(s, null));
			}
		}

		return Operation2.failure(new Diagnostic_8886());
	}

	private void __action_DECL(final @NotNull Instruction instruction, final BufferTabbedOutputStream tos, final BaseGeneratedFunction gf) {
		final SymbolIA           decl_type   = (SymbolIA) instruction.getArg(0);
		final IntegerIA          vte_num     = (IntegerIA) instruction.getArg(1);
		final String             target_name = gc.getRealTargetName(gf, vte_num, AOG.GET);
		final VariableTableEntry vte         = gf.getVarTableEntry(vte_num.getIndex());

		final OS_Type x = vte.type.getAttached();
		if (x == null && vte.potentialTypes().size() == 0) {
			if (vte.vtt == VariableTableType.TEMP) {
				LOG.err("8884 temp variable has no type " + vte + " " + gf);
			} else {
				LOG.err("8885 x is null (No typename specified) for " + vte.getName());
			}
			return;
		}

		final GeneratedNode res = vte.resolvedType();
		if (res instanceof GeneratedClass) {
			final String z = gc.getTypeName((GeneratedClass) res);
			tos.put_string_ln(String.format("%s* %s;", z, target_name));
			return;
		}

		if (x != null) {
			if (x.getType() == OS_Type.Type.USER_CLASS) {
				final String z = gc.getTypeName(x);
				tos.put_string_ln(String.format("%s* %s;", z, target_name));
				return;
			} else if (x.getType() == OS_Type.Type.USER) {
				final TypeName y = x.getTypeName();
				if (y instanceof NormalTypeName) {
					final String z;
					if (((NormalTypeName) y).getName().equals("Any"))
						z = "void *";  // TODO Technically this is wrong
					else
						z = gc.getTypeName(y);
					tos.put_string_ln(String.format("%s %s;", z, target_name));
					return;
				}

				if (y != null) {
					//
					// VARIABLE WASN'T FULLY DEDUCED YET
					//
					LOG.err("8885 "+y.getClass().getName());
					return;
				}
			} else if (x.getType() == OS_Type.Type.BUILT_IN) {
				final Context context = gf.getFD().getContext();
				assert context != null;
				final OS_Type type = x.resolve(context);
				if (type.isUnitType()) {
					// TODO still should not happen
					tos.put_string_ln(String.format("/*%s is declared as the Unit type*/", target_name));
				} else {
					//				LOG.err("Bad potentialTypes size " + type);
					final String z = gc.getTypeName(type);
					tos.put_string_ln(String.format("/*535*/Z<%s> %s; /*%s*/", z, target_name, type.getClassOf()));
				}
			}
		}

		//
		// VARIABLE WASN'T FULLY DEDUCED YET
		// MTL A TEMP VARIABLE
		//
		@NotNull final Collection<TypeTableEntry> pt_ = vte.potentialTypes();
		final List<TypeTableEntry>                pt  = new ArrayList<TypeTableEntry>(pt_);
		if (pt.size() == 1) {
			final TypeTableEntry ty = pt.get(0);
			if (ty.genType.node != null) {
				final GeneratedNode node = ty.genType.node;
				if (node instanceof GeneratedFunction) {
					final int y = 2;
//					((GeneratedFunction)node).typeDeferred()
					// get signature
					final String z = Emit.emit("/*552*/") + "void (*)()";
					tos.put_string_ln(String.format("/*8889*/%s %s;", z, target_name));
				}
			} else {
//				LOG.err("8885 " +ty.attached);
				final OS_Type attached = ty.getAttached();
				final String  z;
				if (attached != null)
					z = gc.getTypeName(attached);
				else
					z = Emit.emit("/*763*/") + "Unknown";
				tos.put_string_ln(String.format("/*8890*/Z<%s> %s;", z, target_name));
			}
		}
		LOG.err("8886 y is null (No typename specified)");
	}

	private void action_CAST(final @NotNull Instruction instruction, final @NotNull BufferTabbedOutputStream tos, final BaseGeneratedFunction gf) {
		final IntegerIA      vte_num_     = (IntegerIA) instruction.getArg(0);
		final IntegerIA      vte_type_    = (IntegerIA) instruction.getArg(1);
		final IntegerIA      vte_targ_    = (IntegerIA) instruction.getArg(2);
		final String         target_name  = gc.getRealTargetName(gf, vte_num_, AOG.GET);
		final TypeTableEntry target_type_ = gf.getTypeTableEntry(vte_type_.getIndex());
//		final String target_type = gc.getTypeName(target_type_.getAttached());
		final String target_type   = gc.getTypeName(target_type_.genType.node);
		final String source_target = gc.getRealTargetName(gf, vte_targ_, AOG.GET);

		tos.put_string_ln(String.format("%s = (%s)%s;", target_name, target_type, source_target));
	}

	void generateCodeForConstructor(final @NotNull GeneratedConstructor gf, final GenerateResult gr, final WorkList aWorkList) {
		// TODO this code is only correct for classes and not meant for namespaces
		final GeneratedClass x = (GeneratedClass) gf.getGenClass();
		switch (x.getKlass().getType()) {
			// Don't generate class definition for these three
			case INTERFACE:
			case SIGNATURE:
			case ABSTRACT:
				return;
		}
		final CClassDecl decl = new CClassDecl(x);
		decl.evaluatePrimitive();

		final String class_name = GenerateC.GetTypeName.forGenClass(x);
		final int    class_code = x.getCode();

		assert gf.cd != null;
		final String constructorName_ = gf.cd.name();
		final String constructorName;
		if (constructorName_.equals("<>"))
			constructorName = "";
		else
			constructorName = constructorName_;

		tos.put_string_ln(String.format("%s* ZC%d%s() {", class_name, class_code, constructorName));
		tos.incr_tabs();
		tos.put_string_ln(String.format("%s* R = GC_malloc(sizeof(%s));", class_name, class_name));
		tos.put_string_ln(String.format("R->_tag = %d;", class_code));
		if (decl.prim) {
			// TODO consider NULL, and floats and longs, etc
			if (!decl.prim_decl.equals("bool"))
				tos.put_string_ln("R->vsv = 0;");
			else if (decl.prim_decl.equals("bool"))
				tos.put_string_ln("R->vsv = false;");
		} else {
			for (final GeneratedClass.VarTableEntry o : x.varTable) {
//					final String typeName = getTypeNameForVarTableEntry(o);
				// TODO this should be the result of getDefaultValue for each type
				tos.put_string_ln(String.format("R->vm%s = 0;", o.nameToken));
			}
		}

		tos.dec_tabs();

		action_invariant(gf, null);

		tos.put_string_ln("return R;");
		tos.dec_tabs();
		tos.put_string_ln(String.format("} // class %s%s", decl.prim ? "box " : "", x.getName()));
		tos.put_string_ln("");

		final String header_string;

		{
			final Generate_Method_Header gmh         = new Generate_Method_Header(gf, gc, LOG);
			final String                 args_string = gmh.args_string;

			// NOTE getGenClass is always a class or namespace, getParent can be a function
			final GeneratedContainerNC parent = (GeneratedContainerNC) gf.getGenClass();

			assert parent == x;

			if (parent instanceof GeneratedClass) {
				final String name = String.format("ZC%d%s", class_code, constructorName);
//				LOG.info("138 class_name >> " + class_name);
				header_string = String.format("%s* %s(%s)", class_name, name, args_string);
			} else if (parent instanceof GeneratedNamespace) {
				// TODO see note above
				final String name = String.format("ZNC%d", class_code);
//				GeneratedNamespace st = (GeneratedNamespace) parent;
//				LOG.info(String.format("143 (namespace) %s -> %s", st.getName(), class_name));
//				final String if_args = args_string.length() == 0 ? "" : ", ";
				// TODO vsi for namespace instance??
//				tos.put_string_ln(String.format("%s %s%s(%s* vsi%s%s) {", returnType, class_name, name, class_name, if_args, args));
				header_string = String.format("%s %s(%s)", class_name, name, args_string);
			} else {
				throw new IllegalStateException("generating a constructor for something not a class.");
//				final String name = String.format("ZC%d", class_code);
//				header_string = String.format("%s %s(%s)", class_name, name, args_string);
			}
		}

		tosHdr.put_string_ln(String.format("%s;", header_string));

		tos.flush();
		tos.close();
		tosHdr.flush();
		tosHdr.close();
		final Buffer buf = tos.getBuffer();
//		LOG.info(buf.getText());
		gr.addConstructor(gf, buf, GenerateResult.TY.IMPL, x.module().getLsp());
		final Buffer bufHdr = tosHdr.getBuffer();
//		LOG.info(bufHdr.getText());
		gr.addConstructor(gf, bufHdr, GenerateResult.TY.HEADER, x.module().getLsp());
	}

	public enum AOG {
		ASSIGN, GET
	}

	static class Generate_Method_Header {

		private final String       return_type;
		private final String       args_string;
		private final String       header_string;
		private final String       name;
		private final GenerateC    gc;
		private final EG_Statement args_statement;
		OS_Type        type;
		TypeTableEntry tte;

		public Generate_Method_Header(final BaseGeneratedFunction gf, @NotNull final GenerateC aGenerateC, final ElLog LOG) {
			gc   = aGenerateC;
			name = gf.getFD().name();
			//
			return_type    = find_return_type(gf, LOG);
			args_statement = find_args_statement(gf);
			args_string    = args_statement.getText();
			header_string  = find_header_string(gf, LOG);
		}

		@NotNull String find_return_type(final BaseGeneratedFunction gf, final ElLog LOG) {
			return discriminator(gf, LOG, gc)
			  .find_return_type(this);
		}

		EG_Statement find_args_statement(final @NotNull BaseGeneratedFunction gf) {

			final String rule = "gen_c:gcfm:Generate_Method_Header:find_args_statement";

			// TODO EG_Statement, rule
			final List<String> args_list = gf.vte_list
			  .stream()
			  .filter(input -> input.vtt == VariableTableType.ARG)

			  //rule=vte:args_at
			  .map(input -> String.format("%s va%s", GenerateC.GetTypeName.forVTE(input), input.getName()))
			  .collect(Collectors.toList());
			final EG_Statement args = new EG_DottedStatement(", ", args_list, new EX_Rule(rule));

			return args;
		}

		static GCM_D discriminator(final BaseGeneratedFunction bgf, final ElLog aLOG, final GenerateC aGc) {
			if (bgf instanceof GeneratedConstructor) {
				return new GCM_GC((GeneratedConstructor) bgf, aLOG, aGc);
			} else if (bgf instanceof GeneratedFunction) {
				return new GCM_GF((GeneratedFunction) bgf, aLOG, aGc);
			}

			throw new IllegalStateException();
		}

		String __find_header_string(final BaseGeneratedFunction gf, final ElLog LOG) {
			final String result;
			// TODO buffer for gf.parent.<element>.locatable

			// NOTE getGenClass is always a class or namespace, getParent can be a function
			final GeneratedContainerNC parent = (GeneratedContainerNC) gf.getGenClass();

			if (parent instanceof GeneratedClass) {
				final GeneratedClass st = (GeneratedClass) parent;

				@NotNull final C_HeaderString chs = C_HeaderString.forClass(st,
				  () -> GenerateC.GetTypeName.forGenClass(st),
				  return_type,
				  name,
				  args_string,
				  LOG);

				result = chs.getResult();
			} else if (parent instanceof GeneratedNamespace) {
				final GeneratedNamespace st = (GeneratedNamespace) parent;

				@NotNull final C_HeaderString chs = C_HeaderString.forNamespace(st,
				  () -> GenerateC.GetTypeName.forGenNamespace(st),
				  return_type,
				  name,
				  args_string,
				  LOG);
				result = chs.getResult();
			} else {
				@NotNull final C_HeaderString chs = C_HeaderString.forOther(parent, return_type, name, args_string);
				//result = String.format("%s %s(%s)", return_type, name, args_string);
				result = chs.getResult();
			}

			return result;
		}

		@NotNull String __find_return_type(final BaseGeneratedFunction gf, final ElLog LOG) {
			String returnType = null;
			if (gf instanceof GeneratedConstructor) {
				@Nullable final InstructionArgument result_index = gf.vte_lookup("self");
				@NotNull final VariableTableEntry   vte          = ((IntegerIA) result_index).getEntry();
				assert vte.vtt == VariableTableType.SELF;

				// Get it from resolved
				tte = gf.getTypeTableEntry(((IntegerIA) result_index).getIndex());
				final GeneratedNode res = tte.resolved();
				if (res instanceof GeneratedContainerNC) {
					final GeneratedContainerNC nc   = (GeneratedContainerNC) res;
					final int                  code = nc.getCode();
					return String.format("Z%d*", code);
				}

				// Get it from type.attached
				type = tte.getAttached();

				LOG.info("228-1 " + type);
				if (type.isUnitType()) {
					assert false;
				} else if (type != null) {
					returnType = String.format("/*267*/%s*", gc.getTypeName(type));
				} else {
					LOG.err("655 Shouldn't be here (type is null)");
					returnType = "void/*2*/";
				}
			} else {
				@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
				if (result_index == null) {
					// if there is no Result, there should be Value
					result_index = gf.vte_lookup("Value");
					// but Value might be passed in. If it is, discard value
					@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT)
						result_index = null;
					if (result_index == null)
						return "void"; // README Assuming Unit
				}

				// Get it from resolved
				tte = gf.getTypeTableEntry(((IntegerIA) result_index).getIndex());
				final GeneratedNode res = tte.resolved();
				if (res instanceof GeneratedContainerNC) {
					final GeneratedContainerNC nc   = (GeneratedContainerNC) res;
					final int                  code = nc.getCode();
					return String.format("Z%d*", code);
				}

				// Get it from type.attached
				type = tte.getAttached();

				LOG.info("228 " + type);
				if (type == null) {
					LOG.err("655 Shouldn't be here (type is null)");
					returnType = "ERR_type_attached_is_null/*2*/";
				} else if (type.isUnitType()) {
					returnType = "void/*Unit*/";
				} else if (type != null) {
					if (type instanceof OS_GenericTypeNameType) {
						final OS_GenericTypeNameType genericTypeNameType = (OS_GenericTypeNameType) type;
						final TypeName               tn                  = genericTypeNameType.getRealTypeName();

						final @Nullable Map<TypeName, OS_Type> gp = gf.fi.getClassInvocation().genericPart;

						OS_Type realType = null;

						for (final Map.Entry<TypeName, OS_Type> entry : gp.entrySet()) {
							if (entry.getKey().equals(tn)) {
								realType = entry.getValue();
								break;
							}
						}

						assert realType != null;
						returnType = String.format("/*267*/%s*", gc.getTypeName(realType));
					} else
						returnType = String.format("/*267*/%s*", gc.getTypeName(type));
				} else {
					throw new IllegalStateException();
//					LOG.err("656 Shouldn't be here (can't reason about type)");
//					returnType = "void/*656*/";
				}
			}
			return returnType;
		}

		interface GCM_D {
			String find_return_type(Generate_Method_Header aGenerate_method_header);
		}

		static class GCM_GC implements GCM_D {
			private final GeneratedConstructor gf;
			private final ElLog                LOG;
			private final GenerateC            gc;

			public GCM_GC(final GeneratedConstructor aGf, final ElLog aLOG, final GenerateC aGc) {
				gf  = aGf;
				LOG = aLOG;
				gc  = aGc;
			}

			@Override
			public String find_return_type(final Generate_Method_Header aGenerate_method_header__) {
				final OS_Type        type;
				final TypeTableEntry tte;
				String               returnType = null;

				@Nullable final InstructionArgument result_index = gf.vte_lookup("self");
				@NotNull final VariableTableEntry   vte          = ((IntegerIA) result_index).getEntry();
				assert vte.vtt == VariableTableType.SELF;

				// Get it from resolved
				tte = gf.getTypeTableEntry(((IntegerIA) result_index).getIndex());
				final GeneratedNode res = tte.resolved();
				if (res instanceof GeneratedContainerNC) {
					final GeneratedContainerNC nc   = (GeneratedContainerNC) res;
					final int                  code = nc.getCode();
					return String.format("Z%d*", code);
				}

				// Get it from type.attached
				type = tte.getAttached();

				LOG.info("228-1 " + type);
				if (type.isUnitType()) {
					assert false;
				} else if (type != null) {
					returnType = String.format("/*267*/%s*", gc.getTypeName(type));
				} else {
					LOG.err("655 Shouldn't be here (type is null)");
					returnType = "void/*2*/";
				}

				return returnType;
			}
		}

		static class GCM_GF implements GCM_D {
			private final GeneratedFunction gf;
			private final ElLog             LOG;
			private final GenerateC         gc;

			public GCM_GF(final GeneratedFunction aGf, final ElLog aLOG, final GenerateC aGc) {
				gf  = aGf;
				LOG = aLOG;
				gc  = aGc;
			}

			@Override
			public String find_return_type(final Generate_Method_Header aGenerate_method_header__) {
				String               returnType = null;
				final TypeTableEntry tte;
				final OS_Type        type;

				@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
				if (result_index == null) {
					// if there is no Result, there should be Value
					result_index = gf.vte_lookup("Value");
					// but Value might be passed in. If it is, discard value
					@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT)
						result_index = null;
					if (result_index == null)
						return "void"; // README Assuming Unit
				}

				// Get it from resolved
				tte = gf.getTypeTableEntry(((IntegerIA) result_index).getIndex());
				final GeneratedNode res = tte.resolved();
				if (res instanceof GeneratedContainerNC) {
					final GeneratedContainerNC nc   = (GeneratedContainerNC) res;
					final int                  code = nc.getCode();
					return String.format("Z%d*", code);
				}

				// Get it from type.attached
				type = tte.getAttached();

				LOG.info("228 " + type);
				if (type == null) {
					LOG.err("655 Shouldn't be here (type is null)");
					returnType = "ERR_type_attached_is_null/*2*/";
				} else if (type.isUnitType()) {
					returnType = "void/*Unit*/";
				} else if (type != null) {
					if (type instanceof OS_GenericTypeNameType) {
						final OS_GenericTypeNameType genericTypeNameType = (OS_GenericTypeNameType) type;
						final TypeName               tn                  = genericTypeNameType.getRealTypeName();

						final @Nullable Map<TypeName, OS_Type> gp = gf.fi.getClassInvocation().genericPart;

						OS_Type realType = null;

						for (final Map.Entry<TypeName, OS_Type> entry : gp.entrySet()) {
							if (entry.getKey().equals(tn)) {
								realType = entry.getValue();
								break;
							}
						}

						assert realType != null;
						returnType = String.format("/*267*/%s*", gc.getTypeName(realType));
					} else
						returnType = String.format("/*267*/%s*", gc.getTypeName(type));
				} else {
					throw new IllegalStateException();
//					LOG.err("656 Shouldn't be here (can't reason about type)");
//					returnType = "void/*656*/";
				}

				return returnType;
			}
		}

		String find_args_string(final BaseGeneratedFunction gf) {
			final String args;
			if (false) {
				args = Helpers.String_join(", ", Collections2.transform(gf.getFD().fal().falis, new Function<FormalArgListItem, String>() {
					@org.jetbrains.annotations.Nullable
					@Override
					public String apply(@org.jetbrains.annotations.Nullable final FormalArgListItem input) {
						assert input != null;
						return String.format("%s va%s", gc.getTypeName(input.typeName()), input.name());
					}
				}));
			} else {
				final Collection<VariableTableEntry> x = Collections2.filter(gf.vte_list, new Predicate<VariableTableEntry>() {
					@Override
					public boolean apply(@org.jetbrains.annotations.Nullable final VariableTableEntry input) {
						assert input != null;
						return input.vtt == VariableTableType.ARG;
					}
				});
				args = Helpers.String_join(", ", Collections2.transform(x, new Function<VariableTableEntry, String>() {
					@org.jetbrains.annotations.Nullable
					@Override
					public String apply(@org.jetbrains.annotations.Nullable final VariableTableEntry input) {
						assert input != null;
						return String.format("%s va%s", gc.getTypeNameForVariableEntry(input), input.getName());
					}
				}));
			}
			return args;
		}

		@NotNull String find_header_string(final @NotNull BaseGeneratedFunction gf, final ElLog LOG) {
			// NOTE getGenClass is always a class or namespace, getParent can be a function
			final GeneratedContainerNC parent = (GeneratedContainerNC) gf.getGenClass();

			final String         s2;
			final C_HeaderString headerString;

			if (parent instanceof GeneratedClass) {
				final GeneratedClass st = (GeneratedClass) parent;

				headerString = C_HeaderString.forClass(st, () -> gc.getTypeName(st), return_type, name, args_string, LOG);
			} else if (parent instanceof GeneratedNamespace) {
				final GeneratedNamespace st = (GeneratedNamespace) parent;

				headerString = C_HeaderString.forNamespace(st, () -> gc.getTypeName(st), return_type, name, args_string, LOG);
			} else {
				headerString = C_HeaderString.forOther(parent, return_type, name, args_string);
			}
			s2 = headerString.getResult();
			return s2;
		}
	}

	private static class Diagnostic_8886 implements GCFM_Diagnostic {
		final int _code = 8886;

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
			stream.println(_message());
		}

		@Override
		public String _message() {
			return String.format("%d y is null (No typename specified)", _code);
		}
	}

	private static class Diagnostic_8887 implements GCFM_Diagnostic {
		final         int      _code = 8887;
		private final TypeName y;

		private Diagnostic_8887(final TypeName aY) {
			y = aY;
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
			stream.println(_message());
		}

		@Override
		public String _message() {
			return String.format("%d VARIABLE WASN'T FULLY DEDUCED YET: %s", _code, y.getClass().getName());
		}
	}
}

//
//
//
