package tripleo.elijah.comp;

import org.jetbrains.annotations.*;
import tripleo.elijah.ci.*;
import tripleo.elijah.lang.*;
import tripleo.elijah.nextgen.inputtree.*;
import tripleo.elijah.nextgen.query.*;
import tripleo.elijah.world.i.*;

import java.io.*;
import java.util.*;

public interface CompFactory {

	EIT_ModuleInput createModuleInput(OS_Module aModule);

	Qualident createQualident(List<String> sl);

	InputRequest createInputRequest(File aFile, final boolean aDo_out, final @Nullable LibraryStatementPart aLsp);

	WorldModule createWorldModule(OS_Module aM);

	class InputRequest {
		private final File                    _file;
		private final boolean                 _do_out;
		private final LibraryStatementPart    lsp;
		private       Operation2<WorldModule> op;

		public InputRequest(final File aFile, final boolean aDoOut, final @Nullable LibraryStatementPart aLsp) {
			_file   = aFile;
			_do_out = aDoOut;
			lsp     = aLsp;
		}

		public File file() {
			return _file;
		}

		public boolean do_out() {
			return _do_out;
		}

		public LibraryStatementPart lsp() {
			return lsp;
		}

		public void setOp(final Operation2<WorldModule> aOwm) {
			op = aOwm;
		}
	}
}
