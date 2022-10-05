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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.logging.ElLog;

/**
 * Created 9/2/21 11:36 PM
 */
class Found_Element_For_ITE {

	private final BaseGeneratedFunction generatedFunction;
	private final Context ctx;
	private final ElLog LOG;
	private final ErrSink errSink;
	private final DeduceTypes2.DeduceClient1 dc;

	public Found_Element_For_ITE(BaseGeneratedFunction aGeneratedFunction, Context aCtx, ElLog aLOG, ErrSink aErrSink, DeduceTypes2.DeduceClient1 aDeduceClient1) {
		generatedFunction = aGeneratedFunction;
		ctx = aCtx;
		LOG = aLOG;
		errSink = aErrSink;
		dc = aDeduceClient1;
	}

	public void action(@NotNull IdentTableEntry ite) {
		final OS_Element y = ite.getResolvedElement();

		if (y instanceof VariableStatement) {
			action_VariableStatement(ite, (VariableStatement) y);
		} else if (y instanceof ClassStatement) {
			action_ClassStatement(ite, (ClassStatement) y);
		} else if (y instanceof FunctionDef) {
			action_FunctionDef(ite, (FunctionDef) y);
		} else if (y instanceof PropertyStatement) {
			action_PropertyStatement(ite, (PropertyStatement) y);
		} else if (y instanceof AliasStatement) {
			action_AliasStatement(ite, (AliasStatement) y);
		} else {
			//LookupResultList exp = lookupExpression();
			LOG.info("2009 " + y);
			return;
		}

		final String normal_path = generatedFunction.getIdentIAPathNormal(new IdentIA(ite.getIndex(), generatedFunction));
		if (!ite.resolveExpectation.isSatisfied())
			ite.resolveExpectation.satisfy(normal_path);
	}

	public void action_AliasStatement(@NotNull IdentTableEntry ite, @NotNull AliasStatement y) {
		LOG.err("396 AliasStatement");
		@Nullable OS_Element x = dc._resolveAlias(y);
		if (x == null) {
			ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
			errSink.reportError("399 resolveAlias returned null");
		} else {
			ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
			dc.found_element_for_ite(generatedFunction, ite, x, ctx);
		}
	}

	public void action_PropertyStatement(@NotNull IdentTableEntry ite, @NotNull PropertyStatement ps) {
		OS_Type attached;
		switch (ps.getTypeName().kindOfType()) {
			case GENERIC:
				attached = new OS_Type(ps.getTypeName());
				break;
			case NORMAL:
				try {
					attached = (dc.resolve_type(new OS_Type(ps.getTypeName()), ctx).resolved.getClassOf()).getOS_Type();
				} catch (ResolveError resolveError) {
					LOG.err("378 resolveError");
					resolveError.printStackTrace();
					return;
				}
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + ps.getTypeName().kindOfType());
		}
		if (ite.type == null) {
			ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, attached);
		} else
			ite.type.setAttached(attached);
		dc.genCIForGenType2(ite.type.genType);
		int yy = 2;
	}

	public void action_FunctionDef(@NotNull IdentTableEntry ite, FunctionDef functionDef) {
		@NotNull OS_Type attached = functionDef.getOS_Type();
		if (ite.type == null) {
			ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, attached);
		} else
			ite.type.setAttached(attached);
	}

	public void action_ClassStatement(@NotNull IdentTableEntry ite, ClassStatement classStatement) {
		@NotNull OS_Type attached = classStatement.getOS_Type();
		if (ite.type == null) {
			ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, attached);
		} else
			ite.type.setAttached(attached);
	}

	public void action_VariableStatement(@NotNull IdentTableEntry ite, @NotNull VariableStatement vs) {
		@NotNull TypeName typeName = vs.typeName();
		if (ite.type == null || ite.type.getAttached() == null) {
			if (!(typeName.isNull())) {
				if (ite.type == null)
					ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, vs.initialValue());
				ite.type.setAttached(new OS_Type(typeName));
			} else {
				final OS_Element parent = vs.getParent().getParent();
				if (parent instanceof NamespaceStatement || parent instanceof ClassStatement) {
					boolean state;
					if (generatedFunction instanceof GeneratedFunction) {
						final @NotNull GeneratedFunction generatedFunction1 = (GeneratedFunction) generatedFunction;
						state = (parent != generatedFunction1.getFD().getParent());
					} else {
						state = (parent != ((GeneratedConstructor) generatedFunction).getFD().getParent());
					}
					if (state) {
						final IInvocation invocation = dc.getInvocationFromBacklink(ite.getBacklink());
						final @NotNull DeferredMember dm = dc.deferred_member(parent, invocation, vs, ite);
						dm.typePromise().
								done(new DoneCallback<GenType>() {
									@Override
									public void onDone(@NotNull GenType result) {
										if (ite.type == null)
											ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, vs.initialValue());
										assert result.resolved != null;
										if (result.ci == null) {
											genCIForGenType(result);
										}
										ite.setGenType(result);
										if (ite.fefi) {
											ite.fefiDone(result);
										}

										final String normal_path = generatedFunction.getIdentIAPathNormal(new IdentIA(ite.getIndex(), generatedFunction));
										ite.resolveExpectation.satisfy(normal_path);
									}
								});
					} else {
						IInvocation invocation;
						if (ite.getBacklink() == null) {
							if (parent instanceof ClassStatement) {
								final ClassStatement classStatement = (ClassStatement) parent;
								final @Nullable ClassInvocation ci = dc.registerClassInvocation(classStatement, null);
								assert ci != null;
								invocation = ci;
							} else {
								invocation = null; // TODO shouldn't be null
							}
						} else {
							invocation = dc.getInvocationFromBacklink(ite.getBacklink());
						}
						final @NotNull DeferredMember dm = dc.deferred_member(parent, invocation, vs, ite);
						dm.typePromise().then(new DoneCallback<GenType>() {
							@Override
							public void onDone(final GenType result) {
								if (ite.type == null)
									ite.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, vs.initialValue());
								assert result.resolved != null;
								ite.setGenType(result);
//								ite.resolveTypeToClass(result.node); // TODO setting this has no effect on output

								final String normal_path = generatedFunction.getIdentIAPathNormal(new IdentIA(ite.getIndex(), generatedFunction));
								ite.resolveExpectation.satisfy(normal_path);
							}
						});
					}

					@Nullable GenType genType = null;
					if (parent instanceof NamespaceStatement)
						genType = new GenType((NamespaceStatement) parent);
					else if (parent instanceof ClassStatement)
						genType = new GenType((ClassStatement) parent);

					generatedFunction.addDependentType(genType);
				}
//				LOG.err("394 typename is null " + vs.getName());
			}
		}
	}

	/**
	 * Sets the invocation ({@code genType#ci}) and the node for a GenType
	 *
	 * @param aGenType the GenType to modify.
	 */
	public void genCIForGenType(final GenType aGenType) {
		//assert aGenType.nonGenericTypeName != null ;//&& ((NormalTypeName) aGenType.nonGenericTypeName).getGenericPart().size() > 0;

		dc.genCI(aGenType, aGenType.nonGenericTypeName);
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
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
