package tripleo.elijah.lang.imports;

import tripleo.elijah.contexts.ImportContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

/**
 * Created 8/7/20 2:10 AM
 */
public class NormalImportStatement extends _BaseImportStatement {
	final OS_Element parent;
	private final QualidentList importList = new QualidentList();
	private Context _ctx;

	public NormalImportStatement(final OS_Element aParent) {
		parent = aParent;
		if (parent instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else
			throw new NotImplementedException();
	}

	public void addNormalPart(final Qualident aQualident) {
		importList.add(aQualident);
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
		return importList.parts;
	}

	@Override
	public void setContext(final ImportContext ctx) {
		_ctx = ctx;
	}

}

//
//
//
