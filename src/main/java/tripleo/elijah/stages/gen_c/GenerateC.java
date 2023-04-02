/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.IFixedList;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 10/8/20 7:13 AM
 */
public class GenerateC implements CodeGenerator, GenerateFiles {
	private static final String PHASE = "GenerateC";
	final ErrSink errSink;
	private final ElLog LOG;

	public GenerateC(OS_Module aM, ErrSink aErrSink, ElLog.Verbosity verbosity, PipelineLogic pipelineLogic) {
		errSink = aErrSink;
		LOG = new ElLog(aM.getFileName(), verbosity, PHASE);
		//
		pipelineLogic.addLog(LOG);
	}

	public GenerateC(final @NotNull OutputFileFactoryParams aParams) {
		errSink = aParams.getErrSink();

		final OS_Module       mod           = aParams.getMod();
		final ElLog.Verbosity verbosity     = aParams.getVerbosity();
		final PipelineLogic   pipelineLogic = aParams.getPipelineLogic();

		LOG = new ElLog(mod.getFileName(), verbosity, PHASE);

		pipelineLogic.addLog(LOG);
	}

	@Override
	public GenerateResult generateCode(final Collection<EvaNode> lgn, final WorkManager wm) {
		GenerateResult gr = new GenerateResult();

		for (final EvaNode evaNode : lgn) {
			if (evaNode instanceof EvaFunction) {
				EvaFunction generatedFunction = (EvaFunction) evaNode;
				WorkList    wl                = new WorkList();
				generate_function(generatedFunction, gr, wl);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			} else if (evaNode instanceof EvaContainerNC) {
				EvaContainerNC containerNC = (EvaContainerNC) evaNode;
				containerNC.generateCode(this, gr);
			} else if (evaNode instanceof EvaConstructor) {
				final EvaConstructor evaConstructor = (EvaConstructor) evaNode;
				WorkList             wl             = new WorkList();
				generate_constructor(evaConstructor, gr, wl);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			}
		}

		return gr;
	}

	@Override
	public GenerateResult resultsFromNodes(final List<EvaNode> aNodes, final WorkManager aWm) {
		throw new Error();
	}

	@NotNull
	public String getTypeName(EvaNode aNode) {
		if (aNode instanceof EvaClass)
			return getTypeName((EvaClass) aNode);
		if (aNode instanceof EvaNamespace)
			return getTypeName((EvaNamespace) aNode);
		throw new IllegalStateException("Must be class or namespace.");
	}

	String getRealTargetName(final @NotNull BaseEvaFunction gf, final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
		return getRealTargetName(gf, varTableEntry);
	}

	static class WlGenerateFunctionC implements WorkJob {

		private final BaseEvaFunction gf;
		private final GenerateResult  gr;
		private final WorkList wl;
		private final GenerateC generateC;
		private boolean _isDone = false;

		public WlGenerateFunctionC(BaseEvaFunction aGf, GenerateResult aGr, WorkList aWl, GenerateC aGenerateC) {
			gf = aGf;
			gr = aGr;
			wl = aWl;
			generateC = aGenerateC;
		}

		@Override
		public void run(WorkManager aWorkManager) {
			if (gf instanceof EvaFunction)
				generateC.generate_function((EvaFunction) gf, gr, wl);
			else
				generateC.generate_constructor((EvaConstructor) gf, gr, wl);
			_isDone = true;
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}
	}

