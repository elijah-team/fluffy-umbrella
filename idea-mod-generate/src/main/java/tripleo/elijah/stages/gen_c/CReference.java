/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.RegularTypeName;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 1/9/21 7:12 AM
 */
public class CReference {
	private String          rtext = null;
	private List<String>    args;
	private List<Reference> refs;

	private final GI_Repo _repo = new GI_Repo();


	//
	//
	GeneratedClass _cheat = null;
	//
	//

	public String getIdentIAPath(final @NotNull IdentIA ia2, final Generate_Code_For_Method.AOG aog, final String aValue) {
		final BaseGeneratedFunction     generatedFunction = ia2.gf;
		final List<InstructionArgument> s                 = _getIdentIAPathList(ia2);
		refs = new ArrayList<Reference>(s.size());

		//
		// TODO NOT LOOKING UP THINGS, IE PROPERTIES, MEMBERS
		//
		String             text = "";
		final List<String> sl   = new ArrayList<String>();
		for (int i = 0, sSize = s.size(); i < sSize; i++) {
			final InstructionArgument ia = s.get(i);
			if (ia instanceof IntegerIA) {
				// should only be the first element if at all
				assert i == 0;
				final VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(ia));

				if (vte.getName().equals("a1")) {
					final GenType        gt1 = vte.genType;
					final GenType        gt2 = vte.type.genType;
					final GeneratedClass gc1 = (GeneratedClass) vte.genType.node;

					_cheat = gc1;

					// only gt1.node is not null

					assert gc1.getCode() == 106;
					assert gc1.getName().equals("ConstString");

					// gt2

					assert gt2.resolvedn == null;
					assert gt2.typeName instanceof OS_UserType;
					assert gt2.nonGenericTypeName instanceof RegularTypeName;
					assert gt2.resolved instanceof OS_FuncType; // wrong: should be usertype: GeneratedClass
					assert ((ClassInvocation) gt2.ci).resolvePromise().isResolved();

					((ClassInvocation) gt2.ci).resolvePromise().then(gc -> { // wrong: should be ConstString
						assert gc.getCode() == 102;
						assert gc.getKlass().getName().equals("Arguments");
					});

					assert gt2.functionInvocation == null;

					final int y = 2;
				}

				text = "vv" + vte.getName();
				addRef(vte.getName(), Ref.LOCAL);
			} else if (ia instanceof IdentIA) {
				final IdentTableEntry idte = ((IdentIA) ia).getEntry();
				text = CRI_Ident.of(idte, ((IdentIA) ia).gf).getIdentIAPath(i, sSize, aog, sl, aValue, refs::add, s, ia2, this);
			} else if (ia instanceof ProcIA) {
				final ProcTableEntry prte = generatedFunction.getProcTableEntry(to_int(ia));
				text = getIdentIAPath_Proc(prte);
			} else {
				throw new NotImplementedException();
			}
			if (text != null)
				sl.add(text);
		}
		rtext = Helpers.String_join(".", sl);
		return rtext;
	}

	public String getIdentIAPath_Proc(final @NotNull ProcTableEntry aPrte) {
		final String[]              text      = new String[1];
		final BaseGeneratedFunction generated = aPrte.getFunctionInvocation().getGenerated();
		final IDeduceElement3       de_pte    = aPrte.getDeduceElement3();

		if (generated == null)
			throw new IllegalStateException();

		if (generated instanceof GeneratedConstructor) {
			NotImplementedException.raise();
			generated.onGenClass(genClass -> {
				final IdentExpression constructorName = generated.getFD().getNameNode();
				final String          constructorNameText;
				if (constructorName == ConstructorDef.emptyConstructorName) {
					constructorNameText = "";
				} else {
					constructorNameText = constructorName.getText();
				}
				text[0] = String.format("ZC%d%s", genClass.getCode(), constructorNameText);
				addRef(text[0], Ref.CONSTRUCTOR);
			});
			final GeneratedContainerNC genClass = (GeneratedContainerNC) generated.getGenClass();
			if (genClass == null) {
				final int y = 2;
				//generated.setClass(genClass);
			}
		} else {
			generated.onGenClass(genClass -> {
				text[0] = String.format("Z%d%s", genClass.getCode(), generated.getFD().getNameNode().getText());
				addRef(text[0], Ref.FUNCTION);
			});
		}

		return text[0];
	}

	enum Ref {
		//  was:
		//	enum Ref {
		//		LOCAL, MEMBER, PROPERTY_GET, PROPERTY_SET, INLINE_MEMBER, CONSTRUCTOR, DIRECT_MEMBER, LITERAL, PROPERTY (removed), FUNCTION
		//	}


		// https://www.baeldung.com/a-guide-to-java-enums
		LOCAL {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = "vv" + ref.text;
				sb.appendText(text, false);
			}
		},
		MEMBER {
			@Override
			public void buildHelper(final Reference ref, final @NotNull BuildState sb) {
				final String text = "->vm" + ref.text;

				final StringBuilder sb1 = new StringBuilder();

				sb1.append(text);
				if (ref.value != null) {
					sb1.append(" = ");
					sb1.append(ref.value);
					sb1.append(";");
				}

				sb.appendText(sb1.toString(), false);
			}
		},
		PROPERTY_GET {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s%s)", ref.text, s);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		},
		PROPERTY_SET {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s%s, %s);", ref.text, s, ref.value);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		},
		INLINE_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = Emit.emit("/*219*/") + ".vm" + ref.text;
				sb.appendText(text, false);
			}
		},
		CONSTRUCTOR {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String          text;
				final @NotNull String s = sb.toString();
				text    = String.format("%s(%s", ref.text, s);
				sb.open = false;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text + ")", true);
			}
		},
		DIRECT_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final @NotNull BuildState sb) {
				final String text;
				text = Emit.emit("/*124*/") + "vsc->vm" + ref.text;

				final StringBuilder sb1 = new StringBuilder();

				sb1.append(text);
				if (ref.value != null) {
					sb1.append(" = ");
					sb1.append(ref.value);
					sb1.append(";");
				}

				sb.appendText(sb1.toString(), false);
			}
		},
		LITERAL {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = ref.text;
				sb.appendText(text, false);
			}
		},
		FUNCTION {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s(%s", ref.text, s);
				sb.open = true;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text, true);
			}
		};

		public abstract void buildHelper(final Reference ref, final BuildState sb);
	}

	interface GenerateC_Statement {
		String getText();

		GCR_Rule rule();
	}

	interface GCR_Rule {
		String text();
	}

	interface GenerateC_Item {

	}

	class GI_ProcIA implements GenerateC_Item {
		private final ProcIA carrier;

		public GI_ProcIA(final ProcIA aProcIA) {
			carrier = aProcIA;
		}
	}

	/**
	 * Call before you call build
	 *
	 * @param sl3
	 */
	public void args(final List<String> sl3) {
		args = sl3;
	}

	@NotNull
	public String build() {
		final BuildState st = new BuildState();

		for (final Reference ref : refs) {
			switch (ref.type) {
				case LITERAL:
				case DIRECT_MEMBER:
				case INLINE_MEMBER:
				case MEMBER:
				case LOCAL:
				case FUNCTION:
				case PROPERTY_GET:
				case PROPERTY_SET:
				case CONSTRUCTOR:
					ref.buildHelper(st);
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + ref.type);
			}
//			sl.add(text);
		}
