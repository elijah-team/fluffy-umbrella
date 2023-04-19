/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.diagnostic.Locatable;

/**
 * Created 8/16/20 2:16 AM
 */
public interface TypeName extends Locatable {
	boolean isNull();

	Context getContext();

	void setContext(Context context);

	Type kindOfType();

	@Override
	boolean equals(Object o);

	enum Type {
		NORMAL, GENERIC, TYPE_OF, FUNCTION
	}

	enum Nullability {
		NOT_SPECIFIED, NEVER_NULL, NULLABLE
	}
}

//
//
//
