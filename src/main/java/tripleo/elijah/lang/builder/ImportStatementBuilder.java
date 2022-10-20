/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.contexts.ImportContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.imports.AssigningImportStatement;
import tripleo.elijah.lang.imports.NormalImportStatement;
import tripleo.elijah.lang.imports.QualifiedImportStatement;
import tripleo.elijah.lang.imports.RootedImportStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 2:59 AM
 */
public class ImportStatementBuilder extends ElBuilder {
	private Context _context;
	private State state;

	// ROOTED
	private Qualident xy;
	private QualidentList qil;

	// ASSIGNING
	List<AssigningImportStatement.Part> aparts = new ArrayList<AssigningImportStatement.Part>();

	// SELECTIVE/QUALIFIED
	List<QualifiedImportStatement.Part> sparts = new ArrayList<QualifiedImportStatement.Part>();

	// NORMAL
	List<Qualident> nparts = new ArrayList<Qualident>();

	//
	//
	//

	public void addAssigningPart(IdentExpression i1, Qualident q1) {
		aparts.add(new AssigningImportStatement.Part(i1, q1));
		this.state = State.ASSIGNING;
	}

	public void addSelectivePart(Qualident q3, IdentList il) {
		sparts.add(new QualifiedImportStatement.Part(q3, il));
		this.state = State.SELECTIVE;
	}

	public void addNormalPart(Qualident q2) {
		nparts.add(q2);
		this.state = State.NORMAL;
	}

	enum State {
		ASSIGNING, SELECTIVE, NORMAL, ROOTED
	}

	@Override
	protected ImportStatement build() {
		switch (state) {
		case ROOTED:
			RootedImportStatement rootedImportStatement = new RootedImportStatement(_parent);
			rootedImportStatement.setRoot(xy);
			rootedImportStatement.setImportList(qil);
			rootedImportStatement.setContext(new ImportContext(_context, rootedImportStatement)); // TODO is this correct?
			return rootedImportStatement;
		case ASSIGNING:
			AssigningImportStatement assigningImportStatement = new AssigningImportStatement(_parent);
			for (AssigningImportStatement.Part apart : aparts) {
				assigningImportStatement.addPart(apart);
			}
			return assigningImportStatement;
		case SELECTIVE:
			QualifiedImportStatement qualifiedImportStatement = new QualifiedImportStatement(_parent);
			for (QualifiedImportStatement.Part spart : sparts) {
				qualifiedImportStatement.addPart(spart);
			}
			return qualifiedImportStatement;
		case NORMAL:
			NormalImportStatement normalImportStatement = new NormalImportStatement(_parent);
			for (Qualident npart : nparts) {
				normalImportStatement.addNormalPart(npart);
			}
			return normalImportStatement;
		}
		throw new IllegalStateException();
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public void rooted(Qualident xy, QualidentList qil) {
		this.xy = xy;
		this.qil = qil;
		this.state = State.ROOTED;
	}
}

//
//
//
