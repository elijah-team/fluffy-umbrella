/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

/**
 * Created 8/15/20 6:45 PM
 */
public class AnnotationPart {
	private Qualident      _class;
	private ExpressionList _exprs;

	public void setClass(final Qualident q) {
		_class = q;
	}

	public Qualident annoClass() {
		return _class;
	}

	public ExpressionList getExprs() {
		return _exprs;
	}

	public void setExprs(final ExpressionList el) {
		_exprs = el;
	}
}

//
//
//
