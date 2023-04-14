package tripleo.elijah.stages.deduce.calculate.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.calculate.rider.__Rider__Implement_construct;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.Objects;

public class __Logic_ImplementConstruct {
	private final DeduceTypes2.Implement_construct implement_construct;
	private /*final*/ Context context;
	private final __Rider__Implement_construct r;
	private final DeduceTypes2 dt2;

	public __Logic_ImplementConstruct(final DeduceTypes2.Implement_construct aImplement_construct, final __Rider__Implement_construct aR, final DeduceTypes2 aDeduceTypes2) {
		implement_construct = aImplement_construct;
		r                   = aR;
		dt2 = aDeduceTypes2;
	}

	static class __action_IdentIA__C {
		@Nullable OS_Element el3;
		@Nullable Context    ectx;

		public void ectx(final Context aContext) {
			ectx = aContext;
		}
	}

	public void action_IdentIA(final Context aContext) {
		final ErrSink errSink = dt2._errSink();
		final ElLog LOG = dt2._LOG();

		context             = aContext;

		@NotNull IdentTableEntry idte       = ((IdentIA) r.getExpression()).getEntry();
		DeducePath               deducePath = idte.buildDeducePath(r.getGeneratedFunction());

		final DeduceProcCall dpc = new DeduceProcCall(r.getPte());
		dpc.setDeduceTypes2(dt2, context, r.getGeneratedFunction(), errSink);
		final @Nullable DeduceElement target = dpc.target();
		int                           y      =2;

		@Nullable Context    ectx = r.getGeneratedFunction().getFD().getContext();
		__action_IdentIA__C c = new __action_IdentIA__C();
		__action_IdentIA__action(errSink, LOG, deducePath, ectx, c);
	}

	private void __action_IdentIA__action(final ErrSink errSink, final ElLog LOG, final DeducePath deducePath, @Nullable Context ectx, final __action_IdentIA__C c) {
		@Nullable OS_Element el3;
		for (int i = 0; i < deducePath.size(); i++) {
			InstructionArgument ia2 = deducePath.getIA(i);

			el3 = deducePath.getElement(i);

			if (ia2 instanceof IntegerIA) {
				@NotNull VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
				// TODO will fail if we try to construct a tmp var, but we never try to do that
				assert vte.vtt != VariableTableType.TEMP;
				assert el3     != null;
				assert i       == 0;
				ectx = deducePath.getContext(i);
				c.ectx(deducePath.getContext(i));
			} else if (ia2 instanceof IdentIA) {
				@NotNull IdentTableEntry idte2 = ((IdentIA) ia2).getEntry();
				final String             s     = idte2.getIdent().toString();
				LookupResultList         lrl   = ectx.lookup(s);
				@Nullable OS_Element     el2   = lrl.chooseBest(null);
				if (el2 == null) {
					assert el3 instanceof VariableStatement;
					@Nullable VariableStatement vs = (VariableStatement) el3;
					@NotNull TypeName           tn = vs.typeName();
					@NotNull OS_Type            ty = new OS_UserType(tn);

					GenType resolved = null;
					if (idte2.type == null) {
						// README Don't remember enough about the constructors to select a different one
						@NotNull TypeTableEntry tte = r.getGeneratedFunction().newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty);
						try {
							resolved = dt2.resolve_type(ty, tn.getContext());
							LOG.err("892 resolved: "+resolved);
							tte.setAttached(resolved);
						} catch (ResolveError aResolveError) {
							errSink.reportDiagnostic(aResolveError);
						}

						idte2.type = tte;
					}
					// s is constructor name
					implement_construct.implement_construct_type(idte2, ty, s, null);

					if (resolved == null) {
						try {
							resolved = dt2.resolve_type(ty, tn.getContext());
						} catch (ResolveError aResolveError) {
							errSink.reportDiagnostic(aResolveError);
//									aResolveError.printStackTrace();
							assert false;
						}
					}
					final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
					x.resolveType(resolved);
					resolved.genCIForGenType2(dt2);
					return;
				} else {
					if (i+1 == deducePath.size() && deducePath.size() > 1) {
						assert el3 == el2;
						if (el2 instanceof ConstructorDef) {
							@Nullable GenType type = deducePath.getType(i);
							if (type.nonGenericTypeName == null) {
								type.nonGenericTypeName = Objects.requireNonNull(deducePath.getType(i - 1)).nonGenericTypeName; // HACK. not guararnteed to work!
							}
							@NotNull OS_Type ty = new OS_UserType(type.nonGenericTypeName);
							implement_construct.implement_construct_type(idte2, ty, s, type);

							final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
							if (type.ci == null && type.node == null)
								type.genCIForGenType2(dt2);
							assert x != null;
							x.resolveTypeToClass(type.node);
						} else
							throw new NotImplementedException();
					} else {
						ectx = deducePath.getContext(i);
						c.ectx(deducePath.getContext(i));
					}
				}
//						implement_construct_type(idte/*??*/, ty, null); // TODO how bout when there is no ctor name
			} else {
				throw new NotImplementedException();
			}
		}
	}
}
