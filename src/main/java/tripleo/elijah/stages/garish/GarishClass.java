package tripleo.elijah.stages.garish;

import tripleo.elijah.stages.gen_c.CClassDecl;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.elijah.world.i.LivingClass;
import tripleo.elijah.world.impl.DefaultLivingClass;
import tripleo.util.buffer.Buffer;

public class GarishClass {
	private final LivingClass _lc;

	public GarishClass(final LivingClass aLivingClass) {
		_lc = aLivingClass;
		//_lc.setGarish(this);
	}

	public void garish(final GenerateC aGenerateC, final GenerateResult gr, final GenerateResultSink aResultSink) {
		final DefaultLivingClass dlc = (DefaultLivingClass) _lc;
		final EvaClass x = dlc.evaNode();


		if (x.generatedAlready) return ; ///////////////////////////////////////////////////////////////////////////////////////throw new Error();
		switch (x.getKlass().getType()) {
		// Don't generate class definition for these three
		case INTERFACE:
		case SIGNATURE:
		case ABSTRACT:
			return;
		}
		final CClassDecl decl = new CClassDecl(x);
		decl.evaluatePrimitive();
		final BufferTabbedOutputStream tosHdr = new BufferTabbedOutputStream();
		final BufferTabbedOutputStream tos    = new BufferTabbedOutputStream();
		try {
			tosHdr.put_string_ln("typedef struct {");
			tosHdr.incr_tabs();
			tosHdr.put_string_ln("int _tag;");
			if (!decl.prim) {
				for (EvaClass.VarTableEntry o : x.varTable){
					final String typeName = aGenerateC.getTypeNameGNCForVarTableEntry(o);
					tosHdr.put_string_ln(String.format("%s vm%s;", typeName, o.nameToken));
				}
			} else {
				tosHdr.put_string_ln(String.format("%s vsv;", decl.prim_decl));
			}

			String class_name = aGenerateC.getTypeName(x);
			int class_code = x.getCode();

			tosHdr.dec_tabs();
			tosHdr.put_string_ln("");
//			tosHdr.put_string_ln(String.format("} %s;", class_name));
			tosHdr.put_string_ln(String.format("} %s;  // class %s%s", class_name, decl.prim ? "box " : "", x.getName()));

			tosHdr.put_string_ln("");
			tosHdr.put_string_ln("");

			// TODO remove this block when constructors are added in dependent functions, etc in Deduce
			{
				// TODO what about named constructors and ctor$0 and "the debug stack"
				tos.put_string_ln(String.format("%s* ZC%d() {", class_name, class_code));
				tos.incr_tabs();
				tos.put_string_ln(String.format("%s* R = GC_malloc(sizeof(%s));", class_name, class_name));
				tos.put_string_ln(String.format("R->_tag = %d;", class_code));
				if (decl.prim) {
					// TODO consider NULL, and floats and longs, etc
					if (!decl.prim_decl.equals("bool"))
						tos.put_string_ln("R->vsv = 0;");
					else if (decl.prim_decl.equals("bool"))
						tos.put_string_ln("R->vsv = false;");
				} else {
					for (EvaClass.VarTableEntry o : x.varTable) {
//					final String typeName = getTypeNameForVarTableEntry(o);
						// TODO this should be the result of getDefaultValue for each type
						tos.put_string_ln(String.format("R->vm%s = 0;", o.nameToken));
					}
				}
				tos.put_string_ln("return R;");
				tos.dec_tabs();
				tos.put_string_ln(String.format("} // class %s%s", decl.prim ? "box " : "", x.getName()));
				tos.put_string_ln("");
			}
			tos.flush();
		} finally {
			tos.close();
			tosHdr.close();
			Buffer buf = tos.getBuffer();
//			LOG.info(buf.getText());
			gr.addClass(GenerateResult.TY.IMPL, x, buf, x.module().getLsp());
			Buffer buf2 = tosHdr.getBuffer();
//			LOG.info(buf2.getText());
			gr.addClass(GenerateResult.TY.HEADER, x, buf2, x.module().getLsp());
		}
		x.generatedAlready = true;
	}
}
