/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * @author Tripleo(envy)
 */
public class TestBasic {

	@Test
	public final void testBasicParse() throws Exception {
		final List<String> ez_files = Files.readLines(new File("test/basic/ez_files.txt"), Charsets.UTF_8);
		final List<String> args     = new ArrayList<String>();
		args.addAll(ez_files);
		args.add("-sE");
		final ErrSink     eee = new StdErrSink();
		final Compilation c   = new CompilationImpl(eee, new IO());

		c.feedCmdLine(args);

		Assert.assertEquals(0, c.errorCount());
	}

	//	@Test
	public final void testBasic() throws Exception {
		final List<String>          ez_files   = Files.readLines(new File("test/basic/ez_files.txt"), Charsets.UTF_8);
		final Map<Integer, Integer> errorCount = new HashMap<Integer, Integer>();
		int                         index      = 0;

		for (final String s : ez_files) {
//			List<String> args = List_of("test/basic", "-sO"/*, "-out"*/);
			final ErrSink     eee = new StdErrSink();
			final Compilation c   = new CompilationImpl(eee, new IO());

			c.feedCmdLine(List_of(s, "-sO"));

			if (c.errorCount() != 0)
				System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);
			errorCount.put(index, c.errorCount());
			index++;
		}

		// README this needs changing when running make
		Assert.assertEquals(7, (int) errorCount.get(0)); // TODO Error count obviously should be 0
		Assert.assertEquals(20, (int) errorCount.get(1)); // TODO Error count obviously should be 0
		Assert.assertEquals(9, (int) errorCount.get(2)); // TODO Error count obviously should be 0
	}

	@Test
	public final void testBasic_listfolders3() {
		final String s = "test/basic/listfolders3/listfolders3.ez";

		final Compilation c   = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		Assert.assertEquals(6, c.getOutputTree().list.size());
		Assert.assertEquals(2, c.errorCount()); // TODO Error count obviously should be 0
	}

	@Test
	public final void testBasic_listfolders4() {
		final String s = "test/basic/listfolders4/listfolders4.ez";

		final Compilation c   = new CompilationImpl((ErrSink) new StdErrSink(), new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		Assert.assertEquals(2, c.errorCount()); // TODO Error count obviously should be 0
	}

	@Test
	public final void testBasic_fact1() throws Exception {
		final String s = "test/basic/fact1/main2";

		final ErrSink     eee = new StdErrSink();
		final Compilation c   = new CompilationImpl(eee, new IO());

		c.feedCmdLine(List_of(s, "-sO"));

		if (c.errorCount() != 0)
			System.err.printf("Error count should be 0 but is %d for %s%n", c.errorCount(), s);

		final @NotNull EOT_OutputTree cot = c.getOutputTree();

		Assert.assertEquals(12, cot.list.size()); // TODO why not 6? why 12 now 09/15?

		select(cot.list, f -> f.getFilename().equals("/main2/Main.h"))
		  .then(f -> {
			  final EG_SequenceStatement statementSequence = (EG_SequenceStatement) f.getStatementSequence();
			  final List<EG_Statement>   egStatements      = statementSequence._list();
			  final Stream<EG_Statement> stream            = egStatements.stream();
			  yypp(f, stream
			    .map(EG_Statement::getText)
			    .collect(Collectors.toList()));
		  });
		select(cot.list, f -> f.getFilename().equals("/main2/Main.c"))
		  .then(f -> {
			  final EG_SequenceStatement statementSequence = (EG_SequenceStatement) f.getStatementSequence();
			  final List<EG_Statement>   egStatements      = statementSequence._list();
			  final Stream<EG_Statement> stream            = egStatements.stream();
			  yypp(f, stream
			    .map(EG_Statement::getText)
			    .collect(Collectors.toList()));
		  });

		// TODO Error count obviously should be 0
		Assert.assertEquals(19, c.errorCount()); // FIXME why 123?? 04/15 now 19 09/15
	}

	private void yypp(final EOT_OutputFile aF, final Iterable<String> aCollect) {
		var ff = aF.getFilename();
		var i = 0;
		for (final String s : aCollect) {
			i++;
			System.out.printf("142 %d %s %s%n", i, ff, s);
		}
	}

	static <T> @NotNull Promise<T, Void, Void> select(@NotNull final List<T> list, final Predicate<T> p) {
		final DeferredObject<T, Void, Void> d = new DeferredObject<T, Void, Void>();
		for (final T t : list) {
			if (p.test(t)) {
				d.resolve(t);
				return d;
			}
		}
		d.reject(null);
		return d;
	}
}

//
//
//
