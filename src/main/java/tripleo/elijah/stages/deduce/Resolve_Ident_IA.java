/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.Collection;
import java.util.List;

/**
 * Created 7/8/21 2:31 AM
 */
class Resolve_Ident_IA {
	private final @NotNull Context context;
	private final @NotNull IdentIA identIA;
	private final BaseGeneratedFunction generatedFunction;
	private final @NotNull FoundElement foundElement;
	private final @NotNull ErrSink errSink;

	private final @NotNull DeduceTypes2 deduceTypes2;
	private final @NotNull DeducePhase phase;
	
	private final @NotNull ElLog LOG;

	@Contract(pure = true)
	public Resolve_Ident_IA(final @NotNull DeduceTypes2 aDeduceTypes2,
							final @NotNull DeducePhase aPhase,
							final @NotNull Context aContext,
							final @NotNull IdentIA aIdentIA,
							final BaseGeneratedFunction aGeneratedFunction,
							final @NotNull FoundElement aFoundElement,
							final @NotNull ErrSink aErrSink) {
		deduceTypes2 = aDeduceTypes2;
		phase = aPhase;
		context = aContext;
		identIA = aIdentIA;
		generatedFunction = aGeneratedFunction;
		foundElement = aFoundElement;
		errSink = aErrSink;
		//
		LOG = deduceTypes2.LOG;
	}

	@Nullable OS_Element el;
	Context ectx;

	public void action() {
		final @NotNull List<InstructionArgument> s = generatedFunction._getIdentIAPathList(identIA);

		ectx = context;
		el = null;

/*
		for (final InstructionArgument ia : s) {
			if (ia instanceof IntegerIA) {
				@NotNull RIA_STATE state = action_IntegerIA(ia);
				switch (state) {
					case CONTINUE:
						continue;
					case RETURN:
						return;
					case NEXT:
						break;
					default:
						throw new IllegalStateException("Can't be here");
				}
			} else if (ia instanceof IdentIA) {
				@NotNull RIA_STATE state = action_IdentIA((IdentIA) ia);
				switch (state) {
					case CONTINUE:
						continue; // never happens here
					case RETURN:
						return;  // element notFound. short-circuit and exit. callback already called.
					case NEXT:
						break;
					default:
						throw new IllegalStateException("Can't be here");
				}
			} else if (ia instanceof ProcIA) {
				action_ProcIA(ia);
			} else
				throw new IllegalStateException("Really cant be here");
*/
		if (!process(s.get(0))) return;

		preUpdateStatus(s);
		updateStatus(s);
	}

	private boolean process(final InstructionArgument ia) {
		if (ia instanceof IntegerIA) {
			@NotNull final RIA_STATE state = action_IntegerIA(ia);
			if (state == RIA_STATE.RETURN) {
				return false;
			}
		} else if (ia instanceof IdentIA) {
			@NotNull final RIA_STATE state = action_IdentIA((IdentIA) ia);
			if (state == RIA_STATE.RETURN) {
				return false;
			}
		} else if (ia instanceof ProcIA) {
			action_ProcIA(ia);
		} else
			throw new IllegalStateException("Really cant be here");
		return true;
	}

