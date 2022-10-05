/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.ci;

import antlr.Token;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.checkerframework.checker.nullness.qual.Nullable;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.StringExpression;
import tripleo.elijah.util.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created 9/6/20 11:20 AM
 */
public class CompilerInstructions {
	private IndexingStatement _idx;
	private GenerateStatement gen;
	public List<LibraryStatementPart> lsps = new ArrayList<LibraryStatementPart>();
	private String filename;
	private String name;

	public IndexingStatement indexingStatement() {
		if (_idx == null)
			_idx = new IndexingStatement(this);

		return _idx;
	}

	public void add(final GenerateStatement generateStatement) {
		assert gen == null;
		gen = generateStatement;
	}

	public void add(final LibraryStatementPart libraryStatementPart) {
		libraryStatementPart.setInstructions(this);
		lsps.add(libraryStatementPart);
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	@Nullable
	public String genLang() {
		Collection<GenerateStatement.Directive> gens = Collections2.filter(gen.dirs, new Predicate<GenerateStatement.Directive>() {
			@Override
			public boolean apply(GenerateStatement.@Nullable Directive input) {
				assert input != null;
				if (input.getName().equals("gen")) {
					return true;
				}
				return false;
			}
		});
		Iterator<GenerateStatement.Directive> gi = gens.iterator();
		if (!gi.hasNext()) return null;
		IExpression lang_raw = gi.next().getExpression();
		assert lang_raw instanceof StringExpression;
		return Helpers.remove_single_quotes_from_string(((StringExpression)lang_raw).getText());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(Token name) {
		this.name = name.getText();
	}
}

//
//
//