//		return Helpers.String_join("->", sl);

		final StringBuilder sb = st.sb;

		if (st.needs_comma && args != null && args.size() > 0)
			sb.append(", ");

		if (st.open) {
			if (args != null) {
				sb.append(Helpers.String_join(", ", args));
			}
			sb.append(")");
		}

		return sb.toString();
	}

	@NotNull
	static List<InstructionArgument> _getIdentIAPathList(@NotNull InstructionArgument oo) {
		final List<InstructionArgument> s = new LinkedList<InstructionArgument>();
		while (oo != null) {
			if (oo instanceof IntegerIA) {
				s.add(0, oo);
				oo = null;
			} else if (oo instanceof IdentIA) {
				final IdentTableEntry ite1 = ((IdentIA) oo).getEntry();
				s.add(0, oo);
				oo = ite1.getBacklink();
			} else if (oo instanceof ProcIA) {
//				final ProcTableEntry prte = ((ProcIA)oo).getEntry();
				s.add(0, oo);
				oo = null;
			} else
				throw new IllegalStateException("Invalid InstructionArgument");
		}
		return s;
	}

	void addRef(final String text, final Ref type) {
		refs.add(new Reference(text, type));
	}

	void addRef(final String text, final Ref type, final String aValue) {
		refs.add(new Reference(text, type, aValue));
	}

	private class GI_Module {
		private final OS_Module carrier;

		GI_Module(final OS_Module aCarrier) {
			carrier = aCarrier;
		}
	}

	private class GI_Repo {
		private final Map<Object, GenerateC_Item> items = new HashMap<>();

		public GenerateC_Item itemFor(final ProcIA aProcIA) {
			final GI_ProcIA gi_proc;
			if (items.containsKey(aProcIA)) {
				gi_proc = (GI_ProcIA) items.get(aProcIA);
			} else {
				gi_proc = new GI_ProcIA(aProcIA);
				items.put(aProcIA, gi_proc);
			}
			return gi_proc;
		}
	}

	static class Reference {
		final String text;
		final Ref    type;
		final String value;

		public Reference(final String aText, final Ref aType, final String aValue) {
			text  = aText;
			type  = aType;
			value = aValue;
		}

		public Reference(final String aText, final Ref aType) {
			text  = aText;
			type  = aType;
			value = null;
		}

		public void buildHelper(final BuildState st) {
			type.buildHelper(this, st);
		}
	}

	private final static class BuildState {
		StringBuilder sb   = new StringBuilder();
		boolean       open = false, needs_comma = false;

		public void appendText(final String text, final boolean erase) {
			if (erase)
				sb = new StringBuilder();

			sb.append(text);
		}

		@Override
		public String toString() {
			return sb.toString();
		}
		//ABOVE 3a
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
