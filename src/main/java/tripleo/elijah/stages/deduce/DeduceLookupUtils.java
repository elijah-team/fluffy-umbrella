/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.Collections;
import java.util.Stack;

/**
 * Created 3/7/21 1:13 AM
 */
public class DeduceLookupUtils {
	public static LookupResultList lookupExpression(final @NotNull IExpression left,
	                                                final @NotNull Context ctx,
	                                                final @NotNull DeduceTypes2 deduceTypes2) throws ResolveError {
		switch (left.getKind()) {
			case QIDENT:
				final IExpression de = Helpers.qualidentToDotExpression2((Qualident) left);
				return lookupExpression(de, ctx, deduceTypes2)/*lookup_dot_expression(ctx, de)*/;
			case DOT_EXP:
				return lookup_dot_expression(ctx, (DotExpression) left, deduceTypes2);
			case IDENT: {
				final @NotNull IdentExpression ident = (IdentExpression) left;
				final LookupResultList         lrl   = ctx.lookup(ident.getText());
				if (lrl.results().size() == 0) {
					throw new ResolveError(ident, lrl);
				}
				return lrl;
			}
			default:
				throw new IllegalArgumentException();
		}

	}

	@Nullable
	public static OS_Element _resolveAlias(final @NotNull AliasStatement aliasStatement, @NotNull final DeduceTypes2 deduceTypes2) {
		LookupResultList lrl2;
		if (aliasStatement.getExpression() instanceof Qualident) {
			final IExpression de = Helpers.qualidentToDotExpression2(((Qualident) aliasStatement.getExpression()));
			if (de instanceof DotExpression) {
				try {
					lrl2 = lookup_dot_expression(aliasStatement.getContext(), (DotExpression) de, deduceTypes2);
				} catch (final ResolveError aResolveError) {
					aResolveError.printStackTrace();
					lrl2 = new LookupResultList();
				}
			} else
				lrl2 = aliasStatement.getContext().lookup(((IdentExpression) de).getText());
			return lrl2.chooseBest(null);
		}
		// TODO what about when DotExpression is not just simple x.y.z? then alias equivalent to val
		if (aliasStatement.getExpression() instanceof DotExpression) {
			final IExpression de = aliasStatement.getExpression();
			try {
				lrl2 = lookup_dot_expression(aliasStatement.getContext(), (DotExpression) de, deduceTypes2);
			} catch (final ResolveError aResolveError) {
				aResolveError.printStackTrace();
				lrl2 = new LookupResultList();
			}
			return lrl2.chooseBest(null);
		}
		lrl2 = aliasStatement.getContext().lookup(((IdentExpression) aliasStatement.getExpression()).getText());
		return lrl2.chooseBest(null);
	}

	@Nullable
	public static OS_Element _resolveAlias2(final @NotNull AliasStatement aliasStatement, @NotNull final DeduceTypes2 deduceTypes2) throws ResolveError {
		final LookupResultList lrl2;
		if (aliasStatement.getExpression() instanceof Qualident) {
			final IExpression de = Helpers.qualidentToDotExpression2(((Qualident) aliasStatement.getExpression()));
			if (de instanceof DotExpression) {
				lrl2 = lookup_dot_expression(aliasStatement.getContext(), (DotExpression) de, deduceTypes2);
			} else
				lrl2 = aliasStatement.getContext().lookup(((IdentExpression) de).getText());
			return lrl2.chooseBest(null);
		}
		// TODO what about when DotExpression is not just simple x.y.z? then alias equivalent to val
		if (aliasStatement.getExpression() instanceof DotExpression) {
			final IExpression de = aliasStatement.getExpression();
			lrl2 = lookup_dot_expression(aliasStatement.getContext(), (DotExpression) de, deduceTypes2);
			return lrl2.chooseBest(null);
		}
		lrl2 = aliasStatement.getContext().lookup(((IdentExpression) aliasStatement.getExpression()).getText());
		return lrl2.chooseBest(null);
	}

