/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

public enum ExpressionKind {

	ASSIGNMENT,

	ADDITION, SUBTRACTION, MULTIPLY, DIVIDE,
	AUG_PLUS, AUG_MINUS, AUG_MULT, AUG_DIV,

	AUG_MOD,

	AUG_SR, AUG_BSR,
	AUG_SL, // TODO missing AUG_BSL
	AUG_BAND,
	AUG_BXOR,
	AUG_BOR,

	INCREMENT, DECREMENT,
	POST_INCREMENT, POST_DECREMENT,

	NEG, POS,

	BNOT, LNOT,

	MODULO,

	PROCEDURE_CALL,

	VARREF, QIDENT, IDENT,

	STRING_LITERAL, CHAR_LITERAL,
	NUMERIC, FLOAT,

	BAND, BXOR, BOR,
	LAND, LOR,

	NOT_EQUAL, EQUAL, LT_, GT, LE, GE,

	BSHIFTR, LSHIFT, RSHIFT, // TODO missing BSHIFTL

	DOT_EXP, /*INDEX_OF,*/ GET_ITEM, SET_ITEM, FUNC_EXPR, TO_EXPR,

	CAST_TO, AS_CAST, SUBEXPRESSION, IS_A

}

//
//
//
