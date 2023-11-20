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
import tripleo.elijah.contexts.WithContext;
import tripleo.elijah.gen.ICodeGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 8/30/20 1:51 PM
 */
public class WithStatement implements OS_Element, OS_Container, FunctionItem, StatementItem {
	private final OS_Element _parent;
	private WithContext ctx;
	private final List<FunctionItem> _items = new ArrayList<FunctionItem>();
//	private final List<String> mDocs = new ArrayList<String>();
	private Scope3 scope3;

	@Override
	public void addDocString(final Token aText) {
		scope3.addDocString(aText);
//		mDocs.add(aText.getText());
	}

	public WithStatement(final OS_Element aParent) {
		_parent = aParent;
	}

	@Override
	public void visitGen(final ICodeGen visit) {
		visit.visitWithStatement(this);
	}

	@Override
	public OS_Element getParent() {
		return _parent;
	}

	@Override
	public Context getContext() {
		return null;
	}

	public List<FunctionItem> getItems() {
		return _items;
	}

	public Collection<VariableStatement> getVarItems() {
		return hidden_seq.items();
	}

	public VariableStatement nextVarStmt() {
		return hidden_seq.next();
	}

	VariableSequence hidden_seq = new VariableSequence();

	public void setContext(final WithContext ctx) {
		this.ctx = ctx;
	}

	public void postConstruct() {
	}

	@Override
	public List<OS_Element2> items() {
		return null;
	}

	@Override
	public void add(final OS_Element anElement) {
		if (!(anElement instanceof FunctionItem))
			return;
		_items.add((FunctionItem) anElement);
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

}

//
//
//
