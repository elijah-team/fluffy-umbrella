/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.Subject;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.lang2.SpecialFunctions;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.nextgen.ClassDefinition;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.zero.IZero;
import tripleo.elijah.stages.deduce.zero.Zero_FuncExprType;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created 9/15/20 12:51 PM
 */
public class DeduceTypes2 {
	private static final String PHASE = "DeduceTypes2";
	private final @NotNull OS_Module module;
	final @NotNull DeducePhase phase;
	final ErrSink errSink;
	final @NotNull ElLog LOG;
	@NotNull WorkManager wm = new WorkManager();

	public DeduceTypes2(@NotNull OS_Module module, @NotNull DeducePhase phase) {
		this(module, phase, ElLog.Verbosity.VERBOSE);
	}

	public DeduceTypes2(@NotNull OS_Module module, @NotNull DeducePhase phase, ElLog.Verbosity verbosity) {
		this.module = module;
		this.phase = phase;
		this.errSink = module.getCompilation().getErrSink();
		this.LOG = new ElLog(module.getFileName(), verbosity, PHASE);
		//
		phase.addLog(LOG);
	}

	public void deduceFunctions(final @NotNull Iterable<GeneratedNode> lgf) {
		for (final GeneratedNode generatedNode : lgf) {
			if (generatedNode instanceof GeneratedFunction) {
				@NotNull GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				deduceOneFunction(generatedFunction, phase);
			}
		}
		@NotNull List<GeneratedNode> generatedClasses = (phase.generatedClasses.copy());
		// TODO consider using reactive here
		int size;
		do {
			size = df_helper(generatedClasses, new dfhi_functions());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
		do {
			size = df_helper(generatedClasses, new dfhi_constructors());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
	}

	/**
	 * Deduce functions or constructors contained in classes list
	 *
	 * @param aGeneratedClasses assumed to be a list of {@link GeneratedContainerNC}
	 * @param dfhi specifies what to select for:<br>
	 *             {@link dfhi_functions} will select all functions from {@code functionMap}, and <br>
	 *             {@link dfhi_constructors} will select all constructors from {@code constructors}.
	 * @param <T> generic parameter taken from {@code dfhi}
	 * @return the number of deduced functions or constructors, or 0
	 */
	<T> int df_helper(@NotNull List<GeneratedNode> aGeneratedClasses, @NotNull df_helper_i<T> dfhi) {
		int size = 0;
		for (GeneratedNode generatedNode : aGeneratedClasses) {
			@NotNull GeneratedContainerNC generatedContainerNC = (GeneratedContainerNC) generatedNode;
			final @Nullable df_helper<T> dfh = dfhi.get(generatedContainerNC);
			if (dfh == null) continue;
			@NotNull Collection<T> lgf2 = dfh.collection();
			for (final T generatedConstructor : lgf2) {
				if (dfh.deduce(generatedConstructor))
					size++;
			}
		}
		return size;
	}

	public void deduceClasses(final List<GeneratedNode> lgc) {
		for (GeneratedNode generatedNode : lgc) {
			if (!(generatedNode instanceof GeneratedClass)) continue;

			final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
			deduceOneClass(generatedClass);
		}
	}

	public void deduceOneClass(final GeneratedClass aGeneratedClass) {
		for (GeneratedContainer.VarTableEntry entry : aGeneratedClass.varTable) {
			final OS_Type vt = entry.varType;
			GenType genType = makeGenTypeFromOSType(vt, aGeneratedClass.ci.genericPart);
			if (genType != null)
				entry.resolve(genType.node);
			int y=2;
		}
	}

	@NotNull
	public String getPTEString(final ProcTableEntry aProcTableEntry) {
		String pte_string;
		if (aProcTableEntry == null)
			pte_string = "[]";
		else {
			pte_string = aProcTableEntry.getLoggingString(this, LOG);
		}
		return pte_string;
	}

	public void deduceOneFunction(GeneratedFunction aResult) {
		deduceOneFunction(aResult, phase);
	}

	public ClassInvocation genCI(GenType aType, Object aO) {
		throw new NotImplementedException(/*"ooga booga"*/);
	}

	public DeducePhase _phase() {
		return phase;
	}

	public ErrSink _errSink() {
		return errSink;
	}

	interface IElementProcessor {
		void elementIsNull();

		void hasElement(OS_Element el);
	}

	static class ProcessElement {
		static void processElement(OS_Element el, IElementProcessor ep) {
			if (el == null)
				ep.elementIsNull();
			else
				ep.hasElement(el);
		}
	}

	private GenType makeGenTypeFromOSType(final OS_Type aType, final @Nullable Map<TypeName, OS_Type> aGenericPart) {
		GenType gt = new GenType();
		gt.typeName = aType;
		if (aType.getType() == OS_Type.Type.USER) {
			final TypeName tn1 = aType.getTypeName();
			if (tn1.isNull()) return null; // TODO Unknown, needs to resolve somewhere

			assert tn1 instanceof NormalTypeName;
			final NormalTypeName tn = (NormalTypeName) tn1;
			final LookupResultList lrl = tn.getContext().lookup(tn.getName());
			final @Nullable OS_Element el = lrl.chooseBest(null);

			ProcessElement.processElement(el, new IElementProcessor() {
				@Override
				public void elementIsNull() {
					NotImplementedException.raise();
				}

				@Override
				public void hasElement(final OS_Element el) {
					final @Nullable OS_Element best = preprocess(el);
					if (best == null) return;

					if (best instanceof ClassStatement) {
						final ClassStatement classStatement = (ClassStatement) best;
						gt.resolved = classStatement.getOS_Type();
					} else if (best instanceof ClassContext.OS_TypeNameElement) {
						final ClassContext.OS_TypeNameElement typeNameElement = (ClassContext.OS_TypeNameElement) best;
						assert aGenericPart != null;
						final OS_Type x = aGenericPart.get(typeNameElement.getTypeName());
						switch (x.getType()) {
						case USER_CLASS:
							final ClassStatement classStatement1 = x.getClassOf(); // always a ClassStatement

							// TODO test next 4 (3) lines are copies of above
							if (classStatement1 != null) {
								gt.resolved = classStatement1.getOS_Type();
							}
							break;
						case USER:
							final NormalTypeName tn2 = (NormalTypeName) x.getTypeName();
							final LookupResultList lrl2 = tn.getContext().lookup(tn2.getName());
							final @Nullable OS_Element el2 = lrl2.chooseBest(null);

							// TODO test next 4 lines are copies of above
							if (el2 instanceof ClassStatement) {
								final ClassStatement classStatement = (ClassStatement) el2;
								gt.resolved = classStatement.getOS_Type();
							} else
								throw new NotImplementedException();
							break;
						}
					} else {
						LOG.err("143 "+el);
						throw new NotImplementedException();
					}

					gotResolved(gt);
				}

				private void gotResolved(final GenType gt) {
					if (gt.resolved.getClassOf().getGenericPart().size() != 0) {
						//throw new AssertionError();
						LOG.info("149 non-generic type "+tn1);
					}
					gt.genCI(null, DeduceTypes2.this, errSink, phase); // TODO aGenericPart
					assert gt.ci != null;
					genNodeForGenType2(gt);
				}

				private OS_Element preprocess(final OS_Element el) {
					@Nullable OS_Element best = el;
					try {
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, DeduceTypes2.this);
						}
						assert best != null;
						return best;
					} catch (ResolveError aResolveError) {
						LOG.err("152 Can't resolve Alias statement "+best);
						errSink.reportDiagnostic(aResolveError);
						return null;
					}
				}
			});
		} else
			throw new AssertionError("Not a USER Type");
		return gt;
	}

	interface df_helper_i<T> {
		@Nullable df_helper<T> get(GeneratedContainerNC generatedClass);
	}

	class dfhi_constructors implements df_helper_i<GeneratedConstructor> {
		@Override
		public @Nullable df_helper_Constructors get(GeneratedContainerNC aGeneratedContainerNC) {
			if (aGeneratedContainerNC instanceof GeneratedClass) // TODO namespace constructors
				return new df_helper_Constructors((GeneratedClass) aGeneratedContainerNC);
			else
				return null;
		}
	}

	class dfhi_functions implements df_helper_i<GeneratedFunction> {
		@Override
		public @NotNull df_helper_Functions get(GeneratedContainerNC aGeneratedContainerNC) {
			return new df_helper_Functions(aGeneratedContainerNC);
		}
	}

	interface df_helper<T> {
		@NotNull Collection<T> collection();

		boolean deduce(T generatedConstructor);
	}

	class df_helper_Constructors implements df_helper<GeneratedConstructor> {
		private final GeneratedClass generatedClass;

		public df_helper_Constructors(GeneratedClass aGeneratedClass) {
			generatedClass = aGeneratedClass;
		}

		@Override
		public @NotNull Collection<GeneratedConstructor> collection() {
			return generatedClass.constructors.values();
		}

		@Override
		public boolean deduce(@NotNull GeneratedConstructor generatedConstructor) {
			return deduceOneConstructor(generatedConstructor, phase);
		}
	}

	class df_helper_Functions implements df_helper<GeneratedFunction> {
		private final GeneratedContainerNC generatedContainerNC;

		public df_helper_Functions(GeneratedContainerNC aGeneratedContainerNC) {
			generatedContainerNC = aGeneratedContainerNC;
		}

		@Override
		public @NotNull Collection<GeneratedFunction> collection() {
			return generatedContainerNC.functionMap.values();
		}

		@Override
		public boolean deduce(@NotNull GeneratedFunction aGeneratedFunction) {
			return deduceOneFunction(aGeneratedFunction, phase);
		}
	}
	
