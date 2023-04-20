package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.util.buffer.Buffer;
import tripleo.util.io.DisposableCharSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class WPIS_WriteFiles implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	public WPIS_WriteFiles(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
		// 4. write files
		Multimap<String, Buffer> mb = ArrayListMultimap.create();

		final List<GenerateResultItem> generateResultItems = st.getGr().results();

		writePipeline.prom.then(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(final GenerateResult result) {
/*
				for (final GenerateResultItem ab : generateResultItems) {
					mb.put(((CDependencyRef) ab.getDependency().getRef()).getHeaderFile(), ab.buffer); // TODO see above
				}

				assert st.mmb.equals(mb);
*/

				try {
					final String prefix = st.file_prefix.toString();

					for (final Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
						final String                       key = entry.getKey();
						final Supplier<Collection<Buffer>> e   = entry::getValue;

						write_files_helper_each(prefix, key, e);
					}
				} catch (Exception aE) {
					throw new RuntimeException(aE);
				}

			}

			@Contract("_, null, _ -> fail")
			private void write_files_helper_each(final String prefix,
												 final String key,
												 final @NotNull Supplier<Collection<Buffer>> e) throws Exception {
				assert key != null;

				final Path path = FileSystems.getDefault().getPath(prefix, key);
				// final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

				path.getParent().toFile().mkdirs();

				// TODO functionality
				System.out.println("201 Writing path: " + path);
				try (final DisposableCharSink fileCharSink = st.c.getIO().openWrite(path)) {
					for (final Buffer buffer : e.get()) {
						fileCharSink.accept(buffer.getText());
					}
				}
			}
		});

		for (final GenerateResultItem ab : generateResultItems) {
			mb.put(((CDependencyRef) ab.getDependency().getRef()).getHeaderFile(), ab.buffer); // TODO see above
		}

		//assert st.mmb.equals(mb);

		write_files_helper(mb);

		final List<EOT_OutputFile> leof = new ArrayList<>();

		final Map<String, OS_Module> modmap = new HashMap<String, OS_Module>();
		for (final GenerateResultItem ab : st.getGr().results()) {
			modmap.put(ab.output, ab.node.module());
		}

		int yyyyyyyy=2;

		for (final String s : st.mmb.keySet()) {
			final Collection<Buffer> vs = mb.get(s);

			final EOT_OutputFile eof = EOT_OutputFile.bufferSetToOutputFile(s, vs, st.c, modmap.get(s));
			leof.add(eof);
		}

		writePipeline.st.c.getOutputTree().set(leof);

		//final File fn1 = choose_dir_name();
		//
		//__rest(mb, fn1, leof);
	}

	private void write_files_helper(final Multimap<String, Buffer> aMb) {

	}
}
