/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.AnnotationPart;
import tripleo.elijah.lang.AnnotationWalker;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.StringExpression;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.util.Helpers;

import java.util.ArrayList;

/**
 * Created 12/24/20 7:42 AM
 */
public class CClassDecl {
	private final GeneratedClass generatedClass;
	public String prim_decl;
	public boolean prim = false;

	public CClassDecl(GeneratedClass generatedClass) {
		this.generatedClass = generatedClass;
	}

	public void setDecl(String str) {
		prim_decl = str;
	}

	public void setPrimitive() {
		prim = true;
	}

	public void evaluatePrimitive() {
		ClassStatement xx = generatedClass.getKlass();
		xx.walkAnnotations(new AnnotationWalker() {
			@Override
			public void annotation(AnnotationPart anno) {
				if (anno.annoClass().equals(Helpers.string_to_qualident("C.repr"))) {
					if (anno.getExprs() != null) {
						final ArrayList<IExpression> expressions = new ArrayList<IExpression>(anno.getExprs().expressions());
						final IExpression str0 = expressions.get(0);
						if (str0 instanceof StringExpression) {
							final String str = ((StringExpression) str0).getText();
							setDecl(str);
						} else {
							System.out.println("Illegal C.repr");
						}
					}
				}
				if (anno.annoClass().equals(Helpers.string_to_qualident("Primitive")))
					setPrimitive();
			}
		});
	}
}

//
//
//
