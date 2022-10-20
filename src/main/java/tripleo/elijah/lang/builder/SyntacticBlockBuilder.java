/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.SyntacticBlock;

/**
 * Created 12/23/20 4:31 AM
 */
public class SyntacticBlockBuilder extends ElBuilder {
	private Context _context;
	private SyntacticBlockScope _scope = new SyntacticBlockScope();

	@Override
	protected SyntacticBlock build() {
		SyntacticBlock syntacticBlock = new SyntacticBlock(_parent);
		for (ElBuilder builder : _scope.items()) {
//			if (builder instanceof AccessNotation) {
//				cs.addAccess((AccessNotation) builder);
//			} else {
//				cs.add(builder);
//			}
			OS_Element built;
			builder.setParent(_parent);
			builder.setContext(_context);
			built = builder.build();
			syntacticBlock.add(built);
		}
		syntacticBlock.postConstruct();
		return syntacticBlock;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public BaseScope scope() {
		return _scope;
	}
}

//
//
//
