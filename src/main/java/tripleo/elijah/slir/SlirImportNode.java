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

import tripleo.elijah.lang.ImportStatement;
import tripleo.elijah.lang.OS_Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/6/21 8:34 AM
 */
public class SlirImportNode implements SlirElement {
	private final SlirSourceNode sourceNode;
	private final ImportStatement importStatement;
	private final List<SlirElement> imported = new ArrayList<SlirElement>();

	public SlirImportNode(final SlirSourceNode aSlirSourceNode, final ImportStatement aImportStatement) {
		sourceNode = aSlirSourceNode;
		importStatement = aImportStatement;
	}

	public void markImported(final SlirElement aElement) {
		imported.add(aElement);
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public SlirPos partOfSpeech() {
		return SlirPos.IMPORT;
	}

	@Override
	public OS_Element element() {
		return importStatement;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
