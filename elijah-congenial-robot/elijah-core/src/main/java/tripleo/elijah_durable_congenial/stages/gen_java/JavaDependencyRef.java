package tripleo.elijah_durable_congenial.stages.gen_java;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.util.UnintendedUseException;
import tripleo.elijah_durable_congenial.stages.gen_generic.DependencyRef;

/**
 * Created 9/13/21 4:26 AM
 */
@Data
public class JavaDependencyRef implements DependencyRef {
	private String className;
	private String fieldName; // for static fields
	private String packageName;

	@Override
	public @NotNull String jsonString() {
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
