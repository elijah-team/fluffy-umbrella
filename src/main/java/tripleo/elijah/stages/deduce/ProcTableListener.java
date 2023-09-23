/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.deduce.post_bytecode.DG_ClassStatement;
import tripleo.elijah.stages.gen_fn.AbstractDependencyTracker;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.Constructable;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.world.WorldGlobals;

/**
 * Created 9/10/21 3:42 AM
 */
public class ProcTableListener implements BaseTableEntry.StatusListener {
	class E_Is_FunctionDef {
		private final     FunctionDef        fd;
		private final     OS_Element         parent;
		private final     ProcTableEntry     pte;
		private @Nullable FunctionInvocation fi;
		private           GenType            genType;

		public E_Is_FunctionDef(ProcTableEntry pte, FunctionDef aFd, OS_Element aParent) {
			this.pte = pte;
			fd       = aFd;
			parent   = aParent;
		}

		private void ci_null__fi_not_null(final TypeName typeName) {
			if (parent instanceof final @NotNull ClassStatement classStatement) {
				parentIsClass(classStatement, typeName);
			} else if (parent instanceof final @NotNull NamespaceStatement namespaceStatement) {
				parentIsNamespace(namespaceStatement);
			}
		}

		private void ci_null__fi_null(final TypeName typeName) {
			if (parent instanceof final @NotNull NamespaceStatement namespaceStatement) {
				parentIsNamespace(namespaceStatement);
			} else if (parent instanceof final @NotNull ClassStatement classStatement) {
				parentIsClass(classStatement, typeName);
			} else if (parent instanceof FunctionDef) {
				if (pte.expression_num == null) {
					// TODO need the instruction to get args from FnCallArgs
					fi = null;
				}
			} else
				throw new IllegalStateException("Unknown parent");
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
		public @NotNull E_Is_FunctionDef invoke(TypeName typeName) {
			if (pte.getClassInvocation() == null && pte.getFunctionInvocation() == null) {
				@NotNull ClassInvocation ci;
				ci_null__fi_null(typeName);
				if (fi != null)
					pte.setFunctionInvocation(fi);
			} else if (pte.getClassInvocation() == null && pte.getFunctionInvocation() != null) {
				ci_null__fi_not_null(typeName);
			} else {
				// don't create _inj().new_objects when alrady populated
				genType = new GenType();
				final ClassInvocation classInvocation = pte.getClassInvocation();
				genType.setResolved(classInvocation.getKlass().getOS_Type());
				genType.setCi(classInvocation);
				fi = pte.getFunctionInvocation();
			}
			return this;
		}

		private void parentIsClass(final @NotNull ClassStatement classStatement, final TypeName typeName) {

			CI_Hint hint = null;

			BaseFunctionDef currentFunction = generatedFunction.getFD();
			OS_Element      currentClass0   = currentFunction.getParent();

			if (currentClass0 instanceof ClassStatement currentClass) {
				Map<TypeName, ClassStatement> inh = currentClass.getContext().inheritance();
				if (inh.containsValue(fd.getParent())) {
					// the function referenced by pte.expression is an inherited method
					// defined in genType.resolved and called in generatedFunction
					// for whatever reason we don't have a CodePoint (gf, instruction...)

					hint = new InheritedMethodCalledFromInheritee(fd, classStatement, generatedFunction);
				}
			}


			genType = new GenType(classStatement);
			//							ci = _inj().new_ClassInvocation(classStatement, null);
			//							ci = phase.registerClassInvocation(ci);
			//							genType.ci = ci;
			@NotNull ClassInvocation ci = dc.genCI(genType, typeName);
			pte.setClassInvocation(ci);
			fi = dc.newFunctionInvocation(fd, pte, ci);

			if (hint != null) {
				ci.hint = hint;
				fi.hint = hint;
			}
		}

		private void parentIsNamespace(final @NotNull NamespaceStatement namespaceStatement) {
			genType = GenType.of(namespaceStatement, () -> dc.registerNamespaceInvocation(namespaceStatement));
			fi      = dc.newFunctionInvocation(fd, pte, genType.getCi());
		}
	}
	public static class PTE_Resolution {
		public void apply(final ProcTableEntry aProcTableEntry) {

		}
	}
	private static void resolved_element_pte_ClassStatement_EvaClass(final EvaClass result, final @NotNull ClassStatement e, final @NotNull Constructable co, final @NotNull DG_ClassStatement dcs) {
		//System.err.println("828282 "+((ClassStatement) e).name());
		if ((e.name()).equals("Foo")) {
			System.out.println("828282 Foo found");
		}

		co.resolveTypeToClass(result);

		dcs.attachClass(result); // T168-089
	}
	private final          DeduceTypes2.@NotNull DeduceClient2 dc;

