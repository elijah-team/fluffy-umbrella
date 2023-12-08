package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

class _WP_FileNameProvider implements EOT_OutputFile.FileNameProvider {
	private final @NotNull String prefix1;
	private final @NotNull Path   path1;
	private final @NotNull String key1;
	private final @NotNull File   file_prefix;

	public _WP_FileNameProvider(final String aKey, final @NotNull File aFile_prefix) {
		key1        = aKey;
		file_prefix = aFile_prefix;
		prefix1     = file_prefix.toString();
		path1       = FileSystems.getDefault().getPath(prefix1, key1);
	}

	public String getPrefix1() {
		return prefix1;
	}

	public String getKey1() {
		return key1;
	}

	public File getFile_prefix() {
		return file_prefix;
	}

	@Override
	public String getFilename() {
		return path1.toString();
	}

	public Path getPath() {
		return path1;
	}
}
