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
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 9/10/21 3:42 AM
 */
public class ProcTableListener implements BaseTableEntry.StatusListener {
	private final ProcTableEntry pte;
	private final BaseGeneratedFunction generatedFunction;

	private final DeduceTypes2.@NotNull DeduceClient2 dc;
	private final @NotNull ElLog LOG;

	public ProcTableListener(final ProcTableEntry pte, final BaseGeneratedFunction generatedFunction, final DeduceTypes2.@NotNull DeduceClient2 dc) {
		this.pte = pte;
		this.generatedFunction = generatedFunction;
		this.dc = dc;
		//
		LOG = dc.getLOG();
	}

	@Override
	public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
		@Nullable Constructable co = null;
		if (eh instanceof ConstructableElementHolder) {
			final @NotNull ConstructableElementHolder constructableElementHolder = (ConstructableElementHolder) eh;
			co = constructableElementHolder.getConstructable();
		}
		if (newStatus != BaseTableEntry.Status.UNKNOWN) { // means eh is null
			@Nullable final AbstractDependencyTracker depTracker;
			if (co instanceof IdentIA) {
				final @NotNull IdentIA identIA = (IdentIA) co;
				depTracker = identIA.gf;
			} else if (co instanceof IntegerIA) {
				final @Nullable IntegerIA integerIA = (IntegerIA) co;
				depTracker = integerIA.gf;
			} else
				depTracker = null;

			set_resolved_element_pte(co, eh.getElement(), pte, depTracker);
		}
	}

	void set_resolved_element_pte(final @Nullable Constructable co,
								  final OS_Element e,
								  final @NotNull ProcTableEntry pte,
								  final AbstractDependencyTracker depTracker) {
		@Nullable ClassInvocation ci;
		final FunctionInvocation fi;
		@Nullable final GenType genType = null;

//		pte.setResolvedElement(e); // README already done
		if (e instanceof ClassStatement) {
			ci = new ClassInvocation((ClassStatement) e, null);
			ci = dc.registerClassInvocation(ci);
			fi = dc.newFunctionInvocation(ConstructorDef.defaultVirtualCtor, pte, ci); // TODO might not be virtual ctor, so check
			pte.setFunctionInvocation(fi);

			if (co != null) {
				co.setConstructable(pte);
				ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						co.resolveTypeToClass(result);
					}
				});
			}
		} else if (e instanceof FunctionDef) {
			@NotNull final FunctionDef fd = (FunctionDef) e;
			resolved_element_pte_FunctionDef(co, pte, depTracker, fd);
		} else {
			LOG.err("845 Unknown element for ProcTableEntry " + e);
			return;
		}
	}

	private void resolved_element_pte_FunctionDef(final Constructable co, @NotNull final ProcTableEntry pte, final AbstractDependencyTracker depTracker, @NotNull final FunctionDef fd) {
		@Nullable final FunctionInvocation fi;
		final GenType genType;
		if (pte.expression_num != null) {
			final DeducePath dp = ((IdentIA) pte.expression_num).getEntry().buildDeducePath(generatedFunction);

			if (dp.size() > 1) {
				@Nullable final OS_Element el_self = dp.getElement(dp.size() - 2);

				final @Nullable OS_Element parent = el_self;
				if (parent instanceof IdentExpression) {
					resolved_element_pte_FunctionDef_IdentExpression(co, pte, depTracker, fd, (IdentExpression) parent);
				} else if (parent instanceof FormalArgListItem) {
					resolved_element_pte_FunctionDef_FormalArgListItem(co, pte, depTracker, fd, (FormalArgListItem) parent);
				} else if (parent instanceof VariableStatement) {
					resolved_element_pte_FunctionDef_VariableStatement(co, pte, depTracker, fd, (VariableStatement) parent);
				} else {
					@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(null);
					fi = e_Is_FunctionDef.getFi();
					if (fi != null) { // TODO
						genType = e_Is_FunctionDef.getGenType();
						finish(co, depTracker, fi, genType);
					}
				}
			} else {
				final OS_Element parent = fd.getParent();
				@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(null);
				fi = e_Is_FunctionDef.getFi();
				genType = e_Is_FunctionDef.getGenType();
				finish(co, depTracker, fi, genType);
			}
		} else {
			final OS_Element parent = pte.getResolvedElement(); // for dunder methods

			assert parent != null;

			resolved_element_pte_FunctionDef_dunder(co, depTracker, pte, fd, parent);
		}
	}

	private void resolved_element_pte_FunctionDef_dunder(final Constructable co,
                                                         final AbstractDependencyTracker depTracker,
                                                         @NotNull final ProcTableEntry pte,
                                                         @NotNull final FunctionDef fd,
                                                         OS_Element parent) {
		@Nullable final FunctionInvocation fi;
		final GenType genType;
		if (parent instanceof IdentExpression) {
			@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) parent).getText());
			assert vte_ia != null;
			final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
			variableTableEntry.typePromise().then(new DoneCallback<GenType>() {
				@Override
				public void onDone(@NotNull final GenType result) {
					assert result.resolved.getClassOf() == fd.getParent();

					@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(variableTableEntry.type.genType.nonGenericTypeName);
					@Nullable final FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
					final GenType genType1 = e_Is_FunctionDef.getGenType();
					finish(co, depTracker, fi1, genType1);
				}
			});
		} else {
			@Nullable TypeName typeName = null;

			if (fd == parent) {
				parent = fd.getParent();
				final TypeTableEntry x = pte.getArgs().get(0);
				// TODO highly specialized condition...
				if (x.getAttached() == null && x.tableEntry == null) {
					final String text = ((IdentExpression) x.expression).getText();
					@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(text);
					if (vte_ia != null) {
						final GenType gt = ((IntegerIA) vte_ia).getEntry().type.genType;
						typeName = gt.nonGenericTypeName != null ? gt.nonGenericTypeName : gt.typeName.getTypeName();
					} else {
						if (parent instanceof ClassStatement) {
							// TODO might be wrong in the case of generics. check.
							typeName = null;//new OS_Type((ClassStatement) parent);
						}
					}
				}
			}

			@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(typeName);
			fi = e_Is_FunctionDef.getFi();
			genType = e_Is_FunctionDef.getGenType();
			finish(co, depTracker, fi, genType);
		}
	}

	private void resolved_element_pte_FunctionDef_VariableStatement(final Constructable co, final ProcTableEntry pte, final AbstractDependencyTracker depTracker, final FunctionDef fd, final VariableStatement parent) {
		final VariableStatement variableStatement = parent;
		// TODO lookupVariableStatement?
		//  we really want DeduceVariableStatement < DeduceElement (with type/promise)
		@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(variableStatement.getName());

		if (vte_ia != null) {
			final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
			variableTableEntry.typePromise().then(new DoneCallback<GenType>() {
				@Override
				public void onDone(@NotNull final GenType result) {
					assert result.resolved.getClassOf() == fd.getParent();

					@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(variableTableEntry.type.genType.nonGenericTypeName);
					@Nullable final FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
					final GenType genType1 = e_Is_FunctionDef.getGenType();
					finish(co, depTracker, fi1, genType1);
				}
			});
		} else {
			final List<IdentTableEntry> entries = generatedFunction.idte_list
					.stream()
					.filter(ite -> ite.getIdent().getText().equals(variableStatement.getName()))
					.collect(Collectors.toList());

			if (entries.size() == 1) {
				final IdentTableEntry identTableEntry = entries.get(0);

				identTableEntry.typePromise().then(new DoneCallback<GenType>() {
					@Override
					public void onDone(@NotNull final GenType result) {
						assert result.resolved.getClassOf() == fd.getParent();

						@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(identTableEntry.type.genType.nonGenericTypeName);
						@Nullable final FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
						final GenType genType1 = e_Is_FunctionDef.getGenType();
						finish(co, depTracker, fi1, genType1);
					}
				});
			}
		}
	}

	private void resolved_element_pte_FunctionDef_FormalArgListItem(final Constructable co, final ProcTableEntry pte, final AbstractDependencyTracker depTracker, @NotNull final FunctionDef fd, final FormalArgListItem parent) {
		final FormalArgListItem fali = parent;
		@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(fali.name());
		assert vte_ia != null;
		final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
		variableTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull final GenType result) {
				assert result.resolved.getClassOf() == fd.getParent();

				@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(variableTableEntry.type.genType.nonGenericTypeName);
				@Nullable final FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
				final GenType genType1 = e_Is_FunctionDef.getGenType();
				finish(co, depTracker, fi1, genType1);
			}
		});
	}

	private void resolved_element_pte_FunctionDef_IdentExpression(final Constructable co, final ProcTableEntry pte, final AbstractDependencyTracker depTracker, @NotNull final FunctionDef fd, @NotNull final IdentExpression parent) {
		@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(parent.getText());
		assert vte_ia != null;
		final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
		variableTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull final GenType result) {
				assert result.resolved.getClassOf() == fd.getParent();

				@NotNull final E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(variableTableEntry.type.genType.nonGenericTypeName);
				@Nullable final FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
				final GenType genType1 = e_Is_FunctionDef.getGenType();
				finish(co, depTracker, fi1, genType1);
			}
		});
	}

	private void finish(@Nullable final Constructable co, @Nullable final AbstractDependencyTracker depTracker, @NotNull final FunctionInvocation aFi, @Nullable final GenType aGenType) {
		if (co != null && aGenType != null)
			co.setGenType(aGenType);

		if (depTracker != null) {
			if (aGenType == null && aFi.getFunction() == null) {
				// README Assume constructor
				final @NotNull ClassStatement c = aFi.getClassInvocation().getKlass();
				final @NotNull GenType genType2 = new GenType(c);
				depTracker.addDependentType(genType2);
			} else {
				depTracker.addDependentFunction(aFi);
				if (aGenType != null)
					depTracker.addDependentType(aGenType);
			}
		}
	}

	class E_Is_FunctionDef {
        private final ProcTableEntry pte;
        private final FunctionDef fd;
        private final OS_Element parent;
        private @Nullable FunctionInvocation fi;
        private GenType genType;

        public E_Is_FunctionDef(final ProcTableEntry pte, final FunctionDef aFd, final OS_Element aParent) {
            this.pte = pte;
            fd = aFd;
            parent = aParent;
        }

        public @Nullable FunctionInvocation getFi() {
			return fi;
		}

		public GenType getGenType() {
			return genType;
		}

		/**
		 * Create genType and set ci; set fi
		 *
		 * @param typeName an optional typename, used for generics in {@code genCI}
		 * @return a "function object" with genType and hopefully fi set
		 */
		/* @ensures genType != null && genType.ci != null; */
		/* @ ///// ensures fi != null ; */
		public @NotNull E_Is_FunctionDef invoke(final TypeName typeName) {
			@Nullable final ClassInvocation ci;
			if (parent instanceof NamespaceStatement) {
				final @NotNull NamespaceStatement namespaceStatement = (NamespaceStatement) parent;
				genType = new GenType(namespaceStatement);
				final NamespaceInvocation nsi = dc.registerNamespaceInvocation(namespaceStatement);
//				pte.setNamespaceInvocation(nsi);
				genType.ci = nsi;
				fi = dc.newFunctionInvocation(fd, pte, nsi);
			} else if (parent instanceof ClassStatement) {
				final @NotNull ClassStatement classStatement = (ClassStatement) parent;
				genType = new GenType(classStatement);
//							ci = new ClassInvocation(classStatement, null);
//							ci = phase.registerClassInvocation(ci);
//							genType.ci = ci;
				ci = dc.genCI(genType, typeName);
				pte.setClassInvocation(ci);
				fi = dc.newFunctionInvocation(fd, pte, ci);
			} else if (parent instanceof FunctionDef) {
				if (pte.expression_num == null) {
					// TODO need the instruction to get args from FnCallArgs
					fi = null;
				}
			} else
				throw new IllegalStateException("Unknown parent");
			if (fi != null)
				pte.setFunctionInvocation(fi);
			return this;
		}
	}

}

//
//
//