	private void preUpdateStatus(final @NotNull List<InstructionArgument> s) {
		final String normal_path = generatedFunction.getIdentIAPathNormal(identIA);
		if (s.size() > 1) {
			final InstructionArgument x = s.get(s.size() - 1);
			if (x instanceof IntegerIA) {
				assert false;
				@NotNull final VariableTableEntry y = ((IntegerIA) x).getEntry();
				y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
			} else if (x instanceof IdentIA) {
				@NotNull final IdentTableEntry y = ((IdentIA) x).getEntry();
				y.addStatusListener(new BaseTableEntry.StatusListener() {
					@Override
					public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
						if (newStatus == BaseTableEntry.Status.KNOWN) {
//								assert el2 != eh.getElement();
							y.resolveExpectation.satisfy(normal_path);
//							LOG.info("1424 Found for " + normal_path);
							foundElement.doFoundElement(eh.getElement());
						}
					}
				});
			}
		} else {
//			LOG.info("1431 Found for " + normal_path);
			foundElement.doFoundElement(el);
		}
	}

	private void updateStatus(@NotNull final List<InstructionArgument> aS) {
		final InstructionArgument x = aS.get(/*aS.size()-1*/0);
		if (x instanceof IntegerIA) {
			@NotNull final VariableTableEntry y = ((IntegerIA) x).getEntry();
			y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
		} else if (x instanceof IdentIA) {
			@NotNull final IdentTableEntry y = ((IdentIA) x).getEntry();
			assert y.getStatus() == BaseTableEntry.Status.KNOWN;
//				y.setStatus(BaseTableEntry.Status.KNOWN, el);
		} else if (x instanceof ProcIA) {
			@NotNull final ProcTableEntry y = ((ProcIA) x).getEntry();
			assert y.getStatus() == BaseTableEntry.Status.KNOWN;
			y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
		} else
			throw new NotImplementedException();
	}

	private void action_ProcIA(@NotNull final InstructionArgument ia) {
		@NotNull final ProcTableEntry prte = generatedFunction.getProcTableEntry(DeduceTypes2.to_int(ia));
		if (prte.getResolvedElement() == null) {
			IExpression exp = prte.expression;
			if (exp instanceof ProcedureCallExpression) {
				final @NotNull ProcedureCallExpression pce = (ProcedureCallExpression) exp;
				exp = pce.getLeft(); // TODO might be another pce??!!
				if (exp instanceof ProcedureCallExpression)
					throw new IllegalArgumentException("double pce!");
			}
			try {
				final LookupResultList lrl = DeduceLookupUtils.lookupExpression(exp, ectx, deduceTypes2);
				el = lrl.chooseBest(null);
				ectx = el.getContext();
//					prte.setResolvedElement(el);
				prte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
				// handle constructor calls
				if (el instanceof ClassStatement) {
					_procIA_constructor_helper(prte);
				}
			} catch (final ResolveError aResolveError) {
				aResolveError.printStackTrace();
				throw new NotImplementedException();
			}
		} else {
			el = prte.getResolvedElement();
			ectx = el.getContext();
		}
	}

	private void _procIA_constructor_helper(@NotNull final ProcTableEntry pte) {
		if (pte.getClassInvocation() != null)
			throw new IllegalStateException();

		if (pte.getFunctionInvocation() == null) {
			_procIA_constructor_helper_create_invocations(pte);
		} else {
			final FunctionInvocation fi = pte.getFunctionInvocation();
			final ClassInvocation ci = fi.getClassInvocation();
			if (fi.getFunction() instanceof ConstructorDef) {
				@NotNull final GenType genType = new GenType(ci.getKlass());
				genType.ci = ci;
				ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						genType.node = result;
					}
				});
				final @NotNull WorkList wl = new WorkList();
				final @NotNull OS_Module module = ci.getKlass().getContext().module();
				final @NotNull GenerateFunctions generateFunctions = deduceTypes2.getGenerateFunctions(module);
				if (pte.getFunctionInvocation().getFunction() == ConstructorDef.defaultVirtualCtor)
					wl.addJob(new WlGenerateDefaultCtor(generateFunctions, fi));
				else
					wl.addJob(new WlGenerateCtor(generateFunctions, fi, null));
				deduceTypes2.wm.addJobs(wl);