	private static LookupResultList lookup_dot_expression(Context ctx, final @NotNull DotExpression de, @NotNull final DeduceTypes2 deduceTypes2) throws ResolveError {
		final @NotNull Stack<IExpression> s = dot_expression_to_stack(de);
		@Nullable GenType t = null;
		IExpression ss = s.peek();
		while (/*!*/s.size() > 1/*isEmpty()*/) {
			ss = s.peek();
			if (t != null) {
				final OS_Type resolved = t.resolved;
				if (resolved != null && (resolved.getType() == OS_Type.Type.USER_CLASS || resolved.getType() == OS_Type.Type.FUNCTION))
					ctx = resolved.getClassOf().getContext();
			}
			t = deduceExpression(deduceTypes2, ss, ctx);
			if (t == null) break;
			s.pop();
		}
		{
//			s.pop();
			ss = s.peek();
		}
		if (t == null) {
			NotImplementedException.raise();
			return new LookupResultList(); // TODO throw ResolveError
		} else {
			if (t.resolved instanceof OS_UnknownType)
				return new LookupResultList(); // TODO is this right??
			if (t.resolved == null)
				return new LookupResultList(); // TODO is this right?? feb 20
			final LookupResultList lrl = t.resolved.getElement()/*.getParent()*/.getContext().lookup(((IdentExpression) ss).getText());
			return lrl;
		}
	}

	/**
	 * @see {@link tripleo.elijah.stages.deduce.DotExpressionToStackTest}
	 * @param de The {@link DotExpression} to turn into a {@link Stack}
	 * @return a "flat" {@link Stack<IExpression>} of expressions
	 */
	@NotNull
	static Stack<IExpression> dot_expression_to_stack(final @NotNull DotExpression de) {
		final @NotNull Stack<IExpression> right_stack = new Stack<IExpression>();
		IExpression right = de.getRight();
		right_stack.push(de.getLeft());
		while (right instanceof DotExpression) {
			right_stack.push(right.getLeft());
			right = ((DotExpression) right).getRight();
		}
		right_stack.push(right);
		Collections.reverse(right_stack);
		return right_stack;
	}

	public static @Nullable GenType deduceExpression(@NotNull final DeduceTypes2 aDeduceTypes2, @NotNull final IExpression n, final @NotNull Context context) throws ResolveError {
		switch (n.getKind()) {
		case IDENT:
			final Promise<GenType, ResolveError, Void> x = deduceIdentExpression_p(aDeduceTypes2, (IdentExpression) n, context);
			final GenType[] gt = new GenType[1];
			x.then(y -> gt[0] = y);
			return gt[0];
		case NUMERIC:
			final @NotNull GenType genType = new GenType();
			genType.resolved = new OS_BuiltinType(BuiltInTypes.SystemInteger);
			return genType;
		case DOT_EXP:
			final @NotNull DotExpression de = (DotExpression) n;
			final LookupResultList lrl = lookup_dot_expression(context, de, aDeduceTypes2);
			final @Nullable GenType left_type = deduceExpression(aDeduceTypes2, de.getLeft(), context);
			final @Nullable GenType right_type = deduceExpression(aDeduceTypes2, de.getRight(), left_type.resolved.getClassOf().getContext());
			NotImplementedException.raise();
			break;
		case PROCEDURE_CALL:
			@Nullable final GenType result = new GenType();
			boolean finished = false;
			tripleo.elijah.util.Stupidity.println_err2("979 During deduceProcedureCall " + n);
			@Nullable OS_Element best = null;
			try {
				best = lookup(n.getLeft(), context, aDeduceTypes2);
			} catch (final ResolveError aResolveError) {
				finished = true;// TODO should we log this?
			}
			if (!finished) {
				if (best != null) {
					final int y = 2;
					if (best instanceof ClassStatement) {
						result.resolved = ((ClassStatement) best).getOS_Type();
					} else if (best instanceof FunctionDef) {
						final @Nullable FunctionDef fd = (FunctionDef) best;
						if (fd.returnType() != null && !fd.returnType().isNull()) {
							result.resolved = new OS_UserType(fd.returnType());
						} else {
							result.resolved = new OS_UnknownType(fd);// TODO still must register somewhere
						}
					} else if (best instanceof FuncExpr) {
						final @NotNull FuncExpr funcExpr = (FuncExpr) best;
						if (funcExpr.returnType() != null && !funcExpr.returnType().isNull()) {
							result.resolved = new OS_UserType(funcExpr.returnType());
						} else {
							result.resolved = new OS_UnknownType(funcExpr);// TODO still must register somewhere
						}
					} else {
						tripleo.elijah.util.Stupidity.println_err2("992 " + best.getClass().getName());
						throw new NotImplementedException();
					}
				}
			}
			@Nullable final GenType ty = result;
			return ty/*n.getType()*/;
		case QIDENT:
			final IExpression expression = Helpers.qualidentToDotExpression2(((Qualident) n));
			return deduceExpression(aDeduceTypes2, expression, context);
		}
		return null;
	}

