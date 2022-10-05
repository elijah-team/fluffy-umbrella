package tripleo.elijah.lang.imports;

import tripleo.elijah.contexts.ImportContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/7/20 2:09 AM
 */
public class RootedImportStatement extends _BaseImportStatement {
	final OS_Element parent;
	private QualidentList importList = new QualidentList(); // remove final for ImportStatementBuilder
	/** Used in from syntax
	 * @category from
	 */
	private Qualident root;
	private Context _ctx;

	public RootedImportStatement(final OS_Element aParent) {
		parent = aParent;
		if (parent instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else
			throw new NotImplementedException();
	}

	public Qualident getRoot() {
		return root;
	}

	public void setRoot(final Qualident root) {
		this.root = root;
	}

	/** Used in from syntax
	 * @category from
	 */
	public void importRoot(final Qualident xyz) {
		setRoot(xyz);
	}

	public QualidentList importList() {
		return importList;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public Context getContext() {
		return parent.getContext();
	}

	@Override
	public List<Qualident> parts() {
		final List<Qualident> r = new ArrayList<Qualident>();
		for (final Qualident qualident : importList.parts) {
			final Qualident q = new Qualident();
			// TODO what the hell does this do? Should it be `root'
			for (final IdentExpression part : q.parts()) {
				q.append(part);
			}
			for (final IdentExpression part : qualident.parts()) {
				q.append(part);
			}
			r.add(q);
		}
		return r;
	}

	@Override
	public void setContext(final ImportContext ctx) {
		_ctx = ctx;
	}

	public void setImportList(QualidentList qil) {
		importList = qil;
	}
}

//
//
//
