/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.expand;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tripleo
 *
 * Created 	Apr 24, 2020 at 21:50
 */
public class ExpandFunctions {

	private final OS_Module module;

	public ExpandFunctions(final OS_Module module) {
		this.module = module;
	}

	public void addClass(final ClassStatement klass, final OS_Module parent) {
//		System.out.print("class " + klass.clsName + "{\n");
		{
			for (final ClassItem element : klass.getItems())
				addClassItem(element, klass);
		}
//		System.out.print("}\n");
	}
	
	/**
	 * 
	 * @param element the element to add 
	 * @param parent could be class or namespace
	 */
	private void addClassItem(final ClassItem element, final OS_Element parent) {
		{
			if (element instanceof FunctionDef) {
				final FunctionDef fd = (FunctionDef) element;
//				System.out.print("void " + fd.funName + "(){\n");  // TODO: _returnType and mFal
				{
					for (final FunctionItem fi : fd.getItems())
						addFunctionItem(fi, fd);
				}
//				System.out.print("\n}\n\n");
			} else if (element instanceof ClassStatement) {
//				((ClassStatement) element).visitGen(this);
				System.err.println("91 " + element.getClass().getName());
				//addClassItem(element, parent); // TODO infintie recursion
			} else {
				System.err.println("[ExpandFunctions#addClassItem] "+element.getClass().getName());
			}
		}
	}
	
	public void addFunctionItem(final FunctionItem element, final FunctionDef parent) {
		final FunctionContext fc = (FunctionContext) parent.getContext();
		if (element instanceof VariableSequence) {
			for (final VariableStatement ii : ((VariableSequence) element).items()) {
//				addFunctionItem_deduceVariableStatement(parent, ii);
				final IntroducedVariable i = fc.introduceVariable(ii);
				//i.setModifiers(ii.typeModifiers);
			}
		} else if (element instanceof ProcedureCallExpression) {
			final ProcedureCallExpression pce = (ProcedureCallExpression) element;
			System.out.println(String.format("%s(%s);", pce./*target*/getLeft(), pce.exprList()));
		} else if (element instanceof Loop) {
			addFunctionItem_Loop((Loop) element, parent, fc);
		}  else if (element instanceof StatementWrapper) {
			final IExpression expr = ((StatementWrapper) element).getExpr();
			if (expr.getKind() == ExpressionKind.ASSIGNMENT) {
				final FunctionPrelimInstruction fi = fc.introduceVariable(expr.getLeft());
				final IExpression right_side = ((IBinaryExpression) expr).getRight();
				if (right_side.getKind() == ExpressionKind.IDENT) {
					//FunctionPrelimInstruction fi2 = fc.introduceVariable(right_side);
					final FunctionPrelimInstruction fi3 = fc.assign(fi, /*fi2*/right_side);
//					fi2.setInstructionNumber(-1); // why introduce in the first place?
				} else if (right_side.getKind() == ExpressionKind.PROCEDURE_CALL) {
					System.err.println("2002 here"); // TODO implement me
				} else
					throw new NotImplementedException();
			} else if (expr.getKind() == ExpressionKind.PROCEDURE_CALL) {
				final FunctionPrelimInstruction fi = expandProcedureCall((ProcedureCallExpression) expr, fc);
//				final ExpressionList args = ((ProcedureCallExpression) expr).getArgs();
//				fc.makeProcCall(fi, args);
				final int y=2;
			} else throw new NotImplementedException();
		} else if (element instanceof IfConditional) {
		} else if (element instanceof ClassStatement) {
			//
			// DON'T MODIFY  NAMETABLE
			//
//			parent._a.getContext().nameTable().add((OS_Element) element, ((ClassStatement) element).getName(), new OS_Type((ClassStatement) element));
		} else {
			System.out.println("91 "+element);
			throw new NotImplementedException();
		}
	}

