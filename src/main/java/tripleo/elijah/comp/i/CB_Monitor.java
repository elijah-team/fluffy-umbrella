package tripleo.elijah.comp.i;

import tripleo.elijah.comp.internal.CB_Output;

public interface CB_Monitor {
	void reportFailure(CB_Action aCBAction, CB_Output aCB_output);

	void reportSuccess(CB_Action aCBAction, CB_Output aCB_output);
}
