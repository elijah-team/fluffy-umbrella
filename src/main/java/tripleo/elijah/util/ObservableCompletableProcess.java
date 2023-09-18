package tripleo.elijah.util;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.diagnostic.ExceptionDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class ObservableCompletableProcess<T> implements Observer<T> {
	//private       CompletableProcess<T> cpt;
	private final Subject<T> subject = ReplaySubject.<T>create();
	private final List<CompletableProcess<T>> cps = new ArrayList<>();

	public void almostComplete() {
//		var v = ((ReplaySubject<?>)subject).getValues();
//		System.err.println("1919 "+v);

		for (final CompletableProcess<T> cp : cps) {
			cp.preComplete();
		}
	}

	@Override
	public void onComplete() {
		//cpt.complete();
	}

	@Override
	public void onError(@NonNull final Throwable e) {
		subject.onError(e);
		//cpt.error(new ExceptionDiagnostic((Exception) e));
	}

	@Override
	public void onNext(@NonNull final T aT) {
		subject.onNext(aT);
		//cpt.add(aT);
	}

	@Override
	public void onSubscribe(@NonNull final Disposable d) {
		subject.onSubscribe(d);
		//cpt.start();
	}

	public void subscribe(final @NotNull CompletableProcess<T> cp) {
		cps.add(cp);

		subject.subscribe(new Observer<T>() {
			@Override
			public void onComplete() {
				cp.preComplete();
				cp.complete();
			}

			@Override
			public void onError(@NonNull final Throwable e) {
				cp.error(new ExceptionDiagnostic((Exception) e));
			}

			@Override
			public void onNext(@NonNull final T aT) {
				cp.add(aT);
			}

			@Override
			public void onSubscribe(@NonNull final Disposable d) {
				cp.start();
			}
		});
	}

	public void subscribe(final @NotNull Observer<T> aCio) {
		subject.subscribe(aCio);
	}
}
