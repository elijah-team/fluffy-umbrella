package tripleo.elijah;

import static org.junit.Assert.assertEquals;
import static tripleo.elijah.util.Helpers.List_of;

import java.util.List;

import org.junit.Test;

public class Tfact1_main2Test {

	static class C_FnArg {

		public String theName() {
			return "C";
		}

		public String theType() {
			return "Z100 *";
		}
	}

	class C_FnHdr {
		private final String        _returnType;
		private final String        _fnName;
		private final List<C_FnArg> l;

		public C_FnHdr(final EL_Hdr aEh) {
			l           = List_of(new C_FnArg());
			_returnType = "void";
			_fnName     = "z100main";
		}

		public C_FnArg args(final int aI) {
			return l.get(aI);
		}

		public String fnName() {
			return _fnName;
		}

		public String returnType() {
			return _returnType;
		}
	}

	class el_Arg {
		public el_Arg(final el_genClass aMain, final el_outName aC) {

		}
	}

	class el_genClass {
		public el_genClass(final String aMain) {
		}
	}

	class EL_Hdr {

		private el_outName       _writename;
		private el_genClass      _declaring;
		private el_type_NoneType _rt;
		private List<el_Arg>     _arg;
		private el_outClass      _eclosing;
		private el_name          _name;

		public void args(final List<el_Arg> aArgs) {
			_arg = aArgs;
		}

		public void declaring(final el_genClass aMain) {
			_declaring = aMain;
		}

		public void enclosing(final el_outClass aMain) {
			_eclosing = aMain;
		}

		public void name(final el_name aMain) {
			_name = aMain;
		}

		public void rt(final el_type_NoneType aElTypeNoneType) {
			_rt = aElTypeNoneType;
		}

		public void writename(final el_outName aMain) {
			_writename = aMain;
		}
	}

	class el_name {
		public el_name(final String aMain) {

		}
	}

	class el_outClass {
		public el_outClass(final String aMain) {

		}
	}

	class el_outName {
		public el_outName(final String aMain) {

		}
	}

	interface el_type {
		interface el_type_rider {
		}

		class el_type_rider_NONE implements el_type_rider {
			el_type t;
		}

		enum el_type_type {
			NONE, CLASS // none(t), class(k)
		}

		el_type_rider rider();

		el_type_type type();
	}

	class el_type_NoneType implements el_type {
		@Override
		public el_type_rider rider() {
			return new el_type_rider_NONE();
		}

		@Override
		public el_type_type type() {
			return el_type_type.NONE;
		}
	}

	@Test
	public void z100_main_fn_hdr() {
		final EL_Hdr eh = new EL_Hdr();

		eh.rt(new el_type_NoneType());
		eh.declaring(new el_genClass("Main"));
		eh.enclosing(new el_outClass("Main"));
		eh.name(new el_name("main"));

		eh.writename(new el_outName("main"));

		final el_Arg arg1 = new el_Arg(new el_genClass("Main"), new el_outName("C"));
		eh.args(List_of(arg1));


		final C_FnHdr cf = new C_FnHdr(eh);

		assertEquals("void", cf.returnType());
		assertEquals("z100main", cf.fnName());
		assertEquals("Z100 *", cf.args(0).theType());
		assertEquals("C", cf.args(0).theName());
	}

	@Test
	public void z100_main_fn_hdr2() {
		final EL_Hdr eh = new EL_Hdr();

		eh.rt(new el_type_NoneType());
		eh.declaring(new el_genClass("Main"));
		eh.enclosing(new el_outClass("Main"));
		eh.name(new el_name("main"));

		eh.writename(new el_outName("main"));

		final el_Arg arg1 = new el_Arg(new el_genClass("Main"), new el_outName("C"));
		eh.args(List_of(arg1));


		final C_FnHdr cf = new C_FnHdr(eh);

		assertEquals("void", cf.returnType());
		assertEquals("z100main", cf.fnName());
		assertEquals("Z100 *", cf.args(0).theType());
		assertEquals("C", cf.args(0).theName());
	}
}
