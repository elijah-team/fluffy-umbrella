package tripleo.elijah.lang.nextgen.names.i;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.IdentExpression;

import java.util.LinkedList;
import java.util.List;

public interface EN_Name {
	static void assertUnderstanding(@NotNull EN_Name aName, EN_Understanding u) {
		aName.addUnderstanding(u);

	}

	static void assertUnderstanding(@NotNull IdentExpression aIdentExpression, final EN_Understanding u) {
		aIdentExpression.getName().addUnderstanding(u);
	}

	@Contract(value = "_ -> new", pure = true)
	static @NotNull EN_Name create(@NotNull String name) {
		return new EN_Name() {
			private @NotNull List<EN_Usage> usages = new LinkedList<>();
			private @NotNull List<EN_Understanding> understandings = new LinkedList<>();
			private @NotNull DeferredObject<EN_Type, Void, Void> typePromise = new DeferredObject<>();

			@Override
			public void addUnderstanding(final EN_Understanding u) {
				understandings.add(u);
			}

			@Override
			public void addUsage(EN_Usage aUsage) {
				usages.add(aUsage);
			}

			@Override
			public String getText() {
				return name;
			}

			@Override
			public Promise<EN_Type, Void, Void> getType() {
				return typePromise;
			}

			@Override
			public List<EN_Understanding> getUnderstandings() {
				return understandings;
			}

			@Override
			public List<EN_Usage> getUsages() {
				return usages;
			}

			@Override
			public boolean hasUnderstanding(@NotNull Class className) {
				for (EN_Understanding und : understandings) {
					if (className.isInstance(und)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	void addUnderstanding(EN_Understanding u);

	void addUsage(EN_Usage deduceUsage);

	String getText();

	Promise<EN_Type, Void, Void> getType();

	List<EN_Understanding> getUnderstandings();

	List<EN_Usage> getUsages();

	boolean hasUnderstanding(Class className);

}
