/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.gen.ICodeGen;
import tripleo.elijah.util.NotImplementedException;

public class AliasStatement implements ModuleItem, ClassItem, FunctionItem, OS_Element2, Resolvable {
	private final OS_Element parent;
	private IExpression expr;
//	private String name;
	private OS_Element _resolvedElement;
	private IdentExpression nameToken;

	public AliasStatement(final OS_Element aParent) {
		this.parent = aParent;
		if (parent instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else {
			throw new IllegalStateException("adding AliasStatement to " + aParent.getClass().getName());
		}
	}

	public void setExpression(final IExpression expr) {
		if (expr.getKind() != ExpressionKind.IDENT &&
				    expr.getKind() != ExpressionKind.QIDENT &&
				    expr.getKind() != ExpressionKind.DOT_EXP) // TODO need DOT_EXP to QIDENT
		{
			throw new NotImplementedException();
//			System.out.println(String.format("[AliasStatement#setExpression] %s %s", expr, expr.getKind()));
		}
		this.expr = expr;
	}

	public IExpression getExpression() {
		return expr;
	}

	public void setName(@NotNull final IdentExpression i1) {
		this.nameToken = i1;
	}

	@Override // OS_Element
	public void visitGen(final ICodeGen visit) {
		visit.visitAliasStatement(this);
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return this.parent;
	}

	@Override // OS_Element
	public Context getContext() {
		return getParent().getContext();
	}

	@Override // OS_Element2
	public String name() {
		return this.nameToken.getText();
	}

	@Override
	public boolean hasResolvedElement() {
		return _resolvedElement != null;
	}

	@Override
	public OS_Element getResolvedElement() {
		return _resolvedElement;
	}

	@Override
	public void setResolvedElement(final OS_Element element) {
		_resolvedElement = element;
	}

	// region ClassItem

	private AccessNotation access_note;
	private El_Category category;

	@Override
	public void setCategory(final El_Category aCategory) {
		category = aCategory;
	}

	@Override
	public void setAccess(final AccessNotation aNotation) {
		access_note = aNotation;
	}

	@Override
	public El_Category getCategory() {
		return category;
	}

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	// endregion

}

//
//
//
