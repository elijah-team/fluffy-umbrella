package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.DotExpression;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.ProcedureCallExpression;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;

public class DeduceElement3_ProcTableEntry implements IDeduceElement3 {
    private final ProcTableEntry        principal;
    private final DeduceTypes2          deduceTypes2;
    private final BaseGeneratedFunction generatedFunction;
    private       Instruction           instruction;

    public DeduceElement3_ProcTableEntry(final ProcTableEntry aProcTableEntry, final DeduceTypes2 aDeduceTypes2, final BaseGeneratedFunction aGeneratedFunction) {
        principal         = aProcTableEntry;
        deduceTypes2      = aDeduceTypes2;
        generatedFunction = aGeneratedFunction;
    }

    @Override
    public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resolve(final Context aContext, final DeduceTypes2 dt2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OS_Element getPrincipal() {
        //return principal.getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal(); // README infinite loop

        return principal.getResolvedElement();//getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal();
    }

    @Override
    public DED elementDiscriminator() {
        return new DED.DED_PTE(principal);
    }

    @Override
    public DeduceTypes2 deduceTypes2() {
        return deduceTypes2;
    }

    @Override
    public BaseGeneratedFunction generatedFunction() {
        return generatedFunction;
    }

    @Override
    public GenType genType() {
        throw new UnsupportedOperationException("no type for PTE");
    } // TODO check correctness

    @Override
    public DeduceElement3_Kind kind() {
        return DeduceElement3_Kind.GEN_FN__PTE;
    }

    public ProcTableEntry getTablePrincipal() {
        return principal;
    }

    public BaseGeneratedFunction getGeneratedFunction() {
        return generatedFunction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(final Instruction aInstruction) {
        instruction = aInstruction;
    }

    public void doFunctionInvocation() {
        final FunctionInvocation fi = principal.getFunctionInvocation();

        if (fi == null) {
            if (principal.expression instanceof final ProcedureCallExpression exp) {
                final IExpression             left = exp.getLeft();

                if (left instanceof final DotExpression dotleft) {

                    if (dotleft.getLeft() instanceof final IdentExpression rl && dotleft.getRight() instanceof final IdentExpression rr) {

                        if (rl.getText().equals("a1")) {
                            final GeneratedClass[] gc = new GeneratedClass[1];

                            final InstructionArgument vrl = generatedFunction.vte_lookup(rl.getText());

                            assert vrl != null;

                            final VariableTableEntry vte = ((IntegerIA) vrl).getEntry();

                            vte.typePromise().then(left_type -> {
                                final ClassStatement cs = left_type.resolved.getClassOf(); // TODO we want a DeduceClass here. GeneratedClass may suffice

                                final ClassInvocation ci = deduceTypes2._phase().registerClassInvocation(cs);
                                ci.resolvePromise().then(gc2 -> {
                                    gc[0] = gc2;
                                });

                                final LookupResultList     lrl  = cs.getContext().lookup(rr.getText());
                                @Nullable final OS_Element best = lrl.chooseBest(null);

                                if (best != null) {
                                    final FunctionDef fun = (FunctionDef) best;

                                    final FunctionInvocation fi2 = new FunctionInvocation(fun, null, ci, deduceTypes2._phase().generatePhase); // TODO pte??

                                    principal.setFunctionInvocation(fi2); // TODO pte above

                                    final WlGenerateFunction j = fi2.generateFunction(deduceTypes2, best);
                                    j.run(null);

                                    final @NotNull IdentTableEntry ite      = ((IdentIA) principal.expression_num).getEntry();
                                    final OS_Type                  attached = ite.type.getAttached();

                                    fi2.generatePromise().then(gf -> {
                                        final int y4 = 4;
                                    });

                                    if (attached instanceof final OS_FuncType funcType) {

                                        final GeneratedClass x = gc[0];

                                        fi2.generatePromise().then(gf -> {
                                            final int y4 = 4;
                                        });

                                        final int y = 2;
                                    }
                                    final int yy = 2;
                                }
                            });
                            final int y = 2;
                        }
                    }
                }
            }
        }
    }

    public void _action_002_no_resolved_element(final InstructionArgument _backlink,
                                                final ProcTableEntry backlink,
                                                final DeduceTypes2.DeduceClient3 dc,
                                                final IdentTableEntry ite,
                                                final ErrSink errSink,
                                                final DeducePhase phase) {
        final OS_Element resolvedElement = backlink.getResolvedElement();

        if (resolvedElement == null) return; //throw new AssertionError(); // TODO feb 20

        try {
            final LookupResultList     lrl2 = dc.lookupExpression(ite.getIdent(), resolvedElement.getContext());
            @Nullable final OS_Element best = lrl2.chooseBest(null);
            assert best != null;
            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
        } catch (final ResolveError aResolveError) {
            errSink.reportDiagnostic(aResolveError);
            assert false;
        }

        action_002_1(principal, ite, false, phase, dc);
    }

    private void action_002_1(@NotNull final ProcTableEntry pte,
                              @NotNull final IdentTableEntry ite,
                              final boolean setClassInvocation,
                              final DeducePhase phase, final DeduceTypes2.DeduceClient3 dc) {
        final OS_Element resolvedElement = ite.getResolvedElement();

        assert resolvedElement != null;

        ClassInvocation ci = null;

        if (pte.getFunctionInvocation() == null) {
            @NotNull final FunctionInvocation fi;

            if (resolvedElement instanceof ClassStatement) {
                // assuming no constructor name or generic parameters based on function syntax
                ci = new ClassInvocation((ClassStatement) resolvedElement, null);
                ci = phase.registerClassInvocation(ci);
                fi = new FunctionInvocation(null, pte, ci, phase.generatePhase);
            } else if (resolvedElement instanceof final FunctionDef functionDef) {
                final IInvocation invocation  = dc.getInvocation((GeneratedFunction) generatedFunction);
                fi = new FunctionInvocation(functionDef, pte, invocation, phase.generatePhase);
                if (functionDef.getParent() instanceof ClassStatement) {
                    final ClassStatement classStatement = (ClassStatement) fi.getFunction().getParent();
                    ci = new ClassInvocation(classStatement, null); // TODO generics
                    ci = phase.registerClassInvocation(ci);
                }
            } else {
                throw new IllegalStateException();
            }

            if (setClassInvocation) {
                if (ci != null) {
                    pte.setClassInvocation(ci);
                } else
                    tripleo.elijah.util.Stupidity.println_err2("542 Null ClassInvocation");
            }

            pte.setFunctionInvocation(fi);
        }

//        el   = resolvedElement;
//        ectx = el.getContext();
    }


}
