package tripleo.elijah.util;

public final class Ok {
	static final Ok ok = new Ok();

	public static Ok instance() {
		return ok;
	}

	private Ok() {
	}
}
