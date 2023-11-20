/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
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
	public static LookupResultList lookupExpression(final @NotNull IExpression left, final @NotNull Context ctx, @NotNull final DeduceTypes2 deduceTypes2) throws ResolveError {
		switch (left.getKind()) {
		case QIDENT:
			final IExpression de = Helpers.qualidentToDotExpression2((Qualident) left);
			return lookupExpression(de, ctx, deduceTypes2)/*lookup_dot_expression(ctx, de)*/;
		case DOT_EXP:
			return lookup_dot_expression(ctx, (DotExpression) left, deduceTypes2);
		case IDENT:
			{
				final @NotNull IdentExpression ident = (IdentExpression) left;
				final LookupResultList lrl = ctx.lookup(ident.getText());
				if (lrl.results().size() == 0) {
					throw new ResolveError(ident,  lrl);
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
			return deduceIdentExpression(aDeduceTypes2, (IdentExpression) n, context);
		case NUMERIC:
			final @NotNull GenType genType = new GenType();
			genType.resolved = new OS_Type(BuiltInTypes.SystemInteger);
			return genType;
		case DOT_EXP:
			final @NotNull DotExpression de = (DotExpression) n;
			final LookupResultList lrl = lookup_dot_expression(context, de, aDeduceTypes2);
			final @Nullable GenType left_type = deduceExpression(aDeduceTypes2, de.getLeft(), context);
			final @Nullable GenType right_type = deduceExpression(aDeduceTypes2, de.getRight(), left_type.resolved.getClassOf().getContext());
			NotImplementedException.raise();
			break;
		case PROCEDURE_CALL:
			@Nullable final GenType ty = deduceProcedureCall((ProcedureCallExpression) n, context, aDeduceTypes2);
			return ty/*n.getType()*/;
		case QIDENT:
			final IExpression expression = Helpers.qualidentToDotExpression2(((Qualident) n));
			return deduceExpression(aDeduceTypes2, expression, context);
		}
		return null;
	}

	/**
	 * Try to find the type of a ProcedureCall. Will either be a constructor or function call, most likely
	 *
	 * @param pce the procedure call
	 * @param ctx the context to use for lookup
	 * @param deduceTypes2
	 * @return the deduced type or {@code null}. Do not {@code pce.setType}
	 */
	private static @Nullable GenType deduceProcedureCall(final @NotNull ProcedureCallExpression pce, final @NotNull Context ctx, @NotNull final DeduceTypes2 deduceTypes2) {
		@Nullable final GenType result = new GenType();
		boolean finished = false;
		System.err.println("979 During deduceProcedureCall " + pce);
		@Nullable OS_Element best = null;
		try {
			best = lookup(pce.getLeft(), ctx, deduceTypes2);
		} catch (final ResolveError aResolveError) {
			finished = true;// TODO should we log this?
		}
		if (!finished) {
			if (best != null) {
				final int y = 2;
				if (best instanceof ClassStatement) {
					result.resolved = new OS_Type((ClassStatement) best);
				} else if (best instanceof FunctionDef) {
					final @Nullable FunctionDef fd = (FunctionDef) best;
					if (fd.returnType() != null && !fd.returnType().isNull()) {
						result.resolved = new OS_Type(fd.returnType());
					} else {
						result.resolved = new OS_UnknownType(fd);// TODO still must register somewhere
					}
				} else if (best instanceof FuncExpr) {
					final @NotNull FuncExpr funcExpr = (FuncExpr) best;
					if (funcExpr.returnType() != null && !funcExpr.returnType().isNull()) {
						result.resolved = new OS_Type(funcExpr.returnType());
					} else {
						result.resolved = new OS_UnknownType(funcExpr);// TODO still must register somewhere
					}
				} else {
					System.err.println("992 " + best.getClass().getName());
					throw new NotImplementedException();
				}
			}
		}
		return result;
	}

	private static @Nullable GenType deduceIdentExpression(@NotNull final DeduceTypes2 aDeduceTypes2, final @NotNull IdentExpression ident, final @NotNull Context ctx) throws ResolveError {
		@Nullable GenType result = null;
		@Nullable GenType R = new GenType();

		// is this right?
		final LookupResultList lrl = ctx.lookup(ident.getText());
		@Nullable OS_Element best = lrl.chooseBest(null);
		while (best instanceof AliasStatement) {
			best = _resolveAlias2((AliasStatement) best, aDeduceTypes2);
		}
		if (best instanceof ClassStatement) {
			R.resolved = new OS_Type((ClassStatement) best);
			result = R;
		} else {
			switch (DecideElObjectType.getElObjectType(best)) {
			case VAR:
				final @Nullable VariableStatement vs = (VariableStatement) best;
				if (!vs.typeName().isNull()) {
					try {
						@Nullable final OS_Module lets_hope_we_dont_need_this = null;
						@NotNull final GenType ty = aDeduceTypes2.resolve_type(lets_hope_we_dont_need_this, new OS_Type(vs.typeName()), ctx);
						result = ty;
					} catch (final ResolveError aResolveError) {
						// TODO This is the cheap way to do it
						//  Ideally, we would propagate this up the call chain all the way to lookupExpression
						aResolveError.printStackTrace();
					}
					if (result == null) {
						R.typeName = new OS_Type(vs.typeName());
						result = R;
					}
				} else if (vs.initialValue() == IExpression.UNASSIGNED) {
					R.typeName = new OS_UnknownType(vs);
//				return deduceExpression(vs.initialValue(), ctx); // infinite recursion
				} else {
					R = deduceExpression(aDeduceTypes2, vs.initialValue(), vs.getContext());
				}
				if (result == null) {
					result = R;
				}
				break;
			case FUNCTION:
				final @NotNull FunctionDef functionDef = (FunctionDef) best;
				R.resolved = new OS_FuncType(functionDef);
				result = R;
				break;
			case FORMAL_ARG_LIST_ITEM:
				final @NotNull FormalArgListItem fali = (FormalArgListItem) best;
				if (!fali.typeName().isNull()) {
					try {
						@Nullable final OS_Module lets_hope_we_dont_need_this = null;
						@NotNull final GenType ty = aDeduceTypes2.resolve_type(lets_hope_we_dont_need_this, new OS_Type(fali.typeName()), ctx);
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
		return result;
	}

	static @Nullable OS_Element lookup(@NotNull final IExpression expression, @NotNull final Context ctx, @NotNull final DeduceTypes2 deduceTypes2) throws ResolveError {
		switch (expression.getKind()) {
		case IDENT:
			final LookupResultList lrl = ctx.lookup(((IdentExpression)expression).getText());
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
//			System.err.println("1242 "+expression);
//			throw new NotImplementedException();
		default:
			throw new IllegalStateException("1242 Unexpected value: " + expression.getKind());
		}
	}
}

//
//
//