	private final          BaseEvaFunction                     generatedFunction;

	private final @NotNull ElLog                               LOG;

	private final          ProcTableEntry                      pte;

	public ProcTableListener(ProcTableEntry pte, BaseEvaFunction generatedFunction, DeduceTypes2.@NotNull DeduceClient2 dc) {
		this.pte               = pte;
		this.generatedFunction = generatedFunction;
		this.dc                = dc;
		//
		LOG = dc.getLOG();
	}

	private void __resolved_element_pte_FunctionDef_IdentIA(final Constructable co,
															final @NotNull ProcTableEntry pte,
															final AbstractDependencyTracker depTracker,
															final @NotNull FunctionDef fd,
															final @NotNull IdentIA num) {
		DeducePath dp = num.getEntry().buildDeducePath(generatedFunction);

		GenType                      genType;
		@Nullable FunctionInvocation fi;

		if (dp.size() > 1) {
			@Nullable OS_Element el_self = dp.getElement(dp.size() - 2);

			final @Nullable OS_Element parent = el_self;
			if (parent instanceof IdentExpression) {
				resolved_element_pte_FunctionDef_IdentExpression(co, pte, depTracker, fd, (IdentExpression) parent);
			} else if (parent instanceof FormalArgListItem) {
				resolved_element_pte_FunctionDef_FormalArgListItem(co, pte, depTracker, fd, (FormalArgListItem) parent);
			} else if (parent instanceof VariableStatement) {
				@Nullable OS_Element          p;
				@Nullable InstructionArgument ia;
				if (dp.size() > 2) {
					p  = dp.getElement(dp.size() - 3);
					ia = dp.getIA(dp.size() - 3);
				} else {
					p  = null;
					ia = null;
				}
				resolved_element_pte_FunctionDef_VariableStatement(co, depTracker, pte, fd, p, ia, (VariableStatement) parent);
			} else {
				@NotNull E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(null);
				fi = e_Is_FunctionDef.getFi();
				if (fi != null) { // TODO
					genType = e_Is_FunctionDef.getGenType();
					// NOTE read note below
					genType.setResolved(fd.getOS_Type());
					genType.setFunctionInvocation(fi); // DeduceTypes2.Dependencies#action_type
					finish(co, depTracker, fi, genType);
				}
			}
		} else {
			final OS_Element          parent           = fd.getParent();
			@NotNull E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(null);
			fi      = e_Is_FunctionDef.getFi();
			genType = e_Is_FunctionDef.getGenType();
			// NOTE genType.ci will likely come out as a ClassInvocation here
			//  This is incorrect when pte.expression points to a Function(Def)
			//  It is actually correct, but what I mean is that genType.resolved
			//  will come out as a USER_CLASS when it should be FUNCTION
			//
			//  So we correct it here
			genType.setResolved(fd.getOS_Type());
			genType.setFunctionInvocation(fi); // DeduceTypes2.Dependencies#action_type
			finish(co, depTracker, fi, genType);
		}
	}

	void finish(@Nullable Constructable co, @Nullable AbstractDependencyTracker depTracker, @NotNull FunctionInvocation aFi, @Nullable GenType aGenType) {
		if (co != null && aGenType != null)
			co.setGenType(aGenType);

		if (depTracker != null) {
			if (aGenType == null)
				tripleo.elijah.util.Stupidity.println_err_2("247 genType is null");

			if (/*aGenType == null &&*/ aFi.getFunction() instanceof ConstructorDef) {
				final @NotNull ClassStatement c        = aFi.getClassInvocation().getKlass();
				final @NotNull GenType        genType2 = new GenType(c);
				depTracker.addDependentType(genType2);
				// TODO why not add fi?
			} else {
				depTracker.addDependentFunction(aFi);
				if (aGenType != null)
					depTracker.addDependentType(aGenType);
			}
		}
	}

