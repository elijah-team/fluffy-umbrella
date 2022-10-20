/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.slir;

import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Package;

/**
 * Created 11/10/21 2:17 AM
 */
public class SlirPackageNode implements SlirElement {
	private final SlirSourceNode slirSourceNode;
	private final OS_Package packageStatement;

	public SlirPackageNode(final SlirSourceNode aSlirSourceNode, final OS_Package aPackageStatement) {
		slirSourceNode = aSlirSourceNode;
		packageStatement = aPackageStatement;
	}

	@Override
	public String name() {
		return packageStatement.getName();
	}

	@Override
	public SlirPos partOfSpeech() {
		return SlirPos.PACKAGE;
	}

	@Override
	public OS_Element element() {
		return null; //packageStatement; // TODO OS_Package is not an Element!!
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
