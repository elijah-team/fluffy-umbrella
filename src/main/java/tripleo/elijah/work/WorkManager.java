/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.work;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created 4/26/21 4:22 AM
 */
public class WorkManager {
	List<WorkList> jobs = new ArrayList<WorkList>();
	List<WorkList> doneWork = new ArrayList<WorkList>();

	public void addJobs(final WorkList aList) {
		jobs.add(aList);
	}

	@Nullable public WorkJob next() {
		final Iterator<WorkList> workListIterator = jobs.iterator();
		while (true) {
			if (workListIterator.hasNext()) {
				final WorkList workList = workListIterator.next();
//			for (WorkList workList :jobs) {
				if (!workList.isDone()) {
					for (final WorkJob w : workList.jobs) {
						if (!w.isDone())
							return w;
					}
					workList.setDone();
				} else {
					workListIterator.remove();
					doneWork.add(workList);
					return next();
				}
			} else
				return null;
		}
//		return null;
	}

	public void drain() {
		while (true) {
			@Nullable final WorkJob w = next();
			if (w == null) break;
			w.run(this);
		}
	}
}

//
//
//
