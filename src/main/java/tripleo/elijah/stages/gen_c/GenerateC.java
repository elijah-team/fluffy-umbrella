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
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.gen_c.statements.ReasonedStringListStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.ConstantTableEntry;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaContainer;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.gen_generic.Old_GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.IFixedList;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.LivingClass;
import tripleo.elijah.world.i.LivingNamespace;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 10/8/20 7:13 AM
 */
public class GenerateC implements CodeGenerator, GenerateFiles, ReactiveDimension {
	private static final   String                          PHASE                     = "GenerateC";
	final                  GI_Repo                         _repo                     = new GI_Repo(this);
	final                  Zone                            _zone                     = new Zone();
	final @NotNull         CompilationEnclosure            ce;
	final @NotNull         ErrSink                         errSink;
	final @NotNull         ElLog                           LOG;
	private final          Map<EvaNode, WhyNotGarish_Item> a_directory               = new HashMap<>();
	private final @NotNull GenerateResultEnv               _fileGen;
	final private          GenerateResultProgressive       generateResultProgressive = new GenerateResultProgressive();
	public                 GenerateResultSink              resultSink;

	public GenerateC(final @NotNull OutputFileFactoryParams aParams,
	                 final @NotNull GenerateResultEnv aFileGen) {
		_fileGen = aFileGen;

		errSink = aParams.getErrSink();

		final OS_Module       mod       = aParams.getMod();
		final ElLog.Verbosity verbosity = aParams.getVerbosity();

		LOG = new ElLog(mod.getFileName(), verbosity, PHASE);

		ce = aParams.getCompilationEnclosure();
		ce.getAccessBusPromise()
		  .then(ab -> {
			  ab.subscribePipelineLogic(pl -> pl.addLog(LOG));
		  });

		ce.addReactiveDimension(this);

		ce.getPipelineAccess().resolveWaitGenC(mod, this);
	}

