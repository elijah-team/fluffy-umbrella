///*
// * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
// *
// * The contents of this library are released under the LGPL licence v3,
// * the GNU Lesser General Public License text was downloaded from
// * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
// *
// */
//package tripleo.elijah.stages.gen_fn;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Collections2;
//import org.checkerframework.checker.nullness.qual.Nullable;
//import org.jdeferred2.DoneCallback;
//import org.jetbrains.annotations.NotNull;
//import tripleo.elijah.comp.PipelineLogic;
//import tripleo.elijah.entrypoints.EntryPoint;
//import tripleo.elijah.entrypoints.EntryPointList;
//import tripleo.elijah.lang.*;
//import tripleo.elijah.lang2.BuiltInTypes;
//import tripleo.elijah.lang2.SpecialFunctions;
//import tripleo.elijah.stages.deduce.ClassInvocation;
//import tripleo.elijah.stages.deduce.DeduceConstructStatement;
//import tripleo.elijah.stages.deduce.DeducePhase;
//import tripleo.elijah.stages.deduce.FunctionInvocation;
//import tripleo.elijah.stages.instructions.*;
//import tripleo.elijah.stages.logging.ElLog;
//import tripleo.elijah.stages.stage1.S1_Constructor;
//import tripleo.elijah.util.Helpers;
//import tripleo.elijah.util.NotImplementedException;
//import tripleo.elijah.work.WorkManager;
//import tripleo.util.range.Range;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;
//import static tripleo.elijah.util.Helpers.List_of;
//
///**
// * Created 9/10/20 2:28 PM
// */
//public class GenerateFunctions {
//	private static final String PHASE = "GenerateFunctions";
//	private final GeneratePhase phase;
//	final OS_Module module;
//	private final ElLog LOG;
//
//	public GenerateFunctions(final GeneratePhase aPhase, final OS_Module aModule, final PipelineLogic aPipelineLogic) {
//		phase = aPhase;
//		module = aModule;
//		LOG = new ElLog(module.getFileName(), aPhase.getVerbosity(), PHASE);
//		//
//		aPipelineLogic.addLog(LOG);
//	}
//
//	@Deprecated
//	public void generateFromEntryPoints(final List<EntryPoint> entryPoints, final DeducePhase dp) {
//		@NotNull final EntryPointList epl = new EntryPointList();
//
//		entryPoints.stream().forEach(epl::add);
//
//		generateFromEntryPoints(epl, dp);
//	}
//
//	public void generateFromEntryPoints(final EntryPointList epl, final DeducePhase deducePhase) {
//		epl.generate(this, deducePhase, () -> phase.wm);
//	}
//
//	public static class S1toG_GC_Processor {
//		private final GenerateFunctions gf;
//
//		public S1toG_GC_Processor(final GenerateFunctions generateFunctions) {
//			this.gf = generateFunctions;
//		}
//
//		public int add_i(final GeneratedConstructor gf2, final InstructionName e, final List<InstructionArgument> lia, final Context cctx) {
//			return gf.add_i(gf2, e, lia, cctx);
//		}
//
//		public void generate_item(final OS_Element item, final GeneratedConstructor gf2, final Context cctx) {
//			gf.generate_item(item, gf2, cctx);
//		}
//	}
//
//	public @NotNull GeneratedConstructor generateConstructor(final ConstructorDef aConstructorDef,
//	                                                         final ClassStatement parent, // TODO Namespace constructors
//	                                                         final FunctionInvocation aFunctionInvocation) {
//		final S1_Constructor s1c = new S1_Constructor(aConstructorDef, parent, aFunctionInvocation);
//
//		s1c.process(new S1toG_GC_Processor(this));
//
//		final GeneratedConstructor gf = s1c.getGenerated();
//		gf.fi = aFunctionInvocation; // TODO smelly
//
//		return gf;
//	}
//
//	public @NotNull GeneratedConstructor generateConstructor_000(final ConstructorDef aConstructorDef,
//	                                                             final ClassStatement parent, // TODO Namespace constructors
//	                                                             final FunctionInvocation aFunctionInvocation) {
//		final GeneratedConstructor gf = new GeneratedConstructor(aConstructorDef);
//		gf.setFunctionInvocation(aFunctionInvocation);
//		if (parent instanceof ClassStatement) {
//			final OS_Type parentType = parent.getOS_Type();
//			final IdentExpression selfIdent = IdentExpression.forString("self");
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, parentType, selfIdent);
//			gf.addVariableTableEntry("self", VariableTableType.SELF, tte, null);
//		}
//
//		{
//			final List<FormalArgListItem> fali_args = aConstructorDef.fal().falis;
//			final List<TypeTableEntry> fi_args = aFunctionInvocation.getArgs();
//
//			for (int i = 0; i < fali_args.size(); i++) {
//				final FormalArgListItem fali = fali_args.get(i);
//
//				final TypeTableEntry tte1 = fi_args.get(i);
//				final OS_Type attached = tte1.getAttached();
//
//				// TODO for reference now...
//				final GenType genType = new GenType();
//				final TypeName typeName = fali.typeName();
//				if (typeName != null)
//					genType.typeName = new OS_Type(typeName);
//				genType.resolved = attached;
//
//				final OS_Type attached1;
//				if (attached == null && typeName != null)
//					attached1 = genType.typeName;
//				else
//					attached1 = attached;
//
//				final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, attached1, fali.getNameToken());
////				assert attached != null; // TODO this fails
//
//				gf.addVariableTableEntry(fali.name(), VariableTableType.ARG, tte, fali);
//			}
//		}
//
//		final Context cctx = aConstructorDef.getContext();
//		final int e1 = add_i(gf, InstructionName.E, null, cctx);
//		for (final FunctionItem item : aConstructorDef.getItems()) {
////			LOG.err("7056 aConstructorDef.getItem = "+item);
//			generate_item((OS_Element) item, gf, cctx);
//		}
//		final int x1 = add_i(gf, InstructionName.X, List_of(new IntegerIA(e1, gf)), cctx);
//		gf.addContext(aConstructorDef.getContext(), new Range(e1, x1)); // TODO remove interior contexts
////		LOG.info(String.format("602.1 %s", aConstructorDef.name()));
////		for (Instruction instruction : gf.instructionsList) {
////			LOG.info(instruction);
////		}
////		GeneratedFunction.printTables(gf);
//		gf.fi = aFunctionInvocation;
//		return gf;
//	}
//
//	@NotNull GeneratedFunction generateFunction(@NotNull final FunctionDef fd,
//	                                            final OS_Element parent,
//	                                            @NotNull final FunctionInvocation aFunctionInvocation) {
////		LOG.err("601.1 fn "+fd.name() + " " + parent);
//		final GeneratedFunction gf = new GeneratedFunction(fd);
//		if (parent instanceof ClassStatement)
//			gf.addVariableTableEntry("self",
//			  VariableTableType.SELF,
//			  gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, ((ClassStatement) parent).getOS_Type(), IdentExpression.forString("self")),
//			  null);
//		final OS_Type returnType;
//		final TypeName returnType1 = fd.returnType();
//		if (returnType1 == null)
//			returnType = new OS_Type.OS_UnitType();
//		else
//			returnType = new OS_Type(returnType1);
//		gf.addVariableTableEntry("Result",
//		  VariableTableType.RESULT,
//		  gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, returnType, IdentExpression.forString("Result")),
//		  null); // TODO what about Unit returns?
//
//		{
//			final List<FormalArgListItem> fali_args = fd.fal().falis;
//			final List<TypeTableEntry> fi_args = aFunctionInvocation.getArgs();
//
//			for (int i = 0; i < fali_args.size(); i++) {
//				final FormalArgListItem fali = fali_args.get(i);
//
//				final TypeTableEntry tte1 = fi_args.get(i);
//				final OS_Type attached = tte1.getAttached();
//
//				// TODO for reference now...
//				final GenType genType = new GenType();
//				final TypeName typeName = fali.typeName();
//				final OS_Type type;
//				if (typeName != null)
//					type = new OS_Type(typeName);
//				else
//					type = null;
//				final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, fali.getNameToken());
//				gf.addVariableTableEntry(fali.name(), VariableTableType.ARG, tte, fali);
//			}
//		}
//
//		// TODO Exception !!??
//
//		//
//		final Context cctx = fd.getContext();
//		final int e1 = add_i(gf, InstructionName.E, null, cctx);
//		for (final FunctionItem item : fd.getItems()) {
////			LOG.err("7001 fd.getItem = "+item);
//			generate_item((OS_Element) item, gf, cctx);
//		}
//		final int x1 = add_i(gf, InstructionName.X, List_of(new IntegerIA(e1, gf)), cctx);
//		gf.addContext(fd.getContext(), new Range(e1, x1)); // TODO remove interior contexts
////		LOG.info(String.format("602.1 %s", fd.name()));
////		for (Instruction instruction : gf.instructionsList) {
////			LOG.info(instruction);
////		}
////		GeneratedFunction.printTables(gf);
//
//		gf.fi = aFunctionInvocation;
//		return gf;
//	}
//
//	/**
//	 * See {@link WlGenerateClass#run(WorkManager)}
//	 *
//	 * @param aClassStatement
//	 * @param aClassInvocation
//	 * @return
//	 */
//	public GeneratedClass generateClass(final ClassStatement aClassStatement, final ClassInvocation aClassInvocation) {
//		@NotNull final GeneratedClass Result = generateClass(aClassStatement);
//		Result.ci = aClassInvocation;
//		return Result;
//	}
//
//	/**
//	 * See {@link WlGenerateFunction#run(WorkManager)}
//	 *
//	 * @param aFunctionDef
//	 * @param aClassStatement
//	 * @param aFunctionInvocation
//	 * @return
//	 */
//	public GeneratedFunction generateFunction(@NotNull final FunctionDef aFunctionDef,
//	                                          @NotNull final OS_Element aClassStatement,
//	                                          @NotNull final FunctionInvocation aFunctionInvocation) {
//		@NotNull final GeneratedFunction Result = generateFunction(aFunctionDef, aClassStatement);
//		Result.fi = aFunctionInvocation;
//		return Result;
//	}
//
//	@Deprecated
//	public void generateFromEntryPoints(final List<EntryPoint> entryPoints, final DeducePhase dp) {
//		@NotNull final EntryPointList epl = new EntryPointList();
//
//		entryPoints.stream().forEach(epl::add);
//
//		generateFromEntryPoints(epl, dp);
//	}
//
//	class Generate_Item {
//		void generate_alias_statement(final AliasStatement as) {
//			throw new NotImplementedException();
//		}
//
//		private void generate_case_conditional(final CaseConditional cc) {
//			final int y = 2;
//			LOG.err("Skip CaseConditional for now");
////			throw new NotImplementedException();
//		}
//
//		private void generate_match_conditional(@NotNull final MatchConditional mc, final @NotNull BaseGeneratedFunction gf) {
//			final int y = 2;
//			final Context cctx = mc.getParent().getContext(); // TODO MatchConditional.getContext returns NULL!!!
//			{
//				final IExpression expr = mc.getExpr();
//				final InstructionArgument i = simplify_expression(expr, gf, cctx);
////				LOG.info("710 " + i);
//
//				Label label_next = gf.addLabel();
//				final Label label_end  = gf.addLabel();
//
//				{
//					for (final MatchConditional.MC1 part : mc.getParts()) {
//						if (part instanceof MatchConditional.MatchArm_TypeMatch) {
//							final MatchConditional.MatchArm_TypeMatch mc1 = (MatchConditional.MatchArm_TypeMatch) part;
//							final TypeName tn = mc1.getTypeName();
//							final IdentExpression id = mc1.getIdent();
//
//							final int begin0 = add_i(gf, InstructionName.ES, null, cctx);
//
//							final int tmp = addTempTableEntry(new OS_Type(tn), id, gf, id); // TODO no context!
//							final VariableTableEntry vte_tmp = gf.getVarTableEntry(tmp);
//							final TypeTableEntry t = vte_tmp.type;
//							add_i(gf, InstructionName.IS_A, List_of(i, new IntegerIA(t.getIndex(), gf), /*TODO not*/new LabelIA(label_next)), cctx);
//							final Context context = mc1.getContext();
//
//							add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), context);
//							final int cast_inst = add_i(gf, InstructionName.CAST_TO, List_of(new IntegerIA(tmp, gf), new IntegerIA(t.getIndex(), gf), (i)), context);
//							vte_tmp.addPotentialType(cast_inst, t); // TODO in the future instructionIndex may be unsigned
//
//							for (final FunctionItem item : mc1.getItems()) {
//								generate_item((OS_Element) item, gf, context);
//							}
//
//							add_i(gf, InstructionName.JMP, List_of(label_end), context);
//							add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
//							gf.place(label_next);
//							label_next = gf.addLabel();
//						} else if (part instanceof MatchConditional.MatchConditionalPart2) {
//							final MatchConditional.MatchConditionalPart2 mc2 = (MatchConditional.MatchConditionalPart2) part;
//							final IExpression id = mc2.getMatchingExpression();
//
//							final int begin0 = add_i(gf, InstructionName.ES, null, cctx);
//
//							final InstructionArgument i2 = simplify_expression(id, gf, cctx);
//							add_i(gf, InstructionName.JNE, List_of(i, i2, label_next), cctx);
//							final Context context = mc2.getContext();
//
//							for (final FunctionItem item : mc2.getItems()) {
//								generate_item((OS_Element) item, gf, context);
//							}
//
//							add_i(gf, InstructionName.JMP, List_of(label_end), context);
//							add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
//							gf.place(label_next);
////							label_next = gf.addLabel();
//						} else if (part instanceof MatchConditional.MatchConditionalPart3) {
//							LOG.err("Don't know what this is");
//						}
//					}
//					gf.place(label_next);
//					add_i(gf, InstructionName.NOP, List_of(), cctx);
//					gf.place(label_end);
//				}
//			}
//		}
//
//		private void generate_if(@NotNull final IfConditional ifc, final @NotNull BaseGeneratedFunction gf) {
//			final Context cctx = ifc.getContext();
//			final IdentExpression Boolean_true = Helpers.string_to_ident("true");
//			final Label label_next = gf.addLabel();
//			final Label label_end  = gf.addLabel();
//			{
//				final int begin0 = add_i(gf, InstructionName.ES, null, cctx);
//				final IExpression expr = ifc.getExpr();
//				final InstructionArgument i = simplify_expression(expr, gf, cctx);
////				LOG.info("711 " + i);
//				final int const_true = addConstantTableEntry("true", Boolean_true, new OS_Type(BuiltInTypes.Boolean), gf);
//				add_i(gf, InstructionName.JNE, List_of(i, new ConstTableIA(const_true, gf), label_next), cctx);
//				final int begin_1st = add_i(gf, InstructionName.ES, null, cctx);
//				final int begin_2nd = add_i(gf, InstructionName.ES, null, cctx);
//				for (final OS_Element item : ifc.getItems()) {
//					generate_item(item, gf, cctx);
//				}
//				add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_2nd, gf)), cctx);
//				if (ifc.getParts().size() == 0) {
//					gf.place(label_next);
//					add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_1st, gf)), cctx);
////					gf.place(label_end);
//				} else {
//					add_i(gf, InstructionName.JMP, List_of(label_end), cctx);
//					final List<IfConditional> parts = ifc.getParts();
//					for (final IfConditional part : parts) {
//						gf.place(label_next);
////						label_next = gf.addLabel();
//						if (part.getExpr() != null) {
//							final InstructionArgument ii = simplify_expression(part.getExpr(), gf, cctx);
//							LOG.info("712 " + ii);
//							add_i(gf, InstructionName.JNE, List_of(ii, new ConstTableIA(const_true, gf), label_next), cctx);
//						}
//						final int begin_next = add_i(gf, InstructionName.ES, null, cctx);
//						for (final OS_Element partItem : part.getItems()) {
//							LOG.info("709 " + part + " " + partItem);
//							generate_item(partItem, gf, cctx);
//						}
//						add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_next, gf)), cctx);
//						gf.place(label_next);
//					}
//					gf.place(label_end);
//				}
//				add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
//			}
//		}
//
//		private void generate_loop(@NotNull final Loop loop, final @NotNull BaseGeneratedFunction gf) {
//			final Context cctx = loop.getContext();
//			final int e2 = add_i(gf, InstructionName.ES, null, cctx);
////			LOG.info("702 "+loop.getType());
//			switch (loop.getType()) {
//				case FROM_TO_TYPE:
//					generate_loop_FROM_TO_TYPE(loop, gf, cctx);
//					break;
//				case TO_TYPE:
//					break;
//				case EXPR_TYPE:
//					generate_loop_EXPR_TYPE(loop, gf, cctx);
//					break;
//				case ITER_TYPE:
//					break;
//				case WHILE:
//					break;
//				case DO_WHILE:
//					break;
//			}
//			final int x2 = add_i(gf, InstructionName.XS, List_of(new IntegerIA(e2, gf)), cctx);
//			final Range r = new Range(e2, x2);
//			gf.addContext(loop.getContext(), r);
//		}
//
//		private void generate_loop_FROM_TO_TYPE(@NotNull final Loop loop, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//			final IdentExpression iterNameToken = loop.getIterNameToken();
//			final String iterName = iterNameToken.getText();
//			final int iter_temp = addTempTableEntry(null, iterNameToken, gf, iterNameToken); // TODO deduce later
//			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(iter_temp, gf)), cctx);
//			final InstructionArgument ia1 = simplify_expression(loop.getFromPart(), gf, cctx);
//			if (ia1 instanceof ConstTableIA)
//				add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(iter_temp, gf), ia1), cctx);
//			else
//				add_i(gf, InstructionName.AGN, List_of(new IntegerIA(iter_temp, gf), ia1), cctx);
//			final Label label_top = gf.addLabel("top", true);
//			gf.place(label_top);
//			final Label label_bottom = gf.addLabel("bottom"+label_top.getIndex(), false);
//			add_i(gf, InstructionName.JE, List_of(new IntegerIA(iter_temp, gf), simplify_expression(loop.getToPart(), gf, cctx), label_bottom), cctx);
//			for (final StatementItem statementItem : loop.getItems()) {
//				LOG.info("705 "+statementItem);
//				generate_item((OS_Element)statementItem, gf, cctx);
//			}
//			final IdentExpression pre_inc_name = Helpers.string_to_ident("__preinc__");
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, pre_inc_name);
//			final int pre_inc = addProcTableEntry(pre_inc_name, null, List_of(tte/*getType(left), getType(right)*/), gf);
//			add_i(gf, InstructionName.CALLS, List_of(new ProcIA(pre_inc, gf), new IntegerIA(iter_temp, gf)), cctx);
//			add_i(gf, InstructionName.JMP, List_of(label_top), cctx);
//			gf.place(label_bottom);
//		}
//
//		private void generate_loop_EXPR_TYPE(@NotNull final Loop loop, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//			final int loop_iterator = addTempTableEntry(null, gf); // TODO deduce later
//			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(loop_iterator, gf)), cctx);
//			final int i2 = addConstantTableEntry("", new NumericExpression(0), new OS_Type(BuiltInTypes.SystemInteger), gf);
//			final InstructionArgument ia1 = new ConstTableIA(i2, gf);
////			if (ia1 instanceof ConstTableIA)
//			add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(loop_iterator, gf), ia1), cctx);
////			else
////				add_i(gf, InstructionName.AGN, List_of(new IntegerIA(loop_iterator), ia1), cctx);
//			final Label label_top = gf.addLabel("top", true);
//			gf.place(label_top);
//			final Label label_bottom = gf.addLabel("bottom"+label_top.getIndex(), false);
//			add_i(gf, InstructionName.JE, List_of(new IntegerIA(loop_iterator, gf), simplify_expression(loop.getToPart(), gf, cctx), label_bottom), cctx);
//			for (final StatementItem statementItem : loop.getItems()) {
//				LOG.info("707 "+statementItem);
//				generate_item((OS_Element)statementItem, gf, cctx);
//			}
//			final String txt = SpecialFunctions.of(ExpressionKind.INCREMENT);
//			final IdentExpression pre_inc_name = Helpers.string_to_ident(txt);
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, pre_inc_name);
//			final int pre_inc = addProcTableEntry(pre_inc_name, null, List_of(tte), gf);
//			add_i(gf, InstructionName.CALLS, List_of(new ProcIA(pre_inc, gf), new IntegerIA(loop_iterator, gf)), cctx);
//			add_i(gf, InstructionName.JMP, List_of(label_top), cctx);
//			gf.place(label_bottom);
//		}
//
//		private void generate_variable_sequence(final VariableSequence item, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//			for (final VariableStatement vs : item.items()) {
//				int state = 0;
////				LOG.info("8004 " + vs);
//				final String variable_name = vs.getName();
//				final IExpression initialValue = vs.initialValue();
//				//
//				if (vs.getTypeModifiers() == TypeModifiers.CONST) {
//					if (initialValue.is_simple()) {
//						if (initialValue instanceof IdentExpression) {
//							state = 4;
//						} else {
//							state = 1;
//						}
//					} else {
//						state = 2;
//					}
//				} else {
//					state = 3;
//				}
////				final OS_Type type = vs.initialValue().getType();
////				final String stype = type == null ? "Unknown" : getTypeString(type);
////				LOG.info("8004-1 " + type);
////				LOG.info(String.format("8004-2 %s %s;", stype, vs.getName()));
//				switch (state) {
//					case 1:
//					{
//						final int ci = addConstantTableEntry(variable_name, initialValue, initialValue.getType(), gf);
//						final int vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
//						final IExpression iv = initialValue;
//						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("const"), new IntegerIA(vte_num, gf)), cctx);
//						add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(vte_num, gf), new ConstTableIA(ci, gf)), cctx);
//					}
//					break;
//					case 2:
//					{
//						final int vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
//						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("val"), new IntegerIA(vte_num, gf)), cctx);
//						final IExpression iv = initialValue;
//						assign_variable(gf, vte_num, iv, cctx);
//					}
//					break;
//					case 3:
//					{
//						final TypeTableEntry tte;
//						if (initialValue == IExpression.UNASSIGNED && vs.typeName() != null) {
//							tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, new OS_Type(vs.typeName()), vs.getNameToken());
//						} else {
//							tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, initialValue.getType(), vs.getNameToken());
//						}
//						final int vte_num = addVariableTableEntry(variable_name, tte, gf, vs); // TODO why not vs.initialValue ??
//						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("var"), new IntegerIA(vte_num, gf)), cctx);
//						final IExpression iv = initialValue;
//						assign_variable(gf, vte_num, iv, cctx);
//					}
//					break;
//					case 4:
//					{
//						final int vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
//						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("const"), new IntegerIA(vte_num, gf)), cctx);
//						assign_variable(gf, vte_num, initialValue, cctx);
//					}
//					break;
//					default:
//						throw new IllegalStateException();
//				}
//			}
//		}
//
//		private void generate_statement_wrapper(final StatementWrapper aStatementWrapper,
//		                                        final IExpression x,
//		                                        final ExpressionKind expressionKind,
//		                                        @NotNull final BaseGeneratedFunction gf,
//		                                        final Context cctx) {
////			LOG.err("106-1 "+x.getKind()+" "+x);
//			if (x.is_simple()) {
////				int i = addTempTableEntry(x.getType(), gf);
//				switch (expressionKind) {
//					case ASSIGNMENT:
////					LOG.err(String.format("703.2 %s %s", x.getLeft(), ((BasicBinaryExpression)x).getRight()));
//						generate_item_assignment(aStatementWrapper, x, gf, cctx);
//						break;
//					case AUG_MULT:
//					{
//						LOG.info(String.format("801.1 %s %s %s", expressionKind, x.getLeft(), ((BasicBinaryExpression) x).getRight()));
////						BasicBinaryExpression bbe = (BasicBinaryExpression) x;
////						final IExpression right1 = bbe.getRight();
//						final InstructionArgument left = simplify_expression(x.getLeft(), gf, cctx);
//						final InstructionArgument right = simplify_expression(((BasicBinaryExpression) x).getRight(), gf, cctx);
//						final IdentExpression fn_aug_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
//						final List<TypeTableEntry> argument_types = List_of(gf.getVarTableEntry(to_int(left)).type, gf.getVarTableEntry(to_int(right)).type);
////						LOG.info("801.2 "+argument_types);
//						final int fn_aug = addProcTableEntry(fn_aug_name, null, argument_types, gf);
//						final int i = add_i(gf, InstructionName.CALLS, List_of(new ProcIA(fn_aug, gf), left, right), cctx);
//						//
//						// SEE IF CALL SHOULD BE DEFERRED
//						//
//						for (final TypeTableEntry argument_type : argument_types) {
//							if (argument_type.getAttached() == null) {
//								// still dont know the argument types at this point, which creates a problem
//								// for resolving functions, so wait until later when more information is available
//								if (!gf.deferred_calls.contains(i))
//									gf.deferred_calls.add(i);
//								break;
//							}
//						}
//					}
//					break;
//					default:
//						throw new NotImplementedException();
//				}
//			} else {
//				switch (expressionKind) {
//					case ASSIGNMENT:
////					LOG.err(String.format("803.2 %s %s", x.getLeft(), ((BasicBinaryExpression)x).getRight()));
//						generate_item_assignment(aStatementWrapper, x, gf, cctx);
//						break;
////				case IS_A:
////					break;
//					case PROCEDURE_CALL:
//						final ProcedureCallExpression pce = (ProcedureCallExpression) x;
//						simplify_procedure_call(pce, gf, cctx);
//						break;
//					case DOT_EXP:
//						final DotExpression de = (DotExpression) x;
//						generate_item_dot_expression(null, de.getLeft(), de.getRight(), gf, cctx);
//						break;
//					default:
//						throw new IllegalStateException("Unexpected value: " + expressionKind);
//				}
//			}
//		}
//
//		public void generate_construct_statement(final ConstructStatement aConstructStatement, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//			final IExpression left = aConstructStatement.getExpr(); // TODO need type of this expr, not expr!!
//			final ExpressionList args = aConstructStatement.getArgs();
//			//
//			InstructionArgument expression_num = simplify_expression(left, gf, cctx);
//			if (expression_num == null) {
//				expression_num = gf.get_assignment_path(left, GenerateFunctions.this, cctx);
//			}
//			final int i = addProcTableEntry(left, expression_num, get_args_types(args, gf, cctx), gf);
//			final List<InstructionArgument> l = new ArrayList<InstructionArgument>();
//			l.add(new ProcIA(i, gf));
//			l.addAll(simplify_args(args, gf, cctx));
//			add_i(gf, InstructionName.CONSTRUCT, l, cctx);
//		}
//	}
//
//	private void generate_item(final OS_Element item, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//		final Generate_Item gi = new Generate_Item();
//		if (item instanceof AliasStatement) {
//			gi.generate_alias_statement((AliasStatement) item);
//		} else if (item instanceof CaseConditional) {
//			gi.generate_case_conditional((CaseConditional) item);
//		} else if (item instanceof ClassStatement) {
//			// TODO this still has no ClassInvocation
//			final GeneratedClass gc = generateClass((ClassStatement) item);
//			final int ite_index = gf.addIdentTableEntry(((ClassStatement) item).getNameNode(), cctx);
//			final IdentTableEntry ite = gf.getIdentTableEntry(ite_index);
//			ite.resolveTypeToClass(gc);
//		} else if (item instanceof StatementWrapper) {
//			final StatementWrapper sw = (StatementWrapper) item;
//			final IExpression x = sw.getExpr();
//			final ExpressionKind expressionKind = x.getKind();
//			gi.generate_statement_wrapper(sw, x, expressionKind, gf, cctx);
//		} else if (item instanceof IfConditional) {
//			gi.generate_if((IfConditional)item, gf);
//		} else if (item instanceof Loop) {
//			LOG.err("800 -- generateLoop");
//			gi.generate_loop((Loop) item, gf);
//		} else if (item instanceof MatchConditional) {
//			gi.generate_match_conditional((MatchConditional) item, gf);
//		} else if (item instanceof NamespaceStatement) {
////			LOG.info("Skip namespace for now "+((NamespaceStatement) item).name());
//			throw new NotImplementedException();
//		} else if (item instanceof VariableSequence) {
//			gi.generate_variable_sequence((VariableSequence) item, gf, cctx);
//		} else if (item instanceof WithStatement) {
//			throw new NotImplementedException();
//		} else if (item instanceof SyntacticBlock) {
//			throw new NotImplementedException();
//		} else if (item instanceof ConstructStatement) {
//			final ConstructStatement constructStatement = (ConstructStatement) item;
//			gi.generate_construct_statement(constructStatement, gf, cctx);
//		} else {
//			throw new IllegalStateException("cant be here");
//		}
//	}
//
//	public @NotNull GeneratedClass generateClass(@NotNull final ClassStatement klass) {
//		final GeneratedClass gc = new GeneratedClass(klass, module);
//		AccessNotation an = null;
//
//		for (final ClassItem item : klass.getItems()) {
//			if (item instanceof AliasStatement) {
//				LOG.info("Skip alias statement for now");
////				throw new NotImplementedException();
//			} else if (item instanceof ClassStatement) {
////				final ClassStatement classStatement = (ClassStatement) item;
////				@NotNull GeneratedClass gen_c = generateClass(classStatement);
////				gc.addClass(classStatement, gen_c);
//			} else if (item instanceof ConstructorDef) {
////				final ConstructorDef constructorDef = (ConstructorDef) item;
////				@NotNull GeneratedConstructor f = generateConstructor(constructorDef, klass, null); // TODO remove this null
////				gc.addConstructor(constructorDef, f);
//			} else if (item instanceof DestructorDef) {
//				throw new NotImplementedException();
//			} else if (item instanceof DefFunctionDef) {
////				@NotNull GeneratedFunction f = generateFunction((DefFunctionDef) item, klass);
////				gc.addFunction((DefFunctionDef) item, f);
//			} else if (item instanceof FunctionDef) {
//				// README handled in WlGenerateFunction
////				@NotNull GeneratedFunction f = generateFunction((FunctionDef) item, klass);
////				gc.addFunction((FunctionDef) item, f);
//			} else if (item instanceof NamespaceStatement) {
//				throw new NotImplementedException();
//			} else if (item instanceof VariableSequence) {
//				final VariableSequence vsq = (VariableSequence) item;
//				for (final VariableStatement vs : vsq.items()) {
////					LOG.info("6999 "+vs);
//					gc.addVarTableEntry(an, vs);
//				}
//			} else if (item instanceof AccessNotation) {
//				//
//				// TODO two AccessNotation's can be active at once, for example if the first
//				//  one defined only classes and the second one defined only a category
//				//
//				an = (AccessNotation) item;
////				gc.addAccessNotation(an);
//			} else if (item instanceof PropertyStatement) {
//				final @NotNull PropertyStatement ps = (PropertyStatement) item;
//				LOG.err("307 Skipping property for now");
//			} else {
//				LOG.err("305 "+item.getClass().getName());
//				throw new NotImplementedException();
//			}
//		}
//
//		gc.createCtor0();
//
////		klass._a.setCode(nextClassCode());
//
//		return gc;
//	}
//
//	@NotNull public GeneratedNamespace generateNamespace(final NamespaceStatement namespace1) {
//		final GeneratedNamespace gn = new GeneratedNamespace(namespace1, module);
//		AccessNotation an = null;
//
//		for (final ClassItem item : namespace1.getItems()) {
//			if (item instanceof AliasStatement) {
//				LOG.err("328 Skip AliasStatement for now");
////				throw new NotImplementedException();
//			} else if (item instanceof ClassStatement) {
//				throw new NotImplementedException();
//			} else if (item instanceof ConstructorDef) {
//				throw new NotImplementedException();
//			} else if (item instanceof DestructorDef) {
//				throw new NotImplementedException();
//			} else if (item instanceof FunctionDef) {
//				//throw new NotImplementedException();
////				@NotNull GeneratedFunction f = generateFunction((FunctionDef) item, namespace1);
////				gn.addFunction((FunctionDef) item, f);
//			} else if (item instanceof DefFunctionDef) {
//				throw new NotImplementedException();
//			} else if (item instanceof NamespaceStatement) {
//				throw new NotImplementedException();
//			} else if (item instanceof VariableSequence) {
//				final VariableSequence vsq = (VariableSequence) item;
//				for (final VariableStatement vs : vsq.items()) {
////					LOG.info("6999 "+vs);
//					gn.addVarTableEntry(an, vs);
//				}
//			} else if (item instanceof AccessNotation) {
//				//
//				// TODO two AccessNotation's can be active at once, for example if the first
//				//  one defined only classes and the second one defined only a category
//				//
//				an = (AccessNotation) item;
////				gn.addAccessNotation(an);
//			} else
//				throw new NotImplementedException();
//		}
//
//		gn.createCtor0();
//
//		return gn;
//	}
//
////	public List<GeneratedNode> generateAllTopLevelClasses() {
////		List<GeneratedNode> R = new ArrayList<GeneratedNode>();
////
////		generateAllTopLevelClasses(R);
////
////		return R;
////	}
//
////	public void generateFromEntryPoints(final EntryPointList epl, final DeducePhase deducePhase) {
////		epl.generate(this, deducePhase, () -> phase.wm);
////	}
//
//	public void generateAllTopLevelClasses(final List<GeneratedNode> lgc) {
//		for (final ModuleItem item : module.getItems()) {
//			if (item instanceof NamespaceStatement) {
//				final NamespaceStatement namespaceStatement = (NamespaceStatement) item;
//				final GeneratedNamespace ns = generateNamespace(namespaceStatement);
//				lgc.add(ns);
//			} else if (item instanceof ClassStatement) {
//				final ClassStatement classStatement = (ClassStatement) item;
//				@NotNull final GeneratedClass kl = generateClass(classStatement);
//				lgc.add(kl);
//			}
//			// TODO enums, datatypes, (type)aliases
//		}
//	}
//
//	class Generate_item_assignment {
//
//		public void procedure_call(final StatementWrapper aStatementWrapper, @NotNull final BaseGeneratedFunction gf, final BasicBinaryExpression bbe, final ProcedureCallExpression pce, final Context cctx) {
//			final IExpression left = bbe.getLeft();
//
//			final InstructionArgument lookup = simplify_expression(left, gf, cctx);
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, bbe.getType(), left);
//
//			if (lookup instanceof IntegerIA) {
//				// TODO should be AGNC
//				final int instruction_number = add_i(gf, InstructionName.AGN, List_of(lookup,
//				  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
//				final Instruction instruction = gf.getInstruction(instruction_number);
//				final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA)lookup).getIndex());
//				vte.addPotentialType(instruction.getIndex(), tte);
//			} else {
//				if (left instanceof IdentExpression) {
//					final String text = ((IdentExpression) left).getText();
//					final int vte_num;
//					if (aStatementWrapper instanceof WrappedStatementWrapper) {
//						vte_num = addVariableTableEntry(text, tte, gf, ((WrappedStatementWrapper) aStatementWrapper).getVariableStatement());
//					} else {
//						vte_num = addVariableTableEntry(text, tte, gf, (IdentExpression) left);
//					}
//					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(vte_num, gf)), cctx);
//					// TODO should be AGNC
//					final int instruction_number = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte_num, gf),
//					  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
//					final Instruction instruction = gf.getInstruction(instruction_number);
//					final VariableTableEntry vte = gf.getVarTableEntry(vte_num);
//					vte.addPotentialType(instruction.getIndex(), tte);
//				} else {
//					assert lookup instanceof IdentIA;
//
//					// TODO should be AGNC
//					final int instruction_number = add_i(gf, InstructionName.AGN, List_of(lookup,
//					  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
//					final Instruction instruction = gf.getInstruction(instruction_number);
//					@NotNull final IdentTableEntry ite = ((IdentIA) lookup).getEntry();
//					ite.addPotentialType(instruction.getIndex(), tte);
//				}
//			}
//		}
//
//		public void ident(@NotNull final BaseGeneratedFunction gf, final IdentExpression left, final IdentExpression right, final Context cctx) {
//			final InstructionArgument vte_left = gf.vte_lookup(left.getText());
//			final int ident_left;
//			final int ident_right;
//			final InstructionArgument some_left;
//			if (vte_left == null) {
//				ident_left = gf.addIdentTableEntry(left, cctx);
//				some_left = new IdentIA(ident_left, gf);
//			} else
//				some_left = vte_left;
//			final InstructionArgument vte_right = gf.vte_lookup(right.getText());
//			final int inst;
//			if (vte_right == null) {
//				ident_right = gf.addIdentTableEntry(right, cctx);
//				inst = add_i(gf, InstructionName.AGN, List_of(some_left, new IdentIA(ident_right, gf)), cctx);
//			} else {
//				inst = add_i(gf, InstructionName.AGN, List_of(some_left, vte_right), cctx);
//
//				// TODO this will break one day
//				assert vte_left != null;
//				final VariableTableEntry vte = gf.getVarTableEntry(to_int(vte_left));
//				// ^^
//				vte.addPotentialType(inst, gf.getVarTableEntry(to_int(vte_right)).type);
//			}
//		}
//
//		public void numeric(@NotNull final BaseGeneratedFunction gf, final IExpression left, final NumericExpression ne, final Context cctx) {
//			@NotNull final InstructionArgument agn_path = gf.get_assignment_path(left, GenerateFunctions.this, cctx);
//			final int cte = addConstantTableEntry("", ne, ne.getType(), gf);
//
//			final int agn_inst = add_i(gf, InstructionName.AGN, List_of(agn_path, new ConstTableIA(cte, gf)), cctx);
//			// TODO what now??
//		}
//
//		public void mathematical(@NotNull final BaseGeneratedFunction gf, final IExpression left, final ExpressionKind kind, final IExpression right1, final Context cctx) {
//			// TODO doesn't use kind
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, right1.getType(), right1);
//
//			final InstructionArgument left_ia = simplify_expression(left, gf, cctx);
//			final InstructionArgument right_ia = simplify_expression(right1, gf, cctx);
//
//			final int instruction_number = add_i(gf, InstructionName.AGN, List_of(left_ia, right_ia), cctx);
//			final Instruction instruction = gf.getInstruction(instruction_number);
//			if (left_ia instanceof IntegerIA) {
//				// Assuming this points to a variable and not ie a function
//				final VariableTableEntry vte = gf.getVarTableEntry(to_int(left_ia));
////				vte.type = tte;
//				vte.addPotentialType(instruction.getIndex(), tte);
//			} else if (left_ia instanceof IdentIA) {
//				final IdentTableEntry idte = gf.getIdentTableEntry(to_int(left_ia));
////				idte.type = tte;
//				idte.addPotentialType(instruction.getIndex(), tte);
//			}
//		}
//
//		public void string_literal(@NotNull final BaseGeneratedFunction gf, final IExpression left, final StringExpression right, final Context aContext) {
//			@NotNull final InstructionArgument agn_path = gf.get_assignment_path(left, GenerateFunctions.this, aContext);
//			final int cte = addConstantTableEntry("", right, new OS_Type(BuiltInTypes.String_)/*right.getType()*/, gf);
//
//			final int agn_inst = add_i(gf, InstructionName.AGN, List_of(agn_path, new ConstTableIA(cte, gf)), aContext);
//			// TODO what now??
//		}
//
//		public void neg(@NotNull final BaseGeneratedFunction gf, final IExpression left, final ExpressionKind aKind, final IExpression right1, final Context cctx) {
//			// TODO doesn't use kind
//			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, right1.getType(), right1);
//
//			final InstructionArgument left_ia = simplify_expression(left, gf, cctx);
//			final InstructionArgument right_ia = simplify_expression(right1, gf, cctx);
//
//			final int instruction_number = add_i(gf, InstructionName.AGN, List_of(left_ia, right_ia), cctx);
//			final Instruction instruction = gf.getInstruction(instruction_number);
//			if (left_ia instanceof IntegerIA) {
//				// Assuming this points to a variable and not ie a function
//				final VariableTableEntry vte = gf.getVarTableEntry(to_int(left_ia));
////				vte.type = tte;
//				vte.addPotentialType(instruction.getIndex(), tte);
//			} else if (left_ia instanceof IdentIA) {
//				final IdentTableEntry idte = gf.getIdentTableEntry(to_int(left_ia));
////				idte.type = tte;
//				idte.addPotentialType(instruction.getIndex(), tte);
//			}
//		}
//	}
//
//	private void generate_item_assignment(final StatementWrapper aStatementWrapper, @NotNull final IExpression x, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
////		LOG.err(String.format("801 %s %s", x.getLeft(), ((BasicBinaryExpression) x).getRight()));
//		final BasicBinaryExpression bbe = (BasicBinaryExpression) x;
//		final IExpression right1 = bbe.getRight();
//		final Generate_item_assignment gia = new Generate_item_assignment();
//		switch (right1.getKind()) {
//			case PROCEDURE_CALL:
//				gia.procedure_call(aStatementWrapper, gf, bbe, (ProcedureCallExpression) right1, cctx);
//				break;
//			case IDENT:
//				gia.ident(gf, (IdentExpression) bbe.getLeft(), (IdentExpression) right1, cctx);
//				break;
//			case NUMERIC:
//				gia.numeric(gf, bbe.getLeft(), (NumericExpression) right1, cctx);
//				break;
//			case STRING_LITERAL:
//				gia.string_literal(gf, bbe.getLeft(), (StringExpression) right1, cctx);
//				break;
//			case ADDITION:
//			case MULTIPLY:
//			case GE:
//			case GT:
//				gia.mathematical(gf, bbe.getLeft(), right1.getKind(), right1, cctx);
//				break;
//			case NEG:
//				gia.neg(gf, bbe.getLeft(), right1.getKind(), right1, cctx);
//				break;
//			default:
//				LOG.err("right1.getKind(): "+right1.getKind());
//				throw new NotImplementedException();
//		}
//	}
//
//	private void generate_item_dot_expression(@org.jetbrains.annotations.Nullable final InstructionArgument backlink,
//	                                          final IExpression left,
//	                                          @NotNull final IExpression right,
//	                                          final @NotNull BaseGeneratedFunction gf,
//	                                          final Context cctx) {
//		final int y=2;
//		final int x = gf.addIdentTableEntry((IdentExpression) left, cctx);
//		if (backlink != null) {
//			final IdentIA identIA = new IdentIA(x, gf);
//			identIA.setPrev(backlink);
////			gf.getIdentTableEntry(x).addStatusListener(new DeduceTypes2.FoundParent());
////			gf.getIdentTableEntry(x).backlink = backlink;
//		}
//		if (right.getLeft() == right)
//			return;
//		//
//		if (right instanceof IdentExpression)
//			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), ((IdentExpression)right), gf, cctx);
//		else
//			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), ((BasicBinaryExpression)right).getRight(), gf, cctx);
//	}
//
//	private void assign_variable(final @NotNull BaseGeneratedFunction gf, final int vte, @NotNull final IExpression value, final Context cctx) {
//		if (value == IExpression.UNASSIGNED) return; // default_expression
//		switch (value.getKind()) {
//			case PROCEDURE_CALL:
//				final ProcedureCallExpression pce = (ProcedureCallExpression) value;
//				final FnCallArgs fnCallArgs = new FnCallArgs(expression_to_call(pce, gf, cctx), gf);
//				final int ii2 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), fnCallArgs), cctx);
//				final VariableTableEntry vte_proccall = gf.getVarTableEntry(vte);
//				final InstructionArgument gg = fnCallArgs.expression_to_call.getArg(0);
//				@NotNull final TableEntryIV g;
//				if (gg instanceof IntegerIA) {
//					g = gf.getVarTableEntry(((IntegerIA) gg).getIndex());
//				} else if (gg instanceof IdentIA) {
//					g = gf.getIdentTableEntry(((IdentIA) gg).getIndex());
//				} else if (gg instanceof ProcIA) {
//					g = gf.getProcTableEntry(((ProcIA) gg).getIndex());
//				} else
//					throw new NotImplementedException();
//				final TypeTableEntry tte_proccall = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value, g);
//				fnCallArgs.setType(tte_proccall);
//				vte_proccall.addPotentialType(ii2, tte_proccall);
//				break;
//			case NUMERIC:
//				final int ci = addConstantTableEntry(null, value, value.getType(), gf);
//				final int ii = add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(vte, gf), new ConstTableIA(ci, gf)), cctx);
//				final VariableTableEntry vte_numeric = gf.getVarTableEntry(vte);
//				vte_numeric.addPotentialType(ii, gf.getConstTableEntry(ci).type);
//				break;
//			case IDENT:
//				final InstructionArgument ia1 = simplify_expression(value, gf, cctx);
//				final int ii3 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), ia1), cctx);
//				final VariableTableEntry vte3_ident = gf.getVarTableEntry(vte);
//				final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value);
//				vte3_ident.addPotentialType(ii3, tte);
//				break;
//			case FUNC_EXPR:
//				final FuncExpr fe = (FuncExpr) value;
//				final int pte_index = addProcTableEntry(fe, null, get_args_types(fe.getArgs(), gf), gf);
//				final int ii4 = add_i(gf, InstructionName.AGNF, List_of(new IntegerIA(vte, gf), new IntegerIA(pte_index, gf)), cctx);
//				final VariableTableEntry vte3_func = gf.getVarTableEntry(vte);
//				final TypeTableEntry tte_func = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_FuncExprType(fe), value);
//				vte3_func.addPotentialType(ii4, tte_func);
//				break;
//			default:
//				throw new IllegalStateException("Unexpected value: " + value.getKind());
//		}
//	}
//
//	private TypeTableEntry getType(@NotNull final IExpression arg, final BaseGeneratedFunction gf) {
//		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, arg.getType(), arg);
//		return tte;
//	}
//
//	private void simplify_procedure_call(@NotNull final ProcedureCallExpression pce, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		final IExpression left = pce.getLeft();
//		final ExpressionList args = pce.getArgs();
//		//
//		InstructionArgument expression_num = simplify_expression(left, gf, cctx);
//		if (expression_num == null) {
//			expression_num = gf.get_assignment_path(left, this, cctx);
//		}
//		final int i = addProcTableEntry(left, expression_num, get_args_types(args, gf, cctx), gf);
//		final List<InstructionArgument> l = new ArrayList<InstructionArgument>();
//		l.add(new ProcIA(i, gf));
//		l.addAll(simplify_args(args, gf, cctx));
//		final int instructionIndex = add_i(gf, InstructionName.CALL, l, cctx);
//		{
//			@NotNull final ProcTableEntry pte = gf.getProcTableEntry(i);
//			if (expression_num instanceof IdentIA) {
//				@NotNull final IdentTableEntry idte = ((IdentIA) expression_num).getEntry();
//				idte.setCallablePTE(pte);
//				pte.typePromise().then(new DoneCallback<GenType>() { // TODO should this be done here?
//					@Override
//					public void onDone(final GenType result) {
//						@NotNull final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, result.resolved);
//						tte.genType.copy(result);
//						idte.addPotentialType(instructionIndex, tte);
//					}
//				});
//			}
//		}
//	}
//
//	private @NotNull List<InstructionArgument> simplify_args(@org.jetbrains.annotations.Nullable final ExpressionList args, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		final List<InstructionArgument> R = new ArrayList<InstructionArgument>();
//		if (args == null) return R;
//		//
//		for (final IExpression expression : args) {
//			final InstructionArgument ia = simplify_expression(expression, gf, cctx);
//			if (ia != null) {
////				LOG.err("109 "+expression);
//				R.add(ia);
//			} else {
//				LOG.err("109-0 error expr not found "+expression);
//			}
//		}
//		return R;
//	}
//
//	private @NotNull Collection<InstructionArgument> simplify_args2(@org.jetbrains.annotations.Nullable final ExpressionList args, @NotNull final GeneratedFunction gf, final Context cctx) {
//		Collection<InstructionArgument> R = new ArrayList<InstructionArgument>();
//		if (args == null) return R;
//		//
//		R = Collections2.transform(args.expressions(), new Function<IExpression, InstructionArgument>() {
//			@Override
//			public @Nullable InstructionArgument apply(@Nullable final IExpression input) {
//				assert input != null;
//				@NotNull final IExpression expression = input;
//				final InstructionArgument ia = simplify_expression(expression, gf, cctx);
//				if (ia != null) {
//					LOG.err("109-1 "+expression);
//				} else {
//					LOG.err("109-01 error expr not found "+expression);
//				}
//				return ia;
//			}
//		});
//		return R;
//	}
//
////	public int addProcTableEntry(final IExpression expression, final InstructionArgument expression_num, final List<TypeTableEntry> args, final BaseGeneratedFunction gf) {
////		final ProcTableEntry pte = new ProcTableEntry(gf.prte_list.size(), expression, expression_num, args);
////		gf.prte_list.add(pte);
////		return pte.index;
////	}
//
//	InstructionArgument simplify_expression(@NotNull final IExpression expression, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		final ExpressionKind expressionKind = expression.getKind();
//		switch (expressionKind) {
//			case PROCEDURE_CALL:
//				return simplify_expression_procedure_call(expression, gf, cctx);
//			case CAST_TO:
//			{
//				final TypeCastExpression tce = (TypeCastExpression) expression;
//				final InstructionArgument simp = simplify_expression(tce.getLeft(), gf, cctx);
//				@NotNull final TypeTableEntry tte_index = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_Type(tce.getTypeName()));
//				final int x = add_i(gf, InstructionName.CAST_TO, List_of(simp, new IntegerIA(tte_index.getIndex(), gf)), cctx);
//			}
//			case AS_CAST:
//			{
//				final TypeCastExpression tce = (TypeCastExpression) expression;
//				final InstructionArgument simp = simplify_expression(tce.getLeft(), gf, cctx);
//				@NotNull final TypeTableEntry tte_index = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_Type(tce.getTypeName()));
//				final int x = add_i(gf, InstructionName.AS_CAST, List_of(simp, new IntegerIA(tte_index.getIndex(), gf)), cctx);
//			}
//			case DOT_EXP:
//			{
//				final DotExpression de = (DotExpression) expression;
//				return gf.get_assignment_path(de, this, cctx);
//			}
//			case QIDENT:
//			{
//				final Qualident q = (Qualident) expression;
//				final IExpression de = Helpers.qualidentToDotExpression2(q);
//				return gf.get_assignment_path(de, this, cctx);
//			}
//			case IDENT:
//				final String text = ((IdentExpression) expression).getText();
//				InstructionArgument i = gf.vte_lookup(text);
//				if (i == null) {
//					final IdentTableEntry x = gf.getIdentTableEntryFor(expression);
//					if (x == null) {
//						final int ii = gf.addIdentTableEntry((IdentExpression) expression, cctx);
//						i = new IdentIA(ii, gf);
//					} else {
//						i = new IdentIA(x.getIndex(), gf);
//					}
//				}
//				return i;
//			case NUMERIC:
//			{
//				final NumericExpression ne = (NumericExpression) expression;
//				final int ii = addConstantTableEntry2(null, ne, ne.getType(), gf);
//				return new ConstTableIA(ii, gf);
//			}
//			case STRING_LITERAL:
//			{
//				final StringExpression se = (StringExpression) expression;
//				final int ii = addConstantTableEntry2(null, se, se.getType(), gf);
//				return new ConstTableIA(ii, gf);
//			}
//			case CHAR_LITERAL:
//			{
//				final CharLitExpression cle = (CharLitExpression) expression;
//				final int ii = addConstantTableEntry2(null, cle, cle.getType(), gf);
//				return new ConstTableIA(ii, gf);
//			}
//			case GET_ITEM:
//			{
//				final @NotNull GetItemExpression gie = (GetItemExpression) expression;
//				final IExpression left = gie.getLeft();
//				final IExpression right = gie.index;
//				final InstructionArgument left_instruction;
//				final InstructionArgument right_instruction;
//				if (left.is_simple()) {
//					if (left instanceof IdentExpression) {
//						left_instruction = simplify_expression(left, gf, cctx);
//					} else {
//						// a constant
//						assert IExpression.isConstant(right);
//						final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
//						left_instruction = new ConstTableIA(left_constant_num, gf);
//					}
//				} else {
//					// create a tmp var
//					left_instruction = simplify_expression(left, gf, cctx);
//				}
//				if (right.is_simple()) {
//					if (right instanceof IdentExpression) {
//						right_instruction = simplify_expression(right, gf, cctx);
//					} else {
//						// a constant
//						assert IExpression.isConstant(right);
//						final int right_constant_num = addConstantTableEntry2(null, right, right.getType(), gf);
//						right_instruction = new ConstTableIA(right_constant_num, gf);
//					}
//				} else {
//					// create a tmp var
//					right_instruction = simplify_expression(right, gf, cctx);
//				}
//				{
//					// create a call
//					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
//					//					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
//					final TypeTableEntry tte_left = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
//					final TypeTableEntry tte_right = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, right);
//					final int pte = addProcTableEntry(expr_kind_name, null, List_of(tte_left, tte_right), gf);
//					final int tmp = addTempTableEntry(new OS_Type(BuiltInTypes.Boolean), gf); // README should be Boolean
//					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
//					final Instruction inst = new Instruction();
//					inst.setName(InstructionName.CALLS);
//					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction, right_instruction));
//					final FnCallArgs fca = new FnCallArgs(inst, gf);
//					final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
//					return new IntegerIA(tmp, gf);
//				}
//			}
//			case SUBEXPRESSION:
//			{
//				final SubExpression subexpression = (SubExpression) expression;
//				final IExpression exp = subexpression.getExpression();
//
//				final InstructionArgument ia = simplify_expression(exp, gf, cctx);
//				return ia;
//			}
//			case LT_: case GT: case GE:
//			case ADDITION: case MULTIPLY: // TODO all BinaryExpressions go here
//			case SUBTRACTION: case DIVIDE:
//			case NOT_EQUAL: case EQUAL:
//			case MODULO:
//			{
//				final BasicBinaryExpression bbe = (BasicBinaryExpression) expression;
//				final IExpression left = bbe.getLeft();
//				final IExpression right = bbe.getRight();
//				final InstructionArgument left_instruction;
//				final InstructionArgument right_instruction;
//				if (left.is_simple()) {
//					if (left instanceof IdentExpression) {
//						left_instruction = simplify_expression(left, gf, cctx);
//					} else {
//						// a constant
//						assert IExpression.isConstant(right);
//						final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
//						left_instruction = new ConstTableIA(left_constant_num, gf);
//					}
//				} else {
//					// create a tmp var
//					left_instruction = simplify_expression(left, gf, cctx);
//				}
//				if (right.is_simple()) {
//					if (right instanceof IdentExpression) {
//						right_instruction = simplify_expression(right, gf, cctx);
//					} else {
//						// a constant
//						if (IExpression.isConstant(right)) {
//							final int right_constant_num = addConstantTableEntry2(null, right, right.getType(), gf);
//							right_instruction = new ConstTableIA(right_constant_num, gf);
//						} else {
//							right_instruction = simplify_expression(right, gf, cctx);
//						}
//					}
//				} else {
//					// create a tmp var
//					right_instruction = simplify_expression(right, gf, cctx);
//				}
//				{
//					// create a call
//					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
////					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
//					final TypeTableEntry tte_left  = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
//					final TypeTableEntry tte_right = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, right);
//					final int pte = addProcTableEntry(expr_kind_name, null, List_of(tte_left, tte_right), gf);
//					final int tmp = addTempTableEntry(new OS_Type(BuiltInTypes.Boolean), gf);
//					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
//					final Instruction inst = new Instruction();
//					inst.setName(InstructionName.CALLS);
//					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction, right_instruction));
//					final FnCallArgs fca = new FnCallArgs(inst, gf);
//					// TODO should be AGNC
//					final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
//					return new IntegerIA(tmp, gf); // TODO  is this right?? we want to return the variable, not proc calls, right?
//				}
////				throw new NotImplementedException();
//			}
//			case NEG:
//			{
//				final UnaryExpression bbe = (UnaryExpression) expression;
//				final IExpression left = bbe.getLeft();
//				final InstructionArgument left_instruction;
//				if (left.is_simple()) {
//					if (left instanceof IdentExpression) {
//						left_instruction = simplify_expression(left, gf, cctx);
//					} else {
//						// a constant
//						final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
//						left_instruction = new ConstTableIA(left_constant_num, gf);
//					}
//				} else {
//					// create a tmp var
//					left_instruction = simplify_expression(left, gf, cctx);
//				}
//				{
//					// create a call
//					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
////					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
//					final TypeTableEntry tte_left  = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
//					final int pte = addProcTableEntry(expr_kind_name, null, List_of(tte_left), gf);
//					final int tmp = addTempTableEntry(new OS_Type(BuiltInTypes.Boolean), gf);
//					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
//					final Instruction inst = new Instruction();
//					inst.setName(InstructionName.CALLS);
//					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction));
//					final FnCallArgs fca = new FnCallArgs(inst, gf);
//					// TODO should be AGNC
//					final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
//					return new IntegerIA(tmp, gf);
//				}
//			}
////			break;
//			case ASSIGNMENT:
//			{
//				final InstructionArgument s1 = simplify_expression(expression.getLeft(), gf, cctx);
//				final InstructionArgument s2 = simplify_expression(((BasicBinaryExpression)expression).getRight(), gf, cctx);
//				final int x = add_i(gf, InstructionName.AGN, List_of(s1, s2), cctx);
//				return s1; // TODO is this right?
//			}
//			default:
//				throw new IllegalStateException("Unexpected value: " + expressionKind);
//		}
////		return null;
//	}
//
//	@NotNull
//	private InstructionArgument simplify_expression_procedure_call(@NotNull final IExpression expression, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
//		final @NotNull ProcedureCallExpression pce = (ProcedureCallExpression) expression;
//		final IExpression    left = pce.getLeft();
//		final ExpressionList args = pce.getArgs();
//		final InstructionArgument left_ia;
//		final List<InstructionArgument> right_ia;
//		//
//		final int initialCapacity = args != null ? args.size() + 1 : 1;
//		right_ia = new ArrayList<InstructionArgument>(initialCapacity);
//		//
//		if (left.is_simple()) {
//			if (left instanceof IdentExpression) {
//				// for ident(xyz...)
//				final int x = gf.addIdentTableEntry((IdentExpression) left, cctx);
//				// TODO attach to var/const or lookup later in deduce
//				left_ia = new IdentIA(x, gf);
//			} else if (left instanceof SubExpression) {
//				// for (1).toString() etc
//				final SubExpression se = (SubExpression) left;
//				final InstructionArgument ia = simplify_expression(se.getExpression(), gf, cctx);
//				//return ia;  // TODO is this correct?
//				left_ia = ia;
//			} else {
//				// for "".strip() etc
//				assert IExpression.isConstant(left);
//				final int x = addConstantTableEntry(null, left, left.getType(), gf);
//				left_ia = new ConstTableIA(x, gf);
////						throw new IllegalStateException("Cant be here");
//			}
//		} else {
//			final InstructionArgument x = simplify_expression(left, gf, cctx);
//			left_ia = x;
//		}
//		final List<TypeTableEntry> args1 = new ArrayList<TypeTableEntry>();
//		if (args != null) {
//			for (final IExpression arg : args) {
//				final InstructionArgument ia;
//				final TypeTableEntry iat;
//				if (arg.is_simple()) {
//					final int y=2;
//					if (arg instanceof IdentExpression) {
//						final int x = gf.addIdentTableEntry((IdentExpression) arg, cctx);
//						// TODO attach to var/const or lookup later in deduce
//						ia = new IdentIA(x, gf);
//						iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, arg);
//					} else if (arg instanceof SubExpression) {
//						final SubExpression se = (SubExpression) arg;
//						ia = simplify_expression(se.getExpression(), gf, cctx);
//						iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, arg);
//					} else {
//						assert IExpression.isConstant(arg);
//						final int x = addConstantTableEntry(null, arg, arg.getType(), gf);
//						ia = new ConstTableIA(x, gf);
//						iat = gf.getConstTableEntry(x).type;
//					}
//				} else {
//					ia = simplify_expression(arg, gf, cctx);
//					iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null/*README wait to be deduced*/, arg);
//				}
//				right_ia.add(ia);
//				args1.add(iat);
//			}
//		}
//		final int pte = addProcTableEntry(expression, left_ia, args1, gf);
//		right_ia.add(0, new ProcIA(pte, gf));
//		{
//			final int tmp_var = addTempTableEntry(null, gf); // line 686 is here
//			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp_var, gf)), cctx);
//			final Instruction i = new Instruction();
//			i.setName(InstructionName.CALL);
//			i.setArgs(right_ia);
//			// TODO should be AGNC
//			final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp_var, gf), new FnCallArgs(i, gf)), cctx);
//			return new IntegerIA(tmp_var, gf); // return tmp_var instead of expression assigning it
//		}
//	}
//
//	private void generate_item_dot_expression(@org.jetbrains.annotations.Nullable final InstructionArgument backlink,
//	                                          final IExpression left,
//	                                          @NotNull final IExpression right,
//	                                          final @NotNull BaseGeneratedFunction gf,
//	                                          final Context cctx) {
//		final int y = 2;
//		final int x = gf.addIdentTableEntry((IdentExpression) left, cctx);
//		if (backlink != null) {
//			final IdentIA identIA = new IdentIA(x, gf);
//			identIA.setPrev(backlink);
////			gf.getIdentTableEntry(x).addStatusListener(new DeduceTypes2.FoundParent());
////			gf.getIdentTableEntry(x).backlink = backlink;
//		}
//		if (right.getLeft() == right)
//			return;
//		//
//		if (right instanceof IdentExpression)
//			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), right, gf, cctx);
//		else
//			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), ((BasicBinaryExpression) right).getRight(), gf, cctx);
//	}
//
////	private void assign_variable(final @NotNull BaseGeneratedFunction gf, final int vte, @NotNull final IExpression value, final Context cctx) {
////		if (value == IExpression.UNASSIGNED) return; // default_expression
////		switch (value.getKind()) {
////		case PROCEDURE_CALL:
////			final ProcedureCallExpression pce = (ProcedureCallExpression) value;
////			final FnCallArgs fnCallArgs = new FnCallArgs(expression_to_call(pce, gf, cctx), gf);
////			final int ii2 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), fnCallArgs), cctx);
////			final VariableTableEntry vte_proccall = gf.getVarTableEntry(vte);
////			final InstructionArgument gg = fnCallArgs.expression_to_call.getArg(0);
////			@NotNull final TableEntryIV g;
////			if (gg instanceof IntegerIA) {
////				g = gf.getVarTableEntry(((IntegerIA) gg).getIndex());
////			} else if (gg instanceof IdentIA) {
////				g = gf.getIdentTableEntry(((IdentIA) gg).getIndex());
////			} else if (gg instanceof ProcIA) {
////				g = gf.getProcTableEntry(((ProcIA) gg).getIndex());
////			} else
////				throw new NotImplementedException();
////			final TypeTableEntry tte_proccall = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value, g);
////			fnCallArgs.setType(tte_proccall);
////			vte_proccall.addPotentialType(ii2, tte_proccall);
////			break;
////		case NUMERIC:
////			final int ci = addConstantTableEntry(null, value, value.getType(), gf);
////			final int ii = add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(vte, gf), new ConstTableIA(ci, gf)), cctx);
////			final VariableTableEntry vte_numeric = gf.getVarTableEntry(vte);
////			vte_numeric.addPotentialType(ii, gf.getConstTableEntry(ci).type);
////			break;
////		case IDENT:
////			final InstructionArgument ia1 = simplify_expression(value, gf, cctx);
////			final int ii3 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), ia1), cctx);
////			final VariableTableEntry vte3_ident = gf.getVarTableEntry(vte);
////			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value);
////			vte3_ident.addPotentialType(ii3, tte);
////			break;
////		case FUNC_EXPR:
////			final FuncExpr fe = (FuncExpr) value;
////			final int pte_index = addProcTableEntry(fe, null, get_args_types(fe.getArgs(), gf), gf);
////			final int ii4 = add_i(gf, InstructionName.AGNF, List_of(new IntegerIA(vte, gf), new IntegerIA(pte_index, gf)), cctx);
////			final VariableTableEntry vte3_func = gf.getVarTableEntry(vte);
////			final TypeTableEntry tte_func = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_FuncExprType(fe), value);
////			vte3_func.addPotentialType(ii4, tte_func);
////			break;
////		default:
////			throw new IllegalStateException("Unexpected value: " + value.getKind());
////		}
////	}
//
//	private TypeTableEntry getType(@NotNull final IExpression arg, final BaseGeneratedFunction gf) {
//		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, arg.getType(), arg);
//		return tte;
//	}
//
//	private void simplify_procedure_call(@NotNull final ProcedureCallExpression pce, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		final IExpression left = pce.getLeft();
//		final ExpressionList args = pce.getArgs();
//		//
//		InstructionArgument expression_num = simplify_expression(left, gf, cctx);
//		if (expression_num == null) {
//			expression_num = gf.get_assignment_path(left, this, cctx);
//		}
//		final int i;
//
//		if (left instanceof ProcedureCallExpression)
//			i = addProcTableEntry(left, expression_num, get_args_types(args, gf, cctx), gf);
//		else
//			i = addProcTableEntry(pce, expression_num, get_args_types(args, gf, cctx), gf);
//
//		final List<InstructionArgument> l = new ArrayList<InstructionArgument>();
//		l.add(new ProcIA(i, gf));
//		l.addAll(simplify_args(args, gf, cctx));
//		final int instructionIndex = add_i(gf, InstructionName.CALL, l, cctx);
//		{
//			@NotNull final ProcTableEntry pte = gf.getProcTableEntry(i);
//			if (expression_num instanceof IdentIA) {
//				@NotNull final IdentTableEntry idte = ((IdentIA) expression_num).getEntry();
//				idte.setCallablePTE(pte);
//				pte.typePromise().then(new DoneCallback<GenType>() { // TODO should this be done here?
//					@Override
//					public void onDone(final GenType result) {
//						@NotNull final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, result.resolved);
//						tte.genType.copy(result);
//						idte.addPotentialType(instructionIndex, tte);
//					}
//				});
//			}
//		}
//	}
//
//	private @NotNull List<InstructionArgument> simplify_args(@org.jetbrains.annotations.Nullable final ExpressionList args, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		final List<InstructionArgument> R = new ArrayList<InstructionArgument>();
//		if (args == null) return R;
//		//
//		for (final IExpression expression : args) {
//			final InstructionArgument ia = simplify_expression(expression, gf, cctx);
//			if (ia != null) {
////				LOG.err("109 "+expression);
//				R.add(ia);
//			} else {
//				LOG.err("109-0 error expr not found " + expression);
//			}
//		}
//		return R;
//	}
//
//	private @NotNull Collection<InstructionArgument> simplify_args2(@org.jetbrains.annotations.Nullable final ExpressionList args, @NotNull final GeneratedFunction gf, final Context cctx) {
//		Collection<InstructionArgument> R = new ArrayList<InstructionArgument>();
//		if (args == null) return R;
//		//
//		R = Collections2.transform(args.expressions(), new Function<IExpression, InstructionArgument>() {
//			@Override
//			public @Nullable InstructionArgument apply(@Nullable final IExpression input) {
//				assert input != null;
//				@NotNull final IExpression expression = input;
//				final InstructionArgument ia = simplify_expression(expression, gf, cctx);
//				if (ia != null) {
//					LOG.err("109-1 " + expression);
//				} else {
//					LOG.err("109-01 error expr not found " + expression);
//				}
//				return ia;
//			}
//		});
//		return R;
//	}
//
//	public int addProcTableEntry(final IExpression expression, final InstructionArgument expression_num, final List<TypeTableEntry> args, final BaseGeneratedFunction gf) {
//		final ProcTableEntry pte = new ProcTableEntry(gf.prte_list.size(), expression, expression_num, args);
//		gf.prte_list.add(pte);
//		if (expression_num instanceof IdentIA) {
//			final IdentIA identIA = (IdentIA) expression_num;
//			if (identIA.getEntry().getCallablePTE() == null)
//				identIA.getEntry().setCallablePTE(pte);
//		}
//		return pte.index;
//	}
//
//	@NotNull
//	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
//	                                                 @NotNull final ProcedureCallExpression pce,
//	                                                 final @NotNull IExpression left,
//	                                                 final Context cctx) {
//		final Instruction i = new Instruction();
//		i.setName(InstructionName.CALL); // TODO see line 686
//		final List<InstructionArgument> li = new ArrayList<InstructionArgument>();
//		final InstructionArgument assignment_path = gf.get_assignment_path(left, this, cctx);
//		final List<TypeTableEntry> args_types = get_args_types(pce.getArgs(), gf, cctx);
//		final int pte_num = addProcTableEntry(left, assignment_path, args_types, gf);
//		li.add(new ProcIA(pte_num, gf));
//		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
//		li.addAll(args_);
//		i.setArgs(li);
//		return i;
//	}
//
//	@NotNull
//	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
//	                                                 @NotNull final ProcedureCallExpression pce,
//	                                                 final IExpression left,
//	                                                 final InstructionArgument left1,
//	                                                 final Context cctx) {
//		final Instruction i = new Instruction();
//		i.setName(InstructionName.CALL);
//		final @NotNull List<InstructionArgument> li = new ArrayList<InstructionArgument>();
//		final int pte_num = addProcTableEntry(left, left1, get_args_types(pce.getArgs(), gf, cctx), gf);
//		li.add(new ProcIA(pte_num, gf));
//		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
//		li.addAll(args_);
//		i.setArgs(li);
//		return i;
//	}
//
//	@NotNull List<TypeTableEntry> get_args_types(@org.jetbrains.annotations.Nullable final ExpressionList args,
//	                                             final BaseGeneratedFunction gf,
//	                                             @NotNull final Context aContext) {
//		final List<TypeTableEntry> R = new ArrayList<TypeTableEntry>();
//		if (args == null) return R;
//		//
//		for (final IExpression arg : args) {
//			final OS_Type type = arg.getType();
////			LOG.err(String.format("108 %s %s", arg, type));
//			if (arg instanceof IdentExpression) {
//				final InstructionArgument x = gf.vte_lookup(((IdentExpression) arg).getText());
//				final TypeTableEntry tte;
//				if (x instanceof ConstTableIA) {
//					final ConstantTableEntry cte = gf.getConstTableEntry(((ConstTableIA) x).getIndex());
//					tte = cte.getTypeTableEntry();
//				} else if (x instanceof IntegerIA) {
//					final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) x).getIndex());
//					tte = vte.type;
//				} else {
//					//
//					// WHEN VTE_LOOKUP FAILS, IE WHEN A MEMBER VARIABLE
//					//
//					final int idte_index = gf.addIdentTableEntry((IdentExpression) arg, aContext);
//					tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, arg);
//					gf.getIdentTableEntry(idte_index).type = tte;
//				}
//				R.add(tte);
//			} else
//				R.add(getType(arg, gf));
//		}
//		assert R.size() == args.size();
//		return R;
//	}
//
//	private @NotNull List<TypeTableEntry> get_args_types(@NotNull final List<FormalArgListItem> args,
//	                                                     final @NotNull BaseGeneratedFunction gf) {
//		final List<TypeTableEntry> R = new ArrayList<TypeTableEntry>();
//		//
//		for (final FormalArgListItem arg : args) {
//			final TypeTableEntry tte;
//			final OS_Type ty;
//			if (arg.typeName() == null || arg.typeName().isNull())
//				ty = null;
//			else
//				ty = new OS_Type(arg.typeName());
//
//			tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty, arg.getNameToken());
//
//			R.add(tte);
//		}
//		assert R.size() == args.size();
//		return R;
//	}
//
//	private @NotNull Instruction expression_to_call(@NotNull final ProcedureCallExpression pce,
//	                                                @NotNull final BaseGeneratedFunction gf,
//	                                                @NotNull final Context cctx) {
//		final IExpression left = pce.getLeft();
//		switch (left.getKind()) {
//			case IDENT: {
//				return expression_to_call_add_entry(gf, pce, left, cctx);
//			}
//			case QIDENT: {
//				final IExpression xx = Helpers.qualidentToDotExpression2((Qualident) left);
////			simplify_qident((Qualident) pce.getLeft(), gf); // TODO ??
//				return expression_to_call_add_entry(gf, pce, xx/*pce.getLeft()*/, cctx);
//			}
//			case DOT_EXP: {
//				@NotNull final InstructionArgument x = simplify_dot_expression((DotExpression) left, gf, cctx); // TODO ??
//				return expression_to_call_add_entry(gf, pce, left, x, cctx);
//			}
////		default:
////			throw new NotImplementedException();
//			default:
//				throw new IllegalStateException("Unexpected value: " + left.getKind());
//		}
//	}
//
//	@NotNull
//	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
//	                                                 @NotNull final ProcedureCallExpression pce,
//	                                                 final IExpression left,
//	                                                 final Context cctx) {
//		final Instruction i = new Instruction();
//		i.setName(InstructionName.CALL); // TODO see line 686
//		final List<InstructionArgument> li = new ArrayList<InstructionArgument>();
//		final InstructionArgument assignment_path = gf.get_assignment_path(left, this, cctx);
//		final List<TypeTableEntry> args_types = get_args_types(pce.getArgs(), gf, cctx);
//		final int pte_num = addProcTableEntry(left, assignment_path, args_types, gf);
//		li.add(new ProcIA(pte_num, gf));
//		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
//		li.addAll(args_);
//		i.setArgs(li);
//		return i;
//	}
//
//	@NotNull
//	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
//	                                                 @NotNull final ProcedureCallExpression pce,
//	                                                 final IExpression left,
//	                                                 final InstructionArgument left1,
//	                                                 final Context cctx) {
//		final Instruction i = new Instruction();
//		i.setName(InstructionName.CALL);
//		final List<InstructionArgument> li = new ArrayList<InstructionArgument>();
//		final int pte_num = addProcTableEntry(left, left1, get_args_types(pce.getArgs(), gf, cctx), gf);
//		li.add(new ProcIA(pte_num, gf));
//		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
//		li.addAll(args_);
//		i.setArgs(li);
//		return i;
//	}
//
//	private void simplify_qident(final Qualident left, final GeneratedFunction gf) {
//		throw new NotImplementedException();
//	}
//
//	private @NotNull InstructionArgument simplify_dot_expression(final DotExpression dotExpression, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
//		@NotNull final InstructionArgument x = gf.get_assignment_path(dotExpression, this, cctx);
//		LOG.info("1117 " + x);
//		return x;
//	}
//
//	//
//	// region add-table-entries
//	//
//
//	private int addVariableTableEntry(final String name, final TypeTableEntry type, final @NotNull BaseGeneratedFunction gf, final OS_Element el) {
//		return gf.addVariableTableEntry(name, VariableTableType.VAR, type, el);
//	}
//
//	private int addTempTableEntry(final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
//		return addTempTableEntry(type, null, gf, null);
//	}
//
//	private int addTempTableEntry(final OS_Type type,
//	                              @Nullable final IdentExpression name,
//	                              @NotNull final BaseGeneratedFunction gf,
//	                              @Nullable final OS_Element el) {
//		final String theName;
//		final int num;
//		final TypeTableEntry tte;
//
//		if (name != null) {
//			tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, name);
//			theName = name.getText();
//			// README Don't set tempNum because we have a name
//			num = -1;
//		} else {
//			tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type);
//			theName = null;
//			num = gf.nextTemp();
//		}
//		final VariableTableEntry vte = new VariableTableEntry(gf.vte_list.size(), VariableTableType.TEMP, theName, tte, el);
//		vte.tempNum = num;
//		gf.vte_list.add(vte);
//		return vte.getIndex();
//	}
//
//	/**
//	 * Add a Constant Table Entry of type with Type Table Entry type {@link TypeTableEntry.Type#SPECIFIED}
//	 * @param name
//	 * @param initialValue
//	 * @param type
//	 * @param gf
//	 * @return the cte table index
//	 */
//	private int addConstantTableEntry(final String name, final IExpression initialValue, final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
//		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, initialValue);
//		final ConstantTableEntry cte = new ConstantTableEntry(gf.cte_list.size(), name, initialValue, tte);
//		gf.cte_list.add(cte);
//		return cte.index;
//	}
//
//	/**
//	 * Add a Constant Table Entry of type with Type Table Entry type {@link TypeTableEntry.Type#TRANSIENT}
//	 * @param name
//	 * @param initialValue
//	 * @param type
//	 * @param gf
//	 * @return the cte table index
//	 */
//	private int addConstantTableEntry2(final String name, final IExpression initialValue, final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
//		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, initialValue);
//		final ConstantTableEntry cte = new ConstantTableEntry(gf.cte_list.size(), name, initialValue, tte);
//		gf.cte_list.add(cte);
//		return cte.index;
//	}
//
//	// endregion
//
//	private int add_i(@NotNull final BaseGeneratedFunction gf, final InstructionName x, final List<InstructionArgument> list_of, final Context ctx) {
//		final int i = gf.add(x, list_of, ctx);
//		return i;
//	}
//
//}
//
////
////
////
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang.types.OS_UnitType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.SpecialFunctions;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceConstructStatement;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.Label;
import tripleo.elijah.stages.instructions.LabelIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.SymbolIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.stages.stage1.S1_Constructor;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkManager;
import tripleo.util.range.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;
import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 9/10/20 2:28 PM
 */
