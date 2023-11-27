/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.functionality.f203.F203;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.outputstatement.EG_CompoundStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.outputstatement.GE_BuffersStatement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.Buffer;
import tripleo.util.buffer.DefaultBuffer;
import tripleo.util.buffer.TextBuffer;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.BufferedWriter;
import tripleo.wrap.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember, AccessBus.AB_GenerateResultListener {
	final         OutputStrategy os;
	final         ElSystem       sys;
	private final Compilation    c;
	private final File           file_prefix;
	private       GenerateResult gr;

	public WritePipeline(@NotNull final AccessBus ab) {
		c = ab.getCompilation();

		file_prefix = new File("COMP", c.getCompilationNumberString());

		os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		sys         = new ElSystem();
		sys.verbose = false; // TODO flag? ie CompilationOptions
		sys.setCompilation(c);
		sys.setOutputStrategy(os);

		ab.subscribe_GenerateResult(this);
	}

	@Override
	public void run() throws Exception {
		sys.generateOutputs(gr);

		write_files();
		// TODO flag?
		write_buffers();
	}

	public void write_files() throws IOException {
		final Multimap<String, Buffer> mb = ArrayListMultimap.create();

		for (final GenerateResultItem ab : gr.results()) {
			mb.put(ab.output, ab.buffer);
		}

		final Map<String, OS_Module> modmap = new HashMap<String, OS_Module>();
		for (final GenerateResultItem ab : gr.results()) {
			modmap.put(ab.output, ab.node.module());
		}

		final List<EOT_OutputFile> leof = new ArrayList<>();

		for (final String s : mb.keySet()) {
			final Collection<Buffer> vs = mb.get(s);

			final EOT_OutputFile eof = EOT_OutputFile.bufferSetToOutputFile(s, vs, c, modmap.get(s));
			leof.add(eof);
		}

		c.getOutputTree().set(leof);

		final File fn1 = choose_dir_name();

		__rest(mb, fn1, leof);
	}

	public void write_buffers() throws FileNotFoundException {
		file_prefix.mkdirs();

		final PrintStream db_stream = new PrintStream(new File(file_prefix, "buffers.txt").wrapped());
		PipelineLogic.debug_buffers(gr, db_stream);
	}

	private @NotNull File choose_dir_name() {
		final File fn00 = new F203(c.getErrSink(), c).chooseDirectory();
		final File fn01 = new File(fn00, "code");

		return fn01;
	}

	private void __rest(final @NotNull Multimap<String, Buffer> mb, final @NotNull File aFile_prefix, final List<EOT_OutputFile> leof) throws IOException {
		aFile_prefix.mkdirs();
		final String prefix = aFile_prefix.toString();

		// TODO flag?
		write_inputs(aFile_prefix);

		for (final Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
			final String key  = entry.getKey();
			final Path   path = FileSystems.getDefault().getPath(prefix, key);

			var fnp = new _WP_FileNameProvider(key, aFile_prefix);

//			BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

			path.getParent().toFile().mkdirs();

			// TODO functionality
			System.out.println("201 Writing path: " + path);
			final CharSink x = c.getIO().openWrite(path);

			final EG_SingleStatement beginning   = new EG_SingleStatement("", EX_Explanation.withMessage("WritePipeline.beginning"));
			final EG_Statement       middle      = new GE_BuffersStatement(entry);
			final EG_SingleStatement ending      = new EG_SingleStatement("", EX_Explanation.withMessage("WritePipeline.ending"));
			final EX_Explanation     explanation = EX_Explanation.withMessage("write output file");

			final EG_CompoundStatement seq = new EG_CompoundStatement(beginning, ending, middle, false, explanation);

//			for (final @NotNull Buffer buffer : entry.getValue()) {
//				x.accept(buffer.getText());
//			}
			x.accept(seq.getText());
			((FileCharSink) x).close();

			final @NotNull EOT_OutputTree cot = c.getOutputTree();

			cot._putSeq(key, fnp, seq);
		}
	}

	private void write_inputs(final File file_prefix) throws IOException {
		final DefaultBuffer buf = new DefaultBuffer("");
//			FileBackedBuffer buf = new FileBackedBuffer(fn1);
//			for (OS_Module module : modules) {
//				final String fn = module.getFileName();
//
//				append_hash(buf, fn);
//			}
//
//			for (CompilerInstructions ci : cis) {
//				final String fn = ci.getFilename();
//
//				append_hash(buf, fn);
//			}

		final List<File> recordedreads = c.getIO().recordedreads;
		final List<String> recordedread_filenames = recordedreads.stream()
		                                                         .map(file -> file.toString())
		                                                         .collect(Collectors.toList());

		for (final @NotNull File file : recordedreads) {
			final String fn = file.toString();

			append_hash(buf, fn, c.getErrSink());
		}

		final File   fn1 = new File(file_prefix, "inputs.txt");
		final String s   = buf.getText();
		try (final Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn1.wrapped(), true)))) {
			w.write(s);
		}
	}

	private void append_hash(final TextBuffer aBuf, final String aFilename, final ErrSink errSink) throws IOException {
		@Nullable final String hh = Helpers.getHashForFilename(aFilename, errSink);
		if (hh != null) {
			aBuf.append(hh);
			aBuf.append(" ");
			aBuf.append_ln(aFilename);
		}
	}

	@Override
	public void gr_slot(final GenerateResult gr) {
		this.gr = gr;
	}
}

//
//
//
