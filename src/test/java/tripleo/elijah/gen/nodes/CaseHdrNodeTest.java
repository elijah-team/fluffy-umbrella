package tripleo.elijah.gen.nodes;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class CaseHdrNodeTest {
	
	@Test
	public void simpleGenText() {
		final @NotNull VariableReferenceNode3 vr = new VariableReferenceNode3("the", new ScopeNode(), null);
		final @NotNull CaseHdrNode chn = new CaseHdrNode(vr);
		final @NotNull String actual = chn.simpleGenText();
		Assert.assertEquals("vvthe", actual);
	}
	@Test
	public void setExpr() {
	}
}