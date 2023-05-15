/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tripleo.elijah.nextgen.outputtree;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;

import java.nio.file.Path;
import java.util.List;

/**
 * @author olu
 */
public class EOT_OutputTree {
	private List<EOT_OutputFile> list;

	public void set(final List<EOT_OutputFile> aLeof) {
		list = aLeof;
	}

	public void _putSeq(final String aKey, final Path aPath, final @NotNull EG_Statement aStatement) {
		System.err.printf("[_putSeq] %s %s %s%n", aKey, aPath, aStatement.getText());
	}

	public void add(final @NotNull EOT_OutputFile aOff) {
		System.err.printf("[add] %s %s%n", aOff.getFilename(), aOff.getStatementSequence().getText());

		list.add(aOff);
	}

	public List<EOT_OutputFile> getList() {
		return list;
	}
}