public class GenerateFunctions {
	private static final String        PHASE = "GenerateFunctions";
	final                OS_Module     module;
	private final        GeneratePhase phase;
	private final        ElLog         LOG;

	public GenerateFunctions(final GeneratePhase aPhase, final OS_Module aModule, final PipelineLogic aPipelineLogic) {
		phase  = aPhase;
		module = aModule;
		LOG    = new ElLog(module.getFileName(), aPhase.getVerbosity(), PHASE);
		//
		aPipelineLogic.addLog(LOG);
	}

	@Deprecated
	public void generateFromEntryPoints(final List<EntryPoint> entryPoints, final DeducePhase dp) {
		@NotNull final EntryPointList epl = new EntryPointList();

		entryPoints.stream().forEach(epl::add);

		generateFromEntryPoints(epl, dp);
	}

	public void generateFromEntryPoints(final EntryPointList epl, final DeducePhase deducePhase) {
		epl.generate(this, deducePhase, () -> phase.wm);
	}

	public @NotNull GeneratedConstructor generateConstructor(final ConstructorDef aConstructorDef,
	                                                         final ClassStatement parent, // TODO Namespace constructors
	                                                         final FunctionInvocation aFunctionInvocation) {
		final S1_Constructor s1c = new S1_Constructor(aConstructorDef, parent, aFunctionInvocation);

		s1c.process(new S1toG_GC_Processor(this), true);

		final GeneratedConstructor gf = s1c.getGenerated();
		return gf;
	}

