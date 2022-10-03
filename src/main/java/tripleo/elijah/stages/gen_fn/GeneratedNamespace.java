/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

/**
 * Created 12/22/20 5:39 PM
 */
public class GeneratedNamespace extends GeneratedContainerNC {
	public GeneratedNamespace(NamespaceStatement namespace1, OS_Module module) {
		this.namespaceStatement = namespace1;
		this.module = module;
	}

	private final OS_Module module;
	private final NamespaceStatement namespaceStatement;

	public void addAccessNotation(AccessNotation an) {
		throw new NotImplementedException();
	}

	public void createCtor0() {
		// TODO implement me
		FunctionDef fd = new FunctionDef(namespaceStatement, namespaceStatement.getContext());
		fd.setName(Helpers.string_to_ident("<ctor$0>"));
		Scope3 scope3 = new Scope3(fd);
		fd.scope(scope3);
		for (VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
				IExpression left = varTableEntry.nameToken;
				IExpression right = varTableEntry.initialValue;

				IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
				scope3.add(new StatementWrapper(e, fd.getContext(), fd));
			} else {
				if (getPragma("auto_construct")) {
					scope3.add(new ConstructStatement(fd, fd.getContext(), varTableEntry.nameToken, null, null));
				}
			}
		}
	}

	private boolean getPragma(String auto_construct) { // TODO this should be part of Context
		return false;
	}

	public String getName() {
		return namespaceStatement.getName();
	}

	public NamespaceStatement getNamespaceStatement() {
		return this.namespaceStatement;
	}

    @Override
    public String identityString() {
        return ""+namespaceStatement;
    }

    @Override
    public OS_Module module() {
        return module;
    }

	@Override
	public OS_Element getElement() {
		return getNamespaceStatement();
	}

	@Override
	@Nullable
	public VarTableEntry getVariable(String aVarName) {
		for (VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.nameToken.getText().equals(aVarName))
				return varTableEntry;
		}
		return null;
	}

	@Override
	public void generateCode(CodeGenerator aCodeGenerator, GenerateResult aGr) {
		aCodeGenerator.generate_namespace(this, aGr);
	}
}

//
//
//
