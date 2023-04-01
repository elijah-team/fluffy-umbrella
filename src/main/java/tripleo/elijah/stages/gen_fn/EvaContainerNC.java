/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.FunctionMapDeferred;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.IDependencyReferent;
import tripleo.elijah.stages.post_deduce.IPostDeduce;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 3/16/21 10:45 AM
 */
public abstract class EvaContainerNC extends AbstractDependencyTracker implements EvaContainer, IDependencyReferent {
	public boolean generatedAlready = false;
	private int code = 0;
	private final Dependency dependency = new Dependency(this);

	public Map<FunctionDef, GeneratedFunction> functionMap = new HashMap<FunctionDef, GeneratedFunction>();
	public Map<ClassStatement, EvaClass>       classMap    = new HashMap<ClassStatement, EvaClass>();

	public List<VarTableEntry> varTable = new ArrayList<VarTableEntry>();

	public void addVarTableEntry(AccessNotation an, VariableStatement vs) {
		// TODO dont ignore AccessNotation
		varTable.add(new VarTableEntry(vs, vs.getNameToken(), vs.initialValue(), vs.typeName(), vs.getParent().getParent()));
	}

	@Override
	public @NotNull Maybe<VarTableEntry> getVariable(String aVarName) {
		for (VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.nameToken.getText().equals(aVarName))
				return new Maybe<>(varTableEntry, null);
		}
		return new Maybe<>(null, _def_VarNotFound);
	}


	static Diagnostic _def_VarNotFound = new VarNotFound();

	static class VarNotFound implements Diagnostic {
		@Override
		public String code() {
			return null;
		}

		@Override
		public Severity severity() {
			return null;
		}

		@Override
		public @NotNull Locatable primary() {
			return null;
		}

		@Override
		public @NotNull List<Locatable> secondary() {
			return null;
		}

		@Override
		public void report(final PrintStream stream) {

		}
	}

	public void addClass(ClassStatement aClassStatement, EvaClass aEvaClass) {
		classMap.put(aClassStatement, aEvaClass);
	}

	public void addFunction(FunctionDef functionDef, GeneratedFunction generatedFunction) {
		if (functionMap.containsKey(functionDef))
			throw new IllegalStateException("Function already generated"); // TODO there can be overloads, although we don't handle that yet
		functionMap.put(functionDef, generatedFunction);
		{
			final Collection<FunctionMapDeferred> deferreds = functionMapDeferreds.get(functionDef);
			for (FunctionMapDeferred deferred : deferreds) {
				deferred.onNotify(generatedFunction);
			}
		}
	}

	/**
	 * Get a {@link GeneratedFunction}
	 *
	 * @param fd the function searching for
	 *
	 * @return null if no such key exists
	 */
	public GeneratedFunction getFunction(FunctionDef fd) {
		return functionMap.get(fd);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int aCode) {
		code = aCode;
	}

	public abstract void generateCode(CodeGenerator aGgc, GenerateResult aGr);

	public abstract void analyzeNode(IPostDeduce aPostDeduce);

	Multimap<FunctionDef, FunctionMapDeferred> functionMapDeferreds = ArrayListMultimap.create();
	public void functionMapDeferred(final FunctionDef aFunctionDef, final FunctionMapDeferred aFunctionMapDeferred) {
		functionMapDeferreds.put(aFunctionDef, aFunctionMapDeferred);
	}

	public Dependency getDependency() {
		return dependency;
	}
}

//
//
//