	public @NotNull GeneratedConstructor generateConstructor_000(final ConstructorDef aConstructorDef,
	                                                             final ClassStatement parent, // TODO Namespace constructors
	                                                             final FunctionInvocation aFunctionInvocation) {
		final GeneratedConstructor gf = new GeneratedConstructor(aConstructorDef);
		gf.setFunctionInvocation(aFunctionInvocation);
		if (parent instanceof ClassStatement) {
			final OS_Type         parentType = parent.getOS_Type();
			final IdentExpression selfIdent  = IdentExpression.forString("self");
			final TypeTableEntry  tte        = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, parentType, selfIdent);
			gf.addVariableTableEntry("self", VariableTableType.SELF, tte, null);
		}

		{
			final List<FormalArgListItem> fali_args = aConstructorDef.fal().falis;
			final List<TypeTableEntry>    fi_args   = aFunctionInvocation.getArgs();

			for (int i = 0; i < fali_args.size(); i++) {
				final FormalArgListItem fali = fali_args.get(i);

				final TypeTableEntry tte1     = fi_args.get(i);
				final OS_Type        attached = tte1.getAttached();

				// TODO for reference now...
				final GenType  genType  = new GenType();
				final TypeName typeName = fali.typeName();
				if (typeName != null)
					genType.typeName = new OS_UserType(typeName);
				genType.resolved = attached;

				final OS_Type attached1;
				if (attached == null && typeName != null)
					attached1 = genType.typeName;
				else
					attached1 = attached;

				final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, attached1, fali.getNameToken());
//				assert attached != null; // TODO this fails

				gf.addVariableTableEntry(fali.name(), VariableTableType.ARG, tte, fali);
			}
		}

		final Context cctx = aConstructorDef.getContext();
		final int     e1   = add_i(gf, InstructionName.E, null, cctx);
		for (final FunctionItem item : aConstructorDef.getItems()) {
//			LOG.err("7056 aConstructorDef.getItem = "+item);
			generate_item(item, gf, cctx);
		}
		final int x1 = add_i(gf, InstructionName.X, List_of(new IntegerIA(e1, gf)), cctx);
		gf.addContext(aConstructorDef.getContext(), new Range(e1, x1)); // TODO remove interior contexts
