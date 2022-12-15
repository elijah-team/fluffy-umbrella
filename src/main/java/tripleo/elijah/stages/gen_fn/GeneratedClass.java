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
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 10/29/20 4:26 AM
 */
public class GeneratedClass extends GeneratedContainerNC {
	private final OS_Module module;
	private final ClassStatement klass;
	public Map<ConstructorDef, GeneratedConstructor> constructors = new HashMap<ConstructorDef, GeneratedConstructor>();
	public ClassInvocation ci;
	private boolean resolve_var_table_entries_already = false;

	public GeneratedClass(ClassStatement klass, OS_Module module) {
		this.klass = klass;
		this.module = module;
	}

	public boolean isGeneric() {
		return klass.getGenericPart().size() > 0;
	}

	public void addAccessNotation(AccessNotation an) {
		throw new NotImplementedException();
	}

	public void createCtor0() {
		// TODO implement me
		FunctionDef fd = new FunctionDef(klass, klass.getContext());
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

	@NotNull
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append(klass.getName());
		if (ci.genericPart != null) {
			sb.append("[");
			final String joined = getNameHelper(ci.genericPart);
			sb.append(joined);
			sb.append("]");
		}
		return sb.toString();
	}

	@NotNull
	private String getNameHelper(Map<TypeName, OS_Type> aGenericPart) {
		List<String> ls = new ArrayList<String>();
		for (Map.Entry<TypeName, OS_Type> entry : aGenericPart.entrySet()) { // TODO Is this guaranteed to be in order?
			final OS_Type value = entry.getValue(); // This can be another ClassInvocation using GenType
			final String name = value.getClassOf().getName();
			ls.add(name); // TODO Could be nested generics
		}
		return Helpers.String_join(", ", ls);
	}

	public void addConstructor(ConstructorDef aConstructorDef, @NotNull GeneratedConstructor aGeneratedFunction) {
		constructors.put(aConstructorDef, aGeneratedFunction);
	}

	public ClassStatement getKlass() {
		return this.klass;
	}

    @Override
    public String identityString() {
        return ""+klass;
    }

    @Override
    public OS_Module module() {
        return module;
    }

	public boolean resolve_var_table_entries(DeducePhase aDeducePhase) {
		boolean Result = false;

		if (resolve_var_table_entries_already) return true;

		for (VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.potentialTypes.size() == 0 && varTableEntry.varType == null) {
				final TypeName tn = varTableEntry.typeName;
				if (tn != null) {
					if (tn instanceof NormalTypeName) {
						final NormalTypeName tn2 = (NormalTypeName) tn;
						if (!tn.isNull()) {
							LookupResultList lrl = tn.getContext().lookup(tn2.getName());
							OS_Element best = lrl.chooseBest(null);
							if (best != null) {
								if (best instanceof AliasStatement)
									best = DeduceLookupUtils._resolveAlias((AliasStatement) best, null);
								assert best instanceof ClassStatement;
								varTableEntry.varType = new OS_Type((ClassStatement) best);
							} else {
								// TODO shouldn't this already be calculated?
							}
						}
					}
				} else {
					// must be unknown
				}
			} else {
//				System.err.printf("108 %s %s%n", this, varTableEntry.potentialTypes);
				if (varTableEntry.potentialTypes.size() == 1) {
					TypeTableEntry varType1 = varTableEntry.potentialTypes.get(0);
					if (varType1.resolved() == null) {
						assert varType1.getAttached() != null;
						assert varType1.getAttached().getType() == OS_Type.Type.USER_CLASS;
						//
						ClassInvocation xci = new ClassInvocation(varType1.getAttached().getClassOf(), null);
						xci = aDeducePhase.registerClassInvocation(xci);
						@NotNull GenerateFunctions gf = aDeducePhase.generatePhase.getGenerateFunctions(xci.getKlass().getContext().module());
						WlGenerateClass wgc = new WlGenerateClass(gf, xci, aDeducePhase.generatedClasses);
						wgc.run(null); // !
						varType1.resolve(wgc.getResult());
						Result = true;
					}
					if (varType1.resolved() != null)
						varTableEntry.resolve(varType1.resolved());
					else
						System.err.println("114 Can't resolve "+varTableEntry);
				}
			}
		}

		resolve_var_table_entries_already = true; // TODO is this right?
		return Result;
	}

	@Override
	public OS_Element getElement() {
		return getKlass();
	}

	@Override
	public void generateCode(CodeGenerator aCodeGenerator, GenerateResult aGr) {
		aCodeGenerator.generate_class(this, aGr);
	}

	@NotNull
	public String getNumberedName() {
		return getKlass().getName()+"_"+ getCode();
	}
}

//
//
//