	public static Promise<GenType, ResolveError, Void> deduceExpression_p(@NotNull final DeduceTypes2 aDeduceTypes2, @NotNull final IExpression n, final @NotNull Context context) {
		final __deduceExpression_p p = new __deduceExpression_p();
		p.do_deduceExpression(aDeduceTypes2, n, context);
		return p.getType();
	}

	private static Promise<@Nullable GenType, ResolveError, Void> deduceProcedureCall_p(final @NotNull ProcedureCallExpression pce,
	                                                                                    final @NotNull Context ctx,
	                                                                                    final @NotNull DeduceTypes2 deduceTypes2) {
		final __deduceProcedureCall_p p = new __deduceProcedureCall_p();
		p.do_deduceProcedureCall(pce, ctx, deduceTypes2);
		return p.getType();
	}

	private static Promise<@Nullable GenType, ResolveError, Void> deduceIdentExpression_p(@NotNull final DeduceTypes2 aDeduceTypes2,
	                                                                                      final @NotNull IdentExpression ident,
	                                                                                      final @NotNull Context ctx) {
		final __deduceIdentExpression_p p = new __deduceIdentExpression_p();
		p.do_deduceIdentExpression(aDeduceTypes2, ident, ctx);
		return p.getType();
	}

	static @Nullable OS_Element lookup(@NotNull final IExpression expression, @NotNull final Context ctx, @NotNull final DeduceTypes2 deduceTypes2) throws ResolveError {
		switch (expression.getKind()) {
		case IDENT:
			final LookupResultList lrl = ctx.lookup(((IdentExpression) expression).getText());
			@Nullable final OS_Element best = lrl.chooseBest(null);
			return best;
		case PROCEDURE_CALL:
			final LookupResultList lrl2 = lookupExpression(expression.getLeft(), ctx, deduceTypes2);
			@Nullable final OS_Element best2 = lrl2.chooseBest(null);
				return best2;
			case DOT_EXP:
				final LookupResultList lrl3 = lookupExpression(expression, ctx, deduceTypes2);
				@Nullable final OS_Element best3 = lrl3.chooseBest(null);
				return best3;
//		default:
//			tripleo.elijah.util.Stupidity.println_err2("1242 "+expression);
//			throw new NotImplementedException();
			default:
				throw new IllegalStateException("1242 Unexpected value: " + expression.getKind());
		}
	}

	static class __deduceExpression_p {
		private final DeferredObject<GenType, ResolveError, Void> typePromise = new DeferredObject<>();

		Promise<GenType, ResolveError, Void> getType() {
			return typePromise;
		}

