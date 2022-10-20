/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/22/20 11:48 PM
 */
public class VariableSequenceBuilder extends ElBuilder {
	private IExpression _initial;
	private IdentExpression _name;
	private TypeName _tn;
	private TypeModifiers def = null;

	private Context _context;
	List<Triple> triples = new ArrayList<Triple>();

	public void defaultModifiers(TypeModifiers modifiers) {
		def = modifiers;
	}

	public void next() {
		if (_initial == null) _initial = IExpression.UNASSIGNED;
		triples.add(new Triple(_initial, _name, _tn));
		_initial = null;
		_name = null;
//		_tn = null;
	}

	static class Triple {
		IExpression _initial;
		IdentExpression _name;
		TypeName _tn;

		public Triple(IExpression _initial, IdentExpression _name, TypeName _tn) {
			this._initial = _initial;
			this._name = _name;
			this._tn = _tn;
		}
	}

	public void setName(IdentExpression i) {
		_name = i;
	}

	public void setTypeName(TypeName tn) {
		_tn = tn;
	}

	public void setInitial(IExpression expr) {
		_initial = expr;
	}

	@Override
	protected VariableSequence build() {
		VariableSequence variableSequence = new VariableSequence(_context);
		variableSequence.defaultModifiers(def);
		if (_name != null)
			next(); // create singular entry
		for (Triple triple : triples) {
			VariableStatement vs = variableSequence.next();
			if (triple._tn != null)
				vs.setTypeName(triple._tn);
			vs.initial(triple._initial);
			vs.setName(triple._name);
		}
		if (_tn != null) {
			_tn.setContext(_context);
			variableSequence.setTypeName(_tn);
		}
		return variableSequence;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}
}

//
//
//