//		LOG.info(String.format("602.1 %s", aConstructorDef.name()));
//		for (Instruction instruction : gf.instructionsList) {
//			LOG.info(instruction);
//		}
//		GeneratedFunction.printTables(gf);
		gf.fi = aFunctionInvocation;
		return gf;
	}

	private void generate_item(final OS_Element item, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
		final Generate_Item gi = new Generate_Item();
		if (item instanceof AliasStatement) {
			gi.generate_alias_statement((AliasStatement) item);
		} else if (item instanceof CaseConditional) {
			gi.generate_case_conditional((CaseConditional) item);
		} else if (item instanceof ClassStatement) {
			// TODO this still has no ClassInvocation
			final GeneratedClass  gc        = generateClass((ClassStatement) item);
			final int             ite_index = gf.addIdentTableEntry(((ClassStatement) item).getNameNode(), cctx);
			final IdentTableEntry ite       = gf.getIdentTableEntry(ite_index);
			ite.resolveTypeToClass(gc);
		} else if (item instanceof StatementWrapper) {
			final StatementWrapper sw             = (StatementWrapper) item;
			final IExpression      x              = sw.getExpr();
			final ExpressionKind   expressionKind = x.getKind();
			gi.generate_statement_wrapper(sw, x, expressionKind, gf, cctx);
		} else if (item instanceof IfConditional) {
			gi.generate_if((IfConditional) item, gf);
		} else if (item instanceof Loop) {
//			LOG.err("800 -- generateLoop");
			gi.generate_loop((Loop) item, gf);
		} else if (item instanceof MatchConditional) {
			gi.generate_match_conditional((MatchConditional) item, gf);
		} else if (item instanceof NamespaceStatement) {
//			LOG.info("Skip namespace for now "+((NamespaceStatement) item).name());
			throw new NotImplementedException();
		} else if (item instanceof VariableSequence) {
			gi.generate_variable_sequence((VariableSequence) item, gf, cctx);
		} else if (item instanceof WithStatement) {
			throw new NotImplementedException();
		} else if (item instanceof SyntacticBlock) {
			throw new NotImplementedException();
		} else if (item instanceof ConstructStatement) {
			final ConstructStatement constructStatement = (ConstructStatement) item;
			gi.generate_construct_statement(constructStatement, gf, cctx);
		} else {
			throw new IllegalStateException("cant be here");
		}
	}

	public @NotNull GeneratedClass generateClass(@NotNull final ClassStatement klass) {
		final GeneratedClass gc = new GeneratedClass(klass, module);
		AccessNotation       an = null;

		for (final ClassItem item : klass.getItems()) {
			if (item instanceof AliasStatement) {
//				LOG.info("Skip alias statement for now");
//				throw new NotImplementedException();
			} else if (item instanceof ClassStatement) {
//				final ClassStatement classStatement = (ClassStatement) item;
//				@NotNull GeneratedClass gen_c = generateClass(classStatement);
//				gc.addClass(classStatement, gen_c);
			} else if (item instanceof ConstructorDef) {
//				final ConstructorDef constructorDef = (ConstructorDef) item;
//				@NotNull GeneratedConstructor f = generateConstructor(constructorDef, klass, null); // TODO remove this null
//				gc.addConstructor(constructorDef, f);
			} else if (item instanceof DestructorDef) {
				throw new NotImplementedException();
			} else if (item instanceof DefFunctionDef) {
//				@NotNull GeneratedFunction f = generateFunction((DefFunctionDef) item, klass);
//				gc.addFunction((DefFunctionDef) item, f);
			} else if (item instanceof FunctionDef) {
				// README handled in WlGenerateFunction
//				@NotNull GeneratedFunction f = generateFunction((FunctionDef) item, klass);
//				gc.addFunction((FunctionDef) item, f);
			} else if (item instanceof NamespaceStatement) {
				throw new NotImplementedException();
			} else if (item instanceof VariableSequence) {
				final VariableSequence vsq = (VariableSequence) item;
				for (final VariableStatement vs : vsq.items()) {
//					LOG.info("6999 "+vs);
					gc.addVarTableEntry(an, vs);
				}
			} else if (item instanceof AccessNotation) {
				//
				// TODO two AccessNotation's can be active at once, for example if the first
				//  one defined only classes and the second one defined only a category
				//
				an = (AccessNotation) item;
//				gc.addAccessNotation(an);
			} else if (item instanceof PropertyStatement) {
				final @NotNull PropertyStatement ps = (PropertyStatement) item;
				LOG.err("307 Skipping property for now");
			} else {
				LOG.err("305 "+item.getClass().getName());
				throw new NotImplementedException();
			}
		}

		gc.createCtor0();

//		klass._a.setCode(nextClassCode());

		return gc;
	}

	/**
	 * See {@link WlGenerateClass#run(WorkManager)}
	 *
	 * @param aClassStatement
	 * @param aClassInvocation
	 * @return
	 */
	public GeneratedClass generateClass(final ClassStatement aClassStatement, final ClassInvocation aClassInvocation) {
		@NotNull final GeneratedClass Result = generateClass(aClassStatement);
		Result.ci = aClassInvocation;
		return Result;
	}