	public void generate_function(EvaFunction aGeneratedFunction, GenerateResult gr, WorkList wl) {
		generateCodeForMethod(aGeneratedFunction, gr, wl);
		for (IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.isResolved()) {
				EvaNode x = identTableEntry.resolvedType();

				if (x instanceof EvaClass) {
					generate_class((EvaClass) x, gr);
				} else if (x instanceof EvaFunction) {
					wl.addJob(new WlGenerateFunctionC((EvaFunction) x, gr, wl, this));
				} else {
					LOG.err(x.toString());
					throw new NotImplementedException();
				}
			}
		}
		for (ProcTableEntry pte : aGeneratedFunction.prte_list) {
//			ClassInvocation ci = pte.getClassInvocation();
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
				int y=2;
/*
				if (pte.getClassInvocation() == null)
					assert pte.getStatus() == BaseTableEntry.Status.UNKNOWN;
*/
			} else {
				BaseEvaFunction gf = fi.getGenerated();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(gf, gr, wl, this));
				}
			}
		}
	}

	public void generate_constructor(EvaConstructor aEvaConstructor, GenerateResult gr, WorkList wl) {
		generateCodeForConstructor(aEvaConstructor, gr, wl);
		for (IdentTableEntry identTableEntry : aEvaConstructor.idte_list) {
			if (identTableEntry.isResolved()) {
				EvaNode x = identTableEntry.resolvedType();

				if (x instanceof EvaClass) {
					generate_class((EvaClass) x, gr);
				} else if (x instanceof EvaFunction) {
					wl.addJob(new WlGenerateFunctionC((EvaFunction) x, gr, wl, this));
				} else {
					LOG.err(x.toString());
					throw new NotImplementedException();
				}
			}
		}
		for (ProcTableEntry pte : aEvaConstructor.prte_list) {
//			ClassInvocation ci = pte.getClassInvocation();
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
				int y=2;
			} else {
				BaseEvaFunction gf = fi.getGenerated();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(gf, gr, wl, this));
				}
			}
		}
	}

	@Override
	public void generate_class(EvaClass x, GenerateResult gr) {
		if (x.generatedAlready) return;
		switch (x.getKlass().getType()) {
			// Don't generate class definition for these three
			case INTERFACE:
			case SIGNATURE:
			case ABSTRACT:
				return;
		}
		final CClassDecl decl = new CClassDecl(x);
		decl.evaluatePrimitive();
		final BufferTabbedOutputStream tosHdr = new BufferTabbedOutputStream();
		final BufferTabbedOutputStream tos = new BufferTabbedOutputStream();
		try {
			tosHdr.put_string_ln("typedef struct {");
			tosHdr.incr_tabs();
			tosHdr.put_string_ln("int _tag;");
			if (!decl.prim) {
				for (EvaClass.VarTableEntry o : x.varTable){
					final String typeName = getTypeNameGNCForVarTableEntry(o);
					tosHdr.put_string_ln(String.format("%s vm%s;", typeName, o.nameToken));
				}
			} else {
				tosHdr.put_string_ln(String.format("%s vsv;", decl.prim_decl));
			}

			String class_name = getTypeName(x);
			int class_code = x.getCode();

			tosHdr.dec_tabs();
			tosHdr.put_string_ln("");
//			tosHdr.put_string_ln(String.format("} %s;", class_name));
			tosHdr.put_string_ln(String.format("} %s;  // class %s%s", class_name, decl.prim ? "box " : "", x.getName()));

			tosHdr.put_string_ln("");
			tosHdr.put_string_ln("");

			// TODO remove this block when constructors are added in dependent functions, etc in Deduce
			{
				// TODO what about named constructors and ctor$0 and "the debug stack"
				tos.put_string_ln(String.format("%s* ZC%d() {", class_name, class_code));
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
					for (EvaClass.VarTableEntry o : x.varTable) {
//					final String typeName = getTypeNameForVarTableEntry(o);
						// TODO this should be the result of getDefaultValue for each type
						tos.put_string_ln(String.format("R->vm%s = 0;", o.nameToken));
					}
				}
				tos.put_string_ln("return R;");
				tos.dec_tabs();
				tos.put_string_ln(String.format("} // class %s%s", decl.prim ? "box " : "", x.getName()));
				tos.put_string_ln("");
			}
			tos.flush();
		} finally {
			tos.close();
			tosHdr.close();
			Buffer buf = tos.getBuffer();
//			LOG.info(buf.getText());
			gr.addClass(GenerateResult.TY.IMPL, x, buf, x.module().getLsp());
			Buffer buf2 = tosHdr.getBuffer();
//			LOG.info(buf2.getText());
			gr.addClass(GenerateResult.TY.HEADER, x, buf2, x.module().getLsp());
		}
		x.generatedAlready = true;
	}

	@NotNull public String getTypeNameGNCForVarTableEntry(EvaContainer.VarTableEntry o) {
		final String typeName;
		if (o.resolvedType() != null) {
			EvaNode xx = o.resolvedType();
			if (xx instanceof EvaClass) {
				typeName = getTypeName((EvaClass) xx);
			} else if (xx instanceof EvaNamespace) {
				typeName = getTypeName((EvaNamespace) xx);
			} else
				throw new NotImplementedException();
		} else {
			if (o.varType != null)
				typeName = getTypeName(o.varType);
			else
				typeName = "void*/*null*/";
		}
		return typeName;
	}

	@Override
	public void generate_namespace(EvaNamespace x, GenerateResult gr) {
		if (x.generatedAlready) return;
		// TODO do we need `self' parameters for namespace?
		final BufferTabbedOutputStream tosHdr = new BufferTabbedOutputStream();
		final BufferTabbedOutputStream tos = new BufferTabbedOutputStream();
		try {
			if (x.varTable.size() > 0) {
				tosHdr.put_string_ln("typedef struct {");
				tosHdr.incr_tabs();
//				tosHdr.put_string_ln("int _tag;");
				for (EvaNamespace.VarTableEntry o : x.varTable) {
					final String typeName = getTypeNameGNCForVarTableEntry(o);

					tosHdr.put_string_ln(String.format("%s* vm%s;", o.varType == null ? "void " : typeName, o.nameToken));
				}

				String class_name = getTypeName(x);
				final int class_code = x.getCode();

				tosHdr.dec_tabs();
				tosHdr.put_string_ln("");
				tosHdr.put_string_ln(String.format("} %s; // namespace `%s'", class_name, x.getName()));
				// TODO "instance" namespaces
				tosHdr.put_string_ln("");
				tosHdr.put_string_ln(String.format("Z%d zN%s_instance; // namespace `%s'", class_code, class_name, x.getName()));
				tosHdr.put_string_ln("");

				tosHdr.put_string_ln("");
				tosHdr.put_string_ln("");
				tos.put_string_ln(String.format("%s* ZN%d() {", class_name, class_code));
				tos.incr_tabs();
				// TODO multiple calls of namespace function (need if/else statement)
				tos.put_string_ln(String.format("%s* R = GC_malloc(sizeof(%s));", class_name, class_name));
//				tos.put_string_ln(String.format("R->_tag = %d;", class_code));
				for (EvaNamespace.VarTableEntry o : x.varTable) {
//					final String typeName = getTypeNameForVarTableEntry(o);
					tosHdr.put_string_ln(String.format("R->vm%s = 0;", o.nameToken));
				}
				tos.put_string_ln("");
				tos.put_string_ln(String.format("zN%s_instance = R;", class_name));
				tos.put_string_ln("return R;");
				tos.dec_tabs();
				tos.put_string_ln(String.format("} // namespace `%s'", x.getName()));
				tos.put_string_ln("");
				tos.flush();
			}
		} finally {
			tos.close();
			tosHdr.close();
			if (x.varTable.size() > 0) { // TODO should we let this through?
				Buffer buf = tos.getBuffer();
//				LOG.info(buf.getText());
				gr.addNamespace(GenerateResult.TY.IMPL, x, buf, x.module().getLsp());
				Buffer buf2 = tosHdr.getBuffer();
//				LOG.info(buf2.getText());
				gr.addNamespace(GenerateResult.TY.HEADER, x, buf2, x.module().getLsp());
			}
		}
		x.generatedAlready = true;
	}

	private void generateCodeForMethod(BaseEvaFunction gf, GenerateResult gr, WorkList aWorkList) {
		if (gf.getFD() == null) return;
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(this, LOG);
		gcfm.generateCodeForMethod(gf, gr, aWorkList);
	}

	private void generateCodeForConstructor(EvaConstructor gf, GenerateResult gr, WorkList aWorkList) {
		if (gf.getFD() == null) return;
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(this, LOG);
		gcfm.generateCodeForConstructor(gf, gr, aWorkList);
	}

	static class GetTypeName {
		static String getTypeNameForGenClass(@NotNull EvaNode aGenClass) {
			String ty;
			if (aGenClass instanceof EvaClass)
				ty = forGenClass((EvaClass) aGenClass);
			else if (aGenClass instanceof EvaNamespace)
				ty = forGenNamespace((EvaNamespace) aGenClass);
			else
				ty = "Error_Unknown_GenClass";
			return ty;
		}

		static String forVTE(@NotNull VariableTableEntry input) {
			OS_Type attached = input.type.getAttached();
			if (attached == null)
				return Emit.emit("/*390*/")+"Z__Unresolved*"; // TODO remove this ASAP
			//
			// special case
			//
			if (input.type.genType.node != null)
				return Emit.emit("/*395*/")+getTypeNameForGenClass(input.type.genType.node)+"*";
			//
			if (input.getStatus() == BaseTableEntry.Status.UNCHECKED)
				return "Error_UNCHECKED_Type";
			if (attached.getType() == OS_Type.Type.USER_CLASS) {
				return attached.getClassOf().name();
			} else if (attached.getType() == OS_Type.Type.USER) {
				TypeName typeName = attached.getTypeName();
				String name;
				if (typeName instanceof NormalTypeName)
					name = ((NormalTypeName) typeName).getName();
				else
					name = typeName.toString();
				return String.format(Emit.emit("/*543*/")+"Z<%s>*", name);
			} else
				throw new NotImplementedException();
		}

		static String forGenNamespace(@NotNull EvaNamespace aGeneratedNamespace) {
			String z;
			z = String.format("Z%d", aGeneratedNamespace.getCode());
			return z;
		}

		static String forGenClass(@NotNull EvaClass aEvaClass) {
			String z;
			z = String.format("Z%d", aEvaClass.getCode());
			return z;
		}

		static String forTypeTableEntry(@NotNull TypeTableEntry tte) {
			EvaNode res = tte.resolved();
			if (res instanceof EvaContainerNC) {
				final EvaContainerNC nc   = (EvaContainerNC) res;
				int                  code = nc.getCode();
				return "Z"+code;
			} else
				return "Z<-1>";
		}

		@Deprecated
		static String forOSType(final @NotNull OS_Type ty, ElLog LOG) {
			if (ty == null) throw new IllegalArgumentException("ty is null");
			//
			String z;
			switch (ty.getType()) {
				case USER_CLASS:
					final ClassStatement el = ty.getClassOf();
					final String name;
					if (ty instanceof NormalTypeName)
						name = ((NormalTypeName) ty).getName();
					else
						name = el.getName();
					z = Emit.emit("/*443*/")+String.format("Z%d/*%s*/", el._a.getCode(), name);//.getName();
					break;
				case FUNCTION:
					z = "<function>";
					break;
				case FUNC_EXPR:
				{
					z = "<function>";
					OS_FuncExprType fe = (OS_FuncExprType) ty;
					int             y  =2;
				}
				break;
				case USER:
					final TypeName typeName = ty.getTypeName();
					LOG.err("Warning: USER TypeName in GenerateC "+ typeName);
					final String s = typeName.toString();
					if (s.equals("Unit"))
						z = "void";
					else
						z = String.format("Z<Unknown_USER_Type /*%s*/>", s);
					break;
				case BUILT_IN:
					LOG.err("Warning: BUILT_IN TypeName in GenerateC");
					z = "Z"+ty.getBType().getCode();  // README should not even be here, but look at .name() for other code gen schemes
					break;
				case UNIT_TYPE:
					z = "void";
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + ty.getType());
			}
			return z;
		}

		@Deprecated
		static String forTypeName(final @NotNull TypeName typeName, final @NotNull ErrSink errSink) {
			if (typeName instanceof RegularTypeName) {
				final String name = ((RegularTypeName) typeName).getName(); // TODO convert to Z-name

				return String.format("Z<%s>/*kklkl*/", name);
//			return getTypeName(new OS_UserType(typeName));
			}
			errSink.reportError("Type is not fully deduced "+typeName);
			return ""+typeName; // TODO type is not fully deduced
		}
	}

	String getTypeNameForGenClass(@NotNull EvaNode aGenClass) {
		return GetTypeName.getTypeNameForGenClass(aGenClass);
	}

	String getTypeNameForVariableEntry(@NotNull VariableTableEntry input) {
		return GetTypeName.forVTE(input);
	}

	String getTypeName(@NotNull EvaNamespace aGeneratedNamespace) {
		return GetTypeName.forGenNamespace(aGeneratedNamespace);
	}

	String getTypeName(@NotNull EvaClass aEvaClass) {
		return GetTypeName.forGenClass(aEvaClass);
	}

	String getTypeName(@NotNull TypeTableEntry tte) {
		return GetTypeName.forTypeTableEntry(tte);
	}

	@Deprecated
	String getTypeName(final @NotNull OS_Type ty) {
		return GetTypeName.forOSType(ty, LOG);
	}

	@Deprecated
	String getTypeName(final @NotNull TypeName typeName) {
		return GetTypeName.forTypeName(typeName, errSink);
	}

	@NotNull List<String> getArgumentStrings(final @NotNull Supplier<IFixedList<InstructionArgument>> instructionSupplier) {
		final @NotNull List<String> sl3       = new ArrayList<String>();
		final int                   args_size = instructionSupplier.get().size();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instructionSupplier.get().get(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName((IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/") + "" + realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference();
				reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
				final String text = reference.build();
				sl3.add(Emit.emit("/*673*/") + "" + text);
			} else if (ia instanceof ConstTableIA) {
				final ConstTableIA       c   = (ConstTableIA) ia;
				final ConstantTableEntry cte = c.getEntry();
				final String             s   = GetAssignmentValue.const_to_string(cte.initialValue);
				sl3.add(s);
				final int y = 2;
			} else if (ia instanceof ProcIA) {
				LOG.err("740 ProcIA");
				throw new NotImplementedException();
			} else {
				LOG.err(ia.getClass().getName());
				throw new IllegalStateException("Invalid InstructionArgument");
			}
		}
		return sl3;
	}
	@NotNull List<String> getArgumentStrings(final BaseEvaFunction gf, final Instruction instruction) {
		final List<String> sl3 = new ArrayList<String>();
		final int args_size = instruction.getArgsSize();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instruction.getArg(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName(gf, (IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/")+""+realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference();
				reference.getIdentIAPath((IdentIA) ia, gf, Generate_Code_For_Method.AOG.GET, null);
				String text = reference.build();
				sl3.add(Emit.emit("/*673*/")+""+text);
			} else if (ia instanceof ConstTableIA) {
				ConstTableIA c = (ConstTableIA) ia;
				ConstantTableEntry cte = gf.getConstTableEntry(c.getIndex());
				String s = GetAssignmentValue.const_to_string(cte.initialValue);
				sl3.add(s);
				int y = 2;
			} else if (ia instanceof ProcIA) {
				LOG.err("740 ProcIA");
				throw new NotImplementedException();
			} else {
				LOG.err(ia.getClass().getName());
				throw new IllegalStateException("Invalid InstructionArgument");
			}
		}
		return sl3;
	}

	static class GetAssignmentValue {

		public String FnCallArgs(FnCallArgs fca, BaseEvaFunction gf, ElLog LOG) {
			final StringBuilder sb = new StringBuilder();
			final Instruction inst = fca.getExpression();
//			LOG.err("9000 "+inst.getName());
			final InstructionArgument x = inst.getArg(0);
			assert x instanceof ProcIA;
			final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
//			LOG.err("9000-2 "+pte);
			switch (inst.getName()) {
			case CALL:
			{
				if (pte.expression_num == null) {
//					assert false; // TODO synthetic methods
					final IdentExpression ptex = (IdentExpression) pte.expression;
					sb.append(ptex.getText());
					sb.append(Emit.emit("/*671*/")+"(");

					final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
					sb.append(Helpers.String_join(", ", sll));

					sb.append(")");
				} else {
					final IdentIA ia2 = (IdentIA) pte.expression_num;
					final IdentTableEntry idte = ia2.getEntry();
					if (idte.getStatus() == BaseTableEntry.Status.KNOWN) {
						final CReference reference = new CReference();
						final FunctionInvocation functionInvocation = pte.getFunctionInvocation();
						if (functionInvocation == null || functionInvocation.getFunction() == ConstructorDef.defaultVirtualCtor) {
							reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
							final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
							reference.args(sll);
							String path = reference.build();
							sb.append(Emit.emit("/*829*/") + path);
						} else {
							final BaseEvaFunction pte_generated = functionInvocation.getGenerated();
							if (idte.resolvedType() == null && pte_generated != null)
								idte.resolveTypeToClass(pte_generated);
							reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
							final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
							reference.args(sll);
							String path = reference.build();
							sb.append(Emit.emit("/*827*/") + path);
						}
					} else {
						final String path = gf.getIdentIAPathNormal(ia2);
						sb.append(Emit.emit("/*828*/")+String.format("%s is UNKNOWN", path));
					}
				}
				return sb.toString();
			}
			case CALLS:
			{
				CReference reference = null;
				if (pte.expression_num == null) {
					final int y=2;
					final IdentExpression ptex = (IdentExpression) pte.expression;
					sb.append(Emit.emit("/*684*/"));
					sb.append(ptex.getText());
				} else {
					// TODO Why not expression_num?
					reference = new CReference();
					final IdentIA ia2 = (IdentIA) pte.expression_num;
					reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
					final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
					reference.args(sll);
					String path = reference.build();
					sb.append(Emit.emit("/*807*/")+path);

					final IExpression ptex = pte.expression;
					if (ptex instanceof IdentExpression) {
						sb.append(Emit.emit("/*803*/"));
						sb.append(((IdentExpression) ptex).getText());
					} else if (ptex instanceof ProcedureCallExpression) {
						sb.append(Emit.emit("/*806*/"));
						sb.append(ptex.getLeft()); // TODO Qualident, IdentExpression, DotExpression
					}
				}
				if (true /*reference == null*/) {
					sb.append(Emit.emit("/*810*/") + "(");
					{
						final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
						sb.append(Helpers.String_join(", ", sll));
					}
					sb.append(");");
				}
				return sb.toString();
			}
			default:
				throw new IllegalStateException("Unexpected value: " + inst.getName());
			}
		}

		public String ConstTableIA(ConstTableIA constTableIA, BaseEvaFunction gf) {
			final ConstantTableEntry cte = gf.getConstTableEntry(constTableIA.getIndex());
//			LOG.err(("9001-3 "+cte.initialValue));
			switch (cte.initialValue.getKind()) {
			case NUMERIC:
				return const_to_string(cte.initialValue);
			case STRING_LITERAL:
				return const_to_string(cte.initialValue);
			case IDENT:
				final String text = ((IdentExpression) cte.initialValue).getText();
				if (BuiltInTypes.isBooleanText(text))
					return text;
				else
					throw new NotImplementedException();
			default:
				throw new NotImplementedException();
			}
		}

		@NotNull
		private List<String> getAssignmentValueArgs(final Instruction inst, final BaseEvaFunction gf, ElLog LOG) {
			final int args_size = inst.getArgsSize();
			final List<String> sll = new ArrayList<String>();
			for (int i = 1; i < args_size; i++) {
				final InstructionArgument ia = inst.getArg(i);
				final int y=2;
//			LOG.err("7777 " +ia);
				if (ia instanceof ConstTableIA) {
					final ConstantTableEntry constTableEntry = gf.getConstTableEntry(((ConstTableIA) ia).getIndex());
					sll.add(""+ const_to_string(constTableEntry.initialValue));
				} else if (ia instanceof IntegerIA) {
					final VariableTableEntry variableTableEntry = gf.getVarTableEntry(((IntegerIA) ia).getIndex());
					sll.add(Emit.emit("/*853*/")+""+getRealTargetName(gf, variableTableEntry));
				} else if (ia instanceof IdentIA) {
					String path = gf.getIdentIAPathNormal((IdentIA) ia);    // return x.y.z
					IdentTableEntry ite = gf.getIdentTableEntry(to_int(ia));
					if (ite.getStatus() == BaseTableEntry.Status.UNKNOWN) {
						sll.add(String.format("%s is UNKNOWN", path));
					} else {
						final CReference reference = new CReference();
						reference.getIdentIAPath((IdentIA) ia, gf, Generate_Code_For_Method.AOG.GET, null);
						String path2 = reference.build();                        // return ZP105get_z(vvx.vmy)
						if (path.equals(path2)) {
							// should always fail
							//throw new AssertionError();
							LOG.err(String.format("864 should always fail but didn't %s %s", path, path2));
						}
//					assert ident != null;
//					IdentTableEntry ite = gf.getIdentTableEntry(((IdentIA) ia).getIndex());
//					sll.add(Emit.emit("/*748*/")+""+ite.getIdent().getText());
						sll.add(Emit.emit("/*748*/") + "" + path2);
						LOG.info("743 " + path2 + " " + path);
					}
				} else if (ia instanceof ProcIA) {
					LOG.err("863 ProcIA");
					throw new NotImplementedException();
				} else {
					throw new IllegalStateException("Cant be here: Invalid InstructionArgument");
				}
			}
			return sll;
		}

		private static String const_to_string(final IExpression expression) {
			if (expression instanceof NumericExpression) {
				return ""+((NumericExpression) expression).getValue();
			}
			if (expression instanceof CharLitExpression) {
				return String.format("'%s'", expression.toString());
			}
			if (expression instanceof StringExpression) {
				// TODO triple quoted strings and other escaping concerns
				return String.format("\"%s\"", ((StringExpression) expression).getText());
			}

			// FloatLitExpression
			// BooleanExpression
			throw new NotImplementedException();
		}

		public String IntegerIA(IntegerIA integerIA, BaseEvaFunction gf) {
			VariableTableEntry vte = gf.getVarTableEntry(integerIA.getIndex());
			String x = getRealTargetName(gf, vte);
			return x;
		}

		public String IdentIA(IdentIA identIA, BaseEvaFunction gf) {
			assert gf == identIA.gf;
			final CReference reference = new CReference();
			reference.getIdentIAPath(identIA, gf, Generate_Code_For_Method.AOG.GET, null);
			return reference.build();
		}

		public String forClassInvocation(Instruction aInstruction, ClassInvocation aClsinv, BaseEvaFunction gf, ElLog LOG) {
			int y=2;
			InstructionArgument _arg0 = aInstruction.getArg(0);
			@NotNull ProcTableEntry pte = gf.getProcTableEntry(((ProcIA) _arg0).getIndex());
			final CtorReference reference = new CtorReference();
			reference.getConstructorPath(pte.expression_num, gf);
			@NotNull List<String> x = getAssignmentValueArgs(aInstruction, gf, LOG);
			reference.args(x);
			return reference.build(aClsinv);
		}
	}

	String getAssignmentValue(VariableTableEntry aSelf, Instruction aInstruction, ClassInvocation aClsinv, BaseEvaFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		return gav.forClassInvocation(aInstruction, aClsinv, gf, LOG);
	}

	@NotNull
	String getAssignmentValue(VariableTableEntry value_of_this, final InstructionArgument value, final BaseEvaFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		if (value instanceof FnCallArgs) {
			final FnCallArgs fca = (FnCallArgs) value;
			return gav.FnCallArgs(fca, gf, LOG);
		}

		if (value instanceof ConstTableIA) {
			ConstTableIA constTableIA = (ConstTableIA) value;
			return gav.ConstTableIA(constTableIA, gf);
		}

		if (value instanceof IntegerIA) {
			IntegerIA integerIA = (IntegerIA) value;
			return gav.IntegerIA(integerIA, gf);
		}

		if (value instanceof IdentIA) {
			IdentIA identIA = (IdentIA) value;
			return gav.IdentIA(identIA, gf);
		}

		LOG.err(String.format("783 %s %s", value.getClass().getName(), value));
		return ""+value;
	}

	//String getRealTargetName(final BaseEvaFunction gf, final IntegerIA target, final Generate_Code_For_Method.AOG aog) {
	//	final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
	//	return getRealTargetName(gf, varTableEntry);
	//}
	//String getRealTargetName(final BaseEvaFunction gf, final IntegerIA target, final Generate_Code_For_Method.AOG aog) {
	//	final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
	//	return getRealTargetName(gf, varTableEntry);
	//}

	String getRealTargetName(final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final BaseEvaFunction gf            = target.gf;
		final VariableTableEntry    varTableEntry = gf.getVarTableEntry(target.getIndex());
		return getRealTargetName(gf, varTableEntry);
	}


	static String getRealTargetName(final BaseEvaFunction gf, final VariableTableEntry varTableEntry) {
		final String vte_name = varTableEntry.getName();
		if (varTableEntry.vtt == VariableTableType.TEMP) {
			if (varTableEntry.getName() == null) {
				return "vt" + varTableEntry.tempNum;
			} else {
				return "vt" + varTableEntry.getName();
			}
		} else if (varTableEntry.vtt == VariableTableType.ARG) {
			return "va" + vte_name;
		} else if (SpecialVariables.contains(vte_name)) {
			return SpecialVariables.get(vte_name);
		} else if (isValue(gf, vte_name)) {
			return "vsc->vsv";
		} else {
			return Emit.emit("/*879*/")+"vv" + vte_name;
		}
	}

	private static boolean isValue(BaseEvaFunction gf, String name) {
		if (!name.equals("Value")) return false;
		//
		FunctionDef fd = (FunctionDef) gf.getFD();
		switch (fd.getSpecies()) {
		case REG_FUN:
		case DEF_FUN:
			if (!(fd.getParent() instanceof ClassStatement)) return false;
			for (AnnotationPart anno : ((ClassStatement) fd.getParent()).annotationIterable()) {
				if (anno.annoClass().equals(Helpers.string_to_qualident("Primitive"))) {
					return true;
				}
			}
			return false;
		case PROP_GET:
		case PROP_SET:
			return true;
		default:
			throw new IllegalStateException("Unexpected value: " + fd.getSpecies());
		}
	}

	String getRealTargetName(final BaseEvaFunction gf, final IdentIA target, final Generate_Code_For_Method.AOG aog, final String value) {
		int state = 0, code = -1;
		IdentTableEntry identTableEntry = gf.getIdentTableEntry(target.getIndex());
		LinkedList<String> ls = new LinkedList<String>();
		// TODO in Deduce set property lookupType to denote what type of lookup it is: MEMBER, LOCAL, or CLOSURE
		InstructionArgument backlink = identTableEntry.getBacklink();
		final String text = identTableEntry.getIdent().getText();
		if (backlink == null) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element parent = vs.getParent().getParent();
				if (parent != gf.getFD()) {
					// we want identTableEntry.resolved which will be a GeneratedMember
					// which will have a container which will be either be a function,
					// statement (semantic block, loop, match, etc) or a EvaContainerNC
					int     y  =2;
					EvaNode er = identTableEntry.externalRef;
					if (er instanceof EvaContainerNC) {
						final EvaContainerNC nc = (EvaContainerNC) er;
						assert nc instanceof EvaNamespace;
						EvaNamespace ns = (EvaNamespace) nc;
//						if (ns.isInstance()) {}
						state = 1;
						code = nc.getCode();
					}
				}
			}
			switch (state) {
			case 0:
				ls.add(Emit.emit("/*912*/")+"vsc->vm"+ text); // TODO blindly adding "vm" might not always work, also put in loop
				break;
			case 1:
				ls.add(Emit.emit("/*845*/")+String.format("zNZ%d_instance->vm%s", code, text));
				break;
			default:
				throw new IllegalStateException("Can't be here");
			}
		} else
			ls.add(Emit.emit("/*872*/")+"vm"+ text); // TODO blindly adding "vm" might not always work, also put in loop
		while (backlink != null) {
			if (backlink instanceof IntegerIA) {
				IntegerIA integerIA = (IntegerIA) backlink;
				String realTargetName = getRealTargetName(gf, integerIA, Generate_Code_For_Method.AOG.ASSIGN);
				ls.addFirst(Emit.emit("/*892*/")+realTargetName);
				backlink = null;
			} else if (backlink instanceof IdentIA) {
				IdentIA identIA = (IdentIA) backlink;
				int identIAIndex = identIA.getIndex();
				IdentTableEntry identTableEntry1 = gf.getIdentTableEntry(identIAIndex);
				String identTableEntryName = identTableEntry1.getIdent().getText();
				ls.addFirst(Emit.emit("/*885*/")+"vm"+identTableEntryName); // TODO blindly adding "vm" might not always be right
				backlink = identTableEntry1.getBacklink();
			} else
				throw new IllegalStateException("Invalid InstructionArgument for backlink");
		}
		final CReference reference = new CReference();
		reference.getIdentIAPath(target, gf, aog, value);
		String path = reference.build();
		LOG.info("932 "+path);
		String s = Helpers.String_join("->", ls);
		LOG.info("933 "+s);
		if (identTableEntry.getResolvedElement() instanceof ConstructorDef || identTableEntry.getResolvedElement() instanceof PropertyStatement || value != null)
			return path;
		else
			return s;
	}
}

//
//
//
