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

import tripleo.elijah.nextgen.ClassDefinition;

/**
 * Created 3/4/22 7:24 AM
 */
public interface ClassExpectationMatch {
	// TODO do we need ClassSystem here?
	void onMatch(ClassDefinition aClassDefinition);
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//