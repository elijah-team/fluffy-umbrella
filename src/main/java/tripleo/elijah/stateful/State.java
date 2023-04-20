package tripleo.elijah.stateful;

public interface State {
	void apply(DefaultStateful element);

	void setIdentity(int aId);

	boolean checkState(DefaultStateful aElement3);
}
