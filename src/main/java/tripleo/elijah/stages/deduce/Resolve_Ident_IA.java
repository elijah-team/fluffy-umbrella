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

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.deduce.zero.ITE_Zero;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateCtor;
import tripleo.elijah.stages.gen_fn.WlGenerateDefaultCtor;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;

import java.util.Collection;
import java.util.List;

/**
 * Created 7/8/21 2:31 AM
 */
class Resolve_Ident_IA {
	private final @NotNull Context               context;
	private final @NotNull IdentIA               identIA;
	private final          BaseGeneratedFunction generatedFunction;
	private final @NotNull FoundElement          foundElement;
	private final @NotNull ErrSink               errSink;

	private final @NotNull DeduceTypes2.DeduceClient3 dc;
	private final @NotNull DeducePhase                phase;

	private final @NotNull ElLog LOG;

	@Contract(pure = true)
	public Resolve_Ident_IA(final @NotNull DeduceTypes2.DeduceClient3 aDeduceClient3,
	                        final @NotNull Context aContext,
	                        final @NotNull IdentIA aIdentIA,
	                        final BaseGeneratedFunction aGeneratedFunction,
	                        final @NotNull FoundElement aFoundElement,
	                        final @NotNull ErrSink aErrSink) {
		dc                = aDeduceClient3;
		phase             = dc.getPhase();
		context           = aContext;
		identIA           = aIdentIA;
		generatedFunction = aGeneratedFunction;
		foundElement      = aFoundElement;
		errSink           = aErrSink;
		//
		LOG = dc.getLOG();
	}

	@Nullable OS_Element el;
	Context ectx;

	public void action() throws ResolveError {
		final @NotNull List<InstructionArgument> s = BaseGeneratedFunction._getIdentIAPathList(identIA);

		ectx = context;
		el   = null;

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
		if (!process(s.get(0), s)) return;

		preUpdateStatus(s);
		updateStatus(s);
	}