//	/**
//	 * See {@link WlGenerateFunction#run(WorkManager)}
//	 *
//	 * @param aFunctionDef
//	 * @param aClassStatement
//	 * @param aFunctionInvocation
//	 * @return
//	 */
//	public GeneratedFunction generateFunction(@NotNull FunctionDef aFunctionDef,
//											  @NotNull OS_Element aClassStatement,
//											  @NotNull FunctionInvocation aFunctionInvocation) {
//		@NotNull GeneratedFunction Result = generateFunction(aFunctionDef, aClassStatement);
//		Result.fi = aFunctionInvocation;
//		return Result;
//	}

	@NotNull GeneratedFunction generateFunction(@NotNull final FunctionDef fd,
	                                            final OS_Element parent,
	                                            @NotNull final FunctionInvocation aFunctionInvocation) {
//		LOG.err("601.1 fn "+fd.name() + " " + parent);
		final GeneratedFunction gf = new GeneratedFunction(fd);
		if (parent instanceof ClassStatement)
			gf.addVariableTableEntry("self",
			  VariableTableType.SELF,
			  gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, ((ClassStatement) parent).getOS_Type(), IdentExpression.forString("self")),
			  null);
		final OS_Type  returnType;
		final TypeName returnType1 = fd.returnType();
		if (returnType1 == null)
			returnType = new OS_UnitType();
		else
			returnType = new OS_UserType(returnType1);
		gf.addVariableTableEntry("Result",
		  VariableTableType.RESULT,
		  gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, returnType, IdentExpression.forString("Result")),
		  null); // TODO what about Unit returns?

		{
			final List<FormalArgListItem> fali_args = fd.fal().falis;
			final List<TypeTableEntry>    fi_args   = aFunctionInvocation.getArgs();

			if (fi_args.size() > 0) {
				for (int i = 0; i < fali_args.size(); i++) {
					final FormalArgListItem fali = fali_args.get(i);

					final TypeTableEntry tte1     = fi_args.get(i);
					final OS_Type        attached = tte1.getAttached();

					// TODO for reference now...
					final GenType  genType  = new GenType();
					final TypeName typeName = fali.typeName();
					if (typeName != null)
						genType.typeName = new OS_UserType(typeName);
					genType.resolved = attached;

					final OS_Type attached1;
					if (attached == null && typeName != null)
						attached1 = genType.typeName;
					else
						attached1 = attached;

					final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, attached1, fali.getNameToken());
//		    		assert attached != null; // TODO this fails

					gf.addVariableTableEntry(fali.name(), VariableTableType.ARG, tte, fali);
				}
			}
		}

		// TODO Exception !!??

		//
		final Context cctx = fd.getContext();
		final int     e1   = add_i(gf, InstructionName.E, null, cctx);
		for (final FunctionItem item : fd.getItems()) {
//			LOG.err("7001 fd.getItem = "+item);
			generate_item(item, gf, cctx);
		}
		final int x1 = add_i(gf, InstructionName.X, List_of(new IntegerIA(e1, gf)), cctx);
		gf.addContext(fd.getContext(), new Range(e1, x1)); // TODO remove interior contexts
//		LOG.info(String.format("602.1 %s", fd.name()));
//		for (Instruction instruction : gf.instructionsList) {
//			LOG.info(instruction);
//		}
//		GeneratedFunction.printTables(gf);

		gf.fi = aFunctionInvocation;
		return gf;
	}

	public void generateAllTopLevelClasses(final List<GeneratedNode> lgc) {
		for (final ModuleItem item : module.getItems()) {
			if (item instanceof NamespaceStatement) {
				final NamespaceStatement          namespaceStatement = (NamespaceStatement) item;
				final @NotNull GeneratedNamespace ns                 = generateNamespace(namespaceStatement);
				lgc.add(ns);
			} else if (item instanceof ClassStatement) {
				final ClassStatement          classStatement = (ClassStatement) item;
				@NotNull final GeneratedClass kl             = generateClass(classStatement);
				lgc.add(kl);
			}
			// TODO enums, datatypes, (type)aliases
		}
	}

	@NotNull
	public GeneratedNamespace generateNamespace(final NamespaceStatement namespace1) {
		final GeneratedNamespace                           gn = new GeneratedNamespace(namespace1, module);
		@org.jetbrains.annotations.Nullable AccessNotation an = null;

		for (final ClassItem item : namespace1.getItems()) {
			if (item instanceof AliasStatement) {
//				LOG.err("328 Skip AliasStatement for now");
//				throw new NotImplementedException();
			} else if (item instanceof ClassStatement) {
				throw new NotImplementedException();
			} else if (item instanceof ConstructorDef) {
				throw new NotImplementedException();
			} else if (item instanceof DestructorDef) {
				throw new NotImplementedException();
			} else if (item instanceof FunctionDef) {
				//throw new NotImplementedException();
//				@NotNull GeneratedFunction f = generateFunction((FunctionDef) item, namespace1);
//				gn.addFunction((FunctionDef) item, f);
			} else if (item instanceof DefFunctionDef) {
				throw new NotImplementedException();
			} else if (item instanceof NamespaceStatement) {
				throw new NotImplementedException();
			} else if (item instanceof VariableSequence) {
				final VariableSequence vsq = (VariableSequence) item;
				for (final VariableStatement vs : vsq.items()) {
//					LOG.info("6999 "+vs);
					gn.addVarTableEntry(an, vs);
				}
			} else if (item instanceof AccessNotation) {
				//
				// TODO two AccessNotation's can be active at once, for example if the first
				//  one defined only classes and the second one defined only a category
				//
				an = (AccessNotation) item;
//				gn.addAccessNotation(an);
			} else
				throw new NotImplementedException();
		}

		gn.createCtor0();

		return gn;
	}

	private void generate_item_assignment(final StatementWrapper aStatementWrapper, @NotNull final IExpression x, @NotNull final BaseGeneratedFunction gf, final @NotNull Context cctx) {
//		LOG.err(String.format("801 %s %s", x.getLeft(), ((BasicBinaryExpression) x).getRight()));
		final BasicBinaryExpression             bbe    = (BasicBinaryExpression) x;
		final IExpression                       right1 = bbe.getRight();
		final @NotNull Generate_item_assignment gia    = new Generate_item_assignment();
		switch (right1.getKind()) {
			case PROCEDURE_CALL:
				gia.procedure_call(aStatementWrapper, gf, bbe, (ProcedureCallExpression) right1, cctx);
				break;
			case IDENT:
				if (bbe.getLeft() instanceof IdentExpression) {
					gia.ident(gf, (IdentExpression) bbe.getLeft(), (IdentExpression) right1, cctx);
				} else if (bbe.getLeft() instanceof DotExpression) {
					gia.dot(gf, (DotExpression) bbe.getLeft(), right1, cctx);
				}
				break;
			case NUMERIC:
				gia.numeric(gf, bbe.getLeft(), (NumericExpression) right1, cctx);
				break;
			case STRING_LITERAL:
				gia.string_literal(gf, bbe.getLeft(), (StringExpression) right1, cctx);
				break;
			case ADDITION:
			case MULTIPLY:
			case GE:
			case GT:
				gia.mathematical(gf, bbe.getLeft(), right1.getKind(), right1, cctx);
				break;
			case NEG:
				gia.neg(gf, bbe.getLeft(), right1.getKind(), right1, cctx);
				break;
			default:
				LOG.err("right1.getKind(): " + right1.getKind());
				throw new NotImplementedException();
		}
	}

