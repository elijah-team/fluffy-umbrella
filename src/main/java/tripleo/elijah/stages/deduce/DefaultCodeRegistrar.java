/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

/**
 * Created 11/28/21 4:52 PM
 */
public class DefaultCodeRegistrar implements ICodeRegistrar {
	private final Compilation compilation;

	public DefaultCodeRegistrar(final Compilation aCompilation) {
		compilation = aCompilation;
	}

	@Override
	public void registerNamespace(final GeneratedNamespace aNamespace) {
		final int code = compilation.nextClassCode();
		aNamespace.setCode(code);
	}

	@Override
	public void registerClass(final GeneratedClass aClass) {
		final int code = compilation.nextClassCode();
		aClass.setCode(code);
	}

	@Override
	public void registerFunction(final BaseGeneratedFunction aFunction) {
		final int code = compilation.nextFunctionCode();
		aFunction.setCode(code);
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
