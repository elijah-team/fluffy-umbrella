/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.nextgen.outputtree;

import tripleo.elijah.DebugFlags;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;

import java.util.List;

/**
 * @author tripleo
 */
public class EOT_OutputTree {
	public List<EOT_OutputFile> list;

	public void set(final List<EOT_OutputFile> aOutputFileList) {
		list = aOutputFileList;
	}

	public void _putSeq(final String aKey, final EOT_OutputFile.FileNameProvider aPath, final EG_Statement aStatement) {
		if (DebugFlags.__EOT_OutputTree__putSeq) { // TODO FlowK, CompProgress, Ulf
			System.out.printf("[_putSeq] %s %s {{%s}}%n", aKey, aPath.getFilename(), aStatement.getExplanation().getText());
		}
	}
}