//	public List<GeneratedNode> generateAllTopLevelClasses() {
//		List<GeneratedNode> R = new ArrayList<GeneratedNode>();
//
//		generateAllTopLevelClasses(R);
//
//		return R;
//	}

/*
	public void generateFromEntryPoints(@NotNull List<EntryPoint> epl, DeducePhase deducePhase) {
		new EntryPointList(epl).generateFromEntryPoints(deducePhase, this, phase.wm);
	}
*/

	InstructionArgument simplify_expression(@NotNull final IExpression expression, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
		final ExpressionKind expressionKind = expression.getKind();
		switch (expressionKind) {
			case PROCEDURE_CALL:
				return simplify_expression_procedure_call(expression, gf, cctx);
			case CAST_TO: {
				final TypeCastExpression      tce       = (TypeCastExpression) expression;
				final InstructionArgument     simp      = simplify_expression(tce.getLeft(), gf, cctx);
				@NotNull final TypeTableEntry tte_index = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_UserType(tce.getTypeName()));
				final int                     x         = add_i(gf, InstructionName.CAST_TO, List_of(simp, new IntegerIA(tte_index.getIndex(), gf)), cctx);
			}
			case AS_CAST: {
				final TypeCastExpression      tce       = (TypeCastExpression) expression;
				final InstructionArgument     simp      = simplify_expression(tce.getLeft(), gf, cctx);
				@NotNull final TypeTableEntry tte_index = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_UserType(tce.getTypeName()));
				final int                     x         = add_i(gf, InstructionName.AS_CAST, List_of(simp, new IntegerIA(tte_index.getIndex(), gf)), cctx);
			}
			case DOT_EXP: {
				final DotExpression de = (DotExpression) expression;
				return gf.get_assignment_path(de, this, cctx);
			}
			case QIDENT: {
				final Qualident   q  = (Qualident) expression;
				final IExpression de = Helpers.qualidentToDotExpression2(q);
				return gf.get_assignment_path(de, this, cctx);
			}
			case IDENT:
				final String text = ((IdentExpression) expression).getText();
				InstructionArgument i = gf.vte_lookup(text);
				if (i == null) {
					final IdentTableEntry x = gf.getIdentTableEntryFor(expression);
					if (x == null) {
						final int ii = gf.addIdentTableEntry((IdentExpression) expression, cctx);
						i = new IdentIA(ii, gf);
					} else {
						i = new IdentIA(x.getIndex(), gf);
					}
				}
				return i;
			case NUMERIC: {
				final NumericExpression ne = (NumericExpression) expression;
				final int               ii = addConstantTableEntry2(null, ne, ne.getType(), gf);
				return new ConstTableIA(ii, gf);
			}
			case STRING_LITERAL: {
				final StringExpression se = (StringExpression) expression;
				final int              ii = addConstantTableEntry2(null, se, se.getType(), gf);
				return new ConstTableIA(ii, gf);
			}
			case CHAR_LITERAL: {
				final CharLitExpression cle = (CharLitExpression) expression;
				final int               ii  = addConstantTableEntry2(null, cle, cle.getType(), gf);
				return new ConstTableIA(ii, gf);
			}
			case GET_ITEM: {
				final @NotNull GetItemExpression gie   = (GetItemExpression) expression;
				final IExpression                left  = gie.getLeft();
				final IExpression                right = gie.index;
				final InstructionArgument        left_instruction;
				final InstructionArgument        right_instruction;
				if (left.is_simple()) {
					if (left instanceof IdentExpression) {
						left_instruction = simplify_expression(left, gf, cctx);
					} else {
						// a constant
						assert IExpression.isConstant(right);
						final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
						left_instruction = new ConstTableIA(left_constant_num, gf);
					}
				} else {
					// create a tmp var
					left_instruction = simplify_expression(left, gf, cctx);
				}
				if (right.is_simple()) {
					if (right instanceof IdentExpression) {
						right_instruction = simplify_expression(right, gf, cctx);
					} else {
						// a constant
						assert IExpression.isConstant(right);
						final int right_constant_num = addConstantTableEntry2(null, right, right.getType(), gf);
						right_instruction = new ConstTableIA(right_constant_num, gf);
					}
				} else {
					// create a tmp var
					right_instruction = simplify_expression(right, gf, cctx);
				}
				{
					// create a call
					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
					//					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
					final TypeTableEntry tte_left  = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
					final TypeTableEntry tte_right = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, right);
					final int            pte       = addProcTableEntry(expr_kind_name, null, List_of(tte_left, tte_right), gf);
					final int            tmp       = addTempTableEntry(new OS_BuiltinType(BuiltInTypes.Boolean), gf); // README should be Boolean
					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
					final Instruction inst = new Instruction();
					inst.setName(InstructionName.CALLS);
					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction, right_instruction));
					final FnCallArgs fca = new FnCallArgs(inst, gf);
					final int        x   = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
					return new IntegerIA(tmp, gf);
				}
			}
			case SUBEXPRESSION: {
				final SubExpression subexpression = (SubExpression) expression;
				final IExpression   exp           = subexpression.getExpression();

				final InstructionArgument ia = simplify_expression(exp, gf, cctx);
				return ia;
			}
			case LT_:
			case GT:
			case GE:
			case ADDITION:
			case MULTIPLY: // TODO all BinaryExpressions go here
			case SUBTRACTION:
			case DIVIDE:
			case NOT_EQUAL:
			case EQUAL:
			case MODULO: {
				final BasicBinaryExpression bbe   = (BasicBinaryExpression) expression;
				final IExpression           left  = bbe.getLeft();
				final IExpression           right = bbe.getRight();
				final InstructionArgument   left_instruction;
				final InstructionArgument   right_instruction;
				if (left.is_simple()) {
					if (left instanceof IdentExpression) {
						left_instruction = simplify_expression(left, gf, cctx);
					} else {
						// a constant
						if (IExpression.isConstant(left)) {
							final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
							left_instruction = new ConstTableIA(left_constant_num, gf);
						} else {
							left_instruction = simplify_expression(left, gf, cctx);
						}
					}
				} else {
					// create a tmp var
					left_instruction = simplify_expression(left, gf, cctx);
				}
				if (right.is_simple()) {
					if (right instanceof IdentExpression) {
						right_instruction = simplify_expression(right, gf, cctx);
					} else {
						// a constant
						if (IExpression.isConstant(right)) {
							final int right_constant_num = addConstantTableEntry2(null, right, right.getType(), gf);
							right_instruction = new ConstTableIA(right_constant_num, gf);
						} else {
							right_instruction = simplify_expression(right, gf, cctx);
						}
					}
				} else {
					// create a tmp var
					right_instruction = simplify_expression(right, gf, cctx);
				}
				{
					// create a call
					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
//					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
					final TypeTableEntry tte_left  = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
					final TypeTableEntry tte_right = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, right);
					final int            pte       = addProcTableEntry(expr_kind_name, null, List_of(tte_left, tte_right), gf);
					final int            tmp       = addTempTableEntry(new OS_BuiltinType(BuiltInTypes.Boolean), gf);
					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
					final Instruction inst = new Instruction();
					inst.setName(InstructionName.CALLS);
					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction, right_instruction));
					final FnCallArgs fca = new FnCallArgs(inst, gf);
					// TODO should be AGNC
					final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
					return new IntegerIA(tmp, gf); // TODO  is this right?? we want to return the variable, not proc calls, right?
				}
//				throw new NotImplementedException();
			}
			case NEG: {
				final UnaryExpression     bbe  = (UnaryExpression) expression;
				final IExpression         left = bbe.getLeft();
				final InstructionArgument left_instruction;
				if (left.is_simple()) {
					if (left instanceof IdentExpression) {
						left_instruction = simplify_expression(left, gf, cctx);
					} else {
						// a constant
						final int left_constant_num = addConstantTableEntry2(null, left, left.getType(), gf);
						left_instruction = new ConstTableIA(left_constant_num, gf);
					}
				} else {
					// create a tmp var
					left_instruction = simplify_expression(left, gf, cctx);
				}
				{
					// create a call
					final IdentExpression expr_kind_name = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
//					TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, expr_kind_name);
					final TypeTableEntry tte_left = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, left);
					final int            pte      = addProcTableEntry(expr_kind_name, null, List_of(tte_left), gf);
					final int            tmp      = addTempTableEntry(new OS_BuiltinType(BuiltInTypes.Boolean), gf);
					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), cctx);
					final Instruction inst = new Instruction();
					inst.setName(InstructionName.CALLS);
					inst.setArgs(List_of(new ProcIA(pte, gf), left_instruction));
					final FnCallArgs fca = new FnCallArgs(inst, gf);
					// TODO should be AGNC
					final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp, gf), fca), cctx);
					return new IntegerIA(tmp, gf);
				}
			}
//			break;
			case ASSIGNMENT: {
				final InstructionArgument s1 = simplify_expression(expression.getLeft(), gf, cctx);
				final InstructionArgument s2 = simplify_expression(((BasicBinaryExpression) expression).getRight(), gf, cctx);
				final int                 x  = add_i(gf, InstructionName.AGN, List_of(s1, s2), cctx);
				return s1; // TODO is this right?
			}
			default:
				throw new IllegalStateException("Unexpected value: " + expressionKind);
		}
