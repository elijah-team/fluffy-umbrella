/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.post_deduce;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_c.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 10/8/20 7:13 AM
 */
public class PostDeduce implements IPostDeduce {
	final static protected Logger log = LoggerFactory.getLogger(PostDeduce.class);

	private final ErrSink errSink;
	private final DeducePhase dp;

	public PostDeduce(ErrSink aErrSink, DeducePhase aDp) {
		errSink = aErrSink;
		dp = aDp;
	}

	@NotNull
	public static Collection<GeneratedNode> functions_to_list_of_generated_nodes(Collection<GeneratedFunction> generatedFunctions) {
		return Collections2.transform(generatedFunctions, new Function<GeneratedFunction, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable GeneratedFunction input) {
				return input;
			}
		});
	}

	@NotNull
	public static Collection<GeneratedNode> classes_to_list_of_generated_nodes(Collection<GeneratedClass> aGeneratedClasses) {
		return Collections2.transform(aGeneratedClasses, new Function<GeneratedClass, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable GeneratedClass input) {
				return input;
			}
		});
	}

/*
	public GenerateResult generateCode(final Collection<GeneratedNode> lgn, final WorkManager wm) {
		GenerateResult gr = new GenerateResult();

		for (final GeneratedNode generatedNode : lgn) {
			if (generatedNode instanceof GeneratedFunction) {
				GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				WorkList wl = new WorkList();
				generate_function(generatedFunction, gr, wl);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			} else if (generatedNode instanceof GeneratedContainerNC) {
				GeneratedContainerNC containerNC = (GeneratedContainerNC) generatedNode;
				containerNC.generateCode(this, gr);
			}
		}

		return gr;
	}
*/

	public void analyze() {
		int y=2;
		WorkList wl = new WorkList();
		for (GeneratedNode generatedNode : dp.generatedClasses) {
			if (generatedNode instanceof GeneratedContainerNC) {
				final GeneratedContainerNC nc = (GeneratedContainerNC) generatedNode;

				nc.analyzeNode(this);

				final Analyze_Code_For_Method acfm = new Analyze_Code_For_Method(this);
				for (GeneratedFunction generatedFunction : nc.functionMap.values()) {
					acfm.analyzeCodeForMethod(generatedFunction, wl);
				}
			}
		}
	}

