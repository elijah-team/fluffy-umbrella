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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.stages.gen_fn.WlGenerateNamespace;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;

/**
 * Created 9/5/21 2:54 AM
 */
class Resolve_Variable_Table_Entry {
	private final BaseGeneratedFunction generatedFunction;
	private final Context               ctx;

	private final          DeduceTypes2 deduceTypes2;
	private final @NotNull ElLog        LOG;
	private final @NotNull WorkManager  wm;
	private final @NotNull DeducePhase  phase;
	private final          ErrSink      errSink;

	public Resolve_Variable_Table_Entry(final BaseGeneratedFunction aGeneratedFunction, final Context aCtx, final DeduceTypes2 aDeduceTypes2) {
		generatedFunction = aGeneratedFunction;
		ctx               = aCtx;
		deduceTypes2      = aDeduceTypes2;
		//
		LOG     = deduceTypes2.LOG;
		wm      = deduceTypes2.wm;
		errSink = deduceTypes2.errSink;
		phase   = deduceTypes2.phase;
	}

	public void action(final @NotNull VariableTableEntry vte, final @NotNull DeduceTypes2.IVariableConnector aConnector) {
		switch (vte.vtt) {
		case ARG:
			action_ARG(vte);
			break;
		case VAR:
			action_VAR(vte);
			break;
		}
		aConnector.connect(vte, vte.getName());
	}

	public void action_VAR(@NotNull final VariableTableEntry vte) {
		if (vte.type.getAttached() == null && vte.potentialTypes.size() == 1) {
			final TypeTableEntry pot = new ArrayList<>(vte.potentialTypes()).get(0);
			if (pot.getAttached() instanceof OS_FuncExprType) {
				action_VAR_potsize_1_and_FuncExprType(vte, (OS_FuncExprType) pot.getAttached(), pot.genType, pot.expression);
			} else if (pot.getAttached() != null && pot.getAttached().getType() == OS_Type.Type.USER_CLASS) {
				final int y = 1;
				vte.type = pot;
				vte.resolveType(pot.genType);
			} else {
				action_VAR_potsize_1_other(vte, pot);
			}
		}
	}

