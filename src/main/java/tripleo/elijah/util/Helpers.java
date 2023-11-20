/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.util;

import antlr.CommonToken;
import antlr.Token;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.DotExpression;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.Qualident;
import tripleo.elijjah.ElijjahTokenTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;

/**
 * Created 9/10/20 3:44 PM
 */
public class Helpers {
	public static void printXML(final Object obj, @NotNull final TabbedOutputStream tos) {
		final XStream x = new XStream();
		//x.setMode(XStream.ID_REFERENCES);
		x.toXML(obj, tos.getStream());
	}

	@NotNull
	public static <E> List<E> List_of(@NotNull final E... e1) {
		final List<E> r = new ArrayList<E>();
		for (final E e : e1) {
			r.add(e);
		}
		return r;
	}

	public static IExpression qualidentToDotExpression2(@NotNull final Qualident q) {
		return qualidentToDotExpression2(q.parts(), 1);
	}

	public static IExpression qualidentToDotExpression2(@NotNull final List<IdentExpression> ts) {
		return qualidentToDotExpression2(ts, 1);
	}

	public static IExpression qualidentToDotExpression2(@NotNull final List<IdentExpression> ts, int i) {
		if (ts.size() == 1) return ts.get(0);
		if (ts.size() == 0) return null;
		IExpression r = ts.get(0);
//		int i=1;
		while (ts.size() > i) {
			final IExpression dotExpression = qualidentToDotExpression2(ts.subList(i++, ts.size()), i+1);
			if (dotExpression == null) break;
//			r.setRight(dotExpression);
			r = new DotExpression(r, dotExpression);
		}
		return r;
	}

	public static Token makeToken(final String aText) {
		final CommonToken t = new CommonToken();
		t.setText(aText);
		return t;
	}

	@NotNull
	public static IdentExpression string_to_ident(final String txt) {
		final CommonToken t = new CommonToken(ElijjahTokenTypes.IDENT, txt);
		return new IdentExpression(t);
	}

	@NotNull
	public static String remove_single_quotes_from_string(final String s) {
		return s.substring(1, s.length()-1);
	}

	public static String String_join(final String separator, final Iterable<String> stringIterable) {
		if (false) {
			final StringBuilder sb = new StringBuilder();

			for (final String part : stringIterable) {
				sb.append(part);
				sb.append(separator);
			}
			final String ss = sb.toString();
			final String substring = separator.substring(0, ss.length() - separator.length());
			return substring;
		}
		// since Java 1.8
		return String.join(separator, stringIterable);
	}

	public static Qualident string_to_qualident(final String x) {
		final Qualident q = new Qualident();
		for (final String xx : x.split("\\.")) {
			q.append(string_to_ident(xx));
		}
		return q;
	}

	public static String getHash(final byte[] aBytes) throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("SHA-256");

//		String input;
//		md.update(input.getBytes(StandardCharsets.UTF_8));
		md.update(aBytes);

		final byte[] hashBytes = md.digest();

		final StringBuilder sb = new StringBuilder();
		for (final byte b : hashBytes) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	public static String getHashForFilename(final String aFilename, final ErrSink aErrSink) throws IOException {
		final String hdigest = new DigestUtils(SHA_256).digestAsHex(new File(aFilename));
		return hdigest;
	}

	@Nullable
	public static String getHashForFilenameJava(final String aFilename, final ErrSink aErrSink) throws IOException {
		final File file = new File(aFilename);
		final long size = file.length();
		final byte[] ba = new byte[(int)size];  // README Counting on reasonable sizes here
		FileInputStream bb = null;
		try {
			bb = new FileInputStream(file);
			bb.read(ba);

			try {
				final String hh = getHash(ba);
				return hh;
			} catch (final NoSuchAlgorithmException aE) {
				aErrSink.exception(aE);
//				aE.printStackTrace();
			}
		} finally {
			if (bb != null)
				bb.close();
		}
		return null;
	}
}

//
//
//