	public boolean deduceOneFunction(@NotNull GeneratedFunction aGeneratedFunction, @NotNull DeducePhase aDeducePhase) {
		if (aGeneratedFunction.deducedAlready) return false;
		deduce_generated_function(aGeneratedFunction);
		aGeneratedFunction.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element el = vs.getParent().getParent();
				OS_Element el2 = aGeneratedFunction.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull GeneratedFunction gf = aGeneratedFunction;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								vte.genType.typeName = a;
								try {
									@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											vte.genType.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								int y3 = 2;
								break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
		aDeducePhase.addFunction(aGeneratedFunction, (FunctionDef) aGeneratedFunction.getFD());
		return true;
	}

	public boolean deduceOneConstructor(@NotNull GeneratedConstructor aGeneratedConstructor, @NotNull DeducePhase aDeducePhase) {
		if (aGeneratedConstructor.deducedAlready) return false;
		deduce_generated_function_base(aGeneratedConstructor, aGeneratedConstructor.getFD());
		aGeneratedConstructor.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aGeneratedConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element el = vs.getParent().getParent();
				OS_Element el2 = aGeneratedConstructor.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull GeneratedConstructor gf = aGeneratedConstructor;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								b.typeName = a;
								try {
									@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											b.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								int y3 = 2;
								break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
//		aDeducePhase.addFunction(aGeneratedConstructor, (FunctionDef) aGeneratedConstructor.getFD()); // TODO do we need this?
		return true;
	}

	private void dof_uc(@NotNull VariableTableEntry aVte, @NotNull OS_Type aA) {
		// we really want a ci from somewhere
		assert aA.getClassOf().getGenericPart().size() == 0;
		@Nullable ClassInvocation ci = new ClassInvocation(aA.getClassOf(), null);
		ci = phase.registerClassInvocation(ci);

		aVte.genType.resolved = aA; // README assuming OS_Type cannot represent namespaces
		aVte.genType.ci = ci;

		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	@NotNull List<Runnable> onRunnables = new ArrayList<Runnable>();

	void onFinish(Runnable r) {
		onRunnables.add(r);
	}

	public void deduce_generated_constructor(final @NotNull GeneratedConstructor generatedFunction) {
		final @NotNull ConstructorDef fd = (ConstructorDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduce_generated_function(final @NotNull GeneratedFunction generatedFunction) {
		final @NotNull FunctionDef fd = (FunctionDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduce_generated_function_base(final @NotNull BaseGeneratedFunction generatedFunction, @NotNull BaseFunctionDef fd) {
		final Context fd_ctx = fd.getContext();
		//
		{
			ProcTableEntry pte = generatedFunction.fi.pte;
			final @NotNull String pte_string = getPTEString(pte);
//			LOG.info("** deduce_generated_function "+ fd.name()+" "+pte_string);//+" "+((OS_Container)((FunctionDef)fd).getParent()).name());
		}
		//
		//
		for (final @NotNull Instruction instruction : generatedFunction.instructions()) {
			final Context context = generatedFunction.getContextFromPC(instruction.getIndex());
//			LOG.info("8006 " + instruction);
			switch (instruction.getName()) {
			case E:
				onEnterFunction(generatedFunction, context);
				break;
			case X:
				onExitFunction(generatedFunction, fd_ctx, context);
				break;
			case ES:
				break;
			case XS:
				break;
			case AGN:
				do_assign_normal(generatedFunction, fd_ctx, instruction, context);
				break;
			case AGNK:
				{
					final @NotNull IntegerIA arg = (IntegerIA)instruction.getArg(0);
					final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
					final InstructionArgument i2 = instruction.getArg(1);
					final @NotNull ConstTableIA ctia = (ConstTableIA) i2;
					do_assign_constant(generatedFunction, instruction, vte, ctia);
				}
				break;
			case AGNT:
				break;
			case AGNF:
				LOG.info("292 Encountered AGNF");
				break;
			case JE:
				LOG.info("296 Encountered JE");
				break;
			case JNE:
				break;
			case JL:
				break;
			case JMP:
				break;
			case CALL: {
				final int pte_num = ((ProcIA)instruction.getArg(0)).getIndex();
				final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(pte_num);
//				final InstructionArgument i2 = (instruction.getArg(1));
				{
					final @NotNull IdentIA identIA = (IdentIA) pte.expression_num;
					final String x = generatedFunction.getIdentIAPathNormal(identIA);
//					LOG.info("298 Calling "+x);
					resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(phase) {

						@SuppressWarnings("unused") final String xx = x;

						@Override
						public void foundElement(OS_Element e) {
							found_element_for_ite(generatedFunction, identIA.getEntry(), e, context);
//							identIA.getEntry().setCallablePTE(pte); // TODO ??

							pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
							if (fd instanceof DefFunctionDef) {
								final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
								forFunction(newFunctionInvocation((FunctionDef) e, pte, invocation, phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull GenType aType) {
										@Nullable InstructionArgument x = generatedFunction.vte_lookup("Result");
										assert x != null;
										((IntegerIA) x).getEntry().type.setAttached(gt(aType));
									}
								});
							}
						}

						@Override
						public void noFoundElement() {
//							errSink.reportError("370 Can't find callsite "+x);
							// TODO don't know if this is right
							@NotNull IdentTableEntry entry = identIA.getEntry();
							if (entry.getStatus() != BaseTableEntry.Status.UNKNOWN)
								entry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
						}
					});
				}
			}
			break;
			case CALLS: {
				final int i1 = to_int(instruction.getArg(0));
				final InstructionArgument i2 = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
					implement_calls(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
/*
				if (i2 instanceof IntegerIA) {
					int i2i = to_int(i2);
					VariableTableEntry vte = generatedFunction.getVarTableEntry(i2i);
					int y =2;
				} else
					throw new NotImplementedException();
*/
			}
			break;
			case RET:
				break;
			case YIELD:
				break;
			case TRY:
				break;
			case PC:
				break;
			case CAST_TO:
				// README potentialType info is already added by MatchConditional
				break;
			case DECL:
				// README for GenerateC, etc: marks the spot where a declaration should go. Wouldn't be necessary if we had proper Range's
				break;
			case IS_A:
				implement_is_a(generatedFunction, instruction);
				break;
			case NOP:
				break;
			case CONSTRUCT:
				implement_construct(generatedFunction, instruction/*, context*/);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + instruction.getName());
			}
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.type.getAttached() == null) {
				int potential_size = vte.potentialTypes().size();
				if (potential_size == 1)
					vte.type.setAttached(getPotentialTypesVte(vte).get(0).getAttached());
				else if (potential_size > 1) {
					// TODO Check type compatibility
//					LOG.err("703 "+vte.getName()+" "+vte.potentialTypes());
					errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				} else {
					// potential_size == 0
					// Result is handled by phase.typeDecideds, self is always valid
					if (/*vte.getName() != null &&*/ !(vte.vtt == VariableTableType.RESULT || vte.vtt == VariableTableType.SELF))
						errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				}
			} else if (vte.vtt == VariableTableType.RESULT) {
				final OS_Type attached = vte.type.getAttached();
				if (attached.getType() == OS_Type.Type.USER) {
					try {
						vte.type.setAttached(resolve_type(attached, fd_ctx));
					} catch (ResolveError aResolveError) {
						aResolveError.printStackTrace();
						assert false;
					}
				}
			}
		}
		{
			//
			// NOW CALCULATE DEFERRED CALLS
			//
			for (final Integer deferred_call : generatedFunction.deferred_calls) {
				final Instruction instruction = generatedFunction.getInstruction(deferred_call);

				final int i1 = to_int(instruction.getArg(0));
				final InstructionArgument i2 = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
//					generatedFunction.deferred_calls.remove(deferred_call);
					implement_calls_(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
			}
		}
	}

	public void do_assign_normal(final @NotNull BaseGeneratedFunction generatedFunction, final Context aFd_ctx, final @NotNull Instruction instruction, final Context aContext) {
		// TODO doesn't account for __assign__
		final InstructionArgument agn_lhs = instruction.getArg(0);
		if (agn_lhs instanceof IntegerIA) {
			final @NotNull IntegerIA arg = (IntegerIA) agn_lhs;
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
			final InstructionArgument i2 = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				vte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof FnCallArgs) {
				final @NotNull FnCallArgs fca = (FnCallArgs) i2;
				do_assign_call(generatedFunction, aContext, vte, fca, instruction);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, vte, (ConstTableIA) i2);
			} else if (i2 instanceof IdentIA) {
				@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(to_int(i2));
				if (idte.type == null) {
					final IdentIA identIA = new IdentIA(idte.getIndex(), generatedFunction);
					resolveIdentIA_(aContext, identIA, generatedFunction, new FoundElement(phase) {

						@Override
						public void foundElement(final OS_Element e) {
							found_element_for_ite(generatedFunction, idte, e, aContext);
						}

						@Override
						public void noFoundElement() {
							// TODO: log error
						}
					});
				}
				assert idte.type != null;
				assert idte.getResolvedElement() != null;
				vte.addPotentialType(instruction.getIndex(), idte.type);
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		} else if (agn_lhs instanceof IdentIA) {
			final @NotNull IdentIA arg = (IdentIA) agn_lhs;
			final @NotNull IdentTableEntry idte = arg.getEntry();
			final InstructionArgument i2 = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				idte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof FnCallArgs) {
				final @NotNull FnCallArgs fca = (FnCallArgs) i2;
				do_assign_call(generatedFunction, aFd_ctx, idte, fca, instruction.getIndex());
			} else if (i2 instanceof IdentIA) {
				if (idte.getResolvedElement() instanceof VariableStatement) {
					do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte);
				}
				@NotNull IdentTableEntry idte2 = generatedFunction.getIdentTableEntry(to_int(i2));
				do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte2);
				idte.addPotentialType(instruction.getIndex(), idte2.type);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, idte, (ConstTableIA) i2);
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		}
	}

	public void do_assign_normal_ident_deferred(final @NotNull BaseGeneratedFunction generatedFunction,
												final @NotNull Context aContext,
												final @NotNull IdentTableEntry aIdentTableEntry) {
		if (aIdentTableEntry.type == null) {
			aIdentTableEntry.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, (OS_Type) null);
		}
		LookupResultList lrl1 = aContext.lookup(aIdentTableEntry.getIdent().getText());
		@Nullable OS_Element best = lrl1.chooseBest(null);
		if (best != null) {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
			// TODO check for elements which may contain type information
			if (best instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) best;
				do_assign_normal_ident_deferred_VariableStatement(generatedFunction, aIdentTableEntry, vs);
			} else if (best instanceof FormalArgListItem) {
				final FormalArgListItem fali = (FormalArgListItem) best;
				do_assign_normal_ident_deferred_FALI(generatedFunction, aIdentTableEntry, fali);
			} else
				throw new NotImplementedException();
		} else {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
			LOG.err("242 Bad lookup" + aIdentTableEntry.getIdent().getText());
		}
	}

	private void do_assign_normal_ident_deferred_FALI(final BaseGeneratedFunction generatedFunction, final IdentTableEntry aIdentTableEntry, final FormalArgListItem fali) {
		GenType genType = new GenType();
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null) {
			invocation = generatedFunction.fi.getClassInvocation();
			genType.resolved = ((ClassInvocation) invocation).getKlass().getOS_Type();
		} else {
			invocation = generatedFunction.fi.getNamespaceInvocation();
			genType.resolvedn = ((NamespaceInvocation) invocation).getNamespace();
		}
		genType.ci = invocation;
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(fali.name());
		assert vte_ia != null;
		((IntegerIA) vte_ia).getEntry().typeResolvePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		generatedFunction.addDependentType(genType);
	}

	public void do_assign_normal_ident_deferred_VariableStatement(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull IdentTableEntry aIdentTableEntry, final @NotNull VariableStatement vs) {
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null)
			invocation = generatedFunction.fi.getClassInvocation();
		else
			invocation = generatedFunction.fi.getNamespaceInvocation();
		@NotNull DeferredMember dm = deferred_member(vs.getParent().getParent(), invocation, vs, aIdentTableEntry);
		dm.typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		GenType genType = new GenType();
		genType.ci = dm.getInvocation();
		if (genType.ci instanceof NamespaceInvocation) {
			genType.resolvedn = ((NamespaceInvocation) genType.ci).getNamespace();
		} else if (genType.ci instanceof ClassInvocation) {
			genType.resolved = ((ClassInvocation) genType.ci).getKlass().getOS_Type();
		} else {
			throw new IllegalStateException();
		}
		generatedFunction.addDependentType(genType);
	}

	private void implement_is_a(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction instruction) {
		final IntegerIA testing_var_  = (IntegerIA) instruction.getArg(0);
		final IntegerIA testing_type_ = (IntegerIA) instruction.getArg(1);
		final Label target_label      = ((LabelIA) instruction.getArg(2)).label;

		final VariableTableEntry testing_var    = gf.getVarTableEntry(testing_var_.getIndex());
		final TypeTableEntry     testing_type__ = gf.getTypeTableEntry(testing_type_.getIndex());

		GenType genType = testing_type__.genType;
		if (genType.resolved == null) {
			try {
				genType.resolved = resolve_type(genType.typeName, gf.getFD().getContext()).resolved;
			} catch (ResolveError aResolveError) {
//				aResolveError.printStackTrace();
				errSink.reportDiagnostic(aResolveError);
				return;
			}
		}
		if (genType.ci == null) {
			genType.genCI(genType.nonGenericTypeName, this, errSink, phase);
		}
		if (genType.node == null) {
			if (genType.ci instanceof ClassInvocation) {
				WlGenerateClass gen = new WlGenerateClass(getGenerateFunctions(module), (ClassInvocation) genType.ci, phase.generatedClasses);
				gen.run(null);
				genType.node = gen.getResult();
			} else if (genType.ci instanceof NamespaceInvocation) {
				WlGenerateNamespace gen = new WlGenerateNamespace(getGenerateFunctions(module), (NamespaceInvocation) genType.ci, phase.generatedClasses);
				gen.run(null);
				genType.node = gen.getResult();
			}
		}
		GeneratedNode testing_type = testing_type__.resolved();
		assert testing_type != null;
	}

	public void onEnterFunction(final @NotNull BaseGeneratedFunction generatedFunction, final Context aContext) {
		for (VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
			variableTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		}
		for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
			identTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		}
		for (ProcTableEntry procTableEntry : generatedFunction.prte_list) {
			procTableEntry.setDeduceTypes2(this, aContext, generatedFunction, errSink);
		}
		//
		// resolve all cte expressions
		//
		for (final @NotNull ConstantTableEntry cte : generatedFunction.cte_list) {
			resolve_cte_expression(cte, aContext);
		}
		//
		// add proc table listeners
		//
		add_proc_table_listeners(generatedFunction);
		//
		// resolve ident table
		//
		for (@NotNull IdentTableEntry ite : generatedFunction.idte_list) {
			ite.resolveExpectation = promiseExpectation(ite, "Element Resolved");
			resolve_ident_table_entry(ite, generatedFunction, aContext);
		}
		//
		// resolve arguments table
		//
		@NotNull Resolve_Variable_Table_Entry rvte = new Resolve_Variable_Table_Entry(generatedFunction, aContext, this);
		@NotNull DeduceTypes2.IVariableConnector connector;
		if (generatedFunction instanceof GeneratedConstructor) {
			connector = new CtorConnector((GeneratedConstructor) generatedFunction);
		} else {
			connector = new NullConnector();
		}
		for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			rvte.action(vte, connector);
		}
	}

	public void onExitFunction(final @NotNull BaseGeneratedFunction generatedFunction, final Context aFd_ctx, final Context aContext) {
		//
		// resolve var table. moved from `E'
		//
		for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			vte.resolve_var_table_entry_for_exit_function();
		}
		for (@NotNull Runnable runnable : onRunnables) {
			runnable.run();
		}