	public void action_VAR_potsize_1_other(@NotNull final VariableTableEntry vte, @NotNull final TypeTableEntry aPot) {
		try {
			if (aPot.tableEntry instanceof ProcTableEntry) {
				final @NotNull ProcTableEntry pte1 = (ProcTableEntry) aPot.tableEntry;
				@Nullable final OS_Element    e    = DeduceLookupUtils.lookup(pte1.expression, ctx, deduceTypes2);
				if (e == null) {
					System.err.println("** 97: " + pte1.expression);
					return;
					//throw new AssertionError();
				}
				if (e instanceof FunctionDef) {
//						final FunctionDef fd = (FunctionDef) e;
					@NotNull final IdentTableEntry ite1 = ((IdentIA) pte1.expression_num).getEntry();
					final DeducePath               dp   = ite1.buildDeducePath(generatedFunction);
					@Nullable final GenType        t    = dp.getType(dp.size() - 1);
					ite1.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
					pte1.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
					pte1.typePromise().then(new DoneCallback<GenType>() {
						@Override
						public void onDone(@NotNull final GenType result) {
							if (t == null) {
								ite1.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, result.resolved);
								ite1.setGenType(result);
							} else {
//								assert false; // we don't expect this, but note there is no problem if it happens
								t.copy(result);
							}
						}
					});
					final int y = 2;
				} else {
					vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
					pte1.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, vte));
//					vte.setCallablePTE(pte1);

					final GenType gt = aPot.genType;
					setup_GenType(e, gt);
//					if (gt.node == null)
//						gt.node = vte.genType.node;

					vte.genType.copy(gt);
				}
				final int y = 2;
			} else if (aPot.tableEntry == null) {
				final OS_Element el = vte.getResolvedElement();
				if (el instanceof VariableStatement) {
					final VariableStatement variableStatement = (VariableStatement) el;
					action_VAR_pot_1_tableEntry_null(variableStatement);
				}
			} else
				throw new NotImplementedException();
		} catch (final ResolveError aResolveError) {
			errSink.reportDiagnostic(aResolveError);
		}
	}

	public void action_VAR_pot_1_tableEntry_null(final VariableStatement aVariableStatement) {
		final @NotNull IExpression iv = aVariableStatement.initialValue();
		if (iv != IExpression.UNASSIGNED) {
			if (iv instanceof ProcedureCallExpression) {
				final ProcedureCallExpression procedureCallExpression = (ProcedureCallExpression) iv;
				final IExpression name_exp = procedureCallExpression.getLeft();
				assert name_exp instanceof IdentExpression;

				final IdentExpression name2 = (IdentExpression) name_exp;
				final LookupResultList lrl2 = ((IdentExpression) name_exp).getContext().lookup(name2.getText());
				final @Nullable OS_Element el2 = lrl2.chooseBest(null);

				if (el2 != null) {
					if (el2 instanceof ClassStatement) {
						final ClassStatement classStatement = (ClassStatement) el2;
						final GenType        genType        = new GenType(classStatement);
						//deferredMember.typeResolved().resolve(genType);
						genCIForGenType2(genType);
					}
				}
			} else
				assert false;
		} else
			assert false;
	}

	public void setup_GenType(final OS_Element element, @NotNull final GenType aGt) {
		if (element instanceof NamespaceStatement) {
			final @NotNull NamespaceStatement namespaceStatement = (NamespaceStatement) element;
			aGt.resolvedn = (NamespaceStatement) element;
			final NamespaceInvocation nsi = phase.registerNamespaceInvocation(namespaceStatement);
//			pte.setNamespaceInvocation(nsi);
			aGt.ci = nsi;
//			fi = newFunctionInvocation(fd, pte, nsi, phase);
		} else if (element instanceof ClassStatement) {
			final @NotNull ClassStatement classStatement = (ClassStatement) element;
			aGt.resolved = ((ClassStatement) element).getOS_Type();
			// TODO genCI ??
			@Nullable ClassInvocation ci = new ClassInvocation(classStatement, null);
			ci = phase.registerClassInvocation(ci);
			aGt.ci = ci;
//			pte.setClassInvocation(ci);
//			fi = newFunctionInvocation(fd, pte, ci, phase);
		} else if (element instanceof FunctionDef) {
			// TODO this seems to be an elaborate copy of the above with no differentiation for functionDef
			final @NotNull FunctionDef  functionDef = (FunctionDef) element;
			final OS_Element            parent      = functionDef.getParent();
			@Nullable final IInvocation inv;
			switch (DecideElObjectType.getElObjectType(parent)) {
				case CLASS:
					aGt.resolved = ((ClassStatement) parent).getOS_Type();
					inv = phase.registerClassInvocation((ClassStatement) parent, null);
					((ClassInvocation) inv).resolveDeferred().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							result.functionMapDeferred(functionDef, new FunctionMapDeferred() {
								@Override
								public void onNotify(final GeneratedFunction aGeneratedFunction) {
									aGt.node = aGeneratedFunction;
								}
							});
						}
				});
				break;
			case NAMESPACE:
				aGt.resolvedn = (NamespaceStatement) parent;
				inv = phase.registerNamespaceInvocation((NamespaceStatement) parent);
				((NamespaceInvocation)inv).resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
					@Override
					public void onDone(final GeneratedNamespace result) {
						result.functionMapDeferred(functionDef, new FunctionMapDeferred() {
							@Override
							public void onNotify(final GeneratedFunction aGeneratedFunction) {
								aGt.node = aGeneratedFunction;
							}
						});
					}
				});
				break;
			default:
				throw new NotImplementedException();
			}
			aGt.ci = inv;
		} else if (element instanceof AliasStatement) {
			@Nullable OS_Element el = element;
			while (el instanceof AliasStatement) {
				el = DeduceLookupUtils._resolveAlias((AliasStatement) el, deduceTypes2);
			}
			setup_GenType(el, aGt);
		} else // TODO will fail on FunctionDef's
			throw new IllegalStateException("Unknown parent");
	}

	public void action_VAR_potsize_1_and_FuncExprType(@NotNull final VariableTableEntry vte,
	                                                  @NotNull final OS_FuncExprType funcExprType,
	                                                  @NotNull final GenType aGenType,
	                                                  final IExpression aPotentialExpression) {
		aGenType.typeName = funcExprType;

		final @NotNull FuncExpr fe = (FuncExpr) funcExprType.getElement();

		// add namespace
		@NotNull final OS_Module           mod1   = fe.getContext().module();
		@Nullable final NamespaceStatement mod_ns = lookup_module_namespace(mod1);

		@Nullable ProcTableEntry callable_pte = null;

		if (mod_ns != null) {
			// add func_expr to namespace
			@NotNull final FunctionDef fd1 = new FunctionDef(mod_ns, mod_ns.getContext());
			fd1.setFal(fe.fal());
			fd1.setContext((FunctionContext) fe.getContext());
			fd1.scope(fe.getScope());
			fd1.setSpecies(BaseFunctionDef.Species.FUNC_EXPR);
//			tripleo.elijah.util.Stupidity.println_out("1630 "+mod_ns.getItems()); // element 0 is ctor$0
			fd1.setName(IdentExpression.forString(String.format("$%d", mod_ns.getItems().size() + 1)));

			@NotNull final WorkList            wl   = new WorkList();
			@NotNull final GenerateFunctions   gen  = phase.generatePhase.getGenerateFunctions(mod1);
			@NotNull final NamespaceInvocation modi = new NamespaceInvocation(mod_ns);
			final @Nullable ProcTableEntry     pte  = findProcTableEntry(generatedFunction, aPotentialExpression);
			assert pte != null;
			callable_pte = pte;
			@NotNull final FunctionInvocation fi = new FunctionInvocation(fd1, pte, modi, phase.generatePhase);
			wl.addJob(new WlGenerateNamespace(gen, modi, phase.generatedClasses, phase.codeRegistrar)); // TODO hope this works (for more than one)
			final @Nullable WlGenerateFunction wlgf = new WlGenerateFunction(gen, fi, phase.codeRegistrar);
			wl.addJob(wlgf);
			wm.addJobs(wl);
			wm.drain(); // TODO here?

			aGenType.ci   = modi;
			aGenType.node = wlgf.getResult();

			final DeduceTypes2.@NotNull PromiseExpectation<GenType> pe = deduceTypes2.promiseExpectation(/*pot.genType.node*/new DeduceTypes2.ExpectationBase() {
				@Override
				public @NotNull String expectationString() {
					return "FuncType..."; // TODO
				}
			}, "FuncType Result");
			((GeneratedFunction) aGenType.node).onType(new DoneCallback<GenType>() {
				@Override
				public void onDone(final GenType result) {
					pe.satisfy(result);
					vte.resolveType(result);
				}
			});

			//vte.typeDeferred().resolve(pot.genType); // this is wrong
		}

		if (callable_pte != null)
			vte.setCallablePTE(callable_pte);
	}

	private @Nullable ProcTableEntry findProcTableEntry(@NotNull final BaseGeneratedFunction aGeneratedFunction, final IExpression aExpression) {
		for (@NotNull final ProcTableEntry procTableEntry : aGeneratedFunction.prte_list) {
			if (procTableEntry.expression == aExpression)
				return procTableEntry;
		}
		return null;
	}

	public @Nullable NamespaceStatement lookup_module_namespace(@NotNull final OS_Module aModule) {
		try {
			final @NotNull IdentExpression module_ident = IdentExpression.forString("__MODULE__");
			@Nullable final OS_Element     e            = DeduceLookupUtils.lookup(module_ident, aModule.getContext(), deduceTypes2);
			if (e != null) {
				if (e instanceof NamespaceStatement) {
					return (NamespaceStatement) e;
				} else {
					LOG.err("__MODULE__ should be namespace");
					return null;
				}
			} else {
				// not found, so add. this is where AST would come in handy
				@NotNull final NamespaceStatement ns = new NamespaceStatement(aModule, aModule.getContext());
				ns.setName(module_ident);
				return ns;
			}
		} catch (final ResolveError aResolveError) {
//			LOG.err("__MODULE__ should be namespace");
			errSink.reportDiagnostic(aResolveError);
			return null;
		}
	}

	public void action_ARG(@NotNull final VariableTableEntry vte) {
		final TypeTableEntry tte      = vte.type;
		final OS_Type        attached = tte.getAttached();
		if (attached != null) {
			switch (attached.getType()) {
				case USER:
					if (tte.genType.typeName == null)
						tte.genType.typeName = attached;
					try {
						tte.genType.copy(deduceTypes2.resolve_type(attached, ctx));
						tte.setAttached(tte.genType.resolved); // TODO probably not necessary, but let's leave it for now
					} catch (final ResolveError aResolveError) {
						errSink.reportDiagnostic(aResolveError);
						LOG.err("Can't resolve argument type " + attached);
						return;
					}
					if (generatedFunction.fi.getClassInvocation() != null)
						genNodeForGenType(tte.genType, generatedFunction.fi.getClassInvocation());
					else
						genCIForGenType(tte.genType);
					vte.resolveType(tte.genType);
					break;
				case USER_CLASS:
					if (tte.genType.resolved == null)
						tte.genType.resolved = attached;
					// TODO genCI and all that -- Incremental?? (.increment())
					vte.resolveType(tte.genType);
					genCIForGenType2(tte.genType);
					break;
			}
		} else {
			final int y = 2;
		}
	}

	/**
	 * Sets the invocation ({@code genType#ci}) and the node for a GenType
	 *
	 * @param aGenType the GenType to modify. must be set to a nonGenericTypeName that is non-null and generic
	 */
	public void genCIForGenType(final GenType aGenType) {
		assert aGenType.nonGenericTypeName != null;//&& ((NormalTypeName) aGenType.nonGenericTypeName).getGenericPart().size() > 0;

		aGenType.genCI(aGenType.nonGenericTypeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
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

	/**
	 * Sets the node for a GenType, given an invocation
	 *
	 * @param aGenType the GenType to modify. must be set to a nonGenericTypeName that is non-null and generic
	 */
	public void genNodeForGenType(final GenType aGenType, final IInvocation invocation) {
		assert aGenType.nonGenericTypeName != null;

//		final IInvocation invocation = aGenType.ci;
		assert aGenType.ci == null || aGenType.ci == invocation;
		aGenType.ci = invocation;
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

	/**
	 * Sets the invocation ({@code genType#ci}) and the node for a GenType
	 *
	 * @param aGenType the GenType to modify. doesn;t care about  nonGenericTypeName
	 */
	public void genCIForGenType2(final GenType aGenType) {
		aGenType.genCI(aGenType.nonGenericTypeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
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
}

//
//
//
