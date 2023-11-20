/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import antlr.Token;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.contexts.MatchConditionalContext;
import tripleo.elijah.contexts.MatchContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tripleo
 *
 * Created 	Apr 15, 2020 at 10:11:16 PM
 */
public class MatchConditional implements OS_Element, StatementItem, FunctionItem {

//	private final SingleIdentContext _ctx;
	private final List<MC1> parts = new ArrayList<MC1>();
	private IExpression expr;
	private OS_Element parent;
	private MatchContext __ctx;

	public MatchConditional(final OS_Element parent, final Context parentContext) {
		this.parent = parent;
//		this._ctx = new SingleIdentContext(parentContext, this);
	}

	public List<MC1> getParts() {
		return parts;
	}

	/**
	 * @category OS_Element
	 */
	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitMatchConditional(this);
	}

	/**
	 * @category OS_Element
	 */
	@Override
	public OS_Element getParent() {
		return this.parent;
	}

	public void setParent(final OS_Element aParent) {
		this.parent = aParent;
	}

	// region OS_Element

	@Override
	public Context getContext() {
		return __ctx;
	}

	public void setContext(final MatchContext ctx) {
		__ctx = ctx;
	}

	// endregion

	public void postConstruct() {
	}

	//
	// EXPR
	//

	public IExpression getExpr() {
		return expr;
	}

	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	//
	//
	//
	public MatchArm_TypeMatch typeMatch() {
		final MatchArm_TypeMatch p = new MatchArm_TypeMatch();
		parts.add(p);
		return p;
	}

	public MatchConditionalPart2 normal() {
		final MatchConditionalPart2 p = new MatchConditionalPart2();
		parts.add(p);
		return p;
	}

	public MatchConditionalPart3 valNormal() {
		final MatchConditionalPart3 p = new MatchConditionalPart3();
		parts.add(p);
		return p;
	}

	public interface MC1 extends OS_Element, Documentable {
		void add(FunctionItem aItem);

		@Override
		Context getContext();

		Iterable<? extends FunctionItem> getItems();

		@Override
		default void visitGen(final ElElementVisitor visit) {
			visit.visitMC1(this);
		}
	}

	public class MatchConditionalPart3 implements MC1 {

		private final Context ___ctx = new MatchConditionalContext(MatchConditional.this.getContext(), this);

//		private final List<FunctionItem> items = new ArrayList<FunctionItem>();
private final List<Token> docstrings = null;
        private IdentExpression matching_expression;
		private Scope3 scope3;

		public void expr(final IdentExpression expr) {
			this.matching_expression = expr;
		}

		@Override
		public Context getContext() {
			return ___ctx;
		}

		@Override
		public void add(final FunctionItem aItem) {
			scope3.add(aItem);
			//items.add(aItem);
		}

		@Override
		public void addDocString(final Token text) {
//			if (docstrings == null)
//				docstrings = new ArrayList<Token>();
//			docstrings.add(text);
			scope3.addDocString(text);
		}

		@Override
		public List<FunctionItem> getItems() {
			final List<FunctionItem> collection = new ArrayList<FunctionItem>();
			for (final OS_Element element : scope3.items()) {
				if (element instanceof FunctionItem)
					collection.add((FunctionItem) element);
			}
			return collection;
//			return items;
		}

		@Override
		public OS_Element getParent() {
			return MatchConditional.this;
		}

		public void scope(final Scope3 sco) {
			scope3=sco;
		}
	}

	public class MatchConditionalPart2 implements MC1 {

		private final Context ___ctx = new MatchConditionalContext(MatchConditional.this.getContext(), this);

//		private final List<FunctionItem> items = new ArrayList<FunctionItem>();
//		private List<Token> docstrings = new ArrayList<Token>();
		private IExpression matching_expression;
		private Scope3 scope3;

		public IExpression getMatchingExpression() {
			return matching_expression;
		}

		public void expr(final IExpression expr) {
			this.matching_expression = expr;
		}

		@Override
		public void add(final FunctionItem aItem) {
			scope3.add(aItem);
			//items.add(aItem);
		}

		@Override
		public void addDocString(final Token text) {
//			if (docstrings == null)
//				docstrings = new ArrayList<Token>();
//			docstrings.add(text);
			scope3.addDocString(text);
		}

		@Override
		public List<FunctionItem> getItems() {
			final List<FunctionItem> collection = new ArrayList<FunctionItem>();
			for (final OS_Element element : scope3.items()) {
				if (element instanceof FunctionItem)
					collection.add((FunctionItem) element);
			}
			return collection;
//			return items;
		}

		@Override
		public Context getContext() {
			return ___ctx;
		}

		@Override
		public OS_Element getParent() {
			return MatchConditional.this;
		}

		public void scope(final Scope3 sco) {
			scope3 = sco;
		}
	}

	public class MatchArm_TypeMatch implements MC1 {

//		private final List<FunctionItem> items = new ArrayList<FunctionItem>();
		private final Context ___ctx = new MatchConditionalContext(//MatchConditional.this.getContext(), this);
			getParent().getParent().getContext(), this);

		TypeName tn /*= new RegularTypeName()*/;
//		private List<Token> docstrings = new ArrayList<Token>();
		private IdentExpression ident;
		private Scope3 scope3;

		public void ident(final IdentExpression i1) {
			this.ident = i1;
		}

		@Override
		public void add(final FunctionItem aItem) {
			scope3.add(aItem);
			//items.add(aItem);
		}

		@Override
		public void addDocString(final Token text) {
//			if (docstrings == null)
//				docstrings = new ArrayList<Token>();
//			docstrings.add(text);
			scope3.addDocString(text);
		}

		@Override
		public List<FunctionItem> getItems() {
			final List<FunctionItem> collection = new ArrayList<FunctionItem>();
			for (final OS_Element element : scope3.items()) {
				if (element instanceof FunctionItem)
					collection.add((FunctionItem) element);
			}
			return collection;
//			return items;
		}

		public TypeName getTypeName() {
			return tn;
		}

		public void setTypeName(final TypeName typeName) {
			tn = typeName;
		}

		public IdentExpression getIdent() {
			return ident;
		}

		@Override
		public @NotNull Context getContext() {
			return ___ctx;
		}

		@Override
		public OS_Element getParent() {
			return MatchConditional.this;
		}

		public void scope(final Scope3 sco) {
			scope3 = sco;
		}
	}

}

//
//
//
