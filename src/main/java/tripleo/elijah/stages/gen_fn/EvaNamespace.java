/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.ExpressionBuilder;
import tripleo.elijah.lang.ExpressionKind;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.Scope3;
import tripleo.elijah.lang.StatementWrapper;
import tripleo.elijah.nextgen.reactive.DefaultReactive;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.UnintendedUseException;
import tripleo.elijah.world.impl.DefaultLivingNamespace;

import java.util.function.Consumer;

/**
 * Created 12/22/20 5:39 PM
 */
public class EvaNamespace extends EvaContainerNC implements GNCoded {
	private final OS_Module          module;
	private final NamespaceStatement namespaceStatement;
	private DefaultLivingNamespace _living;

	public EvaNamespace(final NamespaceStatement namespace1, final OS_Module module) {
		this.namespaceStatement = namespace1;
		this.module             = module;
	}

	public void addAccessNotation(final AccessNotation an) {
		throw new NotImplementedException();
	}

	public void createCtor0() {
		// TODO implement me
		final FunctionDef fd = new FunctionDef(namespaceStatement, namespaceStatement.getContext());
		fd.setName(Helpers.string_to_ident("<ctor$0>"));
		final Scope3 scope3 = new Scope3(fd);
		fd.scope(scope3);
		for (final VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
				final IExpression left  = varTableEntry.nameToken;
				final IExpression right = varTableEntry.initialValue;

				final @NotNull IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
				scope3.add(new StatementWrapper(e, fd.getContext(), fd));
			} else {
				if (getPragma("auto_construct")) {
					scope3.add(new ConstructStatement(fd, fd.getContext(), varTableEntry.nameToken, null, null));
				}
			}
		}
	}

	private boolean getPragma(final String auto_construct) { // TODO this should be part of Context
		return false;
	}

	public String getName() {
		return namespaceStatement.getName();
	}

	@Override
	public OS_Element getElement() {
		return getNamespaceStatement();
	}

	public NamespaceStatement getNamespaceStatement() {
		return this.namespaceStatement;
	}

	@Override
	public void generateCode(final CodeGenerator aCodeGenerator, final GenerateResult aGr) {
		aCodeGenerator.generate_namespace(this, aGr);
	}

	@Override
	public @NotNull String identityString() {
		return String.valueOf(namespaceStatement);
	}

	@Override
	public OS_Module module() {
		return module;
	}

	@Override
	public void generateCode(final GenerateResultEnv aFileGen, final CodeGenerator aGgc) {
		throw new NotImplementedException();
	}

	@Override
	public Role getRole() {
		return Role.NAMESPACE;
	}

	@Override
	public void register(final ICodeRegistrar aCr) {
		throw new NotImplementedException();

	}

	public void setLiving(final DefaultLivingNamespace aLiving) {
		_living = aLiving;
	}

	private final _Reactive_EvaNamespace reactiveEvaNamespace = new _Reactive_EvaNamespace();

	public Reactive reactive() {
		return reactiveEvaNamespace;
	}

	static class _Reactive_EvaNamespace extends DefaultReactive {
		@Override
		public <T> void addListener(final Consumer<T> t) {
			throw new UnintendedUseException();
		}

		@Override
		public <T> void addResolveListener(final Consumer<T> aO) {
			throw new NotImplementedException();
		}
	}

}

//
//
//
