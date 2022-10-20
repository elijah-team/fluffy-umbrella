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

/**
 * Created 12/29/20 3:57 AM
 */
public class PropertyStatementBuilder extends ElBuilder {
	private IdentExpression prop_name;
	private TypeName tn;
	private Context _context;
	private BaseScope _get_scope = null;
	private BaseScope _set_scope = null;
	private boolean _get_is_abstract = false;
	private boolean _set_is_abstract = false;

	@Override
	protected OS_Element build() {
		PropertyStatement ps = new PropertyStatement(_parent, _context);
		ps.setName(this.prop_name);
		ps.setTypeName(this.tn);
		if (_get_scope != null) {
			Scope3 scope3 = new Scope3(ps.get_fn);
			for (ElBuilder gsi : _get_scope.items()) {
				gsi.setParent(ps);
				gsi.setContext(ps.getContext());
				OS_Element built = gsi.build();
				scope3.add(built);
			}
			ps.get_fn.scope(scope3);
		} else if (_get_is_abstract) {
			ps.get_fn.setAbstract(true);
		} else
			ps.get_fn = null;
		if (_set_scope != null) {
			Scope3 scope3 = new Scope3(ps.set_fn);
			for (ElBuilder gsi : _set_scope.items()) {
				gsi.setParent(ps);
				gsi.setContext(ps.getContext());
				OS_Element built = gsi.build();
				scope3.add(built);
			}
			ps.set_fn.scope(scope3);
		} else if (_set_is_abstract) {
			ps.set_fn.setAbstract(true);
		} else
			ps.set_fn = null;
		return ps;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public void setName(IdentExpression prop_name) {
		this.prop_name = prop_name;
	}

	public void setTypeName(TypeName tn) {
		this.tn = tn;
	}

	public BaseScope get_scope() {
		_get_scope = new BaseScope();
		return _get_scope;
	}

	public BaseScope set_scope() {
		_set_scope = new BaseScope();
		return _set_scope;
	}

	public void addGet() {
		_get_is_abstract = true;
	}

	public void addSet() {
		_set_is_abstract = true;
	}

//	public void setParentContext(final Context ctx) {
//		_context = ctx;
//	}
}

//
//
//