	@Override
	public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
		@Nullable Constructable co = null;
		if (eh instanceof final @NotNull ConstructableElementHolder constructableElementHolder) {
			co = constructableElementHolder.getConstructable();
		}
		if (newStatus != BaseTableEntry.Status.UNKNOWN) { // means eh is null
			@Nullable AbstractDependencyTracker depTracker;
			if (co instanceof final @NotNull IdentIA identIA) {
				depTracker = (AbstractDependencyTracker) identIA.gf;
			} else if (co instanceof final @Nullable IntegerIA integerIA) {
				depTracker = (AbstractDependencyTracker) integerIA.gf;
			} else
				depTracker = null;

			set_resolved_element_pte(co, eh.getElement(), pte, depTracker);

			final InstructionArgument expressionNum = pte.expression_num;

			if (expressionNum instanceof IdentIA identIA) {
				final IdentTableEntry entry = identIA.getEntry();
				final DR_Ident        ident = generatedFunction.getIdent(entry);
				ident.resolve(eh, pte);
			} else {
				System.err.println("*************************** i still refuse");
			}
		}
	}

	private void resolved_element_pte_ClassStatement(final @Nullable Constructable co,
													 final @NotNull ClassStatement e,
													 final @NotNull ProcTableEntry pte) {
		FunctionInvocation        fi;
		@Nullable ClassInvocation ci;

		final DG_ClassStatement dcs = dc.deduceTypes2().DG_ClassStatement(e);

		ci = dcs.classInvocation();
		ci = dc.registerClassInvocation(ci);
		fi = dc.newFunctionInvocation(WorldGlobals.defaultVirtualCtor, pte, ci); // TODO might not be virtual ctor, so check
		pte.setFunctionInvocation(fi);

		final IdentTableEntry entry = ((IdentIA) pte.expression_num).getEntry();
		generatedFunction.getIdent(entry).resolve(dcs);

		dcs.attach(fi, pte);

		if (co != null) {
			co.setConstructable(pte);
			ci.resolvePromise().done((EvaClass result) -> {
				resolved_element_pte_ClassStatement_EvaClass(result, e, co, dcs);
			});
		}
	}

	private void resolved_element_pte_FunctionDef(Constructable co, @NotNull ProcTableEntry pte, AbstractDependencyTracker depTracker, @NotNull FunctionDef fd) {
		if (pte.expression_num != null) {
			if (pte.expression_num instanceof final @NotNull IdentIA num) {
				__resolved_element_pte_FunctionDef_IdentIA(co, pte, depTracker, fd, num);

				final DR_Ident dr_ident = generatedFunction.getIdent(num.getEntry());
				dr_ident.resolve();

			} else if (pte.expression_num instanceof final @NotNull IntegerIA integerIA) {
				final @NotNull VariableTableEntry variableTableEntry = integerIA.getEntry();

				VTE_TypePromises.resolved_element_pte(co, pte, depTracker, fd, variableTableEntry, this);

				int y = 2;
			}
		} else {
			OS_Element parent = pte.getResolvedElement(); // for dunder methods

			assert parent != null;

			resolved_element_pte_FunctionDef_dunder(co, depTracker, pte, fd, parent);
		}
	}

