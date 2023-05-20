package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.DefaultBuffer;

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

		dl.notify(string);
	}


	public HashBuffer(final String aFileName, final HashBufferList aHashBufferList, final Executor aExecutor) {
		super("");

		parent = aHashBufferList;
		//parent.setNext(this);

		dl.notify(aFileName);
	}

	final DoubleLatch<String> dl = new DoubleLatch<>(aFilename -> {
		final HashBuffer outputBuffer = this;

		final @NotNull String hh;
		final @NotNull Operation<String> hh2 = Helpers.getHashForFilename(aFilename);

		if (hh2.mode() == Mode.SUCCESS) {
			hh = hh2.success();

			if (hh != null) {
				outputBuffer.append(hh);
				outputBuffer.append(" ");
				outputBuffer.append_ln(aFilename);
			}
		} else {
			throw new RuntimeException(hh2.failure());
		}
	});
}