//		return null;
	}

	@NotNull
	private InstructionArgument simplify_expression_procedure_call(@NotNull final IExpression expression, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
		final @NotNull ProcedureCallExpression pce  = (ProcedureCallExpression) expression;
		final IExpression                      left = pce.getLeft();
		final ExpressionList                   args = pce.getArgs();
		final InstructionArgument              left_ia;
		final List<InstructionArgument>        right_ia;
		//
		final int initialCapacity = args != null ? args.size() + 1 : 1;
		right_ia = new ArrayList<InstructionArgument>(initialCapacity);
		//
		if (left.is_simple()) {
			if (left instanceof IdentExpression) {
				// for ident(xyz...)
				final int x = gf.addIdentTableEntry((IdentExpression) left, cctx);
				// TODO attach to var/const or lookup later in deduce
				left_ia = new IdentIA(x, gf);
			} else if (left instanceof SubExpression) {
				// for (1).toString() etc
				final SubExpression       se = (SubExpression) left;
				final InstructionArgument ia = simplify_expression(se.getExpression(), gf, cctx);
				//return ia;  // TODO is this correct?
				left_ia = ia;
			} else {
				// for "".strip() etc
				assert IExpression.isConstant(left);
				final int x = addConstantTableEntry(null, left, left.getType(), gf);
				left_ia = new ConstTableIA(x, gf);
//						throw new IllegalStateException("Cant be here");
			}
		} else {
			final InstructionArgument x = simplify_expression(left, gf, cctx);
			left_ia = x;
		}
		final List<TypeTableEntry> args1 = new ArrayList<TypeTableEntry>();
		if (args != null) {
			for (final IExpression arg : args) {
				final InstructionArgument ia;
				final TypeTableEntry      iat;
				if (arg.is_simple()) {
					final int y =2;
					if (arg instanceof IdentExpression) {
						final int x = gf.addIdentTableEntry((IdentExpression) arg, cctx);
						// TODO attach to var/const or lookup later in deduce
						ia  = new IdentIA(x, gf);
						iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, arg);
					} else if (arg instanceof SubExpression) {
						final SubExpression se = (SubExpression) arg;
						ia  = simplify_expression(se.getExpression(), gf, cctx);
						iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, arg);
					} else {
						assert IExpression.isConstant(arg);
						final int x = addConstantTableEntry(null, arg, arg.getType(), gf);
						ia  = new ConstTableIA(x, gf);
						iat = gf.getConstTableEntry(x).type;
					}
				} else {
					ia  = simplify_expression(arg, gf, cctx);
					iat = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null/*README wait to be deduced*/, arg);
				}
				right_ia.add(ia);
				args1.add(iat);
			}
		}
		final int pte = addProcTableEntry(expression, left_ia, args1, gf);
		right_ia.add(0, new ProcIA(pte, gf));
		{
			final int tmp_var = addTempTableEntry(null, gf); // line 686 is here
			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp_var, gf)), cctx);
			final Instruction i = new Instruction();
			i.setName(InstructionName.CALL);
			i.setArgs(right_ia);
			// TODO should be AGNC
			final int x = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(tmp_var, gf), new FnCallArgs(i, gf)), cctx);
			return new IntegerIA(tmp_var, gf); // return tmp_var instead of expression assigning it
		}
	}

	private void generate_item_dot_expression(@org.jetbrains.annotations.Nullable final InstructionArgument backlink,
	                                          final IExpression left,
	                                          @NotNull final IExpression right,
	                                          final @NotNull BaseGeneratedFunction gf,
	                                          final Context cctx) {
		final int y = 2;
		final int x = gf.addIdentTableEntry((IdentExpression) left, cctx);
		if (backlink != null) {
			final IdentIA identIA = new IdentIA(x, gf);
			identIA.setPrev(backlink);
//			gf.getIdentTableEntry(x).addStatusListener(new DeduceTypes2.FoundParent());
//			gf.getIdentTableEntry(x).backlink = backlink;
		}
		if (right.getLeft() == right)
			return;
		//
		if (right instanceof IdentExpression)
			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), right, gf, cctx);
		else
			generate_item_dot_expression(new IdentIA(x, gf), right.getLeft(), ((BasicBinaryExpression) right).getRight(), gf, cctx);
	}

	private void assign_variable(final @NotNull BaseGeneratedFunction gf, final int vte, @NotNull final IExpression value, final Context cctx) {
		if (value == IExpression.UNASSIGNED) return; // default_expression
		switch (value.getKind()) {
		case PROCEDURE_CALL:
			final ProcedureCallExpression pce = (ProcedureCallExpression) value;
			final FnCallArgs fnCallArgs = new FnCallArgs(expression_to_call(pce, gf, cctx), gf);
			final int ii2 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), fnCallArgs), cctx);
			final VariableTableEntry vte_proccall = gf.getVarTableEntry(vte);
			final InstructionArgument gg = fnCallArgs.expression_to_call.getArg(0);

			@NotNull final TableEntryIV g;
			if (gg instanceof IntegerIA) {
				g = gf.getVarTableEntry(((IntegerIA) gg).getIndex());
			} else if (gg instanceof IdentIA) {
				g = gf.getIdentTableEntry(((IdentIA) gg).getIndex());
			} else if (gg instanceof ProcIA) {
				g = gf.getProcTableEntry(((ProcIA) gg).getIndex());
			} else
				throw new NotImplementedException();

			final TypeTableEntry tte_proccall = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value, g);
				fnCallArgs.setType(tte_proccall);
				vte_proccall.addPotentialType(ii2, tte_proccall);
				break;
			case NUMERIC:
				final int ci = addConstantTableEntry(null, value, value.getType(), gf);
				final int ii = add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(vte, gf), new ConstTableIA(ci, gf)), cctx);
				final VariableTableEntry vte_numeric = gf.getVarTableEntry(vte);
				vte_numeric.addPotentialType(ii, gf.getConstTableEntry(ci).type);
				break;
			case IDENT:
				final InstructionArgument ia1 = simplify_expression(value, gf, cctx);
				final int ii3 = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte, gf), ia1), cctx);
				final VariableTableEntry vte3_ident = gf.getVarTableEntry(vte);
				final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, value);
				vte3_ident.addPotentialType(ii3, tte);
				break;
			case FUNC_EXPR:
				final FuncExpr fe = (FuncExpr) value;
				final int pte_index = addProcTableEntry(fe, null, get_args_types(fe.getArgs(), gf), gf);
				final int ii4 = add_i(gf, InstructionName.AGNF, List_of(new IntegerIA(vte, gf), new IntegerIA(pte_index, gf)), cctx);
				final VariableTableEntry vte3_func = gf.getVarTableEntry(vte);
				final TypeTableEntry tte_func = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_FuncExprType(fe), value);
				vte3_func.addPotentialType(ii4, tte_func);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + value.getKind());
		}
	}

	private TypeTableEntry getType(@NotNull final IExpression arg, final BaseGeneratedFunction gf) {
		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, arg.getType(), arg);
		return tte;
	}

	private void simplify_procedure_call(@NotNull final ProcedureCallExpression pce, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
		final IExpression    left = pce.getLeft();
		final ExpressionList args = pce.getArgs();
		//
		InstructionArgument expression_num = simplify_expression(left, gf, cctx);
		if (expression_num == null) {
			expression_num = gf.get_assignment_path(left, this, cctx);
		}
		final int i;

		if (left instanceof ProcedureCallExpression)
			i = addProcTableEntry(left, expression_num, get_args_types(args, gf, cctx), gf);
		else
			i = addProcTableEntry(pce, expression_num, get_args_types(args, gf, cctx), gf);

		final List<InstructionArgument> l = new ArrayList<InstructionArgument>();
		l.add(new ProcIA(i, gf));
		l.addAll(simplify_args(args, gf, cctx));
		final int instructionIndex = add_i(gf, InstructionName.CALL, l, cctx);
		{
			@NotNull final ProcTableEntry pte = gf.getProcTableEntry(i);
			if (expression_num instanceof IdentIA) {
				@NotNull final IdentTableEntry idte = ((IdentIA) expression_num).getEntry();
				idte.setCallablePTE(pte);
				pte.typePromise().then(new DoneCallback<GenType>() { // TODO should this be done here?
					@Override
					public void onDone(final GenType result) {
						@NotNull final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, result.resolved);
						tte.genType.copy(result);
						idte.addPotentialType(instructionIndex, tte);
					}
				});
			}
		}
	}

	private @NotNull List<InstructionArgument> simplify_args(@org.jetbrains.annotations.Nullable final ExpressionList args, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
		final List<InstructionArgument> R = new ArrayList<InstructionArgument>();
		if (args == null) return R;
		//
		for (final IExpression expression : args) {
			final InstructionArgument ia = simplify_expression(expression, gf, cctx);
			if (ia != null) {
//				LOG.err("109 "+expression);
				R.add(ia);
			} else {
				LOG.err("109-0 error expr not found " + expression);
			}
		}
		return R;
	}

	private @NotNull Collection<InstructionArgument> simplify_args2(@org.jetbrains.annotations.Nullable final ExpressionList args, @NotNull final GeneratedFunction gf, final Context cctx) {
		Collection<InstructionArgument> R = new ArrayList<InstructionArgument>();
		if (args == null) return R;
		//
		R = Collections2.transform(args.expressions(), new Function<IExpression, InstructionArgument>() {
			@Override
			public @Nullable InstructionArgument apply(@Nullable final IExpression input) {
				assert input != null;
				@NotNull final IExpression expression = input;
				final InstructionArgument  ia         = simplify_expression(expression, gf, cctx);
				if (ia != null) {
					LOG.err("109-1 " + expression);
				} else {
					LOG.err("109-01 error expr not found " + expression);
				}
				return ia;
			}
		});
		return R;
	}

	public int addProcTableEntry(final IExpression expression, final InstructionArgument expression_num, final List<TypeTableEntry> args, final BaseGeneratedFunction gf) {
		final ProcTableEntry pte = new ProcTableEntry(gf.prte_list.size(), expression, expression_num, args);
		gf.prte_list.add(pte);
		if (expression_num instanceof IdentIA) {
			final IdentIA identIA = (IdentIA) expression_num;
			if (identIA.getEntry().getCallablePTE() == null)
				identIA.getEntry().setCallablePTE(pte);
		}
		return pte.index;
	}

	@NotNull
	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
	                                                 @NotNull final ProcedureCallExpression pce,
	                                                 final @NotNull IExpression left,
	                                                 final Context cctx) {
		final Instruction i = new Instruction();
		i.setName(InstructionName.CALL); // TODO see line 686
		final List<InstructionArgument> li              = new ArrayList<InstructionArgument>();
		final InstructionArgument       assignment_path = gf.get_assignment_path(left, this, cctx);
		final List<TypeTableEntry>      args_types      = get_args_types(pce.getArgs(), gf, cctx);
		final int                       pte_num         = addProcTableEntry(left, assignment_path, args_types, gf);
		li.add(new ProcIA(pte_num, gf));
		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
		li.addAll(args_);
		i.setArgs(li);
		return i;
	}

	@NotNull
	private Instruction expression_to_call_add_entry(final @NotNull BaseGeneratedFunction gf,
	                                                 @NotNull final ProcedureCallExpression pce,
	                                                 final IExpression left,
	                                                 final InstructionArgument left1,
	                                                 final Context cctx) {
		final Instruction i = new Instruction();
		i.setName(InstructionName.CALL);
		final @NotNull List<InstructionArgument> li      = new ArrayList<InstructionArgument>();
		final int                                pte_num = addProcTableEntry(left, left1, get_args_types(pce.getArgs(), gf, cctx), gf);
		li.add(new ProcIA(pte_num, gf));
		final List<InstructionArgument> args_ = simplify_args(pce.getArgs(), gf, cctx);
		li.addAll(args_);
		i.setArgs(li);
		return i;
	}

	@NotNull List<TypeTableEntry> get_args_types(@org.jetbrains.annotations.Nullable final ExpressionList args,
	                                             final BaseGeneratedFunction gf,
	                                             @NotNull final Context aContext) {
		final List<TypeTableEntry> R = new ArrayList<TypeTableEntry>();
		if (args == null) return R;
		//
		for (final IExpression arg : args) {
			final OS_Type type = arg.getType();
//			LOG.err(String.format("108 %s %s", arg, type));
			if (arg instanceof IdentExpression) {
				final InstructionArgument x = gf.vte_lookup(((IdentExpression) arg).getText());
				final TypeTableEntry      tte;
				if (x instanceof ConstTableIA) {
					final ConstantTableEntry cte = gf.getConstTableEntry(((ConstTableIA) x).getIndex());
					tte = cte.getTypeTableEntry();
				} else if (x instanceof IntegerIA) {
					final VariableTableEntry vte = gf.getVarTableEntry(((IntegerIA) x).getIndex());
					tte = vte.type;
				} else {
					//
					// WHEN VTE_LOOKUP FAILS, IE WHEN A MEMBER VARIABLE
					//
					final int idte_index = gf.addIdentTableEntry((IdentExpression) arg, aContext);
					tte                                    = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, arg);
					gf.getIdentTableEntry(idte_index).type = tte;
				}
				R.add(tte);
			} else
				R.add(getType(arg, gf));
		}
		assert R.size() == args.size();
		return R;
	}

	private @NotNull List<TypeTableEntry> get_args_types(@NotNull final List<FormalArgListItem> args,
	                                                     final @NotNull BaseGeneratedFunction gf) {
		final List<TypeTableEntry> R = new ArrayList<TypeTableEntry>();
		//
		for (final FormalArgListItem arg : args) {
			final TypeTableEntry tte;
			final OS_UserType    ty;
			if (arg.typeName() == null || arg.typeName().isNull())
				ty = null;
			else
				ty = new OS_UserType(arg.typeName());

			tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty, arg.getNameToken());

			R.add(tte);
		}
		assert R.size() == args.size();
		return R;
	}

	private @NotNull Instruction expression_to_call(@NotNull final ProcedureCallExpression pce,
	                                                @NotNull final BaseGeneratedFunction gf,
	                                                @NotNull final Context cctx) {
		final IExpression left = pce.getLeft();
		switch (left.getKind()) {
		case IDENT: {
			return expression_to_call_add_entry(gf, pce, left, cctx);
		}
		case QIDENT: {
			final IExpression xx = Helpers.qualidentToDotExpression2((Qualident) left);
//			simplify_qident((Qualident) pce.getLeft(), gf); // TODO ??
			return expression_to_call_add_entry(gf, pce, xx/*pce.getLeft()*/, cctx);
		}
		case DOT_EXP: {
			@NotNull final InstructionArgument x = simplify_dot_expression((DotExpression) left, gf, cctx); // TODO ??
			return expression_to_call_add_entry(gf, pce, left, x, cctx);
		}
		case PROCEDURE_CALL: {
			final int         y           = 2;
			final Instruction instruction = new Instruction();
			instruction.setName(InstructionName.NOP);
			instruction.setArgs(List_of());
			return instruction;
		}
		default:
			throw new IllegalStateException("Unexpected value: " + left.getKind());
		}
	}

	private int addTempTableEntry(final OS_Type type,
	                              @Nullable final IdentExpression name,
	                              @NotNull final BaseGeneratedFunction gf,
	                              @Nullable final OS_Element el) {
		final String         theName;
		final int            num;
		final TypeTableEntry tte;

		if (name != null) {
			tte     = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, name);
			theName = name.getText();
			// README Don't set tempNum because we have a name
			num = -1;
		} else {
			tte     = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type);
			theName = null;
			num     = gf.nextTemp();
		}
		final int                         vte_index = gf.addVariableTableEntry(theName, VariableTableType.TEMP, tte, el);
		final @NotNull VariableTableEntry vte       = gf.getVarTableEntry(vte_index);
		vte.tempNum = num;
		gf.vte_list.add(vte);
		return vte.getIndex();
	}

	/**
	 * Add a Constant Table Entry of type with Type Table Entry type {@link TypeTableEntry.Type#SPECIFIED}
	 * @param name
	 * @param initialValue
	 * @param type
	 * @param gf
	 * @return the cte table index
	 */
	private int addConstantTableEntry(final String name, final IExpression initialValue, final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
		final TypeTableEntry     tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, initialValue);
		final ConstantTableEntry cte = new ConstantTableEntry(gf.cte_list.size(), name, initialValue, tte);
		gf.cte_list.add(cte);
		return cte.index;
	}

	/**
	 * Add a Constant Table Entry of type with Type Table Entry type {@link TypeTableEntry.Type#TRANSIENT}
	 * @param name
	 * @param initialValue
	 * @param type
	 * @param gf
	 * @return the cte table index
	 */
	private int addConstantTableEntry2(final String name, final IExpression initialValue, final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
		final TypeTableEntry     tte = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, initialValue);
		final ConstantTableEntry cte = new ConstantTableEntry(gf.cte_list.size(), name, initialValue, tte);
		gf.cte_list.add(cte);
		return cte.index;
	}

	private void simplify_qident(final Qualident left, final GeneratedFunction gf) {
		throw new NotImplementedException();
	}

	private @NotNull InstructionArgument simplify_dot_expression(final DotExpression dotExpression, final @NotNull BaseGeneratedFunction gf, final Context cctx) {
		@NotNull final InstructionArgument x = gf.get_assignment_path(dotExpression, this, cctx);
		LOG.info("1117 " + x);
		return x;
	}

	//
	// region add-table-entries
	//

	private int addVariableTableEntry(final String name, final TypeTableEntry type, final @NotNull BaseGeneratedFunction gf, final OS_Element el) {
		return gf.addVariableTableEntry(name, VariableTableType.VAR, type, el);
	}

	private int addTempTableEntry(final OS_Type type, final @NotNull BaseGeneratedFunction gf) {
		return addTempTableEntry(type, null, gf, null);
	}

	public static class S1toG_GC_Processor {
		private final GenerateFunctions gf;

		public S1toG_GC_Processor(final GenerateFunctions generateFunctions) {
			this.gf = generateFunctions;
		}

		public int add_i(final GeneratedConstructor gf2, final InstructionName e, final List<InstructionArgument> lia, final Context cctx) {
			return gf.add_i(gf2, e, lia, cctx);
		}

		public void generate_item(final OS_Element item, final GeneratedConstructor gf2, final Context cctx) {
			gf.generate_item(item, gf2, cctx);
		}
	}

	class Generate_Item {
		void generate_alias_statement(final AliasStatement as) {
			throw new NotImplementedException();
		}

		private void generate_case_conditional(final CaseConditional cc) {
			NotImplementedException.raise();
//			LOG.err("Skip CaseConditional for now");
//			throw new NotImplementedException();
		}

		private void generate_match_conditional(@NotNull final MatchConditional mc, final @NotNull BaseGeneratedFunction gf) {
			final int     y    = 2;
			final Context cctx = mc.getParent().getContext(); // TODO MatchConditional.getContext returns NULL!!!
			{
				final IExpression         expr = mc.getExpr();
				final InstructionArgument i    = simplify_expression(expr, gf, cctx);
//				LOG.info("710 " + i);

				Label       label_next = gf.addLabel();
				final Label label_end  = gf.addLabel();

				{
					for (final MatchConditional.MC1 part : mc.getParts()) {
						if (part instanceof MatchConditional.MatchArm_TypeMatch) {
							final MatchConditional.MatchArm_TypeMatch mc1 = (MatchConditional.MatchArm_TypeMatch) part;
							final TypeName                            tn  = mc1.getTypeName();
							final IdentExpression                     id  = mc1.getIdent();

							final int begin0 = add_i(gf, InstructionName.ES, null, cctx);

							final int                tmp     = addTempTableEntry(new OS_UserType(tn), id, gf, id); // TODO no context!
							final VariableTableEntry vte_tmp = gf.getVarTableEntry(tmp);
							final TypeTableEntry     t       = vte_tmp.type;
							add_i(gf, InstructionName.IS_A, List_of(i, new IntegerIA(t.getIndex(), gf), /*TODO not*/new LabelIA(label_next)), cctx);
							final Context context = mc1.getContext();

							add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(tmp, gf)), context);
							final int cast_inst = add_i(gf, InstructionName.CAST_TO, List_of(new IntegerIA(tmp, gf), new IntegerIA(t.getIndex(), gf), (i)), context);
							vte_tmp.addPotentialType(cast_inst, t); // TODO in the future instructionIndex may be unsigned

							for (final FunctionItem item : mc1.getItems()) {
								generate_item(item, gf, context);
							}

							add_i(gf, InstructionName.JMP, List_of(label_end), context);
							add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
							gf.place(label_next);
							label_next = gf.addLabel();
						} else if (part instanceof MatchConditional.MatchConditionalPart2) {
							final MatchConditional.MatchConditionalPart2 mc2 = (MatchConditional.MatchConditionalPart2) part;
							final IExpression                            id  = mc2.getMatchingExpression();

							final int begin0 = add_i(gf, InstructionName.ES, null, cctx);

							final InstructionArgument i2 = simplify_expression(id, gf, cctx);
							add_i(gf, InstructionName.JNE, List_of(i, i2, label_next), cctx);
							final Context context = mc2.getContext();

							for (final FunctionItem item : mc2.getItems()) {
								generate_item(item, gf, context);
							}

							add_i(gf, InstructionName.JMP, List_of(label_end), context);
							add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
							gf.place(label_next);
//							label_next = gf.addLabel();
						} else if (part instanceof MatchConditional.MatchConditionalPart3) {
							LOG.err("Don't know what this is");
						}
					}
					gf.place(label_next);
					add_i(gf, InstructionName.NOP, List_of(), cctx);
					gf.place(label_end);
				}
			}
		}

		private void generate_if(@NotNull final IfConditional ifc, final @NotNull BaseGeneratedFunction gf) {
			final Context         cctx         = ifc.getContext();
			final IdentExpression Boolean_true = Helpers.string_to_ident("true");
			final Label           label_next   = gf.addLabel();
			final Label           label_end    = gf.addLabel();
			{
				final int                 begin0 = add_i(gf, InstructionName.ES, null, cctx);
				final IExpression         expr   = ifc.getExpr();
				final InstructionArgument i      = simplify_expression(expr, gf, cctx);
//				LOG.info("711 " + i);
				final int const_true = addConstantTableEntry("true", Boolean_true, new OS_BuiltinType(BuiltInTypes.Boolean), gf);
				add_i(gf, InstructionName.JNE, List_of(i, new ConstTableIA(const_true, gf), label_next), cctx);
				final int begin_1st = add_i(gf, InstructionName.ES, null, cctx);
				final int begin_2nd = add_i(gf, InstructionName.ES, null, cctx);
				for (final OS_Element item : ifc.getItems()) {
					generate_item(item, gf, cctx);
				}
				add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_2nd, gf)), cctx);
				if (ifc.getParts().size() == 0) {
					gf.place(label_next);
					add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_1st, gf)), cctx);
//					gf.place(label_end);
				} else {
					add_i(gf, InstructionName.JMP, List_of(label_end), cctx);
					final @NotNull List<IfConditional> parts = ifc.getParts();
					for (final IfConditional part : parts) {
						gf.place(label_next);
//						label_next = gf.addLabel();
						if (part.getExpr() != null) {
							final InstructionArgument ii = simplify_expression(part.getExpr(), gf, cctx);
							LOG.info("712 " + ii);
							add_i(gf, InstructionName.JNE, List_of(ii, new ConstTableIA(const_true, gf), label_next), cctx);
						}
						final int begin_next = add_i(gf, InstructionName.ES, null, cctx);
						for (final OS_Element partItem : part.getItems()) {
							LOG.info("709 " + part + " " + partItem);
							generate_item(partItem, gf, cctx);
						}
						add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin_next, gf)), cctx);
						gf.place(label_next);
					}
					gf.place(label_end);
				}
				add_i(gf, InstructionName.XS, List_of(new IntegerIA(begin0, gf)), cctx);
			}
		}

		private void generate_loop(@NotNull final Loop loop, final @NotNull BaseGeneratedFunction gf) {
			final Context cctx = loop.getContext();
			final int     e2   = add_i(gf, InstructionName.ES, null, cctx);
//			LOG.info("702 "+loop.getType());
			switch (loop.getType()) {
				case FROM_TO_TYPE:
					generate_loop_FROM_TO_TYPE(loop, gf, cctx);
					break;
				case TO_TYPE:
					break;
				case EXPR_TYPE:
					generate_loop_EXPR_TYPE(loop, gf, cctx);
					break;
				case ITER_TYPE:
					break;
				case WHILE:
					break;
				case DO_WHILE:
					break;
			}
			final int   x2 = add_i(gf, InstructionName.XS, List_of(new IntegerIA(e2, gf)), cctx);
			final Range r  = new Range(e2, x2);
			gf.addContext(loop.getContext(), r);
		}

		private void generate_loop_FROM_TO_TYPE(@NotNull final Loop loop, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
			final IdentExpression iterNameToken = loop.getIterNameToken();
			final String          iterName      = iterNameToken.getText();
			final int             iter_temp     = addTempTableEntry(null, iterNameToken, gf, iterNameToken); // TODO deduce later
			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(iter_temp, gf)), cctx);
			final InstructionArgument ia1 = simplify_expression(loop.getFromPart(), gf, cctx);
			if (ia1 instanceof ConstTableIA)
				add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(iter_temp, gf), ia1), cctx);
			else
				add_i(gf, InstructionName.AGN, List_of(new IntegerIA(iter_temp, gf), ia1), cctx);
			final Label label_top = gf.addLabel("top", true);
			gf.place(label_top);
			final Label label_bottom = gf.addLabel("bottom" + label_top.getIndex(), false);
			add_i(gf, InstructionName.JE, List_of(new IntegerIA(iter_temp, gf), simplify_expression(loop.getToPart(), gf, cctx), label_bottom), cctx);
			for (final StatementItem statementItem : loop.getItems()) {
//				LOG.info("705 "+statementItem);
				generate_item((OS_Element) statementItem, gf, cctx);
			}
			final IdentExpression pre_inc_name = Helpers.string_to_ident("__preinc__");
			final TypeTableEntry  tte          = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, pre_inc_name);
			final int             pre_inc      = addProcTableEntry(pre_inc_name, null, List_of(tte/*getType(left), getType(right)*/), gf);
			add_i(gf, InstructionName.CALLS, List_of(new ProcIA(pre_inc, gf), new IntegerIA(iter_temp, gf)), cctx);
			add_i(gf, InstructionName.JMP, List_of(label_top), cctx);
			gf.place(label_bottom);
		}

		private void generate_loop_EXPR_TYPE(@NotNull final Loop loop, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
			final int loop_iterator = addTempTableEntry(null, gf); // TODO deduce later
			add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(loop_iterator, gf)), cctx);
			final int                 i2  = addConstantTableEntry("", new NumericExpression(0), new OS_BuiltinType(BuiltInTypes.SystemInteger), gf);
			final InstructionArgument ia1 = new ConstTableIA(i2, gf);
