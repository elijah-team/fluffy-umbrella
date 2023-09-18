package tripleo.elijah.util;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.diagnostic.ExceptionDiagnostic;

public class ObservableCompletableProcess2<T> implements Observer<T> {
	private       CompletableProcess<T> cpt;

	public void almostComplete() {
		cpt.preComplete();
	}

	@Override
	public void onSubscribe(@NonNull final Disposable d) {
		cpt.start();
	}

	@Override
	public void onNext(@NonNull final T aT) {
		cpt.add(aT);
	}

	@Override
	public void onError(@NonNull final Throwable e) {
		cpt.error(new ExceptionDiagnostic((Exception) e));
	}

	@Override
	public void onComplete() {
		cpt.complete();
	}

	public void subscribe(final @NotNull Observer<T> aCio) {
		cpt.start();
	}

	public void subscribe(final @NotNull CompletableProcess<T> cp) {
//		subscribe(new ); //!!
	}
}
