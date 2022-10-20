/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import antlr.Token;
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.Documentable;
import tripleo.elijah.lang.InvariantStatement;
import tripleo.elijah.lang.OS_Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 2:47 AM
 */
public class NamespaceScope extends ClassOrNamespaceScope implements Documentable {
	private final List<Token> _docstrings = new ArrayList<Token>();

//	public Iterable<OS_Element> items() {
//		return _items;
//	}
//
//	public void add(OS_Element item) {
//		_items.add(item);
//	}

	private final List<OS_Element> _items = new ArrayList<OS_Element>();

	@Override
	public void addDocString(Token s1) {
		_docstrings.add(s1);
	}

	public void addAccess(AccessNotation acs) {
		// _items.add(acs);
	}

	public TypeAliasBuilder typeAlias() {
		return new TypeAliasBuilder();
	}

	public FunctionDefBuilder funcDef() {
		return new FunctionDefBuilder();
	}

	public InvariantStatement invariantStatement() {
		return new InvariantStatement();
	}

//	@Override
//	public Iterable<ElBuilder> items() {
//		return bs;
//	}
}

//
//
//
