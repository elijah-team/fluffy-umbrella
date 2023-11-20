/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.gen_fn;

/**
 * Created 11/19/21 9:08 PM
 */
public interface GNCoded {
	enum Role {
		FUNCTION, CLASS, NAMESPACE
	}

	int getCode();
	Role getRole();
	void setCode(int aCode);
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
