/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.nextgen;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.nextgen.composable.IComposable;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.gen_fn.GeneratedClass;

import java.util.HashSet;
import java.util.Set;

/**
 * Created 3/4/22 7:14 AM
 */
public class ClassDefinition {
	final ClassStatement      primary;
	final Set<ClassStatement> extended = new HashSet<ClassStatement>();
	ClassInvocation invocation;
	GeneratedClass  node;
	IComposable     composable;

	public ClassDefinition(final ClassStatement aPrimary) {
		primary = aPrimary;
	}

	public ClassDefinition(final @NotNull ClassInvocation aClassInvocation) {
		primary    = aClassInvocation.getKlass();
		invocation = aClassInvocation;
	}

	public ClassStatement getPrimary() {
		return primary;
	}

	public Set<ClassStatement> getExtended() {
		return extended;
	}

	public ClassInvocation getInvocation() {
		return invocation;
	}

	public void setInvocation(final ClassInvocation aInvocation) {
		invocation = aInvocation;
	}

	public GeneratedClass getNode() {
		return node;
	}

	public void setNode(final GeneratedClass aNode) {
		node = aNode;
	}

	public IComposable getComposable() {
		return composable;
	}

	public void setComposable(final IComposable aComposable) {
		composable = aComposable;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
