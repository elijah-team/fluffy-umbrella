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
 * Created 8/23/21 2:36 AM
 */
public class FunctionBody {
	public  Scope3  scope3;
	private boolean isAbstract;

	private List<Postcondition> postconditions;
	private List<Precondition>  preconditions;

	public boolean getAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean aAbstract) {
		isAbstract = aAbstract;
	}

	public void addPreCondition(Precondition aPrecondition) {
		if (preconditions == null)
			preconditions = new ArrayList<Precondition>();
		preconditions.add(aPrecondition);
	}

	public void addPostCondition(Postcondition aPostcondition) {
		if (postconditions == null)
			postconditions = new ArrayList<Postcondition>();
		postconditions.add(aPostcondition);
	}
}

//
//
//
