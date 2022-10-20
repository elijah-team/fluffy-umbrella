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
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.DefFunctionDef;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.PropertyStatement;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
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
import java.util.LinkedList;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 1/9/21 7:12 AM
 */
public class CReference {
	private String rtext = null;
	private List<String> args;
	private List<Reference> refs;

	static class Reference {
		final String              text;
		final Ref                 type;
		final String              value;

		public Reference(final String aText, final Ref aType, final String aValue) {
			text = aText;
			type = aType;
			value = aValue;
		}

		public Reference(final String aText, final Ref aType) {
			text = aText;
			type = aType;
			value = null;
		}

		public void buildHelper(final BuildState st) {
			type.buildHelper(this, st);
		}
	}

	enum Ref {
		// https://www.baeldung.com/a-guide-to-java-enums
		LOCAL {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text = "vv" + ref.text;
				sb.appendText(text, false);
			}
		},
		MEMBER {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text = "->vm" + ref.text;

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
				String text;
				final String s = sb.toString();
				text = String.format("%s%s)", ref.text, s);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		},
		PROPERTY_SET {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text;
				final String s = sb.toString();
				text = String.format("%s%s, %s);", ref.text, s, ref.value);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		},
		INLINE_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text = Emit.emit("/*219*/") + ".vm" + ref.text;
				sb.appendText(text, false);
			}
		},
		CONSTRUCTOR {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text;
				final String s = sb.toString();
				text = String.format("%s(%s", ref.text, s);
				sb.open = false;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text + ")", true);
			}
		},
		DIRECT_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text;
				text = Emit.emit("/*124*/")+"vsc->vm" + ref.text;

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
				String text = ref.text;
				sb.appendText(text, false);
			}
		},
		FUNCTION {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				String text;
				final String s = sb.toString();
				text = String.format("%s(%s", ref.text, s);
				sb.open = true;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text, true);
			}
		};

		public abstract void buildHelper(final Reference ref, final BuildState sb);
	}

	void addRef(String text, Ref type) {
		refs.add(new Reference(text, type));
	}

	void addRef(String text, Ref type, String aValue) {
		refs.add(new Reference(text, type, aValue));
	}

	public String getIdentIAPath(final @NotNull IdentIA ia2, BaseGeneratedFunction generatedFunction, Generate_Code_For_Method.AOG aog, final String aValue) {
		assert ia2.gf == generatedFunction;
		final List<InstructionArgument> s = _getIdentIAPathList(ia2);
		refs = new ArrayList<Reference>(s.size());

		//
		// TODO NOT LOOKING UP THINGS, IE PROPERTIES, MEMBERS
		//
		String text = "";
		List<String> sl = new ArrayList<String>();
		for (int i = 0, sSize = s.size(); i < sSize; i++) {
			InstructionArgument ia = s.get(i);
			if (ia instanceof IntegerIA) {
				// should only be the first element if at all
				assert i == 0;
				final VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(ia));
				text = "vv" + vte.getName();
				addRef(vte.getName(), Ref.LOCAL);
			} else if (ia instanceof IdentIA) {
				boolean skip = false;
				final IdentTableEntry idte = ((IdentIA)ia).getEntry();
				OS_Element resolved_element = idte.getResolvedElement();
				if (resolved_element != null) {
					GeneratedNode resolved = null;
					if (resolved_element instanceof ClassStatement) {
						if (idte.type != null)
							resolved = idte.type.resolved();
						if (resolved == null)
							resolved = idte.resolvedType();
					} else if (resolved_element instanceof FunctionDef) {
						@Nullable ProcTableEntry pte = idte.getCallablePTE();
						if (pte != null) {
							FunctionInvocation fi = pte.getFunctionInvocation();
							if (fi != null) {
								BaseGeneratedFunction gen = fi.getGenerated();
								if (gen != null)
									resolved = gen;
							}
						}
						if (resolved == null) {
							GeneratedNode resolved1 = idte.resolvedType();
							if (resolved1 instanceof GeneratedFunction)
								resolved = resolved1;
							else if (resolved1 instanceof GeneratedClass)
								resolved = resolved1;
						}
					} else if (resolved_element instanceof PropertyStatement) {
						NotImplementedException.raise();
						GeneratedNode resolved1 = idte.type.resolved();
						int code;
						if (resolved1 != null)
							code = ((GeneratedContainerNC) resolved1).getCode();
						else
							code = -1;
						short state = 0;
						if (i < sSize-1) {
							state = 1;
						} else {
							switch (aog) {
							case GET:
								state =1;
								break;
							case ASSIGN:
								state =2;
								break;
							}
						}
						switch (state) {
						case 1:
							addRef(String.format("ZP%d_get%s(", code, idte.getIdent().getText()), Ref.PROPERTY_GET);
							skip = true;
							text = null;
							break;
						case 2:
							addRef(String.format("ZP%d_set%s(", code, idte.getIdent().getText()), Ref.PROPERTY_SET, aValue);
							skip = true;
							text = null;
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + state);
						}
					}
					if (!skip) {
						short state = 1;
						if (idte.externalRef != null) {
							state = 2;
						}
						switch (state) {
						case 1:
							if (resolved == null) {
								System.err.println("***88*** resolved is null for "+idte);
							}
							if (sSize >= i + 1) {
								_getIdentIAPath_IdentIAHelper(null, sl, i, sSize, resolved_element, generatedFunction, resolved, aValue);
								text = null;
							} else {
								boolean b = _getIdentIAPath_IdentIAHelper(s.get(i + 1), sl, i, sSize, resolved_element, generatedFunction, resolved, aValue);
								if (b) i++;
							}
							break;
						case 2:
							if ((resolved_element instanceof VariableStatement)) {
								final String text2 = ((VariableStatement) resolved_element).getName();

								final GeneratedNode externalRef = idte.externalRef;
								if (externalRef instanceof GeneratedNamespace) {
									final String text3 = String.format("zN%d_instance", ((GeneratedNamespace) externalRef).getCode());
									addRef(text3, Ref.LITERAL, null);
								} else if (externalRef instanceof GeneratedClass) {
									assert false;
									final String text3 = String.format("zN%d_instance", ((GeneratedClass) externalRef).getCode());
									addRef(text3, Ref.LITERAL, null);
								} else
									throw new IllegalStateException();
								addRef(text2, Ref.MEMBER, aValue);
							} else
								throw new NotImplementedException();
							break;
						}
					}
				} else {
					switch (ia2.getEntry().getStatus()) {
					case KNOWN:
						assert false;
						break;
					case UNCHECKED:
						final String path2 = generatedFunction.getIdentIAPathNormal(ia2);
						final String text3 = String.format("<<UNCHECKED ia2: %s>>", path2/*idte.getIdent().getText()*/);
						text = text3;
//						assert false;
						break;
					case UNKNOWN:
						final String path = generatedFunction.getIdentIAPathNormal(ia2);
						final String text1 = idte.getIdent().getText();
//						assert false;
						// TODO make tests pass but I dont like this (should emit a dummy function or placeholder)
						if (sl.size() == 0) {
							text = Emit.emit("/*149*/") + text1; // TODO check if it belongs somewhere else (what does this mean?)
						} else {
							text = Emit.emit("/*152*/") + "vm" + text1;
						}
						System.err.println("119 " + idte.getIdent() + " " + idte.getStatus());
						String text2 = (Emit.emit("/*114*/") + String.format("%s is UNKNOWN", text1));
						addRef(text2, Ref.MEMBER);
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + ia2.getEntry().getStatus());
					}
				}
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

	public String getIdentIAPath_Proc(ProcTableEntry aPrte) {
		final String text;
		final BaseGeneratedFunction generated = aPrte.getFunctionInvocation().getGenerated();

		if (generated == null)
			throw new IllegalStateException();

		final GeneratedContainerNC genClass = (GeneratedContainerNC) generated.getGenClass();

		if (generated instanceof GeneratedConstructor) {
			int y = 2;
			final String constructorNameText = generated.getFunctionName();

			text = String.format("ZC%d%s", genClass.getCode(), constructorNameText);
			addRef(text, Ref.CONSTRUCTOR);
		} else {
			text = String.format("Z%d%s", genClass.getCode(), generated.getFunctionName());
			addRef(text, Ref.FUNCTION);
		}

		return text;
	}

	boolean _getIdentIAPath_IdentIAHelper(InstructionArgument ia_next,
										  List<String> sl,
										  int i,
										  int sSize,
										  OS_Element resolved_element,
										  BaseGeneratedFunction generatedFunction,
										  GeneratedNode aResolved,
										  final String aValue) {
		boolean b = false;
		if (resolved_element instanceof ClassStatement) {
			// Assuming constructor call
			int code;
			if (aResolved != null) {
				code = ((GeneratedContainerNC)aResolved).getCode();
			} else {
				code = -1;
				System.err.println("** 31116 not resolved "+resolved_element);
			}
			// README might be calling reflect or Type or Name
			// TODO what about named constructors -- should be called with construct keyword
			if (ia_next instanceof IdentIA) {
				IdentTableEntry ite = ((IdentIA) ia_next).getEntry();
				final String text = ite.getIdent().getText();
				if (text.equals("reflect")) {
					b = true;
					String text2 = String.format("ZS%d_reflect", code);
					addRef(text2, Ref.FUNCTION);
				} else if (text.equals("Type")) {
					b = true;
					String text2 = String.format("ZST%d", code); // return a TypeInfo structure
					addRef(text2, Ref.FUNCTION);
				} else if (text.equals("Name")) {
					b = true;
					String text2 = String.format("ZSN%d", code);
					addRef(text2, Ref.FUNCTION); // TODO make this not a function
				} else {
					assert i == sSize-1; // Make sure we are ending with a constructor call
					// README Assuming this is for named constructors
					String text2 = String.format("ZC%d%s", code, text);
					addRef(text2, Ref.CONSTRUCTOR);
				}
			} else {
				assert i == sSize-1; // Make sure we are ending with a constructor call
				String text2 = String.format("ZC%d", code);
				addRef(text2, Ref.CONSTRUCTOR);
			}
		} else if (resolved_element instanceof ConstructorDef) {
			assert i == sSize - 1; // Make sure we are ending with a constructor call
			int code;
			if (aResolved != null) {
				code = ((BaseGeneratedFunction) aResolved).getCode();
			} else {
				code = -1;
				System.err.println("** 31161 not resolved " + resolved_element);
			}
			// README Assuming this is for named constructors
			String text = ((ConstructorDef) resolved_element).name();
			String text2 = String.format("ZC%d%s", code, text);
			addRef(text2, Ref.CONSTRUCTOR);
		} else if (resolved_element instanceof FunctionDef) {
			OS_Element parent = resolved_element.getParent();
			int code = -1;
			if (aResolved != null) {
				if (aResolved instanceof BaseGeneratedFunction) {
					final BaseGeneratedFunction rf = (BaseGeneratedFunction) aResolved;
					GeneratedNode gc = rf.getGenClass();
					if (gc instanceof GeneratedContainerNC) // and not another function
						code = ((GeneratedContainerNC) gc).getCode();
					else
						code = -2;
				} else if (aResolved instanceof GeneratedClass) {
					final GeneratedClass generatedClass = (GeneratedClass) aResolved;
					code = generatedClass.getCode();
				}
			}
			// TODO what about overloaded functions
			assert i == sSize-1; // Make sure we are ending with a ProcedureCall
			sl.clear();
			if (code == -1) {
//				text2 = String.format("ZT%d_%d", enclosing_function._a.getCode(), closure_index);
			}
			String text2 = String.format("Z%d%s", code, ((FunctionDef) resolved_element).name());
			addRef(text2, Ref.FUNCTION);
		} else if (resolved_element instanceof DefFunctionDef) {
			OS_Element parent = resolved_element.getParent();
			int code;
			if (aResolved != null) {
				assert aResolved instanceof BaseGeneratedFunction;
				final BaseGeneratedFunction rf = (BaseGeneratedFunction) aResolved;
				GeneratedNode gc = rf.getGenClass();
				if (gc instanceof GeneratedContainerNC) // and not another function
					code = ((GeneratedContainerNC) gc).getCode();
				else
					code = -2;
			} else {
				if (parent instanceof ClassStatement) {
					code = ((ClassStatement) parent)._a.getCode();
				} else if (parent instanceof NamespaceStatement) {
					code = ((NamespaceStatement) parent)._a.getCode();
				} else {
					// TODO what about FunctionDef, etc
					code = -1;
				}
			}
			// TODO what about overloaded functions
			assert i == sSize-1; // Make sure we are ending with a ProcedureCall
			sl.clear();
			if (code == -1) {
//				text2 = String.format("ZT%d_%d", enclosing_function._a.getCode(), closure_index);
			}
			final DefFunctionDef defFunctionDef = (DefFunctionDef) resolved_element;
			String text2 = String.format("Z%d%s", code, defFunctionDef.name());
			addRef(text2, Ref.FUNCTION);
		} else if (resolved_element instanceof VariableStatement) {
			// first getParent is VariableSequence
			final String text2 = ((VariableStatement) resolved_element).getName();
			if (resolved_element.getParent().getParent() == generatedFunction.getFD().getParent()) {
				// A direct member value. Doesn't handle when indirect
//				text = Emit.emit("/*124*/")+"vsc->vm" + text2;
				addRef(text2, Ref.DIRECT_MEMBER, aValue);
			} else {
				final OS_Element parent = resolved_element.getParent().getParent();
				if (parent == generatedFunction.getFD()) {
					addRef(text2, Ref.LOCAL);
				} else {
//					if (parent instanceof NamespaceStatement) {
//						int y=2;
//					}
					addRef(text2, Ref.MEMBER, aValue);
				}
			}
		} else if (resolved_element instanceof PropertyStatement) {
			OS_Element parent = resolved_element.getParent();
			int code;
			if (parent instanceof ClassStatement) {
				code = ((ClassStatement) parent)._a.getCode();
			} else if (parent instanceof NamespaceStatement) {
				code = ((NamespaceStatement) parent)._a.getCode();
			} else {
//							code = -1;
				throw new IllegalStateException("PropertyStatement cant have other parent than ns or cls. " + resolved_element.getClass().getName());
			}
			sl.clear();  // don't we want all the text including from sl?
//			if (text.equals("")) text = "vsc";
//			text = String.format("ZP%dget_%s(%s)", code, ((PropertyStatement) resolved_element).name(), text); // TODO Don't know if get or set!
			String text2 = String.format("ZP%dget_%s", code, ((PropertyStatement) resolved_element).name()); // TODO Don't know if get or set!
			addRef(text2, Ref.PROPERTY_GET);
		} else if (resolved_element instanceof AliasStatement) {
			int y=2;
			NotImplementedException.raise();
//			text = Emit.emit("/*167*/")+((AliasStatement)resolved_element).name();
//			return _getIdentIAPath_IdentIAHelper(text, sl, i, sSize, _res)
		} else {
//						text = idte.getIdent().getText();
			System.out.println("1008 "+resolved_element.getClass().getName());
			throw new NotImplementedException();
		}
		return b;
	}

	@NotNull static List<InstructionArgument> _getIdentIAPathList(@NotNull InstructionArgument oo) {
		List<InstructionArgument> s = new LinkedList<InstructionArgument>();
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

	private final static class BuildState {
		StringBuilder sb = new StringBuilder();
		boolean open = false, needs_comma = false;

		public void appendText(final String text, final boolean erase) {
			if (erase)
				sb = new StringBuilder();

			sb.append(text);
		}
	}

	@NotNull
	public String build() {
		final BuildState st = new BuildState();

		for (Reference ref : refs) {
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

	/**
	 * Call before you call build
	 *
	 * @param sl3
	 */
	public void args(List<String> sl3) {
		args = sl3;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
