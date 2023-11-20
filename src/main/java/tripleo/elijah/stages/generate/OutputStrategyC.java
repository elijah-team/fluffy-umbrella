/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.generate;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.DecideElObjectType;
import tripleo.elijah.lang.ElObjectType;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.NamespaceTypes;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.io.File;

/**
 * Created 1/13/21 5:54 AM
 */
public class OutputStrategyC {
	private final OutputStrategy outputStrategy;

	public OutputStrategyC(final OutputStrategy outputStrategy) {
		this.outputStrategy = outputStrategy;
	}

	public String nameForNamespace(final GeneratedNamespace generatedNamespace, final GenerateResult.TY aTy) {
		if (generatedNamespace.module().isPrelude()) {
			// We are dealing with the Prelude
			final StringBuilder sb = new StringBuilder();
			sb.append("/Prelude/");
			sb.append("Prelude");
			appendExtension(aTy, sb);
			return sb.toString();
		}
		String filename;
		if (generatedNamespace.getNamespaceStatement().getKind() == NamespaceTypes.MODULE) {
			final String moduleFileName = generatedNamespace.module().getFileName();
			final File moduleFile = new File(moduleFileName);
			filename = moduleFile.getName();
			filename = strip_elijah_extension(filename);
		} else
			filename = generatedNamespace.getName();
		final StringBuilder sb = new StringBuilder();
		sb.append("/");
		final LibraryStatementPart lsp = generatedNamespace.module().getLsp();
		if (lsp == null)
			sb.append("___________________");
		else
			sb.append(lsp.getInstructions().getName());
		sb.append("/");
		OS_Package pkg = generatedNamespace.getNamespaceStatement().getPackageName();
		if (pkg != OS_Package.default_package) {
			if (pkg == null)
				pkg = findPackage(generatedNamespace.getNamespaceStatement());
			sb.append(pkg.getName());
			sb.append("/");
		}
		sb.append(filename);
		appendExtension(aTy, sb);
		return sb.toString();
	}

	private OS_Package findPackage(OS_Element e) {
		while (e != null) {
			e = e.getParent();
			if (e.getContext().getParent() == e.getContext())
				e = null;
			else {
				@NotNull final ElObjectType t = DecideElObjectType.getElObjectType(e);
				switch (t) {
					case NAMESPACE:
						if (((NamespaceStatement) e).getPackageName() != null)
							return ((NamespaceStatement) e).getPackageName();
						break;
					case CLASS:
						if (((ClassStatement) e).getPackageName() != null)
							return ((ClassStatement) e).getPackageName();
						break;
					case FUNCTION:
						continue;
					default:
						// datatype, enum, alias
						continue;
				}
			}
		}
		return null;
	}

	String strip_elijah_extension(String aFilename) {
		if (aFilename.endsWith(".elijah")) {
			aFilename = aFilename.substring(0, aFilename.length()-7);
		} else if (aFilename.endsWith(".elijjah")) {
			aFilename = aFilename.substring(0, aFilename.length()-8);
		}
		return aFilename;
	}

	public String nameForFunction(final @NotNull GeneratedFunction generatedFunction, final GenerateResult.TY aTy) {
		GeneratedNode c = generatedFunction.getGenClass();
		if (c == null) c = generatedFunction.getParent(); // TODO fixme
		if (c instanceof GeneratedClass)
			return nameForClass((GeneratedClass) c, aTy);
		else if (c instanceof GeneratedNamespace)
			return nameForNamespace((GeneratedNamespace) c, aTy);
		return null;
	}

	public @NotNull String nameForClass(final GeneratedClass generatedClass, final GenerateResult.TY aTy) {
		if (generatedClass.module().isPrelude()) {
			// We are dealing with the Prelude
			final @NotNull StringBuilder sb = new StringBuilder();
			sb.append("/Prelude/");
			sb.append("Prelude");
			appendExtension(aTy, sb);
			return sb.toString();
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("/");
		final LibraryStatementPart lsp = generatedClass.module().getLsp();
		if (lsp == null)
			sb.append("______________");
		else
//			sb.append(generatedClass.module.lsp.getName());
			sb.append(lsp.getInstructions().getName());
		sb.append("/");
		OS_Package pkg = generatedClass.getKlass().getPackageName();
		if (pkg != OS_Package.default_package) {
			if (pkg == null)
				pkg = findPackage(generatedClass.getKlass());
			sb.append(pkg.getName());
			sb.append("/");
		}
		switch (outputStrategy.per()) {
		case PER_CLASS:
			{
				if (generatedClass.isGeneric())
					sb.append(generatedClass.getNumberedName());
				else
					sb.append(generatedClass.getName());
			}
			break;
		case PER_MODULE:
			{
//					mod = generatedClass.getKlass().getContext().module();
				final OS_Module mod = generatedClass.module();
				final File f = new File(mod.getFileName());
				String ff = f.getName();
				final int y=2;
				ff = strip_elijah_extension(ff);
				sb.append(ff);
//					sb.append('/');
			}
			break;
		case PER_PACKAGE:
			{
				final OS_Package pkg2 = generatedClass.getKlass().getPackageName();
				final String pkgName;
				if (pkg2 != OS_Package.default_package) {
					pkgName = "$default_package";
				} else
					pkgName = pkg2.getName();
				sb.append(pkgName);
//					sb.append('/');
			}
			break;
		case PER_PROGRAM:
			{
				final CompilerInstructions xx = lsp.getInstructions();
				final String n = xx.getName();
				sb.append(n);
//					sb.append('/');
			}
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + outputStrategy.per());
		}
		appendExtension(aTy, sb);
		return sb.toString();
	}

	public void appendExtension(final GenerateResult.TY aTy, final StringBuilder aSb) {
		switch (aTy) {
			case IMPL:
				aSb.append(".c");
				break;
			case PRIVATE_HEADER:
				aSb.append("_Priv.h");
			case HEADER:
				aSb.append(".h");
				break;
		}
	}

	public String nameForConstructor(final GeneratedConstructor generatedConstructor, final GenerateResult.@NotNull TY aTy) {
		GeneratedNode c = generatedConstructor.getGenClass();
		if (c == null) c = generatedConstructor.getParent(); // TODO fixme
		if (c instanceof GeneratedClass)
			return nameForClass((GeneratedClass) c, aTy);
		else if (c instanceof GeneratedNamespace)
			return nameForNamespace((GeneratedNamespace) c, aTy);
		return null;
	}
}

//
//
//
