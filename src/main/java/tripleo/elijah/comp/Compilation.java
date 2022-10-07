package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface Compilation {
	static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	void feedCmdLine(@NotNull List<String> args) throws Exception;

	void feedCmdLine(String @NotNull [] args) throws Exception /* finally!! */;

	IO getIO();

	void setIO(IO io);

	List<ClassStatement> findClass(String aClassName);

	int errorCount();

	boolean findStdLib(String prelude_name);

	void addModule(OS_Module module, String fn);

	int nextClassCode();

	int nextFunctionCode();

	boolean isPackage(String pkg);

	OS_Package getPackage(Qualident pkg_name);

	OS_Package makePackage(Qualident pkg_name);

	int compilationNumber();

	String getCompilationNumberString();

	ErrSink getErrSink();

	void addFunctionMapHook(@NotNull FunctionMapHook aFunctionMapHook);

	@NotNull EOT_OutputTree getOutputTree();
}
