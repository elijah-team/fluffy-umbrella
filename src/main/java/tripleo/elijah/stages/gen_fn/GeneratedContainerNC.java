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
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 3/16/21 10:45 AM
 */
public abstract class GeneratedContainerNC extends AbstractDependencyTracker implements GeneratedContainer {
	public boolean generatedAlready = false;
	private int code = 0;

	public Map<FunctionDef, GeneratedFunction> functionMap = new HashMap<FunctionDef, GeneratedFunction>();
	public Map<ClassStatement, GeneratedClass> classMap = new HashMap<ClassStatement, GeneratedClass>();

	public List<VarTableEntry> varTable = new ArrayList<VarTableEntry>();

	public void addVarTableEntry(AccessNotation an, VariableStatement vs) {
		// TODO dont ignore AccessNotation
		varTable.add(new VarTableEntry(vs, vs.getNameToken(), vs.initialValue(), vs.typeName(), vs.getParent().getParent()));
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

	public void addClass(ClassStatement aClassStatement, GeneratedClass aGeneratedClass) {
		classMap.put(aClassStatement, aGeneratedClass);
	}

	public void addFunction(FunctionDef functionDef, GeneratedFunction generatedFunction) {
		if (functionMap.containsKey(functionDef))
			throw new IllegalStateException("Function already generated"); // TODO do better than this
		functionMap.put(functionDef, generatedFunction);
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
}

//
//
//
