package tripleo.elijah.lang;

import tripleo.elijah.gen.ICodeGen;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.TabbedOutputStream;

import java.io.IOException;

/*
 * Created on 5/3/2019 at 21:41
 *
 * $$Id$
 *
 */
public class OS_Package implements OS_Element {
	public static OS_Package default_package = new OS_Package(null, 0);
	private OS_Module _module;
	
	@Override
	public void print_osi(TabbedOutputStream aTos) throws IOException {
		throw new NotImplementedException();
	}
	
	@Override
	public void visitGen(ICodeGen visit) {
		throw new NotImplementedException();
	}

	// TODO packages, elements
	
	public OS_Package(Qualident aName, int aCode) {
		_code = aCode;
		_name = aName;
	}
	
	public OS_Package(Qualident aName, int aCode, OS_Module module) {
		_code = aCode;
		_name = aName;
		_module = module;
	}
	
	int _code;
	Qualident _name;

	@Override
	public OS_Element getParent() {
		return _module;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null; //_a._context;
	}

	
}
