/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.util.buffer.Buffer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 4/27/21 1:12 AM
 */
public class GenerateResultItem {
	public final @NotNull GenerateResult.TY    ty;
	public final @NotNull Buffer               buffer;
	public final @NotNull GeneratedNode        node;
	public final @NotNull LibraryStatementPart lsp;
	public final          int                  counter;
	private final         Dependency           dependency;
	public                String               output;
	public                IOutputFile          outputFile;

	public GenerateResultItem(final @NotNull GenerateResult.TY aTy,
	                          final @NotNull Buffer aBuffer,
	                          final @NotNull GeneratedNode aNode,
	                          final @NotNull LibraryStatementPart aLsp,
	                          final @NotNull Dependency aDependency,
	                          final int aCounter) {
		ty         = aTy;
		buffer     = aBuffer;
		node       = aNode;
		lsp        = aLsp;
		dependency = aDependency;
		counter    = aCounter;
	}

	public Dependency getDependency() {
		final List<DependencyRef> ds = dependencies();
		return dependency;
	}

	public List<DependencyRef> dependencies() {
//		List<DependencyRef> x = Lists.transform(new ArrayList<>(dependency.deps), new Function<Dependency, DependencyRef>() {
//			@Override
//			public DependencyRef apply(@Nullable Dependency input) {
//				assert input != null;
//				return input.dref;
//			}
//		});
		final List<DependencyRef> x = dependency.getNotedDeps().stream()
		                                        .map(dep -> dep.dref)
		                                        .collect(Collectors.toList());
		return x;
	}
}

//
//
//
