/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/*
 * Created on Aug 30, 2005 8:43:27 PM
 * 
 * $Id$
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.ArrayList;
import java.util.List;

public class DefFunctionDef extends BaseFunctionDef {

	private final OS_Element parent;

	public DefFunctionDef(final OS_Element aElement, final Context aContext) {
		parent = aElement;
		if (aElement instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else if (aElement instanceof PropertyStatement) {
			// do nothing
		} else {
			throw new IllegalStateException("adding DefFunctionDef to " + aElement.getClass().getName());
		}
		_a.setContext(new FunctionContext(aContext, this));
		setSpecies(Species.DEF_FUN);
	}

	private TypeName _returnType = null;

	public void setReturnType(final TypeName tn) {
		this._returnType = tn;
	}

	/**
	 * Can be {@code null} under the following circumstances:<br/><br/>
	 *
	 * 1. The compiler(parser) didn't get a chance to set it yet<br/>
	 * 2. The programmer did not specify a return value and the compiler must deduce it<br/>
	 * 3. The function is a void-type and specification isn't required <br/>
	 *
	 * @return the associated TypeName or NULL
	 */
	public @Nullable TypeName returnType() {
		return _returnType;
	}

	private IExpression _expr;
//	private FormalArgList fal;

	// wont use parent scope.items.add so this is ok
	public void setExpr(final IExpression aExpr) {
		_expr = aExpr;
		_items.add(new StatementWrapper(_expr, getContext(), this));
	}

	final List<FunctionItem> _items = new ArrayList<FunctionItem>();


	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitDefFunction(this);
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public List<FunctionItem> getItems() {
		return _items; // TODO what about scope?
	}

	/**
	 * see {@link #_expr} for why getItems().size should be 0, or
	*/
	@Override
	public void postConstruct() {
//		super.postConstruct();
		if (getItems().size() != 1)
			throw new IllegalStateException("Too many items"); // TODO convert to diagnostic?
	}
}

//
//
//
