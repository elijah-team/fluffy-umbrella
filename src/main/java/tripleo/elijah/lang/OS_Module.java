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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.entrypoints.MainClassEntryPoint;
import tripleo.elijah.gen.ICodeGen;
import tripleo.elijah.util.NotImplementedException;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class OS_Module implements OS_Element, OS_Container {

	private final Stack<Qualident> packageNames_q = new Stack<Qualident>();
	public @NotNull List<ModuleItem> items = new ArrayList<ModuleItem>();
	public @NotNull Attached _a = new Attached();
	public OS_Module prelude;

	public Compilation parent;
	private LibraryStatementPart lsp;
	private String _fileName;
	public @NotNull List<EntryPoint> entryPoints = new ArrayList<EntryPoint>();
	private IndexingStatement indexingStatement;

	public @org.jetbrains.annotations.Nullable OS_Element findClass(final String className) {
		for (final ModuleItem item : items) {
			if (item instanceof ClassStatement) {
				if (((ClassStatement) item).getName().equals(className))
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
			public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable final ModuleItem input) {
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
//		System.err.println("[OS_Module#modify_namespace] " + q + " " + aModification);
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
//		System.err.println("[OS_Module#modify_namespace] " + imp + " " + q + " " + aModification);
///*
//		getContext().add(imp, q); // TODO prolly wrong; do a second pass later to add definition...?
//*/
//	}

	@Override
	public void visitGen(final @NotNull ICodeGen visit) {
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
		find_multiple_items();
		//
		// FIND ALL ENTRY POINTS (should only be one per module)
		//
		XX.find_all_entry_points(this);
	}

	private void find_multiple_items() {
		Multimap<String, ModuleItem> items_map = ArrayListMultimap.create(items.size(), 1);
		for (final ModuleItem item : items) {
			if (!(item instanceof OS_Element2/* && item != anElement*/))
				continue;
			final String item_name = ((OS_Element2) item).name();
			items_map.put(item_name, item);
		}
		for (String key : items_map.keys()) {
			boolean warn = false;

			Collection<ModuleItem> moduleItems = items_map.get(key);
			if (moduleItems.size() < 2) // README really 1
				continue;

			Collection<ElObjectType> t = Collections2.transform(moduleItems, new Function<ModuleItem, ElObjectType>() {
				@Override
				public ElObjectType apply(@org.checkerframework.checker.nullness.qual.Nullable ModuleItem input) {
					assert input != null;
					return DecideElObjectType.getElObjectType(input);
				}
			});

			Set<ElObjectType> st = new HashSet<ElObjectType>(t);
			if (st.size() > 1)
				warn = true;
			if (moduleItems.size() > 1)
				if (moduleItems.iterator().next() instanceof NamespaceStatement && st.size() == 1)
					;
				else
					warn = true;

			//
			//
			//

			if (warn) {
				final String module_name = this.toString(); // TODO print module name or something
				final String s = String.format(
						"[Module#add] %s Already has a member by the name of %s",
						module_name, key);
				parent.getErrSink().reportWarning(s);
			}
		}
	}

	public void setContext(final ModuleContext mctx) {
		_a.setContext(mctx);
	}

	@Override
	public void addDocString(final Token s1) {
		throw new NotImplementedException();
	}

	public void remove(ClassStatement cls) {
		items.remove(cls);
	}

	public void addIndexingStatement(IndexingStatement indexingStatement) {
		this.indexingStatement = indexingStatement;
	}

	public boolean isPrelude() {
		return prelude == this;
	}

	public LibraryStatementPart getLsp() {
		return lsp;
	}

	public void setLsp(LibraryStatementPart aLsp) {
		lsp = aLsp;
	}

	public Compilation getCompilation() {
		return parent;
	}



	static class XX {
		private static boolean isMainClassEntryPoint(@NotNull ClassItem input) {
			final FunctionDef fd = (FunctionDef) input;
			return MainClassEntryPoint.is_main_function_with_no_args(fd);
		}

		private static void find_all_entry_points(@NotNull OS_Module aModule) {
			Consumer<ClassStatement> ccs = (x) -> aModule.entryPoints.add(new MainClassEntryPoint(x));

			aModule.items.stream()
					.filter(item -> item instanceof ClassStatement)
					.map(item -> (ClassStatement) item)
					.forEach(classStatement -> faep_002(classStatement, ccs));
		}

		/**
		 * If classStatement is a "main class", send to consumer
		 *
		 * @param classStatement
		 * @param ccs
		 */
		private static void faep_002(ClassStatement classStatement, Consumer<ClassStatement> ccs) {
			if (MainClassEntryPoint.isMainClass(classStatement)) {} else return;

			final Collection<ClassItem> x = classStatement.findFunction("main");
			final Stream<ClassItem> found = x.stream().filter(XX::isMainClassEntryPoint);

//			final int eps = aModule.entryPoints.size();

			found
					.map(classItem -> (ClassStatement) classItem.getParent())
					.forEach(ccs);

//			assert aModule.entryPoints.size() == eps || aModule.entryPoints.size() == eps+1; // TODO this will fail one day

//			System.out.println("243 " + entryPoints +" "+ _fileName);
//			break; // allow for "extend" class
		}
	}

}

//
//
//