	private void addFunctionItem_Loop(final Loop loop, final FunctionDef parent, final FunctionContext fc) {

		if (loop.getType() == LoopTypes.FROM_TO_TYPE) {
			//
			// DON'T MODIFY  NAMETABLE
			//
//			parent.getContext().add(ie(loop.getIterName()), ie(loop.getIterName()));

//			String varname="vt"+loop.getIterName();
			final ToExpression toex = new ToExpression(loop.getFromPart(), loop.getToPart());
			deduceExpression_(toex.getLeft(), parent.getContext(), fc);
			deduceExpression_(toex.getRight(), parent.getContext(), fc);

			if (loop.getFromPart() instanceof IdentExpression) {
				//
				// DON'T MODIFY  NAMETABLE
				//
//				loop.getContext().add((OS_Element) toex.getLeft(), ie(loop.getIterName()), toex.getLeft().getType());
			} else
				throw new NotImplementedException();

//			final NumericExpression fromPart = (NumericExpression)loop.getFromPart();
//			if (loop.getToPart() instanceof NumericExpression) {
//				final NumericExpression toPart = (NumericExpression)loop.getToPart();
//
//				System.out.println(String.format("{for (int %s=%d;%s<=%d;%s++){\n\t",
//						varname, fromPart.getValue(),
//						varname, toPart.getValue(),  varname));
//			} else if (loop.getToPart() instanceof IdentExpression) {
//				final IdentExpression toPart = (IdentExpression)loop.getToPart();
//
//				System.out.println(String.format("{for (int %s=%d;%s<=%s;%s++){\n\t",
//						varname, fromPart.getValue(),
//						varname, "vv"+toPart.getText(),  varname));
//
//			}
//			for (StatementItem item : loop.getItems()) {
//				System.out.println("\t"+item);
//			}
//			System.out.println("}");
		} else if (loop.getType() == LoopTypes.EXPR_TYPE) {
			addFunctionItem_Loop_EXPR_TYPE(loop, parent, fc);
		} else throw new NotImplementedException();
	}

	@NotNull
	private IdentExpression ie(final String s) {
		return new IdentExpression(Helpers.makeToken(s));
	}

	private void addFunctionItem_Loop_EXPR_TYPE(final Loop loop, final FunctionDef parent, final FunctionContext fc) {
		if (loop.getIterName() != null) {
			//
			// DON'T MODIFY  NAMETABLE
			//
//			parent.getContext().add(
//					ie(loop.getIterName()),
//					ie(loop.getIterName()));
		} else {
			System.out.println("loop.getIterName() == null");
//				String varname="vt"+loop.getIterName();
		}
		final ToExpression toex;
		if (loop.getFromPart() == null)
			toex = new ToExpression(new NumericExpression(0), loop.getToPart());
		else
			toex = new ToExpression(loop.getFromPart(), loop.getToPart());
		deduceExpression_(toex.getLeft(), parent.getContext(), fc);
		deduceExpression_(toex.getRight(), parent.getContext(), fc);

		if (loop.getFromPart() instanceof IdentExpression) {
			//
			// DON'T MODIFY  NAMETABLE
			//
//			loop.getContext().add((OS_Element) toex.getLeft(), ie(loop.getIterName()), toex.getLeft().getType());
		} else if (loop.getFromPart() == null) {
			System.out.println("88 loop.getFromPart() == null");
		} else
			throw new NotImplementedException();
//			if (loop.getToPart() instanceof NumericExpression) {
//				String varname="vt0_TODO";
//				final NumericExpression toPart = (NumericExpression)loop.getToPart();
//
//				System.out.println(String.format("{for (int %s=%d;%s<=%d;%s++){\n\t",
//						varname, 0,
//						varname, toPart.getValue(),  varname));
				for (final StatementItem item : loop.getItems()) {
					System.out.println("\t"+item);
					if (item instanceof VariableSequence) {
//						fd._a.setCode(nextFunctionCode());
//						parent._a.getContext().add(element, null);
						for (final VariableStatement ii : ((VariableSequence) item).items())
							deduceVariableStatement(loop, ii, fc);
					} else if (item instanceof StatementWrapper) {
						final IExpression e = ((StatementWrapper) item).getExpr();
						if (e instanceof BasicBinaryExpression) {
							deduceExpression_(e.getLeft(), loop.getContext(), fc);
							deduceExpression_(((BasicBinaryExpression) e).getRight(), loop.getContext(), fc);
							e.setType(e.getLeft().getType());
						}
					}

				}
//				System.out.println("}");
//			} else if (loop.getToPart() instanceof DotExpression) {
//				System.out.println("94 "+loop.getToPart().getClass().getName());
//				NotImplementedException.raise();
//			} else {
//				System.out.println("95 "+loop.getToPart().getClass().getName());
//				throw new NotImplementedException();
//			}
	}

