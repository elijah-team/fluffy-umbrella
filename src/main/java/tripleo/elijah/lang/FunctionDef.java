/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/*
 * Created on Aug 30, 2005 8:43:27 PM
 * 
 * $Id$
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.gen.ICodeGen;

// TODO FunctionDef is not a Container is it?
public class FunctionDef extends BaseFunctionDef implements Documentable, ClassItem, OS_Container, OS_Element2 {

	private TypeName _returnType = null;

	public void setReturnType(final TypeName tn) {
		this._returnType = tn;
	}

	// region constructor

	private final OS_Element parent;

	public FunctionDef(final OS_Element element, final Context context) {
		parent = element;
		if (element instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else if (element instanceof PropertyStatement) {
			// do nothing
		} else {
			throw new IllegalStateException("adding FunctionDef to " + element.getClass().getName());
		}
		_a.setContext(new FunctionContext(context, this));
	}

	// endregion

	// region modifiers

	private FunctionModifiers _mod;

	public void set(final FunctionModifiers mod) {
		assert _mod == null;
		_mod = mod;
	}

	// endregion

	// region abstract

	private boolean _isAbstract;

	public void setAbstract(final boolean b) {
		_isAbstract = b;
		if (b) {this.set(FunctionModifiers.ABSTRACT);}
	}

	// endregion

	/**
	 * Can be {@code null} under the following circumstances:<br/><br/>
	 *
	 * 1. The compiler(parser) didn't get a chance to set it yet<br/>
	 * 2. The programmer did not specify a return value and the compiler must deduce it<br/>
	 * 3. The function is a void-type and specification isn't required <br/>
	 *
	 * @return the associated TypeName or NULL
	 */
	public @Nullable TypeName returnType() {
		return _returnType;
	}

	@Override
	public void visitGen(final ICodeGen visit) {
		visit.visitFunctionDef(this);
	}

	@Override
	public void postConstruct() { // TODO

	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return String.format("<Function %s %s %s>", parent, name(), getArgs());
	}

}

//
//
//
//