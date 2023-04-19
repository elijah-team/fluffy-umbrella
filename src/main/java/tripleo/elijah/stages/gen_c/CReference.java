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
import tripleo.elijah.lang.RegularTypeName;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 1/9/21 7:12 AM
 */
public class CReference {
	private final GI_Repo _repo = new GI_Repo();
	//
	//
	public String __cheat_ret;
	//
	//
	EvaClass _cheat = null;
	private String          rtext = null;
	private List<String>    args;
	private List<Reference> refs;

	public void getIdentIAPath(final IdentIA aIa, final BaseEvaFunction aGf, final Generate_Code_For_Method.AOG aGet, final String aO) {
		getIdentIAPath(aIa, aGet, aO);
	}

	public String getIdentIAPath(final @NotNull IdentIA ia2, final Generate_Code_For_Method.AOG aog, final String aValue) {
		final BaseEvaFunction           generatedFunction = ia2.gf;
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
					final GenType  gt1 = vte.genType;
					final GenType  gt2 = vte.type.genType;
					final EvaClass gc1 = (EvaClass) vte.genType.node;

					_cheat = gc1;

					// only gt1.node is not null

					assert gc1.getCode() == 106;
					assert gc1.getName().equals("ConstString");

					// gt2

					assert gt2.resolvedn == null;
					assert gt2.typeName instanceof OS_UserType;
					assert gt2.nonGenericTypeName instanceof RegularTypeName;
					assert gt2.resolved instanceof OS_FuncType; // wrong: should be usertype: EvaClass
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

				//assert text != null;
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

	public String getIdentIAPath_Proc(final @NotNull ProcTableEntry aPrte) {
		final String[]           text = new String[1];
		final FunctionInvocation fi   = aPrte.getFunctionInvocation();

		if (fi == null) {
			tripleo.elijah.util.Stupidity.println_err_2("7777777777777777 fi getIdentIAPath_Proc " + aPrte.toString());

			return null;//throw new IllegalStateException();
		}


		final BaseEvaFunction generated = fi.getGenerated();
		final IDeduceElement3 de_pte    = aPrte.getDeduceElement3();

		if (generated == null)
			throw new IllegalStateException();

		if (generated instanceof EvaConstructor) {
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
			final EvaContainerNC genClass = (EvaContainerNC) generated.getGenClass();
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

	public void debugPath(final IdentIA identIA, final String aPath) {
		@NotNull final List<InstructionArgument> pl = _getIdentIAPathList(identIA);

		System.out.println("\\ 172-172-172-172-172 ---------------------------------------------");
		for (InstructionArgument instructionArgument : pl) {
			if (instructionArgument instanceof ProcIA) {
				ProcIA procIA = (ProcIA) instructionArgument;
				System.out.println(procIA.getEntry().expression);
			} else if (instructionArgument instanceof IdentIA) {
				IdentIA argument = (IdentIA) instructionArgument;
				System.out.println(argument.getEntry().getIdent().getText());
			} else if (instructionArgument instanceof IntegerIA) {
				IntegerIA integerIA = (IntegerIA) instructionArgument;
				System.out.println(integerIA.getEntry().getName());
			}
		}
		System.out.println("- 172-172-172-172-172 ---------------------------------------------");
		System.out.println(String.format("[%d][%s]", aPath.length(), aPath));
		System.out.println("/ 172-172-172-172-172 ---------------------------------------------");
	}

	public GI_Repo _repo() {
		return _repo;
	}

	public void addRef(final Reference aR) {
		refs.add(aR);
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

	void addRef(final String text, final Ref type, final String aValue) {
		refs.add(new Reference(text, type, aValue));
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
			class Text implements GenerateC_Statement {
				private String            text;
				private Supplier<Boolean> sb;
				private Supplier<String>  ss;

				public Text(String atext, Supplier<Boolean> asb, Supplier<String> ass) {
					text = atext;
					sb   = asb;
					ss   = ass;
				}

				@Override
				public String getText() {
					final StringBuilder sb1 = new StringBuilder();

					sb1.append("->vm" + text);

					if (sb.get()) {
						sb1.append(" = ");
						sb1.append(ss.get());
						sb1.append(";");
					}

					return text;
				}

				@Override
				public GCR_Rule rule() {
					return new GCR_Rule() {
						@Override
						public String text() {
							return "Ref MEMBER Text";
						}
					};
				}
			}

			@Override
			public void buildHelper(final Reference ref, final @NotNull BuildState sb) {
				final Text t = new Text(ref.text, () -> ref.value != null, () -> ref.value);

				sb.appendText(t.getText(), false);
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
