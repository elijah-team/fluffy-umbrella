package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.CantDecideType;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce._post_ByteCode;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.util.NotImplementedException;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public interface PostBC_Processor {
	@Contract("_, _, _ -> new")
	static @NotNull PostBC_Processor make_VTE(@NotNull VariableTableEntry aVariableTableEntry, Context aFd_ctx, DeduceTypes2.DeduceClient1 aDeduceTypes2) {
		OS_Type vte_type_attached = aVariableTableEntry.type.getAttached();

		switch (aVariableTableEntry.vtt) {
		case SELF:
			return new PostBC_Processor__VTE_SELF(aVariableTableEntry, aFd_ctx, vte_type_attached, aDeduceTypes2);
		case RESULT:
			return new PostBC_Processor__VTE_RESULT(aVariableTableEntry, aFd_ctx, vte_type_attached, aDeduceTypes2);
		case ARG:
			return new PostBC_Processor__VTE_ARG(aVariableTableEntry, aFd_ctx, vte_type_attached, aDeduceTypes2);
		case VAR:
			return new PostBC_Processor__VTE_VAR(aVariableTableEntry, aFd_ctx, vte_type_attached, aDeduceTypes2);
		case TEMP:
			return new PostBC_Processor__VTE_TEMP(aVariableTableEntry, aFd_ctx, vte_type_attached, aDeduceTypes2);
		default:
			throw new IllegalStateException("Unexpected value: " + aVariableTableEntry.vtt);
		}
	}

	Maybe<OS_Type> doHasTypeAttached();

	void doSetType(DeduceType3 aDeduceType3, ErrSink aErrSink1);

	DeduceType3 getType(final _post_ByteCode aPost_byteCode, final ErrSink aErrSink1);

	DeduceType3 doNoTypeAttached(final ErrSink errSink1);

	abstract class __PostBC_Processor__VTE implements PostBC_Processor {
		private static DeduceType3 doNoTypeAttached__zero_potential(final @NotNull VariableTableEntry vte, final Supplier<CantDecideType> cdt) {
			// invariant: potential_size == 0

			DeduceType3 r;

			// TODO why both code paths the same? (evolution??)
			switch (vte.vtt) {
			case RESULT:
			case SELF:
				// Result is handled by phase.typeDecideds, self is always valid
				r = new DeduceType3(DeduceType3.dispatch(vte), null, cdt.get());
				break;
			default:
				assert vte.getName() != null;

				r = new DeduceType3(DeduceType3.dispatch(vte), null, cdt.get());
				break;
			}

			return r;
		}

		private static @NotNull DeduceType3 doNoTypeAttached__single_potential(final VariableTableEntry vte, final DeduceTypes2.@NotNull DeduceClient1 deduceTypes2) {
			OS_Type     attached = deduceTypes2.getPotentialTypesVte(vte).get(0).getAttached();
			DeduceType3 r        = new DeduceType3(attached, null);

			// ... ZeroPotentialDiagnostic
			ZeroPotentialDiagnostic zpd = new ZeroPotentialDiagnostic();

			// ---------------------- vte.type.setAttached(attached);
			return r;
		}

		protected abstract DeduceTypes2.DeduceClient1 deduceTypes2();

		protected abstract VariableTableEntry vte();

		protected abstract Context ctx();

		@Override
		public void doSetType(final @NotNull DeduceType3 aDeduceType3, final @NotNull ErrSink errSink1) {
			final VariableTableEntry vte = vte();

			if (aDeduceType3.isException()) {
				deduceTypes2().LOG_err("703-0000 " + vte.getName() + " " + vte.potentialTypes());
				aDeduceType3.reportDiagnostic(errSink1);
			} else {
				vte.type.setAttached(aDeduceType3.getGenType());
			}
		}

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			@NotNull DeduceType3             r;
			final DeduceTypes2.DeduceClient1 deduceTypes2   = deduceTypes2();
			final VariableTableEntry         vte            = vte();
			final Supplier<CantDecideType>   cdt            = () -> new CantDecideType(vte, vte.potentialTypes());
			final int                        potential_size = vte.potentialTypes().size();

			switch (potential_size) {
			case 0: // potential_size == 0
				r = doNoTypeAttached__zero_potential(vte, cdt);
				break;
			case 1: // potential_size == 1
				r = doNoTypeAttached__single_potential(vte, deduceTypes2);
				break;
			default: // potential_size > 1
				r = doNoTypeAttached__numerous_potential(vte.getDeduceElement3(), cdt, errSink1);
				break;
			}

			return r;
		}

		private @NotNull DeduceType3 doNoTypeAttached__numerous_potential(final @NotNull IDeduceElement3 de3,
																		  final @NotNull Supplier<CantDecideType> cdt,
																		  final @NotNull ErrSink errSink1) {
			// TODO Check type compatibility (--> what does this mean?)
			// TODO can inline more things here when ready/bored

			// 1. access
			final VariableTableEntry vte = vte();
			// 1a. use lazy (always)
			final CantDecideType diagnostic = cdt.get();

			// 2. report error (always CantDecideType here)
			final String message = String.format("703 %s %s", vte.getName(), vte.potentialTypes());
			// TODO why both?
			deduceTypes2().LOG_err(message);
			errSink1.reportDiagnostic(diagnostic);

			// 3. craft and return r-value
			DeduceElement3_ConstantTableEntry rr = (DeduceElement3_ConstantTableEntry) DeduceType3.dispatch(vte);
			rr.deduceElement3 = de3;
			rr.osType         = null;
			rr.diagnostic     = diagnostic;

			DeduceType3 r = new DeduceType3(de3, null, diagnostic);
			return r;
		}

		@Override
		public DeduceType3 getType(final @NotNull _post_ByteCode __aPost_byteCode, final ErrSink aErrSink1) {
			final DeduceType3 dty = postBC_getTypeFor_VTE(vte(), ctx(), aErrSink1);
			return dty;
		}

		private DeduceType3 postBC_getTypeFor_VTE(final @NotNull VariableTableEntry vte, Context fd_ctx, final ErrSink errSink1) {
			final DeduceType3                r;
			final DeduceTypes2.DeduceClient1 deduceClient1     = deduceTypes2();
			final OS_Type                    vte_type_attached = vte.type.getAttached();

			if (vte_type_attached == null) {
				DeduceType3 r2 = vte.getPostBC_Processor(fd_ctx, deduceClient1).doNoTypeAttached(errSink1);
				r = r2; //postBC_getTypeFor_VTE__no_type_attached(vte);
			} else {
				Maybe<OS_Type> r1 = vte.getPostBC_Processor(fd_ctx, deduceClient1).doHasTypeAttached();
				DeduceType3    r2 = new DeduceType3(r1.o, r1.exc);
				r = r2;
			}

			return r;
		}

		static class ZeroPotentialDiagnostic implements Diagnostic {
			@Override
			public String code() {
				NotImplementedException.raise();
				return null;
			}

			@Override
			public Severity severity() {
				NotImplementedException.raise();
				return null;
			}

			@Override
			public @NotNull Locatable primary() {
				NotImplementedException.raise();
				return null;
			}

			@Override
			public @NotNull List<Locatable> secondary() {
				NotImplementedException.raise();
				return null;
			}

			@Override
			public void report(final PrintStream stream) {
				NotImplementedException.raise();
				int y = 2;
			}
		}
	}

	class PostBC_Processor__VTE_SELF extends __PostBC_Processor__VTE {
		private final VariableTableEntry         variableTableEntry;
		private final Context                    fd_ctx;
		private final OS_Type                    vte_type_attached;
		private final DeduceTypes2.DeduceClient1 deduceTypes2;

		public PostBC_Processor__VTE_SELF(final VariableTableEntry aVariableTableEntry, final Context aFd_ctx, final OS_Type aVte_type_attached, final DeduceTypes2.DeduceClient1 aDeduceTypes2) {
			variableTableEntry = aVariableTableEntry;
			fd_ctx             = aFd_ctx;
			vte_type_attached  = aVte_type_attached;
			deduceTypes2       = aDeduceTypes2;
		}

		@Override
		public Maybe<OS_Type> doHasTypeAttached() {
			Maybe<OS_Type> r;

			r = new Maybe<>(vte_type_attached, null);

			return r;
		}
       
	/*
			@Override
			public DeduceType3 getType(final @NotNull _post_ByteCode aPost_byteCode, final ErrSink aErrSink1) {
				DED dt3 = new DED() {
					public DED elementDiscriminator() {
						return null;
					}
       
					public DeduceTypes2 deduceTypes2() {
						return null;
					}
       
					public GeneratedFunction generatedFunction() {
						return null;
					}
       
					public GenType genType() {
						return null;
					}
       
					@Override
					public Kind kind() {
						return null;
					}
				};
       
				return new DeduceType3((IDeduceElement3) dt3, variableTableEntry.genType.resolved, null);
			}
	*/

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			return null;
		}

		@Override
		protected DeduceTypes2.DeduceClient1 deduceTypes2() {
			return deduceTypes2;
		}

		@Override
		protected VariableTableEntry vte() {
			return variableTableEntry;
		}

		@Override
		protected Context ctx() {
			return fd_ctx;
		}
	}

	class PostBC_Processor__VTE_RESULT extends __PostBC_Processor__VTE {
		private final VariableTableEntry         variableTableEntry;
		private final Context                    fd_ctx;
		private final OS_Type                    vte_type_attached;
		private final DeduceTypes2.DeduceClient1 deduceTypes2;

		public PostBC_Processor__VTE_RESULT(final VariableTableEntry aVariableTableEntry, final Context aFd_ctx, final OS_Type aVte_type_attached, final DeduceTypes2.DeduceClient1 aDeduceTypes2) {
			variableTableEntry = aVariableTableEntry;
			fd_ctx             = aFd_ctx;
			vte_type_attached  = aVte_type_attached;
			deduceTypes2       = aDeduceTypes2;
		}

		@Override
		public Maybe<OS_Type> doHasTypeAttached() {
			Maybe<OS_Type> r;

			final OS_Type attached = vte_type_attached;

			final OS_Type.Type attachedType = attached.getType();
			switch (attachedType) {
			case USER:
				try {
					final GenType aGenType = deduceTypes2.resolve_type(attached, fd_ctx);
					System.out.println("xxxxxxxxxxxxxx " + aGenType);
					r = new Maybe<>(aGenType.resolved, null); // TODO incomplete!! (send whole GenType)
					// -------------------------- vte.type.setAttached(aGenType);
				} catch (final ResolveError aResolveError) {
					r = new Maybe<>(null, aResolveError);
					// -------------------------- aResolveError.printStackTrace();
					// -------------------------- assert false;
				}
				break;
			case UNIT_TYPE:
				r = new Maybe<>(attached, null);
				break;
			default:
				throw new IllegalStateException("non USER type for vte.vtt.RESULT");
			}

			return r;
		}

		//		@Override
		//		public DeduceType3 getType(final _post_ByteCode aPost_byteCode, final ErrSink aErrSink1) {
		//			return null;
		//		}

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			return null;
		}

		@Override
		protected DeduceTypes2.DeduceClient1 deduceTypes2() {
			return deduceTypes2;
		}

		@Override
		protected VariableTableEntry vte() {
			return variableTableEntry;
		}

		@Override
		protected Context ctx() {
			return fd_ctx;
		}
	}

	class PostBC_Processor__VTE_ARG extends __PostBC_Processor__VTE {
		private final VariableTableEntry         variableTableEntry;
		private final Context                    fd_ctx;
		private final OS_Type                    vte_type_attached;
		private final DeduceTypes2.DeduceClient1 deduceTypes2;

		public PostBC_Processor__VTE_ARG(final VariableTableEntry aVariableTableEntry, final Context aFd_ctx, final OS_Type aVte_type_attached, final DeduceTypes2.DeduceClient1 aDeduceTypes2) {
			variableTableEntry = aVariableTableEntry;
			fd_ctx             = aFd_ctx;
			vte_type_attached  = aVte_type_attached;
			deduceTypes2       = aDeduceTypes2;
		}

		@Override
		public Maybe<OS_Type> doHasTypeAttached() {
			Maybe<OS_Type> r;

			r = new Maybe<>(vte_type_attached, null);

			return r;
		}

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			return null;
		}

		@Override
		protected DeduceTypes2.DeduceClient1 deduceTypes2() {
			return deduceTypes2;
		}

		@Override
		protected VariableTableEntry vte() {
			return variableTableEntry;
		}

		@Override
		protected Context ctx() {
			return fd_ctx;
		}
	}

	class PostBC_Processor__VTE_VAR extends __PostBC_Processor__VTE {
		private final VariableTableEntry         variableTableEntry;
		private final Context                    fd_ctx;
		private final OS_Type                    vte_type_attached;
		private final DeduceTypes2.DeduceClient1 deduceTypes2;

		public PostBC_Processor__VTE_VAR(final VariableTableEntry aVariableTableEntry, final Context aFd_ctx, final OS_Type aVte_type_attached, final DeduceTypes2.DeduceClient1 aDeduceTypes2) {
			variableTableEntry = aVariableTableEntry;
			fd_ctx             = aFd_ctx;
			vte_type_attached  = aVte_type_attached;
			deduceTypes2       = aDeduceTypes2;
		}

		@Override
		public Maybe<OS_Type> doHasTypeAttached() {
			//Maybe<OS_Type> r;

			throw new IllegalStateException("xzxxxxxxxxxxxxxxxxxxxxxxxx not RESULT or SELF or ARG or TEMP, so VAR");

			//return r;
		}

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			return null;
		}

		@Override
		protected DeduceTypes2.DeduceClient1 deduceTypes2() {
			return deduceTypes2;
		}

		@Override
		protected VariableTableEntry vte() {
			return variableTableEntry;
		}

		@Override
		protected Context ctx() {
			return fd_ctx;
		}
	}

	class PostBC_Processor__VTE_TEMP extends __PostBC_Processor__VTE {
		private final VariableTableEntry         variableTableEntry;
		private final Context                    fd_ctx;
		private final OS_Type                    vte_type_attached;
		private final DeduceTypes2.DeduceClient1 deduceTypes2;

		public PostBC_Processor__VTE_TEMP(final VariableTableEntry aVariableTableEntry, final Context aFd_ctx, final OS_Type aVte_type_attached, final DeduceTypes2.DeduceClient1 aDeduceTypes2) {
			variableTableEntry = aVariableTableEntry;
			fd_ctx             = aFd_ctx;
			vte_type_attached  = aVte_type_attached;
			deduceTypes2       = aDeduceTypes2;
		}

		@Override
		public Maybe<OS_Type> doHasTypeAttached() {
			Maybe<OS_Type> r;

			r = new Maybe<>(vte_type_attached, null);

			return r;
		}
       
	/*
			@Override
			public DeduceType3 getType(final @NotNull _post_ByteCode aPost_byteCode, final ErrSink aErrSink1) {
				return null;
			}
	*/

		@Override
		public DeduceType3 doNoTypeAttached(final ErrSink errSink1) {
			throw new NotImplementedException();
		}

//			public IDeduceElement3 doNoTypeAttached(final ErrSink errSink1) {
//				return new DeduceElement3_VariableTableEntry(vte_type_attached);
//			}

		@Override
		protected DeduceTypes2.DeduceClient1 deduceTypes2() {
			return deduceTypes2;
		}

		@Override
		protected VariableTableEntry vte() {
			return variableTableEntry;
		}

		@Override
		protected Context ctx() {
			return fd_ctx;
		}
	}
}