//				generatedFunction.addDependentType(genType);
//				generatedFunction.addDependentFunction(fi);
			}
		}
	}

	private void _procIA_constructor_helper_create_invocations(@NotNull final ProcTableEntry pte) {
		@Nullable ClassInvocation ci = new ClassInvocation((ClassStatement) el, null);

		ci = phase.registerClassInvocation(ci);
//		prte.setClassInvocation(ci);
		final Collection<ConstructorDef> cs = (((ClassStatement) el).getConstructors());
		@Nullable ConstructorDef selected_constructor = null;
		if (pte.getArgs().size() == 0 && cs.size() == 0) {
			// TODO use a virtual default ctor
			LOG.info("2262 use a virtual default ctor for " + pte.expression);
			selected_constructor = ConstructorDef.defaultVirtualCtor;
		} else {
			// TODO find a ctor that matches prte.getArgs()
			final List<TypeTableEntry> x = pte.getArgs();
			final int yy = 2;
		}
		assert ((ClassStatement) el).getGenericPart().size() == 0;
		@NotNull final FunctionInvocation fi = new FunctionInvocation(selected_constructor, pte, ci, phase.generatePhase);
//		fi.setClassInvocation(ci);
		pte.setFunctionInvocation(fi);
		if (fi.getFunction() instanceof ConstructorDef) {
			@NotNull final GenType genType = new GenType(ci.getKlass());
			genType.ci = ci;
			ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					genType.node = result;
				}
			});
			generatedFunction.addDependentType(genType);
			generatedFunction.addDependentFunction(fi);
		} else
			generatedFunction.addDependentFunction(fi);
	}

	private @NotNull RIA_STATE action_IdentIA(@NotNull final IdentIA ia) {
		final @NotNull IdentTableEntry idte = ia.getEntry();
		if (idte.getStatus() == BaseTableEntry.Status.UNKNOWN) {
			LOG.info("1257 Not found for " + generatedFunction.getIdentIAPathNormal(ia));
			// No need checking more than once
			idte.resolveExpectation.fail();
			foundElement.doNoFoundElement();
			return RIA_STATE.RETURN;
		}
		//assert idte.backlink == null;

		if (idte.getStatus() == BaseTableEntry.Status.UNCHECKED) {
			if (idte.backlink == null) {
				final String text = idte.getIdent().getText();
				if (idte.getResolvedElement() == null) {
					final LookupResultList lrl = ectx.lookup(text);
					el = lrl.chooseBest(null);
				} else {
					assert false;
					el = idte.getResolvedElement();
				}
				{
					if (el instanceof FunctionDef) {
						final @NotNull FunctionDef functionDef = (FunctionDef) el;
						final OS_Element parent = functionDef.getParent();
						@Nullable GenType genType = null;
						@Nullable IInvocation invocation = null;
						switch (DecideElObjectType.getElObjectType(parent)) {
						case UNKNOWN:
							break;
						case CLASS:
							genType = new GenType((ClassStatement) parent);
							@Nullable final ClassInvocation ci = new ClassInvocation((ClassStatement) parent, null);
							invocation = phase.registerClassInvocation(ci);
							break;
						case NAMESPACE:
							genType = new GenType((NamespaceStatement) parent);
							invocation = phase.registerNamespaceInvocation((NamespaceStatement) parent);
							break;
						default:
							// do nothing
							break;
						}
						if (genType != null) {
							generatedFunction.addDependentType(genType);

							// TODO might not be needed
							if (invocation != null) {
								@NotNull final FunctionInvocation fi = new FunctionInvocation((BaseFunctionDef) el, null, invocation, phase.generatePhase);
//								generatedFunction.addDependentFunction(fi); // README program fails if this is included
							}
						}
					} else if (el instanceof ClassStatement) {
						@NotNull final GenType genType = new GenType((ClassStatement) el);
						generatedFunction.addDependentType(genType);
					}
				}
				if (el != null) {
					idte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));

					if (el.getContext() == null)
						throw new IllegalStateException("2468 null context");

					ectx = el.getContext();
				} else {
					errSink.reportError("1179 Can't resolve " + text);
					idte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					foundElement.doNoFoundElement();
					return RIA_STATE.RETURN;
				}
			} else /*if (false)*/ {
				deduceTypes2.resolveIdentIA2_(ectx/*context*/, ia, null, generatedFunction, new FoundElement(phase) {
					final String z = generatedFunction.getIdentIAPathNormal(ia);

					@Override
					public void foundElement(final OS_Element e) {
						idte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
						foundElement.doFoundElement(e);
					}

					@Override
					public void noFoundElement() {
						foundElement.noFoundElement();
						LOG.info("2002 Cant resolve " + z);
						idte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					}
				});
			}
