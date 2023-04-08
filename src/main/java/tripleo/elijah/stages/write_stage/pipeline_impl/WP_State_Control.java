package tripleo.elijah.stages.write_stage.pipeline_impl;

interface WP_State_Control {
	void exception(final Exception e);

	void clear();

	boolean hasException();

	Exception getException();
}