//					LOG.info("167 "+generatedFunction);
		//
		// ATTACH A TYPE TO VTE'S
		// CONVERT USER TYPES TO USER_CLASS TYPES
		//
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
//						LOG.info("704 "+vte.type.attached+" "+vte.potentialTypes());
			final OS_Type attached = vte.type.getAttached();
			if (attached != null && attached.getType() == OS_Type.Type.USER) {
				final TypeName x = attached.getTypeName();
				if (x instanceof NormalTypeName) {
					final String tn = ((NormalTypeName) x).getName();
					final LookupResultList lrl = x.getContext().lookup(tn);
					@Nullable OS_Element best = lrl.chooseBest(null);
					if (best != null) {
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias((AliasStatement) best, this);
						}
						if (!(OS_Type.isConcreteType(best))) {
							errSink.reportError(String.format("Not a concrete type %s for (%s)", best, tn));
						} else {
//									LOG.info("705 " + best);
							// NOTE that when we set USER_CLASS from USER generic information is
							// still contained in constructable_pte
							@NotNull GenType genType = new GenType(attached, ((ClassStatement) best).getOS_Type(), true, x, this, errSink, phase);
							vte.type.genType.copy(genType);
							// set node when available
							((ClassInvocation) vte.type.genType.ci).resolvePromise().done(new DoneCallback<GeneratedClass>() {
								@Override
								public void onDone(GeneratedClass result) {
									vte.type.genType.node = result;
									vte.resolveTypeToClass(result);
									vte.genType = vte.type.genType; // TODO who knows if this is necessary?
								}
							});
						}
						//vte.el = best;
						// NOTE we called resolve_var_table_entry above
						LOG.err("200 "+best);
						if (vte.getResolvedElement() != null)
							assert vte.getStatus() == BaseTableEntry.Status.KNOWN;
//									vte.setStatus(BaseTableEntry.Status.KNOWN, best/*vte.el*/);
					} else {
						errSink.reportDiagnostic(new ResolveError(x, lrl));
					}
				}
			}
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.vtt == VariableTableType.ARG) {
				final OS_Type attached = vte.type.getAttached();
				if (attached != null) {
					if (attached.getType() == OS_Type.Type.USER)
						//throw new AssertionError();
						errSink.reportError("369 ARG USER type (not deduced) "+vte);
				} else {
					errSink.reportError("457 ARG type not deduced/attached "+vte);
				}
			}
		}
		//
		// ATTACH A TYPE TO IDTE'S
		//
		for (@NotNull IdentTableEntry ite : generatedFunction.idte_list) {
			int y=2;
			assign_type_to_idte(ite, generatedFunction, aFd_ctx, aContext);
		}
		{
			final @NotNull WorkManager workManager = wm;//new WorkManager();
			@NotNull DeduceTypes2.Dependencies deps = new Dependencies(/*phase, this, errSink*/workManager);
			deps.subscribeTypes(generatedFunction.dependentTypesSubject());
			deps.subscribeFunctions(generatedFunction.dependentFunctionSubject());
//						for (@NotNull GenType genType : generatedFunction.dependentTypes()) {
//							deps.action_type(genType, workManager);
//						}
//						for (@NotNull FunctionInvocation dependentFunction : generatedFunction.dependentFunctions()) {
//							deps.action_function(dependentFunction, workManager);
//						}
			int x = workManager.totalSize();

			workManager.drain();
		}
		//
		// RESOLVE FUNCTION RETURN TYPES
		//
		resolve_function_return_type(generatedFunction);
		{
			for (VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
				final @NotNull Collection<TypeTableEntry> pot = variableTableEntry.potentialTypes();
				int y=2;
				if (pot.size() == 1 && variableTableEntry.genType.isNull()) {
					final OS_Type x = pot.iterator().next().getAttached();
					if (x != null)
						if (x.getType() == OS_Type.Type.USER_CLASS) {
							try {
								final @NotNull GenType yy = resolve_type(x, aFd_ctx);
								// HACK TIME
								if (yy.resolved == null && yy.typeName.getType()== OS_Type.Type.USER_CLASS) {
									yy.resolved = yy.typeName;
									yy.typeName = null;
								}

								yy.genCIForGenType2(this);
								variableTableEntry.resolveType(yy);
								variableTableEntry.resolveTypeToClass(yy.node);
//								variableTableEntry.dlv.type.resolve(yy);
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
					}
				}
			}
		}
		//
		// LOOKUP FUNCTIONS
		//
		{
			@NotNull DeduceTypes2.Lookup_function_on_exit lfoe = new Lookup_function_on_exit();
			for (@NotNull ProcTableEntry pte : generatedFunction.prte_list) {
				lfoe.action(pte);
			}
			wm.drain();
		}

		expectations.check();
	}

	interface IVariableConnector {
		void connect(VariableTableEntry aVte, String aName);
	}

	static class CtorConnector implements IVariableConnector {
		private final GeneratedConstructor generatedConstructor;

		public CtorConnector(final GeneratedConstructor aGeneratedConstructor) {
			generatedConstructor = aGeneratedConstructor;
		}

		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
			final List<GeneratedContainer.VarTableEntry> vt = ((GeneratedClass) generatedConstructor.getGenClass()).varTable;
			for (GeneratedContainer.VarTableEntry gc_vte : vt) {
				if (gc_vte.nameToken.getText().equals(aName)) {
					gc_vte.connect(aVte, generatedConstructor);
					break;
				}
			}
		}
	}

	static class NullConnector implements IVariableConnector {
		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
		}
	}

	private void add_proc_table_listeners(@NotNull BaseGeneratedFunction generatedFunction) {
		for (final @NotNull ProcTableEntry pte : generatedFunction.prte_list) {
			pte.addStatusListener(new ProcTableListener(pte, generatedFunction, new DeduceClient2(this)));

			InstructionArgument en = pte.expression_num;
			if (en != null) {
				if (en instanceof IdentIA) {
					final @NotNull IdentIA identIA = (IdentIA) en;
					@NotNull IdentTableEntry idte = identIA.getEntry();
					idte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case NAMESPACE:
								@NotNull GenType genType = new GenType((NamespaceStatement) el);
								generatedFunction.addDependentType(genType);
								break;
							case CLASS:
								@NotNull GenType genType2 = new GenType((ClassStatement) el);
								generatedFunction.addDependentType(genType2);
								break;
							case FUNCTION:
								@Nullable IdentIA identIA2 = null;
								if (pte.expression_num instanceof IdentIA)
									identIA2 = (IdentIA) pte.expression_num;
								if (identIA2 != null) {
									@NotNull IdentTableEntry idte2 = identIA.getEntry();
									@Nullable ProcTableEntry procTableEntry = idte2.getCallablePTE();
//									if (procTableEntry == pte) System.err.println("940 procTableEntry == pte");
									if (procTableEntry != null) {
										// TODO doesn't seem like we need this
										procTableEntry.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
											@Override
											public void onDone(@NotNull FunctionInvocation functionInvocation) {
												ClassInvocation ci = functionInvocation.getClassInvocation();
												NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
												// do we register?? probably not
												if (ci == null && nsi == null)
													assert false;
												@NotNull FunctionInvocation fi = newFunctionInvocation((FunctionDef) el, pte, ci != null ? ci : nsi, phase);

												{
													if (functionInvocation.getClassInvocation() == fi.getClassInvocation() &&
														functionInvocation.getFunction() == fi.getFunction() &&
														functionInvocation.pte == fi.pte) {
//														System.err.println("955 It seems like we are generating the same thing...");
													} else {
														int ok=2;
													}

												}
												generatedFunction.addDependentFunction(fi);
											}
										});
										// END
									}
								}
								break;
							case CONSTRUCTOR:
								int y=2;
								break;
							default:
//								LOG.err(String.format("228 Don't know what to do %s %s", type, el));
								break;
							}
						}
					});
				} else if (en instanceof IntegerIA) {
					// TODO this code does nothing so commented out
/*
					final @NotNull IntegerIA integerIA = (IntegerIA) en;
					@NotNull VariableTableEntry vte = integerIA.getEntry();
					vte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							@NotNull VariableTableEntry vte2 = vte;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case VAR:
								break;
							default:
								throw new NotImplementedException();
							}
						}
					});
*/
				} else
					throw new NotImplementedException();
			}
		}
	}

	@NotNull FunctionInvocation newFunctionInvocation(BaseFunctionDef aFunctionDef, ProcTableEntry aPte, @NotNull IInvocation aInvocation, @NotNull DeducePhase aDeducePhase) {
		@NotNull FunctionInvocation fi = new FunctionInvocation(aFunctionDef, aPte, aInvocation, aDeducePhase.generatePhase);
		// TODO register here
		return fi;
	}

	public String getFileName() {
		return module.getFileName();
	}

	public @NotNull GenerateFunctions getGenerateFunctions(@NotNull OS_Module aModule) {
		return phase.generatePhase.getGenerateFunctions(aModule);
	}

	class WlDeduceFunction implements WorkJob {
		private final WorkJob workJob;
		private final List<BaseGeneratedFunction> coll;
		private boolean _isDone;

		public WlDeduceFunction(final WorkJob aWorkJob, List<BaseGeneratedFunction> aColl) {
			workJob = aWorkJob;
			coll = aColl;
		}

		@Override
		public void run(final WorkManager aWorkManager) {
			// TODO assumes result is in the same file as this (DeduceTypes2)

			if (workJob instanceof WlGenerateFunction) {
				final GeneratedFunction generatedFunction1 = ((WlGenerateFunction) workJob).getResult();
				if (!coll.contains(generatedFunction1)) {
					coll.add(generatedFunction1);
					if (!generatedFunction1.deducedAlready) {
						deduce_generated_function(generatedFunction1);
					}
					generatedFunction1.deducedAlready= true;
				}
			} else if (workJob instanceof WlGenerateDefaultCtor) {
				final GeneratedConstructor generatedConstructor = (GeneratedConstructor) ((WlGenerateDefaultCtor) workJob).getResult();
				if (!coll.contains(generatedConstructor)) {
					coll.add(generatedConstructor);
					if (!generatedConstructor.deducedAlready) {
						deduce_generated_constructor(generatedConstructor);
					}
					generatedConstructor.deducedAlready= true;
				}
			} else if (workJob instanceof WlGenerateCtor) {
				final GeneratedConstructor generatedConstructor = ((WlGenerateCtor) workJob).getResult();
				if (!coll.contains(generatedConstructor)) {
					coll.add(generatedConstructor);
					if (!generatedConstructor.deducedAlready) {
						deduce_generated_constructor(generatedConstructor);
					}
					generatedConstructor.deducedAlready= true;
				}
			} else
				throw new NotImplementedException();

			assert coll.size() == 1;

			_isDone = true;
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}
	}

	class Dependencies {
		final WorkList wl = new WorkList();
		final WorkManager wm;

		Dependencies(final WorkManager aWm) {
			wm = aWm;
		}

		public void action_type(@NotNull GenType genType) {
			// TODO work this out further, maybe like a Deepin flavor
			if (genType.resolvedn != null) {
				@NotNull OS_Module mod = genType.resolvedn.getContext().module();
				final @NotNull GenerateFunctions gf = phase.generatePhase.getGenerateFunctions(mod);
				NamespaceInvocation ni = phase.registerNamespaceInvocation(genType.resolvedn);
				@NotNull WlGenerateNamespace gen = new WlGenerateNamespace(gf, ni, phase.generatedClasses);

				assert genType.ci == null || genType.ci == ni;
				genType.ci = ni;

				wl.addJob(gen);

				ni.resolvePromise().then(new DoneCallback<GeneratedNamespace>() {
					@Override
					public void onDone(final GeneratedNamespace result) {
						genType.node = result;
						result.dependentTypes().add(genType);
					}
				});
			} else if (genType.resolved != null) {
				if (genType.functionInvocation != null) {
					action_function(genType.functionInvocation);
					return;
				}

				final ClassStatement c = genType.resolved.getClassOf();
				final @NotNull OS_Module mod = c.getContext().module();
				final @NotNull GenerateFunctions gf = phase.generatePhase.getGenerateFunctions(mod);
				@Nullable ClassInvocation ci;
				if (genType.ci == null) {
					ci = new ClassInvocation(c, null);
					ci = phase.registerClassInvocation(ci);

					genType.ci = ci;
				} else {
					assert genType.ci instanceof ClassInvocation;
					ci = (ClassInvocation) genType.ci;
				}

				final Promise<ClassDefinition, Diagnostic, Void> pcd = phase.generateClass(gf, ci);

				pcd.then(new DoneCallback<ClassDefinition>() {
					@Override
					public void onDone(final ClassDefinition result) {
						final GeneratedClass genclass = result.getNode();

						genType.node = genclass;
						genclass.dependentTypes().add(genType);
					}
				});
			}
			//
			wm.addJobs(wl);
		}

		public void action_function(@NotNull FunctionInvocation aDependentFunction) {
			final BaseFunctionDef function = aDependentFunction.getFunction();
			WorkJob gen;
			final @NotNull OS_Module mod;
			if (function == ConstructorDef.defaultVirtualCtor) {
				ClassInvocation ci = aDependentFunction.getClassInvocation();
				if (ci == null) {
					NamespaceInvocation ni = aDependentFunction.getNamespaceInvocation();
					if (ni == null)
						assert false;
					mod = ni.getNamespace().getContext().module();

					ni.resolvePromise().then(new DoneCallback<GeneratedNamespace>() {
						@Override
						public void onDone(final GeneratedNamespace result) {
							result.dependentFunctions().add(aDependentFunction);
						}
					});
				} else {
					mod = ci.getKlass().getContext().module();
					ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							result.dependentFunctions().add(aDependentFunction);
						}
					});
				}
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateDefaultCtor(gf, aDependentFunction);
			} else {
				mod = function.getContext().module();
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateFunction(gf, aDependentFunction);
			}
			wl.addJob(gen);
			List<BaseGeneratedFunction> coll = new ArrayList<>();
			wl.addJob(new WlDeduceFunction(gen, coll));
			wm.addJobs(wl);
		}

		public void subscribeTypes(final Subject<GenType> aDependentTypesSubject) {
			aDependentTypesSubject.subscribe(new Observer<GenType>() {
				@Override
				public void onSubscribe(@NonNull final Disposable d) {

				}

				@Override
				public void onNext(final GenType aGenType) {
					action_type(aGenType);
				}

				@Override
				public void onError(final Throwable aThrowable) {

				}

				@Override
				public void onComplete() {

				}
			});
		}

		public void subscribeFunctions(final Subject<FunctionInvocation> aDependentFunctionSubject) {
			aDependentFunctionSubject.subscribe(new Observer<FunctionInvocation>() {
				@Override
				public void onSubscribe(@NonNull final Disposable d) {

				}

				@Override
				public void onNext(@NonNull final FunctionInvocation aFunctionInvocation) {
					action_function(aFunctionInvocation);
				}

				@Override
				public void onError(@NonNull final Throwable e) {

				}

				@Override
				public void onComplete() {

				}
			});
		}
	}

	private void resolve_cte_expression(@NotNull ConstantTableEntry cte, Context aContext) {
		final IExpression initialValue = cte.initialValue;
		switch (initialValue.getKind()) {
		case NUMERIC:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemInteger);
			break;
		case STRING_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.String_);
			break;
		case CHAR_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemCharacter);
			break;
		case IDENT:
			{
				final OS_Type a = cte.getTypeTableEntry().getAttached();
				if (a != null) {
					assert a.getType() != null;
					if (a.getType() == OS_Type.Type.BUILT_IN && a.getBType() == BuiltInTypes.Boolean) {
						assert BuiltInTypes.isBooleanText(cte.getName());
					} else
						throw new NotImplementedException();
				} else {
					assert false;
				}
				break;
			}
		default:
			{
				LOG.err("8192 "+initialValue.getKind());
				throw new NotImplementedException();
			}
		}
	}

	private void resolve_cte_expression_builtin(@NotNull ConstantTableEntry cte, Context aContext, BuiltInTypes aBuiltInType) {
		final OS_Type a = cte.getTypeTableEntry().getAttached();
		if (a == null || a.getType() != OS_Type.Type.USER_CLASS) {
			try {
				cte.getTypeTableEntry().setAttached(resolve_type(new OS_Type(aBuiltInType), aContext));
			} catch (ResolveError resolveError) {
				System.out.println("117 Can't be here");
//				resolveError.printStackTrace(); // TODO print diagnostic
			}
		}
	}

	class Lookup_function_on_exit {
		@NotNull WorkList wl = new WorkList();

		public void action(@NotNull ProcTableEntry pte) {
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) return;

			if (fi.getFunction() == null) {
				if (fi.pte == null) {
					return;
				} else {
//					LOG.err("592 " + fi.getClassInvocation());
					if (fi.pte.getClassInvocation() != null)
						fi.setClassInvocation(fi.pte.getClassInvocation());
//					else
//						fi.pte.setClassInvocation(fi.getClassInvocation());
				}
			}

			@Nullable ClassInvocation ci = fi.getClassInvocation();
			BaseFunctionDef fd3 = fi.getFunction();
			if (ci == null) {
				ci = fi.pte.getClassInvocation();
			}
			if (fd3 == ConstructorDef.defaultVirtualCtor) {
				if (ci == null) {
					if (/*fi.getClassInvocation() == null &&*/ fi.getNamespaceInvocation() == null) {
						// Assume default constructor
						ci = new ClassInvocation((ClassStatement) pte.getResolvedElement(), null);
						ci = phase.registerClassInvocation(ci);
						fi.setClassInvocation(ci);
					} else
						throw new NotImplementedException();
				}
				final ClassStatement klass = ci.getKlass();

				Collection<ConstructorDef> cis = klass.getConstructors();
				for (@NotNull ConstructorDef constructorDef : cis) {
					final Iterable<FormalArgListItem> constructorDefArgs = constructorDef.getArgs();

					if (!constructorDefArgs.iterator().hasNext()) { // zero-sized arg list
						fd3 = constructorDef;
						break;
					}
				}
			}

			final OS_Element parent;
			if (fd3 != null) {
				parent = fd3.getParent();
				if (parent instanceof ClassStatement) {
					if (ci != pte.getClassInvocation()) {
						ci = new ClassInvocation((ClassStatement) parent, null);
						{
							final ClassInvocation classInvocation = pte.getClassInvocation();
							if (classInvocation != null) {
								Map<TypeName, OS_Type> gp = classInvocation.genericPart;
								if (gp != null) {
									int i = 0;
									for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
										ci.set(i, entry.getKey(), entry.getValue());
										i++;
									}
								}
							}
						}
					}
					proceed(fi, ci, (ClassStatement) parent, wl);
				} else if (parent instanceof NamespaceStatement) {
					proceed(fi, (NamespaceStatement) parent, wl);
				}
			} else {
				parent = ci.getKlass();
				{
					final ClassInvocation classInvocation = pte.getClassInvocation();
					if (classInvocation != null && classInvocation.genericPart != null) {
						Map<TypeName, OS_Type> gp = classInvocation.genericPart;
						int i = 0;
						for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
							ci.set(i, entry.getKey(), entry.getValue());
							i++;
						}
					}
				}
				proceed(fi, ci, (ClassStatement) parent, wl);
			}

