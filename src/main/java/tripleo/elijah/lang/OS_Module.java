/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/*
 * Created on Sep 1, 2005 8:16:32 PM
 *
 * $Id$
 *
 */
package tripleo.elijah.lang;

import antlr.Token;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyModule;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class OS_Module implements OS_Element, OS_Container {

	public final @NotNull                      List<ModuleItem>     items          = new ArrayList<ModuleItem>();
	public final @NotNull                      Attached             _a             = new Attached();
	public final @NotNull                      EntryPointList       entryPoints    = new EntryPointList();
	private final                              Stack<Qualident>     packageNames_q = new Stack<Qualident>();
	public @org.jetbrains.annotations.Nullable OS_Module            prelude;
	public                                     Compilation          parent;
	private                                    LibraryStatementPart lsp;
	private                                    String               _fileName;
	private                                    IndexingStatement    indexingStatement;

	public @org.jetbrains.annotations.Nullable OS_Element findClass(final String aClassName) {
		for (final ModuleItem item : items) {
			if (item instanceof ClassStatement) {
				if (((ClassStatement) item).getName().equals(aClassName))
					return item;
			}
		}
		return null;
	}

	public void finish() {
//		parent.put_module(_fileName, this);
	}

	public String getFileName() {
		return _fileName;
	}

	public void setFileName(final String fileName) {
		this._fileName = fileName;
	}

	public @NotNull Collection<ModuleItem> getItems() {
		return items;
	}

	public boolean hasClass(final String className) {
		for (final ModuleItem item : items) {
			if (item instanceof ClassStatement) {
				if (((ClassStatement) item).getName().equals(className))
					return true;
			}
		}
		return false;
	}

	@Override // OS_Container
	public @NotNull List<OS_Element2> items() {
		final Collection<ModuleItem> c = Collections2.filter(getItems(), new Predicate<ModuleItem>() {
			@Override
			public boolean apply(@org.jetbrains.annotations.Nullable final ModuleItem input) {
				final boolean b = input instanceof OS_Element2;
				return b;
			}
		});
		final ArrayList<OS_Element2> a = new ArrayList<OS_Element2>();
		for (final ModuleItem moduleItem : c) {
			a.add((OS_Element2) moduleItem);
		}
		return a;
	}

	@Override
	public void add(final OS_Element anElement) {
		if (!(anElement instanceof ModuleItem)) {
			parent.getErrSink().info(String.format(
					"[Module#add] not adding %s to OS_Module", anElement.getClass().getName()));
			return; // TODO FalseAddDiagnostic
		}
		items.add((ModuleItem) anElement);
	}

//	public void modify_namespace(Qualident q, NamespaceModify aModification) { // TODO aModification is unused
////		NotImplementedException.raise();
//		tripleo.elijah.util.Stupidity.println_err2("[OS_Module#modify_namespace] " + q + " " + aModification);
//		//
//		// DON'T MODIFY  NAMETABLE
//		//
///*
//		getContext().add(null, q);
//*/
//	}
//
//	public void modify_namespace(ImportStatement imp, Qualident q, NamespaceModify aModification) { // TODO aModification is unused
////		NotImplementedException.raise();
//		tripleo.elijah.util.Stupidity.println_err2("[OS_Module#modify_namespace] " + imp + " " + q + " " + aModification);
///*
//		getContext().add(imp, q); // TODO prolly wrong; do a second pass later to add definition...?
//*/
//	}

	@Override
	public void visitGen(final @NotNull ElElementVisitor visit) {
		visit.addModule(this); // visitModule
	}

	/**
	 * A module has no parent which is an element (not even a package - this is not Java).<br>
	 * If you want the Compilation use the member {@link #parent}
	 *
	 * @return null
	 */
	/**
	 * @ ensures \result == null
	 */
	@Override
	public @org.jetbrains.annotations.Nullable OS_Element getParent() {
		return null;
	}

	public void setParent(@NotNull final Compilation parent) {
		this.parent = parent;
	}

	@Override
	public Context getContext() {
		return _a._context;
	}

	/**
	 * The last package declared in the source file
	 *
	 * @return a new OS_Package instance or default_package
	 */
	@NotNull public OS_Package pullPackageName() {
		if (packageNames_q.empty())
			return OS_Package.default_package;
		return parent.makePackage(packageNames_q.peek());
	}

	public void pushPackageName(final Qualident xyz) {
		packageNames_q.push(xyz);
	}

	/**
	 * Get a class by name. Must not be qualified. Wont return a {@link NamespaceStatement}
	 * Same as {@link #findClass(String)}
	 *
	 * @param name the class we are looking for
	 * @return either the class or null
	 */
	@Nullable
	public ClassStatement getClassByName(final String name) {
		for (final ModuleItem item : items) {
			if (item instanceof ClassStatement)
				if (((ClassStatement) item).getName().equals(name))
					return (ClassStatement) item;
		}
		return null;
	}

	public void postConstruct() {
		final FluffyComp fc = getContext().module().getCompilation().getFluffy();

		final FluffyModule fm = fc.module(this);

		fm.find_multiple_items(fc);
		fm.find_all_entry_points();
	}

	public void setContext(final ModuleContext mctx) {
		_a.setContext(mctx);
	}

	@Override
	public void addDocString(final Token s1) {
		throw new NotImplementedException();
	}

	public void remove(final ClassStatement cls) {
		items.remove(cls);
	}

	public void addIndexingStatement(final IndexingStatement indexingStatement) {
		this.indexingStatement = indexingStatement;
	}

	public boolean isPrelude() {
		return prelude == this;
	}

	public LibraryStatementPart getLsp() {
		return lsp;
	}

	public void setLsp(final @NotNull LibraryStatementPart aLsp) {
		lsp = aLsp;
	}

	public Compilation getCompilation() {
		return parent;
	}
}

//
//
//
