/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.util.buffer.Buffer;

/**
 * Created 4/27/21 1:12 AM
 */
public class GenerateResultItem {
	public final int counter;
	public final GenerateResult.TY ty;
	public final Buffer buffer;
	public final GeneratedNode node;
	public String output;

	public GenerateResultItem(GenerateResult.TY aTy, Buffer aBuffer, GeneratedNode aNode, int aCounter) {
		ty = aTy;
		buffer = aBuffer;
		node = aNode;
		counter = aCounter;
	}
}

//
//
//