	private boolean process(final InstructionArgument ia, final @NotNull List<InstructionArgument> aS) throws ResolveError {
		if (ia instanceof IntegerIA) {
			@NotNull final RIA_STATE state = action_IntegerIA(ia);
			if (state == RIA_STATE.RETURN) {
				return false;
			} else if (state == RIA_STATE.NEXT) {
				final IdentIA                  identIA2 = identIA; //(IdentIA) aS.get(1);
				final @NotNull IdentTableEntry idte     = identIA2.getEntry();

				dc.resolveIdentIA2_(context, identIA2, aS, generatedFunction, new FoundElement(phase) {
					final String z = generatedFunction.getIdentIAPathNormal(identIA2);

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
		} else if (ia instanceof IdentIA) {
			@NotNull final RIA_STATE state = action_IdentIA((IdentIA) ia);
			return state != RIA_STATE.RETURN;
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
				if (!y.preUpdateStatusListenerAdded) {
					y.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
							final ITE_Zero zero = y.zero();

							zero.preUpdateStatus_Change(eh, newStatus, foundElement, normal_path);
						}
					});
					y.preUpdateStatusListenerAdded = true;
				}
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
			if (el instanceof final @NotNull VariableStatement vs) {
				y.setStatus(BaseTableEntry.Status.KNOWN, dc.newGenericElementHolderWithType(el, vs.typeName()));
			}
			y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolderWithDC(el, dc));
		} else if (x instanceof IdentIA) {
			@NotNull final IdentTableEntry y = ((IdentIA) x).getEntry();
			assert y.getStatus() == BaseTableEntry.Status.KNOWN;
//				y.setStatus(BaseTableEntry.Status.KNOWN, el);
		} else if (x instanceof ProcIA) {
			@NotNull final ProcTableEntry y = ((ProcIA) x).getEntry();
			assert y.getStatus() == BaseTableEntry.Status.KNOWN;

			assert el != null;

			y.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
		} else
			throw new NotImplementedException();
	}

	private @NotNull RIA_STATE action_IntegerIA(@NotNull final InstructionArgument ia) {
		final @NotNull VariableTableEntry vte  = ((IntegerIA) ia).getEntry();
		final @NotNull String             text = vte.getName();

		final @NotNull LookupResultList lrl = ectx.lookup(text);
		el = lrl.chooseBest(null);

		if (el == null) {
			errSink.reportError("1001 Can't resolve " + text);
			foundElement.doNoFoundElement();
			return RIA_STATE.RETURN;
		}

		//
		// TYPE INFORMATION IS CONTAINED IN VARIABLE DECLARATION
		//
		if (el instanceof @NotNull final VariableStatement vs) {
			if (!vs.typeName().isNull()) {
				ectx = vs.typeName().getContext();
				return RIA_STATE.CONTINUE;
			}
		}
		//
		// OTHERWISE TYPE INFORMATION MAY BE IN POTENTIAL_TYPES
		//
		@NotNull final List<TypeTableEntry> pot = dc.getPotentialTypesVte(vte);
		if (pot.size() == 1) {
			final OS_Type attached = pot.get(0).getAttached();
			if (attached != null) {
				action_001(attached);
			} else {
				action_002(pot.get(0));
			}
		}

		return RIA_STATE.NEXT;
	}

	private @NotNull RIA_STATE action_IdentIA(@NotNull final IdentIA ia) {
		final @NotNull __Action_IdentIA action_identIA = new __Action_IdentIA(this, ia, foundElement, phase, generatedFunction, dc, LOG, identIA);
		action_identIA.set(el, ectx); // !!
		final RIA_STATE run = action_identIA.run();

		if (run != RIA_STATE.RETURN) {
			el   = action_identIA.el;
			ectx = action_identIA.ectx;
		}

		return run;
	}

	private void action_ProcIA(@NotNull final InstructionArgument ia) throws ResolveError {
		@NotNull final ProcTableEntry prte = ((ProcIA) ia).getEntry();
		if (prte.getResolvedElement() == null) {
			IExpression exp = prte.expression;
			if (exp instanceof final @NotNull ProcedureCallExpression pce) {
				exp = pce.getLeft(); // TODO might be another pce??!!
				if (exp instanceof ProcedureCallExpression)
					throw new IllegalStateException("double pce!");
			} else
				throw new IllegalStateException("prte resolvedElement not ProcCallExpression");
			final LookupResultList lrl = dc.lookupExpression(exp, ectx);
			el = lrl.chooseBest(null);
			assert el != null;
			ectx = el.getContext();
//					prte.setResolvedElement(el);
			prte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
			// handle constructor calls
			if (el instanceof ClassStatement) {
				_procIA_constructor_helper(prte);
			}
		} else {
			el   = prte.getResolvedElement();
			ectx = el.getContext();
		}
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
						final @NotNull GenType ty = dc.resolve_type(new OS_UserType(tn), tn.getContext());
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

	private void action_002(final @NotNull TypeTableEntry tte) {
		//>ENTRY
		//assert vte.potentailTypes().size() == 1;
		assert tte.getAttached() == null;
		//<ENTRY

		if (tte.expression instanceof ProcedureCallExpression) {
			if (tte.tableEntry != null) {
				if (tte.tableEntry instanceof @NotNull final ProcTableEntry pte) {
					@NotNull final IdentIA         x   = (IdentIA) pte.expression_num;
					@NotNull final IdentTableEntry y   = x.getEntry();
					if (y.getResolvedElement() == null) {
						action_002_no_resolved_element(pte, y);
					} else {
						final OS_Element               res = y.getResolvedElement();
						final @NotNull IdentTableEntry ite = identIA.getEntry();
						action_002_1(pte, y, true);
					}
				} else
					throw new IllegalStateException("tableEntry must be ProcTableEntry");
			}
		}
	}

	private void _procIA_constructor_helper(@NotNull final ProcTableEntry pte) {
		if (pte.getClassInvocation() != null)
			throw new IllegalStateException();

		if (pte.getFunctionInvocation() == null) {
			_procIA_constructor_helper_create_invocations(pte);
		} else {
			final FunctionInvocation fi = pte.getFunctionInvocation();
			final ClassInvocation    ci = fi.getClassInvocation();
			if (fi.getFunction() instanceof ConstructorDef) {
				@NotNull final GenType genType = new GenType(ci.getKlass());
				genType.ci = ci;
				assert ci.resolvePromise().isResolved();
				ci.resolvePromise().then(result -> genType.node = result);
				final @NotNull OS_Module         module            = ci.getKlass().getContext().module();
				final @NotNull GenerateFunctions generateFunctions = dc.getGenerateFunctions(module);
				final WorkJob                    j;
				if (fi.getFunction() == ConstructorDef.defaultVirtualCtor)
					j = new WlGenerateDefaultCtor(generateFunctions, fi, phase.codeRegistrar);
				else
					j = new WlGenerateCtor(generateFunctions, fi, null, phase.codeRegistrar);
				dc.addJobs(j);
//				generatedFunction.addDependentType(genType);
//				generatedFunction.addDependentFunction(fi);
			}
		}
	}

	private void action_002_no_resolved_element(final @NotNull ProcTableEntry pte, final @NotNull IdentTableEntry ite) {
		final InstructionArgument _backlink = ite.getBacklink();
		if (_backlink instanceof ProcIA) {
			final @NotNull ProcIA         backlink_ = (ProcIA) _backlink;
			@NotNull final ProcTableEntry backlink  = generatedFunction.getProcTableEntry(backlink_.getIndex());

			final DeduceElement3_ProcTableEntry pte_de3 = (DeduceElement3_ProcTableEntry) backlink.getDeduceElement3(this.dc._dt2(), this.generatedFunction);
			pte_de3._action_002_no_resolved_element(_backlink, backlink, dc, ite, errSink, phase);
		} else if (_backlink instanceof final @NotNull IntegerIA backlink_) {
			@NotNull final VariableTableEntry backlink  = backlink_.getEntry();

			final DeduceElement3_VariableTableEntry vte_de3 = (DeduceElement3_VariableTableEntry) backlink.getDeduceElement3();
			vte_de3._action_002_no_resolved_element(errSink, pte, ite, dc, phase);
		} else {
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
			System.err.println("=== 397 ===================================");
		}
	}

	private void action_002_1(@NotNull final ProcTableEntry pte, @NotNull final IdentTableEntry ite, final boolean setClassInvocation) {
		final OS_Element resolvedElement = ite.getResolvedElement();

		assert resolvedElement != null;

		ClassInvocation ci = null;

		if (pte.getFunctionInvocation() == null) {
			@NotNull final FunctionInvocation fi;

			if (resolvedElement instanceof ClassStatement) {
				// assuming no constructor name or generic parameters based on function syntax
				ci = new ClassInvocation((ClassStatement) resolvedElement, null);
				ci = phase.registerClassInvocation(ci);
				fi = new FunctionInvocation(null, pte, ci, phase.generatePhase);
			} else if (resolvedElement instanceof final FunctionDef functionDef) {
				final IInvocation invocation  = dc.getInvocation((GeneratedFunction) generatedFunction);
				fi = new FunctionInvocation(functionDef, pte, invocation, phase.generatePhase);
				if (functionDef.getParent() instanceof ClassStatement) {
					final ClassStatement classStatement = (ClassStatement) fi.getFunction().getParent();
					ci = new ClassInvocation(classStatement, null); // TODO generics
					ci = phase.registerClassInvocation(ci);
				}
			} else {
				throw new IllegalStateException();
			}

			if (setClassInvocation) {
				if (ci != null) {
					pte.setClassInvocation(ci);
				} else
					tripleo.elijah.util.Stupidity.println_err2("542 Null ClassInvocation");
			}

			pte.setFunctionInvocation(fi);
		}

		el   = resolvedElement;
		ectx = el.getContext();
	}

	private void _procIA_constructor_helper_create_invocations(@NotNull final ProcTableEntry pte) {
		assert el != null;

		@Nullable ClassInvocation ci = new ClassInvocation((ClassStatement) el, null);

		ci = phase.registerClassInvocation(ci);
//		prte.setClassInvocation(ci);
		final Collection<ConstructorDef> cs                   = (((ClassStatement) el).getConstructors());
		@Nullable ConstructorDef         selected_constructor = null;
		if (pte.getArgs().size() == 0 && cs.size() == 0) {
			// TODO use a virtual default ctor
			LOG.info("2262 use a virtual default ctor for " + pte.expression);
			selected_constructor = ConstructorDef.defaultVirtualCtor;
		} else {
			// TODO find a ctor that matches prte.getArgs()
			final List<TypeTableEntry> x = pte.getArgs();
			NotImplementedException.raise();
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


	static class GenericElementHolderWithDC implements IElementHolder {
		private final OS_Element                 element;
		private final DeduceTypes2.DeduceClient3 deduceClient3;

		public GenericElementHolderWithDC(final OS_Element aElement, final DeduceTypes2.DeduceClient3 aDeduceClient3) {
			element       = aElement;
			deduceClient3 = aDeduceClient3;
		}

		@Override
		public OS_Element getElement() {
			return element;
		}

		public DeduceTypes2.DeduceClient3 getDC() {
			return deduceClient3;
		}
	}

	static class __Action_IdentIA {
		private final Resolve_Ident_IA resolve_ident_ia;
		private final IdentIA          ia;

		private final @NotNull IdentTableEntry            idte;
		private final          FoundElement               foundElement;
		private final          DeducePhase                phase;
		private final          BaseGeneratedFunction      generatedFunction;
		private final          DeduceTypes2.DeduceClient3 dc;
		private final @NotNull ElLog                      LOG;
		private final          IdentIA                    identIA;
		public                 Context                    ectx;
		public                 OS_Element                 el;

		public __Action_IdentIA(final Resolve_Ident_IA aResolve_ident_ia, final IdentIA aIa, final FoundElement aFoundElement, final DeducePhase aPhase, final BaseGeneratedFunction aGeneratedFunction, final DeduceTypes2.DeduceClient3 aDc, @NotNull final ElLog aLOG, final IdentIA aIdentIA) {
			resolve_ident_ia  = aResolve_ident_ia;
			ia                = aIa;
			foundElement      = aFoundElement;
			phase             = aPhase;
			generatedFunction = aGeneratedFunction;
			dc                = aDc;
			LOG               = aLOG;
			identIA           = aIdentIA;
			//
			idte = ia.getEntry();
		}

		public RIA_STATE run() {
			if (run_one()) return RIA_STATE.RETURN;

			//assert idte.backlink == null;

			if (idte.getStatus() == BaseTableEntry.Status.UNCHECKED) {
				if (idte.getBacklink() == null) {
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
							__el_is_FunctionDef();
						} else if (el instanceof ClassStatement) {
							@NotNull final GenType genType = new GenType((ClassStatement) el);
							generatedFunction.addDependentType(genType);
						}
					}
					if (el != null) {
						while (el instanceof AliasStatement) {
							el = dc.resolveAlias((AliasStatement) el);
						}

						idte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));

						if (el.getContext() == null)
							throw new IllegalStateException("2468 null context");

						ectx = el.getContext();
					} else {
//					errSink.reportError("1179 Can't resolve " + text);
						idte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
						foundElement.doNoFoundElement();
						return RIA_STATE.RETURN;
					}
				} else /*if (false)*/ {
					__backlink_is_not_null();
				}
//				assert idte.getStatus() != BaseTableEntry.Status.UNCHECKED;
				final String normal_path = generatedFunction.getIdentIAPathNormal(identIA);
				if (idte.resolveExpectation == null) {
					tripleo.elijah.util.Stupidity.println_err2("385 idte.resolveExpectation is null for " + idte);
				} else
					idte.resolveExpectation.satisfy(normal_path);
			} else if (idte.getStatus() == BaseTableEntry.Status.KNOWN) {
				final String normal_path = generatedFunction.getIdentIAPathNormal(identIA);
				//assert idte.resolveExpectation.isSatisfied();
				if (idte.resolveExpectation != null && !idte.resolveExpectation.isSatisfied())
					idte.resolveExpectation.satisfy(normal_path);

				el   = idte.getResolvedElement();
				ectx = el.getContext();
			}

			return RIA_STATE.NEXT;
		}

		public boolean run_one() {
			if (idte.getStatus() == BaseTableEntry.Status.UNKNOWN) {
//			LOG.info("1257 Not found for " + generatedFunction.getIdentIAPathNormal(ia));
				// No need checking more than once
				if (idte.resolveExpectation != null)
					idte.resolveExpectation.fail();
				foundElement.doNoFoundElement();
				return true;
			}

			return false;
		}

		private void __el_is_FunctionDef() {
			final @NotNull FunctionDef functionDef = (FunctionDef) el;
			final OS_Element           parent      = functionDef.getParent();
			@Nullable GenType          genType     = null;
			@Nullable IInvocation      invocation  = null;

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
					generatedFunction.addDependentFunction(fi); // README program fails if this is included
				}
			}
			final ProcTableEntry callablePTE = idte.getCallablePTE();
			assert callablePTE != null;
			final @NotNull FunctionInvocation fi = dc.newFunctionInvocation((BaseFunctionDef) el, callablePTE, invocation);
			if (invocation instanceof ClassInvocation) {
				callablePTE.setClassInvocation((ClassInvocation) invocation);
			}
			callablePTE.setFunctionInvocation(fi);
			generatedFunction.addDependentFunction(fi);
		}

		private void __backlink_is_not_null() {
			dc.resolveIdentIA2_(ectx/*context*/, ia, null, generatedFunction, new FoundElement(phase) {
				final String z = generatedFunction.getIdentIAPathNormal(ia);

				@Override
				public void foundElement(final OS_Element e) {
					idte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
					foundElement.doFoundElement(e);
					dc.found_element_for_ite(generatedFunction, idte, e, ectx);
				}

				@Override
				public void noFoundElement() {
					foundElement.noFoundElement();
					LOG.info("2002 Cant resolve " + z);
					idte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
				}
			});
		}

		public void set(final OS_Element aEl, final Context aEctx) {
			el   = aEl;
			ectx = aEctx;
		}
	}

	enum RIA_STATE {
		CONTINUE, RETURN, NEXT
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
