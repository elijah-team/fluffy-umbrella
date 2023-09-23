package tripleo.elijah.stage2;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

@Value.Immutable
//@JsonSerialize(as = ImmutableVal.class)
//@JsonDeserialize(as = ImmutableVal.class)
interface Val {
	int a();
	@JsonProperty("b") String second();
}
