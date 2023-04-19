/**
 *
 */
package tripleo.elijah.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tripleo
 *
 * Created 	Mar 29, 2020 at 7:00:10 PM
 */
public class NameTable {

	Map<String, TypedElement> members = new HashMap<String, TypedElement>();

	public void add(final OS_Element element, final String name, final OS_Type dtype) {
//		element.setType(dtype);
		members.put(name, new TypedElement(element, dtype));
		tripleo.elijah.util.Stupidity.println_err_2("[NameTable#add] " + members);
	}

	class TypedElement {
		OS_Element element;
		OS_Type    type;
		public TypedElement(final OS_Element element2, final OS_Type dtype) {
			this.element = element2;
			this.type    = dtype;
		}

		@Override
		public String toString() {
			return "TypedElement{" +
					"element=" + element +
					", type=" + type +
					'}';
		}
	}

}

//
//
//