/*
	static class WlGenerateFunctionC implements WorkJob {

		private final GeneratedFunction gf;
		private final GenerateResult gr;
		private final WorkList wl;
		private final GenerateC generateC;
		private boolean _isDone = false;

		public WlGenerateFunctionC(GeneratedFunction aGf, GenerateResult aGr, WorkList aWl, GenerateC aGenerateC) {
			gf = aGf;
			gr = aGr;
			wl = aWl;
			generateC = aGenerateC;
		}

		@Override
		public void run(WorkManager aWorkManager) {
			generateC.generate_function(gf, gr, wl);
			_isDone = true;
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}
	}

	public void generate_function(GeneratedFunction aGeneratedFunction, GenerateResult gr, WorkList wl) {
		generateCodeForMethod(aGeneratedFunction, gr, wl);
		for (IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.isResolved()) {
				GeneratedNode x = identTableEntry.resolvedType();

				if (x instanceof GeneratedClass) {
					generate_class((GeneratedClass) x, gr);
				} else if (x instanceof GeneratedFunction) {
					wl.addJob(new WlGenerateFunctionC((GeneratedFunction) x, gr, wl, this));
				} else {
					System.err.println(x);
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
			} else {
				GeneratedFunction gf = fi.getGenerated();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(gf, gr, wl, this));
				}
			}
		}
	}
*/

	@Override
	public void analyze_class(GeneratedClass x) {
		switch (x.getKlass().getType()) {
			// Don't generate class definition for these three
			case INTERFACE:
			case SIGNATURE:
			case ABSTRACT:
				log.info("Class is interface, signature, or abstract {}", x);
				return;
		}

		final CClassDecl decl = new CClassDecl(x);
		decl.evaluatePrimitive();

		String class_name = getTypeName(x);
		int class_code = x.getCode();

		if (!decl.prim) {
			for (GeneratedClass.VarTableEntry o : x.varTable){
				final String typeName = getTypeNameForVarTableEntry(o);
				log.info("Class {} code {} member {} with type {}", class_name, class_code, o.nameToken, typeName);
			}
		} else {
			log.info("Class {} code {} is primitive with type {}", class_name, class_code, decl.prim_decl);
		}
	}

	@NotNull public String getTypeNameForVarTableEntry(GeneratedContainer.VarTableEntry o) {
		final String typeName;
		if (o.resolvedType() != null) {
			GeneratedNode xx = o.resolvedType();
			if (xx instanceof GeneratedClass) {
				typeName = getTypeName((GeneratedClass) xx);
			} else if (xx instanceof GeneratedNamespace) {
				typeName = getTypeName((GeneratedNamespace) xx);
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
	public void analyze_namespace(GeneratedNamespace x) {
		// TODO do we need `self' parameters for namespace?
		if (x.varTable.size() > 0) {
			String class_name = getTypeName(x);
			final int class_code = x.getCode();

			for (GeneratedNamespace.VarTableEntry o : x.varTable) {
				final String typeName = getTypeNameForVarTableEntry(o);

				final String s = o.varType == null ? "void " : typeName;
				log.info("Namespace `{}' code {} member {} is {}", class_name, class_code, o.nameToken, typeName);
			}
		} else {
			log.info("Namespace {} has no variable members", x);
		}
	}

	static class GetTypeName {
		static String getTypeNameForGenClass(@NotNull GeneratedNode aGenClass) {
			String ty;
			if (aGenClass instanceof GeneratedClass)
				ty = forGenClass((GeneratedClass) aGenClass);
			else if (aGenClass instanceof GeneratedNamespace)
				ty = forGenNamespace((GeneratedNamespace) aGenClass);
			else
				ty = "Error_Unknown_GenClass";
			return ty;
		}

		static FOR_VTE forVTE(@NotNull VariableTableEntry input) {
			OS_Type attached = input.type.getAttached();
			if (input.getStatus() == BaseTableEntry.Status.UNCHECKED)
				return FOR_VTE.UNCHECKED;
			if (attached.getType() == OS_Type.Type.USER_CLASS) {
				return FOR_VTE.USER_CLASS;
			} else if (attached.getType() == OS_Type.Type.USER) {
				return FOR_VTE.USER;
			} else
				return FOR_VTE.OTHER;
		}

		static String forGenNamespace(@NotNull GeneratedNamespace aGeneratedNamespace) {
			String z;
			z = String.format("Z%d", aGeneratedNamespace.getCode());
			return z;
		}

		static String forGenClass(@NotNull GeneratedClass aGeneratedClass) {
			String z;
			z = String.format("Z%d", aGeneratedClass.getCode());
			return z;
		}

		static String forTypeTableEntry(@NotNull TypeTableEntry tte) {
			GeneratedNode res = tte.resolved();
			if (res instanceof GeneratedContainerNC) {
				final GeneratedContainerNC nc = (GeneratedContainerNC) res;
				int code = nc.getCode();
				return "Z"+code;
			} else
				return "Z<-1>";
		}

		@Deprecated
		static String forOSType(final @NotNull OS_Type ty) {
			if (ty == null) throw new IllegalArgumentException("ty is null");
			//
			String z;
			switch (ty.getType()) {
			case USER_CLASS:
				final ClassStatement el = ty.getClassOf();
				z = String.format("Z%d", el._a.getCode());//.getName();
				break;
			case FUNCTION:
				z = "<function>";
				break;
			case FUNC_EXPR:
				{
					z = "<function>";
					OS_FuncExprType fe = (OS_FuncExprType) ty;
					int y=2;
				}
				break;
			case USER:
				final TypeName typeName = ty.getTypeName();
				System.err.println("Warning: USER TypeName in GenerateC "+ typeName);
				final String s = typeName.toString();
				if (s.equals("Unit"))
					z = "void";
				else
					z = String.format("Z<%s>", s);
				break;
			case BUILT_IN:
				System.err.println("Warning: BUILT_IN TypeName in GenerateC");
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
//			return getTypeName(new OS_Type(typeName));
			}
			errSink.reportError("Type is not fully deduced "+typeName);
			return ""+typeName; // TODO type is not fully deduced
		}
	}

	enum FOR_VTE {
		UNCHECKED,
		USER_CLASS,
		USER,
		OTHER
	}

	String getTypeNameForGenClass(@NotNull GeneratedNode aGenClass) {
		return GetTypeName.getTypeNameForGenClass(aGenClass);
	}

	FOR_VTE getTypeNameForVariableEntry(@NotNull VariableTableEntry input) {
		return GetTypeName.forVTE(input);
	}

	String getTypeName(@NotNull GeneratedNamespace aGeneratedNamespace) {
		return GetTypeName.forGenNamespace(aGeneratedNamespace);
	}

	String getTypeName(@NotNull GeneratedClass aGeneratedClass) {
		return GetTypeName.forGenClass(aGeneratedClass);
	}

	String getTypeName(@NotNull TypeTableEntry tte) {
		return GetTypeName.forTypeTableEntry(tte);
	}

	@Deprecated
	String getTypeName(final @NotNull OS_Type ty) {
		return GetTypeName.forOSType(ty);
	}

	@Deprecated
	String getTypeName(final @NotNull TypeName typeName) {
		return GetTypeName.forTypeName(typeName, errSink);
	}

	@NotNull List<String> getArgumentStrings(final GeneratedFunction gf, final Instruction instruction) {
		final List<String> sl3 = new ArrayList<String>();
		final int args_size = instruction.getArgsSize();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instruction.getArg(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName(gf, (IntegerIA) ia);
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
				System.err.println("740 ProcIA");
				throw new NotImplementedException();
			} else {
				System.err.println(ia.getClass().getName());
				throw new IllegalStateException("Invalid InstructionArgument");
			}
		}
		return sl3;
	}

	static class GetAssignmentValue {

		public String FnCallArgs(FnCallArgs fca, GeneratedFunction gf) {
			final StringBuilder sb = new StringBuilder();
			final Instruction inst = fca.getExpression();
//			System.err.println("9000 "+inst.getName());
			final InstructionArgument x = inst.getArg(0);
			assert x instanceof ProcIA;
			final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
//			System.err.println("9000-2 "+pte);
			switch (inst.getName()) {
			case CALL:
			{
				if (pte.expression_num == null) {
//					assert false; // TODO synthetic methods
					final IdentExpression ptex = (IdentExpression) pte.expression;
					sb.append(ptex.getText());
					sb.append(Emit.emit("/*671*/")+"(");

					final List<String> sll = getAssignmentValueArgs(inst, gf);
					sb.append(Helpers.String_join(", ", sll));

					sb.append(")");
				} else {
					final IdentIA ia2 = (IdentIA) pte.expression_num;
					final IdentTableEntry idte = ia2.getEntry();
					if (idte.getStatus() == BaseTableEntry.Status.KNOWN) {
						final CReference reference = new CReference();
						final FunctionInvocation functionInvocation = pte.getFunctionInvocation();
						if (functionInvocation == null || functionInvocation.getFunction() == ConstructorDef.defaultVirtualCtor) {
							PostDeduce.log.warn("444 defaultVirtualCtor or null");
							reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
							final List<String> sll = getAssignmentValueArgs(inst, gf);
							reference.args(sll);
							String path = reference.build();
							sb.append(Emit.emit("/*829*/") + path);
						} else {
							final BaseGeneratedFunction pte_generated = functionInvocation.getGenerated();
							if (idte.resolvedType() == null && pte_generated != null)
								idte.resolveTypeToClass(pte_generated);
							reference.getIdentIAPath(ia2, gf, Generate_Code_For_Method.AOG.GET, null);
							final List<String> sll = getAssignmentValueArgs(inst, gf);
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
					final List<String> sll = getAssignmentValueArgs(inst, gf);
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
						final List<String> sll = getAssignmentValueArgs(inst, gf);
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

		public String ConstTableIA(ConstTableIA constTableIA, GeneratedFunction gf) {
			final ConstantTableEntry cte = gf.getConstTableEntry(constTableIA.getIndex());
//			System.err.println(("9001-3 "+cte.initialValue));
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
		private List<String> getAssignmentValueArgs(final Instruction inst, final GeneratedFunction gf) {
			final int args_size = inst.getArgsSize();
			final List<String> sll = new ArrayList<String>();
			for (int i = 1; i < args_size; i++) {
				final InstructionArgument ia = inst.getArg(i);
				final int y=2;
//			System.err.println("7777 " +ia);
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
							System.err.println(String.format("864 should always fail but didn't %s %s", path, path2));
						}
//					assert ident != null;
//					IdentTableEntry ite = gf.getIdentTableEntry(((IdentIA) ia).getIndex());
//					sll.add(Emit.emit("/*748*/")+""+ite.getIdent().getText());
						sll.add(Emit.emit("/*748*/") + "" + path2);
						System.out.println("743 " + path2 + " " + path);
					}
				} else if (ia instanceof ProcIA) {
					System.err.println("863 ProcIA");
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

		public String IntegerIA(IntegerIA integerIA, GeneratedFunction gf) {
			VariableTableEntry vte = gf.getVarTableEntry(integerIA.getIndex());
			String x = getRealTargetName(gf, vte);
			return x;
		}

		public String IdentIA(IdentIA identIA, GeneratedFunction gf) {
			assert gf == identIA.gf;
			final CReference reference = new CReference();
			reference.getIdentIAPath(identIA, gf, Generate_Code_For_Method.AOG.GET, null);
			return reference.build();
		}

		public String forClassInvocation(Instruction aInstruction, ClassInvocation aClsinv, GeneratedFunction gf) {
			int y=2;
			InstructionArgument _arg0 = aInstruction.getArg(0);
			@NotNull ProcTableEntry pte = ((ProcIA) _arg0).getEntry();
			final CtorReference reference = new CtorReference();
			reference.getConstructorPath(pte.expression_num, gf);
			@NotNull List<String> x = getAssignmentValueArgs(aInstruction, gf);
			reference.args(x);
			return reference.build(aClsinv);
		}
	}

	String getAssignmentValue(VariableTableEntry aSelf, Instruction aInstruction, ClassInvocation aClsinv, GeneratedFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		return gav.forClassInvocation(aInstruction, aClsinv, gf);
	}

	@NotNull
	String getAssignmentValue(VariableTableEntry value_of_this, final InstructionArgument value, final GeneratedFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		if (value instanceof FnCallArgs) {
			final FnCallArgs fca = (FnCallArgs) value;
			return gav.FnCallArgs(fca, gf);
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

		System.err.println(String.format("783 %s %s", value.getClass().getName(), value));
		return ""+value;
	}

	String getRealTargetName(final GeneratedFunction gf, final IntegerIA target) {
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
		return getRealTargetName(gf, varTableEntry);
	}

	static String getRealTargetName(final GeneratedFunction gf, final VariableTableEntry varTableEntry) {
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

	private static boolean isValue(GeneratedFunction gf, String name) {
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

	String getRealTargetName(final GeneratedFunction gf, final IdentIA target) {
		IdentTableEntry identTableEntry = gf.getIdentTableEntry(target.getIndex());
		LinkedList<String> ls = new LinkedList<String>();
		// TODO in Deduce set property lookupType to denote what type of lookup it is: MEMBER, LOCAL, or CLOSURE
		InstructionArgument backlink = identTableEntry.getBacklink();
		if (backlink == null) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element parent = vs.getParent().getParent();
				if (parent != gf.getFD()) {
					// we want identTableEntry.resolved which will be a GeneratedMember
					// which will have a container which will be either be a function,
					// statement (semantic block, loop, match, etc) or a GeneratedContainerNC
					int y=2;
				}
			}
			ls.add(Emit.emit("/*912*/")+"vsc->vm"+identTableEntry.getIdent().getText()); // TODO blindly adding "vm" might not always work, also put in loop
		} else
			ls.add(Emit.emit("/*872*/")+"vm"+identTableEntry.getIdent().getText()); // TODO blindly adding "vm" might not always work, also put in loop
		while (backlink != null) {
			if (backlink instanceof IntegerIA) {
				IntegerIA integerIA = (IntegerIA) backlink;
				String realTargetName = getRealTargetName(gf, integerIA);
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
		reference.getIdentIAPath(target, gf, Generate_Code_For_Method.AOG.GET, null);
		String path = reference.build();
		System.out.println("932 "+path);
		String s = Helpers.String_join("->", ls);
		System.out.println("933 "+s);
		if (identTableEntry.getResolvedElement() instanceof ConstructorDef)
			return path;
		else
			return s;
	}
}

//
//
//
