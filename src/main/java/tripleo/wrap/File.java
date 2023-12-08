package tripleo.wrap;

import com.google.common.io.Files;
import tripleo.elijah.comp.IO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class File {
	java.io.File wrap;
	public File(final String aBase, final String aName) {
		wrap = new java.io.File(aBase, aName);
	}

	public File(final String aBase) {
		wrap = new java.io.File(aBase);
	}

	public File(final java.io.File aDirectory, final String aFileName) {
		wrap = new java.io.File(aDirectory, aFileName);
	}

	public File(final java.io.File aFile) {
		wrap = aFile;
	}

	public File(final File aDirectory, final String aFileName) {
		wrap = new java.io.File(aDirectory.wrapped(), aFileName);
	}

	public static File wrap(final java.io.File aFile) {
		return new File(aFile);
	}

	public static List<String> readLines(final String aFilename, final Charset aCharset) throws IOException {
		return Files.readLines(new java.io.File(aFilename), aCharset);
	}

	public boolean exists() {
		return wrap.exists();
	}

	public String getName() {
		return wrap.getName();
	}

	public java.io.File getCanonicalFile() throws IOException {
		return wrap.getCanonicalFile();
	}

	public String[] list(final FilenameFilter aFilter) {
		return wrap.list(aFilter);
	}

	public String getAbsolutePath() {
		return wrap.getAbsolutePath();
	}

	public InputStream readFile(final IO aIo) throws FileNotFoundException {
		return aIo.readFile(this);
	}

	public FileInputStream getFileInputStream() throws FileNotFoundException {
		return new FileInputStream(wrap);
	}

	public boolean isDirectory() {
		return wrap.isDirectory();
	}

	public java.io.File wrapped() {
		return this.wrap;
	}

	public long length() {
		return wrap.length();
	}

	public boolean mkdirs() {
		return wrap.mkdirs();
	}

	public File getParentFile() {
		return new File(wrap.getParentFile());
	}

	public File[] listFiles(final FilenameFilter aFilter) {
		// FIXME 11/27 Get some help with this one
		java.io.File[] r = wrap.listFiles(aFilter);
		File[] R = new File[r.length];
		for (int i = 0; i < r.length; i++) {
			final java.io.File file = r[i];
			R[i] = new File(file);
		}
		return R;
	}

	@Override
	public String toString() {
		return wrap.toString();
	}
}
