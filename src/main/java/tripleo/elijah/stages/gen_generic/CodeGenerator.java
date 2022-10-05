/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;

/**
 * Created 4/26/21 11:22 PM
 */
public interface CodeGenerator {
	void generate_namespace(GeneratedNamespace aGeneratedNamespace, GenerateResult aGenerateResult);

	void generate_class(GeneratedClass aGeneratedClass, GenerateResult aGenerateResult);
}

//
//
//
