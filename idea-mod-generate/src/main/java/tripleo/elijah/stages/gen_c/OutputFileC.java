/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.gen_generic.DependencyRef;
import tripleo.elijah.stages.gen_generic.IOutputFile;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created 9/13/21 10:50 PM
 */
public class OutputFileC implements IOutputFile {
	private final String              output;
	private final List<DependencyRef> dependencies = new ArrayList<>();
	private final List<Dependency>    notedDeps    = new ArrayList<>();
	private final List<Buffer>        buffers      = new ArrayList<>(); // LinkedList??

	public OutputFileC(final String aOutput) {
		output = aOutput;
	}

	@Override
	public void putDependencies(final List<DependencyRef> aDependencies) {
		dependencies.addAll(aDependencies);
	}

	@Override
	public void putBuffer(final Buffer aBuffer) {
		buffers.add(aBuffer);
	}

	@Override
	public String getOutput() {
		final StringBuilder sb = new StringBuilder();

		final @NotNull Predicate<Dependency> dependencyPredicate = next -> {
			for (final DependencyRef dependency : dependencies) {
				if (next.dref == dependency) {
					return true;
				}
			}

			return false;
		};

		final List<Dependency> wnd = notedDeps.stream()
		                                      .filter(dependencyPredicate)
		                                      .collect(Collectors.toList());

/*
		//new ArrayList<Dependency>(notedDeps);
		final Iterator<Dependency> iterator = wnd.iterator();

		// TODO figure this dumb shht out
		while (iterator.hasNext()) {
			Dependency next = iterator.next();
			for (DependencyRef dependency : dependencies) {
				if (next.dref == dependency) {
					iterator.remove();
				}
			}
		}
*/

		assert wnd.size() == dependencies.size();

		for (final DependencyRef dependencyRaw : dependencies) {
			final CDependencyRef dependency = (CDependencyRef) dependencyRaw;
			final String         headerFile = dependency.getHeaderFile();
			final String         output     = String.format("#include \"%s\"\n", headerFile.substring(1));
			sb.append(output);
		}

		sb.append('\n');

		for (final Dependency dependency : wnd) {
			final String resolvedString = "" + dependency.resolved;
			final String output         = String.format("//#include \"%s\" // for %s\n", "nothing.h", resolvedString);
			sb.append(output);
		}

		sb.append('\n');

		for (final Buffer buffer : buffers) {
			sb.append(buffer.getText());
			sb.append('\n');
		}
		return sb.toString();
	}

	public void putDependencies(final Set<Dependency> aNotedDeps) {
		notedDeps.addAll(aNotedDeps);
	}
}

//
//
//