	static boolean isValue(@NotNull BaseEvaFunction gf, @NotNull String name) {
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

	public void generateCodeForConstructor(final GenerateResultEnv aFileGen, final EvaConstructor gf) {
		final WhyNotGarish_Constructor cc = this.a_lookup(gf);

		cc.resolveFileGenPromise(aFileGen);
	}

	WhyNotGarish_Constructor a_lookup(final EvaConstructor aGf) {
		if (a_directory.containsKey(aGf)) {
			return (WhyNotGarish_Constructor) a_directory.get(aGf);
		}

		var ncc1907 = new WhyNotGarish_Constructor(aGf, this);
		a_directory.put(aGf, ncc1907);
		return ncc1907;
	}

	@NotNull String getRealTargetName(final @NotNull BaseEvaFunction gf, final @NotNull IdentIA target, final Generate_Code_For_Method.AOG aog, final String value) {
		int                state           = 0, code = -1;
		IdentTableEntry    identTableEntry = gf.getIdentTableEntry(target.getIndex());
		LinkedList<String> ls              = new LinkedList<String>();
		// TODO in Deduce set property lookupType to denote what type of lookup it is: MEMBER, LOCAL, or CLOSURE
		InstructionArgument backlink = identTableEntry.getBacklink();
		final String        text     = identTableEntry.getIdent().getText();
		if (backlink == null) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatement vs) {
				OS_Element parent = vs.getParent().getParent();
				if (parent != gf.getFD()) {
					// we want identTableEntry.resolved which will be a EvaMember
					// which will have a container which will be either be a function,
					// statement (semantic block, loop, match, etc) or a EvaContainerNC
					int     y  = 2;
					EvaNode er = identTableEntry.externalRef(); // FIXME move to onExternalRef
					if (er instanceof final @NotNull EvaContainerNC nc) {
						if (!(nc instanceof EvaNamespace ns)) {
							throw new AssertionError();
						} else {
							//if (ns.isInstance()) {}
							state = 1;
							code  = nc.getCode();
						}
					}
				}
			}
			switch (state) {
			case 0:
				ls.add(Emit.emit("/*912*/") + "vsc->vm" + text); // TODO blindly adding "vm" might not always work, also put in loop
				break;
			case 1:
				ls.add(Emit.emit("/*845*/") + String.format("zNZ%d_instance->vm%s", code, text));
				break;
			default:
				throw new IllegalStateException("Can't be here");
			}
		} else
			ls.add(Emit.emit("/*872*/") + "vm" + text); // TODO blindly adding "vm" might not always work, also put in loop
		while (backlink != null) {
			if (backlink instanceof final @NotNull IntegerIA integerIA) {
				String realTargetName = getRealTargetName(gf, integerIA, Generate_Code_For_Method.AOG.ASSIGN);
				ls.addFirst(Emit.emit("/*892*/") + realTargetName);
				backlink = null;
			} else if (backlink instanceof final @NotNull IdentIA identIA) {
				int             identIAIndex        = identIA.getIndex();
				IdentTableEntry identTableEntry1    = gf.getIdentTableEntry(identIAIndex);
				String          identTableEntryName = identTableEntry1.getIdent().getText();
				ls.addFirst(Emit.emit("/*885*/") + "vm" + identTableEntryName); // TODO blindly adding "vm" might not always be right
				backlink = identTableEntry1.getBacklink();
			} else
				throw new IllegalStateException("Invalid InstructionArgument for backlink");
		}
		final CReference reference = new CReference(_repo, ce);
		reference.getIdentIAPath(target, aog, value);
		String path = reference.build();
		LOG.info("932 " + path);
		String s = Helpers.String_join("->", ls);
		LOG.info("933 " + s);
		if (identTableEntry.getResolvedElement() instanceof ConstructorDef || identTableEntry.getResolvedElement() instanceof PropertyStatement/* || value != null*/)
			return path;
		else
			return s;
	}

	String getRealTargetName(final @NotNull BaseEvaFunction gf, final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
		return getRealTargetName(gf, varTableEntry);
	}

	String getRealTargetName(final BaseEvaFunction gf, final VariableTableEntry varTableEntry) {
		ZoneVTE zone_vte = _zone.get(varTableEntry, gf);

		return zone_vte.getRealTargetName();
	}

	public CompilationEnclosure _ce() {
		return ce;
	}

	//	@Override
	public ElLog elLog() {
		return this.LOG;
	}

	public GenerateResultProgressive generateResultProgressive() {
		return generateResultProgressive;
	}

	@NotNull
	List<String> getArgumentStrings(final @NotNull Supplier<IFixedList<InstructionArgument>> instructionSupplier) {
		final @NotNull List<String> sl3       = new ArrayList<String>();
		final int                   args_size = instructionSupplier.get().size();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instructionSupplier.get().get(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName((IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/") + realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference(_repo, ce);
				reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
				final String text = reference.build();
				sl3.add(Emit.emit("/*673*/") + text);
			} else if (ia instanceof final @NotNull ConstTableIA c) {
				final ConstantTableEntry cte = c.getEntry();
				final String             s   = new GetAssignmentValue().const_to_string(cte.initialValue);
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

	String getRealTargetName(final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final BaseEvaFunction    gf            = target.gf;
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());

		final ZoneVTE zone_vte = _zone.get(varTableEntry, gf);

		return zone_vte.getRealTargetName();
	}

	@NotNull String getTypeName(@NotNull TypeTableEntry tte) {
		return GetTypeName.forTypeTableEntry(tte);
	}

	@Deprecated
	public String getRealTargetName(final @NotNull WhyNotGarish_BaseFunction aGf, final @NotNull IntegerIA aTarget, final Generate_Code_For_Method.AOG aAOG) {
		return getRealTargetName(aGf.cheat(), aTarget, aAOG);
	}

	public @NotNull GI_Repo repo() {
		return _repo;
	}

	public @NotNull List<String> getArgumentStrings(final @NotNull WhyNotGarish_BaseFunction aGf, final @NotNull Instruction aInstruction) {
		return getArgumentStrings(aGf.cheat(), aInstruction);
	}

	@NotNull
	List<String> getArgumentStrings(final @NotNull BaseEvaFunction gf, final @NotNull Instruction instruction) {
		final List<String> sl3       = new ArrayList<String>();
		final int          args_size = instruction.getArgsSize();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instruction.getArg(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName(gf, (IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/") + realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference(_repo, ce);
				reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
				String text = reference.build();
				sl3.add(Emit.emit("/*673*/") + text);
			} else if (ia instanceof final @NotNull ConstTableIA c) {
				ConstantTableEntry cte = gf.getConstTableEntry(c.getIndex());
				String             s   = new GetAssignmentValue().const_to_string(cte.initialValue);
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

	@NotNull
	public String getTypeName(EvaNode aNode) {
		if (aNode instanceof EvaClass ec) {
			return a_lookup(ec).getTypeNameString();
		}
		if (aNode instanceof EvaNamespace en) {
			return a_lookup(en).getTypeNameString();
		}
		throw new IllegalStateException("Must be class or namespace.");
	}

	WhyNotGarish_Class a_lookup(final EvaClass aGc) {
		if (a_directory.containsKey(aGc)) {
			return (WhyNotGarish_Class) a_directory.get(aGc);
		}

		var ncc = new WhyNotGarish_Class(aGc, this);
		a_directory.put(aGc, ncc);
		return ncc;
	}

	public WhyNotGarish_Namespace a_lookup(final EvaNamespace en) {
		if (a_directory.containsKey(en)) {
			return (WhyNotGarish_Namespace) a_directory.get(en);
		}

		var ncn = new WhyNotGarish_Namespace(en, this);
		a_directory.put(en, ncn);
		return ncn;
	}

	@Deprecated
	String getTypeName(final @NotNull TypeName typeName) {
		return GetTypeName.forTypeName(typeName, errSink);
	}

	String getTypeNameForGenClass(@NotNull EvaNode aGenClass) {
		return GetTypeName.getTypeNameForEvaNode(aGenClass);
	}

	String getTypeNameForVariableEntry(@NotNull VariableTableEntry input) {
		return GetTypeName.forVTE(input);
	}

	@NotNull
	public String getTypeNameGNCForVarTableEntry(EvaContainer.@NotNull VarTableEntry o) {
		final String typeName;
		if (o.resolvedType() != null) {
			EvaNode xx = o.resolvedType();
			if (xx instanceof EvaClass) {
				typeName = GetTypeName.forGenClass((EvaClass) xx);
			} else if (xx instanceof EvaNamespace) {
				typeName = GetTypeName.forGenNamespace((EvaNamespace) xx);
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

	@Deprecated
	String getTypeName(final @NotNull OS_Type ty) {
		return GetTypeName.forOSType(ty, LOG);
	}

	public @NotNull String getAssignmentValue(final VariableTableEntry aSelf, final InstructionArgument aRhs, final @NotNull WhyNotGarish_BaseFunction aGf) {
		return getAssignmentValue(aSelf, aRhs, aGf.cheat());
	}

	@NotNull
	String getAssignmentValue(VariableTableEntry value_of_this, final InstructionArgument value, final @NotNull BaseEvaFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		if (value instanceof final @NotNull FnCallArgs fca) {
			return gav.FnCallArgs(fca, gf, LOG);
		}

		if (value instanceof final @NotNull ConstTableIA constTableIA) {
			return gav.ConstTableIA(constTableIA, gf);
		}

		if (value instanceof final @NotNull IntegerIA integerIA) {
			return gav.IntegerIA(integerIA, gf);
		}

		if (value instanceof final @NotNull IdentIA identIA) {
			return gav.IdentIA(identIA, gf);
		}

		LOG.err(String.format("783 %s %s", value.getClass().getName(), value));
		return String.valueOf(value);
	}

	@Override
	public void generate_namespace(final EvaNamespace aGeneratedNamespace, final GenerateResult aGenerateResult) {
		throw new NotImplementedException();

	}

	@Override
	public void generate_class(final EvaClass aGeneratedClass, final GenerateResult aGenerateResult) {
		throw new NotImplementedException();

	}

	@Override
	public void generate_function(@NotNull EvaFunction aEvaFunction, GenerateResult gr, @NotNull WorkList wl, final GenerateResultSink aResultSink) {
		generateCodeForMethod(_fileGen, aEvaFunction);
		_post_generate_function(aEvaFunction, wl, _fileGen);
	}

	@Override
	public void generate_constructor(@NotNull EvaConstructor aEvaConstructor, GenerateResult gr, @NotNull WorkList wl, final GenerateResultSink aResultSink, final WorkManager aWorkManager, final @NotNull GenerateResultEnv aFileGen) {
		generateCodeForConstructor(aEvaConstructor, gr, wl, aFileGen);
		postGenerateCodeForConstructor(aEvaConstructor, wl, aFileGen);
	}

	@Override
	public void finishUp(final GenerateResult aGenerateResult, final WorkManager wm, final WorkList aWorkList) {
		assert _fileGen != null;

		for (WhyNotGarish_Item value : new ArrayList<>(a_directory.values())) {
			if (!value.hasFileGen())
				value.provideFileGen(_fileGen);
		}
	}

	@Override
	public void generate_namespace(final @NotNull EvaNamespace x, final GenerateResult gr, final @NotNull GenerateResultSink aResultSink) {
		final LivingNamespace ln = aResultSink.getLivingNamespaceForEva(x); // TODO could also add _living property
		ln.garish(this, gr, aResultSink);
	}

	@Override
	public GenerateResultEnv getFileGen() {
		return _fileGen;
	}

	@Override
	public GenerateResult resultsFromNodes(final @NotNull List<EvaNode> aNodes,
	                                       final @NotNull WorkManager wm,
	                                       final @NotNull GenerateResultSink grs,
	                                       final @NotNull GenerateResultEnv fg) {
		final GenerateResult gr2 = fg.gr();

		assert fg.resultSink() == grs;

		for (final EvaNode generatedNode : aNodes) {
			if (generatedNode instanceof final @NotNull EvaContainerNC nc) {

				nc.generateCode(_fileGen, this);
				final @NotNull Collection<EvaNode> gn1 = (nc.functionMap.values()).stream().map(x -> (EvaNode) x).collect(Collectors.toList());
				final GenerateResult               gr3 = this.generateCode(gn1, fg);
				grs.additional(gr3);
				final @NotNull Collection<EvaNode> gn2 = (nc.classMap.values()).stream().map(x -> (EvaNode) x).collect(Collectors.toList());
				final GenerateResult               gr4 = this.generateCode(gn2, fg);
				grs.additional(gr4);
			} else {
				LOG.info("2009 " + generatedNode.getClass().getName());
			}
		}

		return gr2;
	}

	//	@Override
	public @NotNull GenerateResult generateCode(final @NotNull Collection<EvaNode> lgn, final @NotNull GenerateResultEnv aFileGen) {
		GenerateResult gr = new Old_GenerateResult();
		WorkList       wl = new WorkList();

		var                      wm          = aFileGen.wm();
		final GenerateResultSink aResultSink = aFileGen.resultSink();

		for (final EvaNode evaNode : lgn) {
			if (evaNode instanceof final @NotNull EvaFunction generatedFunction) {
				generate_function(generatedFunction, gr, wl, aResultSink);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			} else if (evaNode instanceof final @NotNull EvaContainerNC containerNC) {
				containerNC.generateCode(_fileGen, this);
			} else if (evaNode instanceof final @NotNull EvaConstructor evaConstructor) {
				generate_constructor(evaConstructor, gr, wl, aResultSink, wm, aFileGen);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			}
		}

		return gr;
	}

	public void generateCodeForMethod(final GenerateResultEnv aFileGen, final BaseEvaFunction aEvaFunction) {
		final WhyNotGarish_Function cf = this.a_lookup(aEvaFunction);

		cf.resolveFileGenPromise(aFileGen);
	}

	private void _post_generate_function(final @NotNull EvaFunction aEvaFunction, final @NotNull WorkList wl, final @NotNull GenerateResultEnv fileGen) {
		for (IdentTableEntry identTableEntry : aEvaFunction.idte_list) {
			if (identTableEntry.isResolved()) {
				EvaNode x = identTableEntry.resolvedType();

				if (x instanceof EvaClass) {
					generate_class(fileGen, (EvaClass) x);
				} else if (x instanceof EvaFunction) {
					wl.addJob(new WlGenerateFunctionC(fileGen, (EvaFunction) x, this));
				} else {
					LOG.err(x.toString());
					throw new NotImplementedException();
				}
			}
		}
		for (ProcTableEntry pte : aEvaFunction.prte_list) {
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
			} else {
				BaseEvaFunction gf = fi.getEva();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(fileGen, gf, this));
				}
			}
		}
	}

	private void generateCodeForConstructor(@NotNull EvaConstructor aEvaConstructor, GenerateResult aGenerateResult, WorkList aWorkList, final @NotNull GenerateResultEnv aFileGen) {
		if (aEvaConstructor.getFD() == null) return;
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(this, LOG);
		gcfm.generateCodeForConstructor(aEvaConstructor, aGenerateResult, aWorkList, aFileGen);
	}

	private void postGenerateCodeForConstructor(final @NotNull EvaConstructor aEvaConstructor, final @NotNull WorkList wl, final @NotNull GenerateResultEnv aFileGen) {
		for (IdentTableEntry identTableEntry : aEvaConstructor.idte_list) {
			identTableEntry.reactive().addResolveListener((IdentTableEntry x) -> {
				generateIdent(x);
			});

			if (identTableEntry.isResolved()) {
				generateIdent(identTableEntry);
			}
		}
		for (ProcTableEntry pte : aEvaConstructor.prte_list) {
//			ClassInvocation ci = pte.getClassInvocation();
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
				int y = 2;
			} else {
				BaseEvaFunction gf = fi.getEva();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(aFileGen, gf, this));
				}
			}
		}
	}

	WhyNotGarish_Function a_lookup(final BaseEvaFunction aGf) {
		if (a_directory.containsKey(aGf)) {
			return (WhyNotGarish_Function) a_directory.get(aGf);
		}

		var ncf = new WhyNotGarish_Function(aGf, this);
		a_directory.put(aGf, ncf);
		return ncf;
	}

	//	@Override
	public void generate_class(@NotNull GenerateResultEnv aFileGen, EvaClass x) {
		var gr          = aFileGen.gr();
		var aResultSink = aFileGen.resultSink();

		final LivingClass lc = aResultSink.getLivingClassForEva(x); // TODO could also add _living property

		lc.getGarish().garish(this, gr, aResultSink);
	}

	private void generateIdent(@NotNull IdentTableEntry identTableEntry) {
		assert identTableEntry.isResolved();

		final @NotNull EvaNode x = identTableEntry.resolvedType();

		final GenerateResult     gr          = _fileGen.gr();
		final GenerateResultSink resultSink1 = _fileGen.resultSink();
		final WorkList           wl          = _fileGen.wl();

		if (x instanceof final EvaClass evaClass) {
			generate_class(_fileGen, evaClass);
		} else if (x instanceof final EvaFunction evaFunction) {
			wl.addJob(new WlGenerateFunctionC(_fileGen, evaFunction, this));
		} else {
			LOG.err(x.toString());
			throw new NotImplementedException();
		}
	}

	enum GetTypeName {
		;

		@Deprecated
		static String forOSType(final @NotNull OS_Type ty, @NotNull ElLog LOG) {
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
				z = Emit.emit("/*443*/") + String.format("Z%d/*%s*/", -4 /*el._a.getCode()*/, name);//.getName();
				break;
			case FUNCTION:
				z = "<function>";
				break;
			case FUNC_EXPR: {
				z = "<function>";
				OS_FuncExprType fe = (OS_FuncExprType) ty;
				int             y  = 2;
			}
			break;
			case USER:
				final TypeName typeName = ty.getTypeName();
				LOG.err("Warning: USER TypeName in GenerateC " + typeName);
				final String s = typeName.toString();
				if (s.equals("Unit"))
					z = "void";
				else
					z = String.format("Z<Unknown_USER_Type /*%s*/>", s);
				break;
			case BUILT_IN:
				LOG.err("Warning: BUILT_IN TypeName in GenerateC");
				z = "Z" + ty.getBType().getCode();  // README should not even be here, but look at .name() for other code gen schemes
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
			errSink.reportError("Type is not fully deduced " + typeName);
			return String.valueOf(typeName); // TODO type is not fully deduced
		}

		static @NotNull String forTypeTableEntry(@NotNull TypeTableEntry tte) {
			EvaNode res = tte.resolved();
			if (res instanceof final @NotNull EvaContainerNC nc) {
				int code = nc.getCode();
				return "Z" + code;
			} else
				return "Z<-1>";
		}

		static String forVTE(@NotNull VariableTableEntry input) {
			OS_Type attached = input.getType().getAttached();
			if (attached == null)
				return Emit.emit("/*390*/") + "Z__Unresolved*"; // TODO remove this ASAP
			//
			// special case
			//
			if (input.getType().genType.getNode() != null)
				return Emit.emit("/*395*/") + getTypeNameForEvaNode(input.getType().genType.getNode()) + "*";
			//
			if (input.getStatus() == BaseTableEntry.Status.UNCHECKED)
				return "Error_UNCHECKED_Type";
			if (attached.getType() == OS_Type.Type.USER_CLASS) {
				return attached.getClassOf().name();
			} else if (attached.getType() == OS_Type.Type.USER) {
				TypeName typeName = attached.getTypeName();
				String   name;
				if (typeName instanceof NormalTypeName)
					name = ((NormalTypeName) typeName).getName();
				else
					name = typeName.toString();
				return String.format(Emit.emit("/*543*/") + "Z<%s>*", name);
			} else
				throw new NotImplementedException();
		}

		static String getTypeNameForEvaNode(@NotNull EvaNode aEvaNode) {
			String ty;
			if (aEvaNode instanceof EvaClass)
				ty = forGenClass((EvaClass) aEvaNode);
			else if (aEvaNode instanceof EvaNamespace)
				ty = forGenNamespace((EvaNamespace) aEvaNode);
			else
				ty = "Error_Unknown_GenClass";
			return ty;
		}

		static String forGenClass(@NotNull EvaClass aEvaClass) {
			String z;
			z = String.format("Z%d", aEvaClass.getCode());
			return z;
		}

		static String forGenNamespace(@NotNull EvaNamespace aEvaNamespace) {
			String z;
			z = String.format("Z%d", aEvaNamespace.getCode());
			return z;
		}
	}

	static class WlGenerateFunctionC implements WorkJob {

		public final           GenerateResultSink resultSink;
		private final          GenerateFiles      generateC;
		private final          BaseEvaFunction    gf;
		private final          GenerateResult     gr;
		private final          WorkList           wl;
		private final @NotNull GenerateResultEnv  fileGen;
		private                boolean            _isDone = false;

		public WlGenerateFunctionC(@NotNull GenerateResultEnv fileGen, BaseEvaFunction aGf, final GenerateFiles aGenerateC) {
			gf = aGf;

			gr         = fileGen.gr();
			wl         = fileGen.wl();
			generateC  = aGenerateC;
			resultSink = fileGen.resultSink();

			this.fileGen = fileGen;
		}

		@Override
		public void run(WorkManager aWorkManager) {
			if (gf instanceof EvaFunction)
				generateC.generate_function((EvaFunction) gf, gr, wl, resultSink);
			else
				generateC.generate_constructor((EvaConstructor) gf, gr, wl, resultSink, aWorkManager, fileGen);
			_isDone = true;
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}
	}

	public class GenerateResultProgressive {
		GenerateResult _gr = new Old_GenerateResult();

		public void addConstructor(final @NotNull EvaConstructor aGf, final Buffer aBuf, final GenerateResult.TY aTY) {
			final LibraryStatementPart lsp = aGf.module().getLsp();

			_gr.addConstructor(aGf, aBuf, aTY, lsp);
		}

		public void addFunction(final @NotNull EvaFunction aGf, final Buffer aBufHdr, final GenerateResult.TY aTY) {
			final LibraryStatementPart lsp = aGf.module().getLsp();

			_gr.addFunction(aGf, aBufHdr, aTY, lsp);
		}
	}

	/*static*/  class GetAssignmentValue {

		public String ConstTableIA(@NotNull ConstTableIA constTableIA, @NotNull BaseEvaFunction gf) {
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

		String const_to_string(final IExpression expression) {
			final GCX_ConstantString cs = new GCX_ConstantString(GenerateC.this,
			  GetAssignmentValue.this,
			  expression);

			return cs.getText();
		}

		public @NotNull String FnCallArgs(@NotNull FnCallArgs fca, @NotNull BaseEvaFunction gf, @NotNull ElLog LOG) {
			final StringBuilder sb   = new StringBuilder();
			final Instruction   inst = fca.getExpression();
//			LOG.err("9000 "+inst.getName());
			final InstructionArgument x = inst.getArg(0);
			assert x instanceof ProcIA;
			final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
//			LOG.err("9000-2 "+pte);
			switch (inst.getName()) {
			case CALL: {
				if (pte.expression_num == null) {
//					assert false; // TODO synthetic methods
					final FnCallArgs_Statement statement = new FnCallArgs_Statement(GenerateC.this, this, pte, inst, gf);

					sb.append(statement.getText());
				} else {
					final FnCallArgs_Statement2 statement = new FnCallArgs_Statement2(GenerateC.this, gf, LOG, inst, pte, this);

					sb.append(statement.getText());
				}
				return sb.toString();
			}
			case CALLS: {
				CReference reference = null;
				if (pte.expression_num == null) {
					final int             y    = 2;
					final IdentExpression ptex = (IdentExpression) pte.__debug_expression();
					sb.append(Emit.emit("/*684*/"));
					sb.append(ptex.getText());

					final DeduceElement3_ProcTableEntry pte_de3 = (DeduceElement3_ProcTableEntry) pte.getDeduceElement3(pte._deduceTypes2(), pte.__gf);
					var                                 s       = pte_de3.toString();

					//final DR_Ident id = pte.__gf.getIdent((IdentExpression) pte.expression);
					//id.resolve();

					int yy = 2;
				} else {
					// TODO Why not expression_num?
					reference = new CReference(_repo, ce);
					final IdentIA ia2 = (IdentIA) pte.expression_num;
					reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
					final GetAssignmentValueArgsStatement ava = getAssignmentValueArgs(inst, gf, LOG);
					final List<String>                    sll = ava.stringList();
					reference.args(sll);
					String path = reference.build();
					sb.append(Emit.emit("/*807*/") + path);

					final IExpression ptex = pte.__debug_expression();
					if (ptex instanceof IdentExpression aIdentExpression) {
						var z = new ReasonedStringListStatement();

						z.append(Emit.emit("/*803*/"), "emit-code");
						z.append(aIdentExpression.getText(), "ptex");

						sb.append(z.getText());
					} else if (ptex instanceof ProcedureCallExpression) {
						var z = new ReasonedStringListStatement();

						z.append(Emit.emit("/*806*/"), "emit-code");
						z.append(ptex.getLeft().toString()/*FIXME 09/07*/, "ptex"); // TODO Qualident, IdentExpression, DotExpression

						sb.append(z.getText());
					}
				}
				if (true /*reference == null*/) {
					var z = new ReasonedStringListStatement();

					final GetAssignmentValueArgsStatement ava = getAssignmentValueArgs(inst, gf, LOG);
					final List<String>                    sll = ava.stringList();

					z.append(Emit.emit("/*810*/"), "emit-code");
					z.append("(", "open-brace");
					z.append(ava, "GetAssignmentValueArgsStatement");
					z.append(");", "close-brace");

					sb.append(z.getText());
				}
				return sb.toString();
			}
			default:
				throw new IllegalStateException("Unexpected value: " + inst.getName());
			}
		}

		GetAssignmentValueArgsStatement getAssignmentValueArgs(final @NotNull Instruction inst, final @NotNull BaseEvaFunction gf, @NotNull ElLog LOG) {
			var gavas = new GetAssignmentValueArgsStatement(inst);

			final int args_size = inst.getArgsSize();

			for (int i = 1; i < args_size; i++) {
				final InstructionArgument ia = inst.getArg(i);
				final int                 y  = 2;

				//LOG.err("7777 " + ia);

				if (ia instanceof final ConstTableIA constTableIA) {
					final ConstantTableEntry constTableEntry = gf.getConstTableEntry(constTableIA.getIndex());
					gavas.add_string(const_to_string(constTableEntry.initialValue));
				} else if (ia instanceof final IntegerIA integerIA) {
					final VariableTableEntry variableTableEntry = gf.getVarTableEntry(integerIA.getIndex());
					gavas.add_string(Emit.emit("/*853*/") + _zone.get(variableTableEntry, gf).getRealTargetName());
				} else if (ia instanceof final IdentIA identIA) {
					final String          path = gf.getIdentIAPathNormal(identIA);    // return x.y.z
					final IdentTableEntry ite  = identIA.getEntry();

					if (ite.getStatus() == BaseTableEntry.Status.UNKNOWN) {
						gavas.add_string(String.format("%s is UNKNOWN", path));
					} else {
						final CReference reference = new CReference(_repo, ce);
						reference.getIdentIAPath(identIA, Generate_Code_For_Method.AOG.GET, null);
						final String path2 = reference.build();                        // return ZP105get_z(vvx.vmy)

						if (path.equals(path2)) {
							// should always fail
							//throw new AssertionError();
							LOG.err(String.format("864 should always fail but didn't %s %s", path, path2));
						}

						//assert ident != null;
						//IdentTableEntry ite = gf.getIdentTableEntry(((IdentIA) ia).getIndex());
						//sll.add(Emit.emit("/*748*/")+""+ite.getIdent().getText());
						gavas.add_string(Emit.emit("/*748*/") + path2);
						LOG.info("743 " + path2 + " " + path);
					}
				} else if (ia instanceof ProcIA) {
					LOG.err("863 ProcIA");
					throw new NotImplementedException();
				} else {
					throw new IllegalStateException("Cant be here: Invalid InstructionArgument");
				}
			}

			return gavas;
		}

		// TODO look at me
		public String forClassInvocation(@NotNull Instruction aInstruction, ClassInvocation aClsinv, @NotNull BaseEvaFunction gf, @NotNull ElLog LOG) {
			InstructionArgument     _arg0     = aInstruction.getArg(0);
			@NotNull ProcTableEntry pte       = gf.getProcTableEntry(((ProcIA) _arg0).index());
			final CtorReference     reference = new CtorReference();
			reference.getConstructorPath(pte.expression_num, gf);
			final GetAssignmentValueArgsStatement ava = getAssignmentValueArgs(aInstruction, gf, LOG);
			@NotNull List<String>                 x   = ava.stringList();
			reference.args(x);
			return reference.build(aClsinv);
		}

		public @NotNull String IdentIA(@NotNull IdentIA identIA, BaseEvaFunction gf) {
			assert gf == identIA.gf; // yup
			final CReference reference = new CReference(_repo, ce);
			reference.getIdentIAPath(identIA, Generate_Code_For_Method.AOG.GET, null);
			return reference.build();
		}

		public String IntegerIA(@NotNull IntegerIA integerIA, @NotNull BaseEvaFunction gf) {
			VariableTableEntry vte = gf.getVarTableEntry(integerIA.getIndex());
			String             x   = getRealTargetName(gf, vte);
			return x;
		}
	}
}

//
//
//