		public void do_deduceExpression(final DeduceTypes2 aDeduceTypes2, final IExpression n, final Context context) {
			try {
				switch (n.getKind()) {
				case IDENT:
					final Promise<GenType, ResolveError, Void> x = deduceIdentExpression_p(aDeduceTypes2, (IdentExpression) n, context);
					x.then(typePromise::resolve);
					x.fail(typePromise::reject);
					break;
				case NUMERIC:
					final @NotNull GenType genType = new GenType();
					genType.resolved = new OS_BuiltinType(BuiltInTypes.SystemInteger);
					typePromise.resolve(genType);
					return;
				case DOT_EXP:
					final @NotNull DotExpression de = (DotExpression) n;
					final LookupResultList lrl = lookup_dot_expression(context, de, aDeduceTypes2);
					final @Nullable GenType left_type = deduceExpression(aDeduceTypes2, de.getLeft(), context);
					final @Nullable GenType right_type = deduceExpression(aDeduceTypes2, de.getRight(), left_type.resolved.getClassOf().getContext());
					NotImplementedException.raise();
					typePromise.resolve(null);
					break;
				case PROCEDURE_CALL:
					final Promise<GenType, ResolveError, Void> x2 = deduceProcedureCall_p((ProcedureCallExpression) n, context, aDeduceTypes2);
					x2.then(typePromise::resolve);
//					x2.fail(); // TODO
					break;
				case QIDENT:
					final IExpression expression = Helpers.qualidentToDotExpression2(((Qualident) n));
					@Nullable final Promise<GenType, ResolveError, Void> x3 = deduceExpression_p(aDeduceTypes2, expression, context);
					x3.then(typePromise::resolve);
//					x3.fail(); // TODO
					break;
				}
			} catch (final ResolveError e) {
				typePromise.reject(e);
			}
		}
	}

	static class __deduceProcedureCall_p {
		private final DeferredObject<GenType, ResolveError, Void> typePromise = new DeferredObject<>();

		public void do_deduceProcedureCall(final ProcedureCallExpression pce, final Context ctx, final DeduceTypes2 deduceTypes2) {
			@Nullable final GenType result = new GenType();

			tripleo.elijah.util.Stupidity.println_err2("979 During deduceProcedureCall " + pce);
			@Nullable OS_Element best = null;
			try {
				best = lookup(pce.getLeft(), ctx, deduceTypes2);
			} catch (final ResolveError aResolveError) {
				typePromise.reject(aResolveError);
				return;
			}

			if (best != null) {
				final int y = 2;
				if (best instanceof ClassStatement) {
					result.resolved = ((ClassStatement) best).getOS_Type();
				} else if (best instanceof FunctionDef) {
					final @Nullable FunctionDef fd = (FunctionDef) best;
					if (fd.returnType() != null && !fd.returnType().isNull()) {
						result.resolved = new OS_UserType(fd.returnType());
					} else {
						result.resolved = new OS_UnknownType(fd);// TODO still must register somewhere
					}
				} else if (best instanceof FuncExpr) {
					final @NotNull FuncExpr funcExpr = (FuncExpr) best;
					if (funcExpr.returnType() != null && !funcExpr.returnType().isNull()) {
						result.resolved = new OS_UserType(funcExpr.returnType());
					} else {
						result.resolved = new OS_UnknownType(funcExpr);// TODO still must register somewhere
					}
				} else {
					tripleo.elijah.util.Stupidity.println_err2("992 " + best.getClass().getName());
					throw new NotImplementedException();
				}
			}

			typePromise.resolve(result);
		}

		public Promise<GenType, ResolveError, Void> getType() {
			return typePromise;
		}
	}

	static class __deduceIdentExpression_p {
		private final DeferredObject<GenType, ResolveError, Void> typePromise = new DeferredObject<>();

