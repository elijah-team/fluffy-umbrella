/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.Documentable;

/**
 * Created 12/22/20 8:38 PM
 */
public class ClassScope extends ClassOrNamespaceScope implements Documentable {

	public void addAccess(AccessNotation acs) {
		// TODO find something to do with this
		// _items.add(acs);
	}

	public FunctionDefBuilder funcDef() {
		FunctionDefBuilder functionDefBuilder = new FunctionDefBuilder();
		add(functionDefBuilder);
		return functionDefBuilder;
	}

	public TypeAliasBuilder typeAlias() {
		TypeAliasBuilder typeAliasBuilder = new TypeAliasBuilder();
		add(typeAliasBuilder);
		return typeAliasBuilder;
	}

}

//
//
//
