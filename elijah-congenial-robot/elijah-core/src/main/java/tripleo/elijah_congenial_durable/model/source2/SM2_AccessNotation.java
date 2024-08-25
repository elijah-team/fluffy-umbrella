package tripleo.elijah_congenial_durable.model.source2;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.nextgen.model.SM_Node;
import tripleo.elijah.util.UnintendedUseException;
import tripleo.elijah.xlang.LocatableString;

import java.util.List;

public record SM2_AccessNotation(
		LocatableString shortHand,
		LocatableString category,
		List<SM2_TypeName> typeNameList
) implements SM_Node {
	@Override
	public @Nullable String jsonString() {
		final Gson gson = new GsonBuilder()
				//.registerTypeAdapter(_JsonLog.class, new _JsonLog_TypeAdapter())
				.enableComplexMapKeySerialization()
				//.serializeNulls()
				//.setDateFormat(DateFormat.LONG)
				.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
				.setPrettyPrinting()
				.setVersion(1.0)
				.create();
		final String jsonString = gson.toJson(this);
		return jsonString;
	}
}