//			proceed(fi, ci, parent);
		}

		void proceed(@NotNull FunctionInvocation fi, ClassInvocation ci, ClassStatement aParent, @NotNull WorkList wl) {
			ci = phase.registerClassInvocation(ci);

			ClassStatement kl = ci.getKlass(); // TODO Don't you see aParent??
			assert kl != null;

			final BaseFunctionDef fd2 = fi.getFunction();
			int state = 0;

			if (fd2 == ConstructorDef.defaultVirtualCtor) {
				if (fi.pte.getArgs().size() == 0)
					state = 1;
				else
					state = 2;
			} else if (fd2 instanceof ConstructorDef) {
				if (fi.getClassInvocation().getConstructorName() != null)
					state = 3;
				else
					state = 2;
			} else {
				if (fi.getFunction() == null && fi.getClassInvocation() != null)
					state = 3;
				else
					state = 4;
			}

			switch (state) {
			case 1:
				assert fi.pte.getArgs().size() == 0;
				// default ctor
				wl.addJob(new WlGenerateDefaultCtor(phase.generatePhase.getGenerateFunctions(module), fi));
				break;
			case 2:
				wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, fd2.getNameNode()));
				break;
			case 3:
				// README this is a special case to generate constructor
				// TODO should it be GenerateDefaultCtor? (check args size and ctor-name)
				final String constructorName = fi.getClassInvocation().getConstructorName();
				final @NotNull IdentExpression constructorName1 = constructorName != null ? IdentExpression.forString(constructorName) : null;
				wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, constructorName1));
				break;
			case 4:
				wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module), fi));
				break;
			default:
				throw new NotImplementedException();
			}

			wm.addJobs(wl);
		}

		void proceed(@NotNull FunctionInvocation fi, @NotNull NamespaceStatement aParent, @NotNull WorkList wl) {
//			ci = phase.registerClassInvocation(ci);

			final @NotNull OS_Module module1 = aParent.getContext().module();

			final NamespaceInvocation nsi = phase.registerNamespaceInvocation(aParent);

			wl.addJob(new WlGenerateNamespace(phase.generatePhase.getGenerateFunctions(module1), nsi, phase.generatedClasses));
			wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module1), fi));

			wm.addJobs(wl);
		}
	}

	public void assign_type_to_idte(@NotNull IdentTableEntry ite,
									@NotNull BaseGeneratedFunction generatedFunction,
									Context aFunctionContext,
									@NotNull Context aContext) {
		if (!ite.hasResolvedElement()) {
			@NotNull IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
			resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

				final String path = generatedFunction.getIdentIAPathNormal(ident_a);

				@Override
				public void foundElement(OS_Element x) {
					if (ite.getResolvedElement() != x)
						ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
					if (ite.type != null && ite.type.getAttached() != null) {
						switch (ite.type.getAttached().getType()) {
						case USER:
							try {
								@NotNull GenType xx = resolve_type(ite.type.getAttached(), aFunctionContext);
								ite.type.setAttached(xx);
							} catch (ResolveError resolveError) {
								LOG.info("192 Can't attach type to " + path);
								errSink.reportDiagnostic(resolveError);
							}
							if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
								use_user_class(ite.type.getAttached(), ite);
							}
							break;
						case USER_CLASS:
							use_user_class(ite.type.getAttached(), ite);
							break;
						case FUNCTION:
							{
								// TODO All this for nothing
								//  the ite points to a function, not a function call,
								//  so there is no point in resolving it
								if (ite.type.tableEntry instanceof ProcTableEntry) {
									final @NotNull ProcTableEntry pte = (ProcTableEntry) ite.type.tableEntry;

								} else if (ite.type.tableEntry instanceof IdentTableEntry) {
									final @NotNull IdentTableEntry identTableEntry = (IdentTableEntry) ite.type.tableEntry;
									if (identTableEntry.getCallablePTE() != null) {
										@Nullable ProcTableEntry cpte = identTableEntry.getCallablePTE();
										cpte.typePromise().then(new DoneCallback<GenType>() {
											@Override
											public void onDone(@NotNull GenType result) {
												System.out.println("1483 "+result.resolved+" "+result.node);
											}
										});
									}
								}
							}
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + ite.type.getAttached().getType());
						}
					} else {
						int yy=2;
						if (!ite.hasResolvedElement()) {
							@Nullable LookupResultList lrl = null;
							try {
								lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, DeduceTypes2.this);
								@Nullable OS_Element best = lrl.chooseBest(null);
								if (best != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
									if (ite.type != null && ite.type.getAttached() != null) {
										if (ite.type.getAttached().getType() == OS_Type.Type.USER) {
											try {
												@NotNull GenType xx = resolve_type(ite.type.getAttached(), aFunctionContext);
												ite.type.setAttached(xx);
											} catch (ResolveError resolveError) { // TODO double catch
												LOG.info("210 Can't attach type to "+ite.getIdent());
												errSink.reportDiagnostic(resolveError);
//												continue;
											}
										}
									}
								} else {
									LOG.err("184 Couldn't resolve "+ite.getIdent());
								}
							} catch (ResolveError aResolveError) {
								LOG.err("184-506 Couldn't resolve "+ite.getIdent());
								aResolveError.printStackTrace();
							}
							if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
								use_user_class(ite.type.getAttached(), ite);
							}
						}
					}
				}

				private void use_user_class(@NotNull OS_Type aType, @NotNull IdentTableEntry aEntry) {
					final ClassStatement cs = aType.getClassOf();
					if (aEntry.constructable_pte != null) {
						int yyy=3;
						System.out.println("use_user_class: "+cs);
					}
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					errSink.reportError("165 Can't resolve "+path);
				}
			});
		}
	}

	public void resolve_ident_table_entry(@NotNull IdentTableEntry ite, @NotNull BaseGeneratedFunction generatedFunction, Context ctx) {
		new Resolve_ident_table_entry(this).act(ite, generatedFunction, ctx);
	}

	public void resolve_var_table_entry(@NotNull VariableTableEntry vte, BaseGeneratedFunction generatedFunction, Context ctx) {
		new Resolve_var_table_entry(this).act(vte, generatedFunction, ctx);
	}