//				assert idte.getStatus() != BaseTableEntry.Status.UNCHECKED;
			final String normal_path = generatedFunction.getIdentIAPathNormal(identIA);
			idte.resolveExpectation.satisfy(normal_path);
		} else if (idte.getStatus() == BaseTableEntry.Status.KNOWN) {
			final String normal_path = generatedFunction.getIdentIAPathNormal(identIA);
			idte.resolveExpectation.satisfy(normal_path);

			el = idte.getResolvedElement();
			ectx = el.getContext();
		}
		return RIA_STATE.NEXT;
	}

	private @NotNull RIA_STATE action_IntegerIA(@NotNull final InstructionArgument ia) {
		@NotNull final VariableTableEntry vte = generatedFunction.getVarTableEntry(DeduceTypes2.to_int(ia));
		final String text = vte.getName();
		final LookupResultList lrl = ectx.lookup(text);
		el = lrl.chooseBest(null);
		if (el != null) {
			//
			// TYPE INFORMATION IS CONTAINED IN VARIABLE DECLARATION
			//
			if (el instanceof VariableStatement) {
				@NotNull final VariableStatement vs = (VariableStatement) el;
				if (!vs.typeName().isNull()) {
					ectx = vs.typeName().getContext();
					return RIA_STATE.CONTINUE;
				}
			}
			//
			// OTHERWISE TYPE INFORMATION MAY BE IN POTENTIAL_TYPES
			//
			@NotNull final List<TypeTableEntry> pot = deduceTypes2.getPotentialTypesVte(vte);
			if (pot.size() == 1) {
				final OS_Type attached = pot.get(0).getAttached();
				if (attached != null) {
					action_001(attached);
				} else {
					action_002(pot);
				}
			}
		} else {
			errSink.reportError("1001 Can't resolve " + text);
			foundElement.doNoFoundElement();
			return RIA_STATE.RETURN;
		}
		return RIA_STATE.NEXT;
	}

	private void action_002(@NotNull final List<TypeTableEntry> aPot) {
		final TypeTableEntry tte = aPot.get(0);
		if (tte.expression instanceof ProcedureCallExpression) {
			if (tte.tableEntry != null) {
				assert tte.tableEntry instanceof ProcTableEntry;
				@NotNull final ProcTableEntry pte = (ProcTableEntry) tte.tableEntry;
				@NotNull final IdentIA x = (IdentIA) pte.expression_num;
				@NotNull final IdentTableEntry y = x.getEntry();
				if (y.getResolvedElement() == null) {
					if (y.backlink instanceof ProcIA) {
						final @NotNull ProcIA backlink_ = (ProcIA) y.backlink;
						@NotNull final ProcTableEntry backlink = generatedFunction.getProcTableEntry(backlink_.getIndex());
						final OS_Element resolvedElement = backlink.getResolvedElement();
						assert resolvedElement != null;
						try {
							final LookupResultList lrl2 = DeduceLookupUtils.lookupExpression(y.getIdent(), resolvedElement.getContext(), deduceTypes2);
							@Nullable final OS_Element best = lrl2.chooseBest(null);
							assert best != null;
							y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
						} catch (final ResolveError aResolveError) {
							errSink.reportDiagnostic(aResolveError);
							assert false;
						}
						action_002_1(pte, y);
					} else if (y.backlink instanceof IntegerIA) {
						final @NotNull IntegerIA backlink_ = (IntegerIA) y.backlink;
						@NotNull final VariableTableEntry backlink = backlink_.getEntry();
						final OS_Element resolvedElement = backlink.getResolvedElement();
						assert resolvedElement != null;
						try {
							if (resolvedElement instanceof IdentExpression) {
								backlink.typePromise().then(new DoneCallback<GenType>() {
									@Override
									public void onDone(@NotNull final GenType result) {
										try {
											final Context context = result.resolved.getClassOf().getContext();
											final LookupResultList lrl2 = DeduceLookupUtils.lookupExpression(y.getIdent(), context, deduceTypes2);
											@Nullable final OS_Element best = lrl2.chooseBest(null);
											assert best != null;
											y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
											action_002_1(pte, y);
										} catch (final ResolveError aResolveError) {
											errSink.reportDiagnostic(aResolveError);
										}
									}
								});
							} else {
								final LookupResultList lrl2 = DeduceLookupUtils.lookupExpression(y.getIdent(), resolvedElement.getContext(), deduceTypes2);
								@Nullable final OS_Element best = lrl2.chooseBest(null);
								assert best != null;
								y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
								action_002_1(pte, y);
							}
						} catch (final ResolveError aResolveError) {
							errSink.reportDiagnostic(aResolveError);
							assert false;
						}
					} else
						assert false;
				}
			}
		}
	}

	private void action_002_1(@NotNull final ProcTableEntry aPte, @NotNull final IdentTableEntry aY) {
		@Nullable FunctionInvocation fi = null;
		if (aY.getResolvedElement() instanceof ClassStatement) {
			// assuming no constructor name or generic parameters based on function syntax
			@Nullable ClassInvocation ci = new ClassInvocation((ClassStatement) aY.getResolvedElement(), null);
			ci = phase.registerClassInvocation(ci);
			fi = new FunctionInvocation(null, aPte, ci, phase.generatePhase);
		} else if (aY.getResolvedElement() instanceof FunctionDef) {
			final IInvocation invocation = deduceTypes2.getInvocation((GeneratedFunction) generatedFunction);
			fi = new FunctionInvocation((FunctionDef) aY.getResolvedElement(), aPte, invocation, phase.generatePhase);
		} else
			assert false;
		if (fi != null) {
			if (aPte.getFunctionInvocation() == null) {
				aPte.setFunctionInvocation(fi);
			}
		}
		el = aY.getResolvedElement();
		ectx = el.getContext();
	}

	private void action_001(@NotNull final OS_Type aAttached) {
		switch (aAttached.getType()) {
			case USER_CLASS: {
				final ClassStatement x = aAttached.getClassOf();
				ectx = x.getContext();
				break;
			}
			case FUNCTION: {
				final int yy = 2;
				LOG.err("1005");
				@NotNull final FunctionDef x = (FunctionDef) aAttached.getElement();
				ectx = x.getContext();
				break;
			}
			case USER:
				if (el instanceof MatchConditional.MatchArm_TypeMatch) {
					// for example from match conditional
					final TypeName tn = ((MatchConditional.MatchArm_TypeMatch) el).getTypeName();
					try {
						final @NotNull GenType ty = deduceTypes2.resolve_type(new OS_Type(tn), tn.getContext());
						ectx = ty.resolved.getElement().getContext();
					} catch (final ResolveError resolveError) {
						resolveError.printStackTrace();
						LOG.err("1182 Can't resolve " + tn);
						throw new IllegalStateException("ResolveError.");
					}
//						ectx = el.getContext();
				} else
					ectx = aAttached.getTypeName().getContext(); // TODO is this right?
				break;
			case FUNC_EXPR: {
				@NotNull final FuncExpr x = (FuncExpr) aAttached.getElement();
				ectx = x.getContext();
				break;
			}
			default:
				LOG.err("1010 " + aAttached.getType());
				throw new IllegalStateException("Don't know what you're doing here.");
		}
	}

	enum RIA_STATE {
		CONTINUE, RETURN, NEXT
	}
}

//
//
//