	private void resolved_element_pte_FunctionDef_dunder(Constructable co,
														 AbstractDependencyTracker depTracker,
														 @NotNull ProcTableEntry pte,
														 @NotNull FunctionDef fd,
														 OS_Element parent) {
		@Nullable FunctionInvocation fi;
		GenType                      genType;
		if (parent instanceof IdentExpression) {
			@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) parent).getText());
			assert vte_ia != null;
			final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
			VTE_TypePromises.resolved_element_pte(co, pte, depTracker, fd, variableTableEntry, this);
		} else {
			@Nullable TypeName typeName = null;

			if (fd == parent) {
				parent = fd.getParent();
				TypeTableEntry x = pte.getArgs().get(0);
				// TODO highly specialized condition...
				if (x.getAttached() == null && x.tableEntry == null) {
					String                        text   = ((IdentExpression) x.__debug_expression()).getText();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(text);
					if (vte_ia != null) {
						GenType gt = ((IntegerIA) vte_ia).getEntry().getType().genType;
						typeName = gt.getNonGenericTypeName() != null ? gt.getNonGenericTypeName() : gt.getTypeName().getTypeName();
					} else {
						if (parent instanceof ClassStatement) {
							// TODO might be wrong in the case of generics. check.
							typeName = null;//_inj().new_OS_Type((ClassStatement) parent);
							tripleo.elijah.util.Stupidity.println_err_2("NOTE ineresting in genericA/__preinc__");
						}
					}
				}
			}

			@NotNull E_Is_FunctionDef e_Is_FunctionDef = new E_Is_FunctionDef(pte, fd, parent).invoke(typeName);
			fi      = e_Is_FunctionDef.getFi();
			genType = e_Is_FunctionDef.getGenType();
			finish(co, depTracker, fi, genType);
		}
	}

	private void resolved_element_pte_FunctionDef_FormalArgListItem(Constructable co, ProcTableEntry pte, AbstractDependencyTracker depTracker, @NotNull FunctionDef fd, FormalArgListItem parent) {
		final FormalArgListItem       fali   = parent;
		@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(fali.name());
		assert vte_ia != null;
		final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
		VTE_TypePromises.resolved_element_pte(co, pte, depTracker, fd, variableTableEntry, this);
	}

	private void resolved_element_pte_FunctionDef_IdentExpression(Constructable co, ProcTableEntry pte, AbstractDependencyTracker depTracker, @NotNull FunctionDef fd, @NotNull IdentExpression parent) {
		@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(parent.getText());
		assert vte_ia != null;
		final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
		VTE_TypePromises.resolved_element_pte(co, pte, depTracker, fd, variableTableEntry, this);
	}

	private void resolved_element_pte_FunctionDef_VariableStatement(final Constructable co,
																	final AbstractDependencyTracker depTracker,
																	final ProcTableEntry pte,
																	final @NotNull FunctionDef fd,
																	final @Nullable OS_Element parent,
																	final @Nullable InstructionArgument ia,
																	final @NotNull VariableStatement variableStatement) {
		if (ia != null) {
			if (ia instanceof IdentIA) {
				@NotNull IdentTableEntry identTableEntry = ((IdentIA) ia).getEntry();
				int                      y               = 2;
			} else if (ia instanceof final @NotNull ProcIA procIA) {
				final @NotNull ProcTableEntry procTableEntry = procIA.getEntry();

				final ClassInvocation ci = procTableEntry.getFunctionInvocation().getClassInvocation();
				if (ci != null) {
					VTE_TypePromises.resolved_element_pte_VariableStatement(co, depTracker, fd, variableStatement, procTableEntry, ci, this);
				} else {
					assert false;
				}
			} else {
				int y = 2;
			}
			return;
		}
		// TODO lookupVariableStatement?
		//  we really want DeduceVariableStatement < DeduceElement (with type/promise)
		@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(variableStatement.getName());
		assert vte_ia != null;
		final @NotNull VariableTableEntry variableTableEntry = ((IntegerIA) vte_ia).getEntry();
		VTE_TypePromises.resolved_element_pte_VariableStatement2(co, depTracker, pte, fd, variableTableEntry, this);
	}

	void set_resolved_element_pte(final @Nullable Constructable co,
								  final OS_Element e,
								  final @NotNull ProcTableEntry pte,
								  final AbstractDependencyTracker depTracker) {
		if (e instanceof ClassStatement) {
			resolved_element_pte_ClassStatement(co, (ClassStatement) e, pte);
		} else if (e instanceof @NotNull final FunctionDef fd) {
			resolved_element_pte_FunctionDef(co, pte, depTracker, fd);
		} else {
			LOG.err("845 Unknown element for ProcTableEntry " + e);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
