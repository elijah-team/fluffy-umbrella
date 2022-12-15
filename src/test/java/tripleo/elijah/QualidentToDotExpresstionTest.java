package tripleo.elijah;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.util.Helpers;

public class QualidentToDotExpresstionTest {

    @Test
    public void qualidentToDotExpression2() {
        final @NotNull Qualident q = new Qualident();
        q.append(tripleo.elijah.util.Helpers.string_to_ident("a"));
        q.append(tripleo.elijah.util.Helpers.string_to_ident("b"));
        q.append(tripleo.elijah.util.Helpers.string_to_ident("c"));
        final @Nullable IExpression e = Helpers.qualidentToDotExpression2(q);
        System.out.println(e);
        Assert.assertEquals("a.b.c", e.toString());
    }
}