/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tripleo.elijah.lang.DotExpression;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.util.Helpers;

import java.util.Stack;

public class DotExpressionToStackTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_dot_expression_to_stack() {
//		DeduceTypes2 d = new DeduceTypes2(null);
		//
		final IdentExpression c = Helpers.string_to_ident("c");
		final IdentExpression b = Helpers.string_to_ident("b");
		final IdentExpression a = Helpers.string_to_ident("a");
		//
		final DotExpression de2 = new DotExpression(b, c);
		final DotExpression de = new DotExpression(a, de2);
		//
		@NotNull final Stack<IExpression> s = DeduceLookupUtils.dot_expression_to_stack(de);
//		IExpression[] sa = (IExpression[]) s.toArray();
		Assert.assertEquals(a, s.pop());
		Assert.assertEquals(b, s.pop());
		Assert.assertEquals(c, s.pop());
	}

	@Test
	public void test_dot_expression_to_stack2() {
//		DeduceTypes2 dt2 = new DeduceTypes2(null);
		//
		final IdentExpression e = Helpers.string_to_ident("e");
		final IdentExpression d = Helpers.string_to_ident("d");
		final IdentExpression c = Helpers.string_to_ident("c");
		final IdentExpression b = Helpers.string_to_ident("b");
		final IdentExpression a = Helpers.string_to_ident("a");
		//
		final DotExpression de4 = new DotExpression(d, e);
		final DotExpression de3 = new DotExpression(c, de4);
		final DotExpression de2 = new DotExpression(b, de3);
		final DotExpression de = new DotExpression(a, de2);
		//
		@NotNull final Stack<IExpression> s = DeduceLookupUtils.dot_expression_to_stack(de);
//		IExpression[] sa = (IExpression[]) s.toArray();
		Assert.assertEquals(a, s.pop());
		Assert.assertEquals(b, s.pop());
		Assert.assertEquals(c, s.pop());
		Assert.assertEquals(d, s.pop());
		Assert.assertEquals(e, s.pop());
	}
}
