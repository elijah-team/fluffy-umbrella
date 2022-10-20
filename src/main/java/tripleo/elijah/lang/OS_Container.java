/**
 * 
 */
package tripleo.elijah.lang;

import java.util.List;

/**
 * @author Tripleo(sb)
 *
 * Created 	Dec 9, 2019 at 3:32:14 PM
 */
public interface OS_Container extends Documentable {

	List<OS_Element2> items();

	void add(OS_Element anElement);
}

//
//
//