		public void do_deduceIdentExpression(final DeduceTypes2 aDeduceTypes2, final IdentExpression ident, final Context ctx) {
			try {
				@Nullable GenType       result = null;
				@Nullable final GenType R      = new GenType();

				// is this right?
				final LookupResultList lrl  = ctx.lookup(ident.getText());
				@Nullable OS_Element   best = lrl.chooseBest(null);
				while (best instanceof AliasStatement) {
					best = _resolveAlias2((AliasStatement) best, aDeduceTypes2);
				}
				if (best instanceof ClassStatement) {
					R.resolved = ((ClassStatement) best).getOS_Type();
					result     = R;
				} else {
					switch (DecideElObjectType.getElObjectType(best)) {
					case VAR:
						final @Nullable VariableStatement vs = (VariableStatement) best;
						result = do_deduceIdentExpression__VAR(aDeduceTypes2, ctx, R, vs);
						break;
					case FUNCTION:
						final @NotNull FunctionDef functionDef = (FunctionDef) best;
						result = do_deduceIdentExpression__FUNCTION(R, functionDef);
						break;
					case FORMAL_ARG_LIST_ITEM:
						final @NotNull FormalArgListItem fali = (FormalArgListItem) best;
						result = do_deduceIdentExpression__fali(aDeduceTypes2, ctx, R, fali);
						break;
					}
					if (result == null) {
						final ResolveError e = new ResolveError(ident, lrl);
						typePromise.reject(e);
						return;
					}
				}
				typePromise.resolve(result);
			} catch (final ResolveError e) {
				typePromise.reject(e);
			}
		}

		@Nullable
		private static GenType do_deduceIdentExpression__VAR(final DeduceTypes2 aDeduceTypes2, final Context ctx, final @Nullable GenType R, final @Nullable VariableStatement vs) throws ResolveError {
			final GenType[]      result = new GenType[1];
			final ResolveError[] e      = {null};

			if (!vs.typeName().isNull()) {
				@Nullable final OS_Module                           lets_hope_we_dont_need_this = null;
				@NotNull final Promise<GenType, ResolveError, Void> x                           = aDeduceTypes2.resolve_type_p(lets_hope_we_dont_need_this, new OS_UserType(vs.typeName()), ctx);
				final @Nullable GenType                             finalR                      = R;
				x.then(ty -> {
					if (ty == null) {
						finalR.typeName = new OS_UserType(vs.typeName());
						result[0]       = finalR;
					} else {
						result[0] = ty;
					}
				});
				x.fail(aResolveError -> e[0] = aResolveError);
			} else if (vs.initialValue() == IExpression.UNASSIGNED) {
				R.typeName = new OS_UnknownType(vs);
//				return deduceExpression(vs.initialValue(), ctx); // infinite recursion
				result[0] = R;
			} else {
				final Promise<GenType, ResolveError, Void> x2 = deduceExpression_p(aDeduceTypes2, vs.initialValue(), vs.getContext());
				x2.then(gt -> result[0] = gt);
			}

			if (e[0] != null) throw e[0];

			return result[0];
		}

		@NotNull
		private static GenType do_deduceIdentExpression__FUNCTION(final @NotNull GenType R, final @Nullable FunctionDef functionDef) {
			R.resolved = functionDef.getOS_Type();
			return R;
		}

		@NotNull
		private static GenType do_deduceIdentExpression__fali(final DeduceTypes2 aDeduceTypes2, final Context ctx, final @NotNull GenType R, final @Nullable FormalArgListItem fali) throws ResolveError {
			GenType result = null;

			if (!fali.typeName().isNull()) {
				@Nullable final OS_Module lets_hope_we_dont_need_this = null;
				@NotNull final GenType    ty                          = aDeduceTypes2.resolve_type(lets_hope_we_dont_need_this, new OS_UserType(fali.typeName()), ctx);
				result = ty;
				if (result == null) {
					R.typeName = new OS_UserType(fali.typeName());
				}
			} else {
				R.typeName = new OS_UnknownType(fali);
			}
			if (result == null) {
				result = R;
			}
			return result;
		}

