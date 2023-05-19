package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.comp.functionality.f203.F203;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.outputstatement.*;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.util.buffer.Buffer;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_WriteFiles implements WP_Indiviual_Step {
	private final WritePipeline      writePipeline;
	private final DoubleLatch<Triple<GenerateResult, @NotNull WritePipelineSharedState, @NotNull WP_State_Control>> dl;
	private       Operation<Boolean> op;

	@Contract(pure = true)
	public WPIS_WriteFiles(final @NotNull WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
		dl = new DoubleLatch<Triple<GenerateResult, @NotNull WritePipelineSharedState, @NotNull WP_State_Control>>((t -> {
			hasGenerateResult(t.getLeft(), t.getMiddle(), t.getRight());
		}));
	}

	private void hasGenerateResult(final @NotNull GenerateResult 			result,
								   final @NotNull WritePipelineSharedState 	st,
								   final @NotNull WP_State_Control 			sc) {
		final List<EOT_OutputFile> leof = new ArrayList<>();

		final Map<String, OS_Module> modmap = new HashMap<String, OS_Module>();
		for (final GenerateResultItem ab : st.getGr().results()) {
			modmap.put(ab.output, ab.node.module());
		}

		int yyyyyyyy=2;

		for (final String s : st.mmb.keySet()) {
			final Collection<Buffer> vs = st.mmb.get(s);

			final EOT_OutputFile eof = EOT_OutputFile.bufferSetToOutputFile(s, vs, st.c, modmap.get(s));
			leof.add(eof);
		}

		st.c.getOutputTree().set(leof);

		final File fn1 = choose_dir_name(st.c);

		try {
			__rest(st.mmb, fn1, leof, st.c);
		} catch (IOException aE) {
			op = Operation.failure(aE);
			sc.exception(aE);
			return;
		}

		if (false) {
			leof.stream()
					.forEach(outputFile -> {
						System.out.println(outputFile.getFilename());
						final EG_Statement   seqs  = outputFile.getStatementSequence();
						System.out.println(seqs.getText());
					});
		}

		op = Operation.success(true);
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		// 4. write files

		//final List<GenerateResultItem> generateResultItems = st.getGr().results();

		writePipeline.prom.then((final GenerateResult result) -> {
			dl.notifyData(Triple.of(result, st, sc));
		});

		dl.notifyLatch(true);
	}

	private @NotNull File choose_dir_name(final @NotNull Compilation c) {
		final File fn00 = new F203(c.getErrSink(), c).chooseDirectory();
		final File fn01 = new File(fn00, "code");

		return fn01;
	}

	private void __rest(final @NotNull Multimap<String, Buffer> mb,
						final @NotNull File 					aFile_prefix,
						final List<EOT_OutputFile> 				leof,
						final @NotNull Compilation 				c) throws IOException {
		aFile_prefix.mkdirs();
		final String prefix = aFile_prefix.toString();

		// TODO flag?
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
		//write_inputs(aFile_prefix);
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////

		for (final Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
			final String key  = entry.getKey();
			final Path   path = FileSystems.getDefault().getPath(prefix, key);
//			BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

			path.getParent().toFile().mkdirs();

			// TODO functionality
			System.out.println("201 Writing path: " + path);
			final CharSink x = c.getIO().openWrite(path);

			final EG_SingleStatement beginning = new EG_SingleStatement("", EX_Explanation.withMessage("write output file >> beginning"));
			final EG_Statement middle = new GE_BuffersStatement(entry);
			final EG_SingleStatement ending = new EG_SingleStatement("", EX_Explanation.withMessage("write output file >> ending"));
			final EX_Explanation explanation = EX_Explanation.withMessage("write output file");

			final EG_CompoundStatement seq = new EG_CompoundStatement(beginning, ending, middle, false, explanation);

			x.accept(seq.getText());
			((FileCharSink) x).close();

			final @NotNull EOT_OutputTree cot = c.getOutputTree();
			cot._putSeq(key, path, seq);

			final EOT_OutputFile off = new EOT_OutputFile(c, List_of(), path.toString(), EOT_OutputType.SOURCES, seq);
			cot.add(off);
		}
	}
}