//	private GeneratedNode makeNode(GenType aGenType) {
//		if (aGenType.ci instanceof ClassInvocation) {
//			final ClassInvocation ci = (ClassInvocation) aGenType.ci;
//			@NotNull GenerateFunctions gen = phase.generatePhase.getGenerateFunctions(ci.getKlass().getContext().module());
//			WlGenerateClass wlgc = new WlGenerateClass(gen, ci, phase.generatedClasses);
//			wlgc.run(null);
//			return wlgc.getResult();
//		}
//		return null;
//	}

	void implement_construct(BaseGeneratedFunction generatedFunction, Instruction instruction) {
		final @NotNull Implement_construct ic = newImplement_construct(generatedFunction, instruction);
		ic.action();
	}

	@NotNull
	public Implement_construct newImplement_construct(BaseGeneratedFunction generatedFunction, Instruction instruction) {
		return new Implement_construct(this, generatedFunction, instruction);
	}

	void resolve_function_return_type(@NotNull BaseGeneratedFunction generatedFunction) {
		final GenType gt = resolve_function_return_type_int(generatedFunction);
		if (gt != null)
			//phase.typeDecided((GeneratedFunction) generatedFunction, gt);
			generatedFunction.resolveTypeDeferred(gt);
	}

	private @Nullable GenType resolve_function_return_type_int(final @NotNull BaseGeneratedFunction generatedFunction) {
		// TODO what about resolved?
		@NotNull GenType unitType = new GenType();
		unitType.typeName = new OS_Type(BuiltInTypes.Unit);

		// MODERNIZATION Does this have any affinity with DeferredMember?
		@Nullable final InstructionArgument vte_index = generatedFunction.vte_lookup("Result");
		if (vte_index != null) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(vte_index));

			if (vte.type.getAttached() != null) {
				vte.resolveType(vte.type.genType); // TODO doesn't fit pattern of returning and then setting
				return vte.type.genType;
			} else {
				@NotNull Collection<TypeTableEntry> pot1 = vte.potentialTypes();
				@NotNull ArrayList<TypeTableEntry> pot = new ArrayList<TypeTableEntry>(pot1);
				if (pot.size() == 1) {
					return pot.get(0).genType;
				} else if (pot.size() == 0) {
					return unitType;
				} else {
					// TODO report some kind of error/diagnostic and/or let ForFunction know...
					errSink.reportWarning("Can't resolve type of `Result'. potentialTypes > 1 for "+vte);
				}
			}
		} else {
			if (generatedFunction instanceof GeneratedConstructor) {
				// cant set return type of constructors
			} else {
				// if Result is not present, then make function return Unit
				// TODO May not be correct in all cases, such as when Value is present
				// but works for current code structure, where Result is a always present
				return unitType;
			}
		}
		return null;
	}

	public static class DeduceClient1 {
		private final DeduceTypes2 dt2;

		@Contract(pure = true)
		public DeduceClient1(DeduceTypes2 aDeduceTypes2) {
			dt2 = aDeduceTypes2;
		}

		public @Nullable OS_Element _resolveAlias(@NotNull AliasStatement aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, dt2);
		}

		public void found_element_for_ite(BaseGeneratedFunction aGeneratedFunction, @NotNull IdentTableEntry aIte, OS_Element aX, Context aCtx) {
			dt2.found_element_for_ite(aGeneratedFunction, aIte, aX, aCtx);
		}

		public @NotNull GenType resolve_type(@NotNull OS_Type aType, Context aCtx) throws ResolveError {
			return dt2.resolve_type(aType, aCtx);
		}

		public @Nullable IInvocation getInvocationFromBacklink(InstructionArgument aInstructionArgument) {
			return dt2.getInvocationFromBacklink(aInstructionArgument);
		}

		public @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatement aVariableStatement, @NotNull IdentTableEntry aIdentTableEntry) {
			return dt2.deferred_member(aParent, aInvocation, aVariableStatement, aIdentTableEntry);
		}

		public void genCI(final GenType aResult, final TypeName aNonGenericTypeName) {
			aResult.genCI(aNonGenericTypeName, dt2, dt2.errSink, dt2.phase);
		}

		public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String aS) {
			return dt2.phase.registerClassInvocation(aClassStatement, aS);
		}

		public void genCIForGenType2(final GenType genType) {
			genType.genCIForGenType2(dt2);
		}


		public void LOG_err(String aS) {
			dt2.LOG.err(aS);
		}

		public @NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(VariableTableEntry aVte) {
			return dt2.getPotentialTypesVte(aVte);
		}
	}

	void found_element_for_ite(BaseGeneratedFunction generatedFunction, @NotNull IdentTableEntry ite, @Nullable OS_Element y, Context ctx) {
		if (y != ite.getResolvedElement())
			System.err.println(String.format("2571 Setting FoundElement for ite %s to %s when it is already %s", ite, y, ite.getResolvedElement()));

		@NotNull Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(this));
		fefi.action(ite);
	}

	private @Nullable IInvocation getInvocationFromBacklink(@Nullable InstructionArgument aBacklink) {
		if (aBacklink == null) return null;
		// TODO implement me
		return null;
	}

	private @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatement aVariableStatement, @NotNull IdentTableEntry ite) {
		@NotNull DeferredMember dm = deferred_member(aParent, aInvocation, aVariableStatement);
		dm.externalRef().then(new DoneCallback<GeneratedNode>() {
			@Override
			public void onDone(GeneratedNode result) {
				ite.externalRef = result;
			}
		});
		return dm;
	}

	private @Nullable DeferredMember deferred_member(OS_Element aParent, @Nullable IInvocation aInvocation, VariableStatement aVariableStatement) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
		}
		@Nullable DeferredMember dm = new DeferredMember(aParent, aInvocation, aVariableStatement);
		phase.addDeferredMember(dm);
		return dm;
	}

	@NotNull DeferredMemberFunction deferred_member_function(OS_Element aParent, @Nullable IInvocation aInvocation, BaseFunctionDef aFunctionDef, final FunctionInvocation aFunctionInvocation) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
			else if (aParent instanceof OS_SpecialVariable) {
				aInvocation = ((OS_SpecialVariable) aParent).getInvocation(this);
			}
		}
		DeferredMemberFunction dm = new DeferredMemberFunction(aParent, aInvocation, aFunctionDef, this, aFunctionInvocation);
		phase.addDeferredMember(dm);
		return dm;
	}

	@NotNull
	public GenType resolve_type(final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		return ResolveType.resolve_type(module, type, ctx, LOG, this);
	}

	/*static*/ @NotNull GenType resolve_type(final OS_Module module, final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		return ResolveType.resolve_type(module, type, ctx, LOG, this);
	}

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull VariableTableEntry vte, final @NotNull ConstTableIA i2) {
		if (vte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.info("Null type in CTE "+cte);
		}
//		vte.type = cte.type;
		vte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
								final @NotNull Context ctx,
								final @NotNull VariableTableEntry vte,
								final @NotNull FnCallArgs fca,
								final @NotNull Instruction instruction) {
		final DoAssignCall dac = new DoAssignCall(new DeduceClient4(this), generatedFunction);
		dac.do_assign_call(instruction, vte, fca, ctx);
	}

	@NotNull PromiseExpectations expectations = new PromiseExpectations();

	class PromiseExpectations {
		long counter = 0;

		@NotNull List<PromiseExpectation> exp = new ArrayList<>();

		public void add(@NotNull PromiseExpectation aExpectation) {
			counter++;
			aExpectation.setCounter(counter);
			exp.add(aExpectation);
		}

		public void check() {
			for (@NotNull PromiseExpectation promiseExpectation : exp) {
				if (!promiseExpectation.isSatisfied())
					promiseExpectation.fail();
			}
		}
	}

	public interface ExpectationBase {
		String expectationString();
	}

	public class PromiseExpectation<B> {

		private final ExpectationBase base;
		private B result;
		private long counter;
		private final String desc;
		private boolean satisfied;
		private boolean _printed;

		public PromiseExpectation(ExpectationBase aBase, String aDesc) {
			base = aBase;
			desc = aDesc;
		}

		public void satisfy(B aResult) {
			final String satisfied_already = satisfied ? " already" : "";
			if (satisfied)
				assert false;
			result = aResult;
			satisfied = true;
//			LOG.info(String.format("Expectation (%s, %d)%s met: %s %s", DeduceTypes2.this, counter, satisfied_already, desc, base.expectationString()));
		}

		public void fail() {
			if (!_printed) {
//				LOG.err(String.format("Expectation (%s, %d) not met", DeduceTypes2.this, counter));
				_printed = true;
			}
		}

		public boolean isSatisfied() {
			return satisfied;
		}

		public void setCounter(long aCounter) {
			counter = aCounter;

//			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}
	}

	public <B> @NotNull PromiseExpectation<B> promiseExpectation(ExpectationBase base, String desc) {
		final @NotNull PromiseExpectation<B> promiseExpectation = new PromiseExpectation<>(base, desc);
		expectations.add(promiseExpectation);
		return promiseExpectation;
	}

	public IInvocation getInvocation(@NotNull GeneratedFunction generatedFunction) {
		final ClassInvocation classInvocation = generatedFunction.fi.getClassInvocation();
		final NamespaceInvocation ni;
		if (classInvocation == null) {
			ni = generatedFunction.fi.getNamespaceInvocation();
			return ni;
		} else
			return classInvocation;
	}

	void do_assign_call_args_ident(@NotNull BaseGeneratedFunction generatedFunction,
	                               @NotNull Context ctx,
	                               @NotNull VariableTableEntry vte,
	                               int aInstructionIndex,
	                               @NotNull ProcTableEntry aPte,
	                               int aI,
	                               @NotNull TypeTableEntry aTte,
	                               @NotNull IdentExpression aExpression) {
		final String e_text = aExpression.getText();
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(e_text);
//		LOG.info("10000 "+vte_ia);
		if (vte_ia != null) {
			final @NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			final Promise<GenType, Void, Void> p = vte1.typePromise();
			p.done(new DoneCallback<GenType>() {
				@Override
				public void onDone(GenType result) {
//					assert vte != vte1;
//					aTte.setAttached(result.resolved != null ? result.resolved : result.typeName);
					aTte.genType.copy(result);
//					vte.addPotentialType(aInstructionIndex, result); // TODO!!
				}
			});
			@NotNull Runnable runnable = new Runnable() {
				@Override
				public void run() {
					final @NotNull List<TypeTableEntry> ll = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
					doLogic(ll);
				}

				public void doLogic(@NotNull List<TypeTableEntry> potentialTypes) {
					assert potentialTypes.size() >= 0;
					switch (potentialTypes.size()) {
						case 1:
//							tte.attached = ll.get(0).attached;
//							vte.addPotentialType(instructionIndex, ll.get(0));
							if (p.isResolved()) {
//								LOG.info(String.format("1047 (vte already resolved) %s vte1.type = %s, gf = %s, tte1 = %s %n", vte1.getName(), vte1.type, generatedFunction, potentialTypes.get(0)));
							} else {
								final OS_Type attached = potentialTypes.get(0).getAttached();
								if (attached == null) return;
								switch (attached.getType()) {
									case USER:
										vte1.type.setAttached(attached); // !!
										break;
									case USER_CLASS:
										final GenType gt = vte1.genType;
										gt.resolved = attached;
										vte1.resolveType(gt);
										break;
									default:
										errSink.reportWarning("Unexpected value: " + attached.getType());
//										throw new IllegalStateException("Unexpected value: " + attached.getType());
								}
							}
							break;
						case 0:
							// README moved up here to elimiate work
							if (p.isResolved()) {
//								System.out.printf("890-1 Already resolved type: vte1.type = %s, gf = %s %n", vte1.type, generatedFunction);
								break;
							}
							LookupResultList lrl = ctx.lookup(e_text);
							@Nullable OS_Element best = lrl.chooseBest(null);
							if (best instanceof FormalArgListItem) {
								@NotNull final FormalArgListItem fali = (FormalArgListItem) best;
								final @NotNull OS_Type osType = new OS_Type(fali.typeName());
								if (!osType.equals(vte.type.getAttached())) {
									@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(
											TypeTableEntry.Type.SPECIFIED, osType, fali.getNameToken(), vte1);
									/*if (p.isResolved())
										System.out.printf("890 Already resolved type: vte1.type = %s, gf = %s, tte1 = %s %n", vte1.type, generatedFunction, tte1);
									else*/ {
										final OS_Type attached = tte1.getAttached();
										switch (attached.getType()) {
											case USER:
												vte1.type.setAttached(attached); // !!
												break;
											case USER_CLASS:
												final GenType gt = vte1.genType;
												gt.resolved = attached;
												vte1.resolveType(gt);
												break;
											default:
												errSink.reportWarning("2853 Unexpected value: " + attached.getType());
//												throw new IllegalStateException("Unexpected value: " + attached.getType());
										}
									}
								}
//								vte.type = tte1;
//								tte.attached = tte1.attached;
//								vte.setStatus(BaseTableEntry.Status.KNOWN, best);
							} else if (best instanceof VariableStatement) {
								final @NotNull VariableStatement vs = (VariableStatement) best;
								//
								assert vs.getName().equals(e_text);
								//
								@Nullable InstructionArgument vte2_ia = generatedFunction.vte_lookup(vs.getName());
								@NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte2_ia));
								if (p.isResolved())
									System.out.printf("915 Already resolved type: vte2.type = %s, gf = %s %n", vte1.type, generatedFunction);
								else {
									final GenType gt = vte1.genType;
									final OS_Type attached = vte2.type.getAttached();
									gt.resolved = attached;
									vte1.resolveType(gt);
								}
//								vte.type = vte2.type;
//								tte.attached = vte.type.attached;
								vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
								vte2.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best)); // TODO ??
							} else {
								int y = 2;
								LOG.err("543 " + best.getClass().getName());
								throw new NotImplementedException();
							}
							break;
						default:
							// TODO hopefully this works
							final @NotNull ArrayList<TypeTableEntry> potentialTypes1 = new ArrayList<TypeTableEntry>(
									Collections2.filter(potentialTypes, new Predicate<TypeTableEntry>() {
										@Override
										public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable TypeTableEntry input) {
											assert input != null;
											return input.getAttached() != null;
										}
									}));
							// prevent infinite recursion
							if (potentialTypes1.size() < potentialTypes.size())
								doLogic(potentialTypes1);
							else
								LOG.info("913 Don't know");
							break;
					}
				}
			};
			onFinish(runnable);
		} else {
			int ia = generatedFunction.addIdentTableEntry(aExpression, ctx);
			@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(ia);
			idte.addPotentialType(aInstructionIndex, aTte); // TODO DotExpression??
			final int ii = aI;
			idte.onType(phase, new OnType() {
				@Override
				public void typeDeduced(@NotNull OS_Type aType) {
					aPte.setArgType(ii, aType); // TODO does this belong here or in FunctionInvocation?
					aTte.setAttached(aType); // since we know that tte.attached is always null here
				}

				@Override
				public void noTypeFound() {
					LOG.err("719 no type found "+generatedFunction.getIdentIAPathNormal(new IdentIA(ia, generatedFunction)));
				}
			});
		}
	}

	void do_assign_call_GET_ITEM(@NotNull GetItemExpression gie, TypeTableEntry tte, @NotNull BaseGeneratedFunction generatedFunction, Context ctx) {
		try {
			final LookupResultList lrl = DeduceLookupUtils.lookupExpression(gie.getLeft(), ctx, this);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof VariableStatement) { // TODO what about alias?
					@NotNull VariableStatement vs = (VariableStatement) best;
					String s = vs.getName();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
						throw new NotImplementedException();
					} else {
						final IdentTableEntry idte = generatedFunction.getIdentTableEntryFor(vs.getNameToken());
						assert idte != null;
						@Nullable OS_Type ty = idte.type.getAttached();
						idte.onType(phase, new OnType() {
							@Override public void typeDeduced(final @NotNull OS_Type ty) {
								assert ty != null;
								@NotNull GenType rtype = null;
								try {
									rtype = resolve_type(ty, ctx);
								} catch (ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@NotNull FunctionDef fd = (FunctionDef) best2;
											@Nullable ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													if (idte.type == null) {
														@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), idte); // TODO expression?
														idte.type = tte1;
													} else
														idte.type.setAttached(gt(aType));
												}
											});
										} else {
											throw new NotImplementedException();
										}
									} else {
										throw new NotImplementedException();
									}
								}
							}

							@Override
							public void noTypeFound() {
								throw new NotImplementedException();
							}
						});
						if (ty == null) {
							@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_Type(vs.typeName()), vs.getNameToken());
							idte.type = tte3;
							ty = idte.type.getAttached();
						}
					}

	//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof FormalArgListItem) {
					final @Nullable FormalArgListItem fali = (FormalArgListItem) best;
					String s = fali.name();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

	//					final @Nullable OS_Type ty2 = vte2.type.attached;
						vte2.typePromise().done(new DoneCallback<GenType>() {
							@Override
							public void onDone(@NotNull GenType result) {
	//							assert false; // TODO this code is never reached
								final @Nullable OS_Type ty2 = result.typeName/*.getAttached()*/;
								assert ty2 != null;
								@NotNull GenType rtype = null;
								try {
									rtype = resolve_type(ty2, ctx);
								} catch (ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty2); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@Nullable FunctionDef fd = (FunctionDef) best2;
											@Nullable ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), vte2); // TODO expression?
													vte2.type = tte1;
												}
											});
										} else {
											throw new NotImplementedException();
										}
									} else {
										throw new NotImplementedException();
									}
								}
							}
						});
	//					vte2.onType(phase, new OnType() {
	//						@Override public void typeDeduced(final OS_Type ty2) {
	//						}
	//
	//						@Override
	//						public void noTypeFound() {
	//							throw new NotImplementedException();
	//						}
	//					});
	/*
						if (ty2 == null) {
							@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_Type(fali.typeName()), fali.getNameToken());
							vte2.type = tte3;
	//						ty2 = vte2.type.attached; // TODO this is final, but why assign anyway?
						}
	*/
					}
				} else {
					final int y=2;
					throw new NotImplementedException();
				}
			} else {
				final int y=2;
				throw new NotImplementedException();
			}
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			int y=2;
			throw new NotImplementedException();
		}
	}

