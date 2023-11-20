/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah;

import org.junit.Assert;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;

import java.io.File;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * @author Tripleo(sb)
 *
 */
public class FindBothSourceFiles {

	/**
	 * Compiler should find both parse files
	 * <p>
	 * Test method for {@link tripleo.elijah.Main#parseFile(java.lang.String, java.io.InputStream)}.
	 *
	 * @throws Exception
	 */
	@Test
	public final void compilerShouldFindBothParseFiles() throws Exception {
		final List<String> args = List_of("test/demo-el-normal", "test/demo-el-normal/main2", "-sE");
//		ErrSink eee = JMock.of(ErrSink.class);
		final ErrSink     eee = new StdErrSink();
		final Compilation c   = new CompilationImpl(eee, new IO());

		c.feedCmdLine(args);

		//fail("Not yet implemented"); // TODO
		Assert.assertTrue(c.getIO().recordedRead(new File(new File("test", "demo-el-normal"), "fact1.elijah")));
		Assert.assertTrue(c.getIO().recordedRead(new File(new File(new File("test", "demo-el-normal"), "main2"), "main2.elijah")));
	}

/*
	public static TextBuffer GenLocalAgn(CompilerContext cctx, LocalAgnTmpNode node, GenBuffer gbn) {
		// TODO Auto-generated method stub
		Buffer buf=gbn.moduleBufImpl(cctx.module());
//		if (node instanceof LocalAgnTmpNode) {
//			BufferSequenceBuilder sb=new BufferSequenceBuilder(6).
//					named("open").named("type").named("name").
//					named("equality").named("value").semieol();
//			sb.set("open", "{", XX.INDENT);
//			sb.set("type", node.getRight().genType, XX.SPACE);
//		}
		buf.append(node.getLeft().genText(cctx));
		buf.append(" = ");
		buf.append(node.getRight().genText(cctx));
		buf.append_ln(";");
		return buf;
	}

	public static TextBuffer BeginTmpSSACtx(CompilerContext cctx, TmpSSACtxNode node, GenBuffer gbn) {
		// TODO Auto-generated method stub
		Buffer buf=gbn.moduleBufImpl(cctx.module());
		buf.incr_i();
		buf.append_ln("{");
		if (node._tmp != null) {
//			NotImplementedException.raise();
//			buf.append(node._tmp.genName());
			buf.append(node._tmp.getRight().genType());
		}else {
			buf.append(node.getType().genText(cctx));
		}
		buf.append(" ");
		return buf;
	}

	public void GenMethHdr(CompilerContext cctx, MethHdrNode node, GenBuffer gbn) {
		Buffer buf = gbn.moduleBufHdr(cctx.module());
		buf.append_s(node.returnType().genType());
		buf.append(node.genName());
		buf.append("(");
		for (int c=0;c<node.argCount;c++) {
			buf.append_s(node.argument(c).getGenType());
			buf.append(node.argument(c).getGenName());
			if (c<node.argCount-1) {buf.append(",");}
		}
		buf.append(");");
	}
*/

}
	
//
//
//
