package tripleo.elijah.util;

import org.jetbrains.annotations.Contract;

public class Stupidity {
	@Contract(pure = true)
	public static void println_err(final String aS) {
//		tripleo.elijah.util.Stupidity.println_err_2(aS);
	}

	@Contract(pure = true)
	public static void println_out(final String aS) {
//		System.out.println(aS);
	}

	@Contract(pure = true)
	public static void println2(final Object aS) {
//		System.out.println(""+aS);
	}

	@Contract(pure = true)
	public static void println_err2(final Object aS) {
//		tripleo.elijah.util.Stupidity.println_err_2(""+aS);
	}

	public static void println_err_2(final Object aS) {
		System.err.println(""+aS);
	}

	@Contract(pure = true)
	public static void println_out_2(final Object aS) {
		//System.out.println(""+aS);
	}
}
