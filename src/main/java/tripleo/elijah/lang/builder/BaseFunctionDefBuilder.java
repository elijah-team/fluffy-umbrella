/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.AnnotationClause;
import tripleo.elijah.lang.FormalArgList;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/22/20 10:41 PM
 */
public abstract class BaseFunctionDefBuilder extends ElBuilder {
	protected List<AnnotationClause> annotations = new ArrayList<AnnotationClause>();
	protected IdentExpression _name;
	protected FormalArgList mFal;
	protected FunctionDef.Species _species = null;

	public void addAnnotation(AnnotationClause a) {
		annotations.add(a);
	}

	public void setName(IdentExpression i1) {
		_name = i1;
	}

	public void fal(FormalArgList fal) {
		mFal = fal;
	}

	public void setSpecies(FunctionDef.Species aSpecies) {
		_species = aSpecies;
	}

}

//
//
//
