package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.DefaultBuffer;

import java.io.IOException;
import java.util.concurrent.Executor;

/*
 * intent: HashBuffer
 *  - contains 3 sub-buffers: hash, space, and filename
 *  - has all logic to update and present hash
 *    - codec: MTL sha2 here
 *    - encoding: reg or multihash (hint hint...)
 */
public class HashBuffer extends DefaultBuffer {
	private final HashBufferList parent;

	public HashBuffer(final String string) {
		super(string);

		parent = null;
	}

	public HashBuffer(final String aFileName, final HashBufferList aHashBufferList, final Executor aExecutor, final ErrSink errSink) {
		super("");

		String[] y = new String[1];
		DoubleLatch<String> dl = new DoubleLatch<>(aFilename -> {
			y[0] = aFilename;

			final HashBuffer outputBuffer = this;

			@Nullable final String hh;
			try {
				hh = Helpers.getHashForFilename(aFilename, errSink);
			} catch (IOException aE) {
				throw new RuntimeException(aE);
			}

			if (hh != null) {
				outputBuffer.append(hh);
				outputBuffer.append(" ");
				outputBuffer.append_ln(aFilename);
			}
		});

		dl.notify(aFileName);

		parent = aHashBufferList;
		//parent.setNext(this);
	}
}
