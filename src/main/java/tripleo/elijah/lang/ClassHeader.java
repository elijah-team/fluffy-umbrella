/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/22/21 16:22
 */
public class ClassHeader {
	IdentExpression        nameToken;
	boolean                extends_;
	List<AnnotationClause> annos = new ArrayList<>();
	ClassTypes             type;
	ClassInheritance       inh   = new ClassInheritance();
	TypeNameList           genericPart;
	private boolean isConst;

	public ClassHeader(boolean aExtends, List<AnnotationClause> as) {
		extends_ = aExtends;
		annos    = as;
	}

	public void setName(final IdentExpression aNameToken) {
		nameToken = aNameToken;
	}

	public void setConst(boolean aIsConst) {
		isConst = aIsConst;
	}

	public void setType(ClassTypes ct) {
		type = ct;
	}

	public ClassInheritance inheritancePart() {
		return inh;
	}

	public void setGenericPart(final TypeNameList aTypeNameList) {
		genericPart = aTypeNameList;
	}
}

//
//
//