/*
	private void forFunction(GeneratedFunction gf, ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}
*/

	void forFunction(@NotNull FunctionInvocation gf, @NotNull ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull IdentTableEntry idte, final @NotNull ConstTableIA i2) {
		if (idte.type != null && idte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.err("*** ERROR: Null type in CTE "+cte);
		}
		// idte.type may be null, but we still addPotentialType here
		idte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
								final @NotNull Context ctx,
								final @NotNull IdentTableEntry idte,
								final @NotNull FnCallArgs fca,
								final int instructionIndex) {
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		for (final @NotNull TypeTableEntry tte : pte.getArgs()) {
			LOG.info("771 "+tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC:
			{
				tte.setAttached(new OS_Type(BuiltInTypes.SystemInteger));
				idte.type = tte; // TODO why not addPotentialType ? see below for example
			}
			break;
			case IDENT:
			{
				final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) e).getText());
				final @NotNull List<TypeTableEntry> ll = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
				if (ll.size() == 1) {
					tte.setAttached(ll.get(0).getAttached());
					idte.addPotentialType(instructionIndex, ll.get(0));
				} else
					throw new NotImplementedException();
			}
			break;
			default:
			{
				throw new NotImplementedException();
			}
			}
		}
		{
			final String s = ((IdentExpression) pte.expression).getText();
			final LookupResultList lrl = ctx.lookup(s);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				pte.setResolvedElement(best);

				// TODO do we need to add a dependency for class, etc?
				if (false) {
					if (best instanceof ConstructorDef) {
						// TODO Dont know how to handle this
						int y=2;
					} else if (best instanceof FunctionDef || best instanceof DefFunctionDef) {
						final OS_Element parent = best.getParent();
						IInvocation invocation;
						if (parent instanceof NamespaceStatement) {
							invocation = new NamespaceInvocation((NamespaceStatement) parent);
						} else if (parent instanceof ClassStatement) {
							invocation = new ClassInvocation((ClassStatement) parent, null);
						} else
							throw new NotImplementedException();

						FunctionInvocation fi = newFunctionInvocation((BaseFunctionDef) best, pte, invocation, phase);
						generatedFunction.addDependentFunction(fi);
					} else if (best instanceof ClassStatement) {
						GenType genType = new GenType();
						genType.resolved = new OS_Type((ClassStatement) best);
						// ci, typeName, node
	//					genType.
						genType.genCI(null, DeduceTypes2.this, errSink, phase);
						generatedFunction.addDependentType(genType);
					}
				}
			} else
				throw new NotImplementedException();
		}
	}

	void implement_calls(final @NotNull BaseGeneratedFunction gf, final @NotNull Context context, final InstructionArgument i2, final @NotNull ProcTableEntry fn1, final int pc) {
		if (gf.deferred_calls.contains(pc)) {
			LOG.err("Call is deferred "/*+gf.getInstruction(pc)*/+" "+fn1);
			return;
		}
		implement_calls_(gf, context, i2, fn1, pc);
	}

	class Implement_Calls_ {
		private final BaseGeneratedFunction gf;
		private final Context context;
		private final InstructionArgument i2;
		private final ProcTableEntry pte;
		private final int pc;

		public Implement_Calls_(final @NotNull BaseGeneratedFunction aGf,
								final @NotNull Context aContext,
								final @NotNull InstructionArgument aI2,
								final @NotNull ProcTableEntry aPte,
								final int aPc) {
			gf = aGf;
			context = aContext;
			i2 = aI2;
			pte = aPte;
			pc = aPc;
		}

		void action() {
			final IExpression pn1 = pte.expression;
			if (!(pn1 instanceof IdentExpression)) {
				throw new IllegalStateException("pn1 is not IdentExpression");
			}

			final String pn = ((IdentExpression) pn1).getText();
			boolean found = lookup_name_calls(context, pn, pte);
			if (found) return;

			final @Nullable String pn2 = SpecialFunctions.reverse_name(pn);
			if (pn2 != null) {
//				LOG.info("7002 "+pn2);
				found = lookup_name_calls(context, pn2, pte);
				if (found) return;
			}

			if (i2 instanceof IntegerIA) {
				found = action_i2_IntegerIA(pn, pn2);
			} else {
				found = action_dunder(pn);
			}

			if (!found)
				pte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
		}

		private boolean action_dunder(String pn) {
			assert Pattern.matches("__[a-z]+__", pn);
//			LOG.info(String.format("i2 is not IntegerIA (%s)",i2.getClass().getName()));
			//
			// try to get dunder method from class
			//
			IExpression exp = pte.getArgs().get(0).expression;
			if (exp instanceof IdentExpression) {
				return action_dunder_doIt(pn, (IdentExpression) exp);
			}
			return false;
		}

		private boolean action_dunder_doIt(String pn, IdentExpression exp) {
			final @NotNull IdentExpression identExpression = exp;
			@Nullable InstructionArgument vte_ia = gf.vte_lookup(identExpression.getText());
			if (vte_ia != null) {
				VTE_TypePromises.dunder(pn, (IntegerIA) vte_ia, pte, DeduceTypes2.this);
				return true;
			}
			return false;
		}

		private boolean action_i2_IntegerIA(String pn, @Nullable String pn2) {
			boolean found;
			final @NotNull VariableTableEntry vte = gf.getVarTableEntry(to_int(i2));
			final Context ctx = gf.getContextFromPC(pc); // might be inside a loop or something
			final String vteName = vte.getName();
			if (vteName != null) {
				found = action_i2_IntegerIA_vteName_is_null(pn, pn2, ctx, vteName);
			} else {
				found = action_i2_IntegerIA_vteName_is_not_null(pn, pn2, vte);
			}
			return found;
		}

		private boolean action_i2_IntegerIA_vteName_is_not_null(String pn, @Nullable String pn2, @NotNull VariableTableEntry vte) {
			final @NotNull List<TypeTableEntry> tt = getPotentialTypesVte(vte);
			if (tt.size() != 1) {
				return false;
			}
			final OS_Type x = tt.get(0).getAttached();
			assert x != null;
			switch (x.getType()) {
			case USER_CLASS:
				pot_types_size_is_1_USER_CLASS(pn, pn2, x);
				return true;
			case BUILT_IN:
				final Context ctx2 = context;//x.getTypeName().getContext();
				try {
					@NotNull GenType ty2 = resolve_type(x, ctx2);
					pot_types_size_is_1_USER_CLASS(pn, pn2, ty2.resolved);
					return true;
				} catch (ResolveError resolveError) {
					resolveError.printStackTrace();
					errSink.reportDiagnostic(resolveError);
					return false;
				}
			default:
				assert false;
				return false;
			}
		}

		private void pot_types_size_is_1_USER_CLASS(String pn, @Nullable String pn2, OS_Type x) {
			boolean found;
			final Context ctx1 = x.getClassOf().getContext();

			found = lookup_name_calls(ctx1, pn, pte);
			if (found) return;

			if (pn2 != null) {
				found = lookup_name_calls(ctx1, pn2, pte);
			}

			if (!found) {
				//throw new NotImplementedException(); // TODO
				errSink.reportError("Special Function not found " + pn);
			}
		}

		private boolean action_i2_IntegerIA_vteName_is_null(String pn, @Nullable String pn2, Context ctx, String vteName) {
			boolean found = false;
			if (SpecialVariables.contains(vteName)) {
				LOG.err("Skipping special variable " + vteName + " " + pn);
			} else {
				final LookupResultList lrl2 = ctx.lookup(vteName);
//				LOG.info("7003 "+vteName+" "+ctx);
				final @Nullable OS_Element best2 = lrl2.chooseBest(null);
				if (best2 != null) {
					found = lookup_name_calls(best2.getContext(), pn, pte);
					if (found) return true;

					if (pn2 != null) {
						found = lookup_name_calls(best2.getContext(), pn2, pte);
						if (found) return true;
					}

					errSink.reportError("Special Function not found " + pn);
				} else {
					throw new NotImplementedException(); // Cant find vte, should never happen
				}
			}
			return found;
		}
	}

	private void implement_calls_(final @NotNull BaseGeneratedFunction gf,
								  final @NotNull Context context,
								  final InstructionArgument i2,
								  final @NotNull ProcTableEntry pte,
								  final int pc) {
		Implement_Calls_ ic = new Implement_Calls_(gf, context, i2, pte, pc);
		ic.action();
	}

	boolean lookup_name_calls(final @NotNull Context ctx, final @NotNull String pn, final @NotNull ProcTableEntry pte) {
		final LookupResultList lrl = ctx.lookup(pn);
		final @Nullable OS_Element best = lrl.chooseBest(null); // TODO check arity and arg matching
		if (best != null) {
			pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(best, null)); // TODO why include if only to be null?
			return true;
		}
		return false;
	}

	public static int to_int(@NotNull final InstructionArgument arg) {
		if (arg instanceof IntegerIA)
			return ((IntegerIA) arg).getIndex();
		if (arg instanceof ProcIA)
			return ((ProcIA) arg).getIndex();
		if (arg instanceof IdentIA)
			return ((IdentIA) arg).getIndex();
		throw new NotImplementedException();
	}

	public void resolveIdentIA2_(@NotNull Context context, @NotNull IdentIA identIA, @NotNull GeneratedFunction generatedFunction, @NotNull FoundElement foundElement) {
		final @NotNull List<InstructionArgument> s = generatedFunction._getIdentIAPathList(identIA);
		resolveIdentIA2_(context, identIA, s, generatedFunction, foundElement);
	}

	public void resolveIdentIA_(@NotNull Context context, @NotNull IdentIA identIA, BaseGeneratedFunction generatedFunction, @NotNull FoundElement foundElement) {
		@NotNull Resolve_Ident_IA ria = new Resolve_Ident_IA(new DeduceClient3(this), context, identIA, generatedFunction, foundElement, errSink);
		try {
			ria.action();
		} catch (ResolveError aE) {
			throw new RuntimeException(aE);
		}
	}


	public void resolveIdentIA2_(@NotNull final Context ctx,
								 @Nullable IdentIA identIA,
								 @Nullable List<InstructionArgument> s,
								 @NotNull final BaseGeneratedFunction generatedFunction,
								 @NotNull final FoundElement foundElement) {
		@NotNull Resolve_Ident_IA2 ria2 = new Resolve_Ident_IA2(this, errSink, phase, generatedFunction, foundElement);
		ria2.resolveIdentIA2_(ctx, identIA, s);
	}

	OS_Type gt(@NotNull GenType aType) {
		return aType.resolved != null ? aType.resolved : aType.typeName;
	}

	@NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull VariableTableEntry vte) {
		return new ArrayList<TypeTableEntry>(vte.potentialTypes());
	}

	@NotNull
	private ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull GeneratedFunction generatedFunction, @NotNull InstructionArgument vte_index) {
		return getPotentialTypesVte(generatedFunction.getVarTableEntry(to_int(vte_index)));
	}

	public void register_and_resolve(@NotNull VariableTableEntry aVte, @NotNull ClassStatement aKlass) {
		@Nullable ClassInvocation ci = new ClassInvocation(aKlass, null);
		ci = phase.registerClassInvocation(ci);
		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	/**
	 * Sets the node for a GenType, invocation must already be set
	 *
	 * @param aGenType the GenType to modify.
	 */
	public void genNodeForGenType2(final GenType aGenType) {
//		assert aGenType.nonGenericTypeName != null;

		final IInvocation invocation = aGenType.ci;

		if (invocation instanceof NamespaceInvocation) {
			final NamespaceInvocation namespaceInvocation = (NamespaceInvocation) invocation;
			namespaceInvocation.resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
				@Override
				public void onDone(final GeneratedNamespace result) {
					aGenType.node = result;
				}
			});
		} else if (invocation instanceof ClassInvocation) {
			final ClassInvocation classInvocation = (ClassInvocation) invocation;
			classInvocation.resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					aGenType.node = result;
				}
			});
		} else
			throw new IllegalStateException("invalid invocation");
	}

	static class DeduceClient2 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient2(DeduceTypes2 deduceTypes2) {
			this.deduceTypes2 = deduceTypes2;
		}

		public @Nullable ClassInvocation registerClassInvocation(@NotNull ClassInvocation ci) {
			return deduceTypes2.phase.registerClassInvocation(ci);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(BaseFunctionDef constructorDef, ProcTableEntry pte, @NotNull IInvocation ci) {
			return deduceTypes2.newFunctionInvocation(constructorDef, pte, ci, deduceTypes2.phase);
		}

		public NamespaceInvocation registerNamespaceInvocation(NamespaceStatement namespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(namespaceStatement);
		}

		public @NotNull ClassInvocation genCI(@NotNull GenType genType, TypeName typeName) {
			return genType.genCI(typeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public @NotNull ElLog getLOG() {
			return deduceTypes2.LOG;
		}
	}

	public static class DeduceClient3 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient3(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public ElLog getLOG() {
			return deduceTypes2.LOG;
		}

		public LookupResultList lookupExpression(final IExpression aExp, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExp, aContext, deduceTypes2);
		}

		public GenerateFunctions getGenerateFunctions(final OS_Module aModule) {
			return deduceTypes2.getGenerateFunctions(aModule);
		}

		public void resolveIdentIA2_(final Context aEctx,
									 final IdentIA aIdentIA,
									 final @Nullable List<InstructionArgument> aInstructionArgumentList,
									 final BaseGeneratedFunction aGeneratedFunction,
									 final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA2_(aEctx, aIdentIA, aInstructionArgumentList, aGeneratedFunction, aFoundElement);
		}

		public List<TypeTableEntry> getPotentialTypesVte(final VariableTableEntry aVte) {
			return deduceTypes2.getPotentialTypesVte(aVte);
		}

		public IInvocation getInvocation(final GeneratedFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public GenType resolve_type(final OS_Type aType, final Context aContext) throws ResolveError {
			return deduceTypes2.resolve_type(aType, aContext);
		}

		public DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public void addJobs(final WorkList aWl) {
			deduceTypes2.wm.addJobs(aWl);
		}

		public IElementHolder newGenericElementHolderWithType(final OS_Element aElement, final TypeName aTypeName) {
			final OS_Type typeName;
			if (aTypeName.isNull())
				typeName = null;
			else
				typeName = new OS_Type(aTypeName);
			return new GenericElementHolderWithType(aElement, typeName, deduceTypes2);
		}

		public void found_element_for_ite(final BaseGeneratedFunction generatedFunction,
										  final @NotNull IdentTableEntry ite,
										  final @Nullable OS_Element y,
										  final Context ctx) {
			deduceTypes2.found_element_for_ite(generatedFunction, ite, y, ctx);
		}

		public void genCIForGenType2(final @NotNull GenType genType) {
			genType.genCIForGenType2(deduceTypes2);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef aFunctionDef, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aFunctionDef, aPte, aInvocation, deduceTypes2.phase);
		}
	}

	class DeduceClient4 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient4(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public OS_Element lookup(final IdentExpression aElement, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookup(aElement, aContext, deduceTypes2);
		}

		public void reportDiagnostic(final ResolveError aResolveError) {
			deduceTypes2.errSink.reportDiagnostic(aResolveError);
		}

		public ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String constructorName) {
			return deduceTypes2.phase.registerClassInvocation(aClassStatement, constructorName);
		}

		public FunctionInvocation newFunctionInvocation(final FunctionDef aElement, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aElement, aPte, aInvocation, deduceTypes2.phase);
		}

		public DeferredMemberFunction deferred_member_function(final OS_Element aParent, final IInvocation aInvocation, final FunctionDef aFunctionDef, final FunctionInvocation aFunctionInvocation) {
			return deduceTypes2.deferred_member_function(aParent, aInvocation, aFunctionDef, aFunctionInvocation);
		}

		public @NotNull OS_Module getModule() {
			return module;
		}

		public @NotNull ElLog getLOG() {
			return LOG;
		}

		public DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public OS_Element _resolveAlias(final AliasStatement aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, deduceTypes2);
		}

		public void found_element_for_ite(final BaseGeneratedFunction aGeneratedFunction, final IdentTableEntry aEntry, final OS_Element aE, final Context aCtx) {
			deduceTypes2.found_element_for_ite(aGeneratedFunction, aEntry, aE, aCtx);
		}

		public <T> PromiseExpectation<T> promiseExpectation(final BaseGeneratedFunction aGeneratedFunction, final String aName) {
			return deduceTypes2.promiseExpectation(aGeneratedFunction, aName);
		}

		public OS_Element _resolveAlias2(final AliasStatement aAliasStatement) throws ResolveError {
			return DeduceLookupUtils._resolveAlias2(aAliasStatement, deduceTypes2);
		}

		public LookupResultList lookupExpression(final IExpression aExpression, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExpression, aContext, deduceTypes2);
		}

		public ClassInvocation registerClassInvocation(final ClassInvocation aCi) {
			return deduceTypes2.phase.registerClassInvocation(aCi);
		}

		public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(aNamespaceStatement);
		}

		public void forFunction(final FunctionInvocation aFunctionInvocation, final ForFunction aForFunction) {
			deduceTypes2.forFunction(aFunctionInvocation, aForFunction);
		}

		public void implement_calls(final BaseGeneratedFunction aGeneratedFunction, final Context aParent, final InstructionArgument aArg, final ProcTableEntry aPte, final int aInstructionIndex) {
			deduceTypes2.implement_calls(aGeneratedFunction, aParent, aArg, aPte, aInstructionIndex);
		}

		public void resolveIdentIA_(final Context aCtx, final IdentIA aIdentIA, final BaseGeneratedFunction aGeneratedFunction, final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA_(aCtx, aIdentIA, aGeneratedFunction, aFoundElement);
		}

		public IInvocation getInvocation(final GeneratedFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public ClassInvocation genCI(final GenType aType, final TypeName aGenericTypeName) {
			return aType.genCI(aGenericTypeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public OS_Type gt(final GenType aType) {
			return deduceTypes2.gt(aType);
		}

		public void register_and_resolve(final VariableTableEntry aVte, final ClassStatement aClassStatement) {
			deduceTypes2.register_and_resolve(aVte, aClassStatement);
		}

		public ErrSink getErrSink() {
			return deduceTypes2.errSink;
		}

		public void onFinish(final Runnable aRunnable) {
			deduceTypes2.onFinish(aRunnable);
		}

		public List<TypeTableEntry> getPotentialTypesVte(final GeneratedFunction aGeneratedFunction, final InstructionArgument aVte_ia) {
			return deduceTypes2.getPotentialTypesVte(aGeneratedFunction, aVte_ia);
		}

		public DeduceTypes2 get() {
			return deduceTypes2;
		}

		public GenType resolve_type(final OS_Type aTy, final Context aCtx) throws ResolveError {
			return deduceTypes2.resolve_type(aTy, aCtx);
		}
	}

	public static class ClassInvocationMake {
		public static ClassInvocation withGenericPart(ClassStatement best,
		                                              String constructorName,
		                                              NormalTypeName aTyn1,
		                                              DeduceTypes2 dt2,
		                                              final ErrSink aErrSink) {
			@NotNull GenericPart genericPart = new GenericPart(best, aTyn1);

			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);

			if (genericPart.hasGenericPart()) {
				final @NotNull List<TypeName> gp = best.getGenericPart();
				final @NotNull TypeNameList gp2 = genericPart.getGenericPartFromTypeName();

				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull GenType typeName2;
					try {
						typeName2 = dt2.resolve_type(new OS_Type(typeName), typeName.getContext());
						// TODO transition to GenType
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (ResolveError aResolveError) {
//						aResolveError.printStackTrace();
						aErrSink.reportDiagnostic(aResolveError);
					}
				}
			}
			return clsinv;
		}
	}

	static class GenericPart {
		private final ClassStatement classStatement;
		private final TypeName genericTypeName;

		@Contract(pure = true)
		public GenericPart(final ClassStatement aClassStatement, final TypeName aGenericTypeName) {
			classStatement = aClassStatement;
			genericTypeName = aGenericTypeName;
		}

		@Contract(pure = true)
		public boolean hasGenericPart() {
			return classStatement.getGenericPart().size() > 0;
		}

		@Contract(pure = true)
		private NormalTypeName getGenericTypeName() {
			assert genericTypeName != null;
			assert genericTypeName instanceof NormalTypeName;

			return (NormalTypeName) genericTypeName;
		}

		@Contract(pure = true)
		public TypeNameList getGenericPartFromTypeName() {
			final NormalTypeName ntn = getGenericTypeName();
			return ntn.getGenericPart();
		}
	}

	public static class OS_SpecialVariable implements OS_Element {
		private final VariableTableEntry variableTableEntry;
		private final VariableTableType type;
		private final BaseGeneratedFunction generatedFunction;
		public DeduceLocalVariable.MemberInvocation memberInvocation;

		public OS_SpecialVariable(final VariableTableEntry aVariableTableEntry, final VariableTableType aType, final BaseGeneratedFunction aGeneratedFunction) {
			variableTableEntry = aVariableTableEntry;
			type = aType;
			generatedFunction = aGeneratedFunction;
		}

		@Override
		public void visitGen(final ElElementVisitor visit) {
			throw new IllegalArgumentException("not implemented");
		}

		@Override
		public Context getContext() {
			return generatedFunction.getFD().getContext();
		}

		@Override
		public OS_Element getParent() {
			return generatedFunction.getFD();
		}

		@Nullable
		public IInvocation getInvocation(final DeduceTypes2 aDeduceTypes2) {
			final @Nullable IInvocation aInvocation;
			final OS_SpecialVariable specialVariable = this;
			assert specialVariable.type == VariableTableType.SELF;
			// first parent is always a function
			switch (DecideElObjectType.getElObjectType(specialVariable.getParent().getParent())) {
				case CLASS:
					final ClassStatement classStatement = (ClassStatement) specialVariable.getParent().getParent();
					aInvocation = aDeduceTypes2.phase.registerClassInvocation(classStatement, null); // TODO generics
//				ClassInvocationMake.withGenericPart(classStatement, null, null, this);
					break;
				case NAMESPACE:
					throw new NotImplementedException(); // README ha! implemented in
				default:
					throw new IllegalArgumentException("Illegal object type for parent");
			}
			return aInvocation;
		}
	}

	private Map<Object, IZero> _zeros = new HashMap<>();

	public Zero_FuncExprType getZero(OS_FuncExprType aFuncExprType) {
		if (!(_zeros.containsKey(aFuncExprType)))
			_zeros.put(aFuncExprType, new Zero_FuncExprType(aFuncExprType));

		return (Zero_FuncExprType) _zeros.get(aFuncExprType);
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