	private void deduceExpression_(final IExpression expression, final Context context, final FunctionContext fc) {
		final FunctionPrelimInstruction fi = expandExpression(expression, fc);
		//expression.setType(t);
		final int y=2;
	}

	private FunctionPrelimInstruction expandProcedureCall(final ProcedureCallExpression pce, final FunctionContext fc) {
		FunctionPrelimInstruction i;
		final ExpressionKind pce_left_kind = pce.getLeft().getKind();
		if (pce_left_kind == ExpressionKind.PROCEDURE_CALL) {
			i =  expandProcedureCall((ProcedureCallExpression) pce.getLeft(), fc);
		} else if (pce_left_kind == ExpressionKind.DOT_EXP){
			final DotExpression de = (DotExpression) pce.getLeft();
			i =  expandExpression(de.getLeft(), fc);
			if (de.getRight().getKind() == ExpressionKind.IDENT) {
				i = fc.dotExpression(i, de.getRight());
				i = fc.makeProcCall(i, pce.getArgs()); // TODO look below
				//return i;
			} else {
				throw new NotImplementedException();
			}
			//i = fc.makeProcCall(i, pce.getArgs());
		} else if (pce_left_kind == ExpressionKind.IDENT) {
			final IntroducedVariable intro = fc.introduceVariable(pce.getLeft());
			intro.makeIntoFunctionCall(pce.getArgs()); // TODO look above
			i = intro;
		} else {
			throw new NotImplementedException();
		}
		return i;
	}

	public void deduceVariableStatement(final OS_Element parent, @NotNull final VariableStatement vs, final FunctionContext fc) {
		{
			OS_Type dtype = null;
			if (vs.typeName().isNull()) {
				if (vs.initialValue() != null) {
					final IExpression iv = vs.initialValue();
					if (iv instanceof NumericExpression) {
						dtype = new OS_Type(BuiltInTypes.SystemInteger);
					} else if (iv instanceof IdentExpression) {
						final LookupResultList lrl = parent.getContext().lookup(((IdentExpression) iv).getText());
						for (final LookupResult n: lrl.results()) {
							System.out.println("99 "+n);
						}
					} else if (iv instanceof ProcedureCallExpression) {
						final ProcedureCallExpression pce = (ProcedureCallExpression) iv;
						final IExpression left = pce.getLeft();
						if (left.getKind() == ExpressionKind.IDENT) {
							deduceVariableStatement_procedureCallExpression(parent, iv, pce, (IdentExpression) left, fc);
						}
					}
				}
			} else {
				dtype = new OS_Type(vs.typeName());
			}
//100			parent._a.getContext().add(vs, vs.getName(), dtype);
//				String theType;
//				if (ii.typeName().isNull()) {
////					theType = "int"; // Z0*
//					theType = ii.initialValueType();
//				} else{
//					theType = ii.typeName().getName();
//				}
			System.out.println(String.format("[#addFunctionItem_deduceVariableStatement] %s %s;", vs.getName(), dtype));

		}
	}

	private void deduceVariableStatement_procedureCallExpression(
			@NotNull final OS_Element parent, final IExpression iv,
			final ProcedureCallExpression pce, @NotNull final IdentExpression left, final FunctionContext fc) {
		final String text = left.getText();
		final LookupResultList lrl = parent.getContext().lookup(text);
		System.out.println("98 "+/*n*/iv);
		if (lrl.results().size() == 0 )
			System.err.println("596 no results for "+text);
		for (final LookupResult n: lrl.results()) {
			System.out.println("597 "+n);
//			Helpers.printXML(iv, new TabbedOutputStream(System.out));
		}
		final Collection<IExpression> expressions = pce.getArgs().expressions();
		final List<FunctionPrelimInstruction> q = expressions.stream()
				.map(n -> expandExpression(n, fc))
				.collect(Collectors.toList());
		System.out.println("590 "+q);
		NotImplementedException.raise();
	}

