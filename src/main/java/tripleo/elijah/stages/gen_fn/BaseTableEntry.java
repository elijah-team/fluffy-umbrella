/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.FailCallback;
import org.jdeferred2.Promise;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypeResolve;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce.ResolveUnknown;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 2/4/21 10:11 PM
 */
public abstract class BaseTableEntry {
	// region resolved_element

	protected OS_Element resolved_element;

	private final DeferredObject2<OS_Element, Diagnostic, Void> elementPromise = new DeferredObject2<OS_Element, Diagnostic, Void>();

	public void elementPromise(final DoneCallback<OS_Element> dc, final FailCallback<Diagnostic> fc) {
		if (dc != null)
			elementPromise.then(dc);
		if (fc != null)
			elementPromise.fail(fc);
	}

	public OS_Element getResolvedElement() {
		return resolved_element;
	}

	public void setResolvedElement(final OS_Element aResolved_element) {
		if (elementPromise.isResolved()) {
			if (resolved_element instanceof AliasStatement) {
				elementPromise.reset();
			} else {
				assert resolved_element == aResolved_element;
				return;
			}
		}
		resolved_element = aResolved_element;
		elementPromise.resolve(resolved_element);
	}

	// endregion resolved_element

	// region status
	protected Status status = Status.UNCHECKED;
	private final List<StatusListener> statusListenerList = new ArrayList<StatusListener>();

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status newStatus, final IElementHolder eh) {
		status = newStatus;
		assert newStatus != Status.KNOWN || eh.getElement() != null;
		for (final StatusListener statusListener : statusListenerList) {
			statusListener.onChange(eh, newStatus);
		}
		if (newStatus == Status.UNKNOWN)
			if (!elementPromise.isRejected())
				elementPromise.reject(new ResolveUnknown());
	}

	public void addStatusListener(final StatusListener sl) {
		statusListenerList.add(sl);
	}

	public enum Status {
		UNKNOWN, UNCHECKED, KNOWN
	}

	public interface StatusListener {
		void onChange(IElementHolder eh, Status newStatus);
	}

	// endregion status

	DeduceTypeResolve typeResolve;

	public Promise<GenType, ResolveError, Void> typeResolvePromise() {
		return typeResolve.typeResolution();
	}

	protected void setupResolve() {
		typeResolve = new DeduceTypeResolve(this);
	}


}

//
//
//
