/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred2.Deferred;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.FailCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypeResolve;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce.ResolveUnknown;
import tripleo.elijah.util.NotImplementedException;

/**
 * Created 2/4/21 10:11 PM
 */
public abstract class BaseTableEntry {
	public enum Status {
		UNKNOWN, UNCHECKED, KNOWN
	}

public interface StatusListener {
		void onChange(IElementHolder eh, Status newStatus);
	}
	protected final DeferredObject2<OS_Element, Diagnostic, Void> _p_elementPromise  = new DeferredObject2<OS_Element, Diagnostic, Void>() {
		@Override
		public Deferred<OS_Element, Diagnostic, Void> resolve(final @Nullable OS_Element resolve) {
			if (resolve == null) {
				if (BaseTableEntry.this instanceof VariableTableEntry vte) {
					switch (vte.getVtt()) {
					case SELF, TEMP, RESULT -> {
						return super.resolve(resolve);
					}
					}
				}
				NotImplementedException.raise();
			}
			return super.resolve(resolve);
		}
	};
	//	private final DeferredObject2<OS_Element, Diagnostic, Void> elementPromise = new DeferredObject2<OS_Element, Diagnostic, Void>();
	private final List<StatusListener> statusListenerList = new ArrayList<StatusListener>();
	protected OS_Element resolved_element;

	// region status
	protected Status     status = Status.UNCHECKED;
	DeduceTypeResolve typeResolve;

	public DeduceTypes2 __dt2;

	public BaseEvaFunction __gf;

	public DeduceTypes2 _deduceTypes2() {
		return __dt2;
	}

	public void _fix_table(final DeduceTypes2 aDeduceTypes2, final @NotNull BaseEvaFunction aEvaFunction) {
		__dt2 = aDeduceTypes2;
		__gf  = aEvaFunction;
	}

	public void addStatusListener(final StatusListener sl) {
		statusListenerList.add(sl);
	}

	public void elementPromise(final DoneCallback<OS_Element> dc, final FailCallback<Diagnostic> fc) {
		if (dc != null)
			_p_elementPromise.then(dc);
		if (fc != null)
			_p_elementPromise.fail(fc);
	}

	public OS_Element getResolvedElement() {
		return resolved_element;
	}

	// endregion status

	public Status getStatus() {
		return status;
	}

	public void setResolvedElement(final OS_Element aResolved_element) {
		if (_p_elementPromise.isResolved()) {
			if (resolved_element instanceof AliasStatement) {
				_p_elementPromise.reset();
			} else {
				assert resolved_element == aResolved_element;
				return;
			}
		}
		resolved_element = aResolved_element;
		_p_elementPromise.resolve(resolved_element);
	}

	public void setStatus(final Status newStatus, final IElementHolder eh) {
		status = newStatus;
		assert newStatus != Status.KNOWN || eh.getElement() != null;
		for (final StatusListener statusListener : statusListenerList) {
			statusListener.onChange(eh, newStatus);
		}
		if (newStatus == Status.UNKNOWN)
			if (!_p_elementPromise.isRejected())
				_p_elementPromise.reject(new ResolveUnknown());
	}

	protected void setupResolve() {
		typeResolve = new DeduceTypeResolve(this, ()->__dt2);
	}

	public void typeResolve(final GenType aGt) {
		throw new NotImplementedException();
	}

	public Promise<GenType, ResolveError, Void> typeResolvePromise() {
		return typeResolve.typeResolution();
	}

}

//
//
//