	public FunctionPrelimInstruction expandExpression(@NotNull final IExpression n, final FunctionContext fc) {
		if (n.getKind() == ExpressionKind.IDENT) {
			final LookupResultList lrl = /*context*/fc.lookup(((IdentExpression)n).getText());
			final ErrSink errSink = module.parent.getErrSink();
			if (/*lrl.results().size() == 1*/true) { // TODO the reason were having problems here is constraints vs shadowing
				// TODO what to do here??
				final OS_Element element = lrl.chooseBest(null);
				if (element instanceof VariableStatement) {
					final VariableStatement vs = (VariableStatement) element;
					if (vs.typeName() != null) {
						final FunctionPrelimInstruction i;
						if (vs.typeName().isNull()) {
							i = expandExpression(vs.initialValue(), fc);
							final int y=2;
							return i;
						} else {
							i = fc.introduceVariable(vs);
							return i;
						}
//						return new OS_Type(((VariableStatement) element).typeName());
					}
				} else if (element instanceof FormalArgListItem) {
					final NormalTypeName typeName = (NormalTypeName) ((FormalArgListItem) element).typeName();
					if (typeName != null) {
//						return new OS_Type(typeName);
					}
				}
				System.err.println("89 "+n);//element.getClass().getName());
				errSink.reportError("type not specified: "+ getElementName(element));
				return null;
			}
			errSink.reportError("IDENT not found: "+((IdentExpression) n).getText());
			NotImplementedException.raise();
		} else if (n.getKind() == ExpressionKind.NUMERIC) {
//			return new OS_Type(BuiltInTypes.SystemInteger);
		} else if (n.getKind() == ExpressionKind.DOT_EXP) {
			final DotExpression de = (DotExpression) n;
			final FunctionPrelimInstruction left_type = expandExpression(de.getLeft(), fc);
//			var right_type = deduceExpression(de.getRight(), left_type.getClassOf().getContext(), fc);
			final int y=2;
		} else if (n.getKind() == ExpressionKind.GET_ITEM) {
			final int y=2;
			final FunctionPrelimInstruction i = fc.introduceFunction(n.getLeft());
			final IntroducedFunction nn = (IntroducedFunction) i;
			nn.setName("__getitem__");
			nn.setArgs(tripleo.elijah.util.Helpers.List_of(((GetItemExpression)n).index));
			return i;
		} else if (n.getKind() == ExpressionKind.PROCEDURE_CALL) {
			return expandProcedureCall((ProcedureCallExpression) n, fc);
		}
		
		return null;
	}

	private String getElementName(final OS_Element element) {
		if (element instanceof VariableStatement) {
			return "<VariableStatement>";
		} else if (element instanceof FormalArgListItem) {
			return ((FormalArgListItem) element).name();
		} else if (element instanceof OS_Element2) {
			return ((OS_Element2) element).name();
		}
		return "<"+element.getClass().getName()+">";
	}

	/** Only interested in classes and namespaces here, as that's where the functions are */
	private void addModuleItem(final ModuleItem element) {
		if (element instanceof ClassStatement) {
			final ClassStatement cl = (ClassStatement) element;
			addClass(cl, module);
		} else if (element instanceof NamespaceStatement) {
			final NamespaceStatement ns = (NamespaceStatement) element;
			addNamespace(ns, module);
		}
	}
	private void addNamespace(final NamespaceStatement ns, final OS_Module parent) {
		System.out.print("namespace " + ns.getName() + "{\n");
		{
			for (final ClassItem element : ns.getItems())
				addClassItem(element, ns);
		}
		System.out.print("}\n");
	}

	public void expand() {
		System.out.println("-- [ExpandFunctions#expand] ----------------------------");
		for (final ModuleItem element : module.items) {
			addModuleItem(element);
		}
	}

}

//
//
//
