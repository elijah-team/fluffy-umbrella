/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.gen_fn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.util.Maybe;
import tripleo.elijah.util.NotImplementedException;

/**
 * Created 2/28/21 3:23 AM
 */
public interface EvaContainer extends EvaNode {
	class VarTableEntry {
		public static class ConnectionPair {
			public final VariableTableEntry vte;
			final        EvaConstructor     constructor;

			public ConnectionPair(final VariableTableEntry aVte, final EvaConstructor aConstructor) {
				vte         = aVte;
				constructor = aConstructor;
			}
		}
		public interface UpdatePotentialTypesCB {
			void call(final @NotNull EvaContainer aGeneratedContainer);
		}
		public final  VariableStatement                                  vs;
		public final  IdentExpression                                    nameToken;
		public final  IExpression                                        initialValue;
		public final  DeferredObject<UpdatePotentialTypesCB, Void, Void> updatePotentialTypesCBPromise = new DeferredObject<>();
		public final List<ConnectionPair> connectionPairs               = new ArrayList<>();
		public final TypeName             typeName;
		public final List<TypeTableEntry> potentialTypes = new ArrayList<TypeTableEntry>();
		private final OS_Element           parent;
		public        OS_Type                                            varType;

		UpdatePotentialTypesCB updatePotentialTypesCB;

		private EvaNode _resolvedType;

		public VarTableEntry(final VariableStatement aVs,
		                     final @NotNull IdentExpression aNameToken,
		                     final IExpression aInitialValue,
		                     final @NotNull TypeName aTypeName,
		                     final @NotNull OS_Element aElement) {
			vs           = aVs;
			nameToken    = aNameToken;
			initialValue = aInitialValue;
			typeName     = aTypeName;
			varType      = new OS_UserType(typeName);
			parent       = aElement;
		}

		public void addPotentialTypes(@NotNull final Collection<TypeTableEntry> aPotentialTypes) {
			potentialTypes.addAll(aPotentialTypes);
		}

		public void connect(final VariableTableEntry aVte, final EvaConstructor aConstructor) {
			connectionPairs.add(new ConnectionPair(aVte, aConstructor));
		}

		public @NotNull OS_Element getParent() {
			return parent;
		}

		public boolean isResolved() {
			return nameToken.hasResolvedElement(); // ??
		}

		public void resolve(@NotNull final EvaNode aResolvedType) {
			System.out.printf("** [GeneratedContainer 56] resolving VarTableEntry %s to %s%n", nameToken, aResolvedType.identityString());
			_resolvedType = aResolvedType;
		}

		public void resolve_varType(final OS_Type aOSType) {
			throw new NotImplementedException();

		}

		public void resolve_varType_cb(final Consumer<GenType> a) {
			throw new NotImplementedException();
		}

		public @Nullable EvaNode resolvedType() {
			return _resolvedType;
		}

		public void updatePotentialTypes(final @NotNull EvaContainer aGeneratedContainer) {
//			assert aGeneratedContainer == GeneratedContainer.this;
			updatePotentialTypesCBPromise.then(result -> result.call(aGeneratedContainer));
		}
	}

	OS_Element getElement();

	@Nullable Maybe<VarTableEntry> getVariable(String aVarName);
}

//
//
//
