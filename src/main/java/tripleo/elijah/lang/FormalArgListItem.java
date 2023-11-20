/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

public class FormalArgListItem implements OS_Element, OS_Element2 {

	private IdentExpression name;
	private TypeName        tn = null;

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitFormalArgListItem(this);
	}

	@Override // OS_Element
	public Context getContext() {
//        throw new NotImplementedException();
//        return null;
		return name.getContext();
	}

	@Override // OS_Element
	public OS_Element getParent() {
		throw new NotImplementedException();
//        return null;
	}

	@Override // OS_Element2
	public String name() {
		return name.getText();
	}

	public IdentExpression getNameToken() {
		return name;
	}

	public void setName(final IdentExpression s) {
		name=s;
	}

	public void setTypeName(final TypeName tn1) {
		tn = tn1;
	}

	public TypeName typeName() {
		return tn;
	}
}

//
//
//
