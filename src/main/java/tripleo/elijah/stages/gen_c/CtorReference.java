/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 3/7/21 1:22 AM
 */
public class CtorReference {

	private String ctorName = "";
	private List<String> args;
	List<CReference.Reference> refs = new ArrayList<CReference.Reference>();
	private GeneratedNode _resolved;

	void addRef(final String text, final CReference.Ref type) {
		refs.add(new CReference.Reference(text, type));
	}

	public void getConstructorPath(final InstructionArgument ia2, final BaseGeneratedFunction gf) {
		final List<InstructionArgument> s = CReference._getIdentIAPathList(ia2);

		for (int i = 0, sSize = s.size(); i < sSize; i++) {
			final InstructionArgument ia = s.get(i);
			if (ia instanceof IntegerIA) {
				// should only be the first element if at all
				assert i == 0;
				final VariableTableEntry vte = gf.getVarTableEntry(to_int(ia));
				if (sSize == 1) {
					final GeneratedNode resolved = vte.type.resolved();
					if (resolved != null) {
						_resolved = resolved;
					} else {
						_resolved = vte.resolvedType();
					}
				}
				addRef(vte.getName(), CReference.Ref.LOCAL);
			} else if (ia instanceof IdentIA) {
				final IdentTableEntry idte = gf.getIdentTableEntry(to_int(ia));
				final OS_Element resolved_element = idte.getResolvedElement();
				if (idte.resolvedType() != null) {
					_resolved = idte.resolvedType();
					ctorName = ((ConstructorDef) resolved_element).name();
				} /*else if (resolved_element != null) {
					assert false;
					if (resolved_element instanceof VariableStatement) {
						addRef(((VariableStatement) resolved_element).getName(), CReference.Ref.MEMBER);
					} else if (resolved_element instanceof ConstructorDef) {
						assert i == sSize - 1; // Make sure we are ending with a constructor call
						int code = ((ClassStatement) resolved_element.getParent())._a.getCode();
						if (code == 0) {
							System.err.println("** 31161 ClassStatement with 0 code " + resolved_element.getParent());
						}
						// README Assuming this is for named constructors
						String text = ((ConstructorDef) resolved_element).name();
						String text2 = String.format("ZC%d%s", code, text);

						ctorName = text;

//						addRef(text2, CReference.Ref.CONSTRUCTOR);

//						addRef(((ConstructorDef) resolved_element).name(), CReference.Ref.CONSTRUCTOR);
					}
				}*/
			} else if (ia instanceof ProcIA) {
//				final ProcTableEntry prte = generatedFunction.getProcTableEntry(to_int(ia));
//				text = (prte.expression.getLeft()).toString();
////				assert i == sSize-1;
//				addRef(text, Ref.FUNCTION); // TODO needs to use name of resolved function
				throw new NotImplementedException();
			} else {
				throw new NotImplementedException();
			}
//			sl.add(text);
		}
	}

	public String build(final ClassInvocation aClsinv) {
		StringBuilder sb = new StringBuilder();
		boolean open = false, needs_comma = false;
//		List<String> sl = new ArrayList<String>();
		String text = "";
		for (final CReference.Reference ref : refs) {
			switch (ref.type) {
				case LOCAL:
					text = "vv" + ref.text;
					sb.append(text);
					break;
				case MEMBER:
					text = "->vm" + ref.text;
					sb.append(text);
					break;
				case INLINE_MEMBER:
					text = Emit.emit("/*219*/")+".vm" + ref.text;
					sb.append(text);
					break;
				case DIRECT_MEMBER:
					text = Emit.emit("/*124*/")+"vsc->vm" + ref.text;
					sb.append(text);
					break;
				case FUNCTION: {
					final String s = sb.toString();
					text = String.format("%s(%s", ref.text, s);
					sb = new StringBuilder();
					open = true;
					if (!s.equals("")) needs_comma = true;
					sb.append(text);
					break;
				}
				case CONSTRUCTOR: {
					final String s = sb.toString();
					text = String.format("%s(%s", ref.text, s);
					sb = new StringBuilder();
					open = true;
					if (!s.equals("")) needs_comma = true;
					sb.append(text);
					break;
				}
				case PROPERTY: {
					final String s = sb.toString();
					text = String.format("%s(%s", ref.text, s);
					sb = new StringBuilder();
					open = true;
					if (!s.equals("")) needs_comma = true;
					sb.append(text);
					break;
				}
				default:
					throw new IllegalStateException("Unexpected value: " + ref.type);
			}
//			sl.add(text);
		}
		{
			// Assuming constructor call
			final int code;
			if (_resolved != null) {
				code = ((GeneratedContainerNC) _resolved).getCode();
			} else {
				code = aClsinv.getKlass()._a.getCode(); // TODO this will either always be 0 or irrelevant
			}
			if (code == 0) {
				System.err.println("** 32135 ClassStatement with 0 code " + aClsinv.getKlass());
			}
			final String text2 = String.format("ZC%d%s", code, ctorName); // TODO what about named constructors
			sb.append(" = ");
			sb.append(text2);
			sb.append("(");
			assert open == false;
			open = true;
		}
//		return Helpers.String_join("->", sl);
		if (needs_comma && args != null && args.size() > 0)
			sb.append(", ");
		if (open) {
			if (args != null) {
				sb.append(Helpers.String_join(", ", args));
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * Call before you call build
	 *
	 * @param sl3
	 */
	public void args(final List<String> sl3) {
		args = sl3;
	}
}

//
//
//
