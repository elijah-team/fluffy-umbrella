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
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.FunctionMapDeferred;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.IDependencyReferent;

import java.util.*;

/**
 * Created 3/16/21 10:45 AM
 */
public abstract class GeneratedContainerNC extends AbstractDependencyTracker implements GeneratedContainer, IDependencyReferent {
	public final  Map<FunctionDef, GeneratedFunction>        functionMap          = new HashMap<FunctionDef, GeneratedFunction>();
	public final  Map<ClassStatement, GeneratedClass>        classMap             = new HashMap<ClassStatement, GeneratedClass>();
	public final  List<VarTableEntry>                        varTable             = new ArrayList<VarTableEntry>();
	private final Dependency                                 dependency           = new Dependency(this);
	private final Multimap<FunctionDef, FunctionMapDeferred> functionMapDeferreds = ArrayListMultimap.create();
	public        boolean                                    generatedAlready     = false;
	private       int                                        code                 = 0;

	public void addVarTableEntry(final AccessNotation an, final VariableStatement vs) {
		// TODO dont ignore AccessNotation
		varTable.add(new VarTableEntry(vs, vs.getNameToken(), vs.initialValue(), vs.typeName(), vs.getParent().getParent()));
	}

	@Override
	public OS_Element getElement() {
		return null;
	}

	@Override
	@Nullable
	public VarTableEntry getVariable(final String aVarName) {
		for (final VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.nameToken.getText().equals(aVarName))
				return varTableEntry;
		}
		return null;
	}

	public void addClass(final ClassStatement aClassStatement, final GeneratedClass aGeneratedClass) {
		classMap.put(aClassStatement, aGeneratedClass);
	}

	public void addFunction(final FunctionDef functionDef, final GeneratedFunction generatedFunction) {
		if (functionMap.containsKey(functionDef))
			throw new IllegalStateException("Function already generated"); // TODO there can be overloads, although we don't handle that yet
		functionMap.put(functionDef, generatedFunction);
		{
			final Collection<FunctionMapDeferred> deferreds = functionMapDeferreds.get(functionDef);
			for (final FunctionMapDeferred deferred : deferreds) {
				deferred.onNotify(generatedFunction);
			}
		}
	}

	/**
	 * Get a {@link GeneratedFunction}
	 *
	 * @param fd the function searching for
	 * @return null if no such key exists
	 */
	public GeneratedFunction getFunction(final FunctionDef fd) {
		return functionMap.get(fd);
	}

	public int getCode() {
		return code;
	}

	public void setCode(final int aCode) {
		code = aCode;
	}

	public abstract void generateCode(CodeGenerator aGgc, GenerateResult aGr);

	public void functionMapDeferred(final FunctionDef aFunctionDef, final FunctionMapDeferred aFunctionMapDeferred) {
		functionMapDeferreds.put(aFunctionDef, aFunctionMapDeferred);
	}

	public Dependency getDependency() {
		return dependency;
	}

	@Override
	public String identityString() {
		return null;
	}

	@Override
	public OS_Module module() {
		return null;
	}
}

//
//
//
