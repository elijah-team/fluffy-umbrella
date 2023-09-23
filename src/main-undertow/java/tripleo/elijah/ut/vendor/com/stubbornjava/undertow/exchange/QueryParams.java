package tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange;

import java.util.Deque;
import java.util.Optional;

import io.undertow.server.HttpServerExchange;

public interface QueryParams {

	default Optional<String> queryParam(HttpServerExchange exchange, String name) {
		return Optional.ofNullable(exchange.getQueryParameters().get(name))
		               .map(Deque::getFirst);
	}

	default Optional<Boolean> queryParamAsBoolean(HttpServerExchange exchange, String name) {
		return queryParam(exchange, name).map(Boolean::parseBoolean);
	}

	default Optional<Integer> queryParamAsInteger(HttpServerExchange exchange, String name) {
		return queryParam(exchange, name).map(Integer::parseInt);
	}

	default Optional<Long> queryParamAsLong(HttpServerExchange exchange, String name) {
		return queryParam(exchange, name).map(Long::parseLong);
	}
}