		public Promise<GenType, ResolveError, Void> getType() {
			return typePromise;
		}
	}

/*
	@Contract("_, _ -> param1")
	public static @NotNull DeduceElement3_IdentTableEntry deduceExpression2(final @NotNull DeduceElement3_IdentTableEntry de3_ite, final FunctionContext aFc) {
		final IdentExpression    identExpression = de3_ite.principal.getIdent();
		final IdentTableEntry    ite             = new IdentTableEntry(0, identExpression, identExpression.getContext());
		final DeduceElementIdent dei             = new DeduceElementIdent(ite);

		try {
			deduceIdentExpression2(de3_ite);
		} catch (final ResolveError aE) {
			throw new RuntimeException(aE);
		}

		return de3_ite;
	}
*/

/*
	private static void deduceIdentExpression2(final @NotNull DeduceElement3_IdentTableEntry ident1) throws ResolveError {
		@Nullable GenType result = null;
		@Nullable GenType R      = ident1.genType();

		final @NotNull DeduceTypes2 dt2 = ident1.deduceTypes2();
		//assert dt2 == aDeduceTypes2;

		final @NotNull IdentExpression ident = ident1.principal.getIdent();
		final Context                  ctx   = ident.getContext();

		// is this right?
		final LookupResultList lrl  = ctx.lookup(ident.getText());
		@Nullable OS_Element   best = lrl.chooseBest(null);
		while (best instanceof AliasStatement) {
			best = _resolveAlias2((AliasStatement) best, dt2);
		}
		if (best instanceof ClassStatement) {
			R.resolved = ((ClassStatement) best).getOS_Type();
			result     = R;
		} else {
			switch (DecideElObjectType.getElObjectType(best)) {
				case VAR:
					final @Nullable VariableStatement vs = (VariableStatement) best;
					if (!vs.typeName().isNull()) {
						try {
							@Nullable final OS_Module lets_hope_we_dont_need_this = null;
							@NotNull final GenType    ty                          = dt2.resolve_type(lets_hope_we_dont_need_this, new OS_Type(vs.typeName()), ctx);
							result = ty;
						} catch (final ResolveError aResolveError) {
							// TODO This is the cheap way to do it
							//  Ideally, we would propagate this up the call chain all the way to lookupExpression
							aResolveError.printStackTrace();
						}
						if (result == null) {
							R.typeName = new OS_Type(vs.typeName());
							result     = R;
						}
					} else if (vs.initialValue() == IExpression.UNASSIGNED) {
						R.typeName = new OS_UnknownType(vs);
//				return deduceExpression(vs.initialValue(), ctx); // infinite recursion
					} else {
						final @NotNull IExpression initialValue = vs.initialValue();
						if (initialValue.getKind() == ExpressionKind.PROCEDURE_CALL) {
							final Context                 vsContext = vs.getContext();
							final ProcedureCallExpression pce       = (ProcedureCallExpression) initialValue;
							R = deduceExpression(dt2, pce, vsContext);
						} else {
							R = deduceExpression(dt2, initialValue, vs.getContext());
						}
					}
					if (result == null) {
						result = R;
					}
					break;
				case FUNCTION:
					final @NotNull FunctionDef functionDef = (FunctionDef) best;
					R.resolved = functionDef.getOS_Type();
					result = R;
					break;
				case FORMAL_ARG_LIST_ITEM:
					final @NotNull FormalArgListItem fali = (FormalArgListItem) best;
					if (!fali.typeName().isNull()) {
						try {
							@Nullable final OS_Module lets_hope_we_dont_need_this = null;
							@NotNull final GenType    ty                          = dt2.resolve_type(lets_hope_we_dont_need_this, new OS_Type(fali.typeName()), ctx);
							result = ty;
						} catch (final ResolveError aResolveError) {
							// TODO This is the cheap way to do it
							//  Ideally, we would propagate this up the call chain all the way to lookupExpression
							aResolveError.printStackTrace();
						}
						if (result == null) {
							R.typeName = new OS_Type(fali.typeName());
						}
					} else {
						R.typeName = new OS_UnknownType(fali);
					}
					if (result == null) {
						result = R;
					}
					break;
			}
			if (result == null) {
				throw new ResolveError(ident, lrl);
			}
		}
	}
*/
}


//
//
//
