/**
 *
 */
package tripleo.elijah.stages.stage1;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FunctionItem;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenerateFunctions.S1toG_GC_Processor;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.util.range.Range;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * @author Created    Oct 7, 2022 at 7:00:43 PM
 */
public class S1_Constructor {

	private GeneratedConstructor gf;
	private ConstructorDef       source;
	private FunctionInvocation   invocation;

	public S1_Constructor(final ConstructorDef aConstructorDef, final ClassStatement parent,
	                      final FunctionInvocation aFunctionInvocation) {
		setSource(aConstructorDef);
		setInvocation(aFunctionInvocation);
		setParent(parent); // TODO smelly
		parseArgs();
	}

	public void setSource(final ConstructorDef aConstructorDef) {
		source = aConstructorDef;
		gf     = new GeneratedConstructor(source);
	}

	public void setInvocation(final FunctionInvocation aFunctionInvocation) {
		invocation = aFunctionInvocation;

		if (gf != null) {
			gf.setFunctionInvocation(invocation);
		}
	}

	public void setParent(final ClassStatement parent) {
		if (parent instanceof ClassStatement) {
			final IdentExpression selfIdent = IdentExpression.forString("self");

			final OS_Type        parentType = parent.getOS_Type();
			final TypeTableEntry tte        = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, parentType, selfIdent);

			gf.addVariableTableEntry("self", VariableTableType.SELF, tte, null);
		}
	}

	public void parseArgs() {
		final List<FormalArgListItem> fali_args = source.fal().falis;
		final List<TypeTableEntry>    fi_args   = invocation.getArgs();

		for (int i = 0; i < fali_args.size(); i++) {
			final FormalArgListItem fali = fali_args.get(i);

			final TypeTableEntry tte1     = fi_args.get(i);
			final OS_Type        attached = tte1.getAttached();

			// TODO for reference now...
			final GenType  genType  = new GenType();
			final TypeName typeName = fali.typeName();
			if (typeName != null) {
				genType.typeName = new OS_UserType(typeName);
			}
			genType.resolved = attached;

			final OS_Type attached1;
			if (attached == null && typeName != null) {
				attached1 = genType.typeName;
			} else {
				attached1 = attached;
			}

			final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, attached1, fali.getNameToken());

//			assert attached != null; // TODO this fails

			gf.addVariableTableEntry(fali.name(), VariableTableType.ARG, tte, fali);
		}
	}

	public GeneratedConstructor getGenerated() {
		return gf;
	}

	public void process(final S1toG_GC_Processor p) {
		final Context cctx = source.getContext();
		final int     e1   = p.add_i(gf, InstructionName.E, null, cctx);

		for (final FunctionItem item : source.getItems()) {
//			LOG.err("7056 aConstructorDef.getItem = "+item);
			p.generate_item(item, gf, cctx);
		}

		final int x1 = p.add_i(gf, InstructionName.X, List_of(new IntegerIA(e1, gf)), cctx);
		gf.addContext(source.getContext(), new Range(e1, x1)); // TODO remove interior contexts

//		LOG.info(String.format("602.1 %s", aConstructorDef.name()));
//		for (Instruction instruction : gf.instructionsList) {
//			LOG.info(instruction);
//		}
//		GeneratedFunction.printTables(gf);
	}

	public void process(final S1toG_GC_Processor aProcessor, final boolean aB) {
		process(aProcessor);
		gf.fi = invocation;
	}
}
