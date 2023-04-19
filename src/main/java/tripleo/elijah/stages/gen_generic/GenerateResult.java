/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 4/27/21 1:11 AM
 */
public class GenerateResult {
	final List<GenerateResultItem> _res = new ArrayList<GenerateResultItem>();
	private final Subject<GenerateResultItem> completedItems = ReplaySubject.<GenerateResultItem>create();
	private Map<String, OutputFileC> outputFiles;
	private int bufferCounter = 0;

	public GenerateResult(/*final Map<String, OutputFileC> aOutputFiles, final int aBufferCounter*/) {
/*
		outputFiles   = aOutputFiles;
		bufferCounter = aBufferCounter;
*/
		int y = 2;
	}

	public void addConstructor(EvaConstructor aEvaConstructor, Buffer aBuffer, TY aTY, LibraryStatementPart aLsp) {
		addFunction(aEvaConstructor, aBuffer, aTY, aLsp);
	}

	public void addFunction(BaseEvaFunction aGeneratedFunction, Buffer aBuffer, TY aTY, LibraryStatementPart aLsp) {
		add(aBuffer, aGeneratedFunction, aTY, aLsp, aGeneratedFunction.getDependency());
	}

	public void add(Buffer b, EvaNode n, TY ty, LibraryStatementPart aLsp, @NotNull Dependency d) {


		if (aLsp == null) {
			tripleo.elijah.util.Stupidity.println_err_2("*************************** buffer --> " + b.getText());
			return;
		}


		final GenerateResultItem item = new GenerateResultItem(ty, b, n, aLsp, d, ++bufferCounter);
		_res.add(item);
//		items.onNext(item);
	}

	public void addClass(TY ty, EvaClass aClass, Buffer aBuf, LibraryStatementPart aLsp) {
		add(aBuf, aClass, ty, aLsp, aClass.getDependency());
	}

	public void addNamespace(TY ty, EvaNamespace aNamespace, Buffer aBuf, LibraryStatementPart aLsp) {
		add(aBuf, aNamespace, ty, aLsp, aNamespace.getDependency());
	}

	public void signalDone(final Map<String, OutputFileC> aOutputFiles) {
		outputFiles = aOutputFiles;

		signalDone();
	}

	public void signalDone() {
		completedItems.onComplete();
	}

	public void outputFiles(final @NotNull Consumer<Map<String, OutputFileC>> cmso) {
		cmso.accept(outputFiles);
	}

	public void additional(final @NotNull GenerateResult aGenerateResult) {
		// TODO find something better
		//results()
		_res.addAll(aGenerateResult.results());
	}

	// region REACTIVE

	public List<GenerateResultItem> results() {
		return _res;
	}

	public void subscribeCompletedItems(Observer<GenerateResultItem> aGenerateResultItemObserver) {
		completedItems.subscribe(aGenerateResultItemObserver);
	}

	public void completeItem(GenerateResultItem aGenerateResultItem) {
		completedItems.onNext(aGenerateResultItem);
	}

	public enum TY {
		HEADER, IMPL, PRIVATE_HEADER
	}

	// endregion
}

//
//
//