//			if (ia1 instanceof ConstTableIA)
			add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(loop_iterator, gf), ia1), cctx);
//			else
//				add_i(gf, InstructionName.AGN, List_of(new IntegerIA(loop_iterator), ia1), cctx);
			final Label label_top = gf.addLabel("top", true);
			gf.place(label_top);
			final Label label_bottom = gf.addLabel("bottom" + label_top.getIndex(), false);
			add_i(gf, InstructionName.JE, List_of(new IntegerIA(loop_iterator, gf), simplify_expression(loop.getToPart(), gf, cctx), label_bottom), cctx);
			for (final StatementItem statementItem : loop.getItems()) {
				LOG.info("707 " + statementItem);
				generate_item((OS_Element) statementItem, gf, cctx);
			}
			final String          txt          = SpecialFunctions.of(ExpressionKind.INCREMENT);
			final IdentExpression pre_inc_name = Helpers.string_to_ident(txt);
			final TypeTableEntry  tte          = gf.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, pre_inc_name);
			final int             pre_inc      = addProcTableEntry(pre_inc_name, null, List_of(tte), gf);
			add_i(gf, InstructionName.CALLS, List_of(new ProcIA(pre_inc, gf), new IntegerIA(loop_iterator, gf)), cctx);
			add_i(gf, InstructionName.JMP, List_of(label_top), cctx);
			gf.place(label_bottom);
		}

		private void generate_variable_sequence(final VariableSequence item, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
			for (final VariableStatement vs : item.items()) {
				int state = 0;
//				LOG.info("8004 " + vs);
				final String      variable_name = vs.getName();
				final IExpression initialValue  = vs.initialValue();
				//
				if (vs.getTypeModifiers() == TypeModifiers.CONST) {
					if (initialValue.is_simple()) {
						if (initialValue instanceof IdentExpression) {
							state = 4;
						} else {
							state = 1;
						}
					} else {
						state = 2;
					}
				} else {
					state = 3;
				}
//				final OS_Type type = vs.initialValue().getType();
//				final String stype = type == null ? "Unknown" : getTypeString(type);
//				LOG.info("8004-1 " + type);
//				LOG.info(String.format("8004-2 %s %s;", stype, vs.getName()));
				switch (state) {
					case 1: {
						final int         ci      = addConstantTableEntry(variable_name, initialValue, initialValue.getType(), gf);
						final int         vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
						final IExpression iv      = initialValue;
						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("const"), new IntegerIA(vte_num, gf)), cctx);
						add_i(gf, InstructionName.AGNK, List_of(new IntegerIA(vte_num, gf), new ConstTableIA(ci, gf)), cctx);
					}
					break;
					case 2: {
						final int vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("val"), new IntegerIA(vte_num, gf)), cctx);
						final IExpression iv = initialValue;
						assign_variable(gf, vte_num, iv, cctx);
					}
					break;
					case 3: {
						final TypeTableEntry tte;
						if (initialValue == IExpression.UNASSIGNED && vs.typeName() != null) {
							tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, new OS_UserType(vs.typeName()), vs.getNameToken());
						} else {
							tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, initialValue.getType(), vs.getNameToken());
						}
						final int vte_num = addVariableTableEntry(variable_name, tte, gf, vs); // TODO why not vs.initialValue ??
						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("var"), new IntegerIA(vte_num, gf)), cctx);
						final IExpression iv = initialValue;
						assign_variable(gf, vte_num, iv, cctx);
					}
					break;
					case 4: {
						final int vte_num = addVariableTableEntry(variable_name, gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, (initialValue.getType()), vs.getNameToken()), gf, vs.getNameToken());
						add_i(gf, InstructionName.DECL, List_of(new SymbolIA("const"), new IntegerIA(vte_num, gf)), cctx);
						assign_variable(gf, vte_num, initialValue, cctx);
					}
					break;
					default:
						throw new IllegalStateException();
				}
			}
		}

		private void generate_statement_wrapper(final StatementWrapper aStatementWrapper,
		                                        final IExpression x,
		                                        final ExpressionKind expressionKind,
		                                        @NotNull final BaseGeneratedFunction gf,
		                                        final Context cctx) {
//			LOG.err("106-1 "+x.getKind()+" "+x);
			if (x.is_simple()) {
//				int i = addTempTableEntry(x.getType(), gf);
				switch (expressionKind) {
					case ASSIGNMENT:
//					LOG.err(String.format("703.2 %s %s", x.getLeft(), ((BasicBinaryExpression)x).getRight()));
						generate_item_assignment(aStatementWrapper, x, gf, cctx);
						break;
					case AUG_MULT: {
						LOG.info(String.format("801.1 %s %s %s", expressionKind, x.getLeft(), ((BasicBinaryExpression) x).getRight()));
//						BasicBinaryExpression bbe = (BasicBinaryExpression) x;
//						final IExpression right1 = bbe.getRight();
						final InstructionArgument  left           = simplify_expression(x.getLeft(), gf, cctx);
						final InstructionArgument  right          = simplify_expression(((BasicBinaryExpression) x).getRight(), gf, cctx);
						final IdentExpression      fn_aug_name    = Helpers.string_to_ident(SpecialFunctions.of(expressionKind));
						final List<TypeTableEntry> argument_types = List_of(gf.getVarTableEntry(to_int(left)).type, gf.getVarTableEntry(to_int(right)).type);
//						LOG.info("801.2 "+argument_types);
						final int fn_aug = addProcTableEntry(fn_aug_name, null, argument_types, gf);
						final int i      = add_i(gf, InstructionName.CALLS, List_of(new ProcIA(fn_aug, gf), left, right), cctx);
						//
						// SEE IF CALL SHOULD BE DEFERRED
						//
						for (final @NotNull TypeTableEntry argument_type : argument_types) {
							if (argument_type.getAttached() == null) {
								// still dont know the argument types at this point, which creates a problem
								// for resolving functions, so wait until later when more information is available
								if (!gf.deferred_calls.contains(i))
									gf.deferred_calls.add(i);
								break;
							}
						}
					}
					break;
					default:
						throw new NotImplementedException();
				}
			} else {
				switch (expressionKind) {
					case ASSIGNMENT:
//					LOG.err(String.format("803.2 %s %s", x.getLeft(), ((BasicBinaryExpression)x).getRight()));
						generate_item_assignment(aStatementWrapper, x, gf, cctx);
						break;
//				case IS_A:
//					break;
					case PROCEDURE_CALL:
						final ProcedureCallExpression pce = (ProcedureCallExpression) x;
						simplify_procedure_call(pce, gf, cctx);
						break;
					case DOT_EXP:
						final DotExpression de = (DotExpression) x;
						generate_item_dot_expression(null, de.getLeft(), de.getRight(), gf, cctx);
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + expressionKind);
				}
			}
		}

		public void generate_construct_statement(final ConstructStatement aConstructStatement, @NotNull final BaseGeneratedFunction gf, final Context cctx) {
			final IExpression    left = aConstructStatement.getExpr(); // TODO need type of this expr, not expr!!
			final ExpressionList args = aConstructStatement.getArgs();
			//
			InstructionArgument expression_num = simplify_expression(left, gf, cctx);
			if (expression_num == null) {
				expression_num = gf.get_assignment_path(left, GenerateFunctions.this, cctx);
			}
			final int                       i      = addProcTableEntry(left, expression_num, get_args_types(args, gf, cctx), gf);
			final List<InstructionArgument> l      = new ArrayList<InstructionArgument>();
			final ProcIA                    procIA = new ProcIA(i, gf);
			l.add(procIA);
			final List<InstructionArgument> args1 = simplify_args(args, gf, cctx);
			l.addAll(args1);
			final int instruction_number = add_i(gf, InstructionName.CONSTRUCT, l, cctx);

			{
				final DeduceConstructStatement dcs = new DeduceConstructStatement(gf, aConstructStatement);

				dcs.target = expression_num;
				if (expression_num instanceof IntegerIA || expression_num instanceof IdentIA && ((IdentIA) expression_num).getEntry().backlink == null) {
				} else {
					dcs.toEvaluateTarget = true;
				}

				dcs.call = procIA;
				dcs.args = args1;

				gf.addElement(aConstructStatement, dcs);

				final Instruction instruction = gf.getInstruction(instruction_number);
				instruction.deduceElement = dcs;
			}
		}
	}

	class Generate_item_assignment {

		public void procedure_call(final StatementWrapper aStatementWrapper, @NotNull final BaseGeneratedFunction gf, final BasicBinaryExpression bbe, final ProcedureCallExpression pce, final Context cctx) {
			final IExpression left = bbe.getLeft();

			final InstructionArgument lookup = simplify_expression(left, gf, cctx);
			final TypeTableEntry      tte    = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, bbe.getType(), left);

			if (lookup instanceof IntegerIA) {
				// TODO should be AGNC
				final int instruction_number = add_i(gf, InstructionName.AGN, List_of(lookup,
				  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
				final Instruction        instruction = gf.getInstruction(instruction_number);
				final VariableTableEntry vte         = gf.getVarTableEntry(((IntegerIA) lookup).getIndex());
				vte.addPotentialType(instruction.getIndex(), tte);
			} else {
				if (left instanceof IdentExpression) {
					final String text = ((IdentExpression) left).getText();
					final int    vte_num;
					if (aStatementWrapper instanceof WrappedStatementWrapper) {
						vte_num = addVariableTableEntry(text, tte, gf, ((WrappedStatementWrapper) aStatementWrapper).getVariableStatement());
					} else {
						vte_num = addVariableTableEntry(text, tte, gf, (IdentExpression) left);
					}
					add_i(gf, InstructionName.DECL, List_of(new SymbolIA("tmp"), new IntegerIA(vte_num, gf)), cctx);
					// TODO should be AGNC
					final int instruction_number = add_i(gf, InstructionName.AGN, List_of(new IntegerIA(vte_num, gf),
					  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
					final Instruction        instruction = gf.getInstruction(instruction_number);
					final VariableTableEntry vte         = gf.getVarTableEntry(vte_num);
					vte.addPotentialType(instruction.getIndex(), tte);
				} else {
					assert lookup instanceof IdentIA;

					// TODO should be AGNC
					final int instruction_number = add_i(gf, InstructionName.AGN, List_of(lookup,
					  new FnCallArgs(expression_to_call(pce, gf, cctx), gf)), cctx);
					final Instruction              instruction = gf.getInstruction(instruction_number);
					@NotNull final IdentTableEntry ite         = ((IdentIA) lookup).getEntry();
					ite.addPotentialType(instruction.getIndex(), tte);
				}
			}
		}

		public void dot(@NotNull final BaseGeneratedFunction gf, final DotExpression left, final IExpression right, final Context cctx) {
			final InstructionArgument simple_left  = simplify_expression(left, gf, cctx);
			final InstructionArgument simple_right = simplify_expression(right, gf, cctx);

/*
			IExpression left_dot_left_ = left.getLeft();
			assert left_dot_left_ instanceof IdentExpression;
			IdentExpression left_dot_left = (IdentExpression) left_dot_left_;

			final InstructionArgument vte_left = gf.vte_lookup(left_dot_left.getText());

			IExpression left_dot_right_ = left.getRight();
			assert left_dot_right_ instanceof IdentExpression;
			IdentExpression left_dot_right = (IdentExpression) left_dot_right_;

			final int ident_left;
			final int ident_lright;

			if (vte_left != null) {
				ident_lright = gf.addIdentTableEntry(left_dot_right, cctx);

				gf.getIdentTableEntry(ident_lright).setBacklink(vte_left);
			} else {
				ident_left = gf.addIdentTableEntry(left_dot_left, cctx);
				ident_lright = gf.addIdentTableEntry(left_dot_right, cctx);

				gf.getIdentTableEntry(ident_lright).setBacklink(new IdentIA(ident_left, gf));
			}

			final int ident_right = gf.addIdentTableEntry(right, cctx);
			final int inst = add_i(gf, InstructionName.AGN, List_of(new IdentIA(ident_lright, gf), new IdentIA(ident_right, gf)), cctx);
*/

			final int inst2 = add_i(gf, InstructionName.AGN, List_of(simple_left, simple_right), cctx);

			final int y = 2;
		}

		public void ident(@NotNull final BaseGeneratedFunction gf, final @NotNull IdentExpression left, final IdentExpression right, final Context cctx) {
			final @org.jetbrains.annotations.Nullable InstructionArgument vte_left = gf.vte_lookup(left.getText());
			final int                                                     ident_left;
			final int                                                     ident_right;
			final InstructionArgument                                     some_left;
			if (vte_left == null) {
				ident_left = gf.addIdentTableEntry(left, cctx);
				some_left  = new IdentIA(ident_left, gf);
			} else
				some_left = vte_left;
			final InstructionArgument vte_right = gf.vte_lookup(right.getText());
			final int                 inst;
			if (vte_right == null) {
				ident_right = gf.addIdentTableEntry(right, cctx);
				inst        = add_i(gf, InstructionName.AGN, List_of(some_left, new IdentIA(ident_right, gf)), cctx);
			} else {
				inst = add_i(gf, InstructionName.AGN, List_of(some_left, vte_right), cctx);

				if (vte_left != null) {
					final VariableTableEntry vte = gf.getVarTableEntry(to_int(vte_left));
					// ^^
					vte.addPotentialType(inst, gf.getVarTableEntry(to_int(vte_right)).type);
				} else if (some_left instanceof IdentIA) {
//					((IdentIA) some_left).getEntry().addPotentialType(inst, unknown_type);
				}
			}
		}

		public void numeric(@NotNull final BaseGeneratedFunction gf, final IExpression left, final NumericExpression ne, final Context cctx) {
			@NotNull final InstructionArgument agn_path = gf.get_assignment_path(left, GenerateFunctions.this, cctx);
			final int                          cte      = addConstantTableEntry("", ne, ne.getType(), gf);

			final int agn_inst = add_i(gf, InstructionName.AGN, List_of(agn_path, new ConstTableIA(cte, gf)), cctx);
			// TODO what now??
		}

		public void mathematical(@NotNull final BaseGeneratedFunction gf, final @NotNull IExpression left, final ExpressionKind kind, final IExpression right1, final Context cctx) {
			// TODO doesn't use kind
			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, right1.getType(), right1);

			final InstructionArgument left_ia  = simplify_expression(left, gf, cctx);
			final InstructionArgument right_ia = simplify_expression(right1, gf, cctx);

			final int         instruction_number = add_i(gf, InstructionName.AGN, List_of(left_ia, right_ia), cctx);
			final Instruction instruction        = gf.getInstruction(instruction_number);
			if (left_ia instanceof IntegerIA) {
				// Assuming this points to a variable and not ie a function
				final VariableTableEntry vte = gf.getVarTableEntry(to_int(left_ia));
//				vte.type = tte;
				vte.addPotentialType(instruction.getIndex(), tte);
			} else if (left_ia instanceof IdentIA) {
				final IdentTableEntry idte = gf.getIdentTableEntry(to_int(left_ia));
//				idte.type = tte;
				idte.addPotentialType(instruction.getIndex(), tte);
			}
		}

		public void string_literal(@NotNull final BaseGeneratedFunction gf, final IExpression left, final StringExpression right, final Context aContext) {
			@NotNull final InstructionArgument agn_path = gf.get_assignment_path(left, GenerateFunctions.this, aContext);
			final int                          cte      = addConstantTableEntry("", right, new OS_BuiltinType(BuiltInTypes.String_)/*right.getType()*/, gf);

			final int agn_inst = add_i(gf, InstructionName.AGN, List_of(agn_path, new ConstTableIA(cte, gf)), aContext);
			// TODO what now??
		}

		public void neg(@NotNull final BaseGeneratedFunction gf, final IExpression left, final ExpressionKind aKind, final IExpression right1, final Context cctx) {
			// TODO doesn't use kind
			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, right1.getType(), right1);

			final InstructionArgument left_ia  = simplify_expression(left, gf, cctx);
			final InstructionArgument right_ia = simplify_expression(right1, gf, cctx);

			final int         instruction_number = add_i(gf, InstructionName.AGN, List_of(left_ia, right_ia), cctx);
			final Instruction instruction        = gf.getInstruction(instruction_number);
			if (left_ia instanceof IntegerIA) {
				// Assuming this points to a variable and not ie a function
				final @NotNull VariableTableEntry vte = gf.getVarTableEntry(to_int(left_ia));
//				vte.type = tte;
				vte.addPotentialType(instruction.getIndex(), tte);
			} else if (left_ia instanceof IdentIA) {
				final @NotNull IdentTableEntry idte = gf.getIdentTableEntry(to_int(left_ia));
//				idte.type = tte;
				idte.addPotentialType(instruction.getIndex(), tte);
			}
		}
	}

	// endregion

	private int add_i(@NotNull final BaseGeneratedFunction gf, final InstructionName x, final List<InstructionArgument> list_of, final Context ctx) {
		final int i = gf.add(x, list_of, ctx);
		return i;
	}

}

//
//
//
