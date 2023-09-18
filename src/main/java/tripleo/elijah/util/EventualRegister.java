package tripleo.elijah.util;

public interface EventualRegister {
	void checkFinishEventuals(); // TODO signature

	<P> void register(Eventual<P> e);
}
