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
import java.util.Collection;
import java.util.List;

// Referenced classes of package pak2:
//			FormalArgListItem

public class FormalArgList {

	public List<FormalArgListItem> falis=new ArrayList<FormalArgListItem>();

	public FormalArgListItem next() {
		final FormalArgListItem fali = new FormalArgListItem();
		falis.add(fali);
		return fali;
	}

	@Override
	public String toString( ) {
		return falis.toString();
	}

	public Collection<FormalArgListItem> items() {
		return falis;
	}
}

//
//
//
