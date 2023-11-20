package tripleo.util;

/**
 * https://github.com/beatty/jornado/blob/master/src/main/java/jornado/Base36.java
 *
 * @author john
 */
public class Base36 {
	private static final int RADIX = 36;

	public static String encode(final int value) {
		return Integer.toString(value, RADIX);
	}

	public static long decode(final String base36String) {
		return Integer.parseInt(base36String, RADIX);
	}
}
