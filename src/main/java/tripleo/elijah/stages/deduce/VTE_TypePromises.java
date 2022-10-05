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
import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.NotImplementedException;

/**
 * Created 11/27/21 12:51 PM
 */
public class VTE_TypePromises {

	// region ProcTableListener

	static void resolved_element_pte(final Constructable co,
									 final ProcTableEntry pte,
									 final AbstractDependencyTracker depTracker,
									 final @NotNull FunctionDef fd,
									 final @NotNull VariableTableEntry aVariableTableEntry,
									 final ProcTableListener aProcTableListener) {
		aVariableTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				assert result.resolved.getClassOf() == fd.getParent();

				@NotNull ProcTableListener.E_Is_FunctionDef e_Is_FunctionDef = aProcTableListener.new E_Is_FunctionDef(
						pte, fd, fd.getParent()).invoke(aVariableTableEntry.type.genType.nonGenericTypeName);
				@Nullable FunctionInvocation fi = e_Is_FunctionDef.getFi();
				GenType genType = e_Is_FunctionDef.getGenType();
				aProcTableListener.finish(co, depTracker, fi, genType);
			}
		});
	}

	static void resolved_element_pte_VariableStatement(final Constructable co,
													   final AbstractDependencyTracker depTracker,
													   final @NotNull FunctionDef fd,
													   final @NotNull VariableStatement variableStatement,
													   final @NotNull ProcTableEntry aProcTableEntry,
													   final ClassInvocation aCi,
													   final ProcTableListener aProcTableListener) {
		aCi.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(final GeneratedClass result) {
				for (GeneratedContainer.VarTableEntry varTableEntry : result.varTable) {
					if (varTableEntry.nameToken.getText().equals(variableStatement.getName())) {
						assert varTableEntry.varType.getClassOf() == fd.getParent();

						@NotNull ProcTableListener.E_Is_FunctionDef e_Is_FunctionDef = aProcTableListener.new E_Is_FunctionDef(aProcTableEntry, fd, fd.getParent()).invoke(null/*variableTableEntry.type.genType.nonGenericTypeName*/);
						@Nullable FunctionInvocation fi1 = e_Is_FunctionDef.getFi();
						GenType genType1 = e_Is_FunctionDef.getGenType();
						aProcTableListener.finish(co, depTracker, fi1, genType1);

						break;
					}
				}
			}
		});
	}

	static void resolved_element_pte_VariableStatement2(final Constructable co,
														final AbstractDependencyTracker depTracker,
														final ProcTableEntry pte,
														final @NotNull FunctionDef fd,
														final @NotNull VariableTableEntry aVariableTableEntry,
														final ProcTableListener aProcTableListener) {
		aVariableTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				if (result.resolved.getClassOf() != fd.getParent()) {
					System.err.println("** Failed assertion");
				}

				@NotNull ProcTableListener.E_Is_FunctionDef e_Is_FunctionDef = aProcTableListener.new E_Is_FunctionDef(pte, fd, fd.getParent()).invoke(aVariableTableEntry.type.genType.nonGenericTypeName);
				@Nullable FunctionInvocation fi = e_Is_FunctionDef.getFi();
				GenType genType = e_Is_FunctionDef.getGenType();
				aProcTableListener.finish(co, depTracker, fi, genType);
			}
		});
	}

	// endregion ProcTableListener

	// region DeduceTypes2

	static void getItemFali(final @NotNull BaseGeneratedFunction generatedFunction,
							final @NotNull Context ctx,
							final @NotNull VariableTableEntry aVte2,
							final @NotNull DeduceTypes2 aDeduceTypes2) {
		aVte2.typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				final @Nullable OS_Type ty2 = result.typeName/*.getAttached()*/;
				assert ty2 != null;
				@NotNull GenType rtype = null;
				try {
					rtype = aDeduceTypes2.resolve_type(ty2, ctx);
				} catch (ResolveError resolveError) {
					aDeduceTypes2.errSink.reportError("Cant resolve " + ty2); // TODO print better diagnostic
					return;
				}
				if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
					LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
					@Nullable OS_Element best2 = lrl2.chooseBest(null);
					if (best2 != null) {
						if (best2 instanceof FunctionDef) {
							@Nullable FunctionDef fd = (FunctionDef) best2;
							@Nullable ProcTableEntry pte = null;
							final IInvocation invocation = aDeduceTypes2.getInvocation((GeneratedFunction) generatedFunction);
							aDeduceTypes2.forFunction(aDeduceTypes2.newFunctionInvocation(fd, pte, invocation, aDeduceTypes2.phase), new ForFunction() {
								@Override
								public void typeDecided(final @NotNull GenType aType) {
									assert fd == generatedFunction.getFD();
									//
									@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, aDeduceTypes2.gt(aType), aVte2); // TODO expression?
									aVte2.type = tte1;
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
	}

	static Promise<GenType, Void, Void> do_assign_call_args_ident_vte_promise(final @NotNull TypeTableEntry aTte, final @NotNull VariableTableEntry aVte1) {
		final Promise<GenType, Void, Void> p = aVte1.typePromise();
		p.done(new DoneCallback<GenType>() {
			@Override
			public void onDone(GenType result) {
//					assert vte != vte1;
//					aTte.setAttached(result.resolved != null ? result.resolved : result.typeName);
				aTte.genType.copy(result);
//					vte.addPotentialType(aInstructionIndex, result); // TODO!!
			}
		});
		return p;
	}

	// endregion DeduceTypes2

	static void dunder(final String pn, final IntegerIA aIntegerIA, final ProcTableEntry pte, final DeduceTypes2 aDeduceTypes2) {
		aIntegerIA.getEntry().typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				boolean found1 = aDeduceTypes2.lookup_name_calls(result.resolved.getClassOf().getContext(), pn, pte);
				if (found1) {
					int y=2;
//					System.out.println("3071 "+pte.getStatus());
					IInvocation invocation = result.ci;
//							final BaseFunctionDef fd = gf.getFD();
					final BaseFunctionDef fd = pte.getFunctionInvocation().getFunction();
					if (pte.getFunctionInvocation() == null) {
						@NotNull FunctionInvocation fi = aDeduceTypes2.newFunctionInvocation(fd, pte, invocation, aDeduceTypes2.phase);
						pte.setFunctionInvocation(fi);
					} else
						System.out.println("175 pte.fi is not null");
					aIntegerIA.gf.addDependentFunction(pte.getFunctionInvocation()); // TODO is this needed (here)?
				} else {
					int y=3;
					System.out.println("3074");
				}
			}
		});
	}

	static void found_parent(final @NotNull DeduceTypes2.PromiseExpectation<GenType> aPromiseExpectation,
					  final BaseGeneratedFunction generatedFunction,
					  final VariableTableEntry aBte,
					  final IdentTableEntry ite,
					  final DeduceTypes2 aDeduceTypes2) {
		aBte.typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				aPromiseExpectation.satisfy(result);
				final OS_Type attached1 = result.resolved != null ? result.resolved : result.typeName;
				if (attached1 != null) {
					switch (attached1.getType()) {
					case USER_CLASS:
						if (ite.type.getAttached() == null)
							ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, attached1);
						else {
							aDeduceTypes2.LOG.err(String.format("3603 Trying to set %s to %s", ite.type.getAttached(), attached1));
						}
						break;
					case USER:
						try {
							@NotNull GenType ty3 = aDeduceTypes2.resolve_type(attached1, attached1.getTypeName().getContext());
							// no expression or TableEntryIV below
							@NotNull TypeTableEntry tte4 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
							// README trying to keep genType up to date
							tte4.setAttached(attached1);
							tte4.setAttached(ty3);
							ite.type = tte4; // or ty2?
						} catch (ResolveError aResolveError) {
							aResolveError.printStackTrace();
						}
						break;
					}
				}
			}
		});
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
