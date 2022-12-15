/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 2/28/21 3:23 AM
 */
public interface GeneratedContainer extends GeneratedNode {
    OS_Element getElement();

    @Nullable VarTableEntry getVariable(String aVarName);

    public class VarTableEntry {
        public final VariableStatement vs;
        public final @NotNull IdentExpression nameToken;
        public final IExpression initialValue;
        private final @NotNull OS_Element parent;
        TypeName typeName;
        public OS_Type varType;
        @NotNull List<TypeTableEntry> potentialTypes = new ArrayList<TypeTableEntry>();
        private GeneratedNode _resolvedType;

        public VarTableEntry(final VariableStatement aVs,
                             final @NotNull IdentExpression aNameToken,
                             final IExpression aInitialValue,
                             final @NotNull TypeName aTypeName,
                             final @NotNull OS_Element aElement) {
            vs              = aVs;
            nameToken       = aNameToken;
            initialValue    = aInitialValue;
            typeName        = aTypeName;
            varType         = new OS_Type(typeName);
            parent          = aElement;
        }

        public void addPotentialTypes(@NotNull Collection<TypeTableEntry> aPotentialTypes) {
            potentialTypes.addAll(aPotentialTypes);
        }

        public void resolve(@NotNull GeneratedNode aResolvedType) {
            _resolvedType = aResolvedType;
        }

        public @Nullable GeneratedNode resolvedType() {
            return _resolvedType;
        }

        public @NotNull OS_Element getParent() {
            return parent;
        }

        public void connect(final VariableTableEntry aVte, final GeneratedConstructor aConstructor) {
            connectionPairs.add(new ConnectionPair(aVte, aConstructor));
        }

        public @NotNull List<ConnectionPair> connectionPairs = new ArrayList<>();

        public static class ConnectionPair {
            public final VariableTableEntry vte;
            final GeneratedConstructor constructor;

            public ConnectionPair(final VariableTableEntry aVte, final GeneratedConstructor aConstructor) {
                vte = aVte;
                constructor = aConstructor;
            }
        }
    }
}

//
//
//
