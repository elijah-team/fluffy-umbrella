/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.slir;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.lang.OS_Module;

/**
 * Created 11/6/21 8:27 AM
 */
public class SlirSourceFile {
	private final String filename;

	public SlirSourceFile(final String aFilename) {
		filename = aFilename;
	}

	public String getFilename() {
		return filename;
	}

	public SourceFileTarget getTarget() {
		return null; // TODO implement me
	}

	public enum SourceFileType {
		ELIJAH, EZ, CONFIG
	}

	public interface SourceFileTarget {
		SourceFileType getType();
		//String getFileName();
	}

	public class TargetSource implements SourceFileTarget {

		@Override
		public SourceFileType getType() {
			return SourceFileType.ELIJAH;
		}

		/**
		 * Where are we getting this from?
		 * @return
		 */
		public OS_Module getModule() {
			return null; // TODO implement me
		}
	}

	public class TargetEz implements SourceFileTarget {

		@Override
		public SourceFileType getType() {
			return SourceFileType.EZ;
		}

		public CompilerInstructions getInstructions() {
			return null; // TODO implement me
		}
	}

	/**
	 * This is not implemented yet
	 */
	public class TargetConfig implements SourceFileTarget {

		@Override
		public SourceFileType getType() {
			return SourceFileType.CONFIG;
		}

		public class ElijahConfig {}

		public ElijahConfig getConfig() {
			return null; // TODO implement me
		}
	}


}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
